	package com.seleniumtests.browserfactory.chrome;

import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.har.*;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import io.cucumber.cienvironment.internal.com.eclipsesource.json.Json;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
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
            Instant instant = testStep.getStartDate().toInstant();
            String pageId = String.format("page_" + id++);
            Page page = new Page(instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), pageId, testStep.getName());
            pageStart.put(testStep.getStartDate().toInstant().toEpochMilli(), page);
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
                        requests.putIfAbsent(requestId, new HashMap<>());
                        requests.get(requestId).put("requestWillBeSent", messageObject);
                    }
                    case "Network.requestWillBeSentExtraInfo" -> {
                        String requestId = messageObject.getJSONObject("params").getString("requestId");
                        requests.putIfAbsent(requestId, new HashMap<>());
                        requests.get(requestId).put("requestWillBeSentExtraInfo", messageObject);
                    }
                    // message format: {"message":{"method":"Network.responseReceived","params":{"frameId":"8C5E01A9EE0BD7556C532FCFBE04EE5D","hasExtraInfo":true,"loaderId":"3149B492109FC8E15361AE661A188A05","requestId":"3149B492109FC8E15361AE661A188A05","response":{"alternateProtocolUsage":"unspecifiedReason","charset":"","connectionId":109,"connectionReused":true,"encodedDataLength":101,"fromDiskCache":false,"fromPrefetchCache":false,"fromServiceWorker":false,"headers":{"Content-Length":"124","Date":"Tue, 11 Feb 2025 09:02:20 GMT","Server":"Jetty(11.0.24)"},"mimeType":"text/html","protocol":"http/1.1","remoteIPAddress":"10.200.38.44","remotePort":51230,"responseTime":1.739264540255014e+12,"securityState":"insecure","status":200,"statusText":"OK","timing":{"connectEnd":-1,"connectStart":-1,"dnsEnd":-1,"dnsStart":-1,"proxyEnd":4.019,"proxyStart":2.804,"pushEnd":0,"pushStart":0,"receiveHeadersEnd":11.977,"receiveHeadersStart":5.365,"requestTime":91274.541644,"sendEnd":4.238,"sendStart":4.129,"sslEnd":-1,"sslStart":-1,"workerFetchStart":-1,"workerReady":-1,"workerRespondWithSettled":-1,"workerStart":-1},"url":"http://10.200.38.44:51230/testIFrame3.html"},"timestamp":91274.558756,"type":"Document"}},"webview":"0327E68CEF262C8D77818DC5C8B14339"}
                    case "Network.responseReceived" -> {
                        String requestId = messageObject.getJSONObject("params").getString("requestId");
                        requests.putIfAbsent(requestId, new HashMap<>());
                        requests.get(requestId).put("responseReceived", messageObject);
                    }
                    // {"message":{"method":"Network.loadingFinished","params":{"encodedDataLength":924,"requestId":"28364.2","timestamp":192318.813115}},"webview":"D6FD686EEEFF1CF429E60E5D8F6A8D71"}
                    case "Network.loadingFinished" -> {
                        String requestId = messageObject.getJSONObject("params").getString("requestId");
                        requests.putIfAbsent(requestId, new HashMap<>());
                        requests.get(requestId).put("loadingFinished", messageObject);
                    }
                    case "Network.loadingFailed" -> {
                        String requestId = messageObject.getJSONObject("params").getString("requestId");
                        requests.putIfAbsent(requestId, new HashMap<>());
                        requests.get(requestId).put("loadingFailed", messageObject);
                    }
                    // {"method":"Network.responseReceivedExtraInfo","params":{"blockedCookies":[],"cookiePartitionKey":{"hasCrossSiteAncestor":false,"topLevelSite":"http://127.0.0.1"},"cookiePartitionKeyOpaque":false,"exemptedCookies":[],"headers":{"Content-Length":"2538","Date":"Fri, 14 Feb 2025 15:42:13 GMT","Server":"Jetty(11.0.24)"},"headersText":"HTTP/1.1 200 OK\r\nDate: Fri, 14 Feb 2025 15:42:13 GMT\r\nContent-Length: 2538\r\nServer: Jetty(11.0.24)\r\n\r\n","requestId":"28364.5","resourceIPAddressSpace":"Local","statusCode":200}},"webview":"D6FD686EEEFF1CF429E60E5D8F6A8D71"}
                    // use to get the real status code (in case of cache loading)
                    case "Network.responseReceivedExtraInfo" -> {
                        String requestId = messageObject.getJSONObject("params").getString("requestId");
                        requests.putIfAbsent(requestId, new HashMap<>());
                        requests.get(requestId).put("responseReceivedExtraInfo", messageObject);
                    }
                    //{"message":{"method":"Network.webSocketCreated","params":{"initiator":{"stack":{"callFrames":[{"columnNumber":26053,"functionName":"start","lineNumber":8,"scriptId":"19","url":"https://127.0.0.1:0001/file/script/jquery.signalR.js"},{"columnNumber":20898,"functionName":"f","lineNumber":0,"scriptId":"10","url":"https://127.0.0.1/letmein/somefile.js"}]},"type":"script"},"requestId":"18060.25","url":"wss://127.0.0.1:0001/test"}},"webview":"0A33F09BEE850DE612D04D06EE1884AC"}
					case "Network.webSocketCreated" -> {
						String requestId = messageObject.getJSONObject("params").getString("requestId");
						requests.putIfAbsent(requestId, new HashMap<>());
						requests.get(requestId).put("webSocketCreated", messageObject);
					}
					//{"message":{"method":"Network.webSocketClosed","params":{"requestId":"18060.25","timestamp":7892.631933}},"webview":"0A33F09BEE850DE612D04D06EE1884AC"}
					case "Network.webSocketClosed" -> {
						String requestId = messageObject.getJSONObject("params").getString("requestId");
						requests.putIfAbsent(requestId, new HashMap<>());
						requests.get(requestId).put("webSocketClosed", messageObject);
					}
					//{"message":{"method":"Network.webSocketWillSendHandshakeRequest","params":{"request":{"headers":{someHeaders}},"requestId":"18060.25","timestamp":7889.427888,"wallTime":1742975479.222366}},"webview":"0A33F09BEE850DE612D04D06EE1884AC"}
					case "Network.webSocketWillSendHandshakeRequest" -> {
						String requestId = messageObject.getJSONObject("params").getString("requestId");
						requests.putIfAbsent(requestId, new HashMap<>());
						requests.get(requestId).put("webSocketWillSendHandshakeRequest", messageObject);
					}
					//{"message":{"method":"Network.webSocketHandshakeResponseReceived","params":{"requestId":"18060.25","response":{"headers":{"someHeaders"},"headersText":"someHeadersText","requestHeaders":{[...],"status":101,"statusText":"Switching Protocols"},"timestamp":7889.430853}},"webview":"0A33F09BEE850DE612D04D06EE1884AC"}
					case "Network.webSocketHandshakeResponseReceived" -> {
						String requestId = messageObject.getJSONObject("params").getString("requestId");
						requests.putIfAbsent(requestId, new HashMap<>());
						requests.get(requestId).put("webSocketHandshakeResponseReceived", messageObject);
					}
					//{"message":{"method":"Network.webSocketFrameSent","params":{"requestId":"18060.25","response":{"mask":true,"opcode":1,"payloadData":"somePayloadDataItDoesntMatter"},"timestamp":7889.443974}},"webview":"0A33F09BEE850DE612D04D06EE1884AC"}
					case "Network.webSocketFrameSent" -> {
						String requestId = messageObject.getJSONObject("params").getString("requestId");
						requests.putIfAbsent(requestId, new HashMap<>());
						//I'm adding the size as a way to unify the key in order to avoid overriding previous frames
						requests.get(requestId).put("webSocketFrameSent" + requests.get(requestId).size(), messageObject);
					}
					//{"message":{"method":"Network.webSocketFrameReceived","params":{"requestId":"18060.25","response":{"mask":false,"opcode":1,"payloadData":"somePayloadDataItDoesntMatter"},"timestamp":7889.552894}},"webview":"0A33F09BEE850DE612D04D06EE1884AC"}
					case "Network.webSocketFrameReceived" -> {
						String requestId = messageObject.getJSONObject("params").getString("requestId");
						requests.putIfAbsent(requestId, new HashMap<>());
						//I'm adding the size as a way to unify the key in order to avoid overriding previous frames
						requests.get(requestId).put("webSocketFrameReceived" + requests.get(requestId).size(), messageObject);
					}
                }

            } catch (Exception e) {
                logger.error("Error reading event " + line.getMessage());
            }
        }

        for (Map.Entry<String, HashMap<String, Object>> requestsEntry: requests.entrySet()) {
            String requestId = requestsEntry.getKey();
            try {

                JSONObject jsonRequest = (JSONObject) requestsEntry.getValue().get("requestWillBeSent");
                // without request information, do nothing
                if (jsonRequest == null) {
                	//Check if requestsEntry is of webSocket type
                	Set<String> isThereAWebSocketHere = requestsEntry.getValue().keySet().stream().filter(s -> s.contains("webSocket")).collect(Collectors.toSet());
					if (!isThereAWebSocketHere.isEmpty()) {
						ArrayList<Object> webSocketEntry = getWebSocketEntry(requestsEntry, pageStart);
						if (!webSocketEntry.isEmpty()) {
							log.addEntry((WebSocketEntry) webSocketEntry.get(0));
							usedPages.replace((Page) webSocketEntry.get(1), true);
						}
					}
					continue;
                }
                Request request = buildRequest(requestsEntry, jsonRequest);

                JSONObject jsonResponse = (JSONObject) requestsEntry.getValue().get("responseReceived");
                Response response = buildResponse(requestsEntry, jsonResponse);

                Timing timings;
                double duration = -1;

                if (jsonResponse != null) {
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

                    duration = (endLoadingTimestamp - jsonRequest.getJSONObject("params").getDouble("timestamp")) * 1000;
                } else {
                    timings = new Timing(
                            0, 0, 0, 0, 0, 0, 0
                    );
                }

                long entryDate = (long) (jsonRequest.getJSONObject("params").getDouble("wallTime") * 1000);

                Page pageRef = getPageRef(pageStart, entryDate);
                usedPages.replace(pageRef, true);

                Entry entry = new Entry(
                        pageRef == null ? "" : pageRef.getId(),
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

        return har;
    }
    
    private static Page getPageRef(Map<Long, Page> pageStart, long entryDate) {
		Page pageRef = null;
		for (Map.Entry<Long, Page> pageEntry : pageStart.entrySet()) {
			if (entryDate > pageEntry.getKey() || pageRef == null) {
				pageRef = pageEntry.getValue();
			}
		}
		return pageRef;
	}
	
    private static ArrayList<Object> getWebSocketEntry(Map.Entry<String, HashMap<String, Object>> requestsEntry, Map<Long, Page> pageStart) {
		try {
			JSONObject createdWebSocket = (JSONObject) requestsEntry.getValue().get("webSocketCreated");
			JSONObject sendHandshakeReq = (JSONObject) requestsEntry.getValue().get("webSocketWillSendHandshakeRequest");
			JSONObject receiveHandshakeResp = (JSONObject) requestsEntry.getValue().get("webSocketHandshakeResponseReceived");
			JSONObject closedWebSocket = (JSONObject) requestsEntry.getValue().get("webSocketClosed");
			
			//Init the datas
			String url = "wss://not.found";
			Page pageRef = null;
			long entryDate = 0;
			Request req = new Request(
					0,
					"",
					"",
					"",
					new ArrayList<>(),
					new ArrayList<>(),
					new ArrayList<>(),
					0
			);
			Response res = new Response(
					0,
					"",
					"",
					new ArrayList<>(),
					new ArrayList<>(),
					new Content("x-unknown", 0, ""),
					"",
					0,
					0
			);
			int duration = -1;
			
			//Setting every data possible following the available requests entries
			if (createdWebSocket != null) {
				url = createdWebSocket.getJSONObject("params").getString("url");
				req = buildWebSocketRequest(createdWebSocket, url);
			}
			
			if (receiveHandshakeResp != null) {
				//Set the request
				req = buildWebSocketRequest(receiveHandshakeResp, url);
				
				//Set the response
				JSONObject jsonResponseHeaders = receiveHandshakeResp.getJSONObject("params").getJSONObject("response").getJSONObject("headers");
				res = new Response(
						receiveHandshakeResp.getJSONObject("params").getJSONObject("response").getInt("status"),
						receiveHandshakeResp.getJSONObject("params").getJSONObject("response").getString("statusText"),
						receiveHandshakeResp.getJSONObject("params").getJSONObject("response").getString("headersText").split(" ")[0],
						jsonResponseHeaders.keySet().stream()
								.filter(key -> !key.toLowerCase().contains("token"))
								.map(key -> new Header(key, jsonResponseHeaders.getString(key)))
								.collect(Collectors.toList()),
						new ArrayList<>(),
						new Content("x-unknown", 0, ""),
						"",
						jsonResponseHeaders.optInt("Content-Length", 0),
						0);
			}
			
			if (sendHandshakeReq != null) {
				entryDate = (long) (sendHandshakeReq.getJSONObject("params").getDouble("wallTime") * 1000);
				pageRef = getPageRef(pageStart, entryDate);
				//Set the request in case there's no response message
				if (req == null) {
					req = buildWebSocketRequest(sendHandshakeReq, url);
				}
				if (closedWebSocket != null) {
					duration = (int) (closedWebSocket.getJSONObject("params").getDouble("timestamp") * 1000 - sendHandshakeReq.getJSONObject("params").getDouble("timestamp") * 1000);
				}
			}
			
			//Get the messages, ordered by their timestamps
			List<WebSocketMessage> webSocketMessages = buildWebSocketMessages(requestsEntry);
			
			WebSocketEntry webSocketEntry = new WebSocketEntry(
					pageRef == null ? "" : pageRef.getId(),
					Instant.ofEpochMilli(entryDate).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
					req,
					res,
					new Timing(0, 0, 0, 0, 0, 0, 0),
					duration,
					webSocketMessages);
			
			//Return the WebSocketEntry object and the Page in order to maintain the logic in the parent parsing function
			ArrayList<Object> responseArray = new ArrayList<>();
			responseArray.add(webSocketEntry);
			responseArray.add(pageRef);
			
			return responseArray;
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}
	
	private static Request buildWebSocketRequest(JSONObject jsonEntry, String originalURL) {
		String url = originalURL;
		List<Header> jsonHeaders;
		int jsonHeadersSize;
		
		//First, determine if it's a Response entry or a Request entry
		String entryMethod = jsonEntry.getString("method");
		
		if (entryMethod.contains("Response")) {
			JSONObject jsonRespHeaders = jsonEntry.getJSONObject("params").getJSONObject("response").getJSONObject("requestHeaders");
			jsonHeaders = jsonRespHeaders.keySet().stream()
					.filter(key -> !key.toLowerCase().contains("token"))
					.map(key -> new Header(key, jsonRespHeaders.getString(key)))
					.collect(Collectors.toList());
			jsonHeadersSize = jsonRespHeaders.optInt("Content-Length", 0);
			//in case the url hasn't been found in the webSocketCreated message
			url = jsonEntry.getJSONObject("params").getJSONObject("response").getString("requestHeadersText").split(" ")[1];
		} else if (entryMethod.contains("Request")) {
			JSONObject jsonReqHeaders = jsonEntry.getJSONObject("params").getJSONObject("request").getJSONObject("headers");
			jsonHeaders = jsonReqHeaders.keySet().stream()
					.filter(key -> !key.toLowerCase().contains("token"))
					.map(key -> new Header(key, jsonReqHeaders.getString(key)))
					.collect(Collectors.toList());
			jsonHeadersSize = jsonReqHeaders.optInt("Content-Length", 0);
		} else if (entryMethod.contains("Created")) {
			//We accept webSocketCreated message because even if there's no headers 
			//We at least can return the url this WS tried to reach which can help in debug
			jsonHeaders = new ArrayList<>();
			jsonHeadersSize = 0;
		} else {
			//if it's another entry type just return null instead of an mostly empty object
			return null;
		}

		return new Request(0,
				"GET",
				url,
				"HTTP N/A",
				jsonHeaders,
				new ArrayList<>(),
				new ArrayList<>(),
				jsonHeadersSize);
	}
	
	private static List<WebSocketMessage> buildWebSocketMessages(Map.Entry<String, HashMap<String, Object>> requestsEntry) {
		List<WebSocketMessage> webSocketMessages = new ArrayList<>();
		for (Map.Entry<String, Object> wsRequest : requestsEntry.getValue().entrySet()) {
			JSONObject jswsRequest = (JSONObject) wsRequest.getValue();
			if (jswsRequest.get("method").toString().contains("FrameSent")) {
				String payloadData = jswsRequest.getJSONObject("params").getJSONObject("response").getString("payloadData");
				//Substring the payload to give info on what data has been transferred without giving away sensitive data
				if (payloadData.length() > 20) {
					payloadData = payloadData.substring(0, 10) + " [...] " + payloadData.substring(payloadData.length() - 10);
				} else {
					payloadData = "Payloads less than 20 chars are redacted.";
				}
				webSocketMessages.add(new WebSocketMessage(
						"send",
						jswsRequest.getJSONObject("params").getDouble("timestamp"),
						jswsRequest.getJSONObject("params").getJSONObject("response").getInt("opcode"),
						payloadData));
			} else if (jswsRequest.get("method").toString().contains("FrameReceived")) {
				String payloadData = jswsRequest.getJSONObject("params").getJSONObject("response").getString("payloadData");
				//Substring the payload to give info on what data has been transferred without giving away sensitive data
				if (payloadData.length() > 20) {
					payloadData = payloadData.substring(0, 10) + " [...] " + payloadData.substring(payloadData.length() - 10);
				} else {
					payloadData = "Payloads less than 20 chars are redacted.";
				}
				webSocketMessages.add(new WebSocketMessage(
						"receive",
						jswsRequest.getJSONObject("params").getDouble("timestamp"),
						jswsRequest.getJSONObject("params").getJSONObject("response").getInt("opcode"),
						payloadData));
			}
		}
		webSocketMessages.sort((p1, p2) -> (int) (p1.getTime() * 1000 - p2.getTime() * 1000));
		return webSocketMessages;
	}
	
    private static Response buildResponse(Map.Entry<String, HashMap<String, Object>> requestsEntry, JSONObject jsonResponse) {
        Response response;
        // according to spec, responseReceived event may not be fired in case of CORS
        if (jsonResponse != null) {
            int statusCode = jsonResponse.getJSONObject("params").getJSONObject("response").getInt("status");

            if (requestsEntry.getValue().get("responseReceivedExtraInfo") != null) {
                JSONObject jsonResponseExtraInfo = (JSONObject) requestsEntry.getValue().get("responseReceivedExtraInfo");
                statusCode = jsonResponseExtraInfo.getJSONObject("params").getInt("statusCode");
            }

            JSONObject jsonResponseHeaders = jsonResponse.getJSONObject("params").getJSONObject("response").getJSONObject("headers");
            response = new Response(
                    statusCode,
                    jsonResponse.getJSONObject("params").getJSONObject("response").getString("statusText"),
                    jsonResponse.getJSONObject("params").getJSONObject("response").getString("protocol"),
                    jsonResponseHeaders.keySet().stream()
                            .filter(key -> !key.toLowerCase().contains("token"))
                            .map(key -> new Header(key, jsonResponseHeaders.getString(key)))
                            .collect(Collectors.toList()),
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
        } else {
            String error = "No response received";
            if (requestsEntry.getValue().get("loadingFailed") != null) {
                error = ((JSONObject)requestsEntry.getValue().get("loadingFailed")).getJSONObject("params").getString("errorText");
            }
            response = new Response(
                    0,
                    error,
                    "",
                    new ArrayList<>(),
                    new ArrayList<>(), // cookies
                    new Content("x-unknown", 0, ""),
                    "",
                    -1,
                    -1
            );
        }
        return response;
    }

    private static Request buildRequest(Map.Entry<String, HashMap<String, Object>> requestsEntry, JSONObject jsonRequest) {
        JSONObject jsonRequestHeaders = jsonRequest.getJSONObject("params").getJSONObject("request").getJSONObject("headers");

        if (requestsEntry.getValue().get("requestWillBeSentExtraInfo") != null) {
            jsonRequestHeaders = ((JSONObject) requestsEntry.getValue().get("requestWillBeSentExtraInfo")).getJSONObject("params").getJSONObject("headers");
        }

        JSONObject finalJsonRequestHeaders = jsonRequestHeaders;
        Request request = new Request(0,
                jsonRequest.getJSONObject("params").getJSONObject("request").getString("method"),
                jsonRequest.getJSONObject("params").getJSONObject("request").getString("url"),
                "HTTP N/A",
                jsonRequestHeaders.keySet().stream()
                        .filter(key -> !"Authorization".equals(key))
                        .filter(key -> !key.toLowerCase().contains("token"))
                        .map(key -> new Header(key, finalJsonRequestHeaders.getString(key)))
                        .collect(Collectors.toList()),
                new ArrayList<>(), // cookies
                new ArrayList<>(),
                0
        );
        return request;
    }
}
