package co.casterlabs.caffeinated.builtin.widgets;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.SneakyThrows;

public class KeyboardWidget extends Widget implements NativeKeyListener {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.keyboard_widget")
        .withIcon("command")
        .withCategory(WidgetDetailsCategory.OTHER)
        .withFriendlyName("Keyboard Widget");

    private static final WidgetSettingsLayout LAYOUT = new WidgetSettingsLayout()
        .addSection(
            new WidgetSettingsSection("keyboard_settings", "Keyboard Settings")
                .addItem(WidgetSettingsItem.asCheckbox("enabled", "Enabled", true))
                .addItem(WidgetSettingsItem.asCheckbox("hide_inactive", "Hide Inactive Keys", false))
        );

    @Override
    public void onInit() {
        this.setSettingsLayout(LAYOUT);

        GlobalScreen.addNativeKeyListener(this);
    }

    @Override
    protected void onDestroy() {
        GlobalScreen.removeNativeKeyListener(this);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (this.isEnabled()) {
            for (WidgetInstance instance : this.getWidgetInstances()) {
                try {
                    int keyCode = e.getKeyCode();

                    instance.emit("keyPressed", JsonObject.singleton("keyCode", keyCode));
                } catch (IOException ignored) {}
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        for (WidgetInstance instance : this.getWidgetInstances()) {
            try {
                int keyCode = e.getKeyCode();

                instance.emit("keyReleased", JsonObject.singleton("keyCode", keyCode));
            } catch (IOException ignored) {}
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}

    public boolean isEnabled() {
        return this.getSettings().getBoolean("keyboard_settings.enabled");
    }

    @SneakyThrows
    @Override
    public @Nullable String getWidgetHtml() {
        return CaffeinatedDefaultPlugin.resolveResource("/keyboard.html");
    }

}
