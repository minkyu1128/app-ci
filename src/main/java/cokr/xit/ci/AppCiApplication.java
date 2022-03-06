package cokr.xit.ci;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@ComponentScan(basePackages = "cokr.xit")
@SpringBootApplication
public class AppCiApplication {

    public static void main(String[] args) {
//        SpringApplication.run(AppCiApplication.class, args);

        Long begin = System.currentTimeMillis();
        log.info("CI Application load start...");

        SpringApplication application = new SpringApplication(AppCiApplication.class);
        application.addListeners(new ApplicationPidFileWriter());	//PID(Process ID 작성)
        application.run(args);

        log.info("=========================================================================================");
        log.info("========== CI Application load Complete :: active profiles - {} ==========", System.getProperty("spring.profiles.active"));
        log.info("=========================================================================================");

        Long end = System.currentTimeMillis();
        System.out.println("====================================================");
        System.out.println("기동 소요시간: "+ (end-begin) +"ms");
        System.out.println("====================================================");


    }

}
