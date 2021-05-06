#include <jni.h>
#include <string>
#include <opencv2/core.hpp>

extern "C" JNIEXPORT jstring JNICALL
Java_com_ztfun_ztplayer_MainActivity_stringFromJNI(JNIEnv* env, jobject /* this */) {
    std::string hello = "Hello from C++";

    cv::Mat display_mat;

    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_ztfun_ztplayer_CameraActivity_setupSurface(JNIEnv* env, jobject /* Surface */) {
    // todo:
}

extern "C" JNIEXPORT void JNICALL
Java_com_ztfun_ztplayer_CameraActivity_flipCamera(JNIEnv* env) {
    // todo:
}

