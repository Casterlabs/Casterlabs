package co.casterlabs.caffeinated.localserver;

import java.io.Closeable;
import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.localserver.handlers.RouteLocalServer;
import co.casterlabs.caffeinated.localserver.handlers.RoutePluginApi;
import co.casterlabs.caffeinated.localserver.handlers.RouteWidgetApi;
import co.casterlabs.rakurai.io.http.server.HttpServerImplementation;
import co.casterlabs.sora.Sora;
import co.casterlabs.sora.SoraFramework;
import co.casterlabs.sora.SoraLauncher;
import co.casterlabs.sora.api.SoraPlugin;
import co.casterlabs.sora.api.http.HttpProvider;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class LocalServer implements Closeable, HttpProvider {
    private SoraFramework framework;

    @SneakyThrows
    public LocalServer(int port) {
        this.framework = new SoraLauncher()
            .setPort(port)
            .setImplementation(HttpServerImplementation.UNDERTOW)
            .buildWithoutPluginLoader();

        this.framework
            .getSora()
            .register(new SoraPlugin() {

                @Override
                public void onInit(Sora sora) {
                    sora.addHttpProvider(this, new RouteLocalServer());
                    sora.addHttpProvider(this, new RoutePluginApi());
                    sora.addHttpProvider(this, new RouteWidgetApi());
                }

                @Override
                public void onClose() {}

                @Override
                public @Nullable String getVersion() {
                    return CaffeinatedApp.getInstance().getBuildInfo().getVersionString();
                }

                @Override
                public @Nullable String getAuthor() {
                    return "Casterlabs";
                }

                @Override
                public @NonNull String getName() {
                    return "Caffeinated Conductor (LocalServer)";
                }

                @Override
                public @NonNull String getId() {
                    return "co.casterlabs.caffeinated.conductor";
                }
            });
    }

    /* ---------------- */
    /* IO Related       */
    /* ---------------- */

    public void start() throws IOException {
        this.framework.getServer().start();
        FastLogger.logStatic("Started!");
    }

    public boolean isAlive() {
        return this.framework.getServer().isAlive();
    }

    @Override
    public void close() throws IOException {
        this.framework.getServer().stop();
        FastLogger.logStatic("Stopped!");
    }

}
