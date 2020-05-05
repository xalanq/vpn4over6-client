#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <sys/socket.h>
#include <sys/un.h>

#include "log.h"
#include "io.h"

int local_fd;

int get_local_fd() {
    return local_fd;
}

void close_local_fd() {
    close(local_fd);
}

int init_local_fd(const char *socket_name) {
    LOGD("connect local socket");
    int err;
    struct sockaddr_un addr;
    int count;
    socklen_t len;

    LOGD("local socket init");

    local_fd = socket(PF_LOCAL, SOCK_STREAM, 0);

    if (local_fd < 0) {
        LOGE("Cannot create local socket");
        goto fail;
    }

    addr.sun_family = AF_LOCAL;
    addr.sun_path[0] = '\0';
    strcpy(&addr.sun_path[1], socket_name);
    len = offsetof(struct sockaddr_un, sun_path) + 1 + strlen(&addr.sun_path[1]);

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

    return 0;

fail:
    err = errno;
    LOGE("init_local_fd failed: %s (%d)", strerror(err), err);
    errno = err;
    if (local_fd >= 0)
        close(local_fd);
    return -1;
}

int local_write(const char *format, ...) {
    char *buf;
    int len, c;
    va_list args;

    va_start(args, format);
    if (vasprintf(&buf, format, args) < 0)
        buf = NULL;
    va_end(args);

    if (buf != NULL) {
        LOGD("local write: %s", buf);
        int len = strlen(buf);
        if (write_all(local_fd, buf, len) < 0) {
            free(buf);
            return -1;
        }
        free(buf);
        return len;
    }
    return -1;
}

int local_read_fd() {
    int len = 10;
    char *buf = (char *)malloc(len + 1);
    if (read_all(local_fd, buf, len) < 0) {
        free(buf);
        return -1;
    }
    buf[len] = '\0';
    unsigned int a;
    sscanf(buf, "%x", &a);
    free(buf);
    LOGD("local read fd %d", a);
    return a;
}