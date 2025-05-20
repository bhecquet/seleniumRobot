package com.seleniumtests.ut.connectors.extools;

import static org.mockito.Mockito.*;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.extools.FFMpeg;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.SystemUtility;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class TestFFMpeg extends MockitoTest {


    @Test(groups="ut")
    public void testFFMpegx264() {
        try (MockedStatic mockedOsCommand = Mockito.mockStatic(OSCommand.class)) {
            mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"ffmpeg", "-version"})).thenReturn("ffmpeg version 7.1-full_build-www.gyan.dev Copyright (c) 2000-2024 the FFmpeg developers\n" +
                    "  built with gcc 14.2.0 (Rev1, Built by MSYS2 project)\n" +
                    "  configuration: --enable-gpl --enable-version3 --enable-static --disable-w32threads --disable-autodetect --enable-fontconfig --enable-iconv --enable-gnutls --enable-libxml2 --enable-gmp --enable-bzlib --enable-lzma --enable-libsnappy --enable-zlib --enable-librist --enable-libsrt --enable-libssh --enable-libzmq --enable-avisynth --enable-libbluray --enable-libcaca --enable-sdl2 --enable-libaribb24 --enable-libaribcaption --enable-libdav1d --enable-libdavs2 --enable-libopenjpeg --enable-libquirc --enable-libuavs3d --enable-libxevd --enable-libzvbi --enable-libqrencode --enable-librav1e --enable-libsvtav1 --enable-libvvenc --enable-libwebp --enable-libx264 --enable-libx265 --enable-libxavs2 --enable-libxeve --enable-libxvid --enable-libaom --enable-libjxl --enable-libvpx --enable-mediafoundation --enable-libass --enable-frei0r --enable-libfreetype --enable-libfribidi --enable-libharfbuzz --enable-liblensfun --enable-libvidstab --enable-libvmaf --enable-libzimg --enable-amf --enable-cuda-llvm --enable-cuvid --enable-dxva2 --enable-d3d11va --enable-d3d12va --enable-ffnvcodec --enable-libvpl --enable-nvdec --enable-nvenc --enable-vaapi --enable-libshaderc --enable-vulkan --enable-libplacebo --enable-opencl --enable-libcdio --enable-libgme --enable-libmodplug --enable-libopenmpt --enable-libopencore-amrwb --enable-libmp3lame --enable-libshine --enable-libtheora --enable-libtwolame --enable-libvo-amrwbenc --enable-libcodec2 --enable-libilbc --enable-libgsm --enable-liblc3 --enable-libopencore-amrnb --enable-libopus --enable-libspeex --enable-libvorbis --enable-ladspa --enable-libbs2b --enable-libflite --enable-libmysofa --enable-librubberband --enable-libsoxr --enable-chromaprint\n" +
                    "  libavutil      59. 39.100 / 59. 39.100\n" +
                    "  libavcodec     61. 19.100 / 61. 19.100\n" +
                    "  libavformat    61.  7.100 / 61.  7.100\n" +
                    "  libavdevice    61.  3.100 / 61.  3.100\n" +
                    "  libavfilter    10.  4.100 / 10.  4.100\n" +
                    "  libswscale      8.  3.100 /  8.  3.100\n" +
                    "  libswresample   5.  3.100 /  5.  3.100\n" +
                    "  libpostproc    58.  3.100 / 58.  3.100\n" +
                    "Universal media converter");

            FFMpeg ffmpeg = new FFMpeg();
            Assert.assertTrue(ffmpeg.isX264());
            Assert.assertFalse(ffmpeg.isOpenh264());
        }
    }

    @Test(groups="ut")
    public void testFFMpegopenH264() {
        try (MockedStatic mockedOsCommand = Mockito.mockStatic(OSCommand.class)) {
            mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"ffmpeg", "-version"})).thenReturn("ffmpeg version 7.1-full_build-www.gyan.dev Copyright (c) 2000-2024 the FFmpeg developers\n" +
                    "  built with gcc 14.2.0 (Rev1, Built by MSYS2 project)\n" +
                    "  configuration: --enable-gpl --enable-version3 --enable-static --disable-w32threads --disable-autodetect --enable-fontconfig --enable-iconv --enable-gnutls --enable-libxml2 --enable-gmp --enable-bzlib --enable-lzma --enable-libsnappy --enable-zlib --enable-librist --enable-libsrt --enable-libssh --enable-libzmq --enable-avisynth --enable-libbluray --enable-libcaca --enable-sdl2 --enable-libaribb24 --enable-libaribcaption --enable-libdav1d --enable-libdavs2 --enable-libopenjpeg --enable-libquirc --enable-libuavs3d --enable-libxevd --enable-libzvbi --enable-libqrencode --enable-librav1e --enable-libsvtav1 --enable-libvvenc --enable-libwebp --enable-libopenh264 --enable-libx265 --enable-libxavs2 --enable-libxeve --enable-libxvid --enable-libaom --enable-libjxl --enable-libvpx --enable-mediafoundation --enable-libass --enable-frei0r --enable-libfreetype --enable-libfribidi --enable-libharfbuzz --enable-liblensfun --enable-libvidstab --enable-libvmaf --enable-libzimg --enable-amf --enable-cuda-llvm --enable-cuvid --enable-dxva2 --enable-d3d11va --enable-d3d12va --enable-ffnvcodec --enable-libvpl --enable-nvdec --enable-nvenc --enable-vaapi --enable-libshaderc --enable-vulkan --enable-libplacebo --enable-opencl --enable-libcdio --enable-libgme --enable-libmodplug --enable-libopenmpt --enable-libopencore-amrwb --enable-libmp3lame --enable-libshine --enable-libtheora --enable-libtwolame --enable-libvo-amrwbenc --enable-libcodec2 --enable-libilbc --enable-libgsm --enable-liblc3 --enable-libopencore-amrnb --enable-libopus --enable-libspeex --enable-libvorbis --enable-ladspa --enable-libbs2b --enable-libflite --enable-libmysofa --enable-librubberband --enable-libsoxr --enable-chromaprint\n" +
                    "  libavutil      59. 39.100 / 59. 39.100\n" +
                    "  libavcodec     61. 19.100 / 61. 19.100\n" +
                    "  libavformat    61.  7.100 / 61.  7.100\n" +
                    "  libavdevice    61.  3.100 / 61.  3.100\n" +
                    "  libavfilter    10.  4.100 / 10.  4.100\n" +
                    "  libswscale      8.  3.100 /  8.  3.100\n" +
                    "  libswresample   5.  3.100 /  5.  3.100\n" +
                    "  libpostproc    58.  3.100 / 58.  3.100\n" +
                    "Universal media converter");

            FFMpeg ffmpeg = new FFMpeg();
            Assert.assertFalse(ffmpeg.isX264());
            Assert.assertTrue(ffmpeg.isOpenh264());
        }
    }

    @Test(groups="ut")
    public void testFFMpegWithEnvVariable() {
        try (MockedStatic mockedSystemUtility = Mockito.mockStatic(SystemUtility.class);
                MockedStatic mockedOsCommand = Mockito.mockStatic(OSCommand.class)
        ) {
            mockedSystemUtility.when(() -> SystemUtility.getenv("FFMPEG_PATH")).thenReturn("/usr/bin/ffmpeg");
            mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"/usr/bin/ffmpeg", "-version"})).thenReturn("ffmpeg version 7.1-full_build-www.gyan.dev Copyright (c) 2000-2024 the FFmpeg developers\n" +
                    "  built with gcc 14.2.0 (Rev1, Built by MSYS2 project)\n" +
                    "  configuration: --enable-gpl --enable-version3 --enable-static --disable-w32threads --disable-autodetect --enable-fontconfig --enable-iconv --enable-gnutls --enable-libxml2 --enable-gmp --enable-bzlib --enable-lzma --enable-libsnappy --enable-zlib --enable-librist --enable-libsrt --enable-libssh --enable-libzmq --enable-avisynth --enable-libbluray --enable-libcaca --enable-sdl2 --enable-libaribb24 --enable-libaribcaption --enable-libdav1d --enable-libdavs2 --enable-libopenjpeg --enable-libquirc --enable-libuavs3d --enable-libxevd --enable-libzvbi --enable-libqrencode --enable-librav1e --enable-libsvtav1 --enable-libvvenc --enable-libwebp --enable-libx264 --enable-libx265 --enable-libxavs2 --enable-libxeve --enable-libxvid --enable-libaom --enable-libjxl --enable-libvpx --enable-mediafoundation --enable-libass --enable-frei0r --enable-libfreetype --enable-libfribidi --enable-libharfbuzz --enable-liblensfun --enable-libvidstab --enable-libvmaf --enable-libzimg --enable-amf --enable-cuda-llvm --enable-cuvid --enable-dxva2 --enable-d3d11va --enable-d3d12va --enable-ffnvcodec --enable-libvpl --enable-nvdec --enable-nvenc --enable-vaapi --enable-libshaderc --enable-vulkan --enable-libplacebo --enable-opencl --enable-libcdio --enable-libgme --enable-libmodplug --enable-libopenmpt --enable-libopencore-amrwb --enable-libmp3lame --enable-libshine --enable-libtheora --enable-libtwolame --enable-libvo-amrwbenc --enable-libcodec2 --enable-libilbc --enable-libgsm --enable-liblc3 --enable-libopencore-amrnb --enable-libopus --enable-libspeex --enable-libvorbis --enable-ladspa --enable-libbs2b --enable-libflite --enable-libmysofa --enable-librubberband --enable-libsoxr --enable-chromaprint\n" +
                    "  libavutil      59. 39.100 / 59. 39.100\n" +
                    "  libavcodec     61. 19.100 / 61. 19.100\n" +
                    "  libavformat    61.  7.100 / 61.  7.100\n" +
                    "  libavdevice    61.  3.100 / 61.  3.100\n" +
                    "  libavfilter    10.  4.100 / 10.  4.100\n" +
                    "  libswscale      8.  3.100 /  8.  3.100\n" +
                    "  libswresample   5.  3.100 /  5.  3.100\n" +
                    "  libpostproc    58.  3.100 / 58.  3.100\n" +
                    "Universal media converter");

            new FFMpeg();
        }
    }

    @Test(groups="ut", expectedExceptions = ConfigurationException.class)
    public void testFFMpegNotPresent() {
        try (MockedStatic mockedOsCommand = Mockito.mockStatic(OSCommand.class)) {
            mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"ffmpeg", "-version"})).thenReturn("");

            new FFMpeg();
        }
    }


    @Test(groups="ut")
    public void testrunFFMpeg() {
        try (
             MockedStatic mockedOsCommand = Mockito.mockStatic(OSCommand.class)
        ) {
            mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"ffmpeg", "-version"})).thenReturn("ffmpeg version 7.1-full_build-www.gyan.dev Copyright (c) 2000-2024 the FFmpeg developers\n" +
                    "  built with gcc 14.2.0 (Rev1, Built by MSYS2 project)\n" +
                    "  configuration: --enable-gpl --enable-version3 --enable-static --disable-w32threads --disable-autodetect --enable-fontconfig --enable-iconv --enable-gnutls --enable-libxml2 --enable-gmp --enable-bzlib --enable-lzma --enable-libsnappy --enable-zlib --enable-librist --enable-libsrt --enable-libssh --enable-libzmq --enable-avisynth --enable-libbluray --enable-libcaca --enable-sdl2 --enable-libaribb24 --enable-libaribcaption --enable-libdav1d --enable-libdavs2 --enable-libopenjpeg --enable-libquirc --enable-libuavs3d --enable-libxevd --enable-libzvbi --enable-libqrencode --enable-librav1e --enable-libsvtav1 --enable-libvvenc --enable-libwebp --enable-libx264 --enable-libx265 --enable-libxavs2 --enable-libxeve --enable-libxvid --enable-libaom --enable-libjxl --enable-libvpx --enable-mediafoundation --enable-libass --enable-frei0r --enable-libfreetype --enable-libfribidi --enable-libharfbuzz --enable-liblensfun --enable-libvidstab --enable-libvmaf --enable-libzimg --enable-amf --enable-cuda-llvm --enable-cuvid --enable-dxva2 --enable-d3d11va --enable-d3d12va --enable-ffnvcodec --enable-libvpl --enable-nvdec --enable-nvenc --enable-vaapi --enable-libshaderc --enable-vulkan --enable-libplacebo --enable-opencl --enable-libcdio --enable-libgme --enable-libmodplug --enable-libopenmpt --enable-libopencore-amrwb --enable-libmp3lame --enable-libshine --enable-libtheora --enable-libtwolame --enable-libvo-amrwbenc --enable-libcodec2 --enable-libilbc --enable-libgsm --enable-liblc3 --enable-libopencore-amrnb --enable-libopus --enable-libspeex --enable-libvorbis --enable-ladspa --enable-libbs2b --enable-libflite --enable-libmysofa --enable-librubberband --enable-libsoxr --enable-chromaprint\n" +
                    "  libavutil      59. 39.100 / 59. 39.100\n" +
                    "  libavcodec     61. 19.100 / 61. 19.100\n" +
                    "  libavformat    61.  7.100 / 61.  7.100\n" +
                    "  libavdevice    61.  3.100 / 61.  3.100\n" +
                    "  libavfilter    10.  4.100 / 10.  4.100\n" +
                    "  libswscale      8.  3.100 /  8.  3.100\n" +
                    "  libswresample   5.  3.100 /  5.  3.100\n" +
                    "  libpostproc    58.  3.100 / 58.  3.100\n" +
                    "Universal media converter");

            new FFMpeg().runFFmpegCommand(List.of("-i", "myfile.avi", "-c:v", "libx264"));
            mockedOsCommand.verify(() -> OSCommand.executeCommandAndWait(new String[] {"ffmpeg", "-i", "myfile.avi", "-c:v", "libx264"}));
        }
    }

    /**
     * Check openh264 is translated to x264 when openh264 is not available
     */
    @Test(groups="ut")
    public void testrunFFMpeg2() {
        try (
             MockedStatic mockedOsCommand = Mockito.mockStatic(OSCommand.class)
        ) {
            mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"ffmpeg", "-version"})).thenReturn("ffmpeg version 7.1-full_build-www.gyan.dev Copyright (c) 2000-2024 the FFmpeg developers\n" +
                    "  built with gcc 14.2.0 (Rev1, Built by MSYS2 project)\n" +
                    "  configuration: --enable-gpl --enable-version3 --enable-static --disable-w32threads --disable-autodetect --enable-fontconfig --enable-iconv --enable-gnutls --enable-libxml2 --enable-gmp --enable-bzlib --enable-lzma --enable-libsnappy --enable-zlib --enable-librist --enable-libsrt --enable-libssh --enable-libzmq --enable-avisynth --enable-libbluray --enable-libcaca --enable-sdl2 --enable-libaribb24 --enable-libaribcaption --enable-libdav1d --enable-libdavs2 --enable-libopenjpeg --enable-libquirc --enable-libuavs3d --enable-libxevd --enable-libzvbi --enable-libqrencode --enable-librav1e --enable-libsvtav1 --enable-libvvenc --enable-libwebp --enable-libx264 --enable-libx265 --enable-libxavs2 --enable-libxeve --enable-libxvid --enable-libaom --enable-libjxl --enable-libvpx --enable-mediafoundation --enable-libass --enable-frei0r --enable-libfreetype --enable-libfribidi --enable-libharfbuzz --enable-liblensfun --enable-libvidstab --enable-libvmaf --enable-libzimg --enable-amf --enable-cuda-llvm --enable-cuvid --enable-dxva2 --enable-d3d11va --enable-d3d12va --enable-ffnvcodec --enable-libvpl --enable-nvdec --enable-nvenc --enable-vaapi --enable-libshaderc --enable-vulkan --enable-libplacebo --enable-opencl --enable-libcdio --enable-libgme --enable-libmodplug --enable-libopenmpt --enable-libopencore-amrwb --enable-libmp3lame --enable-libshine --enable-libtheora --enable-libtwolame --enable-libvo-amrwbenc --enable-libcodec2 --enable-libilbc --enable-libgsm --enable-liblc3 --enable-libopencore-amrnb --enable-libopus --enable-libspeex --enable-libvorbis --enable-ladspa --enable-libbs2b --enable-libflite --enable-libmysofa --enable-librubberband --enable-libsoxr --enable-chromaprint\n" +
                    "  libavutil      59. 39.100 / 59. 39.100\n" +
                    "  libavcodec     61. 19.100 / 61. 19.100\n" +
                    "  libavformat    61.  7.100 / 61.  7.100\n" +
                    "  libavdevice    61.  3.100 / 61.  3.100\n" +
                    "  libavfilter    10.  4.100 / 10.  4.100\n" +
                    "  libswscale      8.  3.100 /  8.  3.100\n" +
                    "  libswresample   5.  3.100 /  5.  3.100\n" +
                    "  libpostproc    58.  3.100 / 58.  3.100\n" +
                    "Universal media converter");

            new FFMpeg().runFFmpegCommand(List.of("-i", "myfile.avi", "-c:v", "libopenh264"));
            mockedOsCommand.verify(() -> OSCommand.executeCommandAndWait(new String[] {"ffmpeg", "-i", "myfile.avi", "-c:v", "libx264"}));
        }
    }

    /**
     * Check x264 is translated to openh264 when x264 is not available
     */
    @Test(groups="ut")
    public void testrunFFMpeg3() {
        try (
             MockedStatic mockedOsCommand = Mockito.mockStatic(OSCommand.class)
        ) {
            mockedOsCommand.when(() -> OSCommand.executeCommandAndWait(new String[] {"ffmpeg", "-version"})).thenReturn("ffmpeg version 7.1-full_build-www.gyan.dev Copyright (c) 2000-2024 the FFmpeg developers\n" +
                    "  built with gcc 14.2.0 (Rev1, Built by MSYS2 project)\n" +
                    "  configuration: --enable-gpl --enable-version3 --enable-static --disable-w32threads --disable-autodetect --enable-fontconfig --enable-iconv --enable-gnutls --enable-libxml2 --enable-gmp --enable-bzlib --enable-lzma --enable-libsnappy --enable-zlib --enable-librist --enable-libsrt --enable-libssh --enable-libzmq --enable-avisynth --enable-libbluray --enable-libcaca --enable-sdl2 --enable-libaribb24 --enable-libaribcaption --enable-libdav1d --enable-libdavs2 --enable-libopenjpeg --enable-libquirc --enable-libuavs3d --enable-libxevd --enable-libzvbi --enable-libqrencode --enable-librav1e --enable-libsvtav1 --enable-libvvenc --enable-libwebp --enable-libopenh264 --enable-libx265 --enable-libxavs2 --enable-libxeve --enable-libxvid --enable-libaom --enable-libjxl --enable-libvpx --enable-mediafoundation --enable-libass --enable-frei0r --enable-libfreetype --enable-libfribidi --enable-libharfbuzz --enable-liblensfun --enable-libvidstab --enable-libvmaf --enable-libzimg --enable-amf --enable-cuda-llvm --enable-cuvid --enable-dxva2 --enable-d3d11va --enable-d3d12va --enable-ffnvcodec --enable-libvpl --enable-nvdec --enable-nvenc --enable-vaapi --enable-libshaderc --enable-vulkan --enable-libplacebo --enable-opencl --enable-libcdio --enable-libgme --enable-libmodplug --enable-libopenmpt --enable-libopencore-amrwb --enable-libmp3lame --enable-libshine --enable-libtheora --enable-libtwolame --enable-libvo-amrwbenc --enable-libcodec2 --enable-libilbc --enable-libgsm --enable-liblc3 --enable-libopencore-amrnb --enable-libopus --enable-libspeex --enable-libvorbis --enable-ladspa --enable-libbs2b --enable-libflite --enable-libmysofa --enable-librubberband --enable-libsoxr --enable-chromaprint\n" +
                    "  libavutil      59. 39.100 / 59. 39.100\n" +
                    "  libavcodec     61. 19.100 / 61. 19.100\n" +
                    "  libavformat    61.  7.100 / 61.  7.100\n" +
                    "  libavdevice    61.  3.100 / 61.  3.100\n" +
                    "  libavfilter    10.  4.100 / 10.  4.100\n" +
                    "  libswscale      8.  3.100 /  8.  3.100\n" +
                    "  libswresample   5.  3.100 /  5.  3.100\n" +
                    "  libpostproc    58.  3.100 / 58.  3.100\n" +
                    "Universal media converter");

            new FFMpeg().runFFmpegCommand(List.of("-i", "myfile.avi", "-c:v", "libx264"));
            mockedOsCommand.verify(() -> OSCommand.executeCommandAndWait(new String[] {"ffmpeg", "-i", "myfile.avi", "-c:v", "libopenh264"}));
        }
    }

}
