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

# These are the hardware-specific features
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.nfc.hce.xml:system/etc/permissions/android.hardware.nfc.hce.xml \
    frameworks/native/data/etc/android.hardware.nfc.hcef.xml:system/etc/permissions/android.hardware.nfc.hcef.xml \
    frameworks/native/data/etc/android.hardware.nfc.ese.xml:system/etc/permissions/android.hardware.nfc.ese.xml \
    frameworks/native/data/etc/android.hardware.nfc.uicc.xml:system/etc/permissions/android.hardware.nfc.uicc.xml \
    frameworks/native/data/etc/com.nxp.mifare.xml:system/etc/permissions/com.nxp.mifare.xml \
    frameworks/native/data/etc/android.hardware.nfc.xml:system/etc/permissions/android.hardware.nfc.xml \
    vendor/nxp/frameworks/com.nxp.nfc.xml:system/etc/permissions/com.nxp.nfc.xml \
    vendor/nxp/frameworks/sems/com.nxp.sems.xml:vendor/etc/permissions/com.nxp.sems.xml \
    vendor/nxp/frameworks/secOSuJar/com.nxp.osu.xml:vendor/etc/permissions/com.nxp.osu.xml


ifneq ($(NXP_NFC_HW),SN220)
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.sofware.nfc.beam.xml:system/etc/permissions/android.sofware.nfc.beam.xml
endif

# NFC config files
ifeq ($(NXP_NFC_HW),SN1xx)
PRODUCT_COPY_FILES += \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nci.conf:$(TARGET_COPY_OUT_PRODUCT)/etc/libnfc-nci.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp-SN100U_example.conf:vendor/etc/libnfc-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libese-nxp-P73.conf:vendor/etc/libese-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp_RF-SN100U_example_IguanaLite.conf:vendor/libnfc-nxp_RF.conf
else ifeq ($(NXP_NFC_HW),SN220)
PRODUCT_COPY_FILES += \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nci.conf:$(TARGET_COPY_OUT_PRODUCT)/etc/libnfc-nci.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp-SN220U_example.conf:vendor/etc/libnfc-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libese-nxp-P73.conf:vendor/etc/libese-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp_RF-SN220U_example.conf:vendor/libnfc-nxp_RF.conf
else ifeq ($(NXP_NFC_HW),PN557)
PRODUCT_COPY_FILES += \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nci_NCI2_0.conf:$(TARGET_COPY_OUT_PRODUCT)/etc/libnfc-nci.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp-PN557_example.conf:vendor/etc/libnfc-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp_RF-PN557_example.conf:vendor/libnfc-nxp_RF.conf
endif

# NFC Init Files
PRODUCT_COPY_FILES += \
     vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/init.$(NXP_NFC_PLATFORM).nfc.rc:vendor/etc/init/init.$(NXP_NFC_HOST).nfc.rc \
     vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/init.$(NXP_NFC_PLATFORM).se.rc:vendor/etc/init/init.$(NXP_NFC_HOST).se.rc \

# NFC packages
PRODUCT_PACKAGES += \
    libnfc-nci \
    NfcNci \
    Tag \
    android.hardware.nfc@1.0-impl \
    com.nxp.nfc.jar \
    SBUpdateApp \

PRODUCT_PACKAGES += \
	android.hardware.nfc@1.2-service \
	android.hardware.secure_element@1.2-service \
	android.hardware.wired_se@1.0-service \
    android.hardware.trusted_se@1.2-service\

ifeq ($(ENABLE_TREBLE), true)
PRODUCT_PACKAGES += \
	vendor.nxp.nxpnfc@1.0-impl \
	vendor.nxp.nxpnfc@1.0-service
endif

PRODUCT_PROPERTY_OVERRIDES += \
		ro.hardware.nfc_nci=$(NXP_NFC_PLATFORM)


BOARD_SEPOLICY_DIRS += vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy \
                       vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/nfc \
                       vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/se \
                       vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/wiredse \
                       vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/trustedse \
