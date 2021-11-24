#!/bin/bash

# Build UI
cd ./ui
npm i
npm run build

# Copy output to app-framework/Bootstrap/src/main/resources/app
cd ..
cp -r ./ui/build ./app-framework/Bootstrap/src/main/resources/app

# Build the app jar
cd app-framework
mvn install

# Copy framework .jar to the dist/ folder.
cd ..
mkdir -p ./dist
cp ./app-framework/Bootstrap/target/caffeinated.jar ./dist/caffeinated.jar

# Generate executables... (?)
echo "TODO: Autogenerate executables"