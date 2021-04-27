#!/bin/bash -ex

echo "Configure the building toolchains properly before starting."

NDK="$HOME/ndk"
HOST='linux-x86_64'

# for opencv
export ANDROID_NDK=$NDK
export ANDROID_SDK="$HOME/sdk"

# # for building opencv, use system cmake
# export PATH=$PATH:"$HOME/sdk/cmake/3.10.2.4988404/bin"
# export CMAKE="$HOME/sdk/cmake/3.10.2.4988404/bin/cmake"


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


OPENCV_CONTRIB='opencv_contrib'
OPENCV_CONTRIB_BRANCH='4.5.2'


OPENCV='opencv'
OPENCV_BRANCH='4.5.2'



TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/$HOST
SYSROOT=$NDK/toolchains/llvm/prebuilt/$HOST/sysroot
API=22

TARGETPLATFORM='android'


function setup() {
    ABI=$1

    case $ABI in

        arm64)
            # general
            export PREFIX=$TARGETPLATFORM/arm64-v8a
            # opencv use absolute path, so install ffmpeg static libraries with absolute prefix
            export ABSOLUTE_PREFIX=$(readlink -f ".")"/"$PREFIX

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
        --prefix=$ABSOLUTE_PREFIX \
        --enable-static \
        --disable-runtime-cpudetect \
        --disable-doc \
        --disable-debug \
        --disable-ffmpeg \
        --disable-ffprobe \
        --disable-programs \
        --disable-ffplay \
        --enable-avresample \
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


function build_opencv_contrib() {
    ABI=$1
    setup $ABI

    # export LD_LIBRARY_PATH=../../$FFMPEG/$PREFIX/lib:$LID_LIBRARY_PATH
    # export PKG_CONFIG_LIBDIR=../../$FFMPEG/$PREFIX/lib/pkgconfig:$PKG_CONFIG_LIBDIR
    export PKG_CONFIG_PATH=$PKG_CONFIG_PATH:../../$FFMPEG/$PREFIX/lib/pkgconfig

    cmake -DCMAKE_BUILD_WITH_INSTALL_RPATH=ON \
          -DANDROID_NDK=$NDK \
          -DCMAKE_TOOLCHAIN_FILE="$NDK/build/cmake/android.toolchain.cmake" \
          -DANDROID_NDK=$ANDROID_NDK \
          -DANDROID_SDK_ROOT=$ANDROID_SDK \
          -DANDROID_NATIVE_API_LEVEL=30 \
          -DANDROID_ABI=arm64-v8a \
          -DWITH_FFMPEG=ON \
          -DOPENCV_FFMPEG_SKIP_BUILD_CHECK=ON \
          -DWITH_CUDA=ON \
          -DWITH_MATLAB=OFF \
          -DBUILD_SHARED_LIBS=OFF \
          -DBUILD_ANDROID_EXAMPLES=OFF \
          -DBUILD_DOCS=OFF \
          -DBUILD_PERF_TESTS=OFF \
          -DBUILD_TESTS=OFF \
          -DOPENCV_EXTRA_MODULES_PATH="../../$OPENCV_CONTRIB/modules/"  \
          -DCMAKE_INSTALL_PREFIX=../$PREFIX \
          ..

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

# cd $FFMPEG
# git checkout $FFMPEG_BRANCH
# build_ffmpeg arm64
# cd ..

cd $OPENCV_CONTRIB
git checkout $OPENCV_CONTRIB_BRANCH
mkdir -p ../$OPENCV/build
cd ../$OPENCV/build
git checkout $OPENCV_BRANCH
build_opencv_contrib arm64
cd ..

# Local Variables:
# indent-tabs-mode: nil
# End:
