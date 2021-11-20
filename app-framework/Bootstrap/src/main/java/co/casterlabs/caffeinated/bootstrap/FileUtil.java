package co.casterlabs.caffeinated.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import co.casterlabs.rakurai.io.IOUtil;

public class FileUtil {

    public static String loadResource(String path) throws IOException {
        InputStream in;

        if (Bootstrap.isDev()) {
            in = new FileInputStream(new File("./src/main/resources/", path));
        } else {
            in = FileUtil.class.getClassLoader().getResourceAsStream(path);
        }

        return IOUtil.readInputStreamString(in, StandardCharsets.UTF_8);
    }

    public static URL loadResourceAsUrl(String path) throws IOException {
        if (Bootstrap.isDev()) {
            return new File("./src/main/resources/", path).toURI().toURL();
        } else {
            return FileUtil.class.getClassLoader().getResource(path);
        }
    }

}
