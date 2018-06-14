/*
 *Copyright (c) 2015-2017, The Linux Foundation. All rights reserved.
 *
 *Redistribution and use in source and binary forms, with or without
 *modification, are permitted provided that the following conditions are
 *met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *    * Neither the name of The Linux Foundation nor the names of its
 *      contributors may be used to endorse or promote products derived
 *      from this software without specific prior written permission.

 *THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 *WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 *ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 *BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 *BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 *OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 *IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/*
 * Copyright (C) 2011, The Android Open Source Project
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
/*
 * Contributed by: Giesecke & Devrient GmbH.
 */

/******************************************************************************
 *
 *  The original Work has been changed by NXP Semiconductors.
 *
 *  Copyright (C) 2018 NXP Semiconductors
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

package com.nxp.omapi;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.nxp.omapi.ISmartcardService;
import com.nxp.omapi.ISmartcardServiceCallback;
import com.nxp.omapi.ISmartcardServiceReader;
import com.nxp.omapi.SmartcardError;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * The SEService realises the communication to available Secure Elements on the
 * device. This is the entry point of this API. It is used to connect to the
 * infrastructure and get access to a list of Secure Element Readers.
 *
 * @see <a href="http://simalliance.org">SIMalliance Open Mobile API  v3.0</a>
 */
public class SEService {

    private static final String SERVICE_TAG = "SmartcardService - SEService";

    /**
     * Error code used with ServiceSpecificException.
     * Thrown if there was an error communicating with the Secure Element.
     *
     * @hide
     */
    public static final int IO_ERROR = 1;

    /**
     * Error code used with ServiceSpecificException.
     * Thrown if AID cannot be selected or is not available when opening
     * a logical channel.
     *
     * @hide
     */
    public static final int NO_SUCH_ELEMENT_ERROR = 2;

    private final Object mLock = new Object();

    /** The client context (e.g. activity). */
    private final Context mContext;

    /** The backend system. */
    private volatile ISmartcardService mSmartcardService;

    /**
     * Class for interacting with the main interface of the backend.
     */
    private ServiceConnection mConnection;

    /**
     * Collection of available readers
     */
    final private HashMap<String, Reader> mReaders = new HashMap<String,Reader>();

    /**
     * This implementation is used to receive callbacks from backend.
     */
    private final ISmartcardServiceCallback mCallback
        = new ISmartcardServiceCallback.Stub() {
    };

    /**
     * Callback object that allows the notification of the caller if this
     * SEService could be bound to the backend.
     */
    private CallBack mCallerCallback;

    /**
     * Interface to receive call-backs when the service is connected. If the
     * target language and environment allows it, then this shall be an inner
     * interface of the SEService class.
     */
    public interface CallBack {

        /**
         * Called by the framework when the service is connected.
         *
         * @param service
         *            the connected service.
         */
        void serviceConnected(SEService service);
    }

    /**
     * Establishes a new connection that can be used to connect to all the
     * Secure Elements available in the system. The connection process can be
     * quite long, so it happens in an asynchronous way. It is usable only if
     * the specified listener is called or if isConnected() returns
     * <code>true</code>. <br>
     * The call-back object passed as a parameter will have its
     * serviceConnected() method called when the connection actually happen.
     *
     * @param context
     *            the context of the calling application. Cannot be
     *            <code>null</code>.
     * @param listener
     *            a SEService.CallBack object. Can be <code>null</code>.
     */
    public SEService(Context context, SEService.CallBack listener) {

        if (context == null) {
            throw new NullPointerException("context must not be null");
        }

        mContext = context;
        mCallerCallback = listener;

        mConnection = new ServiceConnection() {

            public synchronized void onServiceConnected(
                    ComponentName className, IBinder service) {

                mSmartcardService = ISmartcardService.Stub.asInterface(service);
                if (mCallerCallback != null) {
                    mCallerCallback.serviceConnected(SEService.this);
                }
                Log.i(SERVICE_TAG, "Service onServiceConnected");
            }

            public void onServiceDisconnected(ComponentName className) {
                mSmartcardService = null;
                Log.i(SERVICE_TAG, "Service onServiceDisconnected");
            }
        };

        Intent intent = new Intent(ISmartcardService.class.getName());
        intent.setClassName("com.nxp.omapi",
                            "com.nxp.omapi.backend.SmartcardService");
        boolean bindingSuccessful = mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        if (bindingSuccessful) {
            Log.i(SERVICE_TAG, "bindService successful");
        }
    }

    /**
     * Tells whether or not the service is connected.
     *
     * @return <code>true</code> if the service is connected.
     */
    public boolean isConnected() {
        return mSmartcardService != null;
    }

    /**
     * Returns the list of available Secure Element readers.
     * There must be no duplicated objects in the returned list.
     * All available readers shall be listed even if no card is inserted.
     *
     * @return The readers list, as an array of Readers. If there are no readers the returned array is of length 0.
     */
    public Reader[] getReaders() {
        if (mSmartcardService == null) {
            throw new IllegalStateException("service not connected to system");
        }
        String[] readerNames;
        try {
            readerNames = mSmartcardService.getReaders();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        Reader[] readers = new Reader[readerNames.length];
        int i = 0;
        for (String readerName : readerNames) {
            if(mReaders.get(readerName)==null) {
                mReaders.put(readerName, new Reader( this, readerName ));
                readers[i++] = mReaders.get(readerName);
            } else {
                readers[i++] = mReaders.get(readerName);
            }
        }
        return readers;
    }

    /**
     * Releases all Secure Elements resources allocated by this SEService
     * (including any binding to an underlying service).
     * As a result isConnected() will return false after shutdown() was called.
     * After this method call, the SEService object is not connected.
     * It is recommended to call this method in the termination method of the calling application
     * (or part of this application) which is bound to this SEService.
     */
    public void shutdown() {
        synchronized (mLock) {
            if (mSmartcardService != null ) {
                Collection<Reader> col = mReaders.values();
                Iterator<Reader> iter = col.iterator();
                while( iter.hasNext() ) {
                    try {
                        Reader reader = iter.next();
                        reader.closeSessions();
                    } catch (Exception ignore) {
                    }
                }
            }
            try {
                mContext.unbindService(mConnection);
            } catch (IllegalArgumentException e) {
                // Do nothing and fail silently since an error here indicates
                // that binding never succeeded in the first place.
            }
            mSmartcardService = null;
        }
    }

    /**
     * Returns the version of the OpenMobile API specification this
     * implementation is based on.
     *
     * @return String containing the OpenMobile API version (e.g. "3.0").
     */
    public String getVersion() {
        return "3.0";
    }

    // ******************************************************************
    // package private methods
    // ******************************************************************

    public ISmartcardServiceReader getReader(String name) throws IOException {
        try {
            SmartcardError error = new SmartcardError();
            ISmartcardServiceReader reader = mSmartcardService.getReader(name, error);
            if (error.isSet()) {
                error.throwException();
            }
            return reader;
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    ISmartcardServiceCallback getCallback() {
        return mCallback;
    }
}
