LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := corevpn
LOCAL_SRC_FILES := main.c logger.c client.c msg.c io.c
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)