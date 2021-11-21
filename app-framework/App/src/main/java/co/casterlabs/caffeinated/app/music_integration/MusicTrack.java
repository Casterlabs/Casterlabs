package co.casterlabs.caffeinated.app.music_integration;

import java.util.List;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@NonNull
@ToString
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class MusicTrack {
    private String title;
    private List<String> artists;
    private String album;
    private String albumArtUrl;
    private String link;

}
