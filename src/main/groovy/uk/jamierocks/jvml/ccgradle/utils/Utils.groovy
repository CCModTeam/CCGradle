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

/**
 * Created by jamie on 31/03/15.
 */
class Utils {

    static int runProcess(File workDir, String... command) throws Exception {
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
