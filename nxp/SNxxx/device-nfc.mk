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

# Variable to enable/disable power tracker feature.
# By default this is disabled. This can be enabled by either by changing below value to true
# or can be specified in make command like "make -j8 POWER_TRACKER_FEATURE=true"
POWER_TRACKER_FEATURE ?= false

# These are the hardware-specific features
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.nfc.hce.xml:system/etc/permissions/android.hardware.nfc.hce.xml \
    frameworks/native/data/etc/android.hardware.nfc.hcef.xml:system/etc/permissions/android.hardware.nfc.hcef.xml \
    frameworks/native/data/etc/android.hardware.nfc.ese.xml:system/etc/permissions/android.hardware.nfc.ese.xml \
    frameworks/native/data/etc/android.hardware.nfc.uicc.xml:system/etc/permissions/android.hardware.nfc.uicc.xml \
    frameworks/native/data/etc/com.nxp.mifare.xml:system/etc/permissions/com.nxp.mifare.xml \
    frameworks/native/data/etc/android.hardware.nfc.xml:system/etc/permissions/android.hardware.nfc.xml \
    frameworks/native/data/etc/android.hardware.se.omapi.ese.xml:system/etc/permissions/android.hardware.se.omapi.ese.xml \
    frameworks/native/data/etc/com.android.se.xml:system/etc/permissions/com.android.se.xml \
    frameworks/native/data/etc/android.software.secure_lock_screen.xml:system/etc/permissions/android.software.secure_lock_screen.xml \


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
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp_RF-SN100U_example_IguanaLite.conf:vendor/libnfc-nxp_RF.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/hal_uuid_map_config.xml:vendor/etc/hal_uuid_map_config.xml
else ifeq ($(NXP_NFC_HW),SN220)
PRODUCT_COPY_FILES += \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nci.conf:$(TARGET_COPY_OUT_PRODUCT)/etc/libnfc-nci.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp-SN220U_example.conf:vendor/etc/libnfc-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libese-nxp-P73.conf:vendor/etc/libese-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp_RF-SN220U_example.conf:vendor/libnfc-nxp_RF.conf \
    frameworks/native/data/etc/android.hardware.keystore.app_attest_key.xml:system/etc/permissions/android.hardware.keystore.app_attest_key.xml \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/hal_uuid_map_config.xml:vendor/etc/hal_uuid_map_config.xml
else ifeq ($(NXP_NFC_HW),PN557)
PRODUCT_COPY_FILES += \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nci.conf:$(TARGET_COPY_OUT_PRODUCT)/etc/libnfc-nci.conf \
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
    com.nxp.nfc \
    SBUpdateApp \

PRODUCT_PACKAGES += \
    android.hardware.nfc_snxxx@1.2-service \
    android.hardware.secure_element_snxxx@1.2-service \
    android.hardware.wired_se@1.0-service \
    android.hardware.trusted_se@1.2-service \

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

ifeq ($(POWER_TRACKER_FEATURE), true)
PRODUCT_PACKAGES += android.hardware.power.stats-service.pixel \
                    power_tracker \
                    VtsHalPowerStatsTargetNfcTest

BOARD_SEPOLICY_DIRS += vendor/$(NXP_VENDOR_DIR)/SNxxx/sepolicy/nfc/power-tracker
BOARD_SEPOLICY_DIRS += hardware/google/pixel-sepolicy/powerstats

PRODUCT_SOONG_NAMESPACES += hardware/google/pixel \
                            device/google/gs101 \
                            device/google/gs-common/powerstats \
                            device/google/gs101/powerstats \
                            hardware/nxp/tests/powerstats
endif
