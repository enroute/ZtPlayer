#!/bin/bash -ex

echo "Configure the building toolchains properly before starting."

NDK="$HOME/ndk"
HOST='linux-x86_64'

X264='x264'
# x264 branch to checkout
X264_BRANCH='stable'

FFMPEG='ffmpeg'
# ffmpeg branch to checkout
FFMPEG_BRANCH='release/4.4'

OPENCV='opencv'
OPENCV_CONTRIB='opencv_contrib'



TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/$HOST
SYSROOT=$NDK/toolchains/llvm/prebuilt/$HOST/sysroot
API=22

TARGETPLATFORM='android'


function setup_arch() {
    arch=$1

    case $arch in
        
        arm64)
            # general
            export PREFIX=./$TARGETPLATFORM/arm64
            export TARGET=aarch64-linux-android

            # for ffmpeg
            export ARCH=aarch64
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
    arch=$1
    setup_arch $arch

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


function build_ffmpeg {
    # https://github.com/bilibili/ijkplayer/issues/4093
    # use --disable-linux-perf to fix B0 issues
    arch=$1
    setup_arch $arch

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
        --arch=aarch64 \
        --cross-prefix=$PREBUILT/bin/aarch64-linux-android- \
        --extra-ldflags="-lx264 -nostdlib -lc -lm -ldl -llog" \
        --enable-zlib \
        --enable-gpl \
        --enable-libx264 \
        --disable-linux-perf \
        --extra-cflags="-I../x264/android/arm64/include" \
        --extra-ldflags="-L../x264/android/arm64/lib"

    #make clean
    make -j4
    make install    
}


# cd $X264
# git checkout $X264_BRANCH
# build_x264 arm64
# cd ..

cd $FFMPEG
git checkout $FFMPEG_BRANCH
build_ffmpeg arm64
cd ..

# Local Variables:
# indent-tabs-mode: nil
# End:
