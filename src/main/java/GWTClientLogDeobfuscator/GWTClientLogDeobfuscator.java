package GWTClientLogDeobfuscator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class GWTClientLogDeobfuscator {

	public static void main(String[] args) throws IOException, ParseException {
		final Options firstOptions = configFirstParameters();
		final Options options = configParameters(firstOptions);
		final CommandLineParser parser = new DefaultParser();
		final CommandLine firstLine = parser.parse(firstOptions, args, true);

		boolean helpMode = firstLine.hasOption("help");
		if (helpMode || firstLine.getArgList().isEmpty()) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("gwt-client-log-deobfuscator-jar-with-dependencies.jar", options, true);
			System.exit(0);
		}

		final CommandLine line = parser.parse(options, args);

		String stackTracePath = line.getOptionValue("stacktrace");
		String war = line.getOptionValue("war", "");
		String symbolMap = line.getOptionValue("symbolmap", "");
		String path = line.getOptionValue("output");
		String userAgent = line.getOptionValue("useragent", "unknown");
		String locale = line.getOptionValue("locale", "unknown");
		String user = line.getOptionValue("user", "");
		String password = line.getOptionValue("password", "");

		Map<String, String> map = new HashMap<>();
		if (!symbolMap.isEmpty()) {
			map = generateMapFromSourceMapFile(symbolMap);
		} else if (!war.isEmpty() && !userAgent.isEmpty() && !locale.isEmpty()) {
			InputStream warSourceMap;
			if (war.startsWith("http")) {
				System.out.println("War path, is an http URL, try to download it");
				InputStream warFile = getFile(war, user, password);
				warSourceMap = getSourceMapFileFromWar(warFile, userAgent, locale);
			} else {
				warSourceMap = getSourceMapFileFromWar(war, userAgent, locale);
			}

			if (warSourceMap == null) {
				return;
			}
			map = generateMapFromSourceMapFile(warSourceMap);
		} else {
			System.err.println(
					"Cannot deobfuscate stacktrace without symbolMap or WAR file with a correct user agent and locale");
			return;
		}

		deobfuscateStackTrace(stackTracePath, map, path);
	}

	private static InputStream getFile(String url, String user, String password)
			throws ClientProtocolException, IOException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(user, password), "UTF-8", false));

		HttpResponse httpResponse = httpClient.execute(httpGet);
		HttpEntity responseEntity = httpResponse.getEntity();
		return responseEntity.getContent();
	}

	private static String normalizeUserAgent(String userAgent) {
		String normalizeUserAgent = new String(userAgent.toLowerCase().trim());

		if (normalizeUserAgent.equals("chrome")) {
			normalizeUserAgent = "safari";
		} else if (normalizeUserAgent.equals("firefox")) {
			normalizeUserAgent = "gecko1_8";
		}
		return normalizeUserAgent;
	}

	private static String normalizeLocale(String userAgent) {
		return userAgent.toLowerCase().trim();
	}

	private static Options configFirstParameters() {

		final Option helpFileOption = Option.builder("h") //
				.longOpt("help").desc("Display help").build();

		final Options firstOptions = new Options();

		firstOptions.addOption(helpFileOption);

		return firstOptions;

	}

	private static Options configParameters(Options firstOptions) {

		final Option stackOption = Option.builder("s").longOpt("stacktrace").desc("Stack trace file path").hasArg(true)
				.argName("stacktrace").required(true).build();

		final Option warOption = Option.builder("w").longOpt("war").desc("Webapp WAR file path").hasArg(true)
				.argName("war").required(false).build();

		final Option userAgentOption = Option.builder("u").longOpt("useragent")
				.desc("User agent used when exception was thrown. \nAuthorized values are : ie8,ie9,ie10, gecko1_8, safari, chrome, firefox")
				.hasArg(true).argName("useragent").required(false).build();

		final Option localeOption = Option.builder("l").longOpt("locale").desc("Locale used when exception was thrown")
				.hasArg(true).argName("locale").required(false).build();

		final Option symbolMapOption = Option.builder("m").longOpt("symbolmap")
				.desc("Symbol map to deobfuscate exception").hasArg(true).argName("symbolmap").required(false).build();

		final Option outputOption = Option.builder("o").longOpt("output")
				.desc("Output file path for deobfuscate stacktrace").hasArg(true).argName("output").required(true)
				.build();

		final Option userOption = Option.builder("user").desc("User to connect to war location if remote").hasArg(true)
				.argName("user").required(false).build();

		final Option passwordOption = Option.builder("pwd").longOpt("password")
				.desc("Password used to connect to war location if remote").hasArg(true).argName("password")
				.required(false).build();
		final Options options = new Options();

		// First Options
		for (final Option fo : firstOptions.getOptions()) {
			options.addOption(fo);
		}

		options.addOption(stackOption);
		options.addOption(warOption);
		options.addOption(userAgentOption);
		options.addOption(localeOption);
		options.addOption(symbolMapOption);
		options.addOption(outputOption);
		options.addOption(userOption);
		options.addOption(passwordOption);

		return options;
	}

	private static InputStream getSourceMapFileFromWar(InputStream war, String inputUserAgent, String inputLocale)
			throws IOException {
		File tmpFile = File.createTempFile("GWTClientLogDeobfuscator-war-file", "tmp");
		FileUtils.copyInputStreamToFile(war, tmpFile);
		ZipFile warFile = new ZipFile(tmpFile);
		return getSourceMapFileFromWar(warFile, inputUserAgent, inputLocale);
	}

	protected static InputStream getSourceMapFileFromWar(String warPath, String inputUserAgent, String inputLocale)
			throws IOException {
		ZipFile warFile = new ZipFile(warPath);
		return getSourceMapFileFromWar(warFile, inputUserAgent, inputLocale);
	}

	private static InputStream getSourceMapFileFromWar(ZipFile warFile, String inputUserAgent, String inputLocale)
			throws IOException {
		String userAgent = normalizeUserAgent(inputUserAgent);
		String locale = normalizeLocale(inputLocale);
		Enumeration<?> zipEntries = warFile.entries();

		while (zipEntries.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();
			String fileName = zipEntry.getName();
			if (fileName.endsWith(".symbolMap")) {
				if (isNeededSourceMap(warFile.getInputStream(zipEntry), userAgent, locale)) {
					return getZipEntryInputStream(warFile, zipEntry);
				}
			}
		}

		String exceptionMsg = "No sourcemap was found for useragent " + userAgent + " and locale " + locale;
		throw new FileNotFoundException(exceptionMsg);
	}

	private static InputStream getZipEntryInputStream(ZipFile warFile, ZipEntry zipEntry) throws IOException {
		return warFile.getInputStream(zipEntry);
	}

	private static boolean isNeededSourceMap(InputStream symbolMap, String userAgent, String locale) {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(symbolMap, "UTF-8"))) {
			String line;
			int lineCounter = 0;
			while (lineCounter < 10) {
				line = br.readLine();
				if (line.startsWith("#") && lineCounter > 0) {
					String substring = line.substring(2);
					if (!substring.startsWith("{")) {
						return false;
					}

					JSONObject json = new JSONObject(substring);
					String jsonLocale = json.get("locale").toString();
					String jsonUserAgent = json.get("user.agent").toString();
					if (jsonLocale.equals(locale) && jsonUserAgent.equals(userAgent)) {
						return true;
					}
				}
				lineCounter++;
			}
		} catch (FileNotFoundException fException) {
		} catch (IOException ioException) {
		}
		return false;
	}

	protected static Map<String, String> generateMapFromSourceMapFile(String sourceMapPath) {
		File file = new File(sourceMapPath);
		Map<String, String> sourceMap = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] splittedLine = StringUtils.split(line, ",");
				if (splittedLine.length > 2 && !splittedLine[0].startsWith("#")) {
					sourceMap.put(splittedLine[0], splittedLine[1]);
				}
			}
		} catch (FileNotFoundException fException) {
		} catch (IOException ioException) {
		}
		return sourceMap;
	}

	protected static Map<String, String> generateMapFromSourceMapFile(InputStream symbolMapInput) {
		Map<String, String> sourceMap = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new InputStreamReader(symbolMapInput))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] splittedLine = StringUtils.split(line, ",");
				if (splittedLine.length > 2 && !splittedLine[0].startsWith("#")) {
					sourceMap.put(splittedLine[0], splittedLine[1]);
				}
			}
		} catch (FileNotFoundException fException) {
		} catch (IOException ioException) {
		}
		return sourceMap;
	}

	protected static void deobfuscateStackTrace(String stackTracePath, Map<String, String> map, String path) {
		File file = new File(stackTracePath);
		List<String> methodCallList = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			while ((line = br.readLine()) != null) {
				String requiredString = StringUtils.substringBetween(line, ".", "(");
				if (requiredString != null && !requiredString.isEmpty()) {
					String key = requiredString;
					String methodName = map.get(key);
					methodCallList.add(methodName == null ? line : methodName);
				} else {
					methodCallList.add(line);
				}
			}
		} catch (FileNotFoundException fException) {
		} catch (IOException ioException) {
		}
		writeToFile(methodCallList, path);
	}

	private static void writeToFile(List<String> methodCallList, String path) {
		File f = new File(path);
		try {
			if (!f.exists()) {
				f.createNewFile();
			}

			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for (String methodName : methodCallList) {
				bw.write(methodName + System.getProperty("line.separator"));
			}
			bw.close();
		} catch (IOException e) {
		}
		System.out.println("Successful deobfuscation to path : " + path);
	}
}
