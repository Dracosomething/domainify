import io.github.dracosomething.util.FileUtils;
import org.apache.commons.compress.archivers.ArchiveException;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class LinuxTests {
    @Test
    public void setup_pcre_test() throws IOException, ArchiveException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(FileUtils.DATA));
        String version = "322r34";
        FileUtils.setupPCRE(writer, version);
    }
}
