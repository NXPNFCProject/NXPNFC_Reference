Patch(felica_on_host_native.patch) is required to be applied for FelicaOnHost(HCE-F) 
as per sony requirement in Android-M environment.

1. Shell commands for applying patch(felica_on_host_native.patch):
    cd AndroidBuildDirectory/frameworks/base
    cp MW/nxp_nci_hal_internal/test_apps/FelicaOnHost/felica_on_host_native.patch  . 
    patch -p1 < felica_on_host_native.patch
    rm core/res/res/values/attrs.xml.orig
    cd ../../
    make update-api
    
2. After Binaries are copied to the target device additional permission file(.xml) needs to be copied 
   cd AndroidBuildDirectory/
   cd frameworks/native/data/etc/
   cp android.hardware.nfc.hce.xml android.hardware.nfc.hcef.xml
        Edit: Replace hce to hcef in this new .xml created.
        i.e, feature name="android.hardware.nfc.hcef"
   adb push android.hardware.nfc.hcef.xml /system/etc/permissions/
   
   
