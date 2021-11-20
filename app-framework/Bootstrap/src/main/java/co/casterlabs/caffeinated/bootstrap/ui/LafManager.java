package co.casterlabs.caffeinated.bootstrap.ui;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@SuppressWarnings({
        "unused"
})
public class LafManager {

    public static void setupLAF() {
        try {
            // Doesn't work atm.
//            if (ConsoleUtil.getPlatform() == JavaPlatform.WINDOWS) {
//                setWindow10LAF();
//            } else {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            }
        } catch (Exception e) {
            FastLogger.logException(e);
        }
    }

    private static void setNimbusDarkLAF() throws Exception {
        UIManager.put("control", new Color(128, 128, 128));
        UIManager.put("info", new Color(128, 128, 128));
        UIManager.put("nimbusBase", new Color(18, 30, 49));
        UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
        UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
        UIManager.put("nimbusFocus", new Color(115, 164, 209));
        UIManager.put("nimbusGreen", new Color(176, 179, 50));
        UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
        UIManager.put("nimbusLightBackground", new Color(18, 30, 49));
        UIManager.put("nimbusOrange", new Color(191, 98, 4));
        UIManager.put("nimbusRed", new Color(169, 46, 34));
        UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
        UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
        UIManager.put("text", new Color(230, 230, 230));

        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                FastLogger.logStatic("Set theme to Nimbus.");
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
    }

    private static void setWindow10LAF() throws Exception {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        UIDefaults uiDefaults = UIManager.getDefaults();
        uiDefaults.put("activeCaption", new javax.swing.plaf.ColorUIResource(Color.GRAY));
        uiDefaults.put("activeCaptionText", new javax.swing.plaf.ColorUIResource(Color.WHITE));
        JFrame.setDefaultLookAndFeelDecorated(true);
        FastLogger.logStatic("Set Window10 Darkmode LAF");
    }

}
