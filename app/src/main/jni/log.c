#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>

#include "log.h"

int local_fd;

int get_local_fd() {
    return local_fd;
}

void set_local_fd(int fd) {
    local_fd = fd;
}

void close_local_fd() {
    close(local_fd);
}

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