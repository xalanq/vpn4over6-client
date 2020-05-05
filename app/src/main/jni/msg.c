#include "msg.h"
#include "io.h"

int read_msg(int fd, struct Msg *msg) {
    if (read_all(fd, msg, 5) < 0)
        return -1;
    if (read_all(fd, ((char *)msg) + 5, msg->length - 5) < 0)
        return -1;
    return msg->length;
}

int write_msg(int fd, struct Msg *msg) {
    return write_all(fd, msg, msg->length);
}