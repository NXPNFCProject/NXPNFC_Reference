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
    frameworks/native/data/etc/android.hardware.device_unique_attestation.xml:system/etc/permissions/android.hardware.device_unique_attestation.xml \

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
else ifeq ($(NXP_NFC_HW),SN300)
PRODUCT_COPY_FILES += \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nci.conf:$(TARGET_COPY_OUT_PRODUCT)/etc/libnfc-nci.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp-SN300U_example.conf:vendor/etc/libnfc-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libese-nxp-P73.conf:vendor/etc/libese-nxp.conf \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/libnfc-nxp_RF-SN300U_example.conf:vendor/libnfc-nxp_RF.conf \
    frameworks/native/data/etc/android.hardware.keystore.app_attest_key.xml:system/etc/permissions/android.hardware.keystore.app_attest_key.xml \
    vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/$(NXP_NFC_HW)/hal_uuid_map_config.xml:vendor/etc/hal_uuid_map_config.xml
endif

# NFC Init Files
PRODUCT_COPY_FILES += \
     vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/init.$(NXP_NFC_PLATFORM).nfc.rc:vendor/etc/init/init.$(NXP_NFC_HOST).nfc.rc \
     vendor/$(NXP_VENDOR_DIR)/SNxxx/hw/init.$(NXP_NFC_PLATFORM).se.rc:vendor/etc/init/init.$(NXP_NFC_HOST).se.rc \

# Service packages
PRODUCT_PACKAGES += \
    NfcNci \
    SecureElement \
    Tag \
    rkpdapp \

# jar packages
PRODUCT_PACKAGES += \
    com.nxp.nfc \
    com.nxp.osu \
    com.nxp.sems \

# DTA, WLC & other libraries
PRODUCT_PACKAGES += \
    WlcServiceLib \
    libnfc_wlc_jni \
    WlcServiceDefaults \
    NxpDTA \

#Test Apps
PRODUCT_PACKAGES += \
    FieldDetectApp \
    OSUTestAPP \
    SBupdateApp \
    JrcpOmapiSmb \
    JrcpOmapiSpi \
    JrcpProxy \
    JrcpOmapiSemdemon \
    JrcpOmapiVISO \
    PaymentApp_DynamicAidNonPaymentOffHostTest \
    PaymentApp_eSE \
    PaymentApp_eSE_ConflictAID \
    PaymentApp_eSE_NoConflictAID \
    PaymentApp_eSE_Overflow \
    PaymentApp_eSE_17BAID \
    PaymentApp_eSE_AllSizeAIDs \
    PaymentApp_eSE_Overflow_48AIDs16Bytes \
    PaymentApp_eSE_Overflow_MAX_AIDs \
    PaymentApp_eSE_Overflow_MAX_AIDs_Plus_1 \
    PaymentApp_host \
    PaymentApp_euicc2 \
    PaymentApp_euicc2_Overflow \
    PaymentApp_host_17BAID \
    PaymentApp_host_AID_CountTest \
    PaymentApp_host_ConflictAID \
    PaymentApp_host_noConflictAID \
    PaymentApp_host_Overflow \
    PaymentApp_uicc \
    PaymentApp_uicc_17ByteAID \
    PaymentApp_uicc_AIDCountTest \
    PaymentApp_uicc_ConflictAID \
    PaymentApp_uicc_NoConflictAID \
    PaymentApp_uicc_Overflow \
    PaymentApp_uicc_overflow_nonpayment_aids \
    PaymentApp_Uicc2 \
    PaymentApp_SE2_eSE \
    PaymentApp_FelicaHostApp \
    PaymentApp_eSE_AIDCountTest \
    dualUiccSwitch \
    JrcpOmapi_Tee \
    JrcpOmapi_Ree \
    RssiApp \
    PaymentApp_LoopbackApp_uicc \
    PaymentApp_LoopbackApp_eSE \
    PaymentApp_host_Nunit \
    NxpTransitWallet \
    PaymentApp_ese_49AIDs \
    PaymentApp_ese_50AIDs \
    PaymentApp_uicc_49AIDs \
    PaymentApp_uicc_50AIDs \
    PaymentApp_FelicaHostApp_4203 \
    PaymentApp_FelicaHostApp_EmptyData \
    PaymentApp_SE2_eSE_AID_Filtering \
    PaymentApp_euicc \
    PaymentApp_euicc_Overflow \
    PaymentApp_Preferred \
    PaymentApp_prefix_sufix \
    PaymentApp_uicc2 \
    PaymentApp_host_UL_F_SO_F \
    PaymentApp_host_UL_F_SO_T \
    PaymentApp_host_UL_T_SO_F \
    PaymentApp_host_UL_T_SO_T \

#ADD ALL HAL Services
PRODUCT_PACKAGES += \
    android.hardware.secure_element-service.nxp \
    android.hardware.wired_se@1.0-service \
    android.hardware.trusted_se@1.2-service \
    android.hardware.nfc-service.nxp \
    android.hardware.authsecret-service.nxp \
    android.hardware.weaver-service.nxp

ifeq ($(NXP_NFC_HW),SN1xx)
# ADD keymaster HAL for SN1xx
PRODUCT_PACKAGES += \
android.hardware.keymaster@4.1-javacard.service \

else

# ADD keymint hal for SN220
PRODUCT_PACKAGES += \
android.hardware.security.keymint-service.strongbox.nxp \

endif


#VTS/gtest native test modules
PRODUCT_PACKAGES += \
    VtsAidlHalNfcTargetTest \
    VtsHalSecureElementTargetTest \
    VtsAidlKeyMintTargetTest \
    VtsHalRemotelyProvisionedComponentTargetTest \
    VtsAidlSharedSecretTargetTest \
    VtsHalKeymasterV4_0TargetTest \
    VtsHalKeymasterV4_1TargetTest \
    VtsHalWeaverTargetTest \
    VtsHalAuthSecretTargetTest \
    SelfTestHalAidlNfc \
    VtsHalOmapiSeAccessControlTestCases \
    VtsAidlKeyMintBenchmarkTest \
    
ifeq ($(ENABLE_TREBLE), true)
PRODUCT_PACKAGES += \
	vendor.nxp.nxpnfc@1.0-impl \
	vendor.nxp.nxpnfc@1.0-service
endif

PRODUCT_PROPERTY_OVERRIDES += \
		ro.hardware.nfc_nci=$(NXP_NFC_PLATFORM)
PRODUCT_PRODUCT_PROPERTIES += remote_provisioning.hostname=remoteprovisioning.googleapis.com
PRODUCT_PRODUCT_PROPERTIES += remote_provisioning.enable_rkpd=true
PRODUCT_PRODUCT_PROPERTIES += remote_provisioning.strongbox.rkp_only=true
VENDOR_SECURITY_PATCH = $(PLATFORM_SECURITY_PATCH)
PRODUCT_PRODUCT_PROPERTIES += ro.crypto.metadata_init_delete_all_keys.enabled=true
PRODUCT_PRODUCT_PROPERTIES += ro.product.device_for_attestation=$(TARGET_PRODUCT)
PRODUCT_PRODUCT_PROPERTIES += ro.product.product_for_attestation=unknown
PRODUCT_PRODUCT_PROPERTIES += ro.product.manufacturer_for_attestation=unknown
PRODUCT_PRODUCT_PROPERTIES += ro.product.vendor.name=unknown
PRODUCT_PRODUCT_PROPERTIES += ro.product.name=unknown
PRODUCT_MODEL_FOR_ATTESTATION := $(TARGET_PRODUCT)
PRODUCT_BRAND_FOR_ATTESTATION := Android

PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.software.device_id_attestation.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.software.device_id_attestation.xml \
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

PRODUCT_SYSTEM_PROPERTIES += ro.boot.hardware.sku=NXP-NFC
