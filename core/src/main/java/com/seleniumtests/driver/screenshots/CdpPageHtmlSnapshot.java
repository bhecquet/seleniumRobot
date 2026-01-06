package com.seleniumtests.driver.screenshots;

import com.seleniumtests.browserfactory.chrome.ChromiumUtils;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import jakarta.activation.DataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CdpPageHtmlSnapshot {
    private static final Logger logger = SeleniumRobotLogger.getLogger(CdpPageHtmlSnapshot.class);

    private CustomEventFiringWebDriver driver;

    public CdpPageHtmlSnapshot(CustomEventFiringWebDriver driver) {
        this.driver = driver;
    }

    public String getFullPageSource() throws MessagingException, IOException {

        String mhtml = ChromiumUtils.captureSnapshot(driver.getWebDriver());

        if (mhtml == null) {
            logger.warn("Could not get page from browser");
            return "";
        }

        // La chaîne est un MIME "multipart/related"
        byte[] bytes = mhtml.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        DataSource ds = new ByteArrayDataSource(bytes, "multipart/related");
        Multipart mp = new MimeMultipart(ds);

        String html = null;

        String rootDocument = "";
        Map<String, String> parts = new LinkedHashMap<>();

        for (int i = 0; i < mp.getCount(); i++) {
            BodyPart part = mp.getBodyPart(i);
            String contentType = part.getContentType(); // ex: "text/html; charset=UTF-8"
            if (contentType != null && contentType.toLowerCase().startsWith("text/html")) {
                Object content = part.getContent();

                try (java.io.InputStream is = (java.io.InputStream) content) {
                    html = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }

                // remove template tag so that shadowed element are now inlined
                html = html.replaceAll("<template[^>]*+>", "").replace("</template>", "");

                // assume the first part is the root document
                if (i == 0) {
                    rootDocument = html;
                } else {
                    parts.put(((MimeBodyPart)part).getContentID().replace("<", "").replace(">", ""), html);
                }
            }
        }

        for (Map.Entry<String, String> entry : parts.entrySet()) {
            entry.setValue(replaceIframeReferenceByContent(entry.getValue(), parts));

            if (entry.getKey().startsWith("frame-")) {
                rootDocument = rootDocument
                        // for frame in root document
                        .replace("src=\"cid:" + entry.getKey() + "\"", "srcdoc=\"" + StringUtility.encodeString(entry.getValue(), "xml") + "\"");
            }
        }

        return rootDocument;
    }

    private String replaceIframeReferenceByContent(String frameContent, Map<String, String> parts) {
        Pattern pattern = Pattern.compile("src=\"cid:(frame-.*?)\"", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(frameContent);

        // remove meta tag that is added by chrome
        frameContent = frameContent.replaceAll("<meta[^>]*+>", "");

        while (matcher.find()) {
            String frameId = matcher.group(1);
            String subFrame = replaceIframeReferenceByContent(parts.get(frameId), parts);
            frameContent = frameContent.replace("src=\"cid:" + frameId + "\"", "srcdoc=\"" + StringUtility.encodeString(subFrame, "xml") + "\"");
        }

        return frameContent;


    }
}
