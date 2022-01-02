package co.casterlabs.caffeinated.bootstrap.windows;

import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.BOOLByReference;
import com.sun.jna.platform.win32.WinDef.HWND;

import co.casterlabs.caffeinated.bootstrap.NativeSystemProvider;
import co.casterlabs.caffeinated.bootstrap.theming.ThemeableJFrame;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class WindowsLafManager implements NativeSystemProvider.LafManager {

    @Override
    public ThemeableJFrame getFrame0() throws Exception {
        return new WindowsThemeableJFrame();
    }

    private static class WindowsThemeableJFrame extends ThemeableJFrame {
        private static final long serialVersionUID = ThemeableJFrame.serialVersionUID;

        @Override
        protected void setDarkMode0() throws Exception {
            // References:
            // https://docs.microsoft.com/en-us/windows/win32/api/dwmapi/nf-dwmapi-dwmsetwindowattribute
            // https://winscp.net/forum/viewtopic.php?t=30088
            // https://gist.github.com/rossy/ebd83ba8f22339ce25ef68bfc007dfd2
            //
            // This is the code that we're mimicking (in c):
            /*
            DwmSetWindowAttribute(
                    hwnd, 
                    DWMWA_USE_IMMERSIVE_DARK_MODE,
                    &(BOOL) { TRUE }, 
                    sizeof(BOOL)
             );
             */

            HWND hwnd = DWM.getHWND(this);
            BOOLByReference pvAttribute = new BOOLByReference(new BOOL(ThemeableJFrame.isDarkModeEnabled()));

            DWM.INSTANCE.DwmSetWindowAttribute(
                hwnd,
                DWM.DWMWA_USE_IMMERSIVE_DARK_MODE,
                pvAttribute,
                BOOL.SIZE
            );

            DWM.INSTANCE.DwmSetWindowAttribute(
                hwnd,
                DWM.DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1,
                pvAttribute,
                BOOL.SIZE
            );

            FastLogger.logStatic(
                LogLevel.DEBUG,
                "Set IMMERSIVE_DARK_MODE and DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1 to %b.",
                ThemeableJFrame.isDarkModeEnabled()
            );

            // Trick windows into repainting the window.
            this.flashVisibility();
        }

    }

}
