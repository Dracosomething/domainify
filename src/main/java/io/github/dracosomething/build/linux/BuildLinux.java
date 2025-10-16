package io.github.dracosomething.build.linux;

import io.github.dracosomething.build.Build;
import io.github.dracosomething.util.Console;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class BuildLinux {
    public static void build(File jar, File out) {
        // add build-linux module that will build the project for linux.
        // use https://developer.apple.com/library/archive/documentation/Java/Conceptual/Jar_Bundler/Introduction/Introduction.html#//apple_ref/doc/uid/TP40000884
        // for mac os
        // use launch4j for windows

        File buildDir = new File(out, "buildLinux");
        if (!buildDir.exists()) {
            buildDir.mkdirs();
        }

        File buildJar = new File(buildDir, "domainify.jar");
        File script = new File(buildDir, "domainify");
        try {
            Files.copy(jar.toPath(), buildJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Console console = new Console();
            console.directory(buildDir);
            console.runCommand("echo '#!/usr/bin/java -jar' > domainify");
            console.runCommand("car domainify.jar >> domainify");
            console.runCommand("chmod +x domainify");
            File tarBall = new File(buildDir, "linux.tar.gz");
            if (!tarBall.exists()) {
                tarBall.createNewFile();
            }

            Build.archive("tar", tarBall, buildJar, script);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
