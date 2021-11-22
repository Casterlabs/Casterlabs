package co.casterlabs.caffeinated.app;

import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;

public interface AppBridge {

    public JsonObject getQueryData();

    public void emit(String type, JsonElement data);

    public void eval(String script);

}
