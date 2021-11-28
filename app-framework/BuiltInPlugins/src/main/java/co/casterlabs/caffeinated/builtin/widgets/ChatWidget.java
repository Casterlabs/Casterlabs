package co.casterlabs.caffeinated.builtin.widgets;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class ChatWidget extends Widget {

    @Override
    public void onInit() {
        // I spend way too long on this shit.
        FastLogger.logStatic(" _____________");
        FastLogger.logStatic("|     Hi!     |");
        FastLogger.logStatic("| My name is: |");
        FastLogger.logStatic("|‾‾‾‾‾‾‾‾‾‾‾‾‾|");
        FastLogger.logStatic("| %-11s |", this.getName());
        FastLogger.logStatic("|_____________|");
    }

}
