#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>
#include <time.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/un.h>

#include "io.h"
#include "msg.h"
#include "logger.h"
#include "client.h"

static int fd;
static int tun_fd;
static int running;

void client_connect(const char *ip, int port) {
    int err;
    struct sockaddr_in6 addr;
    int tries = 0;

    logger_log("开始连接服务器 [%s]:%d", ip, port);

    fd = socket(PF_INET6, SOCK_STREAM, 0);
    if (fd < 0) {
        logger_off("无法创建 IPv6 socket");
        goto fail;
    }
    // 设置非阻塞模式，让我们自己来分配时间片
    // fcntl(fd, F_SETFL, fcntl(fd, F_GETFL) | O_NONBLOCK);

    addr.sin6_family = AF_INET6;
    inet_pton(AF_INET6, ip, &addr.sin6_addr);
    addr.sin6_port = htons(port);

    if (connect(fd, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
        logger_off("无法连接到 IPv6 服务器");
        goto fail;
    }

    logger_log("连接服务器连接成功！");

    struct Msg msg;
    msg.length = 5;
    msg.type = MSG_IP_REQUEST;

    if (msg_write(fd, &msg) < 0) {
        logger_off("无法发出 IP 请求");
        goto fail;
    }

    do {
        if (msg_read(fd, &msg) < 0) {
            logger_off("读取数据包出错");
            goto fail;
        }
        LOGD("packet len: %d, type %d", msg.length, msg.type);
        if (++tries >= 5) {
            logger_off("服务器发的 5 个包都不是 IP 包诶");
            goto fail;
        }
    } while (msg.type != MSG_IP_RESPONSE);

    msg.data[msg.length - 5] = '\0';
    logger_ip("%s %d", msg.data, fd);
    tun_fd = logger_read_fd();
    if (tun_fd < 0) {
        logger_off("/dev/tun socket获取失败");
        goto fail;
    }
    running = 1;
    logger_run("开始收发数据！");

    return;

fail:
    err = errno;
    LOGE("%s failed: %s (%d)\n", __FUNCTION__, strerror(err), err);
    errno = err;
}

void client_disconnect() {
    running = 0;
    close(fd);
}

time_t lastTime;

void client_listen_server() {
    int err;
    struct Msg msg;

    while (running) {
        if (msg_read(fd, &msg) < 0) {
            logger_off("读取服务器发来的数据包出错");
            goto fail;
        }
        LOGD("packet recv len: %d, type %d", msg.length, msg.type);
        switch (msg.type) {
        case MSG_NET_RESPONSE:
            if (write_all(tun_fd, msg.data, msg.length - 5) < 0) {
                logger_off("写入 /dev/tun 出错");
                goto fail;
            }
            break;
        case MSG_KEEP_ALIVE:
            lastTime = time(NULL);
            logger_log("收到一个心跳包");
            break;
        }
    }

    return;

fail:
    err = errno;
    LOGE("%s failed: %s (%d)\n", __FUNCTION__, strerror(err), err);
    errno = err;
}

void client_listen_client() {
    int err;
    struct Msg msg;
    msg.type = MSG_NET_REQUEST;

    while (running) {
        msg.length = read(tun_fd, msg.data, (sizeof(struct Msg)) - 5);
        if (msg.length < 0) {
            logger_off("读取本地发送的数据包出错");
            goto fail;
        }
        if (msg.length == 0) {
            sleep(0);
        } else {
            msg.length += 5;
            if (msg_write(fd, &msg) < 0) {
                logger_off("发送数据包失败");
                goto fail;
            }
            LOGD("packet send len: %d, type %d", msg.length, msg.type);
        }
    }

    return;

fail:
    err = errno;
    LOGE("%s failed: %s (%d)\n", __FUNCTION__, strerror(err), err);
    errno = err;
}

void client_schedule() {
    int err;
    struct Msg msg;
    time_t now;
    msg.type = MSG_KEEP_ALIVE;

    while (running) {
        sleep(20);
        if (msg_write(fd, &msg) < 0) {
            logger_off("发送心跳包失败");
            goto fail;
        }
        sleep(20);
        if (msg_write(fd, &msg) < 0) {
            logger_off("发送心跳包失败");
            goto fail;
        }
        sleep(20);
        now = time(NULL);
        if (now - lastTime > 60) {
            logger_off("连接超时");
            goto fail;
        }
        if (msg_write(fd, &msg) < 0) {
            logger_off("发送心跳包失败");
            goto fail;
        }
    }

    return;

fail:
    err = errno;
    LOGE("%s failed: %s (%d)\n", __FUNCTION__, strerror(err), err);
    errno = err;
}
