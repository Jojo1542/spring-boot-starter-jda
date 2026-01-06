# Slash Commands

This starter integrates [Triumph CMDs](https://github.com/TriumphTeam/triumph-cmds) for Discord slash command support with full Spring dependency injection.

## Table of Contents

- [Quick Start](#quick-start)
- [Command Structure](#command-structure)
- [Subcommands](#subcommands)
- [Arguments](#arguments)
- [Dependency Injection](#dependency-injection)
- [Customization](#customization)

---

## Quick Start

### 1. Create a Command

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

The command is automatically registered when your application starts.

---

## Command Structure

### Basic Command

```java
@SlashCommand                    // Marks as Spring-managed slash command
@Command("commandname")          // Discord command name
public class MyCommand extends BaseCommand {

    @Command                     // Default execution (no subcommand)
    public void execute(CommandSender sender) {
        sender.getEvent().reply("Hello!").queue();
    }
}
```

### CommandSender

The `CommandSender` provides access to:

```java
@Command
public void execute(CommandSender sender) {
    // Get the slash command event
    SlashCommandInteractionEvent event = sender.getEvent();

    // Get the user
    User user = sender.getUser();

    // Get the member (if in guild)
    Member member = sender.getMember();

    // Get the guild
    Guild guild = sender.getGuild();

    // Reply methods
    sender.getEvent().reply("Message").queue();
    sender.getEvent().replyEmbeds(embed).queue();
    sender.getEvent().deferReply().queue();
}
```

---

## Subcommands

Use nested `@Command` annotations for subcommands:

```java
@SlashCommand
@Command("info")
public class InfoCommand extends BaseCommand {

    // /info bot
    @Command("bot")
    public void botInfo(CommandSender sender) {
        JDA jda = sender.getEvent().getJDA();
        sender.getEvent().reply("Bot: " + jda.getSelfUser().getName()).queue();
    }

    // /info server
    @Command("server")
    public void serverInfo(CommandSender sender) {
        Guild guild = sender.getGuild();
        if (guild == null) {
            sender.getEvent().reply("Use in a server!").setEphemeral(true).queue();
            return;
        }
        sender.getEvent().reply("Server: " + guild.getName()).queue();
    }

    // /info user
    @Command("user")
    public void userInfo(CommandSender sender) {
        User user = sender.getUser();
        sender.getEvent().reply("User: " + user.getName()).queue();
    }
}
```

---

## Arguments

### Basic Arguments

Arguments are automatically parsed from method parameters:

```java
@Command("greet")
public void greet(CommandSender sender, String name) {
    sender.getEvent().reply("Hello, " + name + "!").queue();
}

@Command("ban")
public void ban(CommandSender sender, User target, String reason) {
    sender.getEvent().reply("Banned " + target.getName() + ": " + reason).queue();
}
```

### Supported Types

| Type | Discord Type | Example |
|------|--------------|---------|
| `String` | STRING | Any text |
| `int`, `Integer` | INTEGER | Whole numbers |
| `long`, `Long` | INTEGER | Large numbers |
| `double`, `Double` | NUMBER | Decimal numbers |
| `boolean`, `Boolean` | BOOLEAN | true/false |
| `User` | USER | Discord user |
| `Member` | USER | Guild member |
| `Role` | ROLE | Discord role |
| `TextChannel` | CHANNEL | Text channel |
| `VoiceChannel` | CHANNEL | Voice channel |

---

## Dependency Injection

### Inject Services

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
        String odId = sender.getUser().getId();
        var stats = statsRepository.findByUserId(userId);
        sender.getEvent().reply("Your stats: " + stats).queue();
    }
}
```

### Inject JDA

```java
@SlashCommand
@Command("servercount")
public class ServerCountCommand extends BaseCommand {

    private final JDA jda;

    public ServerCountCommand(JDA jda) {
        this.jda = jda;
    }

    @Command
    public void execute(CommandSender sender) {
        int servers = jda.getGuilds().size();
        sender.getEvent().reply("I'm in " + servers + " servers!").queue();
    }
}
```

### Inject ComponentBuilder

```java
@SlashCommand
@Command("poll")
public class PollCommand extends BaseCommand {

    private final ComponentBuilder components;

    public PollCommand(ComponentBuilder components) {
        this.components = components;
    }

    @Command
    public void execute(CommandSender sender) {
        var yesBtn = components.button()
            .success("Yes")
            .onClick((event, ctx) -> event.reply("Voted Yes!").queue())
            .build();

        sender.getEvent()
            .reply("Vote:")
            .addActionRow(yesBtn)
            .queue();
    }
}
```

---

## Customization

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

---

## Configuration

```yaml
spring:
  jda:
    commands:
      enabled: true  # Enable/disable command system
```

---

## Best Practices

### 1. Use Ephemeral for Errors

```java
sender.getEvent().reply("Error!")
    .setEphemeral(true)  // Only visible to the user
    .queue();
```

### 2. Defer Long Operations

```java
@Command("fetch")
public void fetch(CommandSender sender) {
    sender.getEvent().deferReply().queue();

    String result = expensiveOperation();

    sender.getEvent().getHook().sendMessage(result).queue();
}
```

### 3. Use Embeds

```java
var embed = new EmbedBuilder()
    .setTitle("Info")
    .setColor(Color.BLUE)
    .addField("Field", "Value", true)
    .build();

sender.getEvent().replyEmbeds(embed).queue();
```

---

## See Also

- [Triumph CMDs Documentation](https://triumphteam.dev/library/triumph-cmds/introduction)
- [Example Commands](../example/src/main/java/es/jojo1542/example/command/)
