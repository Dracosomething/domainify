package io.github.dracosomething.windows;

import io.github.dracosomething.Build;
import io.github.dracosomething.util.Console;
import io.github.dracosomething.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class BuildWindows {
    public static void build(File jar, File out) {
        File buildDir = new File(out, "buildWindows");
        FileUtils.makeDir(buildDir);


        File buildJar = new File(buildDir, "domainify.jar");
        try {
            Files.copy(jar.toPath(), buildJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            File config = new Configure.ConfigureBuilder()
                    .setParentFile(buildDir)
                    .createInternalObject("launch4jConfig")
                    .addXMLObject("headerType", "gui")
                    .addXMLObject("outfile", "./domainify.exe")
                    .addXMLObject("jar", "domainify.jar")
                    .addXMLObject("dontWrapJar", true)
                    .addXMLObject("stayAlive", true)
                    .createInternalObject("classPath")
                    .addXMLObject("mainClass", "io.github.dracosomething.Main")
                    .addXMLObject("cp", "./domainify.jar")
                    .leaveInternalObject()
                    .createInternalObject("jre")
                    .addXMLObject("path", "%JAVA_HOME%;%PATH%")
                    .leaveInternalObject()
                    .createInternalObject("versionInfo")
                    .addXMLObject("fileVersion", "1.0.0.0")
                    .addXMLObject("txtFileVersion", "1.0-SNAPSHOT")
                    .addXMLObject("fileDescription", "The main executable for domainify.")
                    .addXMLObject("copyright", "null")
                    .addXMLObject("productVersion", "1.0.0.0")
                    .addXMLObject("txtProductVersion", "1.0-SNAPSHOT")
                    .addXMLObject("productName", "Domainify")
                    .addXMLObject("internalName", "domainify")
                    .addXMLObject("originalFilename", "domainify.exe")
                    .leaveInternalObject()
                    .leaveInternalObject()
                    .build();

            File launch4jDir = new File("C:\\Program Files (x86)\\Launch4j");
            Console console = new Console();
            console.directory(launch4jDir);
            console.runCommand("launch4jc.exe " + config.getAbsolutePath());
            console.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
