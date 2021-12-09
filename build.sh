#!/bin/bash


# (Optional) Compile everything
if [[ $@ != *"nocompile"* ]]; then
    # Delete old ui build stuff.
    rm -rf ui/build
    rm -rf app-framework/Bootstrap/src/main/resources/app
    
    # Build UI
    cd ui
    npm i
    npm run build
    
    # Copy output to app-framework/Bootstrap/src/main/resources/app
    cd ..
    cp -r ui/build app-framework/Bootstrap/src/main/resources/app
    
    # Compile the maven project
	cd app-framework
    mvn clean package
    cd ..
fi

# Reset/clear the dist folder
rm -rf dist/*
mkdir -p dist
mkdir dist/windows
mkdir dist/linux
mkdir dist/macos

if [[ $@ != *"nodist"* ]]; then
    echo ""
    echo "Completing packaging of application."
    echo ""
    
    sh app-framework/Build/Caffeinated-Windows/build.sh
    echo ""
    
    sh app-framework/Build/Caffeinated-MacOS/build.sh
    echo ""

fi
