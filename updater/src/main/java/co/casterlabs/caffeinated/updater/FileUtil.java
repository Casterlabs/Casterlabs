package co.casterlabs.caffeinated.updater;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import co.casterlabs.rakurai.io.IOUtil;

public class FileUtil {

    public static String loadResource(String path) throws IOException {
        InputStream in = FileUtil.class.getClassLoader().getResourceAsStream(path);

        return IOUtil.readInputStreamString(in, StandardCharsets.UTF_8);
    }

    public static String loadResourceFromBuildProject(String path, String project) throws IOException {
        InputStream in = FileUtil.class.getClassLoader().getResourceAsStream(path);

        return IOUtil.readInputStreamString(in, StandardCharsets.UTF_8);
    }

    public static byte[] loadResourceBytes(String path) throws IOException {
        InputStream in = FileUtil.class.getClassLoader().getResourceAsStream(path);

        return IOUtil.readInputStreamBytes(in);
    }

    public static URL loadResourceAsUrl(String path) throws IOException {
        return FileUtil.class.getClassLoader().getResource(path);
    }

}
