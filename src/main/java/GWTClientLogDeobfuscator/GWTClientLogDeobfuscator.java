package GWTClientLogDeobfuscator;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GWTClientLogDeobfuscator {
    public static void main(String[] args) {
        System.out.println("Launch GWT Client Log Debobfuscator");

        if(args.length == 3){
            String stackTracePath = args[0];
            String sourceMapPath = args[1];
            String path = args[2];

            Map<String, String> map = generateMapFromSourceMapFile(sourceMapPath);
            deobfuscateStackTrace(stackTracePath, map, path);
        }
        else{
            System.out.println("Missing parameters");
            System.out.println("Usage : ");
            System.out.println("java -jar gwt-client-log-deobfuscator-jar-with-dependencies.jar STACKTRACE_FILEPATH SOURCEMAP_FILEPATH OUTPUT_FILEPATH");
        }
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

    protected static void writeToFile(List<String> methodCallList, String
            path) {
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
    }
}
