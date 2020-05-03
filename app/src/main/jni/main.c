#include "com_xalanq_vpn4over6_Backend.h"

#include <unistd.h>
#include <android/log.h>
#include <stdio.h>
#include <fcntl.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>

#define  LOG_TAG "backend"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define TYPE_OFF 0
#define TYPE_LOG 1
#define TYPE_IP 2

int local_fd;
int running;

int local_write_msg(int type, const char *msg) {
    char *buf = (char *)malloc(strlen(msg) + 3 + 2);
    sprintf(buf, "%d %s\n", type, msg);
    LOGD("write message: %s", buf);
    int len = strlen(buf);
    int c = 0;
    while (c < len) {
        int t = write(local_fd, buf + c, len - c);
        if (t < 0) {
            return -1;
        }
        c += t;
    }
    return c;
}

int local_off(const char *msg) {
    return local_write_msg(TYPE_OFF, msg);
}

int local_log(const char *msg) {
    return local_write_msg(TYPE_LOG, msg);
}

int local_ip(const char *msg) {
    return local_write_msg(TYPE_IP, msg);
}

JNIEXPORT jint JNICALL Java_com_xalanq_vpn4over6_Backend_serve(JNIEnv *env, jclass thiz, jstring ip, jint port) {
    LOGD("serve");
    running = 1;
    int c = 0;
    while (running) {
        if (c < 3) {
            if (local_log("test") < 0) {
                return -1;
            }
            sleep(1);
        } else if (c == 4) {
            if (local_ip("13.8.0.2 0.0.0.0 202.38.120.242 8.8.8.8 202.106.0.20") < 0) {
                return -1;
            }
            sleep(1);
        } else if (c == 5) {
            if (local_off("超时啦") < 0) {
                return -1;
            }
            break;
        }
        c++;
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
