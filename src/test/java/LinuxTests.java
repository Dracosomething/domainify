import io.github.dracosomething.util.FileUtils;
import org.apache.commons.compress.archivers.ArchiveException;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LinuxTests {
    @Test
    public void gnu_setup_test() throws IOException, ArchiveException {
        String[] data = FileUtils.extractData();
        final int libTool = 6;
        final int autoconf = 7;
        String libtoolVersion = data[libTool];
        String autoconfVersion = data[autoconf];
        BufferedWriter writer = new BufferedWriter(new FileWriter(FileUtils.DATA));
        FileUtils.setupGNU(writer, libtoolVersion, autoconfVersion);
    }
}
