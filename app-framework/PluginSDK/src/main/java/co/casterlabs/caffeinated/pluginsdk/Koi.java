package co.casterlabs.caffeinated.pluginsdk;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.user.User;

public class Koi {
    private static List<KoiEvent> chatHistory = new LinkedList<>();
    private static Map<String, List<User>> viewers = new HashMap<>();

}
