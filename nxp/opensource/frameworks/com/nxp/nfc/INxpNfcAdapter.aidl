 /*
  * Copyright (C) 2015-2018 NXP Semiconductors
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
package com.nxp.nfc;

import com.nxp.nfc.INfcVzw;
import com.nxp.nfc.gsma.internal.INxpNfcController;
import com.nxp.nfc.NxpAidServiceInfo;

/**
 * @hide
 */
interface INxpNfcAdapter
{
    INfcVzw getNfcVzwInterface();
    INxpNfcController getNxpNfcControllerInterface();
    int setEmvCoPollProfile(boolean enable, int route);
    void DefaultRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower);
    void MifareDesfireRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower);
    void MifareCLTRouteSet(int routeLoc, boolean fullPower, boolean lowPower, boolean noPower);
    byte[]  getFWVersion();
    List<NxpAidServiceInfo> getServicesAidInfo(int userId, String category);
    int getMaxAidRoutingTableSize();
    int getCommittedAidRoutingTableSize();
    int[] getActiveSecureElementList(String pkg);
    int updateServiceState(int userId , in Map serviceState);
    int setConfig(String configs , String pkg);
    byte[] readerPassThruMode(byte status, byte modulationTyp);
    byte[] transceiveAppData(in byte[] data);
    int mPOSSetReaderMode(String pkg, boolean on);
    boolean mPOSGetReaderMode(String pkg);
    void stopPoll(String pkg, int mode);
    void startPoll(String pkg);
    int nfcSelfTest(String pkg, int type);
    int selectUicc(int uiccSlot);
    int getSelectedUicc();
}
