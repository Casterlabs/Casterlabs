#!/bin/bash


# Let's get it started.
mkdir dist/macos/Casterlabs-Caffeinated.app
cd app/Build/Caffeinated-MacOS

echo "Building for MacOS..."

JRE_DOWNLOAD_URL="https://api.adoptium.net/v3/binary/version/jdk-11.0.13%2B8/mac/x64/jre/hotspot/normal/eclipse?project=jdk"
MAIN_CLASS="co.casterlabs.caffeinated.bootstrap.macos.MacOSBootstrap"

rm -rf target/dist

if [ ! -f runtime.tar.gz ]; then
    echo "Downloading JRE from ${JRE_DOWNLOAD_URL}."
    wget -O runtime.tar.gz $JRE_DOWNLOAD_URL
fi

java -jar "../packr.jar" \
     --platform mac \
     --jdk runtime.tar.gz \
     --executable Casterlabs-Caffeinated \
     --icon app_icon.icns \
     --bundle co.casterlabs.caffeinated \
     --classpath target/Caffeinated-MacOS.jar \
     --mainclass $MAIN_CLASS \
     --output target/dist

# Copy the files.
cp -r target/dist/* ../../../dist/macos/Casterlabs-Caffeinated.app

# Return us back to the root dir.
cd ..
cd ..
cd ..

echo "Finished building for MacOS."
