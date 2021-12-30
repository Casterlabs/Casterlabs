package co.casterlabs.caffeinated.bootstrap.windows;

import javax.swing.JFrame;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.WinDef.HWND;

public interface DWM extends Library {
    public static final int DWMWA_USE_IMMERSIVE_DARK_MODE_BEFORE_20H1 = 19;
    public static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;

    public static final DWM INSTANCE = Native.load("dwmapi", DWM.class);

    int DwmSetWindowAttribute(HWND hwnd, int dwAttribute, PointerType pvAttribute, int cbAttribute);

    public static HWND getHWND(JFrame frame) {
        return new HWND(Native.getComponentPointer(frame));
    }

}
