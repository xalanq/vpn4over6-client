#include <android/log.h>

#define LOG_TAG "backend"

#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define TYPE_OFF 0
#define TYPE_LOG 1
#define TYPE_IP 2
#define TYPE_RUN 3
#define TYPE_STAT 4

#define logger_off(format, arg...) logger_write("%d " format "\n", TYPE_OFF, ##arg)
#define logger_log(format, arg...) logger_write("%d " format "\n", TYPE_LOG, ##arg)
#define logger_ip(format, arg...) logger_write("%d " format "\n", TYPE_IP, ##arg)
#define logger_run(format, arg...) logger_write("%d " format "\n", TYPE_RUN, ##arg)
#define logger_stat(format, arg...) logger_write("%d " format "\n", TYPE_STAT, ##arg)

int logger_connect(const char *socket_name);
void logger_disconnect();
int logger_write(const char *format, ...);
int logger_fd();
int logger_read_fd();
