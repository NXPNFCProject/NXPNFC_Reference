#####
##### NXP NFC Device Configuration makefile
######

NXP_NFC_HOST := $(TARGET_PRODUCT)
NXP_NFC_HW := SN100x
NXP_NFC_PLATFORM := SN100x
NXP_VENDOR_DIR := nxp

# These are the hardware-specific features
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.nfc.hce.xml:system/etc/permissions/android.hardware.nfc.hce.xml \
    frameworks/native/data/etc/android.hardware.nfc.hcef.xml:system/etc/permissions/android.hardware.nfc.hcef.xml \
    frameworks/native/data/etc/com.nxp.mifare.xml:system/etc/permissions/com.nxp.mifare.xml \
    frameworks/native/data/etc/android.hardware.nfc.xml:system/etc/permissions/android.hardware.nfc.xml \
    vendor/$(NXP_VENDOR_DIR)/frameworks/nxp-nfc-gsma/com.gsma.services.nfc.xml:system/etc/permissions/com.gsma.services.nfc.xml

# NFC config files
PRODUCT_COPY_FILES += \
    vendor/$(NXP_VENDOR_DIR)/SN100x/hw/$(NXP_NFC_HW)/libnfc-nci.conf:system/etc/libnfc-nci.conf \
    vendor/$(NXP_VENDOR_DIR)/SN100x/hw/$(NXP_NFC_HW)/libnfc-nxp-SN100U_example.conf:vendor/etc/libnfc-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SN100x/hw/$(NXP_NFC_HW)/libese-nxp-P73.conf:vendor/etc/libese-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SN100x/hw/$(NXP_NFC_HW)/libnfc-nxp_RF-SN100U_example_IguanaLite.conf:vendor/libnfc-nxp_RF.conf

# NFC Init Files
PRODUCT_COPY_FILES += \
     vendor/$(NXP_VENDOR_DIR)/SN100x/hw/init.$(NXP_NFC_PLATFORM).nfc.rc:vendor/etc/init/init.$(NXP_NFC_HOST).nfc.rc \
     vendor/$(NXP_VENDOR_DIR)/SN100x/hw/init.$(NXP_NFC_PLATFORM).se.rc:vendor/etc/init/init.$(NXP_NFC_HOST).se.rc \

# NFC packages
PRODUCT_PACKAGES += \
    libnfc-nci \
    NfcNci \
    Tag \
    android.hardware.nfc@1.0-impl \
    com.nxp.nfc.jar \
    nfc_nci.$(NXP_NFC_PLATFORM) \

PRODUCT_PACKAGES += \
    android.hardware.nfc@1.1-service \
    android.hardware.secure_element@1.0-service \
    android.hardware.wired_se@1.0-service \
    android.hardware.trusted_se@1.0-service\

ifeq ($(ENABLE_TREBLE), true)
PRODUCT_PACKAGES += \
	vendor.nxp.nxpnfc@1.0-impl \
	vendor.nxp.nxpnfc@1.0-service
endif

PRODUCT_PROPERTY_OVERRIDES += \
		ro.hardware.nfc_nci=$(NXP_NFC_PLATFORM)


BOARD_SEPOLICY_DIRS += vendor/$(NXP_VENDOR_DIR)/SN100x/sepolicy \
                       vendor/$(NXP_VENDOR_DIR)/SN100x/sepolicy/nfc \
                       vendor/$(NXP_VENDOR_DIR)/SN100x/sepolicy/se \
                       vendor/$(NXP_VENDOR_DIR)/SN100x/sepolicy/wiredse \
                       vendor/$(NXP_VENDOR_DIR)/SN100x/sepolicy/trustedse \
