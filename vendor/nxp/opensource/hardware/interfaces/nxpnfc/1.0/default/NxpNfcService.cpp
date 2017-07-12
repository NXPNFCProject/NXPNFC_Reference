#define LOG_TAG "vendor.nxp.nxpnfc@1.0-service"
#include <android/hardware/nfc/1.0/INfc.h>
#include <vendor/nxp/nxpnfc/1.0/INxpNfc.h>

#include <hidl/LegacySupport.h>

// Generated HIDL files
using android::hardware::nfc::V1_0::INfc;
using vendor::nxp::nxpnfc::V1_0::INxpNfc;
using android::hardware::defaultPassthroughServiceImplementation;
using android::OK;
using android::hardware::configureRpcThreadpool;
using android::hardware::registerPassthroughServiceImplementation;
using android::hardware::joinRpcThreadpool;

int main() {
    int status;
    configureRpcThreadpool(1, true);
    status = registerPassthroughServiceImplementation<INfc>();
    LOG_ALWAYS_FATAL_IF(status != OK, "Error while registering nfc AOSP service: %d", status);
	status = defaultPassthroughServiceImplementation<INxpNfc>();
	LOG_ALWAYS_FATAL_IF(status != OK, "Error while registering nxpnfc vendor service: %d", status);
    ALOGI("Registered INfc & INxpNfc");
    joinRpcThreadpool();
    return 0;
}
