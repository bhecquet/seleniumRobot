/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.ut.browserfactory.mobile;

import static org.mockito.Mockito.mockStatic;

import java.nio.file.Paths;
import java.util.List;

import com.seleniumtests.util.osutility.SystemUtility;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.mobile.AdbWrapper;
import com.seleniumtests.browserfactory.mobile.MobileDevice;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.osutility.OSCommand;

public class TestAdbWrapper extends MockitoTest {
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testAdbNotFound() {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<SystemUtility> mockedSystem = mockStatic(SystemUtility.class)) {
			mockedSystem.when(() -> SystemUtility.getenv("ANDROID_HOME")).thenReturn(null);
			mockedSystem.when(() -> SystemUtility.getenv("ANDROID_SDK_ROOT")).thenReturn(null);
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"adb", "version"})).thenReturn("Cannot run program \"adb\": CreateProcess error=2, Le fichier spécifié est introuvable");
			new AdbWrapper();
		}
	}
	
	@Test(groups={"ut"})
	public void testAdbFoundInEnvVariable() {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<SystemUtility> mockedSystem = mockStatic(SystemUtility.class)) {
			mockedSystem.when(() -> SystemUtility.getenv("ANDROID_HOME")).thenReturn("/opt/android-sdk/");
			mockedSystem.when(() -> SystemUtility.getenv("ANDROID_SDK_ROOT")).thenReturn(null);
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"adb", "version"})).thenReturn("Cannot run program \"adb\": CreateProcess error=2, Le fichier spécifié est introuvable");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {Paths.get("/opt/android-sdk/platform-tools/adb").toString(), "version"})).thenReturn("Android Debug Bridge version 1.0.32\nRevision 09a0d98bebce-android");

			AdbWrapper adb = new AdbWrapper();
			Assert.assertEquals(adb.getAdbVersion(), "1.0.32");
		}
	}
	
	/**
	 * Test with ANDROID_SDK_ROOT env var
	 */
	@Test(groups={"ut"})
	public void testAdbFoundInEnvVariable2() {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<SystemUtility> mockedSystem = mockStatic(SystemUtility.class)) {
			mockedSystem.when(() -> SystemUtility.getenv("ANDROID_HOME")).thenReturn(null);
			mockedSystem.when(() -> SystemUtility.getenv("ANDROID_SDK_ROOT")).thenReturn("/opt/android-sdk/");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"adb", "version"})).thenReturn("Cannot run program \"adb\": CreateProcess error=2, Le fichier spécifié est introuvable");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {Paths.get("/opt/android-sdk/platform-tools/adb").toString(), "version"})).thenReturn("Android Debug Bridge version 1.0.32\nRevision 09a0d98bebce-android");

			AdbWrapper adb = new AdbWrapper();
			Assert.assertEquals(adb.getAdbVersion(), "1.0.32");
		}
	}
	
	@Test(groups={"ut"})
	public void testAdbPresentInSystemPath() {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<SystemUtility> mockedSystem = mockStatic(SystemUtility.class)) {
			mockedSystem.when(() -> SystemUtility.getenv("ANDROID_HOME")).thenReturn(null);
			mockedSystem.when(() -> SystemUtility.getenv("ANDROID_SDK_ROOT")).thenReturn(null);
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"adb", "version"})).thenReturn("Android Debug Bridge version 1.0.32\nRevision 09a0d98bebce-android");

			AdbWrapper adb = new AdbWrapper();
			Assert.assertEquals(adb.getAdbVersion(), "1.0.32");
		}
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testAdbNotPresentInSystemPath() {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<SystemUtility> mockedSystem = mockStatic(SystemUtility.class)) {
			mockedSystem.when(() -> SystemUtility.getenv("ANDROID_HOME")).thenReturn(null);
			mockedSystem.when(() -> SystemUtility.getenv("ANDROID_SDK_ROOT")).thenReturn(null);
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"adb", "version"})).thenReturn("adb command not found");

			new AdbWrapper();
		}
	}
	
	@Test(groups={"ut"})
	public void testDeviceList() {
		try (MockedStatic<OSCommand> mockedOsCommand = mockStatic(OSCommand.class);
			 MockedStatic<SystemUtility> mockedSystem = mockStatic(SystemUtility.class)) {
			mockedSystem.when(() -> SystemUtility.getenv("ANDROID_HOME")).thenReturn(null);
			mockedSystem.when(() -> SystemUtility.getenv("ANDROID_SDK_ROOT")).thenReturn(null);
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"adb", "version"})).thenReturn("Android Debug Bridge version 1.0.32\nRevision 09a0d98bebce-android");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"adb", "-s", "emulator-5554", "shell", "\"dumpsys package com.android.browser | grep versionName\""})).thenReturn("  versionName=6.0-123.4");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"adb", "-s", "emulator-5554", "shell", "\"dumpsys package com.android.chrome | grep versionName\""})).thenReturn("  versionName=56.0.123.4");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"adb", "devices"})).thenReturn("""
List of devices attached
emulator-5554   device""");
			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"adb", "-s", "emulator-5554", "shell", "getprop"})).thenReturn("""
[dalvik.vm.dex2oat-Xms]: [64m]
[dalvik.vm.dex2oat-Xmx]: [512m]
[dalvik.vm.heapsize]: [512m]
[dalvik.vm.image-dex2oat-Xms]: [64m]
[dalvik.vm.image-dex2oat-Xmx]: [64m]
[dalvik.vm.isa.arm.features]: [default]
[dalvik.vm.stack-trace-file]: [/data/anr/traces.txt]
[debug.force_rtl]: [0]
[dev.bootcomplete]: [1]
[gsm.current.phone-type]: [1]
[gsm.defaultpdpcontext.active]: [true]
[gsm.network.type]: [UMTS]
[gsm.nitz.time]: [1478705557978]
[gsm.operator.alpha]: [Android]
[gsm.operator.iso-country]: [us]
[gsm.operator.isroaming]: [false]
[gsm.operator.numeric]: [310260]
[gsm.sim.operator.alpha]: [Android]
[gsm.sim.operator.iso-country]: [us]
[gsm.sim.operator.numeric]: [310260]
[gsm.sim.state]: [READY]
[gsm.version.ril-impl]: [android reference-ril 1.0]
[init.svc.adbd]: [running]
[init.svc.bootanim]: [stopped]
[init.svc.console]: [running]
[init.svc.debuggerd]: [running]
[init.svc.drm]: [running]
[init.svc.fuse_sdcard]: [running]
[init.svc.goldfish-logcat]: [stopped]
[init.svc.goldfish-setup]: [stopped]
[init.svc.healthd]: [running]
[init.svc.installd]: [running]
[init.svc.keystore]: [running]
[init.svc.lmkd]: [running]
[init.svc.logd]: [running]
[init.svc.media]: [running]
[init.svc.netd]: [running]
[init.svc.qemu-props]: [stopped]
[init.svc.qemud]: [running]
[init.svc.ril-daemon]: [running]
[init.svc.servicemanager]: [running]
[init.svc.surfaceflinger]: [running]
[init.svc.ueventd]: [running]
[init.svc.vold]: [running]
[init.svc.zygote]: [running]
[net.bt.name]: [Android]
[net.change]: [net.dns3]
[net.dns1]: [10.0.2.3]
[net.dns2]: [10.0.2.4]
[net.dns3]: [10.0.2.5]
[net.eth0.dns1]: [10.0.2.3]
[net.eth0.dns2]: [10.0.2.4]
[net.eth0.dns3]: [10.0.2.5]
[net.eth0.gw]: [10.0.2.2]
[net.gprs.local-ip]: [10.0.2.15]
[net.hostname]: [android-ac19bf2a38442f23]
[net.qtaguid_enabled]: [1]
[net.tcp.default_init_rwnd]: [60]
[persist.sys.country]: [US]
[persist.sys.dalvik.vm.lib.2]: [libart.so]
[persist.sys.language]: [en]
[persist.sys.localevar]: []
[persist.sys.profiler_ms]: [0]
[persist.sys.timezone]: [GMT]
[persist.sys.usb.config]: [adb]
[qemu.hw.mainkeys]: [0]
[qemu.sf.fake_camera]: [none]
[qemu.sf.lcd_density]: [480]
[rild.libargs]: [-d /dev/ttyS0]
[rild.libpath]: [/system/lib/libreference-ril.so]
[ro.allow.mock.location]: [1]
[ro.baseband]: [unknown]
[ro.board.platform]: []
[ro.boot.hardware]: [goldfish]
[ro.bootloader]: [unknown]
[ro.bootmode]: [unknown]
[ro.build.characteristics]: [default]
[ro.build.date.utc]: [1423923410]
[ro.build.date]: [Sat Feb 14 14:16:50 UTC 2015]
[ro.build.description]: [sdk_phone_armv7-eng 5.1 LKY45 1737576 test-keys]
[ro.build.display.id]: [sdk_phone_armv7-eng 5.1 LKY45 1737576 test-keys]
[ro.build.fingerprint]: [generic/sdk_phone_armv7/generic:5.1/LKY45/1737576:eng/test-keys]
[ro.build.flavor]: [sdk_phone_armv7-eng]
[ro.build.host]: [vpbs4.mtv.corp.google.com]
[ro.build.id]: [LKY45]
[ro.build.product]: [generic]
[ro.build.tags]: [test-keys]
[ro.build.type]: [eng]
[ro.build.user]: [android-build]
[ro.build.version.all_codenames]: [REL]
[ro.build.version.codename]: [REL]
[ro.build.version.incremental]: [1737576]
[ro.build.version.release]: [5.1]
[ro.build.version.sdk]: [22]
[ro.com.google.locationfeatures]: [1]
[ro.config.alarm_alert]: [Alarm_Classic.ogg]
[ro.config.nocheckin]: [yes]
[ro.config.notification_sound]: [OnTheHunt.ogg]
[ro.crypto.state]: [unencrypted]
[ro.dalvik.vm.native.bridge]: [0]
[ro.debuggable]: [1]
[ro.factorytest]: [0]
[ro.hardware]: [goldfish]
[ro.kernel.android.checkjni]: [1]
[ro.kernel.android.qemud]: [ttyS1]
[ro.kernel.androidboot.hardware]: [goldfish]
[ro.kernel.console]: [ttyS0]
[ro.kernel.ndns]: [3]
[ro.kernel.qemu.gles]: [1]
[ro.kernel.qemu]: [1]
[ro.opengles.version]: [131072]
[ro.product.board]: []
[ro.product.brand]: [generic]
[ro.product.cpu.abi2]: [armeabi]
[ro.product.cpu.abi]: [armeabi-v7a]
[ro.product.cpu.abilist32]: [armeabi-v7a,armeabi]
[ro.product.cpu.abilist64]: []
[ro.product.cpu.abilist]: [armeabi-v7a,armeabi]
[ro.product.device]: [generic]
[ro.product.locale.language]: [en]
[ro.product.locale.region]: [US]
[ro.product.manufacturer]: [unknown]
[ro.product.model]: [sdk_phone_armv7]
[ro.product.name]: [sdk_phone_armv7]
[ro.radio.use-ppp]: [no]
[ro.revision]: [0]
[ro.runtime.firstboot]: [1478705722255]
[ro.secure]: [0]
[ro.serialno]: []
[ro.setupwizard.mode]: [EMULATOR]
[ro.wifi.channels]: []
[ro.zygote]: [zygote32]
[selinux.reload_policy]: [1]
[service.bootanim.exit]: [1]
[status.battery.level]: [5]
[status.battery.level_raw]: [50]
[status.battery.level_scale]: [9]
[status.battery.state]: [Slow]
[sys.boot_completed]: [1]
[sys.settings_global_version]: [2]
[sys.settings_system_version]: [4]
[sys.sysctl.extra_free_kbytes]: [24300]
[sys.sysctl.tcp_def_init_rwnd]: [60]
[sys.usb.config]: [adb]
[sys.usb.state]: [adb]
[wlan.driver.status]: [unloaded]
[xmpp.auto-presence]: [true]""");

			mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"adb", "-s", "emulator-5554", "shell", "\"pm list packages\""})).thenReturn("""
					package:com.example.android.livecubes
package:com.android.providers.telephony
package:com.android.providers.calendar
package:com.android.providers.media
package:com.google.android.onetimeinitializer
package:com.android.wallpapercropper
package:com.android.documentsui
package:com.android.galaxy4
package:com.android.externalstorage
package:com.android.htmlviewer
package:com.android.mms.service
package:com.android.providers.downloads
package:com.android.messaging
package:com.android.browser
package:com.google.android.configupdater
package:com.android.soundrecorder
package:com.android.defcontainer
package:com.android.providers.downloads.ui
package:com.android.vending
package:com.android.pacprocessor
package:com.android.certinstaller
package:com.android.carrierconfig
package:com.google.android.launcher.layouts.genymotion
package:android
package:com.android.contacts
package:com.android.camera2
package:com.android.launcher3
package:com.android.backupconfirm
package:com.android.statementservice
package:com.android.wallpaper.holospiral
package:com.android.calendar
package:com.android.phasebeam
package:com.google.android.setupwizard
package:com.android.providers.settings
package:com.android.sharedstoragebackup
package:com.android.printspooler
package:com.android.dreams.basic
package:com.android.webview
package:com.android.inputdevices
package:com.android.providers.calllogbackup
package:com.android.musicfx
package:com.android.development_settings
package:com.android.server.telecom
package:com.google.android.syncadapters.contacts
package:com.android.keychain
package:com.android.chrome
package:com.android.dialer
package:com.android.gallery3d
package:com.google.android.gms
package:com.google.android.gsf
package:com.google.android.tts
package:com.google.android.partnersetup
package:com.android.packageinstaller
package:com.svox.pico
package:com.example.android.apis
package:com.android.proxyhandler
package:com.android.inputmethod.latin
package:com.google.android.feedback
package:com.google.android.syncadapters.calendar
package:com.android.managedprovisioning
package:com.android.dreams.phototable
package:com.android.noisefield
package:com.google.android.gsf.login
package:com.android.smspush
package:com.android.wallpaper.livepicker
package:com.amaze.filemanager
package:com.google.android.backuptransport
package:jp.co.omronsoft.openwnn
package:com.android.settings
package:com.android.calculator2
package:com.android.gesture.builder
package:com.android.wallpaper
package:com.android.vpndialogs
package:com.android.email
package:com.android.music
package:com.android.phone
package:com.android.shell
package:com.android.providers.userdictionary
package:com.android.location.fused
package:com.android.deskclock
package:com.android.systemui
package:com.android.exchange
package:com.android.bluetoothmidiservice
package:com.android.customlocale2
package:io.appium.unlock
package:com.android.bluetooth
package:com.android.development
package:com.android.providers.contacts
package:com.android.captiveportallogin""");

			AdbWrapper adb = new AdbWrapper();
			List<MobileDevice> mobiles = adb.getDeviceList();
			Assert.assertEquals(mobiles.size(), 1);
			Assert.assertEquals(mobiles.get(0).getId(), "emulator-5554");
			Assert.assertEquals(mobiles.get(0).getName(), "sdk_phone_armv7");
			Assert.assertEquals(mobiles.get(0).getPlatform(), "android");
			Assert.assertEquals(mobiles.get(0).getVersion(), "5.1");
			Assert.assertEquals(mobiles.get(0).getBrowsers().size(), 2);
			Assert.assertEquals(mobiles.get(0).getBrowsers().get(0).getBrowser(), BrowserType.BROWSER);
			Assert.assertEquals(mobiles.get(0).getBrowsers().get(0).getVersion(), "6.0");
			Assert.assertEquals(mobiles.get(0).getBrowsers().get(1).getBrowser(), BrowserType.CHROME);
			Assert.assertEquals(mobiles.get(0).getBrowsers().get(1).getVersion(), "56.0");
		}
	}

}
