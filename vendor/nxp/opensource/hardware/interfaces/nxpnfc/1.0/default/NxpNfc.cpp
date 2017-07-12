/*
 * Copyright (C) 2011, 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 /******************************************************************************
 *
 *  The original Work has been changed by NXP Semiconductors.
 *
 *  Copyright (C) 2015 NXP Semiconductors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
#include "NxpNfc.h"

namespace vendor {
namespace nxp {
namespace nxpnfc {
namespace V1_0 {
namespace implementation {

NxpNfc::NxpNfc(nxpnfc_nci_device_t* device) : mDevice(device) {
}
// Methods from ::vendor::nxp::nxpnfc::V1_0::INxpNfc follow.
Return<void> NxpNfc::ioctl(uint64_t ioctlType, const hidl_vec<uint8_t>& inOutData, ioctl_cb _hidl_cb) {
    // TODO implement
    uint32_t status;
    nfc_nci_IoctlInOutData_t inpOutData;
    NfcData  outputData;
    nfc_nci_IoctlInOutData_t *pInOutData=(nfc_nci_IoctlInOutData_t*)&inOutData[0];

    /*data from proxy->stub is copied to local data which can be updated by
     * underlying HAL implementation since its an inout argument*/
    memcpy(&inpOutData,pInOutData,sizeof(nfc_nci_IoctlInOutData_t));
    status = mDevice->ioctl(mDevice, ioctlType, &inpOutData);

    /*copy data and additional fields indicating status of ioctl operation
     * and context of the caller. Then invoke the corresponding proxy callback*/
    inpOutData.out.ioctlType = ioctlType;
    inpOutData.out.context   = pInOutData->inp.context;
    inpOutData.out.result    = status;
    outputData.setToExternal((uint8_t*)&inpOutData.out, sizeof(nfc_nci_ExtnOutputData_t));
    _hidl_cb(outputData);
    return Void();
}


// Methods from ::android::hidl::base::V1_0::IBase follow.

INxpNfc* HIDL_FETCH_INxpNfc(const char* /* name */) {
    nxpnfc_nci_device_t* nfc_device;
    int ret = 0;
    const hw_module_t* hw_module = NULL;
     ALOGE ("HIDL_FETCH_INxpNfc");
    ret = hw_get_module (NFC_NCI_HARDWARE_MODULE_ID, &hw_module);
    if (ret == 0)
    {
        ret = nfc_nci_open (hw_module, (nfc_nci_device_t**)&nfc_device);
        if (ret != 0) {
            ALOGE ("nfc_nci_open failed: %d", ret);
        }
    }
    else
        ALOGE ("hw_get_module %s failed: %d", NFC_NCI_HARDWARE_MODULE_ID, ret);

    if (ret == 0) {
        //return (Nfc*)new NxpNfc(nfc_device);
        //return nullptr;
        return new NxpNfc(nfc_device);
    } else {
        ALOGE("Passthrough failed to load legacy HAL.");
        return nullptr;
    }
}

}  // namespace implementation
}  // namespace V1_0
}  // namespace nxpnfc
}  // namespace nxp
}  // namespace vendor
