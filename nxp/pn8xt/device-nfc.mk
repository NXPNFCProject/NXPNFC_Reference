#####
##### NXP NFC Device Configuration makefile
######

NXP_NFC_HOST := $(TARGET_PRODUCT)
NXP_NFC_HW := pn81T
NXP_RF_REQ := true
NXP_NFC_PLATFORM := pn8x
NXP_VENDOR_DIR := nxp

# These are the hardware-specific features
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.nfc.hce.xml:system/etc/permissions/android.hardware.nfc.hce.xml \
    frameworks/native/data/etc/android.hardware.nfc.hcef.xml:system/etc/permissions/android.hardware.nfc.hcef.xml \
    frameworks/native/data/etc/android.hardware.nfc.ese.xml:system/etc/permissions/android.hardware.nfc.ese.xml \
    frameworks/native/data/etc/android.hardware.nfc.uicc.xml:system/etc/permissions/android.hardware.nfc.uicc.xml \
    frameworks/native/data/etc/android.sofware.nfc.beam.xml:system/etc/permissions/android.sofware.nfc.beam.xml \
    frameworks/native/data/etc/com.nxp.mifare.xml:system/etc/permissions/com.nxp.mifare.xml \
    frameworks/native/data/etc/android.hardware.nfc.xml:system/etc/permissions/android.hardware.nfc.xml 

# SE config files
PRODUCT_COPY_FILES += \
    hardware/nxp/secure_element/libese-spi/p73/libese-nxp-P73.conf:vendor/etc/libese-nxp.conf


ifeq ($(NXP_NFC_HW),pn81T)
PRODUCT_COPY_FILES += \
    hardware/nxp/nfc/halimpl/libnfc-nci_NCI2_0.conf:system/product/etc/libnfc-nci.conf \
    hardware/nxp/nfc/halimpl/libnfc-nxp-PN81T_example.conf:vendor/etc/libnfc-nxp.conf

    ifeq ($(NXP_RF_REQ),true)
    PRODUCT_COPY_FILES += \
        hardware/nxp/nfc/halimpl/libnfc-nxp_RF-PN81T_example.conf:vendor/libnfc-nxp_RF.conf
    endif

else
PRODUCT_COPY_FILES += \
    hardware/nxp/nfc/halimpl/libnfc-nci.conf:system/product/etc/libnfc-nci.conf
    hardware/nxp/nfc/halimpl/libnfc-nxp-PN80T_example.conf:vendor/etc/libnfc-nxp.conf \

    ifeq ($(NXP_RF_REQ),true)
    PRODUCT_COPY_FILES += \
        hardware/nxp/nfc/halimpl/libnfc-nxp_RF-PN80T_example.conf:vendor/libnfc-nxp_RF.conf
    endif

endif

# NFC Init Files
PRODUCT_COPY_FILES += \
     vendor/$(NXP_VENDOR_DIR)/pn8xt/hw/init.$(NXP_NFC_PLATFORM).nfc.rc:vendor/etc/init/init.$(NXP_NFC_HOST).nfc.rc \
     vendor/$(NXP_VENDOR_DIR)/pn8xt/hw/init.$(NXP_NFC_PLATFORM).se.rc:vendor/etc/init/init.$(NXP_NFC_HOST).se.rc \

# NFC packages
PRODUCT_PACKAGES += \
    libnfc-nci \
    NfcNci \
    Tag \
    android.hardware.nfc@1.0-impl \
    com.nxp.nfc \
    nfc_nci.$(NXP_NFC_PLATFORM) \

PRODUCT_PACKAGES += \
	android.hardware.nfc@1.2-service \
	android.hardware.secure_element@1.2-service \
	android.hardware.wired_se@1.0-service \

ifeq ($(ENABLE_TREBLE), true)
PRODUCT_PACKAGES += \
	vendor.nxp.nxpnfc@1.0-impl \
	vendor.nxp.nxpnfc@1.0-service
endif

PRODUCT_PROPERTY_OVERRIDES += \
		ro.hardware.nfc_nci=$(NXP_NFC_PLATFORM)


BOARD_SEPOLICY_DIRS += vendor/$(NXP_VENDOR_DIR)/pn8xt/sepolicy \
                       vendor/$(NXP_VENDOR_DIR)/pn8xt/sepolicy/nfc \
                       vendor/$(NXP_VENDOR_DIR)/pn8xt/sepolicy/se \
                       vendor/$(NXP_VENDOR_DIR)/pn8xt/sepolicy/wiredse \
