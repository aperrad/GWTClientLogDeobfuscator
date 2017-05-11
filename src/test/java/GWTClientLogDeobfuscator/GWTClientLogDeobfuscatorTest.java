package GWTClientLogDeobfuscator;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public class GWTClientLogDeobfuscatorTest
        extends TestCase {
    private static final String LOG_FILE = "src/test/resources/gwt-log.txt";

    private static final String SYMBOLS_FILE = "src/test/resources/test.symbolMap";

    private static final String PATH_TO_WRITE = "src/test/resources/output.log";

    public void test_generate_source_maps() {
        Map<String, String> sourceMapsMap = GWTClientLogDeobfuscator.generateMapFromSourceMapFile(SYMBOLS_FILE);
        Assert.assertTrue(sourceMapsMap.size() == 3);
    }

    public void test_get_method_name_from_source_map_key() throws IOException {
        File initialFile = new File(LOG_FILE);

        Map<String, String> sourceMap = GWTClientLogDeobfuscator.generateMapFromSourceMapFile(SYMBOLS_FILE);
        GWTClientLogDeobfuscator.deobfuscateStackTrace(LOG_FILE, sourceMap, PATH_TO_WRITE);

        File deobfuscateFile = new File(PATH_TO_WRITE);
        String fileAsString = FileUtils.readFileToString(deobfuscateFile, Charset.defaultCharset());
        Assert.assertFalse(FileUtils.contentEquals(initialFile,deobfuscateFile));
        Assert.assertTrue(fileAsString.contains("com.test.Method"));
        Assert.assertTrue(fileAsString.contains("com.test.Method2"));
        Assert.assertTrue(fileAsString.contains("com.test.Method3"));

        deobfuscateFile.delete();
    }
}
