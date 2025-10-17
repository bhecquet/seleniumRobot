/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.connectors.extools;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.SystemUtility;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class FFMpeg {

    public static class Chapter {
        String name;
        Long endTimestamp;
        Long startTimestamp;

        public Chapter(String name, Long startTimestamp, Long endTimestamp) {
            this.name = name;
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
        }
    }

    private String ffmpegPath;
    private boolean openh264 = false;
    private boolean x264 = false;
    private static final Logger logger = SeleniumRobotLogger.getLogger(FFMpeg.class);

    /**
     * Check if ffmpeg is present
     */
    public FFMpeg() {
        checkInstallation();
    }

    private void checkInstallation() {
        String envPath = SystemUtility.getenv("FFMPEG_PATH");
        this.ffmpegPath = (envPath != null && !envPath.isEmpty()) ? envPath : "ffmpeg";

        String out = "";
        try {
            out = OSCommand.executeCommandAndWait(new String[]{ffmpegPath, "-version"});

            if (!out.contains("libavutil")) {
                logger.error(out);
                throw new ConfigurationException("FFmpeg is not installed at : " + ffmpegPath);
            }
        } catch (CustomSeleniumTestsException e) {
            throw new ConfigurationException("FFMPEG is not installed at : " + ffmpegPath, e);
        }
        if (out.contains(" --enable-libopenh264")) {
            this.openh264 = true;
        } else if (out.contains(" --enable-libx264")) {
            this.x264 = true;
        }
    }

    public String runFFmpegCommand(List<String> options) {

        List<String> command = new java.util.ArrayList<>();
        command.add(ffmpegPath);
        for (String option : options) {
            if ("libopenh264".equals(option) && x264) {
                command.add("libx264");
            } else if ("libx264".equals(option) && openh264) {
                command.add("libopenh264");
            } else {
                command.add(option);
            }
        }

        return OSCommand.executeCommandAndWait(command.toArray(new String[0]));
    }

    public boolean isOpenh264() {
        return openh264;
    }

    public boolean isX264() {
        return x264;
    }

    /**
     * Add chapters to video
     *
     * @param videoFile     The video to add chapters to
     * @param chapters     list of steps that help creating chapters
     */
    public void addChapters(File videoFile, List<Chapter> chapters) {

        if (!videoFile.getName().endsWith(".mp4")) {
            logger.info("Only mp4 files can have chapters");
            return;
        }

        try {
            Path metadataFile = Files.createTempFile("metadata", ".txt");
            Files.deleteIfExists(metadataFile);
            Path newVideoFile = Files.createTempFile("newVideo", ".mp4");
            Files.deleteIfExists(newVideoFile);

            StringBuilder content = new StringBuilder();
            for (Chapter chapter : chapters) {
                content.append("\n[CHAPTER]\n")
                        .append("TIMEBASE=1/1000\n")
                        .append(String.format("START=%d", chapter.startTimestamp))
                        .append("\n")
                        .append(String.format("END=%d", chapter.endTimestamp))
                        .append("\n")
                        .append(String.format("title=%s", chapter.name))
                        .append("\n\n");
            }

            runFFmpegCommand(List.of("-i", videoFile.getAbsolutePath(), "-f", "ffmetadata", metadataFile.toAbsolutePath().toString()));

            FileUtils.writeStringToFile(metadataFile.toFile(), content.toString(), StandardCharsets.UTF_8, true);

            runFFmpegCommand(List.of("-i", videoFile.getAbsolutePath(), "-i", metadataFile.toAbsolutePath().toString(), "-map_metadata", "1", "-codec", "copy", newVideoFile.toAbsolutePath().toString()));
            FileUtils.copyFile(newVideoFile.toFile(), videoFile, StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(newVideoFile);
            Files.deleteIfExists(metadataFile);
        } catch (IOException e) {
            logger.warn("Could not create metadatafile: {}", e.getMessage());
        }
    }
}

