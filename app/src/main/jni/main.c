#include "com_xalanq_vpn4over6_Backend.h"

#include <unistd.h>
#include <android/log.h>
#include <stdio.h>
#include <fcntl.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>

#define  LOG_TAG "backend"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define TYPE_CLOSE = 0;
#define TYPE_LOG = 1;
#define TYPE_IP = 2;

char *SOCKET_NAME;
int local_fd;
int running;

JNIEXPORT jint JNICALL Java_com_xalanq_vpn4over6_Backend_serve(JNIEnv *env, jclass thiz, jstring ip, jint port) {
    LOGD("serve");
    running = 1;
    int c = 0;
    while (running) {
        sleep(1);
        LOGD("xalanq test");
        char buf[15] = "1 test\n";
        int r = write(local_fd, buf, strlen(buf) * sizeof(char));
        if (r < 0) {
            return -1;
        }
        c++;
        if (c >= 5) {
            return -1;
        }
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_com_xalanq_vpn4over6_Backend_connectLocalSocket(JNIEnv *env, jclass thiz, jstring socket_name) {
    LOGD("connect local socket");
    const char *name = (*env)->GetStringUTFChars(env, socket_name, JNI_FALSE);

    int err;
    struct sockaddr_un addr;
    socklen_t len;
    addr.sun_family = AF_LOCAL;
    addr.sun_path[0] = '\0';
    strcpy(&addr.sun_path[1], name);
    len = offsetof(struct sockaddr_un, sun_path) + 1 + strlen(&addr.sun_path[1]);

    LOGD("socket init");
    local_fd = socket(PF_LOCAL, SOCK_STREAM, 0);

    if (local_fd < 0) {
        goto fail;
    }

    usleep(100000);

    int count = 0;
    while (count < 5) {
        LOGD("socket connect try %d", count);
        if (connect(local_fd, (struct sockaddr *) &addr, len) < 0) {
            count++;
            sleep(1);
        }
        break;
    }
    if (count == 5) {
        goto fail;
    }
    LOGD("socket connect successfully!");

    return 0;

fail:
    err = errno;
    LOGE("%s: connect() failed: %s (%d)\n",
        __FUNCTION__, strerror(err), err);
    errno = err;
    return -1;
}

JNIEXPORT void JNICALL Java_com_xalanq_vpn4over6_Backend_stop (JNIEnv *env, jclass thiz) {
    LOGD("stop");
    running = 0;
}
