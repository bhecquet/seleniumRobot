package com.seleniumtests.ut.driver.screenshots;

import static org.mockito.Mockito.any;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.chrome.ChromiumUtils;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.screenshots.CdpPageHtmlSnapshot;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestCdpPageHtmlSnapshot extends MockitoTest {

    @Mock
    private CustomEventFiringWebDriver driver;

    /**
     * Check that frames in frames are correctly encoded and put in source with srcdoc attribute
     */
    @Test(groups = "ut")
    public void testGetFullPageSourceWithFramesInFrames() throws Exception {
        try (MockedStatic<ChromiumUtils> mockedDriver = Mockito.mockStatic(ChromiumUtils.class)) {
            mockedDriver.when(() -> ChromiumUtils.captureSnapshot(any())).thenReturn("""
                    From: <Saved by Blink>
                    Snapshot-Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/test.html
                    Subject: Test page
                    Date: Tue, 30 Dec 2025 15:57:51 +0100
                    MIME-Version: 1.0
                    Content-Type: multipart/related;
                    	type="text/html";
                    	boundary="----MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----"
                    
                    
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    Content-Type: text/html
                    Content-ID: <frame-E88044948E82FD1338414FFD36455D5A@mhtml.blink>
                    Content-Transfer-Encoding: quoted-printable
                    Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/test.html
                    
                    <!DOCTYPE html><html><head><meta http-equiv=3D"Content-Type" content=3D"tex=
                    t/html; charset=3Dwindows-1252"><link rel=3D"stylesheet" type=3D"text/css" =
                    href=3D"cid:css-3727bb5a-8122-4759-a022-f13030969cf0@mhtml.blink" />
                    <title>Test page</title>
                    
                    </head>
                    =20
                    <body style=3D"">
                    	<div style=3D"background:#FFFF00;height:6px;width:100%;position:fixed;top:=
                    0px;left:0px;"></div>
                    
                    	<h3>Test IFrame</h3>
                    	<iframe src=3D"cid:frame-5C06395B25807A91AC05F007933FEFA7@mhtml.blink" id=
                    =3D"myIFrame" width=3D"600px" height=3D"180px"></iframe>
                    =09
                    
                    </body></html>
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    Content-Type: text/html
                    Content-ID: <frame-5C06395B25807A91AC05F007933FEFA7@mhtml.blink>
                    Content-Transfer-Encoding: quoted-printable
                    Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/testIFrame.html
                    
                    <html><head><meta http-equiv=3D"Content-Type" content=3D"text/html; charset=
                    =3Dwindows-1252"></head><body>
                    				<input id=3D"textInIFrame">
                    			=09
                    				<iframe src=3D"cid:frame-BEDA5154CDC2BEDAEE506242F3E449C4@mhtml.blink" =
                    name=3D"mySecondIFrame" width=3D"500px" height=3D"100px"></iframe>
                    				<button id=3D"closeButton">close</button>
                    		=09
                    		</body></html>
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    Content-Type: text/html
                    Content-ID: <frame-BEDA5154CDC2BEDAEE506242F3E449C4@mhtml.blink>
                    Content-Transfer-Encoding: quoted-printable
                    Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/testIFrame3.html
                    
                    <html><head><meta http-equiv=3D"Content-Type" content=3D"text/html; charset=
                    =3Dwindows-1252"></head><body>
                    				<input id=3D"textInIFrameWithValue3" value=3D"an other value in an othe=
                    r iframe">
                                    <iframe src=3D"cid:frame-A3DC33419AA328AB5B54BE2A7F72F9F2@mhtml.blink" =
                    name=3D"myThirdIFrame" width=3D"500px" height=3D"30px"></iframe>
                    		=09
                    		</body></html>
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    Content-Type: text/html
                    Content-ID: <frame-A3DC33419AA328AB5B54BE2A7F72F9F2@mhtml.blink>
                    Content-Transfer-Encoding: quoted-printable
                    Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/testIFrame2.html
                    
                    <html><head><meta http-equiv=3D"Content-Type" content=3D"text/html; charset=
                    =3Dwindows-1252"></head><body>
                    				<input id=3D"textInIFrameWithValue2" value=3D"an other value in iframe"=
                    >
                    		=09
                    		</body></html>
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    
                    """);

            String source = new CdpPageHtmlSnapshot(driver).getFullPageSource();
            Assert.assertTrue(source.contains("<iframe srcdoc=\"&lt;html&gt;&lt;head&gt;&lt;/head&gt;&lt;body&gt;\n" +
                    "\t\t\t\t&lt;input id=&quot;textInIFrame&quot;&gt;")); // first frame is encoded
            Assert.assertTrue(source.contains("&lt;iframe srcdoc=&quot;&amp;lt;html&amp;gt;&amp;lt;head&amp;gt;&amp;lt;/head&amp;gt;&amp;lt;body&amp;gt;\n" +
                    "\t\t\t\t&amp;lt;input id=&amp;quot;textInIFrameWithValue3")); // second frame is double encoded
            Assert.assertTrue(source.contains("&amp;lt;iframe srcdoc=&amp;quot;&amp;amp;lt;html&amp;amp;gt;&amp;amp;lt;head&amp;amp;gt;&amp;amp;lt;/head&amp;amp;gt;&amp;amp;lt;body&amp;amp;gt;\n" +
                    "\t\t\t\t&amp;amp;lt;input id=&amp;amp;quot;textInIFrameWithValue2")); // third frame is triple encoded
        }
    }

    /**
     * 2 top level frames, their content is encoded
     */
    @Test(groups = "ut")
    public void testGetFullPageSourceWithMultipleFrames() throws Exception {
        try (MockedStatic<ChromiumUtils> mockedDriver = Mockito.mockStatic(ChromiumUtils.class)) {
            mockedDriver.when(() -> ChromiumUtils.captureSnapshot(any())).thenReturn("""
                    From: <Saved by Blink>
                    Snapshot-Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/test.html
                    Subject: Test page
                    Date: Tue, 30 Dec 2025 15:57:51 +0100
                    MIME-Version: 1.0
                    Content-Type: multipart/related;
                    	type="text/html";
                    	boundary="----MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----"
                    
                    
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    Content-Type: text/html
                    Content-ID: <frame-E88044948E82FD1338414FFD36455D5A@mhtml.blink>
                    Content-Transfer-Encoding: quoted-printable
                    Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/test.html
                    
                    <!DOCTYPE html><html><head><meta http-equiv=3D"Content-Type" content=3D"tex=
                    t/html; charset=3Dwindows-1252"><link rel=3D"stylesheet" type=3D"text/css" =
                    href=3D"cid:css-3727bb5a-8122-4759-a022-f13030969cf0@mhtml.blink" />
                    <title>Test page</title>
                    
                    </head>
                    =20
                    <body style=3D"">
                    	<div style=3D"background:#FFFF00;height:6px;width:100%;position:fixed;top:=
                    0px;left:0px;"></div>
                    
                    	<h3>Test IFrame</h3>
                    	<iframe src=3D"cid:frame-5C06395B25807A91AC05F007933FEFA7@mhtml.blink" id=
                    =3D"myIFrame" width=3D"600px" height=3D"180px"></iframe>
                        <iframe src=3D"cid:frame-A3DC33419AA328AB5B54BE2A7F72F9F2@mhtml.blink" id=
                    =3D"myIFrame2" width=3D"600px" height=3D"180px"></iframe>
                    =09
                    
                    </body></html>
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    Content-Type: text/html
                    Content-ID: <frame-5C06395B25807A91AC05F007933FEFA7@mhtml.blink>
                    Content-Transfer-Encoding: quoted-printable
                    Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/testIFrame.html
                    
                    <html><head><meta http-equiv=3D"Content-Type" content=3D"text/html; charset=
                    =3Dwindows-1252"></head><body>
                    				<input id=3D"textInIFrame">
                    			=09
                    				<iframe src=3D"cid:frame-BEDA5154CDC2BEDAEE506242F3E449C4@mhtml.blink" =
                    name=3D"mySecondIFrame" width=3D"500px" height=3D"100px"></iframe>
                    				<button id=3D"closeButton">close</button>
                    		=09
                    		</body></html>
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    Content-Type: text/html
                    Content-ID: <frame-BEDA5154CDC2BEDAEE506242F3E449C4@mhtml.blink>
                    Content-Transfer-Encoding: quoted-printable
                    Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/testIFrame3.html
                    
                    <html><head><meta http-equiv=3D"Content-Type" content=3D"text/html; charset=
                    =3Dwindows-1252"></head><body>
                    				<input id=3D"textInIFrameWithValue3" value=3D"an other value in an othe=
                    r iframe">
                    		=09
                    		</body></html>
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    Content-Type: text/html
                    Content-ID: <frame-A3DC33419AA328AB5B54BE2A7F72F9F2@mhtml.blink>
                    Content-Transfer-Encoding: quoted-printable
                    Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/testIFrame2.html
                    
                    <html><head><meta http-equiv=3D"Content-Type" content=3D"text/html; charset=
                    =3Dwindows-1252"></head><body>
                    				<input id=3D"textInIFrameWithValue2" value=3D"an other value in iframe"=
                    >
                    		=09
                    		</body></html>
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    
                    """);

            String source = new CdpPageHtmlSnapshot(driver).getFullPageSource();
            Assert.assertTrue(source.contains("<iframe srcdoc=\"&lt;html&gt;&lt;head&gt;&lt;/head&gt;&lt;body&gt;\n" +
                    "\t\t\t\t&lt;input id=&quot;textInIFrame&quot;&gt;")); // first frame is encoded
            Assert.assertTrue(source.contains("&lt;iframe srcdoc=&quot;&amp;lt;html&amp;gt;&amp;lt;head&amp;gt;&amp;lt;/head&amp;gt;&amp;lt;body&amp;gt;\n" +
                    "\t\t\t\t&amp;lt;input id=&amp;quot;textInIFrameWithValue3")); // sub frame is double encoded
            Assert.assertTrue(source.contains("<iframe srcdoc=\"&lt;html&gt;&lt;head&gt;&lt;/head&gt;&lt;body&gt;\n" +
                    "\t\t\t\t&lt;input id=&quot;textInIFrameWithValue2")); // second frame is encoded
        }
    }

    /**
     * 2 frames in a sub frames, both must be double encoded
     */
    @Test(groups = "ut")
    public void testGetFullPageSourceWithMultipleFramesInFrames() throws Exception {
        try (MockedStatic<ChromiumUtils> mockedDriver = Mockito.mockStatic(ChromiumUtils.class)) {
            mockedDriver.when(() -> ChromiumUtils.captureSnapshot(any())).thenReturn("""
                    From: <Saved by Blink>
                    Snapshot-Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/test.html
                    Subject: Test page
                    Date: Tue, 30 Dec 2025 15:57:51 +0100
                    MIME-Version: 1.0
                    Content-Type: multipart/related;
                    	type="text/html";
                    	boundary="----MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----"
                    
                    
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    Content-Type: text/html
                    Content-ID: <frame-E88044948E82FD1338414FFD36455D5A@mhtml.blink>
                    Content-Transfer-Encoding: quoted-printable
                    Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/test.html
                    
                    <!DOCTYPE html><html><head><meta http-equiv=3D"Content-Type" content=3D"tex=
                    t/html; charset=3Dwindows-1252"><link rel=3D"stylesheet" type=3D"text/css" =
                    href=3D"cid:css-3727bb5a-8122-4759-a022-f13030969cf0@mhtml.blink" />
                    <title>Test page</title>
                    
                    </head>
                    =20
                    <body style=3D"">
                    	<div style=3D"background:#FFFF00;height:6px;width:100%;position:fixed;top:=
                    0px;left:0px;"></div>
                    
                    	<h3>Test IFrame</h3>
                    	<iframe src=3D"cid:frame-5C06395B25807A91AC05F007933FEFA7@mhtml.blink" id=
                    =3D"myIFrame" width=3D"600px" height=3D"180px"></iframe>
                    =09
                    
                    </body></html>
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    Content-Type: text/html
                    Content-ID: <frame-5C06395B25807A91AC05F007933FEFA7@mhtml.blink>
                    Content-Transfer-Encoding: quoted-printable
                    Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/testIFrame.html
                    
                    <html><head><meta http-equiv=3D"Content-Type" content=3D"text/html; charset=
                    =3Dwindows-1252"></head><body>
                    				<input id=3D"textInIFrame">
                    			=09
                    				<iframe src=3D"cid:frame-BEDA5154CDC2BEDAEE506242F3E449C4@mhtml.blink" =
                    name=3D"mySecondIFrame" width=3D"500px" height=3D"100px"></iframe>
                                     <iframe src=3D"cid:frame-A3DC33419AA328AB5B54BE2A7F72F9F2@mhtml.blink" =
                    name=3D"myThirdIFrame" width=3D"500px" height=3D"30px"></iframe>
                    				<button id=3D"closeButton">close</button>
                    		=09
                    		</body></html>
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    Content-Type: text/html
                    Content-ID: <frame-BEDA5154CDC2BEDAEE506242F3E449C4@mhtml.blink>
                    Content-Transfer-Encoding: quoted-printable
                    Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/testIFrame3.html
                    
                    <html><head><meta http-equiv=3D"Content-Type" content=3D"text/html; charset=
                    =3Dwindows-1252"></head><body>
                    				<input id=3D"textInIFrameWithValue3" value=3D"an other value in an othe=
                    r iframe">
                    		=09
                    		</body></html>
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    Content-Type: text/html
                    Content-ID: <frame-A3DC33419AA328AB5B54BE2A7F72F9F2@mhtml.blink>
                    Content-Transfer-Encoding: quoted-printable
                    Content-Location: file:///D:/Dev/seleniumRobot/seleniumRobot-core/core/target/test-classes/tu/testIFrame2.html
                    
                    <html><head><meta http-equiv=3D"Content-Type" content=3D"text/html; charset=
                    =3Dwindows-1252"></head><body>
                    				<input id=3D"textInIFrameWithValue2" value=3D"an other value in iframe"=
                    >
                    		=09
                    		</body></html>
                    ------MultipartBoundary--FY34XUbEx5qymkvIN0rhsUviM0LjCJ4VKYlkPTlED2----
                    
                    """);

            String source = new CdpPageHtmlSnapshot(driver).getFullPageSource();
            Assert.assertTrue(source.contains("<iframe srcdoc=\"&lt;html&gt;&lt;head&gt;&lt;/head&gt;&lt;body&gt;\n" +
                    "\t\t\t\t&lt;input id=&quot;textInIFrame&quot;&gt;")); // first frame is encoded
            Assert.assertTrue(source.contains("&lt;iframe srcdoc=&quot;&amp;lt;html&amp;gt;&amp;lt;head&amp;gt;&amp;lt;/head&amp;gt;&amp;lt;body&amp;gt;\n" +
                    "\t\t\t\t&amp;lt;input id=&amp;quot;textInIFrameWithValue3")); // first sub frame is double encoded
            Assert.assertTrue(source.contains("&lt;iframe srcdoc=&quot;&amp;lt;html&amp;gt;&amp;lt;head&amp;gt;&amp;lt;/head&amp;gt;&amp;lt;body&amp;gt;\n" +
                    "\t\t\t\t&amp;lt;input id=&amp;quot;textInIFrameWithValue2")); // second sub frame is double encoded
        }
    }
}
