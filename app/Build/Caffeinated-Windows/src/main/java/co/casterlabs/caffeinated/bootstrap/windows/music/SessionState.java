package co.casterlabs.caffeinated.bootstrap.windows.music;

import co.casterlabs.caffeinated.bootstrap.windows.music.types.MediaInfo;
import co.casterlabs.caffeinated.bootstrap.windows.music.types.PlaybackStatus;
import co.casterlabs.caffeinated.bootstrap.windows.music.types.PlaybackType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@Accessors(chain = true)
@RequiredArgsConstructor
public class SessionState {
    private final String sessionId;

    private PlaybackType type;
    private PlaybackStatus status;
    private @ToString.Exclude MediaInfo mediaInfo;

}
