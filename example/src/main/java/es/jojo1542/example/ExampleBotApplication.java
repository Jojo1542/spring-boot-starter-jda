package es.jojo1542.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Example Discord bot application using spring-boot-starter-jda.
 *
 * <p>This application demonstrates how to create a Discord bot with:
 * <ul>
 *   <li>Automatic JDA configuration via Spring Boot properties</li>
 *   <li>Event listeners using {@code @JdaListener}</li>
 *   <li>Slash commands using {@code @SlashCommand}</li>
 *   <li>Environment variables loaded from .env file</li>
 * </ul>
 *
 * @author jojo1542
 */
@SpringBootApplication
public class ExampleBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleBotApplication.class, args);
    }
}
