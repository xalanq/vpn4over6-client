#include <pthread.h>

#define MSG_IP_REQUEST 100
#define MSG_IP_RESPONSE 101
#define MSG_NET_REQUEST 102
#define MSG_NET_RESPONSE 103
#define MSG_KEEP_ALIVE 104

struct Msg {
    int length;
    char type;
    char data[4096];
};

int msg_read(int fd, struct Msg *msg);
int msg_write(int fd, struct Msg *msg);
int msg_write_safe(int fd, struct Msg *msg, pthread_mutex_t *mutex);
