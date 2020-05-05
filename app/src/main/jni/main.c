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
int fd;
int running;

#define local_off(format, arg...) local_write("%d " format "\n", TYPE_OFF, ##arg)
#define local_log(format, arg...) local_write("%d " format "\n", TYPE_LOG, ##arg)
#define local_ip(format, arg...) local_write("%d " format "\n", TYPE_IP, ##arg)

int local_write(const char *format, ...) {
    char* buf;
    int len, c;
    va_list args;

    va_start(args, format);
    if (vasprintf(&buf, format, args) < 0)
        buf = NULL;
    va_end(args);

    if (buf != NULL) {
        LOGD("local write: %s", buf);
        len = strlen(buf);
        c = 0;
        while (c < len) {
            int t = write(local_fd, buf + c, len - c);
            if (t < 0) {
                free(buf);
                return -1;
            }
            c += t;
        }
        free(buf);
        return c;
    }
    return -1;
}

int local_read_fd() {
    int len = 10;
    int c = 0;
    char *buf = (char *)malloc(len + 1);
    while (c < len) {
        LOGD("read c: %d, len: %d", c, len);
        int t = read(local_fd, buf + c, len - c);
        if (t < 0) {
            free(buf);
            return -1;
        }
        c += t;
    }
    buf[len] = '\0';
    unsigned int a;
    sscanf(buf, "%x", &a);
    free(buf);
    return a;
}

JNIEXPORT jint JNICALL Java_com_xalanq_vpn4over6_Backend_serve(JNIEnv *env, jclass thiz, jstring _ip, jint _port) {
    const char *ip = (*env)->GetStringUTFChars(env, _ip, JNI_FALSE);
    int port = (int)_port;

    LOGD("serve");
    local_log("开始连接服务器 [%s]:%d", ip, port);

    running = 1;
    int c = 0;
    while (running) {
        if (c < 3) {
            if (local_log("test") < 0) {
                return -1;
            }
            sleep(1);
        } else if (c == 4) {
            if (local_ip("13.8.0.2 0.0.0.0 202.38.120.242 8.8.8.8 202.106.0.20 %d", local_fd) < 0) {
                return -1;
            }
            sleep(1);
        } else if (c == 5) {
            if ((fd = local_read_fd()) < 0) {
                return -1;
            }
            LOGD("fd: %d", fd);
            sleep(1);
        } else if (c == 6) {
            if (local_off("超时啦") < 0) {
                return 0;
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

    LOGD("local socket init");
    local_fd = socket(PF_LOCAL, SOCK_STREAM, 0);

    if (local_fd < 0) {
        goto fail;
    }

    usleep(100000);

    int count = 0;
    while (count < 5) {
        LOGD("local socket connect try %d", count);
        if (connect(local_fd, (struct sockaddr *) &addr, len) < 0) {
            count++;
            LOGD("sleep 1");
            sleep(1);
        }
        break;
    }
    if (count == 5) {
        goto fail;
    }
    LOGD("local socket connect successfully!");

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
    close(local_fd);
}
