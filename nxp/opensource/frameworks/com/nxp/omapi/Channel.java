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

import com.nxp.omapi.ISmartcardServiceChannel;
import com.nxp.omapi.SmartcardError;

import android.os.RemoteException;
import android.util.Log;

/**
 * Instances of this class represent an ISO/ICE 7816-4 channel opened to a Secure
 * Element. It can be either a logical channel or the basic channel. They can
 * be used to send APDUs to the secure element. Channels are opened by calling
 * the Session.openBasicChannel(byte[]) or Session.openLogicalChannel(byte[])
 * methods.
 *
 * @see <a href="http://simalliance.org">SIMalliance Open Mobile API  v3.0</a>
 */
public class Channel {

    public static final String _TAG = "SmartcardService - Channel";

    private Session mSession;

    private final ISmartcardServiceChannel mChannel;

    private final SEService mService;

    private final Object mLock = new Object();

    Channel(SEService service, Session session, ISmartcardServiceChannel channel) {
        mService = service;
        mSession = session;
        mChannel = channel;
    }

    /**
     * Closes this channel to the Secure Element. If the method is called when
     * the channel is already closed, this method will be ignored. The close()
     * method shall wait for completion of any pending transmit(byte[] command)
     * before closing the channel.
     */
    public void close() {
        if (mService == null || !mService.isConnected()) {
            Log.e(_TAG, "close(): throw IllegalStateException");
            throw new IllegalStateException("service not connected to system");
        }
        if (mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        }
        if (!isClosed()) {
            synchronized (mLock) {
        try {
                    SmartcardError error = new SmartcardError();

            mChannel.close(error);
                    if (error.isSet()) {
                        error.throwException();
        }
                } catch (Exception e) {
                    Log.e(_TAG, "Error closing channel", e);
                }
            }
        }
    }

    /**
     * Tells if this channel is closed.
     *
     * @return <code>true</code> if the channel is closed, <code>false</code> otherwise.
     */
    public boolean isClosed() {
        if (mService == null || !mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        }
        if (mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        }
        try {
            return mChannel.isClosed();
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Returns a boolean telling if this channel is the basic channel.
     *
     * @return <code>true</code> if this channel is a basic channel. <code>false</code> if
     *         this channel is a logical channel.
     */
    public boolean isBasicChannel() {
        if (mService == null || !mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        }
        if (mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        }
        try {
            return mChannel.isBasicChannel();
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Transmit an APDU command (as per ISO/IEC 7816-4) to the Secure Element. The
     * underlying layers generate as many TPDUs as necessary to transport this APDU. The
     * API shall ensure that all available data returned from Secure Element, including
     * concatenated responses, are retrieved and made available to the calling application. If a
     * warning status code is received the API wont check for further response data but will
     * return all data received so far and the warning status code.<br>
     * The transport part is invisible from the application. The generated response is the
     * response of the APDU which means that all protocols related responses are handled
     * inside the API or the underlying implementation.<br>
     * The transmit method shall support extended length APDU commands independently of
     * the coding within the ATR.<br>
     * For status word '61 XX' the API or underlying implementation shall issue a GET
     * RESPONSE command as specified by ISO 7816-4 standard with LE=XX; for the status
     * word '6C XX', the API or underlying implementation shall reissue the input command
     * with LE=XX. For other status words, the API (or underlying implementation) shall return
     * the complete response including data and status word to the device application. The API
     * (or underlying implementation) shall not handle internally the received status words. The
     * channel shall not be closed even if the Secure Element answered with an error code.
     * The system ensures the synchronization between all the concurrent calls to this method,
     * and that only one APDU will be sent at a time, irrespective of the number of TPDUs that
     * might be required to transport it to the SE. The entire APDU communication to this SE is
     * locked to the APDU.<br>
     * The channel information in the class byte in the APDU will be ignored. The system will
     * add any required information to ensure the APDU is transported on this channel.
     * The only restrictions on the set of commands that can be sent is defined below, the API
     * implementation shall be able to send all other commands: <br>
     * <ul>
     * <li>MANAGE_CHANNEL commands are not allowed.</li>
     * <li>SELECT by DF Name (p1=04) are not allowed.</li>
     * <li>CLA bytes with channel numbers are de-masked.</li>
     * </ul>
     *
     * @param command the APDU command to be transmitted, as a byte array.
     *
     * @return the response received, as a byte array. The returned byte array contains the data bytes
     * in the following order:
     * [&lt;first data byte&gt;, ..., &lt;last data byte&gt;, &lt;sw1&gt;, &lt;sw2&gt;]
     *
     * @throws IOException if there is a communication problem to the reader or the Secure Element.
     * @throws IllegalStateException if the channel is used after being closed.
     * @throws IllegalArgumentException if the command byte array is less than 4 bytes long.
     * @throws IllegalArgumentException if Lc byte is inconsistent with the length of the byte array.
     * @throws IllegalArgumentException if CLA byte is invalid according to [2] (0xff).
     * @throws IllegalArgumentException if INS byte is invalid according to [2] (0x6x or 0x9x).
     * @throws SecurityException if the command is filtered by the security
     *             policy.
     * @throws NullPointerException if command is NULL.
     */
    public byte[] transmit(byte[] command) throws IOException, IllegalStateException,
            IllegalArgumentException, SecurityException, NullPointerException {

        if (mService == null || !mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        }
        if (mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        }

        synchronized( mLock ) {
            try {
            SmartcardError error = new SmartcardError();
            byte[] response = mChannel.transmit(command, error);
                if (error.isSet()) {
                    error.throwException();
            }
            return response;
            } catch (RemoteException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }

    /**
     * Get the session that has opened this channel.
     *
     * @return the session object this channel is bound to.
     */
    public Session getSession() {
        return mSession;
    }

    /**
     * Returns the data as received from the application select command inclusively the status word
     * received at applet selection.
     * The returned byte array contains the data bytes in the following order:
     * [&lt;first data byte&gt;, ..., &lt;last data byte&gt;, &lt;sw1&gt;, &lt;sw2&gt;]
     * @return The data as returned by the application select command inclusively the status word.
     * Only the status word if the application select command has no returned data.
     * Returns null if an application select command has not been performed or the selection response can not
     * be retrieved by the reader implementation.
     */
    public byte[] getSelectResponse()
    {
        if (mService == null || !mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        }
        if (mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        }

        byte[] response;
        try {
            response = mChannel.getSelectResponse();
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getMessage());
        }

        if(response != null && response.length == 0)
            response = null;
        return response;
    }

    /**
     * Performs a selection of the next Applet on this channel that matches to the partial AID specified
     * in the openBasicChannel(byte[] aid) or openLogicalChannel(byte[] aid) method.
     * This mechanism can be used by a device application to iterate through all Applets
     * matching to the same partial AID.
     * If selectNext() returns true a new Applet was successfully selected on this channel.
     * If no further Applet exists with matches to the partial AID this method returns false
     * and the already selected Applet stays selected. <br>
     *
     * Since the API cannot distinguish between a partial and full AID the API shall rely on the
     * response of the Secure Element for the return value of this method. <br>
     * The implementation of the underlying SELECT command within this method shall use
     * the same values as the corresponding openBasicChannel(byte[] aid) or
     * openLogicalChannel(byte[] aid) command with the option: <br>
     * P2='02' (Next occurrence) <br>
     * The select response stored in the Channel object shall be updated with the APDU
     * response of the SELECT command.

     * @return <code>true</code> if new Applet was selected on this channel.
               <code>false</code> he already selected Applet stays selected on this channel.
     *
     * @throws IOException if there is a communication problem to the reader or the Secure Element.
     * @throws IllegalStateException if the channel is used after being closed.
     * @throws UnsupportedOperationException if this operation is not supported by the card.
     */
    public boolean selectNext() throws IOException, IllegalStateException,
            UnsupportedOperationException {

        if (mService == null || !mService.isConnected()) {
            throw new IllegalStateException("service not connected to system");
        }
        if (mChannel == null) {
            throw new IllegalStateException("channel must not be null");
        }
        try {
            if (mChannel.isClosed()) {
                throw new IllegalStateException("channel is closed");
            }
        } catch (RemoteException e) {
            throw new IllegalStateException(e.getMessage());
        }

        synchronized( mLock ) {
            try {
                SmartcardError error = new SmartcardError();
                boolean response = mChannel.selectNext(error);
                if (error.isSet()) {
                    error.throwException();
                }
            return response;
            } catch (RemoteException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }
}