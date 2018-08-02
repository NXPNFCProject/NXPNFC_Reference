/*
* Copyright (C) 2015 NXP Semiconductors
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

package android.nfc.cardemulation;

import android.nfc.cardemulation.ApduServiceInfo;
import android.nfc.cardemulation.AidGroup;
import android.nfc.cardemulation.NfcAidGroup;
import android.nfc.cardemulation.CardEmulation;
import android.nfc.cardemulation.HostApduService;
import android.nfc.cardemulation.OffHostApduService;
import android.os.Parcel;
import android.os.Parcelable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import java.util.ArrayList;
import java.util.HashMap;
import android.util.Log;
import android.util.Xml;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import com.nxp.nfc.NfcConstants;
import java.io.IOException;
import android.content.pm.PackageManager;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.util.AttributeSet;
import java.util.Map;
import android.graphics.Bitmap;
import java.io.FileDescriptor;
import java.util.List;
import java.io.PrintWriter;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
/**
 * @hide
 */
public final class NfcApduServiceInfo extends ApduServiceInfo implements Parcelable {
    static final String TAG = "NfcApduServiceInfo";

    //name of secure element
    static final String SECURE_ELEMENT_ESE = "eSE";
    static final String SECURE_ELEMENT_UICC = "UICC";
    static final String SECURE_ELEMENT_UICC2 = "UICC2";
    //index of secure element
    public static final int SECURE_ELEMENT_ROUTE_ESE = 1;
    public static final int SECURE_ELEMENT_ROUTE_UICC = 2;
    public static final int SECURE_ELEMENT_ROUTE_UICC2 = 0x4;

    //power state value
    static final int POWER_STATE_SWITCH_ON = 1;
    static final int POWER_STATE_SWITCH_OFF = 2;
    static final int POWER_STATE_BATTERY_OFF = 4;

    /**
     * The name of the meta-data element that contains
     * nxp extended SE information about off host service.
     */
    static final String NXP_NFC_EXT_META_DATA =
            "com.nxp.nfc.extensions";


    /**
     * Mapping from category to static AID group
     */
    final HashMap<String, NfcAidGroup> mStaticNfcAidGroups;

    /**
     * Mapping from category to dynamic AID group
     */
    final HashMap<String, NfcAidGroup> mDynamicNfcAidGroups;

    /**
    * The Drawable of the service banner specified by the Application Dynamically to be stored as byteArray.
    */
    byte[] mByteArrayBanner = null;

    /**
     * This says whether the Application can modify the AIDs or not.
     */
    final boolean mModifiable;

    /**
     * This says whether the Service is enabled or disabled by the user
     * By default it is disabled.This is only applicable for OTHER category.
     * states are as follows
     * ENABLING(service creation)->ENABLED(Committed to Routing)->
     * DISABLING(user requested to disable)->DISABLED(Removed from Routing).
     * In ENABLED or DISABLING state, this service will be accounted for routing.
     */
    int mServiceState;

    /**
     * nxp se extension
     */
    final ESeInfo mSeExtension;
   /**
     * @hide
     */
    public NfcApduServiceInfo(ResolveInfo info, boolean onHost, String description,
            ArrayList<NfcAidGroup> staticNfcAidGroups, ArrayList<NfcAidGroup> dynamicNfcAidGroups,
            boolean requiresUnlock, int bannerResource, int uid,
            String settingsActivityName, ESeInfo seExtension,boolean modifiable){
        super(info, onHost, description, nfcAidGroups2AidGroups(staticNfcAidGroups), nfcAidGroups2AidGroups(dynamicNfcAidGroups),
                requiresUnlock, bannerResource, uid, settingsActivityName);
        this.mModifiable = modifiable;        
        this.mServiceState = NfcConstants.SERVICE_STATE_ENABLING;
        this.mStaticNfcAidGroups = new HashMap<String, NfcAidGroup>();
        this.mDynamicNfcAidGroups = new HashMap<String, NfcAidGroup>();
        if(staticNfcAidGroups != null) {
            for (NfcAidGroup nfcAidGroup : staticNfcAidGroups) {
                this.mStaticNfcAidGroups.put(nfcAidGroup.getCategory(), nfcAidGroup);
            }
        }

        if(dynamicNfcAidGroups != null) {
            for (NfcAidGroup nfcAidGroup : dynamicNfcAidGroups) {
                this.mDynamicNfcAidGroups.put(nfcAidGroup.getCategory(), nfcAidGroup);
            }
        }
        this.mSeExtension = seExtension;
    }

    public NfcApduServiceInfo(PackageManager pm, ResolveInfo info, boolean onHost)
            throws XmlPullParserException, IOException {
        super(pm, info, onHost);
        this.mModifiable = false;
        this.mServiceState = NfcConstants.SERVICE_STATE_ENABLING;
        ServiceInfo si = info.serviceInfo;
        XmlResourceParser parser = null;
        XmlResourceParser extParser = null;
        try {
            if (onHost) {
                parser = si.loadXmlMetaData(pm, HostApduService.SERVICE_META_DATA);
                if (parser == null) {
                    throw new XmlPullParserException("No " + HostApduService.SERVICE_META_DATA +
                            " meta-data");
                }
            } else {
                parser = si.loadXmlMetaData(pm, OffHostApduService.SERVICE_META_DATA);
                if (parser == null) {
                    throw new XmlPullParserException("No " + OffHostApduService.SERVICE_META_DATA +
                            " meta-data");
                }

                /* load se extension xml */
                extParser = si.loadXmlMetaData(pm, NXP_NFC_EXT_META_DATA);
                if (extParser == null) {
                    Log.d(TAG,"No " + NXP_NFC_EXT_META_DATA +
                            " meta-data");
                }
            }

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.START_TAG && eventType != XmlPullParser.END_DOCUMENT) {
                eventType = parser.next();
            }

            String tagName = parser.getName();
            if (onHost && !"host-apdu-service".equals(tagName)) {
                throw new XmlPullParserException(
                        "Meta-data does not start with <host-apdu-service> tag");
            } else if (!onHost && !"offhost-apdu-service".equals(tagName)) {
                throw new XmlPullParserException(
                        "Meta-data does not start with <offhost-apdu-service> tag");
            }

            Resources res = pm.getResourcesForApplication(si.applicationInfo);
            AttributeSet attrs = Xml.asAttributeSet(parser);

            mStaticNfcAidGroups = new HashMap<String, NfcAidGroup>();
            mDynamicNfcAidGroups = new HashMap<String, NfcAidGroup>();
            for(Map.Entry<String,AidGroup> stringaidgroup : mStaticAidGroups.entrySet()) {
                String category = stringaidgroup.getKey();
                AidGroup aidg = stringaidgroup.getValue();
                mStaticNfcAidGroups.put(category, new NfcAidGroup(aidg));
            }

            for(Map.Entry<String,AidGroup> stringaidgroup : mDynamicAidGroups.entrySet()) {
                String category = stringaidgroup.getKey();
                AidGroup aidg = stringaidgroup.getValue();
                mDynamicNfcAidGroups.put(category, new NfcAidGroup(aidg));
            }
            final int depth = parser.getDepth();
            NfcAidGroup.ApduPatternGroup currApduPatternGroup = null;
            NfcAidGroup aidGroup = null;
            while (((eventType = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
                    && eventType != XmlPullParser.END_DOCUMENT) {
                tagName = parser.getName();
                if (!onHost && eventType == XmlPullParser.START_TAG && "apdu-pattern-group".equals(tagName) &&
                    currApduPatternGroup == null) {
                    Log.e(TAG, "apdu-pattern-group");
                    final TypedArray groupAttrs = res.obtainAttributes(attrs,
                            com.android.internal.R.styleable.ApduPatternGroup);
                    String groupDescription = groupAttrs.getString(
                            com.android.internal.R.styleable.ApduPatternGroup_description);
                    Log.e(TAG, "apdu-pattern-group description"+groupDescription);
                    aidGroup = mStaticNfcAidGroups.get(CardEmulation.CATEGORY_OTHER);
                    currApduPatternGroup = new NfcAidGroup.ApduPatternGroup(groupDescription);
                    if (aidGroup != null) {
                        /*
                        if (!CardEmulation.CATEGORY_OTHER.equals(groupCategory)) {
                            Log.e(TAG, "Not allowing multiple aid-groups in the " +
                                    groupCategory + " category");
                            currentGroup = null;
                        }*/
                    } else {
                        List<String> aids = new ArrayList<String>();
                        AidGroup group;
                        try {
                            group = new AidGroup(CardEmulation.CATEGORY_OTHER, groupDescription);
                            aidGroup = new NfcAidGroup(CardEmulation.CATEGORY_OTHER, groupDescription);
                        } catch (IllegalArgumentException e) {
                          e.printStackTrace();
                        }
                        mStaticNfcAidGroups.put(CardEmulation.CATEGORY_OTHER ,aidGroup);
                    }
                    groupAttrs.recycle();
                } else if (!onHost && eventType == XmlPullParser.END_TAG && "apdu-pattern-group".equals(tagName) &&
                    currApduPatternGroup != null) {
                    Log.e(TAG, "apdu-pattern-group end");
                    if(currApduPatternGroup.getApduPattern().size() > 0x00) {
                        aidGroup.addApduGroup(currApduPatternGroup);
                    }
                    Log.e(TAG, "apdu-pattern-group end");
                } else if (!onHost && eventType == XmlPullParser.START_TAG && "apdu-pattern-filter".equals(tagName) &&
                    currApduPatternGroup != null) {
                    Log.e(TAG, "apdu pattern enter");
                    TypedArray a = res.obtainAttributes(attrs,
                            com.android.internal.R.styleable.ApduPatternFilter);
                    String description = a.getString(com.android.internal.R.styleable.ApduPatternFilter_description); //.toUpperCase();
                    //String reference_data = a.getString(com.android.internal.R.styleable.ApduPatternFilter_referenceData); //.toUpperCase();
                    String reference_data = a.getString(com.android.internal.R.styleable.ApduPatternFilter_referenceData); //.toUpperCase();
                    String mask = a.getString(com.android.internal.R.styleable.ApduPatternFilter_apduMask); //.toUpperCase();
                    Log.e(TAG, "valid apdu pattern"+ reference_data + mask + description);
                    Log.e(TAG, "valid apdu pattern reference_data"+com.android.internal.R.styleable.ApduPatternFilter_referenceData);
                    Log.e(TAG, "valid apdu pattern reference_mask"+com.android.internal.R.styleable.ApduPatternFilter_apduMask);
                    Log.e(TAG, "valid apdu pattern description"+com.android.internal.R.styleable.ApduPatternFilter_description);
                    if (isValidApduString(reference_data) && isValidApduString(mask)) {
                        NfcAidGroup.ApduPattern apdu = mStaticNfcAidGroups.get(CardEmulation.CATEGORY_OTHER).new ApduPattern(reference_data, mask,description);
                        currApduPatternGroup.addApduPattern(apdu);
                    } else {
                        Log.e(TAG, "Ignoring invalid apdu pattern: " + reference_data);
                    }
                    Log.e(TAG, "valid apdu pattern"+ reference_data+mask+description);
                }
            }
        } catch (NameNotFoundException e) {
            throw new XmlPullParserException("Unable to create context for: " + si.packageName);
        } finally {
            if (parser != null) parser.close();
        }
        if (extParser != null)
        {
            try{
                int eventType = extParser.getEventType();
                final int depth = extParser.getDepth();
                String seName = null;
                int powerState  = 0;
                String optParam = null;

                while (eventType != XmlPullParser.START_TAG && eventType != XmlPullParser.END_DOCUMENT) {
                    eventType = extParser.next();
                }
                String tagName = extParser.getName();
                if (!"extensions".equals(tagName)) {
                    throw new XmlPullParserException(
                            "Meta-data does not start with <extensions> tag "+tagName);
                }
                while (((eventType = extParser.next()) != XmlPullParser.END_TAG || extParser.getDepth() > depth)
                        && eventType != XmlPullParser.END_DOCUMENT) {
                    tagName = extParser.getName();

                    if (eventType == XmlPullParser.START_TAG && "se-id".equals(tagName) ) {
                        // Get name of eSE
                        seName = extParser.getAttributeValue(null, "name");
                        if (seName == null  || (!seName.equalsIgnoreCase(SECURE_ELEMENT_ESE) && !seName.equalsIgnoreCase(SECURE_ELEMENT_UICC)
                                && !seName.equalsIgnoreCase(SECURE_ELEMENT_UICC2)) ) {
                            throw new XmlPullParserException("Unsupported se name: " + seName);
                        }
                    } else if (eventType == XmlPullParser.START_TAG && "se-power-state".equals(tagName) ) {
                        // Get power state
                        String powerName = extParser.getAttributeValue(null, "name");
                        boolean powerValue = (extParser.getAttributeValue(null, "value").equals("true")) ? true : false;
                        if (powerName.equalsIgnoreCase("SwitchOn") && powerValue) {
                            powerState |= POWER_STATE_SWITCH_ON;
                        }else if (powerName.equalsIgnoreCase("SwitchOff") && powerValue) {
                            powerState |= POWER_STATE_SWITCH_OFF;
                        }else if (powerName.equalsIgnoreCase("BatteryOff") && powerValue) {
                            powerState |= POWER_STATE_BATTERY_OFF;
                        }
                    }
                }
                if(seName != null) {
                    mSeExtension = new ESeInfo(seName.equals(SECURE_ELEMENT_ESE)?SECURE_ELEMENT_ROUTE_ESE:(seName.equals(SECURE_ELEMENT_UICC)?SECURE_ELEMENT_ROUTE_UICC:SECURE_ELEMENT_ROUTE_UICC2),powerState);
                    Log.d(TAG, mSeExtension.toString());
                } else {
                    mSeExtension = new ESeInfo(-1, 0);
                    Log.d(TAG, mSeExtension.toString());
                }
            } finally {
                extParser.close();
            }
        }else {
            if(!onHost) {
                Log.e(TAG, "SE extension not present, Setting default offhost seID");
                mSeExtension = new ESeInfo(SECURE_ELEMENT_ROUTE_UICC, 0);
            }
            else {
                mSeExtension = new ESeInfo(-1, 0);
            }
        }
    }

    static ArrayList<AidGroup> nfcAidGroups2AidGroups(ArrayList<NfcAidGroup> nfcAidGroup) {
        ArrayList<AidGroup> aidGroups = new ArrayList<AidGroup>();
        if(nfcAidGroup != null) {
            for(NfcAidGroup nfcag : nfcAidGroup) {
                AidGroup ag = nfcag.createAidGroup();
                aidGroups.add(ag);
            }
        }
        return aidGroups;
    }

    public void writeToXml(XmlSerializer out) throws IOException {
        out.attribute(null, "description", mDescription);
        String modifiable = "";
        if(mModifiable) {
            modifiable = "true";
        } else {
            modifiable = "false";
        }
        out.attribute(null, "modifiable", modifiable);
        out.attribute(null, "uid", Integer.toString(mUid));
        out.attribute(null, "seId", Integer.toString(mSeExtension.seId));
        out.attribute(null, "bannerId", Integer.toString(mBannerResourceId));
        for (AidGroup group : mDynamicAidGroups.values()) {
            group.writeAsXml(out);
        }
    }

    public ResolveInfo getResolveInfo() {
        return mService;
    }
    /**
     * Returns a consolidated list of AIDs from the AID groups
     * registered by this service. Note that if a service has both
     * a static (manifest-based) AID group for a category and a dynamic
     * AID group, only the dynamically registered AIDs will be returned
     * for that category.
     * @return List of AIDs registered by the service
     */
    public ArrayList<String> getAids() {
        final ArrayList<String> aids = new ArrayList<String>();
        for (NfcAidGroup group : getNfcAidGroups()) {
            aids.addAll(group.getAids());
        }
        return aids;
    }
    /**
     * Returns a consolidated list of AID groups
     * registered by this service. Note that if a service has both
     * a static (manifest-based) AID group for a category and a dynamic
     * AID group, only the dynamically registered AID group will be returned
     * for that category.
     * @return List of AIDs registered by the service
     */
    public ArrayList<NfcAidGroup> getNfcAidGroups() {
        final ArrayList<NfcAidGroup> groups = new ArrayList<NfcAidGroup>();
        for (Map.Entry<String, NfcAidGroup> entry : mDynamicNfcAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        for (Map.Entry<String, NfcAidGroup> entry : mStaticNfcAidGroups.entrySet()) {
            if (!mDynamicNfcAidGroups.containsKey(entry.getKey())) {
                // Consolidate AID groups - don't return static ones
                // if a dynamic group exists for the category.
                groups.add(entry.getValue());
            }
        }
        return groups;
    }


    /**
     * This is a convenience function to create an ApduServiceInfo object of the current
     * NfcApduServiceInfo.
     * It is required for legacy functions which expect an ApduServiceInfo on a Binder
     * interface.
     *
     * @return An ApduServiceInfo object which can be correctly serialized as parcel
     */
    public ApduServiceInfo createApduServiceInfo() {
        return new ApduServiceInfo(this.getResolveInfo(), this.isOnHost(), this.getDescription(),
            nfcAidGroups2AidGroups(this.getStaticNfcAidGroups()), nfcAidGroups2AidGroups(this.getDynamicNfcAidGroups()),
            this.requiresUnlock(), this.getBannerId(), this.getUid(),
            this.getSettingsActivityName());
    }

    /**
     * This api can be used to find the total aid size registered
     * by this service.
     * <p> This returns the size of only {@link #CardEmulation.CATEGORY_OTHER}.
     * <p> This includes both static and dynamic aid groups
     * @param category The category of the corresponding service.{@link #CardEmulation.CATEGORY_OTHER}.
     * @return The aid cache size for particular category.
     */
    public int getAidCacheSize(String category) {
        int aidSize = 0x00;
        if(!CardEmulation.CATEGORY_OTHER.equals(category) || !hasCategory(CardEmulation.CATEGORY_OTHER)) {
            return 0x00;
        }
        aidSize = getAidCacheSizeForCategory(CardEmulation.CATEGORY_OTHER);
        return aidSize;
    }

    public int getAidCacheSizeForCategory(String category) {
        ArrayList<NfcAidGroup> nfcAidGroups = new ArrayList<NfcAidGroup>();
        List<String> aids;
        int aidCacheSize = 0x00;
        int aidLen = 0x00;
        nfcAidGroups.addAll(getStaticNfcAidGroups());
        nfcAidGroups.addAll(getDynamicNfcAidGroups());
        if(nfcAidGroups == null || nfcAidGroups.size() == 0x00) {
            return aidCacheSize;
        }
        for(NfcAidGroup aidCache : nfcAidGroups) {
            if(!aidCache.getCategory().equals(category)) {
                continue;
            }
            aids = aidCache.getAids();
            if (aids == null || aids.size() == 0) {
                continue;
            }
            for(String aid : aids) {
                aidLen = aid.length();
                if(aid.endsWith("*")) {
                    aidLen = aidLen - 1;
                }
                aidCacheSize += aidLen >> 1;
            }
        }
        return aidCacheSize;
    }

    /**
     * This api can be used to find the total aids count registered
     * by this service.
     * <p> This returns the size of only {@link #CardEmulation.CATEGORY_OTHER}.
     * <p> This includes both static and dynamic aid groups
     * @param category The category of the corresponding service.{@link #CardEmulation.CATEGORY_OTHER}.
     * @return The num of aids corresponding to particular cateogry
     */
    public int geTotalAidNum ( String category) {
        int aidTotalNum = 0x00;
        if(!CardEmulation.CATEGORY_OTHER.equals(category) || !hasCategory(CardEmulation.CATEGORY_OTHER)) {
            return 0x00;
        }
        aidTotalNum = getTotalAidNumCategory(CardEmulation.CATEGORY_OTHER);
        return aidTotalNum;
    }

    private int getTotalAidNumCategory( String category) {
        ArrayList<NfcAidGroup> aidGroups = new ArrayList<NfcAidGroup>();
        List<String> aids;
        int aidTotalNum = 0x00;
        aidGroups.addAll(getStaticNfcAidGroups());
        aidGroups.addAll(getDynamicNfcAidGroups());
        if(aidGroups == null || aidGroups.size() == 0x00) {
            return aidTotalNum;
        }
        for(NfcAidGroup aidCache : aidGroups) {
            if(!aidCache.getCategory().equals(category)) {
                continue;
            }
            aids = aidCache.getAids();
            if (aids == null || aids.size() == 0) {
                continue;
            }
            for(String aid : aids) {
                if(aid != null && aid.length() > 0x00) { aidTotalNum++;}
            }
        }
        return aidTotalNum;
    }

    /**@hide */
    public ArrayList<NfcAidGroup> getStaticNfcAidGroups() {
        final ArrayList<NfcAidGroup> groups = new ArrayList<NfcAidGroup>();

        for (Map.Entry<String, NfcAidGroup> entry : mStaticNfcAidGroups.entrySet()) {
                groups.add(entry.getValue());
        }
        return groups;
    }

    /**@hide */
    public ArrayList<NfcAidGroup> getDynamicNfcAidGroups() {
        final ArrayList<NfcAidGroup> groups = new ArrayList<NfcAidGroup>();
        for (Map.Entry<String, NfcAidGroup> entry : mDynamicNfcAidGroups.entrySet()) {
            groups.add(entry.getValue());
        }
        return groups;
    }

    public ESeInfo getSEInfo() {
        return mSeExtension;
    }

    public boolean getModifiable() {
        return mModifiable;
    }

    public Bitmap getBitmapBanner() {
        if(mByteArrayBanner == null) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(mByteArrayBanner, 0, mByteArrayBanner.length);
        return bitmap;
    }

    public void setOrReplaceDynamicNfcAidGroup(NfcAidGroup nfcAidGroup) {
        super.setOrReplaceDynamicAidGroup(nfcAidGroup);
        mDynamicNfcAidGroups.put(nfcAidGroup.getCategory(), nfcAidGroup);
    }

    public NfcAidGroup getDynamicNfcAidGroupForCategory(String category) {
        return mDynamicNfcAidGroups.get(category);
    }

    public boolean removeDynamicNfcAidGroupForCategory(String category) {
        super.removeDynamicAidGroupForCategory(category);
        return (mDynamicNfcAidGroups.remove(category) != null);
    }

    public Drawable loadBanner(PackageManager pm) {
        Resources res;
        Drawable banner;
        try {
            res = pm.getResourcesForApplication(mService.serviceInfo.packageName);
            if(mBannerResourceId == -1) {
                banner = new BitmapDrawable((Bitmap)getBitmapBanner());
            } else {
                banner = res.getDrawable(mBannerResourceId,null);
            }
            return banner;
        } catch (NotFoundException e) {
            Log.e(TAG, "Could not load banner.");
            return null;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Could not load banner.");
            return null;
        }
    }

    public int getBannerId() {
        return mBannerResourceId;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("ApduService: ");
        out.append(getComponent());
        out.append(", description: " + mDescription);
        out.append(", Static AID Groups: ");
        for (NfcAidGroup nfcAidGroup : mStaticNfcAidGroups.values()) {
            out.append(nfcAidGroup.toString());
        }
        out.append(", Dynamic AID Groups: ");
        for (NfcAidGroup nfcAidGroup : mDynamicNfcAidGroups.values()) {
            out.append(nfcAidGroup.toString());
        }
        return out.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NfcApduServiceInfo)) return false;
        NfcApduServiceInfo thatService = (NfcApduServiceInfo) o;

        return thatService.getComponent().equals(this.getComponent());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        mService.writeToParcel(dest, flags);
        dest.writeString(mDescription);
        dest.writeInt(mOnHost ? 1 : 0);
        dest.writeInt(mStaticNfcAidGroups.size());
        if (mStaticNfcAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList<NfcAidGroup>(mStaticNfcAidGroups.values()));
        }
        dest.writeInt(mDynamicNfcAidGroups.size());
        if (mDynamicNfcAidGroups.size() > 0) {
            dest.writeTypedList(new ArrayList<NfcAidGroup>(mDynamicNfcAidGroups.values()));
        }
        dest.writeInt(mRequiresDeviceUnlock ? 1 : 0);
        dest.writeInt(mBannerResourceId);
        dest.writeInt(mUid);
        dest.writeString(mSettingsActivityName);
        mSeExtension.writeToParcel(dest, flags);
        dest.writeByteArray(mByteArrayBanner);
        dest.writeInt(mModifiable ? 1 : 0);
        dest.writeInt(mServiceState);
    };

    public static final Parcelable.Creator<NfcApduServiceInfo> CREATOR =
            new Parcelable.Creator<NfcApduServiceInfo>() {
        @Override
        public NfcApduServiceInfo createFromParcel(Parcel source) {
            ResolveInfo info = ResolveInfo.CREATOR.createFromParcel(source);
            String description = source.readString();
            boolean onHost = source.readInt() != 0;
            ArrayList<NfcAidGroup> staticNfcAidGroups = new ArrayList<NfcAidGroup>();
            int numStaticGroups = source.readInt();
            if (numStaticGroups > 0) {
                source.readTypedList(staticNfcAidGroups, NfcAidGroup.CREATOR);
            }
            ArrayList<NfcAidGroup> dynamicNfcAidGroups = new ArrayList<NfcAidGroup>();
            int numDynamicGroups = source.readInt();
            if (numDynamicGroups > 0) {
                source.readTypedList(dynamicNfcAidGroups, NfcAidGroup.CREATOR);
            }
            boolean requiresUnlock = source.readInt() != 0;
            int bannerResource = source.readInt();
            int uid = source.readInt();
            String settingsActivityName = source.readString();
            ESeInfo seExtension = ESeInfo.CREATOR.createFromParcel(source);
            byte[] byteArrayBanner = new byte[]{0};
            byteArrayBanner = source.createByteArray();
            boolean modifiable = source.readInt() != 0;
            NfcApduServiceInfo service = new NfcApduServiceInfo(info, onHost, description, staticNfcAidGroups,
                    dynamicNfcAidGroups, requiresUnlock, bannerResource, uid,
                    settingsActivityName, seExtension, modifiable);
            service.setServiceState(CardEmulation.CATEGORY_OTHER, source.readInt());
            return service;
        }

        @Override
        public NfcApduServiceInfo[] newArray(int size) {
            return new NfcApduServiceInfo[size];
        }
    };
        public boolean isServiceEnabled(String category) {
            if(category != CardEmulation.CATEGORY_OTHER) {
                return true;
            }

            if((mServiceState ==  NfcConstants.SERVICE_STATE_ENABLED) || (mServiceState ==  NfcConstants.SERVICE_STATE_DISABLING)){
                return true;
            }else{/*SERVICE_STATE_DISABLED or SERVICE_STATE_ENABLING*/
                return false;
            }
        }

        /**
         * This method is invoked before the service is commited to routing table.
         * mServiceState is previous state of the service, and,
         * user is now requesting to enable/disable (using flagEnable) this service
         * before committing all the services to routing table.
         * @param flagEnable To Enable/Disable the service.
         *        FALSE Disable service
         *        TRUE Enable service
         */

        public void enableService(String category ,boolean flagEnable) {
            if(category != CardEmulation.CATEGORY_OTHER) {
                return;
            }
            Log.d(TAG, "setServiceState:Description:" + mDescription + ":InternalState:" + mServiceState + ":flagEnable:"+ flagEnable);
            if(((mServiceState == NfcConstants.SERVICE_STATE_ENABLED) &&    (flagEnable == true )) ||
               ((mServiceState ==  NfcConstants.SERVICE_STATE_DISABLED) &&  (flagEnable == false)) ||
               ((mServiceState ==  NfcConstants.SERVICE_STATE_DISABLING) && (flagEnable == false)) ||
               ((mServiceState ==  NfcConstants.SERVICE_STATE_ENABLING) &&  (flagEnable == true ))){
                /*No change in state*/
                return;
            }
            else if((mServiceState ==  NfcConstants.SERVICE_STATE_ENABLED) && (flagEnable == false)){
                mServiceState =  NfcConstants.SERVICE_STATE_DISABLING;
            }
            else if((mServiceState ==  NfcConstants.SERVICE_STATE_DISABLED) && (flagEnable == true)){
                mServiceState =  NfcConstants.SERVICE_STATE_ENABLING;
            }
            else if((mServiceState ==  NfcConstants.SERVICE_STATE_DISABLING) && (flagEnable == true)){
                mServiceState =  NfcConstants.SERVICE_STATE_ENABLED;
            }
            else if((mServiceState ==  NfcConstants.SERVICE_STATE_ENABLING) && (flagEnable == false)){
                mServiceState =  NfcConstants.SERVICE_STATE_DISABLED;
            }
        }

        public int getServiceState(String category) {
            if(category != CardEmulation.CATEGORY_OTHER) {
                return NfcConstants.SERVICE_STATE_ENABLED;
            }

            return mServiceState;
        }

        public int setServiceState(String category ,int state) {
            if(category != CardEmulation.CATEGORY_OTHER) {
                return NfcConstants.SERVICE_STATE_ENABLED;
            }

            mServiceState = state;
            return mServiceState;
        }

        /**
         * Updates the state of the service based on the commit status
         * This method needs to be invoked after current service is pushed for the commit to routing table
         * @param commitStatus Result of the commit.
         *        FALSE if the commit failed. Reason for ex: there was an overflow of routing table
         *        TRUE if the commit was successful
         */
        public void updateServiceCommitStatus(String category ,boolean commitStatus) {
            if(category != CardEmulation.CATEGORY_OTHER) {
                return;
            }
            Log.d(TAG, "updateServiceCommitStatus:Description:" + mDescription + ":InternalState:" + mServiceState + ":commitStatus:"+ commitStatus);
            if(commitStatus){
                /*Commit was successful and all newly added services were registered,
                 * disabled applications were removed/unregistered from routing entries*/
                if(mServiceState ==  NfcConstants.SERVICE_STATE_DISABLING){
                    mServiceState =  NfcConstants.SERVICE_STATE_DISABLED;
                }
                else if(mServiceState == NfcConstants.SERVICE_STATE_ENABLING){
                    mServiceState = NfcConstants.SERVICE_STATE_ENABLED;
                }

            }else{
                /*Commit failed and all newly added services were not registered successfully.
                 * disabled applications were not successfully disabled*/
                if(mServiceState ==  NfcConstants.SERVICE_STATE_DISABLING){
                    mServiceState =  NfcConstants.SERVICE_STATE_ENABLED;
                }
                else if(mServiceState ==  NfcConstants.SERVICE_STATE_ENABLING){
                    mServiceState =  NfcConstants.SERVICE_STATE_DISABLED;
                }
            }
        }

        static String serviceStateToString(int state) {
            switch (state) {
                case NfcConstants.SERVICE_STATE_DISABLED:
                    return "DISABLED";
                case NfcConstants.SERVICE_STATE_ENABLED:
                    return "ENABLED";
                case NfcConstants.SERVICE_STATE_ENABLING:
                    return "ENABLING";
                case NfcConstants.SERVICE_STATE_DISABLING:
                    return "DISABLING";
                default:
                    return "UNKNOWN";
            }
        }
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            super.dump(fd,pw,args);
            pw.println("    Routing Destination: " + (mOnHost ? "host" : "secure element"));
            if (hasCategory(CardEmulation.CATEGORY_OTHER)) {
                pw.println("    Service State: " + serviceStateToString(mServiceState));
            }
        }

    /**
     * A valid APDU pattern according to ISO/IEC 7816-4:
     * <ul>
     * <li>Has >= 1 bytes and <=124 bytes (>=2 hex chars and <= 248 hex chars)
     * <li>Consist of only hex characters
     * </ul>
     *
     * @hide
     */
    public static boolean isValidApduString(String apdu) {
        if (apdu == null)
            return false;

        if (apdu.length() < 2 || apdu.length() > 248 || ((apdu.length() % 2) != 0)) {
            Log.e(TAG, "APDU " + apdu + " is not a valid apdu pattern.");
            return false;
        }

        // Verify hex characters
        if (!apdu.matches("[0-9A-Fa-f]{2,248}")) {
            Log.e(TAG, "APDU " + apdu + " is not a valid apdu pattern.");
            return false;
        }

        return true;
    }

        public static class ESeInfo implements Parcelable {
            final int seId;
            final int powerState;

            public ESeInfo(int seId, int powerState) {
                this.seId = seId;
                this.powerState = powerState;
            }

            public int getSeId() {
                return seId;
            }

            public int getPowerState() {
                return powerState;
            }

            @Override
            public String toString() {
                StringBuilder out = new StringBuilder("seId: " + seId +
                          ",Power state: [switchOn: " +
                          ((powerState & POWER_STATE_SWITCH_ON) !=0) +
                          ",switchOff: " + ((powerState & POWER_STATE_SWITCH_OFF) !=0) +
                          ",batteryOff: " + ((powerState & POWER_STATE_BATTERY_OFF) !=0) + "]");
                return out.toString();
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeInt(seId);
                dest.writeInt(powerState);
            }

            public static final Parcelable.Creator<NfcApduServiceInfo.ESeInfo> CREATOR =
                    new Parcelable.Creator<NfcApduServiceInfo.ESeInfo>() {

                @Override
                public ESeInfo createFromParcel(Parcel source) {
                    int seId = source.readInt();
                    int powerState = source.readInt();
                    return new ESeInfo(seId, powerState);
                }

                @Override
                public ESeInfo[] newArray(int size) {
                    return new ESeInfo[size];
                }
            };
        }
    }