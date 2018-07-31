/*
*
*  The original Work has been changed by NXP Semiconductors.
*
*  Copyright (C) 2013-2018 NXP Semiconductors
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
*/
package com.nxp.nfc;

import java.util.HashMap;
import java.util.Map;
import android.nfc.INfcAdapter;
import android.nfc.NfcAdapter;
import android.nfc.INfcAdapterExtras;
import android.os.IBinder;
import android.os.ServiceManager;

import java.io.IOException;
import android.os.UserHandle;
import android.os.RemoteException;
import com.nxp.nfc.gsma.internal.INxpNfcController;

import android.util.Log;

public final class NxpNfcAdapter {
    private static final String TAG = "NXPNFC";
    private static int ALL_SE_ID_TYPE = 0x07;
    // Guarded by NfcAdapter.class
    static boolean sIsInitialized = false;

    /**
     * The NfcAdapter object for each application context.
     * There is a 1-1 relationship between application context and
     * NfcAdapter object.
     */
    static HashMap<NfcAdapter, NxpNfcAdapter> sNfcAdapters = new HashMap(); //guard by NfcAdapter.class

    // Final after first constructor, except for
    // attemptDeadServiceRecovery() when NFC crashes - we accept a best effort
    // recovery
    private static INfcAdapter sService;
    private static INxpNfcAdapter sNxpService;

    private NxpNfcAdapter() {
    }
    /**
     * Returns the NxpNfcAdapter for application context,
     * or throws if NFC is not available.
     * @hide
     */
    public static synchronized NxpNfcAdapter getNxpNfcAdapter(NfcAdapter adapter) {
        if (!sIsInitialized) {
            if (adapter == null) {
                Log.v(TAG, "could not find NFC support");
                throw new UnsupportedOperationException();
            }
            sService = getServiceInterface();
            if (sService == null) {
                Log.e(TAG, "could not retrieve NFC service");
                throw new UnsupportedOperationException();
            }
            sNxpService = getNxpNfcAdapterInterface();
             if (sNxpService == null) {
                Log.e(TAG, "could not retrieve NXP NFC service");
                throw new UnsupportedOperationException();
            }
            updateNxpSupportedSElist();
            sIsInitialized = true;
        }
        NxpNfcAdapter nxpAdapter = sNfcAdapters.get(adapter);
        if (nxpAdapter == null) {
            nxpAdapter = new NxpNfcAdapter();
            sNfcAdapters.put(adapter, nxpAdapter);
        }
        return nxpAdapter;
    }

    /** get handle to NFC service interface */
    private static INfcAdapter getServiceInterface() {
        /* get a handle to NFC service */
        IBinder b = ServiceManager.getService("nfc");
        if (b == null) {
            return null;
        }
        return INfcAdapter.Stub.asInterface(b);
    }

    /**
     * NFC service dead - attempt best effort recovery
     * @hide
     */
    private static void attemptDeadServiceRecovery(Exception e) {
        Log.e(TAG, "Service dead - attempting to recover",e);
        INfcAdapter service = getServiceInterface();
        if (service == null) {
            Log.e(TAG, "could not retrieve NFC service during service recovery");
            // nothing more can be done now, sService is still stale, we'll hit
            // this recovery path again later
            return;
        }
        // assigning to sService is not thread-safe, but this is best-effort code
        // and on a well-behaved system should never happen
        sService = service;
        sNxpService = getNxpNfcAdapterInterface();
        return;
    }

    /** update NxpSupportedSElist
    * @hide
    */
    private static void updateNxpSupportedSElist()
    {
        try {
            byte[] fwVer = sNxpService.getFWVersion();
            if(fwVer == null) {
                throw new UnsupportedOperationException();
            }
            else if((fwVer[0] == NxpConstants.PN553_FW_MAJOR_NUM) && (fwVer[2] == NxpConstants.PN553_FW_ROM_VER)) {
                ALL_SE_ID_TYPE |= NxpConstants.UICC2_ID_TYPE;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getFWVersion failed", e);
            attemptDeadServiceRecovery(e);
            throw new UnsupportedOperationException("getFWVersion failed");
        }
    }
    /**
     * @hide
     */
    private static INxpNfcAdapter getNxpNfcAdapterInterface() {
        if (sService == null) {
            throw new UnsupportedOperationException("You need a reference from NfcAdapter to use the "
                    + " NXP NFC APIs");
        }
        try {
            IBinder b = sService.getNfcAdapterVendorInterface("nxp");
            if (b == null) {
                return null;
            }
            return INxpNfcAdapter.Stub.asInterface(b);
        } catch (RemoteException e) {
            return null;
        }
    }

    /**
    * Get the handle to an INxpNfcController Interface
    * @hide
    */
    public INxpNfcController getNxpNfcControllerInterface() {
        if(sService == null) {
            throw new UnsupportedOperationException("You need a reference from NfcAdapter to use the "
                    + " NXP NFC APIs");
        }
        try {
            return sNxpService.getNxpNfcControllerInterface();
        }catch(RemoteException e) {
            return null;
        }
    }

    /**
     * Get the Active Secure Element List
     * <p>Requires {@link android.Manifest.permission#NFC} permission.
     *
     * @throws IOException If a failure occurred during the getActiveSecureElementList()
     */
    public String[] getActiveSecureElementList(String pkg) throws IOException {
        int [] activeSEList;
        String [] arr;
        try{
            Log.d(TAG, "getActiveSecureElementList-Enter");
            activeSEList = sNxpService.getActiveSecureElementList(pkg);
            if (activeSEList!=null && activeSEList.length != 0)
            {
                arr= new String[activeSEList.length];
                for(int i=0;i<activeSEList.length;i++)
                {
                    Log.e(TAG, "getActiveSecureElementList activeSE[i]" + activeSEList[i]);
                    if(activeSEList[i]==NxpConstants.SMART_MX_ID_TYPE)
                    {
                        arr[i]= NxpConstants.SMART_MX_ID;
                    }
                    else if(activeSEList[i]==NxpConstants.UICC_ID_TYPE)
                    {
                        arr[i]= NxpConstants.UICC_ID;
                    }
                    else if(activeSEList[i]==NxpConstants.UICC2_ID_TYPE)
                    {
                        arr[i]= NxpConstants.UICC2_ID;
                    }
                    else {
                        throw new IOException("No Secure Element Activeted");
                    }
                }
            } else {
                arr = new String[0];
            }
            return arr;
        } catch (RemoteException e) {
            Log.e(TAG, "getActiveSecureElementList: failed", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("Failure in deselecting the selected Secure Element");
        }
    }

    /**
     * Set listen mode routing table configuration for Default Route.
     * routeLoc is parameter which fetch the text from UI and compare
     * * <p>Requires {@link android.Manifest.permission#NFC} permission.
     * @throws IOException If a failure occurred during Default Route Route set.
     */
    public void DefaultRouteSet(String routeLoc, boolean fullPower, boolean lowPower, boolean noPower)
            throws IOException {
        try {
            int seID=0;
            boolean result = false;
            if (routeLoc.equals(NxpConstants.UICC_ID)) {
            seID = NxpConstants.UICC_ID_TYPE;
            } else if (routeLoc.equals(NxpConstants.UICC2_ID)) {
            seID= NxpConstants.UICC2_ID_TYPE;
            } else if (routeLoc.equals(NxpConstants.SMART_MX_ID)) {
            seID= NxpConstants.SMART_MX_ID_TYPE;
            } else if (routeLoc.equals(NxpConstants.HOST_ID)) {
              seID = NxpConstants.HOST_ID_TYPE;
            } else {
                Log.e(TAG, "DefaultRouteSet: wrong default route ID");
                throw new IOException("DefaultRouteSet failed: Wrong default route ID");
            }
               sNxpService.DefaultRouteSet(seID, fullPower, lowPower, noPower);
            } catch (RemoteException e) {
            Log.e(TAG, "confsetDefaultRoute failed", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("confsetDefaultRoute failed");
        }
    }

    /**
     * Active the Single Wired Protocol (SWP).
     * <p>Requires {@link android.Manifest.permission#NFC} permission.
     *
     * @deprecated Use {@link NxpNfcAdapter#selectDefaultSecureElement}
     * @throws IOException If a failure occurred during the Secure Element selection
     */
    @Deprecated
    public void activeSwp() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the current NFCC firware version.
     * @return 2 byte array with Major ver(0 index) adn Minor ver(1 index)
     */
    public byte[] getFwVersion() throws IOException
    {
        try{
            return sNxpService.getFWVersion();
        }
        catch(RemoteException e)
        {
            Log.e(TAG, "RemoteException in getFwVersion(): ", e);
            attemptDeadServiceRecovery(e);
            throw new IOException("RemoteException in getFwVersion()");
        }
    }

    /**
     * This api returns the CATEGORY_OTHER (non Payment)Services registered by the user
     * along with the size of the registered aid group.
     * This api has to be called when aid routing full intent is broadcast by the system.
     * <p>This gives the list of both static and dynamic card emulation services
     * registered by the user.
     * <p> This api can be called to get the list of offhost and onhost cardemulation
     * services registered by the user.
     * <ul>
     * <li>
     * If the category is CATEGORY_PAYMENT than null value is returned.
     * <li>
     * If there are no non payment services null value is returned.
     * </ul>
     * @param UserID  The user id of current user
     * @param category The category i.e. CATEGORY_PAYMENT , CATEGORY_OTHER
     * @return The hashMap of Component Name of Non Payment Services and size of the
     *         registered aid group
     */
    public Map<String, Integer> getServicesAidCacheSize (int UserID , String category) throws IOException{
        try {
            return sNxpService.getServicesAidCacheSize(UserID ,category);
        }catch(RemoteException e)
        {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return null;
        }
    }

    /**
     * This api is called by applications to update the service state of card emualation
     * services.
     * <p>This api is implemented for  {@link android.nfc.cardemulation.CardEmulation#CATEGORY_OTHER}.
     * <p>Requires {@link android.Manifest.permission#NFC} permission.<ul>
     * <li>This api should be called only when the intent AID routing
     *     table full is sent by NfcService.
     * <li>The service state change is persistent for particular UserId.
     * <li>The service state is written to the Xml and read
     *     before every routing table  change.
     * <li>If there is any change in routing table  the routing table is updated to NFCC
     *     after calling this api.
     * </ul>
     * @param  serviceState Map of ServiceName and state of service.
     * @return whether  the update of Card Emulation services is
     *          success or not.
     *          0xFF - failure
     *          0x00 - success
     * @throws  IOException if any exception occurs during the service state change.
     */
    public int updateServiceState(Map<String , Boolean> serviceState) throws IOException{
        try {
            return sNxpService.updateServiceState(UserHandle.myUserId() , serviceState);
        }catch(RemoteException e)
        {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 0xFF;
        }
    }

    /**
     * This api is called by applications to update the NFC configurations which are
     * already part of libnfc-nxp.conf and libnfc-brcm.conf
     * <p>Requires {@link android.Manifest.permission#NFC} permission.<ul>
     * <li>This api shall be called only Nfcservice is enabled.
     * <li>This api shall be called only when there are no NFC transactions ongoing
     * </ul>
     * @param  configs NFC Configuration to be updated.
     * @param  pkg package name of the caller
     * @return whether  the update of configuration is
     *          success or not.
     *          0xFF - failure
     *          0x00 - success
     * @throws  IOException if any exception occurs during setting the NFC configuration.
     */
    public int setConfig(String configs , String pkg) throws IOException {
        try {
            return sNxpService.setConfig(configs , pkg);
        } catch(RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 0xFF;
        }
    }

    /**
     * This api is called by applications to select the UICC slot. Selected Slot
     * will be activated for all type of CE from UICC.
     * <p>Requires {@link android.Manifest.permission#NFC} permission.<ul>
     * <li>This api shall be called only Nfcservice is enabled.
     * </ul>
     * @param  uicc slot number to select
     * @return whether  the update of configuration is
     *          success or not.
     *          0xFF - failure
     *          0x00 - success
     * @throws  IOException if any exception occurs during setting the NFC configuration.
     */
    public int selectUicc(int uiccSlot) throws IOException {
        try {
            return sNxpService.selectUicc(uiccSlot);
        } catch(RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 0xFF;
        }
    }

    /**
     * This api is called by applications to get Selected UICC slot. Selected Slot
     * will be used for all type of CE from UICC.
     * <p>Requires {@link android.Manifest.permission#NFC} permission.<ul>
     * <li>This api shall be called only Nfcservice is enabled.
     * </ul>
     * @param  uicc slot number to select
     * @return whether  the update of configuration is
     *          success or not.
     *          0xFF - failure
     *          0x00 - success
     * @throws  IOException if any exception occurs during setting the NFC configuration.
     */
    public int getSelectedUicc() throws IOException {
        try {
            return sNxpService.getSelectedUicc();
        } catch(RemoteException e) {
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 0xFF;
        }
    }

    /**
     * This is the first API to be called to start or stop the mPOS mode
     * <p>Requires {@link android.Manifest.permission#NFC} permission.
     * <li>This api shall be called only Nfcservice is enabled.
     * <li>This api shall be called only when there are no NFC transactions ongoing
     * </ul>
     * @param  pkg package name of the caller
     * @param  on Sets/Resets the mPOS state.
     * @return whether the update of state is
     *          success or busy or fail.
     *          MPOS_STATUS_BUSY
     *          MPOS_STATUS_REJECTED
     *          MPOS_STATUS_SUCCESS
     * @throws IOException If a failure occurred during reader mode set or reset
     */
    public int mPOSSetReaderMode (String pkg, boolean on) throws IOException {
        try {
            return sNxpService.mPOSSetReaderMode(pkg, on);
        } catch(RemoteException e) {
            Log.e(TAG, "RemoteException in mPOSSetReaderMode (int state): ", e);
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            throw new IOException("RemoteException in mPOSSetReaderMode (int state)");
        }
    }

    /**
     * This is provides the info whether mPOS mode is activated or not
     * <p>Requires {@link android.Manifest.permission#NFC} permission.
     * <li>This api shall be called only Nfcservice is enabled.
     * <li>This api shall be called only when there are no NFC transactions ongoing
     * </ul>
     * @param  pkg package name of the caller
     * @return TRUE if reader mode is started
     *          FALSE if reader mode is not started
     * @throws IOException If a failure occurred during reader mode set or reset
     */
    public boolean mPOSGetReaderMode (String pkg) throws IOException {
        try {
            return sNxpService.mPOSGetReaderMode(pkg);
        } catch(RemoteException e) {
            Log.e(TAG, "RemoteException in mPOSGetReaderMode (): ", e);
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            throw new IOException("RemoteException in mPOSSetReaderMode ()");
        }
    }

    /**
     * This API is called by application to stop RF discovery
     * <p>Requires {@link android.Manifest.permission#NFC} permission.
     * <li>This api shall be called only Nfcservice is enabled.
     * </ul>
     * @param  pkg package name of the caller
     * @param  mode
     *         LOW_POWER
     *         ULTRA_LOW_POWER
     * @return None
     * @throws IOException If a failure occurred during stop discovery
    */
    public void stopPoll(String pkg, int mode) throws IOException {
        try {
            sNxpService.stopPoll(pkg, mode);
        } catch(RemoteException e) {
            Log.e(TAG, "RemoteException in stopPoll(int mode): ", e);
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            throw new IOException("RemoteException in stopPoll(int mode)");
        }
    }

    /**
     * This API is called by application to start RF discovery
     * <p>Requires {@link android.Manifest.permission#NFC} permission.
     * <li>This api shall be called only Nfcservice is enabled.
     * </ul>
     * @param  pkg package name of the caller
     * @return None
     * @throws IOException If a failure occurred during start discovery
    */
    public void startPoll(String pkg) throws IOException {
        try {
            sNxpService.startPoll(pkg);
        } catch(RemoteException e) {
            Log.e(TAG, "RemoteException in startPoll(): ", e);
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            throw new IOException("RemoteException in startPoll()");
        }
    }
    /**
     * This API is called by application to execute the analog self test
     * <p>Requires {@link android.Manifest.permission#NFC} permission.
     * <li>This api shall be called only Nfcservice is enabled.
     * </ul>
     * @param  pkg, package name of the caller
     * @param  type, type of the analog slef test
     * @return status
     * @throws IOException If a failure occurred during nfcSelfTest
     * @hide
     */
    public int nfcSelfTest(String pkg, int type) throws IOException {
      int status = 0xFF;
      try {
        status = sNxpService.nfcSelfTest(pkg, type);
      } catch (RemoteException e) {
        Log.e(TAG, "RemoteException in nfcSelfTest(): ", e);
        e.printStackTrace();
        attemptDeadServiceRecovery(e);
        throw new IOException("RemoteException in nfcSelfTest()");
      }
      return status;
    }
    /**
     * This api is called by applications to get the maximum routing table for AID registration
     * The returned value doesn't provide the current remaining size available for AID.
     * This value depends on the size available in NFCC and is constant.
     * <p>Requires {@link android.Manifest.permission#NFC} permission.
     * @return maximum routing table size for AID registration.
     * @throws  IOException if any exception occurs while retrieving the size.
     */
    public int getMaxAidRoutingTableSize() throws IOException{
        try{
            return sNxpService.getMaxAidRoutingTableSize();
        }catch(RemoteException e){
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 0x00;
        }
    }
    /**
     * This api is called by applications to get the size of AID data which is already committed
     * to routing table in NFCC.
     * <p>Requires {@link android.Manifest.permission#NFC} permission.
     * @return  occupied size of routing table for AID registrations.
     * @throws  IOException if any exception occurs while retrieving the size.
     */
    public int getCommittedAidRoutingTableSize() throws IOException{
        try{
            return sNxpService.getCommittedAidRoutingTableSize();
        }catch(RemoteException e){
            e.printStackTrace();
            attemptDeadServiceRecovery(e);
            return 0x00;
        }
    }

    public byte[] readerPassThruMode(byte status, byte modulationTyp)
        throws IOException {
      try {
        return sNxpService.readerPassThruMode(status, modulationTyp);
      } catch (RemoteException e) {
        Log.e(TAG, "Remote exception in readerPassThruMode(): ", e);
        throw new IOException("Remote exception in readerPassThruMode()");
      }
    }

    public byte[] transceiveAppData(byte[] data) throws IOException {
      try {
        return sNxpService.transceiveAppData(data);
      } catch (RemoteException e) {
        Log.e(TAG, "RemoteException in transceiveAppData(): ", e);
        throw new IOException("RemoteException in transceiveAppData()");
      }
    }
}
