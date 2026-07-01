#####
##### NXP NFC Device Configuration makefile
######

NXP_NFC_HOST := $(TARGET_PRODUCT)
ifndef TARGET_NXP_NFC_HW
NXP_NFC_HW := SN220
else
NXP_NFC_HW := $(TARGET_NXP_NFC_HW)
endif
NXP_NFC_PLATFORM := SNxxx
NXP_VENDOR_DIR := nxp
KM_VER_DEFAULT ?= KM300
NON_SE_HW := PN557 CTN820

# Variable to enable/disable power tracker feature.
# By default this is disabled. This can be enabled by either by changing below value to true
# or can be specified in make command like "make -j8 POWER_TRACKER_FEATURE=true"
POWER_TRACKER_FEATURE ?= false

# NFC config files
ifeq ($(NXP_NFC_HW),SN1xx)
PRODUCT_COPY_FILES += \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp-SN100U_example.conf:vendor/etc/libnfc-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libese-nxp-P73.conf:vendor/etc/libese-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp_RF-SN100U_example_IguanaLite.conf:vendor/libnfc-nxp_RF.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/hal_uuid_map_config.xml:vendor/etc/hal_uuid_map_config.xml
else ifeq ($(NXP_NFC_HW),SN220)
PRODUCT_COPY_FILES += \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp-SN220U_example.conf:vendor/etc/libnfc-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libese-nxp-P73.conf:vendor/etc/libese-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp_RF-SN220U_example.conf:vendor/libnfc-nxp_RF.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/hal_uuid_map_config.xml:vendor/etc/hal_uuid_map_config.xml
else ifeq ($(NXP_NFC_HW),PN557)
PRODUCT_COPY_FILES += \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp-PN557_example.conf:vendor/etc/libnfc-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp_RF-PN557_example.conf:vendor/libnfc-nxp_RF.conf
else ifeq ($(NXP_NFC_HW),SN300)
PRODUCT_COPY_FILES += \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp-SN300U_example.conf:vendor/etc/libnfc-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libese-nxp-P73.conf:vendor/etc/libese-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp_RF-SN300U_example.conf:vendor/libnfc-nxp_RF.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/hal_uuid_map_config.xml:vendor/etc/hal_uuid_map_config.xml
else ifeq ($(NXP_NFC_HW),CTN820)
PRODUCT_COPY_FILES += \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp-CTN820_example.conf:vendor/etc/libnfc-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp_RF-CTN820_example.conf:vendor/libnfc-nxp_RF.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nci.conf:product/etc/libnfc-nci.conf
endif

# NFC Init Files
PRODUCT_COPY_FILES += \
     vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/init.$(NXP_NFC_PLATFORM).nfc.rc:vendor/etc/init/init.$(NXP_NFC_HOST).nfc.rc

ifneq ($(filter-out $(NON_SE_HW),$(NXP_NFC_HW)),)
PRODUCT_COPY_FILES += \
     vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/init.$(NXP_NFC_PLATFORM).se.rc:vendor/etc/init/init.$(NXP_NFC_HOST).se.rc
endif

# jar packages
ifeq ($(NXP_NFC_HW),CTN820)
PRODUCT_PACKAGES += \
    com.nxp.wlc \
    jrcpProxyVolta \
    WlcTestApp
else
PRODUCT_PACKAGES += \
    com.nxp.nfc \
    com.nxp.osu \
    com.nxp.sems \
    com.nxp.mfckeyverifier
endif

#ADD HAL Ext libraries
ifeq ($(NXP_NFC_HW),CTN820)
PRODUCT_PACKAGES += \
    libwlc_vendor_extn_vnd
endif

#ADD ALL HAL Services
PRODUCT_PACKAGES += \
    android.hardware.nfc2-service.nxp \

ifneq ($(filter-out $(NON_SE_HW),$(NXP_NFC_HW)),)
PRODUCT_PACKAGES += \
    android.hardware.secure_element-service.nxp \
    android.hardware.trusted_se@1.2-service \
    android.hardware.authsecret-service.nxp
endif

ifeq ($(NXP_NFC_HW),SN1xx)
# ADD keymaster HAL for SN1xx
PRODUCT_PACKAGES += \
android.hardware.keymaster@4.1-javacard.service \

else ifneq ($(filter-out $(NON_SE_HW),$(NXP_NFC_HW)),)

PRODUCT_SOONG_NAMESPACES += hardware/nxp/keymint/generic
PRODUCT_SOONG_NAMESPACES += hardware/nxp/weaver/generic

# ADD keymint hal for SN220, SN300
PRODUCT_PACKAGES += \
android.hardware.security.keymint3-service.strongbox.nxp \
android.hardware.security.keymint4-service.strongbox.nxp \
se_update_agent.nxp

# ADD weaver hal for SN220, SN300
PRODUCT_PACKAGES += \
    android.hardware.weaver2-service.nxp \
    android.hardware.weaver3-service.nxp

ifeq ($(KM_VER_DEFAULT),KM300)
PRODUCT_PACKAGES += \
android.hardware.security.keymint3-service.strongbox.nxp.xml \
android.hardware.security.keymint3-service.strongbox.nxp.rc \
android.hardware.security.sharedsecret-service.strongbox3.nxp.xml
else
PRODUCT_PACKAGES += \
android.hardware.security.keymint4-service.strongbox.nxp.xml \
android.hardware.security.keymint4-service.strongbox.nxp.rc \
android.hardware.security.sharedsecret-service.strongbox4.nxp.xml
endif #KM_VER_DEFAULT

endif

ifeq ($(ENABLE_TREBLE), true)
PRODUCT_PACKAGES += \
	vendor.nxp.nxpnfc@1.0-impl \
	vendor.nxp.nxpnfc@1.0-service
endif

ifneq ($(NXP_NFC_HW),PN557)
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.software.device_id_attestation.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.software.device_id_attestation.xml
endif

PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/handheld_core_hardware.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/handheld_core_hardware.xml

BOARD_SEPOLICY_DIRS += vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy \
                       vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/authsecret \
                       vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/keymaster \
                       vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/keymint \
                       vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/nfc \
                       vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/se \
                       vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/weaver

# NXP Specific internal SEPolicy rules.
BOARD_SEPOLICY_DIRS += vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/internal/trustedse \
                       vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/internal/wiredse \
                       vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/internal/nfc \
                       vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/internal

ifeq ($(POWER_TRACKER_FEATURE), true)
PRODUCT_PACKAGES += power_tracker_v2

BOARD_SEPOLICY_DIRS += vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/nfc/power-tracker
endif

ifneq ($(filter-out $(NON_SE_HW),$(NXP_NFC_HW)),)
PRODUCT_PACKAGES += WiredSe
BOARD_SEPOLICY_DIRS += vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/nfc/wired-se
endif

# ADD RKP CSR extraction tool
PRODUCT_PACKAGES += rkp_factory_extraction_tool

PRODUCT_PACKAGES += libnfc_crypto_extn

