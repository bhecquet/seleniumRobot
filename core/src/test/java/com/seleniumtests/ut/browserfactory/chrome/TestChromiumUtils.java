package com.seleniumtests.ut.browserfactory.chrome;

import com.seleniumtests.GenericTest;
import com.seleniumtests.browserfactory.chrome.ChromiumUtils;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.har.Har;
import com.seleniumtests.util.har.WebSocketEntry;

import org.openqa.selenium.logging.LogEntry;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TestChromiumUtils extends GenericTest {

	@Test(groups = "ut")
	public void testParsePerformanceLogsWebSocket() throws IOException {
		List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-with-websockets.txt");
		
		List<TestStep> testSteps = new ArrayList<>();
		testSteps.add(new TestStep("step 1"));
		testSteps.get(0).setTimestamp(Instant.ofEpochMilli(1739547733421L).atZone(ZoneId.systemDefault()).toOffsetDateTime());
		testSteps.add(new TestStep("step 2"));
		testSteps.get(1).setTimestamp(Instant.ofEpochMilli(1739547734198L).atZone(ZoneId.systemDefault()).toOffsetDateTime());
		
		Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);
		
		// check pages
		Assert.assertEquals(har.getLog().getPages().size(), 2);
		Assert.assertEquals(har.getLog().getPages().get(0).getTitle(), "step 1");
		Assert.assertEquals(har.getLog().getPages().get(0).getId(), "page_0");
		Assert.assertTrue(har.getLog().getPages().get(0).getStartedDateTime().startsWith("2025-02-14T")); // to avoid problems on other time zones
		
		// check entries
		Assert.assertEquals(har.getLog().getEntries().size(), 13);
		Assert.assertEquals(har.getLog().getEntries().get(10).getRequest().getMethod(), "GET");
		Assert.assertEquals(har.getLog().getEntries().get(10).getRequest().getUrl(), "wss://127.0.0.1:0001/test");
		Assert.assertEquals(har.getLog().getEntries().get(10).getRequest().getHeaders().size(), 13);
		Assert.assertEquals(har.getLog().getEntries().get(10).getResponse().getStatus(), 101);
		Assert.assertEquals(har.getLog().getEntries().get(10).getResponse().getStatusText(), "Switching Protocols");
		Assert.assertEquals(har.getLog().getEntries().get(10).getResponse().getHttpVersion(), "HTTP/1.1");
		Assert.assertEquals(har.getLog().getEntries().get(10).getResponse().getHeaders().size(), 8);
		Assert.assertEquals(har.getLog().getEntries().get(10).getResponse().getHeadersSize(), 0);
		Assert.assertEquals(har.getLog().getEntries().get(10).getResponse().getBodySize(), 0);
		Assert.assertEquals(har.getLog().getEntries().get(10).getPageref(), "page_1");
		Assert.assertEquals(har.getLog().getEntries().get(10).getTime(), 3202);
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().size(), 9);
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(1).getData(), "somePayloa [...] esntMatter");
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(5).getData(), "Payloads less than 20 chars are redacted.");
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(0).getType(), "receive");
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(1).getType(), "send");
		Assert.assertNotEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getResponse().getContent(), null);
		Assert.assertTrue(har.getLog().getEntries().get(10).getStartedDateTime().startsWith("2025-03-26T"));
		
		// check transition between pages, based on timings
		Assert.assertEquals(har.getLog().getEntries().get(7).getPageref(), "page_0");
		Assert.assertEquals(har.getLog().getEntries().get(8).getPageref(), "page_1");
	}
	
	@Test(groups = "ut")
	public void testParsePerformanceLogsWebSocketNoHandshake() throws IOException {
		List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-with-websockets-no-handshake.txt");
		
		List<TestStep> testSteps = new ArrayList<>();
		testSteps.add(new TestStep("step 1"));
		testSteps.get(0).setTimestamp(Instant.ofEpochMilli(1739547733421L).atZone(ZoneId.systemDefault()).toOffsetDateTime());
		testSteps.add(new TestStep("step 2"));
		testSteps.get(1).setTimestamp(Instant.ofEpochMilli(1739547734198L).atZone(ZoneId.systemDefault()).toOffsetDateTime());
		
		Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);
		
		// check pages
		Assert.assertEquals(har.getLog().getPages().size(), 2);
		Assert.assertEquals(har.getLog().getPages().get(0).getTitle(), "step 1");
		Assert.assertEquals(har.getLog().getPages().get(0).getId(), "page_0");
		Assert.assertTrue(har.getLog().getPages().get(0).getStartedDateTime().startsWith("2025-02-14T")); // to avoid problems on other time zones
		
		// check entries
		Assert.assertEquals(har.getLog().getEntries().size(), 13);
		Assert.assertEquals(har.getLog().getEntries().get(10).getRequest().getMethod(), "GET");
		Assert.assertEquals(har.getLog().getEntries().get(10).getRequest().getUrl(), "wss://127.0.0.1:0001/test");
		Assert.assertEquals(har.getLog().getEntries().get(10).getRequest().getHeaders().size(), 0);
		Assert.assertNotEquals(har.getLog().getEntries().get(10).getResponse(), null);
		Assert.assertEquals(har.getLog().getEntries().get(10).getPageref(), "");
		Assert.assertEquals(har.getLog().getEntries().get(10).getTime(), -1);
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().size(), 9);
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(1).getData(), "somePayloa [...] esntMatter");
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(5).getData(), "Payloads less than 20 chars are redacted.");
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(0).getType(), "receive");
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(1).getType(), "send");
		Assert.assertTrue(har.getLog().getEntries().get(10).getStartedDateTime().startsWith("1970-01-01T"));
		
		// check transition between pages, based on timings
		Assert.assertEquals(har.getLog().getEntries().get(7).getPageref(), "page_0");
		Assert.assertEquals(har.getLog().getEntries().get(8).getPageref(), "page_1");
	}
	
	@Test(groups = "ut")
	public void testParsePerformanceLogsWebSocketNoHandshakeRespNoCreated() throws IOException {
		List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-with-websockets-no-handshake-resp.txt");
		
		List<TestStep> testSteps = new ArrayList<>();
		testSteps.add(new TestStep("step 1"));
		testSteps.get(0).setTimestamp(Instant.ofEpochMilli(1739547733421L).atZone(ZoneId.systemDefault()).toOffsetDateTime());
		testSteps.add(new TestStep("step 2"));
		testSteps.get(1).setTimestamp(Instant.ofEpochMilli(1739547734198L).atZone(ZoneId.systemDefault()).toOffsetDateTime());
		
		Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);
		
		// check pages
		Assert.assertEquals(har.getLog().getPages().size(), 2);
		Assert.assertEquals(har.getLog().getPages().get(0).getTitle(), "step 1");
		Assert.assertEquals(har.getLog().getPages().get(0).getId(), "page_0");
		Assert.assertTrue(har.getLog().getPages().get(0).getStartedDateTime().startsWith("2025-02-14T")); // to avoid problems on other time zones
		
		// check entries
		Assert.assertEquals(har.getLog().getEntries().size(), 13);
		Assert.assertEquals(har.getLog().getEntries().get(10).getRequest().getUrl(), "wss://not.found");
		Assert.assertEquals(har.getLog().getEntries().get(10).getRequest().getHeaders().size(), 13);
		Assert.assertNotEquals(har.getLog().getEntries().get(10).getResponse(), null);
		Assert.assertEquals(har.getLog().getEntries().get(10).getPageref(), "page_1");
		Assert.assertEquals(har.getLog().getEntries().get(10).getTime(), 3202);
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().size(), 9);
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(1).getData(), "somePayloa [...] esntMatter");
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(5).getData(), "Payloads less than 20 chars are redacted.");
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(0).getType(), "receive");
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(1).getType(), "send");
		Assert.assertTrue(har.getLog().getEntries().get(10).getStartedDateTime().startsWith("2025-03-26T"));
		
		// check transition between pages, based on timings
		Assert.assertEquals(har.getLog().getEntries().get(7).getPageref(), "page_0");
		Assert.assertEquals(har.getLog().getEntries().get(8).getPageref(), "page_1");
	}
	
	@Test(groups = "ut")
	public void testParsePerformanceLogsWebSocketOneFrameOnly() throws IOException {
		List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-with-websockets-one-frame-only.txt");
		
		List<TestStep> testSteps = new ArrayList<>();
		testSteps.add(new TestStep("step 1"));
		testSteps.get(0).setTimestamp(Instant.ofEpochMilli(1739547733421L).atZone(ZoneId.systemDefault()).toOffsetDateTime());
		testSteps.add(new TestStep("step 2"));
		testSteps.get(1).setTimestamp(Instant.ofEpochMilli(1739547734198L).atZone(ZoneId.systemDefault()).toOffsetDateTime());
		
		Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);
		
		// check pages
		Assert.assertEquals(har.getLog().getPages().size(), 2);
		Assert.assertEquals(har.getLog().getPages().get(0).getTitle(), "step 1");
		Assert.assertEquals(har.getLog().getPages().get(0).getId(), "page_0");
		Assert.assertTrue(har.getLog().getPages().get(0).getStartedDateTime().startsWith("2025-02-14T")); // to avoid problems on other time zones
		
		// check entries
		Assert.assertEquals(har.getLog().getEntries().size(), 13);
		Assert.assertNotEquals(har.getLog().getEntries().get(10).getRequest(), null);
		Assert.assertNotEquals(har.getLog().getEntries().get(10).getResponse(), null);
		Assert.assertEquals(har.getLog().getEntries().get(10).getPageref(), "");
		Assert.assertEquals(har.getLog().getEntries().get(10).getTime(), -1);
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().size(), 1);
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(0).getData(), "somePayloa [...] esntMatter");
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(0).getType(), "receive");
		Assert.assertTrue(har.getLog().getEntries().get(10).getStartedDateTime().startsWith("1970-01-01T"));
		
		// check transition between pages, based on timings
		Assert.assertEquals(har.getLog().getEntries().get(7).getPageref(), "page_0");
		Assert.assertEquals(har.getLog().getEntries().get(8).getPageref(), "page_1");
	}

	@Test(groups = "ut")
	public void testParsePerformanceLogsWebSocketWithMissingMessages() throws IOException {
		List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-with-websockets.txt");
		
		List<TestStep> testSteps = new ArrayList<>();
		testSteps.add(new TestStep("step 1"));
		testSteps.get(0).setTimestamp(Instant.ofEpochMilli(1739547733421L).atZone(ZoneId.systemDefault()).toOffsetDateTime());
		testSteps.add(new TestStep("step 2"));
		testSteps.get(1).setTimestamp(Instant.ofEpochMilli(1739547734198L).atZone(ZoneId.systemDefault()).toOffsetDateTime());
		
		Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);
		
		// check pages
		Assert.assertEquals(har.getLog().getPages().size(), 2);
		Assert.assertEquals(har.getLog().getPages().get(0).getTitle(), "step 1");
		Assert.assertEquals(har.getLog().getPages().get(0).getId(), "page_0");
		Assert.assertTrue(har.getLog().getPages().get(0).getStartedDateTime().startsWith("2025-02-14T")); // to avoid problems on other time zones
		
		// check entries
		Assert.assertEquals(har.getLog().getEntries().size(), 13);
		Assert.assertEquals(har.getLog().getEntries().get(10).getRequest().getMethod(), "GET");
		Assert.assertEquals(har.getLog().getEntries().get(10).getRequest().getUrl(), "wss://127.0.0.1:0001/test");
		Assert.assertEquals(har.getLog().getEntries().get(10).getRequest().getHeaders().size(), 13);
		Assert.assertEquals(har.getLog().getEntries().get(10).getResponse().getStatus(), 101);
		Assert.assertEquals(har.getLog().getEntries().get(10).getResponse().getStatusText(), "Switching Protocols");
		Assert.assertEquals(har.getLog().getEntries().get(10).getResponse().getHttpVersion(), "HTTP/1.1");
		Assert.assertEquals(har.getLog().getEntries().get(10).getResponse().getHeaders().size(), 8);
		Assert.assertEquals(har.getLog().getEntries().get(10).getResponse().getHeadersSize(), 0);
		Assert.assertEquals(har.getLog().getEntries().get(10).getResponse().getBodySize(), 0);
		Assert.assertEquals(har.getLog().getEntries().get(10).getPageref(), "page_1");
		Assert.assertEquals(har.getLog().getEntries().get(10).getTime(), 3202);
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().size(), 9);
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(1).getData(), "somePayloa [...] esntMatter");
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(5).getData(), "Payloads less than 20 chars are redacted.");
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(0).getType(), "receive");
		Assert.assertEquals(((WebSocketEntry) har.getLog().getEntries().get(10)).getWebSocketMessages().get(1).getType(), "send");
		Assert.assertTrue(har.getLog().getEntries().get(10).getStartedDateTime().startsWith("2025-03-26T"));
		
		// check transition between pages, based on timings
		Assert.assertEquals(har.getLog().getEntries().get(7).getPageref(), "page_0");
		Assert.assertEquals(har.getLog().getEntries().get(8).getPageref(), "page_1");
	}
	
    @Test(groups="ut")
    public void testParsePerformanceLogsNoError() throws IOException {
        List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-all-ok.txt");

        List<TestStep> testSteps = new ArrayList<>();
        testSteps.add(new TestStep("step 1"));
        testSteps.get(0).setTimestamp(Instant.ofEpochMilli(1739547733421L).atZone(ZoneId.systemDefault()).toOffsetDateTime());
        testSteps.add(new TestStep("step 2"));
        testSteps.get(1).setTimestamp(Instant.ofEpochMilli(1739547734198L).atZone(ZoneId.systemDefault()).toOffsetDateTime());

        Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);

        // check pages
        Assert.assertEquals(har.getLog().getPages().size(), 2);
        Assert.assertEquals(har.getLog().getPages().get(0).getTitle(), "step 1");
        Assert.assertEquals(har.getLog().getPages().get(0).getId(), "page_0");
        Assert.assertTrue(har.getLog().getPages().get(0).getStartedDateTime().startsWith("2025-02-14T")); // to avoid problems on other time zones

        // check entries
        Assert.assertEquals(har.getLog().getEntries().size(), 23);
        Assert.assertEquals(har.getLog().getEntries().get(0).getRequest().getMethod(), "GET");
        Assert.assertEquals(har.getLog().getEntries().get(0).getRequest().getUrl(), "http://127.0.0.1:56825/test.html");
        Assert.assertEquals(har.getLog().getEntries().get(0).getRequest().getHeaders().size(), 14);
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getStatus(), 200);
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getStatusText(), "OK");
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getHttpVersion(), "http/1.1");
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getHeaders().size(), 3);
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getContent().getMimeType(), "text/html");
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getContent().getSize(), 12993);
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getContent().getText(), "_masked_");
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getHeadersSize(), 103);
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getBodySize(), 12993);
        Assert.assertEquals(har.getLog().getEntries().get(0).getPageref(), "page_0");
        Assert.assertEquals(har.getLog().getEntries().get(0).getTimings().getBlocked(), 0.854);
        Assert.assertEquals(har.getLog().getEntries().get(0).getTimings().getDns(), -1.0);
        Assert.assertEquals(har.getLog().getEntries().get(0).getTimings().getWait(), 307.798);
        Assert.assertTrue(har.getLog().getEntries().get(0).getStartedDateTime().startsWith("2025-02-14T"));

        // check transition between pages, based on timings
        Assert.assertEquals(har.getLog().getEntries().get(18).getPageref(), "page_0");
        Assert.assertEquals(har.getLog().getEntries().get(19).getPageref(), "page_1");
    }

    /**
     * Check Authorization header is removed
     */
    @Test(groups="ut")
    public void testParsePerformanceLogsAuthorizationHeaderRemoved() throws IOException {
        List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-auth-headers.txt");

        List<TestStep> testSteps = new ArrayList<>();
        testSteps.add(new TestStep("step 1"));
        testSteps.get(0).setTimestamp(Instant.ofEpochMilli(1739547733421L).atZone(ZoneId.systemDefault()).toOffsetDateTime());

        Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);
        Assert.assertTrue(har.getLog().getEntries().get(0).getRequest().getHeaders()
                .stream()
                .filter(h -> h.getName().equals("Authorization"))
                .findAny()
                .isEmpty());
    }

    /**
     * If no steps are provided, no not raise exceptions, only provide empty pages
     */
    @Test(groups="ut")
    public void testParsePerformanceLogsNoSteps() throws IOException {
        List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-auth-headers.txt");

        List<TestStep> testSteps = new ArrayList<>();

        Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);
        Assert.assertEquals(har.getLog().getEntries().size(), 1);
        Assert.assertEquals(har.getLog().getEntries().get(0).getPageref(), "");
        Assert.assertEquals(har.getLog().getPages().size(), 0);
    }

    /**
     * Check that missing requestWillBeSentExtraInfo does not bother analysis
     */
    @Test(groups="ut")
    public void testParsePerformanceLogsMissingRequestWillBeSentExtraInfo() throws IOException {
        List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-missing-sentExtraInfo.txt");

        List<TestStep> testSteps = new ArrayList<>();
        testSteps.add(new TestStep("step 1"));
        testSteps.get(0).setTimestamp(Instant.ofEpochMilli(1739547733421L).atZone(ZoneId.systemDefault()).toOffsetDateTime());

        Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);
        Assert.assertEquals(har.getLog().getEntries().get(0).getRequest().getHeaders().size(), 4);
    }

    /**
     * Check that missing responseReceivedExtraInfo does not bother analysis
     */
    @Test(groups="ut")
    public void testParsePerformanceLogsMissingResponseReceivedExtraInfo() throws IOException {
        List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-missing-responseReceivedExtraInfo.txt");

        List<TestStep> testSteps = new ArrayList<>();
        testSteps.add(new TestStep("step 1"));
        testSteps.get(0).setTimestamp(Instant.ofEpochMilli(1739547733421L).atZone(ZoneId.systemDefault()).toOffsetDateTime());

        Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);
        Assert.assertEquals(har.getLog().getEntries().size(), 1);
    }

    /**
     * Check that missing responseReceived does not bother analysis (case for CORS)
     */
    @Test(groups="ut")
    public void testParsePerformanceLogsMissingResponseReceived() throws IOException {
        List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-missing-responseReceived.txt");

        List<TestStep> testSteps = new ArrayList<>();
        testSteps.add(new TestStep("step 1"));
        testSteps.get(0).setTimestamp(Instant.ofEpochMilli(1739547733421L).atZone(ZoneId.systemDefault()).toOffsetDateTime());

        Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);
        Assert.assertEquals(har.getLog().getEntries().size(), 1);
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getStatus(), 0);
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getStatusText(), "No response received");
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getContent().getMimeType(), "x-unknown");
        Assert.assertEquals(har.getLog().getEntries().get(0).getTimings().getConnect(), 0);
    }

    /**
     * Check missing timings does not prevent analysis (case with local files)
     */
    @Test(groups="ut")
    public void testParsePerformanceLogsMissingTimings() throws IOException {
        List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-missing-timings.txt");

        List<TestStep> testSteps = new ArrayList<>();
        testSteps.add(new TestStep("step 1"));
        testSteps.get(0).setTimestamp(Instant.ofEpochMilli(1739547733421L).atZone(ZoneId.systemDefault()).toOffsetDateTime());

        Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);
        Assert.assertEquals(har.getLog().getEntries().size(), 1);
        Assert.assertEquals(har.getLog().getEntries().get(0).getTimings().getConnect(), 0);
    }

    /**
     * According to https://chromedevtools.github.io/devtools-protocol/tot/Network/#event-responseReceivedExtraInfo, when cache is used
     * status code for responseReceivedExtraInfo is different from responseReceived
     * Check we get status code from responseReceivedExtraInfo
     */
    @Test(groups="ut")
    public void testParsePerformanceLogsWithCache() throws IOException {
        List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-with-cache.txt");

        List<TestStep> testSteps = new ArrayList<>();
        testSteps.add(new TestStep("step 1"));
        testSteps.get(0).setTimestamp(Instant.ofEpochMilli(1739547733421L).atZone(ZoneId.systemDefault()).toOffsetDateTime());

        Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);
        Assert.assertEquals(har.getLog().getEntries().size(), 1);
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getStatus(), 304);
    }

    /**
     * Check that in case some mandatory data is not present, no exception is thrown but entry is discarded
     */
    @Test(groups="ut")
    public void testParsePerformanceMissingMandatoryData() throws IOException {
        List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-missing-data.txt");

        List<TestStep> testSteps = new ArrayList<>();
        testSteps.add(new TestStep("step 1"));
        testSteps.get(0).setTimestamp(Instant.ofEpochMilli(1739547733421L).atZone(ZoneId.systemDefault()).toOffsetDateTime());

        Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);
        Assert.assertEquals(har.getLog().getEntries().size(), 0);
    }

    @Test(groups="ut")
    public void testParsePerformanceLoadingFailed() throws IOException {
        List<LogEntry> logEntries = readLogs("tu/chromePerformance/driver-log-performance-loading-failed.txt");

        List<TestStep> testSteps = new ArrayList<>();
        testSteps.add(new TestStep("step 1"));
        testSteps.get(0).setTimestamp(Instant.ofEpochMilli(1739547733421L).atZone(ZoneId.systemDefault()).toOffsetDateTime());

        Har har = ChromiumUtils.parsePerformanceLogs(logEntries, testSteps);
        Assert.assertEquals(har.getLog().getEntries().size(), 1);
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getStatus(), 0);
        Assert.assertEquals(har.getLog().getEntries().get(0).getResponse().getStatusText(), "net::ERR_NAME_NOT_RESOLVED");
    }

    private static List<LogEntry> readLogs(String resourcePath) throws IOException {
        File perfFile = createFileFromResource(resourcePath);
        return Files.readAllLines(perfFile.toPath())
                .stream()
                .map(line -> new LogEntry(Level.INFO, 0, line))
                .toList();
    }
}
