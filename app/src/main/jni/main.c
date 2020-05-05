#include <unistd.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/un.h>

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
    const char *name = (*env)->GetStringUTFChars(env, socket_name, JNI_FALSE);
    return init_local_fd(name);
}

JNIEXPORT void JNICALL Java_com_xalanq_vpn4over6_Backend_stop (JNIEnv *env, jclass thiz) {
    LOGD("stop");
    running = 0;
    close_local_fd();
}
