package co.casterlabs.caffeinated.bootstrap.theming;

import co.casterlabs.caffeinated.app.theming.Theme;
import co.casterlabs.caffeinated.app.theming.ThemeManager;

@SuppressWarnings("deprecation")
public class ThemeHandleImpl implements ThemeManager.ThemeManagerHandle {

    @Override
    public void applyTheme(Theme theme) {
        ThemeableJFrame.setDarkMode(theme.isDark());
    }

}
