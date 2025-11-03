package io.github.dracosomething.linux;

import io.github.dracosomething.Build;
import io.github.dracosomething.util.Console;
import io.github.dracosomething.util.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class BuildLinux {
    public static void build(File jar, File out) {
        // add build-linux module that will build the project for linux.
        // use https://github.com/crotwell/gradle-macappbundle/wiki/Introref/doc/uid/TP40000884
        // for mac os
        // use launch4j for windows

        File buildDir = new File(out, "buildLinux");
        FileUtils.makeDir(buildDir);

        File buildJar = new File(buildDir, "domainify.jar");
        File script = new File(buildDir, "domainify");
        try {
            Files.copy(jar.toPath(), buildJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Console console = new Console();
            console.directory(buildDir);
            console.runCommand("echo '#!/usr/bin/java -jar' > ./domainify");
            console.runCommand("cat domainify.jar >> ./domainify");
            console.runCommand("chmod +x ./domainify");
            console.schedule((tmp) -> {
                File tarBall = new File(buildDir, "linux.tar.gz");
                if (!tarBall.exists()) {
                    try {
                        tarBall.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Build.archive("tar", tarBall, buildJar, script);
            });
            console.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
