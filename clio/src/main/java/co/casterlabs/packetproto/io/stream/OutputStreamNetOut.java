package co.casterlabs.packetproto.io.stream;

import java.io.IOException;
import java.io.OutputStream;

import co.casterlabs.packetproto.io.NetOut;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputStreamNetOut implements NetOut {
    private OutputStream stream;

    @Override
    public NetOut writeByte(byte b) throws IOException {
        this.stream.write(b);
        return this;
    }

    @Override
    public NetOut writeBytes(byte[] b) throws IOException {
        this.stream.write(b);
        return this;
    }

}
