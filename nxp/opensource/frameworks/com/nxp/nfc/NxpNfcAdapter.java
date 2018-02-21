/*
*
*  The original Work has been changed by NXP Semiconductors.
*
*  Copyright (C) 2013-2014 NXP Semiconductors
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
     * @hide
     */
    public INxpNfcAdapterExtras getNxpNfcAdapterExtrasInterface(INfcAdapterExtras extras) {
        if (sNxpService == null || extras == null) {
            throw new UnsupportedOperationException("You need a context on NxpNfcAdapter to use the "
                    + " NXP NFC extras APIs");
        }
        try {
            return sNxpService.getNxpNfcAdapterExtrasInterface();
        } catch (RemoteException e) {
        Log.e(TAG, "getNxpNfcAdapterExtrasInterface failed", e);
        attemptDeadServiceRecovery(e);
            return null;
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
   
}
