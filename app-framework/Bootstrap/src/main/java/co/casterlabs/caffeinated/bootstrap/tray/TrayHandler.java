package co.casterlabs.caffeinated.bootstrap.tray;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.ImageIcon;

import co.casterlabs.caffeinated.app.preferences.PreferenceFile;
import co.casterlabs.caffeinated.app.ui.UIPreferences;
import co.casterlabs.caffeinated.bootstrap.Bootstrap;
import co.casterlabs.caffeinated.bootstrap.FileUtil;
import co.casterlabs.caffeinated.bootstrap.ui.ApplicationUI;

public class TrayHandler {

    // TODO integrate with the rest of the app.
    @SuppressWarnings("unused")
    private static void tryCreateTray() {
        // Check the SystemTray support
        if (!SystemTray.isSupported()) {
            return;
        }

        PopupMenu popup = new PopupMenu();
        SystemTray tray = SystemTray.getSystemTray();
        changeTrayIcon(Bootstrap.getApp().getUiPreferences().get().getIcon(), popup, tray);

        // Create a popup menu components
        MenuItem itemShow = new MenuItem("Show");
        MenuItem itemExit = new MenuItem("Exit");

        // Add components to popup menu
        popup.add(itemShow);
        popup.addSeparator();
        popup.add(itemExit);

        Bootstrap.getApp().getUiPreferences().addSaveListener((PreferenceFile<UIPreferences> uiPreferences) -> {
            changeTrayIcon(uiPreferences.get().getIcon(), popup, tray);
        });

        itemShow.addActionListener((ActionEvent e) -> {
            ApplicationUI.show();
        });

        itemExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private static void changeTrayIcon(String icon, PopupMenu popup, SystemTray tray) {
        TrayIcon trayIcon = new TrayIcon(createImage(String.format("assets/logo/%s.png", icon), "Casterlabs Logo"));

        trayIcon.setImageAutoSize(true);
        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {}

        trayIcon.addActionListener((ActionEvent e) -> {
            ApplicationUI.show();
        });

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
