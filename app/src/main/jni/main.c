#include "com_xalanq_vpn4over6_Backend.h"
#include "logger.h"
#include "client.h"

JNIEXPORT jint JNICALL Java_com_xalanq_vpn4over6_Backend_clientConnect
    (JNIEnv *env, jclass thiz, jstring ip, jint port) {

    LOGD("%s", __FUNCTION__);
    return client_connect((*env)->GetStringUTFChars(env, ip, JNI_FALSE), (int)port);
}

JNIEXPORT jint JNICALL Java_com_xalanq_vpn4over6_Backend_loggerConnect
    (JNIEnv *env, jclass thiz, jstring socket_name) {

    LOGD("%s", __FUNCTION__);
    return logger_connect((*env)->GetStringUTFChars(env, socket_name, JNI_FALSE));
}

JNIEXPORT jint JNICALL Java_com_xalanq_vpn4over6_Backend_schedule
    (JNIEnv *env, jclass thiz) {
    return 0;
}

JNIEXPORT jint JNICALL Java_com_xalanq_vpn4over6_Backend_listeningServer
    (JNIEnv *env, jclass thiz) {

    LOGD("%s", __FUNCTION__);
    return 0;
}

JNIEXPORT jint JNICALL Java_com_xalanq_vpn4over6_Backend_listeningClient
    (JNIEnv *env, jclass thiz) {

    LOGD("%s", __FUNCTION__);
    return 0;
}

JNIEXPORT void JNICALL Java_com_xalanq_vpn4over6_Backend_disconnect
    (JNIEnv *env, jclass thiz) {

    LOGD("%s", __FUNCTION__);
    client_disconnect();
    logger_disconnect();
}

