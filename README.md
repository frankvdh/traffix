# Traffix for Android

TraffiX is an app that listens on the local WiFi network for GDL90 data and displays this on a map, as well as retransmitting the data in FLARM format on a UDP socket.

A major use case for TraffiX is to receive GPS and traffic data from a SkyEcho2 Electronic Conspicuity device and relay the data to XCSoar running on the same phone or tablet.
XCSoar is unable to directly receive GDL90 data but most of the functionality of the data can be replicated by converting to FLARM format.

Development
-----------
This fork of Traffix expects the AsciiDoc plugin to be installed
  * Settings/Plugins
  * Search for AsciiDoc
  * Install
  * Editing the about.adoc requires you to manually generate the HTML from the AsciiDoc editor tab

* Mapbox
Traffix for Android depends on Mapbox's "Maps SDK for Android". 
* Create an account at https://account.mapbox.com/ 
* Create a token at https://account.mapbox.com/access-tokens/create with Downloads:Read scope
* Add the token to your gradle.properties
  * MAPBOX_DOWNLOADS_TOKEN=[the token base-64 encoded string without quotes or square brackets......................]

* Other Gradle settings
Traffix also needs the following items to be defined in gradle.properties
  * I don't know what they're used for
  * TODO

    PLAY_MANAGED_STORE_FILE=file
    RELEASE_STORE_PASSWORD=store.password
    PLAY_MANAGED_KEY_ALIAS=alias
    RELEASE_KEY_PASSWORD=release.password
