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
        FileUtils.setupGNU();
    }
}
