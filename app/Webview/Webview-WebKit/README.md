# Webview-WebKit

This project uses Eclipse' Standard Widget Toolkit (SWT) to create webviews. (Which internally uses WebKit on Linux and macOS)  
This has the side effect of requiring us to use SWT ONLY for the whole UI, any AWT-based code will not work.  
It also needs the main thread on macOS, so that's why it blocks the MainThread helper and delegates tasks seemingly at it's own will.
