package co.casterlabs.caffeinated.pluginsdk;

import java.util.List;
import java.util.Map;

import co.casterlabs.caffeinated.util.Reflective;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.user.User;
import lombok.Getter;

public class Koi {
    private static @Reflective @Getter List<KoiEvent> chatHistory;
    private static @Reflective @Getter Map<String, List<User>> viewers;

}
