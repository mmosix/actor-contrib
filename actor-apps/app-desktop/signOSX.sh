#!/bin/bash

# Name of your app.
APP="Actor"
# The path of you app to sign.
APP_PATH="build/Actor-darwin-x64/$APP.app"
# The path to the location you want to put the signed package.
RESULT_PATH="build/Actor-darwin-x64/$APP.pkg"
# The name of certificates you requested.
APP_KEY="3rd Party Mac Developer Application: Andrey Kuznetsov (HVJR44Y5B6)"
INSTALLER_KEY="3rd Party Mac Developer Installer: Andrey Kuznetsov (HVJR44Y5B6)"

FRAMEWORKS_PATH="$APP_PATH/Contents/Frameworks"

codesign --deep -fs "$APP_KEY" --entitlements mac/child.plist "$FRAMEWORKS_PATH/Electron Framework.framework/Libraries/libnode.dylib"
codesign --deep -fs "$APP_KEY" --entitlements mac/child.plist "$FRAMEWORKS_PATH/Electron Framework.framework/Electron Framework"
codesign --deep -fs "$APP_KEY" --entitlements mac/child.plist "$FRAMEWORKS_PATH/Electron Framework.framework/"
codesign --deep -fs "$APP_KEY" --entitlements mac/child.plist "$FRAMEWORKS_PATH/$APP Helper.app/"
codesign --deep -fs "$APP_KEY" --entitlements mac/child.plist "$FRAMEWORKS_PATH/$APP Helper EH.app/"
codesign --deep -fs "$APP_KEY" --entitlements mac/child.plist "$FRAMEWORKS_PATH/$APP Helper NP.app/"
codesign  -fs "$APP_KEY" --entitlements mac/parent.plist "$APP_PATH"
productbuild --component "$APP_PATH" /Applications --sign "$INSTALLER_KEY" "$APP_PATH"