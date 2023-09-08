LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-subdir-Java-files)
LOCAL_PACKAGE_NAME := com.example.multimediav2
LOCAL_CERTIFICATE := platform
include $(BUILD_PACKAGE)