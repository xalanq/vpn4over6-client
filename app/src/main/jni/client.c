#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/un.h>

#include "msg.h"
#include "logger.h"
#include "client.h"

static int fd;
static int running;

int client_connect(const char *ip, int port) {
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
    //fcntl(fd, F_SETFL, fcntl(fd, F_GETFL) | O_NONBLOCK);

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

    return 0;

fail:
    err = errno;
    LOGE("%s: serve failed: %s (%d)\n",
        __FUNCTION__, strerror(err), err);
    errno = err;
    if (fd >= 0)
        close(fd);
    return 0;
}

void client_disconnect() {
    running = 0;
    close(fd);
}