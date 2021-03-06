#include "com_xalanq_vpn4over6_Backend.h"
#include "logger.h"
#include "client.h"

JNIEXPORT int JNICALL Java_com_xalanq_vpn4over6_Backend_loggerConnect
    (JNIEnv *env, jclass thiz, jstring socket_name) {

    LOGD("%s", __FUNCTION__);
    return logger_connect((*env)->GetStringUTFChars(env, socket_name, JNI_FALSE));
}

JNIEXPORT void JNICALL Java_com_xalanq_vpn4over6_Backend_clientConnect
    (JNIEnv *env, jclass thiz, jstring ip, jint port) {

    LOGD("%s", __FUNCTION__);
    client_connect((*env)->GetStringUTFChars(env, ip, JNI_FALSE), (int)port);
}

JNIEXPORT void JNICALL Java_com_xalanq_vpn4over6_Backend_listeningServer
    (JNIEnv *env, jclass thiz) {

    LOGD("%s", __FUNCTION__);
    client_listen_server();
}

JNIEXPORT void JNICALL Java_com_xalanq_vpn4over6_Backend_listeningClient
    (JNIEnv *env, jclass thiz) {

    LOGD("%s", __FUNCTION__);
    client_listen_client();
}

JNIEXPORT void JNICALL Java_com_xalanq_vpn4over6_Backend_schedule
    (JNIEnv *env, jclass thiz) {

    LOGD("%s", __FUNCTION__);
    client_schedule();
}

JNIEXPORT void JNICALL Java_com_xalanq_vpn4over6_Backend_disconnect
    (JNIEnv *env, jclass thiz) {

    LOGD("%s", __FUNCTION__);
    client_disconnect();
    logger_disconnect();
}
