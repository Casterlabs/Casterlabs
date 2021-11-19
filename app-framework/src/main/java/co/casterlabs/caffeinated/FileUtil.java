package co.casterlabs.caffeinated;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import co.casterlabs.rakurai.io.IOUtil;
import lombok.Cleanup;

public class FileUtil {

    public static String loadResource(String path) throws IOException {
        @Cleanup
        InputStream in = FileUtil.class.getClassLoader().getResourceAsStream(path);

        return IOUtil.readInputStreamString(in, StandardCharsets.UTF_8);
    }

}
