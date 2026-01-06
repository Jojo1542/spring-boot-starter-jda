# Event Listeners

This starter provides automatic registration of JDA event listeners as Spring-managed beans.

## Table of Contents

- [Quick Start](#quick-start)
- [Creating Listeners](#creating-listeners)
- [Common Events](#common-events)
- [Dependency Injection](#dependency-injection)
- [Best Practices](#best-practices)

---

## Quick Start

### 1. Create a Listener

```java
import es.jojo1542.spring.jda.JdaListener;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@JdaListener
public class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Bot is ready! Logged in as " +
            event.getJDA().getSelfUser().getName());
    }
}
```

The listener is automatically registered with JDA when your application starts.

---

## Creating Listeners

### Using @JdaListener

The `@JdaListener` annotation marks a class as a JDA event listener and registers it as a Spring bean:

```java
@JdaListener
public class MyListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Handle message
    }
}
```

### Using ListenerAdapter

Extend `ListenerAdapter` and override only the methods you need:

```java
@JdaListener
public class GuildListener extends ListenerAdapter {

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        System.out.println("Joined guild: " + event.getGuild().getName());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        System.out.println("Left guild: " + event.getGuild().getName());
    }
}
```

### Implementing EventListener

You can also implement `EventListener` directly:

```java
@JdaListener
public class CustomListener implements EventListener {

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof MessageReceivedEvent messageEvent) {
            // Handle message
        }
    }
}
```

---

## Common Events

### Session Events

```java
@JdaListener
public class SessionListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        // Bot is connected and ready
        JDA jda = event.getJDA();
        System.out.println("Ready! Guilds: " + jda.getGuilds().size());
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        // Bot is shutting down
        System.out.println("Shutting down...");
    }

    @Override
    public void onDisconnect(DisconnectEvent event) {
        // Lost connection to Discord
        System.out.println("Disconnected!");
    }

    @Override
    public void onReconnected(ReconnectedEvent event) {
        // Reconnected to Discord
        System.out.println("Reconnected!");
    }
}
```

### Message Events

```java
@JdaListener
public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Don't respond to bots
        if (event.getAuthor().isBot()) return;

        String content = event.getMessage().getContentRaw();
        MessageChannel channel = event.getChannel();

        if (content.equalsIgnoreCase("!hello")) {
            channel.sendMessage("Hello, " + event.getAuthor().getName() + "!").queue();
        }
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        System.out.println("Message deleted: " + event.getMessageId());
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        System.out.println("Message edited: " + event.getMessage().getContentRaw());
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        System.out.println("Reaction added: " + event.getReaction().getEmoji().getName());
    }
}
```

### Guild Events

```java
@JdaListener
public class GuildListener extends ListenerAdapter {

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();
        System.out.println("Joined: " + guild.getName() + " (" + guild.getMemberCount() + " members)");
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        System.out.println("Left: " + event.getGuild().getName());
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        TextChannel channel = event.getGuild().getDefaultChannel().asTextChannel();
        if (channel != null) {
            channel.sendMessage("Welcome, " + member.getAsMention() + "!").queue();
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        System.out.println(event.getUser().getName() + " left the server");
    }
}
```

### Interaction Events

> **Note:** For component interactions (buttons, select menus, modals), consider using the
> [Component System](COMPONENTS.md) instead of raw event listeners.

```java
@JdaListener
public class InteractionListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Usually handled by the command system
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        // Usually handled by the component system
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        // Usually handled by the component system
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        // Usually handled by the component system
    }
}
```

---

## Dependency Injection

### Inject Services

```java
@JdaListener
public class LoggingListener extends ListenerAdapter {

    private final MessageLogService logService;
    private final UserRepository userRepository;

    public LoggingListener(MessageLogService logService, UserRepository userRepository) {
        this.logService = logService;
        this.userRepository = userRepository;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        logService.log(event.getMessage());
        userRepository.updateLastSeen(event.getAuthor().getId());
    }
}
```

### Inject JDA

```java
@JdaListener
public class StatusListener extends ListenerAdapter {

    private final JDA jda;

    public StatusListener(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void onReady(ReadyEvent event) {
        // Update activity based on guild count
        int guilds = jda.getGuilds().size();
        jda.getPresence().setActivity(Activity.watching(guilds + " servers"));
    }
}
```

---

## Best Practices

### 1. Ignore Bot Messages

```java
@Override
public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) return;
    // Handle message
}
```

### 2. Check Permissions Before Actions

```java
@Override
public void onMessageReceived(MessageReceivedEvent event) {
    if (!event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_SEND)) {
        return;
    }
    event.getChannel().sendMessage("Hello!").queue();
}
```

### 3. Use Async Operations

```java
@Override
public void onMessageReceived(MessageReceivedEvent event) {
    // Good - non-blocking
    event.getChannel().sendMessage("Hello!").queue();

    // With callback
    event.getChannel().sendMessage("Hello!").queue(
        success -> System.out.println("Sent!"),
        error -> System.err.println("Failed: " + error.getMessage())
    );
}
```

### 4. Handle Exceptions

```java
@Override
public void onMessageReceived(MessageReceivedEvent event) {
    try {
        processMessage(event.getMessage());
    } catch (Exception e) {
        log.error("Error processing message", e);
    }
}
```

### 5. Split Large Listeners

Instead of one massive listener, create focused listeners:

```java
@JdaListener
public class WelcomeListener extends ListenerAdapter {
    // Only handles member join/leave
}

@JdaListener
public class LoggingListener extends ListenerAdapter {
    // Only handles logging
}

@JdaListener
public class ModerationListener extends ListenerAdapter {
    // Only handles moderation events
}
```

---

## Configuration

Listeners are automatically registered when JDA starts. No additional configuration needed.

To disable the JDA auto-configuration entirely:

```yaml
spring:
  jda:
    enabled: false
```

---

## See Also

- [JDA Events Documentation](https://jda.wiki/using-jda/events/)
- [JDA ListenerAdapter](https://ci.dv8tion.net/job/JDA5/javadoc/net/dv8tion/jda/api/hooks/ListenerAdapter.html)
- [Example Listeners](../example/src/main/java/es/jojo1542/example/listener/)
