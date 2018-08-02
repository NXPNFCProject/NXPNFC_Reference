/******************************************************************************
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
*  Copyright 2018 NXP
*
******************************************************************************/
package com.nxp.nfc.gsma.internal;
import android.nfc.cardemulation.NfcApduServiceInfo;
import android.content.Intent;
/**
 * @hide
 */
interface INxpNfcController {
    boolean deleteOffHostService(int userId, String packageName, in NfcApduServiceInfo service);
    List<NfcApduServiceInfo> getOffHostServices(int userId, String packageName);
    NfcApduServiceInfo getDefaultOffHostService(int userId, String packageName);
    boolean commitOffHostService(int userId, String packageName, String serviceName, in NfcApduServiceInfo service);
    boolean enableMultiEvt_NxptransactionReception(String packageName, String seName);
    void enableMultiReception(String pkg, String seName);
}
