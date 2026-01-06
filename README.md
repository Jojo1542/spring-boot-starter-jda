# Spring Boot Starter JDA

[![](https://jitpack.io/v/jojo1542/spring-boot-starter-jda.svg)](https://jitpack.io/#jojo1542/spring-boot-starter-jda)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-21+-green.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0+-green.svg)](https://spring.io/projects/spring-boot)

A Spring Boot starter for [JDA (Java Discord API)](https://github.com/discord-jda/JDA) with built-in support for [Triumph CMDs](https://github.com/TriumphTeam/triumph-cmds) slash commands.

## Features

- **Auto-configuration** for JDA with Spring Boot
- **Declarative configuration** via `application.yml`
- **Automatic listener registration** with `@JdaListener`
- **Slash command support** with `@SlashCommand` and Triumph CMDs
- **Customizable** via `JdaBuilderCustomizer` and `SlashCommandManagerCustomizer`
- **Dependency injection** in all components

## Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.triumphteam.dev/snapshots/")
}

dependencies {
    implementation("com.github.jojo1542:spring-boot-starter-jda:1.0.0")
}
```

### Gradle (Groovy)

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
    maven { url 'https://repo.triumphteam.dev/snapshots/' }
}

dependencies {
    implementation 'com.github.jojo1542:spring-boot-starter-jda:1.0.0'
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
    <repository>
        <id>triumph-repo</id>
        <url>https://repo.triumphteam.dev/snapshots/</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.jojo1542</groupId>
    <artifactId>spring-boot-starter-jda</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### 1. Configure your bot token

Create an `application.yml` file:

```yaml
spring:
  jda:
    token: ${DISCORD_TOKEN}
    status: ONLINE
    activity:
      type: PLAYING
      text: "with Spring Boot"
```

> **Tip:** Use [spring-dotenv](https://github.com/paulschwarz/spring-dotenv) to load the token from a `.env` file.

### 2. Create a listener

```java
import es.jojo1542.spring.jda.JdaListener;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@JdaListener
public class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Bot is ready! Logged in as " + event.getJDA().getSelfUser().getName());
    }
}
```

### 3. Create a slash command

```java
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Default;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.slash.sender.SlashSender;
import es.jojo1542.spring.jda.command.SlashCommand;

@SlashCommand
@Command("ping")
public class PingCommand extends BaseCommand {

    @Default
    public void execute(SlashSender sender) {
        long ping = sender.getEvent().getJDA().getGatewayPing();
        sender.reply("Pong! 🏓 " + ping + "ms").queue();
    }
}
```

### 4. Run your application

```java
@SpringBootApplication
public class MyBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyBotApplication.class, args);
    }
}
```

## Configuration

### All Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.jda.token` | `String` | - | Discord bot token **(required)** |
| `spring.jda.enabled` | `Boolean` | `true` | Enable/disable JDA auto-configuration |
| `spring.jda.await-ready` | `Boolean` | `true` | Wait for JDA to be ready before completing startup |
| `spring.jda.status` | `OnlineStatus` | `ONLINE` | Bot online status |
| `spring.jda.activity.type` | `ActivityType` | `PLAYING` | Activity type |
| `spring.jda.activity.text` | `String` | - | Activity text |
| `spring.jda.activity.url` | `String` | - | Streaming URL (only for `STREAMING` type) |
| `spring.jda.intents` | `Set<GatewayIntent>` | See below | Gateway intents to enable |
| `spring.jda.cache-flags` | `Set<CacheFlag>` | - | Cache flags to enable |
| `spring.jda.disabled-cache-flags` | `Set<CacheFlag>` | - | Cache flags to disable |
| `spring.jda.commands.enabled` | `Boolean` | `true` | Enable slash command auto-configuration |

### Default Intents

```yaml
spring:
  jda:
    intents:
      - GUILD_MESSAGES
      - GUILD_MEMBERS
      - MESSAGE_CONTENT
      - DIRECT_MESSAGES
```

### Activity Types

- `PLAYING` - "Playing [text]"
- `STREAMING` - "Streaming [text]" (requires URL)
- `LISTENING` - "Listening to [text]"
- `WATCHING` - "Watching [text]"
- `COMPETING` - "Competing in [text]"
- `CUSTOM` - Custom status message

### Online Status

- `ONLINE` - Green dot
- `IDLE` - Yellow dot
- `DO_NOT_DISTURB` - Red dot
- `INVISIBLE` - Appears offline

## Slash Commands

### Subcommands

```java
@SlashCommand
@Command("admin")
public class AdminCommand extends BaseCommand {

    @Default
    public void help(SlashSender sender) {
        sender.reply("Admin commands: /admin ban, /admin kick").queue();
    }

    @SubCommand("ban")
    public void ban(SlashSender sender, @Argument User user, @Argument String reason) {
        // Ban logic
        sender.reply("Banned " + user.getName() + ": " + reason).queue();
    }

    @SubCommand("kick")
    public void kick(SlashSender sender, @Argument User user) {
        // Kick logic
        sender.reply("Kicked " + user.getName()).queue();
    }
}
```

### Dependency Injection in Commands

```java
@SlashCommand
@Command("stats")
public class StatsCommand extends BaseCommand {

    private final UserService userService;
    private final StatsRepository statsRepository;

    public StatsCommand(UserService userService, StatsRepository statsRepository) {
        this.userService = userService;
        this.statsRepository = statsRepository;
    }

    @Default
    public void execute(SlashSender sender) {
        var stats = statsRepository.findByUserId(sender.getEvent().getUser().getId());
        sender.reply("Your stats: " + stats).queue();
    }
}
```

## Advanced Customization

### Customize JDABuilder

```java
@Bean
public JdaBuilderCustomizer jdaCustomizer() {
    return builder -> builder
        .enableIntents(GatewayIntent.GUILD_PRESENCES)
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .setChunkingFilter(ChunkingFilter.ALL);
}
```

### Customize SlashCommandManager

```java
@Bean
public SlashCommandManagerCustomizer commandCustomizer() {
    return manager -> {
        // Register custom argument types
        manager.registerArgument(MyType.class, (sender, arg) -> MyType.parse(arg));

        // Register suggestions
        manager.registerSuggestion(MyType.class, (sender, context) ->
            List.of("option1", "option2", "option3")
        );
    };
}
```

### Custom JDA Bean

If you need full control, define your own JDA bean:

```java
@Bean
public JDA jda() throws InterruptedException {
    return JDABuilder.createDefault("token")
        .enableIntents(GatewayIntent.ALL_INTENTS)
        .build()
        .awaitReady();
}
```

## Using with spring-dotenv

### 1. Add dependency

```kotlin
implementation("me.paulschwarz:spring-dotenv:4.0.0")
```

### 2. Create `.env` file

```env
DISCORD_TOKEN=your_bot_token_here
```

### 3. Reference in configuration

```yaml
spring:
  jda:
    token: ${DISCORD_TOKEN}
```

## Example Project

See the [example](example/) directory for a complete working bot with:

- Ready listener
- Message listener
- Ping command
- Info command with subcommands

## Requirements

- Java 21+
- Spring Boot 4.0+
- Gradle 8.14+ (for building)

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Author

**José Antonio Ponce Piñero** ([@jojo1542](https://github.com/jojo1542))
