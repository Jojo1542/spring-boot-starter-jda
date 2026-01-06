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
- **Component interaction system** for buttons, select menus, and modals
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
    implementation("com.github.jojo1542:spring-boot-starter-jda:1.1.0")
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
    implementation 'com.github.jojo1542:spring-boot-starter-jda:1.1.0'
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
    <version>1.1.0</version>
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
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.jda.sender.CommandSender;
import es.jojo1542.spring.jda.command.BaseCommand;
import es.jojo1542.spring.jda.command.SlashCommand;

@SlashCommand
@Command("ping")
public class PingCommand extends BaseCommand {

    @Command
    public void execute(CommandSender sender) {
        long ping = sender.getEvent().getJDA().getGatewayPing();
        sender.getEvent().reply("Pong! " + ping + "ms").queue();
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

    @Command("ban")
    public void ban(CommandSender sender, User user, String reason) {
        // Ban logic
        sender.getEvent().reply("Banned " + user.getName() + ": " + reason).queue();
    }

    @Command("kick")
    public void kick(CommandSender sender, User user) {
        // Kick logic
        sender.getEvent().reply("Kicked " + user.getName()).queue();
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

    @Command
    public void execute(CommandSender sender) {
        var stats = statsRepository.findByUserId(sender.getUser().getId());
        sender.getEvent().reply("Your stats: " + stats).queue();
    }
}
```

## Component Interactions

Create interactive Discord components (buttons, select menus, modals) with a fluent API.

### Quick Example

```java
@SlashCommand
@Command("demo")
public class DemoCommand extends BaseCommand {

    private final ComponentBuilder components;

    public DemoCommand(ComponentBuilder components) {
        this.components = components;
    }

    @Command("button")
    public void button(CommandSender sender) {
        var button = components.button()
            .success("Click me!")
            .emoji("👋")
            .onClick((event, ctx) -> {
                event.reply("Hello!").setEphemeral(true).queue();
            })
            .expireAfter(Duration.ofMinutes(5))
            .build();

        sender.getEvent()
            .reply("Here's a button:")
            .addActionRow(button)
            .queue();
    }

    @Command("modal")
    public void modal(CommandSender sender) {
        var openBtn = components.button()
            .primary("Open Form")
            .onClick((event, ctx) -> {
                var modal = components.modal()
                    .title("Feedback")
                    .shortInput("subject", "Subject", "Brief description")
                    .paragraphInput("message", "Message", "Your feedback...")
                    .onSubmit((modalEvent, modalCtx) -> {
                        String subject = modalEvent.getValue("subject").getAsString();
                        modalEvent.reply("Received: " + subject).queue();
                    })
                    .build();
                event.replyModal(modal).queue();
            })
            .build();

        sender.getEvent()
            .reply("Click to open form:")
            .addActionRow(openBtn)
            .queue();
    }
}
```

### Features

| Feature | Description |
|---------|-------------|
| **Buttons** | All styles (primary, secondary, success, danger, link) with emoji support |
| **Select Menus** | String select with options, multi-select support |
| **Entity Selects** | User, role, channel, and mentionable selectors |
| **Modals** | Text input forms with short and paragraph fields |
| **Auto-expiration** | Callbacks automatically expire (default: 15 minutes) |
| **Auto-disable** | Buttons automatically disable when callbacks expire |
| **Stateless Handlers** | Annotation-based handlers that survive bot restarts |

### Configuration

```yaml
spring:
  jda:
    components:
      enabled: true
      callback:
        default-ttl: 15m
        max-size: 10000
        disable-on-expire: true
```

> **Full Documentation:** See [docs/COMPONENTS.md](docs/COMPONENTS.md) for complete guide with examples.

---

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
- Component examples (buttons, select menus, modals)
- Pagination example
- Form/modal examples

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
