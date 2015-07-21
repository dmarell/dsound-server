/*
 * Created by Daniel Marell 13-05-04 2:39 PM
 */
package se.marell.dsoundserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Provides build version.
 *
 * version=${project.version}
 */
public final class BuildInfo {
    private BuildInfo() {
    }

    /**
     * Get application version.
     *
     * @return AppVersion
     */
    public static String getAppVersion() {
        InputStream in = BuildInfo.class.getResourceAsStream("/BuildInfo.properties");
        if (in != null) {
            try {
                Properties props = new Properties();
                props.load(in);
                String version = props.getProperty("AppVersion");
                if (version.contains("SNAPSHOT")) {
                    return "dev-build";
                }
                return version;
            } catch (IOException e) {
                return "Cannot read BuildInfo.properties";
            } finally {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
        }
        throw new IllegalStateException("Missing BuildInfo.properties");
    }
}
