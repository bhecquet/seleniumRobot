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

import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.List;

//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.mobile.AdbWrapper;
import com.seleniumtests.browserfactory.mobile.MobileDevice;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.osutility.OSCommand;

//@PrepareForTest({OSCommand.class, AdbWrapper.class})
public class TestAdbWrapper extends MockitoTest {
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testAdbNotFound() {
//		PowerMockito.mockStatic(OSCommand.class);
//		PowerMockito.mockStatic(System.class);
		when(System.getenv("ANDROID_HOME")).thenReturn(null);
		when(System.getenv("ANDROID_SDK_ROOT")).thenReturn(null);
		when(OSCommand.executeCommandAndWait("adb version")).thenReturn("Cannot run program \"adb\": CreateProcess error=2, Le fichier spécifié est introuvable");
		new AdbWrapper();
	}
	
	@Test(groups={"ut"})
	public void testAdbFoundInEnvVariable() {
//		PowerMockito.mockStatic(OSCommand.class);
//		PowerMockito.mockStatic(System.class);
		when(System.getenv("ANDROID_HOME")).thenReturn("/opt/android-sdk/");
		when(System.getenv("ANDROID_SDK_ROOT")).thenReturn(null);
		when(OSCommand.executeCommandAndWait("adb version")).thenReturn("Cannot run program \"adb\": CreateProcess error=2, Le fichier spécifié est introuvable");
		when(OSCommand.executeCommandAndWait(Paths.get("/opt/android-sdk/platform-tools/adb version").toString())).thenReturn("Android Debug Bridge version 1.0.32\nRevision 09a0d98bebce-android");
		
		AdbWrapper adb = new AdbWrapper();
		Assert.assertEquals(adb.getAdbVersion(), "1.0.32");
	}
	
	/**
	 * Test with ANDROID_SDK_ROOT env var
	 */
	@Test(groups={"ut"})
	public void testAdbFoundInEnvVariable2() {
//		PowerMockito.mockStatic(OSCommand.class);
//		PowerMockito.mockStatic(System.class);
		when(System.getenv("ANDROID_HOME")).thenReturn(null);
		when(System.getenv("ANDROID_SDK_ROOT")).thenReturn("/opt/android-sdk/");
		when(OSCommand.executeCommandAndWait("adb version")).thenReturn("Cannot run program \"adb\": CreateProcess error=2, Le fichier spécifié est introuvable");
		when(OSCommand.executeCommandAndWait(Paths.get("/opt/android-sdk/platform-tools/adb version").toString())).thenReturn("Android Debug Bridge version 1.0.32\nRevision 09a0d98bebce-android");
		
		AdbWrapper adb = new AdbWrapper();
		Assert.assertEquals(adb.getAdbVersion(), "1.0.32");
	}
	
	@Test(groups={"ut"})
	public void testAdbPresentInSystemPath() {
//		PowerMockito.mockStatic(OSCommand.class);
//		PowerMockito.mockStatic(System.class);
		when(System.getenv("ANDROID_HOME")).thenReturn(null);
		when(System.getenv("ANDROID_SDK_ROOT")).thenReturn(null);
		when(OSCommand.executeCommandAndWait("adb version")).thenReturn("Android Debug Bridge version 1.0.32\nRevision 09a0d98bebce-android");
		
		AdbWrapper adb = new AdbWrapper();
		Assert.assertEquals(adb.getAdbVersion(), "1.0.32");
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testAdbNotPresentInSystemPath() {
//		PowerMockito.mockStatic(OSCommand.class);
//		PowerMockito.mockStatic(System.class);
		when(System.getenv("ANDROID_HOME")).thenReturn(null);
		when(System.getenv("ANDROID_SDK_ROOT")).thenReturn(null);
		when(OSCommand.executeCommandAndWait("adb version")).thenReturn("adb command not found");
		
		new AdbWrapper();
	}
	
	@Test(groups={"ut"})
	public void testDeviceList() {
//		PowerMockito.mockStatic(OSCommand.class);
//		PowerMockito.mockStatic(System.class);
		when(System.getenv("ANDROID_HOME")).thenReturn(null);
		when(System.getenv("ANDROID_SDK_ROOT")).thenReturn(null);
		when(OSCommand.executeCommandAndWait("adb version")).thenReturn("Android Debug Bridge version 1.0.32\nRevision 09a0d98bebce-android");
		when(OSCommand.executeCommandAndWait("adb -s emulator-5554 shell \"dumpsys package com.android.browser | grep versionName\"")).thenReturn("  versionName=6.0-123.4");
		when(OSCommand.executeCommandAndWait("adb -s emulator-5554 shell \"dumpsys package com.android.chrome | grep versionName\"")).thenReturn("  versionName=56.0.123.4");
		when(OSCommand.executeCommandAndWait("adb devices")).thenReturn("List of devices attached\n"
																		+ "emulator-5554   device");
		when(OSCommand.executeCommandAndWait("adb -s emulator-5554 shell getprop")).thenReturn("[dalvik.vm.dex2oat-Xms]: [64m]\n"
																		+ "[dalvik.vm.dex2oat-Xmx]: [512m]\n"
																		+ "[dalvik.vm.heapsize]: [512m]\n"
																		+ "[dalvik.vm.image-dex2oat-Xms]: [64m]\n"
																		+ "[dalvik.vm.image-dex2oat-Xmx]: [64m]\n"
																		+ "[dalvik.vm.isa.arm.features]: [default]\n"
																		+ "[dalvik.vm.stack-trace-file]: [/data/anr/traces.txt]\n"
																		+ "[debug.force_rtl]: [0]\n"
																		+ "[dev.bootcomplete]: [1]\n"
																		+ "[gsm.current.phone-type]: [1]\n"
																		+ "[gsm.defaultpdpcontext.active]: [true]\n"
																		+ "[gsm.network.type]: [UMTS]\n"
																		+ "[gsm.nitz.time]: [1478705557978]\n"
																		+ "[gsm.operator.alpha]: [Android]\n"
																		+ "[gsm.operator.iso-country]: [us]\n"
																		+ "[gsm.operator.isroaming]: [false]\n"
																		+ "[gsm.operator.numeric]: [310260]\n"
																		+ "[gsm.sim.operator.alpha]: [Android]\n"
																		+ "[gsm.sim.operator.iso-country]: [us]\n"
																		+ "[gsm.sim.operator.numeric]: [310260]\n"
																		+ "[gsm.sim.state]: [READY]\n"
																		+ "[gsm.version.ril-impl]: [android reference-ril 1.0]\n"
																		+ "[init.svc.adbd]: [running]\n"
																		+ "[init.svc.bootanim]: [stopped]\n"
																		+ "[init.svc.console]: [running]\n"
																		+ "[init.svc.debuggerd]: [running]\n"
																		+ "[init.svc.drm]: [running]\n"
																		+ "[init.svc.fuse_sdcard]: [running]\n"
																		+ "[init.svc.goldfish-logcat]: [stopped]\n"
																		+ "[init.svc.goldfish-setup]: [stopped]\n"
																		+ "[init.svc.healthd]: [running]\n"
																		+ "[init.svc.installd]: [running]\n"
																		+ "[init.svc.keystore]: [running]\n"
																		+ "[init.svc.lmkd]: [running]\n"
																		+ "[init.svc.logd]: [running]\n"
																		+ "[init.svc.media]: [running]\n"
																		+ "[init.svc.netd]: [running]\n"
																		+ "[init.svc.qemu-props]: [stopped]\n"
																		+ "[init.svc.qemud]: [running]\n"
																		+ "[init.svc.ril-daemon]: [running]\n"
																		+ "[init.svc.servicemanager]: [running]\n"
																		+ "[init.svc.surfaceflinger]: [running]\n"
																		+ "[init.svc.ueventd]: [running]\n"
																		+ "[init.svc.vold]: [running]\n"
																		+ "[init.svc.zygote]: [running]\n"
																		+ "[net.bt.name]: [Android]\n"
																		+ "[net.change]: [net.dns3]\n"
																		+ "[net.dns1]: [10.0.2.3]\n"
																		+ "[net.dns2]: [10.0.2.4]\n"
																		+ "[net.dns3]: [10.0.2.5]\n"
																		+ "[net.eth0.dns1]: [10.0.2.3]\n"
																		+ "[net.eth0.dns2]: [10.0.2.4]\n"
																		+ "[net.eth0.dns3]: [10.0.2.5]\n"
																		+ "[net.eth0.gw]: [10.0.2.2]\n"
																		+ "[net.gprs.local-ip]: [10.0.2.15]\n"
																		+ "[net.hostname]: [android-ac19bf2a38442f23]\n"
																		+ "[net.qtaguid_enabled]: [1]\n"
																		+ "[net.tcp.default_init_rwnd]: [60]\n"
																		+ "[persist.sys.country]: [US]\n"
																		+ "[persist.sys.dalvik.vm.lib.2]: [libart.so]\n"
																		+ "[persist.sys.language]: [en]\n"
																		+ "[persist.sys.localevar]: []\n"
																		+ "[persist.sys.profiler_ms]: [0]\n"
																		+ "[persist.sys.timezone]: [GMT]\n"
																		+ "[persist.sys.usb.config]: [adb]\n"
																		+ "[qemu.hw.mainkeys]: [0]\n"
																		+ "[qemu.sf.fake_camera]: [none]\n"
																		+ "[qemu.sf.lcd_density]: [480]\n"
																		+ "[rild.libargs]: [-d /dev/ttyS0]\n"
																		+ "[rild.libpath]: [/system/lib/libreference-ril.so]\n"
																		+ "[ro.allow.mock.location]: [1]\n"
																		+ "[ro.baseband]: [unknown]\n"
																		+ "[ro.board.platform]: []\n"
																		+ "[ro.boot.hardware]: [goldfish]\n"
																		+ "[ro.bootloader]: [unknown]\n"
																		+ "[ro.bootmode]: [unknown]\n"
																		+ "[ro.build.characteristics]: [default]\n"
																		+ "[ro.build.date.utc]: [1423923410]\n"
																		+ "[ro.build.date]: [Sat Feb 14 14:16:50 UTC 2015]\n"
																		+ "[ro.build.description]: [sdk_phone_armv7-eng 5.1 LKY45 1737576 test-keys]\n"
																		+ "[ro.build.display.id]: [sdk_phone_armv7-eng 5.1 LKY45 1737576 test-keys]\n"
																		+ "[ro.build.fingerprint]: [generic/sdk_phone_armv7/generic:5.1/LKY45/1737576:eng/test-keys]\n"
																		+ "[ro.build.flavor]: [sdk_phone_armv7-eng]\n"
																		+ "[ro.build.host]: [vpbs4.mtv.corp.google.com]\n"
																		+ "[ro.build.id]: [LKY45]\n"
																		+ "[ro.build.product]: [generic]\n"
																		+ "[ro.build.tags]: [test-keys]\n"
																		+ "[ro.build.type]: [eng]\n"
																		+ "[ro.build.user]: [android-build]\n"
																		+ "[ro.build.version.all_codenames]: [REL]\n"
																		+ "[ro.build.version.codename]: [REL]\n"
																		+ "[ro.build.version.incremental]: [1737576]\n"
																		+ "[ro.build.version.release]: [5.1]\n"
																		+ "[ro.build.version.sdk]: [22]\n"
																		+ "[ro.com.google.locationfeatures]: [1]\n"
																		+ "[ro.config.alarm_alert]: [Alarm_Classic.ogg]\n"
																		+ "[ro.config.nocheckin]: [yes]\n"
																		+ "[ro.config.notification_sound]: [OnTheHunt.ogg]\n"
																		+ "[ro.crypto.state]: [unencrypted]\n"
																		+ "[ro.dalvik.vm.native.bridge]: [0]\n"
																		+ "[ro.debuggable]: [1]\n"
																		+ "[ro.factorytest]: [0]\n"
																		+ "[ro.hardware]: [goldfish]\n"
																		+ "[ro.kernel.android.checkjni]: [1]\n"
																		+ "[ro.kernel.android.qemud]: [ttyS1]\n"
																		+ "[ro.kernel.androidboot.hardware]: [goldfish]\n"
																		+ "[ro.kernel.console]: [ttyS0]\n"
																		+ "[ro.kernel.ndns]: [3]\n"
																		+ "[ro.kernel.qemu.gles]: [1]\n"
																		+ "[ro.kernel.qemu]: [1]\n"
																		+ "[ro.opengles.version]: [131072]\n"
																		+ "[ro.product.board]: []\n"
																		+ "[ro.product.brand]: [generic]\n"
																		+ "[ro.product.cpu.abi2]: [armeabi]\n"
																		+ "[ro.product.cpu.abi]: [armeabi-v7a]\n"
																		+ "[ro.product.cpu.abilist32]: [armeabi-v7a,armeabi]\n"
																		+ "[ro.product.cpu.abilist64]: []\n"
																		+ "[ro.product.cpu.abilist]: [armeabi-v7a,armeabi]\n"
																		+ "[ro.product.device]: [generic]\n"
																		+ "[ro.product.locale.language]: [en]\n"
																		+ "[ro.product.locale.region]: [US]\n"
																		+ "[ro.product.manufacturer]: [unknown]\n"
																		+ "[ro.product.model]: [sdk_phone_armv7]\n"
																		+ "[ro.product.name]: [sdk_phone_armv7]\n"
																		+ "[ro.radio.use-ppp]: [no]\n"
																		+ "[ro.revision]: [0]\n"
																		+ "[ro.runtime.firstboot]: [1478705722255]\n"
																		+ "[ro.secure]: [0]\n"
																		+ "[ro.serialno]: []\n"
																		+ "[ro.setupwizard.mode]: [EMULATOR]\n"
																		+ "[ro.wifi.channels]: []\n"
																		+ "[ro.zygote]: [zygote32]\n"
																		+ "[selinux.reload_policy]: [1]\n"
																		+ "[service.bootanim.exit]: [1]\n"
																		+ "[status.battery.level]: [5]\n"
																		+ "[status.battery.level_raw]: [50]\n"
																		+ "[status.battery.level_scale]: [9]\n"
																		+ "[status.battery.state]: [Slow]\n"
																		+ "[sys.boot_completed]: [1]\n"
																		+ "[sys.settings_global_version]: [2]\n"
																		+ "[sys.settings_system_version]: [4]\n"
																		+ "[sys.sysctl.extra_free_kbytes]: [24300]\n"
																		+ "[sys.sysctl.tcp_def_init_rwnd]: [60]\n"
																		+ "[sys.usb.config]: [adb]\n"
																		+ "[sys.usb.state]: [adb]\n"
																		+ "[wlan.driver.status]: [unloaded]\n"
																		+ "[xmpp.auto-presence]: [true]\n");
		
		when(OSCommand.executeCommandAndWait("adb -s emulator-5554 shell \"pm list packages\"")).thenReturn("package:com.example.android.livecubes\n"
																				+ "package:com.android.providers.telephony\n"
																				+ "package:com.android.providers.calendar\n"
																				+ "package:com.android.providers.media\n"
																				+ "package:com.google.android.onetimeinitializer\n"
																				+ "package:com.android.wallpapercropper\n"
																				+ "package:com.android.documentsui\n"
																				+ "package:com.android.galaxy4\n"
																				+ "package:com.android.externalstorage\n"
																				+ "package:com.android.htmlviewer\n"
																				+ "package:com.android.mms.service\n"
																				+ "package:com.android.providers.downloads\n"
																				+ "package:com.android.messaging\n"
																				+ "package:com.android.browser\n"
																				+ "package:com.google.android.configupdater\n"
																				+ "package:com.android.soundrecorder\n"
																				+ "package:com.android.defcontainer\n"
																				+ "package:com.android.providers.downloads.ui\n"
																				+ "package:com.android.vending\n"
																				+ "package:com.android.pacprocessor\n"
																				+ "package:com.android.certinstaller\n"
																				+ "package:com.android.carrierconfig\n"
																				+ "package:com.google.android.launcher.layouts.genymotion\n"
																				+ "package:android\n"
																				+ "package:com.android.contacts\n"
																				+ "package:com.android.camera2\n"
																				+ "package:com.android.launcher3\n"
																				+ "package:com.android.backupconfirm\n"
																				+ "package:com.android.statementservice\n"
																				+ "package:com.android.wallpaper.holospiral\n"
																				+ "package:com.android.calendar\n"
																				+ "package:com.android.phasebeam\n"
																				+ "package:com.google.android.setupwizard\n"
																				+ "package:com.android.providers.settings\n"
																				+ "package:com.android.sharedstoragebackup\n"
																				+ "package:com.android.printspooler\n"
																				+ "package:com.android.dreams.basic\n"
																				+ "package:com.android.webview\n"
																				+ "package:com.android.inputdevices\n"
																				+ "package:com.android.providers.calllogbackup\n"
																				+ "package:com.android.musicfx\n"
																				+ "package:com.android.development_settings\n"
																				+ "package:com.android.server.telecom\n"
																				+ "package:com.google.android.syncadapters.contacts\n"
																				+ "package:com.android.keychain\n"
																				+ "package:com.android.chrome\n"
																				+ "package:com.android.dialer\n"
																				+ "package:com.android.gallery3d\n"
																				+ "package:com.google.android.gms\n"
																				+ "package:com.google.android.gsf\n"
																				+ "package:com.google.android.tts\n"
																				+ "package:com.google.android.partnersetup\n"
																				+ "package:com.android.packageinstaller\n"
																				+ "package:com.svox.pico\n"
																				+ "package:com.example.android.apis\n"
																				+ "package:com.android.proxyhandler\n"
																				+ "package:com.android.inputmethod.latin\n"
																				+ "package:com.google.android.feedback\n"
																				+ "package:com.google.android.syncadapters.calendar\n"
																				+ "package:com.android.managedprovisioning\n"
																				+ "package:com.android.dreams.phototable\n"
																				+ "package:com.android.noisefield\n"
																				+ "package:com.google.android.gsf.login\n"
																				+ "package:com.android.smspush\n"
																				+ "package:com.android.wallpaper.livepicker\n"
																				+ "package:com.amaze.filemanager\n"
																				+ "package:com.google.android.backuptransport\n"
																				+ "package:jp.co.omronsoft.openwnn\n"
																				+ "package:com.android.settings\n"
																				+ "package:com.android.calculator2\n"
																				+ "package:com.android.gesture.builder\n"
																				+ "package:com.android.wallpaper\n"
																				+ "package:com.android.vpndialogs\n"
																				+ "package:com.android.email\n"
																				+ "package:com.android.music\n"
																				+ "package:com.android.phone\n"
																				+ "package:com.android.shell\n"
																				+ "package:com.android.providers.userdictionary\n"
																				+ "package:com.android.location.fused\n"
																				+ "package:com.android.deskclock\n"
																				+ "package:com.android.systemui\n"
																				+ "package:com.android.exchange\n"
																				+ "package:com.android.bluetoothmidiservice\n"
																				+ "package:com.android.customlocale2\n"
																				+ "package:io.appium.unlock\n"
																				+ "package:com.android.bluetooth\n"
																				+ "package:com.android.development\n"
																				+ "package:com.android.providers.contacts\n"
																				+ "package:com.android.captiveportallogin\n");
		
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
