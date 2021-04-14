#!/bin/bash -ex

# checkout libaries
#git clone https://code.videolan.org/videolan/x264.git
#git clone https://git.ffmpeg.org/ffmpeg.git
#git clone https://github.com/opencv/opencv.git
#git clone https://github.com/opencv/opencv_contrib.git

NDK='/c/Users/800457/AppData/Local/Android/Sdk/ndk/22.1.7171670'
X264='x264'
FFPMEG='ffmpeg'
OPENCV='opencv'
OPENCV_CONTRIB='opencv_contrib'

echo $NDK

cd $X264
build_x264

function build_x264 {

}
