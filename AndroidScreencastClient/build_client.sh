#!/bin/bash

export PATH=$PATH:/home/julian/bin/adt/adt-bundle-linux-x86_64-20130729/sdk/build-tools/android-4.3
dx --dex --output=./classes.dex ./MyInjectEventApp.jar
aapt add MyInjectEventApp.jar classes.dex
rm classes.dex
mv MyInjectEventApp.jar ../AndroidScreencast
