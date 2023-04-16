package co.casterlabs.mediashareapi;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.sora.Sora;
import co.casterlabs.sora.api.PluginImplementation;
import co.casterlabs.sora.api.SoraPlugin;
import lombok.NonNull;

@PluginImplementation
public class MediaShareApi extends SoraPlugin {

    @Override
    public void onInit(Sora sora) {
        sora.addProvider(this, new RouteMediaShare());
    }

    @Override
    public void onClose() {}

    @Override
    public @Nullable String getVersion() {
        return null;
    }

    @Override
    public @Nullable String getAuthor() {
        return "Casterlabs";
    }

    @Override
    public @NonNull String getName() {
        return "Media Share Api";
    }

    @Override
    public @NonNull String getId() {
        return "co.casterlabs.mediashareapi";
    }

}
