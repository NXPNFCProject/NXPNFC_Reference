# Copyright 2018 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_STEM := nfc/BoardConfigNfcPartial.mk

#BOARD_KERNEL_CMDLINE += androidboot.selinux=permissive
BOARD_KERNEL_CMDLINE += video=HDMI-A-1:1280x800@60

DEVICE_MANIFEST_FILE += vendor/nxp/SNxxx/manifest.xml

ifeq ($(SE_ENABLED),TRUE)
	DEVICE_MANIFEST_FILE += vendor/nxp/SNxxx/manifestEse.xml
endif

TARGET_FS_CONFIG_GEN += vendor/nxp/SNxxx/config.fs

-include vendor/nxp/$(LOCAL_STEM)