package se.marell.dsoundserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.marell.dcommons.sound.AudioException;
import se.marell.dcommons.sound.SoundClip;
import se.marell.dcommons.sound.SoundPlayerDevice;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
public class PlayController {
    private static final String CACHE_DIRECTORY_NAME = "dsound-server-cache";
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String soundFileCacheRoot;
    private String soundFileDirectory;
    private SoundPlayerDevice soundPlayerDevice;

    @Autowired
    private Environment environment;

    @PostConstruct
    private void init() throws IOException {
        soundFileCacheRoot = environment.getProperty("dsound-server.soundFileCacheRoot", File.separator);
        if (!Paths.get(soundFileCacheRoot).toFile().exists()) {
            throw new IOException("No such directory " + soundFileCacheRoot);
        }

        if (soundFileCacheRoot.equals(File.separator)) {
            soundFileDirectory = soundFileCacheRoot + CACHE_DIRECTORY_NAME;
        } else {
            soundFileDirectory = soundFileCacheRoot + File.separator + CACHE_DIRECTORY_NAME;
        }
        String deviceNamePattern = environment.getProperty("dsound-server.soundPlayerDevice");
        if (deviceNamePattern == null) {
            deviceNamePattern = selectSoundPlayerDevice();
            if (deviceNamePattern == null) {
                throw new IOException("No sound player device available");
            }
        }

        soundPlayerDevice = SoundPlayerDevice.createSoundPlayerDevice(deviceNamePattern);
        if (soundPlayerDevice != null) {
            log.info("Using sound player device: " + deviceNamePattern);
        } else {
            log.info("Could not find specified sound playback device matching pattern \"" +
                    deviceNamePattern + "\". Using default");
        }

        Path dir = Paths.get(soundFileDirectory);
        if (!dir.toFile().exists()) {
            if (!dir.toFile().mkdir()) {
                throw new IOException("Failed to create directory " + soundFileDirectory);
            }
        }
    }

    private String selectSoundPlayerDevice() {
        String selectedDeviceName = null;
        StringBuilder sb = new StringBuilder();
        for (String deviceName : SoundPlayerDevice.getPlayerDeviceNames()) {
            if (selectedDeviceName == null) {
                selectedDeviceName = deviceName; // Pick 1st
            }
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(deviceName);
        }
        log.info("Available sound player devices:\n" + sb.toString());
        return selectedDeviceName;
    }

    @RequestMapping("/version")
    public String getAppVersion() {
        return BuildInfo.getAppVersion();
    }

    @RequestMapping(value = "/sound-clips/{soundClipName:.+}", method = RequestMethod.GET)
    public ResponseEntity<Void> play(@PathVariable(value = "soundClipName") String soundClipName) {
        Path soundFilePath = Paths.get(soundFileDirectory, soundClipName);
        if (soundPlayerDevice == null) {
            log.error("No sound player device available");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (!soundFilePath.toFile().canRead()) {
            log.info("Cannot find soundClip " + soundClipName + " in cache. Supply soundData in request body.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        SoundClip player = new SoundClip(soundFilePath.toFile(),
                () -> log.trace("Ready playing clip " + soundClipName + " on device " + soundPlayerDevice));
        try {
            log.info("Playing clip " + soundClipName + " on device " + soundPlayerDevice);
            player.play(soundPlayerDevice);
        } catch (AudioException e) {
            log.error("Failed to play file " + soundClipName + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/sound-clips/{soundClipName:.+}", method = RequestMethod.POST)
    public ResponseEntity<Void> play(@PathVariable(value = "soundClipName") String soundClipName,
                                     @RequestBody byte[] soundData) throws IOException {
        log.info("Saving data for clip " + soundClipName);
        Files.write(Paths.get(soundFileDirectory, soundClipName), soundData);
        return play(soundClipName);
    }

    /**
     * @return List of soundClipName
     */
    @RequestMapping(value = "/sound-clips", method = RequestMethod.GET)
    public List<String> getSoundClips() {
        List<String> result = new ArrayList<>();
        try {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(soundFileDirectory))) {
                for (Path entry : stream) {
                    result.add(entry.getFileName().toFile().getName());
                }
            }
        } catch (IOException ignore) {
            log.error("Cannot read directory " + soundFileDirectory);
        }
        return result;
    }

    @RequestMapping(value = "/cache", method = RequestMethod.DELETE)
    public ResponseEntity<Void> clearSoundCache() throws IOException {
        try {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(soundFileDirectory))) {
                for (Path entry : stream) {
                    File file = entry.toFile();
                    if (file.isFile() && file.canWrite()) {
                        if (!file.delete()) {
                            log.error("Failed to delete file '" + file + "' in sound file directory: " + soundFileDirectory);
                            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to delete files in sound file directory: " + soundFileDirectory + "," + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
