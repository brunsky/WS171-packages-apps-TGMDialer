LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := user

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_SRC_FILES := $(filter-out gen/com/camangi/android/TGMDialer/R.java, $(LOCAL_SRC_FILES))

LOCAL_PACKAGE_NAME := TGMDialer

LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)
