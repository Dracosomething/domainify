package io.github.dracosomething;

import io.github.dracosomething.linux.BuildLinux;
import io.github.dracosomething.util.FileUtils;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Build {
    public static void main(String[] args) {
        File project = new File("./");
        File out = new File(project, "out");
        File jar = new File(out, "artifacts/domainify_jar/domainify.jar");
        File buildDir = new File(out, "build");
        if (!buildDir.exists()) {
            buildDir.mkdirs();
        }
        BuildLinux.build(jar, buildDir);
    }

    public static void archive(String type, File archive, File... toArchive) {
        try {
            FileOutputStream fileOut = new FileOutputStream(archive);
            switch (type) {
                case "tar": {
                    TarArchiveOutputStream out = FileUtils.FACTORY.createArchiveOutputStream("tar",
                            new GzipCompressorOutputStream(fileOut));
                    for (File file : toArchive) {
                        TarArchiveEntry entry = new TarArchiveEntry(file);
                        entry.setSize(file.length());
                        out.putArchiveEntry(entry);
                        try (InputStream stream = new FileInputStream(file)) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = stream.read()) > 0) {
                                out.write(buffer, 0, len);
                            }
                        }
                        out.closeArchiveEntry();
                    }
                    out.close();
                }
                case "zip": {
                    ZipOutputStream out = new ZipOutputStream(fileOut);

                    for (File file : toArchive) {
                        ZipEntry entry = new ZipEntry(file.getName());
                        out.putNextEntry(entry);
                        try (InputStream stream = new FileInputStream(file)) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = stream.read()) > 0) {
                                out.write(buffer, 0, len);
                            }
                        }
                    }

                    out.close();
                }
            }
            fileOut.close();
        } catch (IOException | ArchiveException e) {
            e.printStackTrace();
        }
    }
}
