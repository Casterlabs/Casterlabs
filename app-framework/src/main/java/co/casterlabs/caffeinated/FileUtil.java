package co.casterlabs.caffeinated;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.rakurai.io.IOUtil;

public class FileUtil {

    public static String loadResource(String path) throws IOException {
        InputStream in;

        if (isInJarFile()) {
            in = FileUtil.class.getClassLoader().getResourceAsStream(path);
        } else {
            in = new FileInputStream(new File("./src/main/", path));
        }

        return IOUtil.readInputStreamString(in, StandardCharsets.UTF_8);
    }

    public static boolean isInJarFile() {
        return false; // Figure this out.
    }

}
