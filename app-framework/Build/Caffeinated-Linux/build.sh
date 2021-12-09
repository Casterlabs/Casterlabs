#!/bin/bash


# Let's get it started.
cd app-framework/Build/Caffeinated-Linux

echo "Building for Linux..."

JRE_DOWNLOAD_URL="https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u312-b07/OpenJDK8U-jre_x64_linux_hotspot_8u312b07.tar.gz"
MAIN_CLASS="co.casterlabs.caffeinated.bootstrap.Bootstrap"

rm -rf target/dist

if [ ! -f runtime.tar.gz ]; then
    echo "Downloading JRE from ${JRE_DOWNLOAD_URL}."
    wget -O runtime.tar.gz $JRE_DOWNLOAD_URL
fi

java -jar "../packr.jar" \
     --platform linux64 \
     --jdk runtime.tar.gz \
     --executable Casterlabs-Caffeinated \
     --classpath target/jcef-bundle-linux64/Caffeinated-Linux.jar \
     --mainclass $MAIN_CLASS \
     --resources target/jcef-bundle-linux64/cef_bundle \
     --output target/dist

# Copy the files.
cp -r target/dist/* ../../../dist/linux

# Return us back to the root dir.
cd ..
cd ..
cd ..

echo "Finished building for Linux."
