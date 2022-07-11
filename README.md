### 2THConnector

In ```app/src/main/java/com/example/connector/Connector.kt``` there is a hardcoded IP adress, it is the IP I use localy. When building the apk, keep in mind that this should be a public and static IP, a DNS is a better option if possible. 

This program does not have a main screen, it opens as black screen, it is meant to be installed and left there logging data and pushing to the API, as soon as you open it for the first time it registers a background service in the device and keeps running even if the device is rebooted

It will be turned in a service and not appear as an app, if no user functions is added in the future

### Tested android versions:
- Android 8
- Android 9

### Tested devices:
- Asus zenfone max pro m1

### Meant to be used with https://github.com/wwwxkz/2THPlatform
