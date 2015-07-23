## Spring REST server playing sound files

Play sound files through a REST interface. Serves a single sound output device.
Caches sound data and refer sound clips by name.

### Configuration
There are two parameters to set:
* The name of the sound player device to use
* The path where to store the sound file cache

Set property dsound-server.soundPlayerDevice to a regex matching any of the devices logged when starting up
the first time. It is possible to get a hint about the name by running aplay -L, but the device names convention
showed in aplay does no correspond with the device name scheme in the Java Sound API, and I have not yet discovered
the scheme for this translation. However it can be manually translated by using some fuzzy logic and trial and error.

Set the property dsound-server.soundFileCacheRoot to a directory the sound file cache should be located. The service
will create a directory dsound-file-cache below this path.

The proper way to set these properties are in the upstart config file: dsound-server.conf

### Release notes
* Version 1.0.0 - 2015-07-22
  * First version.
* Version 1.0.1 - 2015-07-23
  * Added optional request parameter volume

### Backlog
* Interrupt playing sound clip
* Text-to-speach
* Sound streaming
