#include <android/log.h>

#define LOG_TAG "backend"

#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define TYPE_OFF 0
#define TYPE_LOG 1
#define TYPE_IP 2

#define local_off(format, arg...) local_write("%d " format "\n", TYPE_OFF, ##arg)
#define local_log(format, arg...) local_write("%d " format "\n", TYPE_LOG, ##arg)
#define local_ip(format, arg...) local_write("%d " format "\n", TYPE_IP, ##arg)

int get_local_fd();
void set_local_fd(int fd);
void close_local_fd();
int local_write(const char *format, ...);
int local_read_fd();
