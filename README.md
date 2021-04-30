# ZtPlayer
Android video player based on ffmpeg

## clone with recursive
```
git clone --recursive git@github.com/xxx.git
```

## build jni libraries
### prerequisites
Install pkg-config
```
sudo apt-get install pkg-config build-essentials
```

```
cd app/src/main/jni
./build_library.sh
```

## Important!!
Most probably, something similar to the following error might occur when running
`./gradlew build` to build the app:
```
/home/yourname/sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/lib/gcc/aarch64-linux-android/4.9.x/../../../../aarch64-linux-android/bin/ld: cannot find -lavresample
/home/yourname/sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/lib/gcc/aarch64-linux-android/4.9.x/../../../../aarch64-linux-android/bin/ld: cannot find -lavutil
```

More info can be found through the prompt:
```
ninja: Entering directory `/home/yourname/ztplayer/app/.cxx/cmake/debug/arm64-v8a'
```

The reason might be that the link parameters of ffmpeg related libraries (injected by opencv? not sure) is just
like `-lavsample -lavresample`. However, the linker can't find the related `libavutil.so` and  `libavsample.so`.
So, after building the underlying libraries, the ffmpeg libraries might be required to be copied or link to:
```
sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/lib/aarch64-linux-android/26/
```
According to the `./gradlew build` error message. E.g.

```
cd ~/sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/lib/aarch64-linux-android/26/
ln ~/path/to/your/ffmpeg/lib/libavutil.so .
ln ~/path/to/your/ffmpeg/lib/libavresample.so .
```

## build app
After jni libraries are built, the app can be built with
```
./gradlew build
```

