# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("ztplayer")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             ffmpeg

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             native-lib.cpp )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

# add_library(avcodec-lib STATIC IMPORTED)
# set_target_properties(avcodec-lib PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/ffmpeg/android/${ANDROID_ABI}/lib/libavdevice.a)

# add_library(avfilter-lib STATIC IMPORTED)
# set_target_properties(avfilter-lib PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/ffmpeg/android/${ANDROID_ABI}/lib/libavfilter.a)

# add_library(avformat-lib STATIC IMPORTED)
# set_target_properties(avformat-lib PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/ffmpeg/android/${ANDROID_ABI}/lib/libavformat.a)

# add_library(avutil-lib STATIC IMPORTED)
# set_target_properties(avutil-lib PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/ffmpeg/android/${ANDROID_ABI}/lib/libavutil.a)

# add_library(postproc-lib STATIC IMPORTED)
# set_target_properties(postproc-lib PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/ffmpeg/android/${ANDROID_ABI}/lib/libpostproc.a)

# add_library(swresample-lib STATIC IMPORTED)
# set_target_properties(swresample-lib PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/ffmpeg/android/${ANDROID_ABI}/lib/libswresample.a)

# add_library(swscale-lib STATIC IMPORTED)
# set_target_properties(swscale-lib PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/ffmpeg/android/${ANDROID_ABI}/lib/libswscale.a)

include_directories(${CMAKE_SOURCE_DIR}/ffmpeg/android/${ANDROID_ABI}/include/)

set(OpenCV_DIR ${CMAKE_CURRENT_SOURCE_DIR}/opencv/android/${ANDROID_ABI}/sdk/native/jni)
find_package(OpenCV REQUIRED)
#include_directories(${OpenCV_INCLUDE_DIRS}) # Not needed for CMake >= 2.8.11
#target_link_libraries(MY_TARGET_NAME ${OpenCV_LIBS})

# add_library(avutil.so SHARED IMPORTED)
# set_target_properties(avutil.so PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/ffmpeg/android/${ANDROID_ABI}/lib/libavutil.so)

#link_directories("${CMAKE_SOURCE_DIR}/ffmpeg/android/${ANDROID_ABI}/lib/")
LINK_LIBRARIES("${CMAKE_SOURCE_DIR}/ffmpeg/android/${ANDROID_ABI}/lib/libavutil.a")

# for native camera API
find_library( camera-lib camera2ndk )
find_library( media-lib mediandk )
find_library( android-lib android )
find_library( gl-lib GLESv2 )

target_link_libraries( # Specifies the target library.
                       ffmpeg

                       # Links the target library to the log library
                       # included in the NDK.
		       # avcodec-lib
		       # avfilter-lib
		       # avformat-lib
		       # avutil-lib
		       # postproc-lib
		       # swresample-lib
		       # swscale-lib
		       # avutil.so
		       "/home/mario/ztplayer/app/src/main/jni/ffmpeg/android/arm64-v8a/lib/libavutil.so"
		       "${CMAKE_SOURCE_DIR}/ffmpeg/android/${ANDROID_ABI}/lib/libavutil.a"
		       ${OpenCV_LIBS}
		       ${camera-lib} ${media-lib} ${android-lib} ${gl-lib}
                       ${log-lib} )
