#ifndef VENDOR_NXP_NXPNFC_V1_0_NXPNFC_H
#define VENDOR_NXP_NXPNFC_V1_0_NXPNFC_H

#include <vendor/nxp/nxpnfc/1.0/INxpNfc.h>
#include <hidl/MQDescriptor.h>
#include <hidl/Status.h>
#include <hardware/hardware.h>
#include <hardware/nfc.h>
#include "utils/Log.h"
#include "hal_nxpnfc.h"

namespace vendor {
namespace nxp {
namespace nxpnfc {
namespace V1_0 {
namespace implementation {

using ::android::hidl::base::V1_0::DebugInfo;
using ::android::hidl::base::V1_0::IBase;
using ::vendor::nxp::nxpnfc::V1_0::INxpNfc;
using ::android::hardware::hidl_array;
using ::android::hardware::hidl_memory;
using ::android::hardware::hidl_string;
using ::android::hardware::hidl_vec;
using ::android::hardware::Return;
using ::android::hardware::Void;
using ::android::sp;

struct NxpNfc : public INxpNfc {
    NxpNfc(nxpnfc_nci_device_t* device);
    // Methods from ::vendor::nxp::nxpnfc::V1_0::INxpNfc follow.
    Return<void> ioctl(uint64_t ioctlType, const hidl_vec<uint8_t>& inOutData, ioctl_cb _hidl_cb) override;

    // Methods from ::android::hidl::base::V1_0::IBase follow.
  private:
    const nxpnfc_nci_device_t*       mDevice;
};

extern "C" INxpNfc* HIDL_FETCH_INxpNfc(const char* name);

}  // namespace implementation
}  // namespace V1_0
}  // namespace nxpnfc
}  // namespace nxp
}  // namespace vendor

#endif  // VENDOR_NXP_NXPNFC_V1_0_NXPNFC_H
