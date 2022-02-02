package co.casterlabs.caffeinated.webview;

import co.casterlabs.caffeinated.util.Producer;

public interface WebviewFactory extends Producer<Webview> {

    public boolean useNuclearOption();

}
