package co.edu.unimagdalena.finalbrasiliant;

import org.springframework.boot.SpringApplication;

public class TestFinalBrasilianTApplication {

    public static void main(String[] args) {
        SpringApplication.from(FinalBrasilianTApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
