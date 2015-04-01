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
package uk.jamierocks.jvml.ccgradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import uk.jamierocks.jvml.ccgradle.utils.Utils

/**
 * Created by jamie on 31/03/15.
 */
class CCPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task("setupJvmlWorkspace") << {
            project.getLogger().lifecycle("***************************");
            project.getLogger().lifecycle("CCGradle by Jamie Mansfield");
            project.getLogger().lifecycle("***************************");

            // These are all the files and directories required
            File outputDir = new File(project.getBuildDir(), "jvml");
            File repoDir = new File(outputDir, "JVML-JIT")
            File cclibDir = new File(repoDir, "CCLib");

            // Create directories
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            // Clone the JVML-JIT repo
            if (repoDir.exists()) {
                repoDir.deleteDir()
            }
            Utils.clone("https://github.com/Team-CC-Corp/JVML-JIT.git", repoDir)
            project.getLogger("Cloned JVML-JIT repo")

            // Build CCLib
            try {
                Utils.runProcess(cclibDir, "ant", "main");
            } catch (Exception e) {
                project.getLogger().error("Oh noes! Something broke", e)
            }
            project.getLogger().debug("Built CCLib");

            // Add CCLib runtime to project
            project.dependencies {
                compile project.files('build/jvml/JVML-JIT/build/jar/cc_rt.jar')
            }
            project.getLogger().debug("Added CCLib to the dependencies");
        };
    }
}
