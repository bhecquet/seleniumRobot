package com.seleniumtests.connectors.extools;

/**
 * Interface to FFMpeg
 */
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.SystemUtility;

import java.io.IOException;
import java.util.List;

public class FFMpeg {

    private String ffmpegPath;
    private boolean openh264 = false;
    private boolean x264 = false;

    /**
     * Check if ffmpeg is present
     */
    public FFMpeg() {
        checkInstallation();
    }

    private void checkInstallation() {
        String envPath = SystemUtility.getenv("FFMPEG_PATH");
        this.ffmpegPath = (envPath != null && !envPath.isEmpty()) ? envPath : "ffmpeg";

        String out = OSCommand.executeCommandAndWait(new String[]{ffmpegPath, "-version"});
        if (!out.contains("libavutil")) {
            throw new ConfigurationException("FFmpeg is not installed at : " + ffmpegPath);
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
}

