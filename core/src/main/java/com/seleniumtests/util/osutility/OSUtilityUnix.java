/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.util.osutility;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.driver.BrowserType;

public class OSUtilityUnix extends OSUtility {

    private static final String WHICH_ERROR = "which:";


    /**
     * Ask console for every running process.
     *
     * @return list of output command lines
     */
    @Override
    public List<ProcessInfo> getRunningProcessList() {
        String command = "ps ax";
        List<String> strProcessList = Arrays.asList(OSCommand.executeCommandAndWait(command).split("\n"));

        List<ProcessInfo> processInfoList = new ArrayList<>();
        for (String sentence : strProcessList) {
            String[] words = sentence.trim().split("\\s+");

            ProcessInfo processInfo = new ProcessInfo();

            int i = 0;

            if (words[i] != null && !words[i].isEmpty()) {

                // PID
                processInfo.setPid(words[i]);
                i++;

                // TTY
                processInfo.setSessionName(words[i]);
                i += 2;

                // TIME
                processInfo.setCpuTime(words[i]);
                i++;

                // CMD
                processInfo.setName(words[i]);
            }
            processInfoList.add(processInfo);
        }
        return processInfoList;
    }

    /**
     * Terminate process from command line terminal.
     *
     * @param process
     * @param force   to kill the process
     * @return
     * @throws IOException
     */
    @Override
    public String killProcess(String pid, boolean force) {

        if (force) {
            return OSCommand.executeCommandAndWait("kill -SIGKILL " + pid);
        } else {
            return OSCommand.executeCommandAndWait("kill -SIGTERM " + pid);
        }
    }

    /**
     * Kill process by name
     */
    @Override
    public String killProcessByName(String programName, boolean force) {
        return OSCommand.executeCommandAndWait("killall -I " + programName);
    }

    @Override
    public String getProgramExtension() {
        return "";
    }

    @Override
    public int getIEVersion() {
        return 0;
    }


    @Override
    public String getOSBuild() {
        return OSCommand.executeCommandAndWait("uname -a");
    }

    @Override
    public Map<BrowserType, List<BrowserInfo>> discoverInstalledBrowsersWithVersion(boolean discoverBetaBrowsers) {
        Map<BrowserType, List<BrowserInfo>> browserList = new EnumMap<>(BrowserType.class);

        browserList.put(BrowserType.HTMLUNIT, Arrays.asList(new BrowserInfo(BrowserType.HTMLUNIT, BrowserInfo.LATEST_VERSION, null)));
        browserList.put(BrowserType.PHANTOMJS, Arrays.asList(new BrowserInfo(BrowserType.PHANTOMJS, BrowserInfo.LATEST_VERSION, null)));


        // TODO: handle multiple installation of firefox and Chrome
        String firefoxLocation = OSCommand.executeCommandAndWait("which firefox").trim();
        String iceweaselLocation = OSCommand.executeCommandAndWait("which iceweasel").trim();
        String chromeLocation = OSCommand.executeCommandAndWait("which google-chrome").trim();
        String chromiumLocation = OSCommand.executeCommandAndWait("which chromium-browser").trim();

        if (!firefoxLocation.isEmpty() && !firefoxLocation.contains(WHICH_ERROR)) {
            String version = getFirefoxVersion("firefox");
            List<BrowserInfo> arrayFirefox = new ArrayList<>();
            arrayFirefox.add(new BrowserInfo(BrowserType.FIREFOX, extractFirefoxVersion(version), firefoxLocation));
            browserList.put(BrowserType.FIREFOX, arrayFirefox);

        } else if (!iceweaselLocation.isEmpty() && !iceweaselLocation.contains(WHICH_ERROR)) {
            String version = getFirefoxVersion("iceweasel");
            List<BrowserInfo> arrayIceWeasel = new ArrayList<>();
            arrayIceWeasel.add(new BrowserInfo(BrowserType.FIREFOX, extractFirefoxVersion(version), iceweaselLocation));
            browserList.put(BrowserType.FIREFOX, arrayIceWeasel);
        }
        if (!chromiumLocation.isEmpty() && !chromiumLocation.contains(WHICH_ERROR)) {
            String version = getChromeVersion("chromium-browser");
            List<BrowserInfo> arrayChromium = new ArrayList<>();
            arrayChromium.add(new BrowserInfo(BrowserType.CHROME, extractChromiumVersion(version), chromiumLocation));
            browserList.put(BrowserType.CHROME, arrayChromium);

        } else if (!chromeLocation.isEmpty() && !chromeLocation.contains(WHICH_ERROR)) {
            String version = getChromeVersion("google-chrome");
            List<BrowserInfo> arrayChrome = new ArrayList<>();
            arrayChrome.add(new BrowserInfo(BrowserType.CHROME, extractChromeVersion(version), chromeLocation));
            browserList.put(BrowserType.CHROME, arrayChrome);
        }

        return browserList;
    }

    @Override
    public List<Long> getChildProcessPid(Long parentProcess, String processName, List<Long> existingPids) throws IOException {

        List<Long> searchedPids = new ArrayList<>();

        String pids = OSCommand.executeCommandAndWait(String.format("pgrep -P %d -d , -l", parentProcess)).trim();
        for (String process : pids.split(",")) {
            String[] processSplit = process.split(" ");
            Long pid;
            try {
                pid = Long.parseLong(processSplit[0]);
            } catch (NumberFormatException e) {
                continue;
            }

            // pgrep limits process name to 15 chars so do not compare with entire name
            if ((processName == null || processName.startsWith(processSplit[1])) && !existingPids.contains(pid)) {
                searchedPids.add(pid);
            }
        }


        return searchedPids;
    }

    @Override
    public String getProgramNameFromPid(Long pid) {
        return OSCommand.executeCommandAndWait(String.format("ps -p %d -o comm=", pid));
    }


    @Override
    public Integer getProcessIdByListeningPort(int port) {
        // example: TCP    127.0.0.1:51239        0.0.0.0:0              LISTENING       22492
        String lines = OSCommand.executeCommandAndWait("netstat -anp").trim();
        Pattern pattern = Pattern.compile(String.format(".*\\:%d\\s+.*\\:.*LISTEN\\s+(\\d+).*", port));
        for (String line : lines.split("\n")) {
            Matcher matcher = pattern.matcher(line.trim());

            if (matcher.matches()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return null;
    }

    @Override
    public Charset getConsoleCharset() {
        try {
            return Charset.forName(OSCommand.executeCommandAndWait("locale charmap", -1, StandardCharsets.UTF_8).trim());
        } catch (Exception e) {
            return Charset.defaultCharset();
        }
    }

}
