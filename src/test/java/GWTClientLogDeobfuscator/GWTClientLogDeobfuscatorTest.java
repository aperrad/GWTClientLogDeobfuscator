package GWTClientLogDeobfuscator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class GWTClientLogDeobfuscatorTest {
	private static final String LOG_FILE = "src/test/resources/gwt-log.txt";

	private static final String SYMBOLS_FILE = "src/test/resources/test.symbolMap";

	private static final String WAR_FILE = "src/test/resources/webapp.war";

	private static final String PATH_TO_WRITE = "src/test/resources/output.log";

	@Test
	public void test_generate_source_maps() {
		Map<String, String> sourceMapsMap = GWTClientLogDeobfuscator.generateMapFromSourceMapFile(SYMBOLS_FILE);
		Assert.assertTrue(sourceMapsMap.size() == 3);
	}

	@Test
	public void test_get_method_name_from_source_map_key() throws IOException {
		File initialFile = new File(LOG_FILE);

		Map<String, String> sourceMap = GWTClientLogDeobfuscator.generateMapFromSourceMapFile(SYMBOLS_FILE);
		GWTClientLogDeobfuscator.deobfuscateStackTrace(LOG_FILE, sourceMap, PATH_TO_WRITE);

		File deobfuscateFile = new File(PATH_TO_WRITE);
		String fileAsString = FileUtils.readFileToString(deobfuscateFile, Charset.defaultCharset());
		Assert.assertFalse(FileUtils.contentEquals(initialFile, deobfuscateFile));
		Assert.assertTrue(fileAsString.contains("com.test.Method"));
		Assert.assertTrue(fileAsString.contains("com.test.Method2"));
		Assert.assertTrue(fileAsString.contains("com.test.Method3"));

		deobfuscateFile.delete();
	}

	@Test
	public void test_get_method_name_from_source_map_key_with_war_file() throws IOException {
		File initialFile = new File(LOG_FILE);

		InputStream sourceMapFile = GWTClientLogDeobfuscator.getSourceMapFileFromWar(WAR_FILE, "safari", "fr");
		Map<String, String> sourceMap = GWTClientLogDeobfuscator.generateMapFromSourceMapFile(sourceMapFile);
		GWTClientLogDeobfuscator.deobfuscateStackTrace(LOG_FILE, sourceMap, PATH_TO_WRITE);

		File deobfuscateFile = new File(PATH_TO_WRITE);
		String fileAsString = FileUtils.readFileToString(deobfuscateFile, Charset.defaultCharset());
		Assert.assertFalse(FileUtils.contentEquals(initialFile, deobfuscateFile));
		Assert.assertTrue(fileAsString.contains("com.test.Method"));
		Assert.assertTrue(fileAsString.contains("com.test.Method2"));
		Assert.assertTrue(fileAsString.contains("com.test.Method3"));

		deobfuscateFile.delete();
	}

	@Test
	public void test_get_method_name_from_source_map_key_with_war_file_and_unormalized_user_agent() throws IOException {
		File initialFile = new File(LOG_FILE);

		InputStream sourceMapFile = GWTClientLogDeobfuscator.getSourceMapFileFromWar(WAR_FILE, "Chrome ", "fr");
		Map<String, String> sourceMap = GWTClientLogDeobfuscator.generateMapFromSourceMapFile(sourceMapFile);
		GWTClientLogDeobfuscator.deobfuscateStackTrace(LOG_FILE, sourceMap, PATH_TO_WRITE);

		File deobfuscateFile = new File(PATH_TO_WRITE);
		String fileAsString = FileUtils.readFileToString(deobfuscateFile, Charset.defaultCharset());
		Assert.assertFalse(FileUtils.contentEquals(initialFile, deobfuscateFile));
		Assert.assertTrue(fileAsString.contains("com.test.Method"));
		Assert.assertTrue(fileAsString.contains("com.test.Method2"));
		Assert.assertTrue(fileAsString.contains("com.test.Method3"));

		deobfuscateFile.delete();
	}

	@Test
	public void test_get_method_name_from_source_map_key_with_war_file_and_unormalized_locale() throws IOException {
		File initialFile = new File(LOG_FILE);

		InputStream sourceMapFile = GWTClientLogDeobfuscator.getSourceMapFileFromWar(WAR_FILE, "safari", "FR");
		Map<String, String> sourceMap = GWTClientLogDeobfuscator.generateMapFromSourceMapFile(sourceMapFile);
		GWTClientLogDeobfuscator.deobfuscateStackTrace(LOG_FILE, sourceMap, PATH_TO_WRITE);

		File deobfuscateFile = new File(PATH_TO_WRITE);
		String fileAsString = FileUtils.readFileToString(deobfuscateFile, Charset.defaultCharset());
		Assert.assertFalse(FileUtils.contentEquals(initialFile, deobfuscateFile));
		Assert.assertTrue(fileAsString.contains("com.test.Method"));
		Assert.assertTrue(fileAsString.contains("com.test.Method2"));
		Assert.assertTrue(fileAsString.contains("com.test.Method3"));

		deobfuscateFile.delete();
	}

	@Test(expected = FileNotFoundException.class)
	public void test_fail_to_find_symbolmap_from_war_file_and_incorrect_useragent() throws IOException {
		InputStream sourceMapFile = GWTClientLogDeobfuscator.getSourceMapFileFromWar(WAR_FILE, "unknown user agent",
				"fr");
		Map<String, String> sourceMap = GWTClientLogDeobfuscator.generateMapFromSourceMapFile(sourceMapFile);
		GWTClientLogDeobfuscator.deobfuscateStackTrace(LOG_FILE, sourceMap, PATH_TO_WRITE);
	}

	@Test(expected = FileNotFoundException.class)
	public void test_fail_to_find_symbolmap_from_war_file_and_incorrect_locale() throws IOException {
		InputStream sourceMapFile = GWTClientLogDeobfuscator.getSourceMapFileFromWar(WAR_FILE, "safari",
				"unknown locale");
		Map<String, String> sourceMap = GWTClientLogDeobfuscator.generateMapFromSourceMapFile(sourceMapFile);
		GWTClientLogDeobfuscator.deobfuscateStackTrace(LOG_FILE, sourceMap, PATH_TO_WRITE);
	}
}
