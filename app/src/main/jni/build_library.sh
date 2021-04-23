#!/bin/bash -ex

echo "Configure the building toolchains properly before starting."

NDK="$HOME/ndk"
HOST='linux-x86_64'


X264='x264'
# x264 branch to checkout
X264_BRANCH='stable'


# openssl 
OPENSSL='openssl'
# openssl branch to checkout
# branch 1.1.1 stable fails with NDK r22
# https://github.com/openssl/openssl/pull/13694
# OPENSSL_BRANCH='OpenSSL_1_1_1-stable'
OPENSSL_BRANCH='master'


FFMPEG='ffmpeg'
# ffmpeg branch to checkout
FFMPEG_BRANCH='release/4.4'


OPENCV='opencv'
OPENCV_CONTRIB='opencv_contrib'



TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/$HOST
SYSROOT=$NDK/toolchains/llvm/prebuilt/$HOST/sysroot
API=22

TARGETPLATFORM='android'


function setup() {
    ABI=$1

    case $ABI in
        
        arm64)
            # general
            export PREFIX=./$TARGETPLATFORM/arm64-v8a
            export ARCH=aarch64
            export TARGET=$ARCH-linux-android
            ;;

        *)
            echo "Unknown arch: $arch"
            exit
            ;;
        
    esac

    # general
    export AR=$TOOLCHAIN/bin/$TARGET-ar
    export CC=$TOOLCHAIN/bin/$TARGET$API-clang
    export CXX=$TOOLCHAIN/bin/$TARGET$API-clang++
    # use gold version of ld
    # export LD=$TOOLCHAIN/bin/$TARGET-ld.gold
    export LD=$TOOLCHAIN/bin/ld
    export RANLIB=$TOOLCHAIN/bin/$TARGET-ranlib
    export STRIP=$TOOLCHAIN/bin/$TARGET-strip

    # ffmpeg
    export NM=$TOOLCHAIN/bin/$TARGET-nm
}


function build_x264() {
    ABI=$1
    setup $ABI

  ./configure \
      --prefix=$PREFIX \
      --enable-static \
      --enable-pic \
      --host=aarch64-linux \
      --cross-prefix=$TOOLCHAIN/bin/$TARGET- \
      --sysroot=$SYSROOT

  make
  make install
}


function build_openssl() {
    ABI=$1
    #setup $ABI

    export ANDROID_NDK_HOME="$NDK"
    PATH=$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin:$ANDROID_NDK_HOME/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin:$PATH
    # openssl requires absolute path for --prefix
    PREFIX=$(readlink -f ".")"/android/$ABI"
    ./Configure android-$ABI -D__ANDROID_API__=29 -no-shared --prefix=$PREFIX -DOPENSSL_NO_DEPRECATED_1_1_0
    make
    make install
}


function build_ffmpeg {
    # https://github.com/bilibili/ijkplayer/issues/4093
    # use --disable-linux-perf to fix B0 issues
    ABI=$1
    setup $ABI

    export PKG_CONFIG_PATH="../openssl/android/amd64/lib/pkgconfig"
    ./configure \
        --target-os=android \
        --prefix=$PREFIX \
        --enable-static \
        --disable-runtime-cpudetect \
        --disable-doc \
        --disable-debug \
        --disable-ffmpeg \
        --disable-ffprobe \
        --disable-programs \
        --disable-ffplay \
        --enable-pic \
        --target-os=linux \
        --cross-prefix=$TOOLCHAIN/bin/$TARGET- \
        --sysroot=$SYSROOT \
        --cc=$CC \
        --nm=$NM \
        --ar=$AR \
        --strip=$STRIP \
        --ranlib=$RANLIB \
        --pkg-config=`(which pkg-config)` \
        --enable-small \
        --enable-cross-compile \
        --extra-libs="-lgcc" \
        --arch=$ARCH \
        --cross-prefix=$PREBUILT/bin/aarch64-linux-android- \
        --extra-ldflags="-lx264 -nostdlib -lc -lm -ldl -llog" \
        --enable-zlib \
        --enable-gpl \
        --enable-nonfree \
        --enable-openssl \
        --enable-libx264 \
        --disable-linux-perf \
        --extra-cflags="-I../x264/android/arm64/include -I../$OPENSSL/android/arm64/include" \
        --extra-ldflags="-L../x264/android/arm64/lib -L../$OPENSSL/android/arm64/lib -lssl -lcrypto"

    #make clean
    make -j4
    make install    
}


# cd $X264
# git checkout $X264_BRANCH
# build_x264 arm64
# cd ..

# cd $OPENSSL
# git checkout $OPENSSL_BRANCH
# build_openssl arm64
# cd ..


cd $FFMPEG
git checkout $FFMPEG_BRANCH
build_ffmpeg arm64
cd ..

# Local Variables:
# indent-tabs-mode: nil
# End:
