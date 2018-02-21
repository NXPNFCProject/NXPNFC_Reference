LOCAL_PATH:= $(call my-dir)

########################################
# com.nxp.nfc - library
########################################
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := com.nxp.nfc
LOCAL_REQUIRED_MODULES := com.nxp.nfc.xml
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
 # Install to system/frameworks
LOCAL_MODULE_PATH := $(TARGET_OUT_JAVA_LIBRARIES)

LOCAL_SRC_FILES := \
	$(call all-Iaidl-files-under, com) \
	$(call all-java-files-under, com) \
	$(call all-java-files-under, android)

LOCAL_CERTIFICATE := platform

include $(BUILD_JAVA_LIBRARY)
# ====  permissions ========================
include $(CLEAR_VARS)

LOCAL_MODULE := com.nxp.nfc.xml

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE_CLASS := ETC

# Install to /system/etc/permissions
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions

LOCAL_SRC_FILES := $(LOCAL_MODULE)

include $(BUILD_PREBUILT)

# the documentation
# ============================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	$(call all-Iaidl-files-under, com) \
	$(call all-java-files-under, com) \
	$(call all-html-files-under, com) \
	$(call all-java-files-under, android) \
	$(call all-html-files-under, android)

LOCAL_MODULE:= com.nxp.nfc
LOCAL_JAVA_LIBRARIES:= com.nxp.nfc
LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_DROIDDOC_USE_STANDARD_DOCLET := true

include $(BUILD_DROIDDOC)

# uncomment for NXP gsma-nfc-service
# ============================================================
#include $(call all-makefiles-under,$(LOCAL_PATH))
