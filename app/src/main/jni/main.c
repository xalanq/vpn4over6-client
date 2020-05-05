#include <unistd.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>

#include "com_xalanq_vpn4over6_Backend.h"
#include "log.h"

int fd;
int running;

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
            if (local_ip("13.8.0.2 0.0.0.0 202.38.120.242 8.8.8.8 202.106.0.20 %d", get_local_fd()) < 0) {
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
    int err;
    struct sockaddr_un addr;
    int local_fd;
    int count;
    socklen_t len;
    const char *name = (*env)->GetStringUTFChars(env, socket_name, JNI_FALSE);

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

    count = 0;
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
    set_local_fd(local_fd);

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
    close_local_fd();
}
