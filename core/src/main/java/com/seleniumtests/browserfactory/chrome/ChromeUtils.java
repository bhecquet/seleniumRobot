package com.seleniumtests.browserfactory.chrome;

import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.har.*;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.logging.LogEntry;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ChromeUtils {

    private static final Logger logger = SeleniumRobotLogger.getLogger(WebUIDriver.class);

    public static Har parsePerformanceLogs(List<LogEntry> logEntries, List<TestStep> testSteps) {
        Map<String, HashMap<String, Object>> requests = new LinkedHashMap<>();

        Har har = new Har();
        Log log = har.getLog();

        Map<Long, Page> pageStart = new LinkedHashMap<>();
        Map<Page, Boolean> usedPages = new LinkedHashMap<>();
        int id = 0;
        for (TestStep testStep: testSteps) {
            Instant instant = Instant.ofEpochMilli(testStep.getStartDate().getTime());
            String pageId = String.format("page_" + id++);
            Page page = new Page(instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), pageId, testStep.getName());
            pageStart.put(testStep.getStartDate().getTime(), page);
            usedPages.put(page, false);
        }

        for (LogEntry line: logEntries) {
            JSONObject jsonObject = null;

            // only read "Network.responseReceived" messages as they contain everything
            try {
                jsonObject = new JSONObject(line.getMessage());
                JSONObject messageObject = jsonObject.getJSONObject("message");
                String method = messageObject.getString("method");
                switch (method) {
                    // message format: {"message":{"method":"Network.requestWillBeSent","params":{"documentURL":"http://10.25.4.70:53669/test.html","frameId":"145E40AEF6F7A76C61973C3946CA0992","hasUserGesture":false,"initiator":{"columnNumber":180,"lineNumber":59,"type":"parser","url":"http://10.25.4.70:53669/test.html"},"loaderId":"6AAED31A84393CDE33A22E12ACA3924B","redirectHasExtraInfo":false,"request":{"headers":{"Referer":"http://10.25.4.70:53669/test.html","User-Agent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"},"initialPriority":"Medium","isSameSite":true,"method":"GET","mixedContentType":"none","referrerPolicy":"strict-origin-when-cross-origin","url":"http://10.25.4.70:53669/googleSearch.png"},"requestId":"34060.2","timestamp":171300.445026,"type":"Image","wallTime":1739344558.303674}},"webview":"145E40AEF6F7A76C61973C3946CA0992"}
                    case "Network.requestWillBeSent" -> {
                        String requestId = messageObject.getJSONObject("params").getString("requestId");
                        requests.put(requestId, new HashMap<>());
                        requests.get(requestId).put("requestWillBeSent", messageObject);
                    }
                    // message format: {"message":{"method":"Network.responseReceived","params":{"frameId":"8C5E01A9EE0BD7556C532FCFBE04EE5D","hasExtraInfo":true,"loaderId":"3149B492109FC8E15361AE661A188A05","requestId":"3149B492109FC8E15361AE661A188A05","response":{"alternateProtocolUsage":"unspecifiedReason","charset":"","connectionId":109,"connectionReused":true,"encodedDataLength":101,"fromDiskCache":false,"fromPrefetchCache":false,"fromServiceWorker":false,"headers":{"Content-Length":"124","Date":"Tue, 11 Feb 2025 09:02:20 GMT","Server":"Jetty(11.0.24)"},"mimeType":"text/html","protocol":"http/1.1","remoteIPAddress":"10.200.38.44","remotePort":51230,"responseTime":1.739264540255014e+12,"securityState":"insecure","status":200,"statusText":"OK","timing":{"connectEnd":-1,"connectStart":-1,"dnsEnd":-1,"dnsStart":-1,"proxyEnd":4.019,"proxyStart":2.804,"pushEnd":0,"pushStart":0,"receiveHeadersEnd":11.977,"receiveHeadersStart":5.365,"requestTime":91274.541644,"sendEnd":4.238,"sendStart":4.129,"sslEnd":-1,"sslStart":-1,"workerFetchStart":-1,"workerReady":-1,"workerRespondWithSettled":-1,"workerStart":-1},"url":"http://10.200.38.44:51230/testIFrame3.html"},"timestamp":91274.558756,"type":"Document"}},"webview":"0327E68CEF262C8D77818DC5C8B14339"}
                    case "Network.responseReceived" -> {
                        String requestId = messageObject.getJSONObject("params").getString("requestId");
                        requests.get(requestId).put("responseReceived", messageObject);
                    }
                    // {"message":{"method":"Network.loadingFinished","params":{"encodedDataLength":924,"requestId":"28364.2","timestamp":192318.813115}},"webview":"D6FD686EEEFF1CF429E60E5D8F6A8D71"}
                    case "Network.loadingFinished" -> {
                        String requestId = messageObject.getJSONObject("params").getString("requestId");
                        requests.get(requestId).put("loadingFinished", messageObject);
                    }
                    case "Network.loadingFailed" -> {
                        String requestId = messageObject.getJSONObject("params").getString("requestId");
                        requests.get(requestId).put("loadingFailed", messageObject);
                    }
                    // {"method":"Network.responseReceivedExtraInfo","params":{"blockedCookies":[],"cookiePartitionKey":{"hasCrossSiteAncestor":false,"topLevelSite":"http://127.0.0.1"},"cookiePartitionKeyOpaque":false,"exemptedCookies":[],"headers":{"Content-Length":"2538","Date":"Fri, 14 Feb 2025 15:42:13 GMT","Server":"Jetty(11.0.24)"},"headersText":"HTTP/1.1 200 OK\r\nDate: Fri, 14 Feb 2025 15:42:13 GMT\r\nContent-Length: 2538\r\nServer: Jetty(11.0.24)\r\n\r\n","requestId":"28364.5","resourceIPAddressSpace":"Local","statusCode":200}},"webview":"D6FD686EEEFF1CF429E60E5D8F6A8D71"}
                    // use to get the real status code (in case of cache loading)
                    case "Network.responseReceivedExtraInfo" -> {
                        String requestId = messageObject.getJSONObject("params").getString("requestId");
                        requests.get(requestId).put("responseReceivedExtraInfo", messageObject);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (Map.Entry<String, HashMap<String, Object>> requestsEntry: requests.entrySet()) {
            String requestId = requestsEntry.getKey();
            try {

                JSONObject jsonRequest = (JSONObject) requestsEntry.getValue().get("requestWillBeSent");
                JSONObject jsonRequestHeaders = jsonRequest.getJSONObject("params").getJSONObject("request").getJSONObject("headers");

                Request request = new Request(0,
                        jsonRequest.getJSONObject("params").getJSONObject("request").getString("method"),
                        jsonRequest.getJSONObject("params").getJSONObject("request").getString("url"),
                        "HTTP N/A",
                        jsonRequestHeaders.keySet().stream().map(key -> new Header(key, jsonRequestHeaders.getString(key))).collect(Collectors.toList()),
                        new ArrayList<>(), // cookies
                        new ArrayList<>(),
                        0
                );

                JSONObject jsonResponse = (JSONObject) requestsEntry.getValue().get("responseReceived");
                ;
                JSONObject jsonResponseHeaders = jsonResponse.getJSONObject("params").getJSONObject("response").getJSONObject("headers");

                int statusCode = jsonResponse.getJSONObject("params").getJSONObject("response").getInt("status");
                if (requestsEntry.getValue().get("responseReceivedExtraInfo") != null) {
                    JSONObject jsonResponseExtraInfo = (JSONObject) requestsEntry.getValue().get("responseReceivedExtraInfo");
                    statusCode = jsonResponseExtraInfo.getJSONObject("params").getInt("statusCode");
                }

                Response response = new Response(
                        statusCode,
                        jsonResponse.getJSONObject("params").getJSONObject("response").getString("statusText"),
                        jsonResponse.getJSONObject("params").getJSONObject("response").getString("protocol"),
                        jsonResponseHeaders.keySet().stream().map(key -> new Header(key, jsonResponseHeaders.getString(key))).collect(Collectors.toList()),
                        new ArrayList<>(), // cookies
                        new Content(
                                jsonResponse.getJSONObject("params").getJSONObject("response").getString("mimeType"),
                                jsonResponseHeaders.optInt("Content-Length", 0),
                                "_masked_"
                        ),
                        "",
                        jsonResponse.getJSONObject("params").getJSONObject("response").getInt("encodedDataLength"),
                        jsonResponseHeaders.optInt("Content-Length", 0)
                );

                Timing timings;
                double endLoadingTimestamp = jsonResponse.getJSONObject("params").getDouble("timestamp");
                try {
                    JSONObject jsonTimings = jsonResponse.getJSONObject("params").getJSONObject("response").getJSONObject("timing");
                    double startLoadingTimestamp = jsonTimings.getDouble("requestTime");
                    if (requestsEntry.getValue().get("loadingFinished") != null) {
                        JSONObject jsonLoadingFinished = (JSONObject) requestsEntry.getValue().get("loadingFinished");
                        endLoadingTimestamp = jsonLoadingFinished.getJSONObject("params").getDouble("timestamp");
                    }

                    // for details about timings: https://chromedevtools.github.io/devtools-protocol/tot/Network/
                    timings = new Timing(
                            // assume that 'blocked' is the time between 'requestTime' and start of proxy negociation
                            Math.min(jsonTimings.getDouble("proxyStart"), jsonTimings.getDouble("sendStart")),
                            jsonTimings.getDouble("dnsEnd") == -1 ? -1 : jsonTimings.getDouble("dnsEnd") - jsonTimings.getDouble("dnsStart"),
                            jsonTimings.getDouble("connectEnd") == -1 ? -1 : jsonTimings.getDouble("connectEnd") - jsonTimings.getDouble("connectStart"),
                            jsonTimings.getDouble("sslEnd") == -1 ? -1 : jsonTimings.getDouble("sslEnd") - jsonTimings.getDouble("sslStart"),
                            jsonTimings.getDouble("sendEnd") - jsonTimings.getDouble("sendStart"),
                            jsonTimings.getDouble("receiveHeadersStart") - jsonTimings.getDouble("sendEnd"),
                            (endLoadingTimestamp - startLoadingTimestamp) * 1000 - jsonTimings.getDouble("receiveHeadersEnd")
                    );
                } catch (JSONException e) {
                    // when timings are not found (case for files), create a stub object
                    timings = new Timing(
                            0, 0, 0, 0, 0, 0, 0
                    );
                }

                double duration = (endLoadingTimestamp - jsonRequest.getJSONObject("params").getDouble("timestamp")) * 1000;

                long entryDate = (long) (jsonRequest.getJSONObject("params").getDouble("wallTime") * 1000);

                Page pageRef = null;
                for (Map.Entry<Long, Page> pageEntry : pageStart.entrySet()) {
                    if (entryDate > pageEntry.getKey() || pageRef == null) {
                        pageRef = pageEntry.getValue();
                    }
                }
                usedPages.replace(pageRef, true);

                Entry entry = new Entry(
                        pageRef.getId(),
                        Instant.ofEpochMilli((long) (jsonRequest.getJSONObject("params").getDouble("wallTime") * 1000)).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        request,
                        response,
                        timings,
                        (int) duration);
                log.addEntry(entry);
            } catch (JSONException e) {
                logger.error("Error parsing request " + requestId, e);
            }
        }

        // add only used pages
        for (Map.Entry<Page, Boolean> pageEntry: usedPages.entrySet()) {
            //if (Boolean.TRUE.equals(pageEntry.getValue())) {
                log.addPage(pageEntry.getKey());
            //}
        }

        // use of kong jsonObject so that sub-objects are serialized
        return har;
    }
}
