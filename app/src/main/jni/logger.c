#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <sys/socket.h>
#include <sys/un.h>

#include "logger.h"
#include "io.h"

static int fd;

int logger_connect(const char *socket_name) {
    LOGD("connect logger socket");
    int err;
    struct sockaddr_un addr;
    int count;
    socklen_t len;

    LOGD("logger socket init");

    fd = socket(PF_LOCAL, SOCK_STREAM, 0);

    if (fd < 0) {
        LOGE("Cannot create logger socket");
        goto fail;
    }

    LOGD("Created logger socket %d", fd);

    addr.sun_family = AF_LOCAL;
    addr.sun_path[0] = '\0';
    strcpy(&addr.sun_path[1], socket_name);
    len = offsetof(struct sockaddr_un, sun_path) + 1 + strlen(&addr.sun_path[1]);

    usleep(100000);

    count = 0;
    while (count < 5) {
        LOGD("logger socket connect try %d", count);
        if (connect(fd, (struct sockaddr *) &addr, len) < 0) {
            count++;
            LOGD("sleep 1");
            sleep(1);
        }
        break;
    }
    if (count == 5) {
        goto fail;
    }
    LOGD("logger socket connect successfully!");

    return 0;

fail:
    err = errno;
    LOGE("%s failed: %s (%d)\n", __FUNCTION__, strerror(err), err);
    errno = err;
    if (fd >= 0)
        close(fd);
    return -1;
}

void logger_disconnect() {
    close(fd);
}

int logger_write(const char *format, ...) {
    char *buf;
    int len, c;
    va_list args;

    va_start(args, format);
    if (vasprintf(&buf, format, args) < 0)
        buf = NULL;
    va_end(args);

    if (buf != NULL) {
        LOGD("logger %d write: %s", fd, buf);
        int len = strlen(buf);
        if (write_all(fd, buf, len) < 0) {
            free(buf);
            return -1;
        }
        free(buf);
        return len;
    }
    return -1;
}

int logger_fd() {
    return fd;
}

int logger_read_fd() {
    int len = 10;
    char *buf = (char *)malloc(len + 1);
    if (read_all(fd, buf, len) < 0) {
        free(buf);
        return -1;
    }
    buf[len] = '\0';
    unsigned int a;
    sscanf(buf, "%x", &a);
    free(buf);
    LOGD("logger read fd %d", a);
    return a;
}