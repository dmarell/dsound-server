package se.marell.dsoundserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import se.marell.dvesta.system.LogbackLoggerInitializer;
import se.marell.dvesta.system.RunEnvironment;

@SpringBootApplication
public class Application {
    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        if (System.getProperty("spring.profiles.active") == null) {
            app.setAdditionalProfiles("local");
        }
        app.addListeners(new ApplicationListener<ApplicationEnvironmentPreparedEvent>() {
            @Override
            public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
                LogbackLoggerInitializer.init(event.getEnvironment());
                logger.info("AppVersion: " + BuildInfo.getAppVersion());
                logger.info("RunEnvironment: " + RunEnvironment.getCurrentEnvironment(event.getEnvironment()));
            }
        });
        app.run(args);
    }
}
