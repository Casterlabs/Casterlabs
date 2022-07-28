package co.casterlabs.packetproto;

import java.io.IOException;

import co.casterlabs.packetproto.io.NetCalc;
import co.casterlabs.packetproto.io.NetIn;
import co.casterlabs.packetproto.io.NetOut;
import lombok.NonNull;

public interface Packet {

    public void calc(@NonNull NetCalc calc);

    public void serialize(@NonNull NetOut out) throws IOException;

    public void deserialize(@NonNull NetIn in) throws IOException;

}
