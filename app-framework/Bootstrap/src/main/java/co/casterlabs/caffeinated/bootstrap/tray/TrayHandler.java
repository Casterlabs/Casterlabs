package co.casterlabs.caffeinated.bootstrap.tray;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.IOException;

import javax.swing.ImageIcon;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.ui.UIPreferences;
import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.FileUtil;
import co.casterlabs.caffeinated.bootstrap.ui.ApplicationUI;
import lombok.NonNull;

public class TrayHandler {
    private static CheckboxMenuItem showCheckbox;
    private static SystemTray tray;

    private static TrayIcon lastIcon;

    public static void tryCreateTray() {
        if (tray == null) {
            // Check the SystemTray support
            if (!SystemTray.isSupported()) {
                return;
            }

            tray = SystemTray.getSystemTray();
            changeTrayIcon(CaffeinatedApp.getInstance().getUiPreferences().get().getIcon());

            CaffeinatedApp.getInstance().getUiPreferences().addSaveListener((PreferenceFile<UIPreferences> uiPreferences) -> {
                changeTrayIcon(uiPreferences.get().getIcon());
            });

            // Remove the icon on shutdown.
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        tray.remove(lastIcon);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            throw new IllegalStateException("Tray handler is already initialized.");
        }
    }

    public static void updateShowCheckbox(boolean newState) {
        if (showCheckbox != null) {
            showCheckbox.setState(newState);
        }
    }

    private static void changeTrayIcon(String icon) {
        // We recreate the pop up menu because..... idk
        PopupMenu popup = new PopupMenu();

        // Create a popup menu components
        showCheckbox = new CheckboxMenuItem("Show");
        MenuItem itemExit = new MenuItem("Exit");

        showCheckbox.setState(ApplicationUI.isOpen());

        // Add components to popup menu
        popup.add(showCheckbox);
        popup.addSeparator();
        popup.add(itemExit);

        showCheckbox.addItemListener((ItemEvent e) -> {
            if (ApplicationUI.isOpen()) {
                ApplicationUI.closeWindow();
            } else {
                ApplicationUI.showWindow();
            }
        });

        itemExit.addActionListener((ActionEvent e) -> {
            Bootstrap.shutdown();
        });

        // Setup the tray icon.
        TrayIcon trayIcon = new TrayIcon(createImage(String.format("assets/logo/%s.png", icon), "Casterlabs Logo"));

        trayIcon.setImageAutoSize(true);
        trayIcon.setPopupMenu(popup);

        trayIcon.addActionListener((ActionEvent e) -> {
            ApplicationUI.showWindow();
        });

        try {
            // The ole' switch'aroo
            tray.add(trayIcon);
            tray.remove(lastIcon);
            lastIcon = trayIcon;
        } catch (AWTException e) {}
    }

    public static void notify(@NonNull String message, @NonNull MessageType type) {
        lastIcon.displayMessage("Casterlabs Caffeinated", message, type);
    }

    public static void destroy() {
        tray.remove(lastIcon);
    }

    private static Image createImage(String path, String description) {
        try {
            return new ImageIcon(FileUtil.loadResourceAsUrl(path), description).getImage();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
