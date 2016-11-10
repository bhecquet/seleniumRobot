/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
package com.seleniumtests.util;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtility;

public class FileUtility {
	private static final Logger logger = SeleniumRobotLogger.getLogger(WebUIDriver.class);
    static final int BUFFER = 2048;
    
    private FileUtility() {
		// As a utility method, it is not made to be instantiated.
	}
    
    public static void extractJar(final String storeLocation, final Class<?> clz) throws IOException {
        File firefoxProfile = new File(storeLocation);
        String location = clz.getProtectionDomain().getCodeSource().getLocation().getFile();

        JarFile jar = new JarFile(location);
        logger.info("Extracting jar file::: " + location);
        firefoxProfile.mkdir();

        Enumeration<?> jarFiles = jar.entries();
        while (jarFiles.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) jarFiles.nextElement();
            String currentEntry = entry.getName();
            File destinationFile = new File(storeLocation, currentEntry);
            File destinationParent = destinationFile.getParentFile();

            // create the parent directory structure if required
            destinationParent.mkdirs();
            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(jar.getInputStream(entry));
                int currentByte;

                // buffer for writing file
                byte[] data = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destinationFile);
                BufferedOutputStream destination = new BufferedOutputStream(fos, BUFFER);

                // read and write till last byte
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    destination.write(data, 0, currentByte);
                }

                destination.flush();
                destination.close();
                is.close();
            }
        }
        jar.close();

        FileUtils.deleteDirectory(new File(storeLocation + "\\META-INF"));
        if (OSUtility.isWindows()) {
            new File(storeLocation + "\\" + clz.getCanonicalName().replaceAll("\\.", "\\\\") + ".class").delete();
        } else {
            new File(storeLocation + "/" + clz.getCanonicalName().replaceAll("\\.", "/") + ".class").delete();
        }
    }

    /**
     * Constructs ImageElement from bytes and stores it.
     *
     * @param  path
     */
    public static synchronized void writeImage(final String path, final byte[] byteArray) {
        if (byteArray.length == 0) {
            return;
        }
        
        File parentDir = new File(path).getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        byte[] decodeBuffer = Base64.decodeBase64(byteArray);

        try (InputStream in = new ByteArrayInputStream(decodeBuffer);
             FileOutputStream fos = new FileOutputStream(path);
        		) {

            BufferedImage img = ImageIO.read(in);
            ImageIO.write(img, "png", fos);
        } catch (Exception e) {
            logger.warn(e);
        } 
    }

    public static String getLatestFile(final String folder) {
        String file = null;
        File folderFile = new File(folder);
        if (folderFile.exists() && folderFile.isDirectory()) {
            File[] files = folderFile.listFiles();
            long date = 0;

            for (int i = 0; i < files.length; i++) {
                if (files[i].lastModified() > date) {
                    date = files[i].lastModified();
                    file = files[i].getAbsolutePath();
                }
            }
        }

        return file;
    }

    public static String decodePath(final String path) throws UnsupportedEncodingException {
        return URLDecoder.decode(path, "UTF-8");
    }
}
