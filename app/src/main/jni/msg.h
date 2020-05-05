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

int read_msg(int fd, struct Msg *msg);
int write_msg(int fd, struct Msg *msg);
