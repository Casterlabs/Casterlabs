#!/bin/bash


# Let's get it started.
cd app-framework/Build/Caffeinated-MacOS

echo "Building for MacOS..."

JRE_DOWNLOAD_URL="https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u312-b07/OpenJDK8U-jre_x64_mac_hotspot_8u312b07.tar.gz"
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
cp -r target/dist/* ../../../dist/macOS/Casterlabs-Caffeinated.app/

# Return us back to the root dir.
cd ..
cd ..
cd ..

echo "Finished building for MacOS."
