#!/bin/bash


# Let's get it started.
cd app-framework/Build/Caffeinated-Windows

echo "Building for Windows..."

JRE_DOWNLOAD_URL="https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u312-b07/OpenJDK8U-jre_x64_windows_hotspot_8u312b07.zip"
MAIN_CLASS="co.casterlabs.caffeinated.bootstrap.windows.WindowsBootstrap"

rm -rf target/dist

if [ ! -f runtime.zip ]; then
    echo "Downloading JRE from ${JRE_DOWNLOAD_URL}."
    wget -O runtime.zip $JRE_DOWNLOAD_URL
fi

java -jar "../packr.jar" \
     --platform windows64 \
     --jdk runtime.zip \
     --executable Casterlabs-Caffeinated \
     --classpath target/jcef-bundle-win64/Caffeinated-Windows.jar \
     --mainclass $MAIN_CLASS \
     --resources target/jcef-bundle-win64/cef_bundle \
     --output target/dist

# Copy the files.
cp -r target/dist/* ../../../dist/windows

# Return us back to the root dir.
cd ..
cd ..
cd ..

echo "Finished building for Windows."
