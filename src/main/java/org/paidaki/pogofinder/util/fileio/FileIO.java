package org.paidaki.pogofinder.util.fileio;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileIO {

    public static final String DATA_JSON = new File(FileIO.class.getProtectionDomain().getCodeSource()
            .getLocation().getFile()).getParent() + File.separator + "data.json";
    public static final String ACCOUNTS_JSON = new File(FileIO.class.getProtectionDomain().getCodeSource()
            .getLocation().getFile()).getParent() + File.separator + "accounts.json";

    private FileIO() {
        super();
    }

    public static String readInputStream(InputStream input) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String line;
        StringBuilder strB = new StringBuilder();

        while ((line = br.readLine()) != null) {
            strB.append(line).append("\n");
        }
        br.close();

        return String.valueOf(strB);
    }

    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));

        return new String(encoded, encoding);
    }

    public static String readFile(String path) throws IOException {
        return readFile(path, StandardCharsets.UTF_8);
    }
}
