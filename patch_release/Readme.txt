Steps to integrate the hotfix patches on top of the MW release - NFC_AR_00_6000_11.04.00_OpnSrc

The patch contains the fix for :
1) Fix to correct the CTS failure case (Nfc Preferred Payment Test from Cts Nfc Test Cases).
2) Fix to correct Wrong route location being thrown when queried for payment service.

In order to apply this fix, ensure your workspace is on the tag - NFC_AR_00_6000_11.04.00_OpnSrc
1) Copy the patch available under NXPNFC_Reference/patch_release/nfcandroid_frameworks to your workpace of GitHub repo - nfcandroid_frameworks & apply it.
2) Copy the patch available under NXPNFC_Reference/patch_release/NFC_NCIHAL_base to your workpace of GitHub repo - NFC_NCIHAL_base & apply it.

Thank you!
