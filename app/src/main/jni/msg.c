#include "msg.h"
#include "io.h"

int msg_read(int fd, struct Msg *msg) {
    if (read_all(fd, msg, 5) != 0)
        return -1;
    if (read_all(fd, ((char *)msg) + 5, msg->length - 5) != 0)
        return -1;
    return 0;
}

int msg_write(int fd, struct Msg *msg) {
    return write_all(fd, msg, msg->length);
}

int msg_write_safe(int fd, struct Msg *msg, pthread_mutex_t *mutex) {
    if (pthread_mutex_lock(mutex) != 0)
        return -1;
    int r = write_all(fd, msg, msg->length);
    pthread_mutex_unlock(mutex);
    return r;
}