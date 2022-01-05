#!/bin/bash


# Let's get it started.
cd app/Build/Caffeinated-Linux

echo "Building for Linux..."

JRE_DOWNLOAD_URL="https://api.adoptium.net/v3/binary/version/jdk-11.0.13%2B8/linux/x64/jre/hotspot/normal/eclipse?project=jdk"
MAIN_CLASS="co.casterlabs.caffeinated.bootstrap.linux.LinuxBootstrap"

rm -rf target/dist

if [ ! -f runtime.tar.gz ]; then
    echo "Downloading JRE from ${JRE_DOWNLOAD_URL}."
    wget -O runtime.tar.gz $JRE_DOWNLOAD_URL
fi

java -jar "../packr.jar" \
     --platform linux64 \
     --jdk runtime.tar.gz \
     --executable Casterlabs-Caffeinated \
     --classpath target/Caffeinated-Linux.jar \
     --mainclass $MAIN_CLASS \
     --output target/dist

# Copy the files.
cp -r target/dist/* ../../../dist/linux

# Return us back to the root dir.
cd ..
cd ..
cd ..

echo "Finished building for Linux."
