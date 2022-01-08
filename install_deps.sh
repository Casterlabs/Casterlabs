#!/bin/bash

# Webviewjar Deps
cd app/build/Webview-Webviewjar/lib

mvn install:install-file \
   -Dfile=WebView.jar \
   -DgroupId=org.webview \
   -DartifactId=WebViewJar \
   -Dversion=1.0.0 \
   -Dpackaging=jar \
   -DgeneratePom=true