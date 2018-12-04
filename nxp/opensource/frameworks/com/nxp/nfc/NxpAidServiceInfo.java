/*
* Copyright 2018 NXP
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.lang.Class;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.lang.reflect.Field;

/**
 * @hide
 */
public final class NxpAidServiceInfo implements Parcelable {

    static final String TAG = "NxpAidServiceInfo";
    /**
     * Mapping from category to static APDU pattern group
     */
    String mComponentName;
    String mServiceDescription;
    String mOtherCategoryAidGroupDescription;
    int  mAidSize;
    boolean  mState;


    public NxpAidServiceInfo(String componentName, int  size, boolean  state,
        String serviceDescription, String otherCategoryAidGroupDescription){
     mComponentName = componentName;
     mAidSize = size;
     mState = state;
     mServiceDescription = serviceDescription;
     mOtherCategoryAidGroupDescription = otherCategoryAidGroupDescription;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mComponentName);
            dest.writeInt(mAidSize);
            dest.writeValue(mState);
            dest.writeString(mServiceDescription);
            dest.writeString(mOtherCategoryAidGroupDescription);
    }

    public static final Parcelable.Creator<NxpAidServiceInfo> CREATOR =
            new Parcelable.Creator<NxpAidServiceInfo>() {

        @Override
        public NxpAidServiceInfo createFromParcel(Parcel source) {
            String componentName = source.readString();
            int listSize = source.readInt();
            boolean state = (Boolean) source.readValue( null );
            String serviceDescription = source.readString();
            String otherCategoryAidGroupDescription = source.readString();

            return new NxpAidServiceInfo(componentName,listSize,state,serviceDescription,
                otherCategoryAidGroupDescription);
        }

        @Override
        public NxpAidServiceInfo[] newArray(int size) {
            return new NxpAidServiceInfo[size];
        }
    };


    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("ApduService: ");
        out.append(" ComponentName: " + getComponentName());
        out.append(" AidSize: " + String.valueOf(getAidSize()));
        out.append(" State: " + String.valueOf(getState()));
        out.append(" ServiceDescription: " + getServiceDescription());
        out.append(" OtherCategoryAidGroupDescription: " + getOtherCategoryAidGroupDescription());

        return out.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NxpAidServiceInfo)) return false;
        NxpAidServiceInfo thatService = (NxpAidServiceInfo) o;

        return thatService.getComponentName().equals(this.getComponentName());
    }

    @Override
    public int hashCode() {
        return getComponentName().hashCode();
    }


    @Override
    public int describeContents() {
        return 0;
    }


    public String getComponentName() { return mComponentName ;}
    public String getServiceDescription() { return mServiceDescription ;}
    public String getOtherCategoryAidGroupDescription() { return mOtherCategoryAidGroupDescription ;}
    public Integer getAidSize() { return mAidSize ;}
    public boolean getState() { return mState ;}

}
