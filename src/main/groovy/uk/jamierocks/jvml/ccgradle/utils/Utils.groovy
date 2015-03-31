/*
 * This file is part of CCGradle, licensed under the MIT License (MIT).
 *
 * Copyright (c) Jamie Mansfield <https://github.com/jamierocks>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package uk.jamierocks.jvml.ccgradle.utils

import com.google.common.base.Throwables

import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Created by jamie on 31/03/15.
 */
class Utils {

    static downloadFile(URL url, File output) {
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(output);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }

    /**
     * Unzip it
     *
     * Thanks to mkyong ( http://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/ )
     *
     * @param zipFile input zip file
     * @param output zip file output folder
     */
    static void unzip(String zipFile, String outputFolder) {
        byte[] buffer = new byte[1024];

        try {
            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public static int runProcess(File workDir, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir);
        pb.environment().put("JAVA_HOME", System.getProperty("java.home"));

        final Process ps = pb.start();

        new Thread(new StreamRedirector(ps.getInputStream(), System.out)).start();
        new Thread(new StreamRedirector(ps.getErrorStream(), System.err)).start();

        int status = ps.waitFor();

        if (status != 0) {
            throw new RuntimeException("Error running command, return status !=0: " + Arrays.toString(command));
        }

        return status;
    }

    private static class StreamRedirector implements Runnable {

        private final InputStream inStream;
        private final PrintStream outputStream;

        public StreamRedirector(InputStream input, PrintStream output) {
            this.inStream = input;
            this.outputStream = output;
        }

        @Override
        public void run() {
            BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    outputStream.println(line);
                }
            } catch (IOException ex) {
                throw Throwables.propagate(ex);
            }
        }
    }
}
