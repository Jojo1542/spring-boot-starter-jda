# Component Interaction System

The component interaction system provides a fluent API for creating and handling Discord **buttons**, **select menus**, and **modals** with automatic memory management and two handler strategies.

## Table of Contents

- [Quick Start](#quick-start)
- [Buttons](#buttons)
- [Select Menus](#select-menus)
- [Entity Select Menus](#entity-select-menus)
- [Modals](#modals)
- [Handler Strategies](#handler-strategies)
- [Configuration](#configuration)
- [Best Practices](#best-practices)

---

## Quick Start

### 1. Inject ComponentBuilder

```java
@SlashCommand
@Command("mycommand")
public class MyCommand extends BaseCommand {

    private final ComponentBuilder components;

    public MyCommand(ComponentBuilder components) {
        this.components = components;
    }
}
```

### 2. Create a Button

```java
@Command
public void execute(CommandSender sender) {
    var button = components.button()
        .primary("Click me!")
        .onClick((event, ctx) -> {
            event.reply("You clicked the button!").queue();
        })
        .expireAfter(Duration.ofMinutes(5))
        .build();

    sender.getEvent()
        .reply("Here's a button:")
        .addActionRow(button)
        .queue();
}
```

---

## Buttons

### Button Styles

```java
// Primary (blurple)
components.button().primary("Label")

// Secondary (gray)
components.button().secondary("Label")

// Success (green)
components.button().success("Label")

// Danger (red)
components.button().danger("Label")

// Link (opens URL, no callback)
components.button().link("Visit", "https://example.com")
```

### Adding Emojis

```java
components.button()
    .success("Confirm")
    .emoji("✅")  // Unicode emoji
    .onClick(...)
    .build();

components.button()
    .primary("Custom")
    .emoji(Emoji.fromCustom("myemoji", 123456789L, false))  // Custom emoji
    .onClick(...)
    .build();
```

### Disabled Buttons

```java
components.button()
    .primary("Disabled")
    .disabled(true)
    .onClick(...)  // Will never fire
    .build();
```

### Single-Use Buttons

```java
// Using singleUse()
components.button()
    .danger("Delete")
    .onClick((event, ctx) -> {
        event.reply("Deleted!").queue();
    })
    .singleUse()  // Automatically invalidates after one use
    .build();

// Using maxInvocations()
components.button()
    .primary("Vote")
    .onClick((event, ctx) -> {
        event.reply("Vote recorded!").queue();
    })
    .maxInvocations(3)  // Can be used 3 times total
    .build();
```

### Counter Example

```java
@Command("counter")
public void counter(CommandSender sender) {
    AtomicInteger count = new AtomicInteger(0);

    var decrementBtn = components.button()
        .secondary("-")
        .onClick((event, ctx) -> {
            int newCount = count.decrementAndGet();
            event.editMessage("Counter: **" + newCount + "**").queue();
        })
        .expireAfter(Duration.ofMinutes(5))
        .build();

    var incrementBtn = components.button()
        .secondary("+")
        .onClick((event, ctx) -> {
            int newCount = count.incrementAndGet();
            event.editMessage("Counter: **" + newCount + "**").queue();
        })
        .expireAfter(Duration.ofMinutes(5))
        .build();

    sender.getEvent()
        .reply("Counter: **0**")
        .addActionRow(decrementBtn, incrementBtn)
        .queue();
}
```

---

## Select Menus

### String Select Menu

```java
var menu = components.selectMenu()
    .placeholder("Choose your favorite color")
    .option("Red", "red", "A vibrant color", "🔴")
    .option("Blue", "blue", "A calm color", "🔵")
    .option("Green", "green", "A fresh color", "🟢")
    .onSelect((event, ctx) -> {
        String selected = event.getValues().get(0);
        event.reply("You selected: " + selected).queue();
    })
    .expireAfter(Duration.ofMinutes(5))
    .build();

sender.getEvent()
    .reply("Pick a color:")
    .addActionRow(menu)
    .queue();
```

### Multi-Select

```java
var menu = components.selectMenu()
    .placeholder("Select your skills")
    .minValues(1)
    .maxValues(3)
    .option("Java", "java")
    .option("Python", "python")
    .option("JavaScript", "javascript")
    .option("Rust", "rust")
    .onSelect((event, ctx) -> {
        String skills = String.join(", ", event.getValues());
        event.reply("Your skills: " + skills).queue();
    })
    .build();
```

### Default Selection

```java
var menu = components.selectMenu()
    .placeholder("Choose difficulty")
    .option("Easy", "easy")
    .option("Normal", "normal", true)  // Selected by default
    .option("Hard", "hard")
    .onSelect(...)
    .build();
```

---

## Entity Select Menus

### User Select

```java
var userSelect = components.entitySelect()
    .userSelect()
    .placeholder("Select a user")
    .onSelect((event, ctx) -> {
        var user = event.getMentions().getUsers().get(0);
        event.reply("Selected: " + user.getAsMention()).queue();
    })
    .expireAfter(Duration.ofMinutes(5))
    .build();
```

### Role Select

```java
var roleSelect = components.entitySelect()
    .roleSelect()
    .placeholder("Select a role")
    .minValues(1)
    .maxValues(3)
    .onSelect((event, ctx) -> {
        var roles = event.getMentions().getRoles();
        event.reply("Roles: " + roles).queue();
    })
    .build();
```

### Channel Select

```java
var channelSelect = components.entitySelect()
    .channelSelect()
    .placeholder("Select a text channel")
    .channelTypes(ChannelType.TEXT, ChannelType.VOICE)  // Filter by type
    .onSelect((event, ctx) -> {
        var channel = event.getMentions().getChannels().get(0);
        event.reply("Channel: " + channel.getAsMention()).queue();
    })
    .build();
```

### Mentionable Select (Users + Roles)

```java
var mentionableSelect = components.entitySelect()
    .mentionableSelect()
    .placeholder("Select users or roles")
    .onSelect((event, ctx) -> {
        event.reply("Selected: " + event.getValues()).queue();
    })
    .build();
```

---

## Modals

### Basic Modal

```java
var button = components.button()
    .primary("Open Form")
    .onClick((event, ctx) -> {
        var modal = components.modal()
            .title("Feedback Form")
            .shortInput("subject", "Subject", "Brief description")
            .paragraphInput("details", "Details", "Tell us more...")
            .onSubmit((modalEvent, modalCtx) -> {
                String subject = modalEvent.getValue("subject").getAsString();
                String details = modalEvent.getValue("details").getAsString();

                modalEvent.reply("Received: " + subject).queue();
            })
            .expireAfter(Duration.ofMinutes(10))
            .build();

        event.replyModal(modal).queue();
    })
    .build();
```

### Input Types

```java
// Short input (single line, max 4000 chars)
.shortInput("field_id", "Label", "Placeholder")

// Short input with constraints
.shortInput("age", "Age", "e.g. 25", true, 1, 3)  // required, min 1, max 3 chars

// Paragraph input (multi-line, max 4000 chars)
.paragraphInput("bio", "Biography", "Write about yourself...")

// Paragraph with constraints
.paragraphInput("essay", "Essay", "Min 100 words...", true, 100, 2000)
```

### Complete Registration Form Example

```java
@Command("register")
public void register(CommandSender sender) {
    var openFormBtn = components.button()
        .success("Open Registration Form")
        .onClick((event, ctx) -> {
            var modal = components.modal()
                .title("Registration Form")
                .shortInput("name", "Full Name", "e.g. John Doe")
                .shortInput("age", "Age", "e.g. 25", true, 1, 3)
                .shortInput("email", "Email", "e.g. john@email.com")
                .paragraphInput("bio", "About You", "Optional...", false)
                .onSubmit((modalEvent, modalCtx) -> {
                    String name = modalEvent.getValue("name").getAsString();
                    String age = modalEvent.getValue("age").getAsString();
                    String email = modalEvent.getValue("email").getAsString();
                    String bio = modalEvent.getValue("bio") != null
                        ? modalEvent.getValue("bio").getAsString()
                        : "Not provided";

                    var embed = new EmbedBuilder()
                        .setTitle("Registration Complete")
                        .setColor(Color.GREEN)
                        .addField("Name", name, true)
                        .addField("Age", age, true)
                        .addField("Email", email, false)
                        .addField("Bio", bio, false)
                        .build();

                    modalEvent.replyEmbeds(embed).queue();
                })
                .expireAfter(Duration.ofMinutes(10))
                .build();

            event.replyModal(modal).queue();
        })
        .expireAfter(Duration.ofMinutes(5))
        .build();

    sender.getEvent()
        .reply("Click below to register:")
        .addActionRow(openFormBtn)
        .queue();
}
```

---

## Handler Strategies

### Callback-Based (Stateful)

Best for **temporary interactions** with context:
- Counters, polls, confirmations
- Data that doesn't need to persist
- Short-lived interactions (minutes/hours)

```java
var button = components.button()
    .primary("Vote Yes")
    .onClick((event, ctx) -> {
        // Access to closure variables
        votes.incrementAndGet();
        event.reply("Vote recorded!").queue();
    })
    .expireAfter(Duration.ofMinutes(30))
    .build();
```

**Callback Context Methods:**
```java
.onClick((event, ctx) -> {
    ctx.invalidate();           // Remove callback immediately
    ctx.getCallbackId();        // Get the callback ID
    ctx.getAttributes();        // Get custom attributes
    ctx.getAttribute("key");    // Get specific attribute
})
```

### Annotation-Based (Stateless)

Best for **persistent interactions** that survive restarts:
- Ticket systems, role selectors
- Permanent UI elements
- Data stored externally (database)

#### Creating Stateless Components

```java
// Button with ID and data
var claimBtn = components.button()
    .primary("Claim Ticket")
    .withId("ticket-claim")       // Handler identifier
    .withData(ticketId)           // Data to pass to handler
    .build();

// Modal with ID and data
var modal = components.modal()
    .title("Report Issue")
    .shortInput("title", "Title", "...")
    .withId("issue-report")
    .withData(userId)
    .build();
```

#### Handler Annotations

```java
// Button handler
@ButtonHandler(value = "ticket-claim", pattern = true)
public void onClaimTicket(ButtonInteractionEvent event, @ComponentData String ticketId) {
    event.reply("Claimed ticket: " + ticketId).queue();
}

// Select menu handler
@SelectMenuHandler(value = "role-select", pattern = true)
public void onRoleSelect(StringSelectInteractionEvent event, @ComponentData String category) {
    String selected = event.getValues().get(0);
    event.reply("Selected " + selected + " in " + category).queue();
}

// Modal handler
@ModalHandler(value = "issue-report", pattern = true)
public void onIssueReport(ModalInteractionEvent event, @ComponentData String userId) {
    String title = event.getValue("title").getAsString();
    event.reply("Report from " + userId + ": " + title).queue();
}
```

#### @ComponentData Annotation

Extract data from the component ID:

```java
// Full data string
@ComponentData String data

// Specific index with delimiter
@ComponentData(index = 0) String firstPart
@ComponentData(index = 1) String secondPart
@ComponentData(index = 0, delimiter = "-") String customDelimiter

// With default value
@ComponentData(defaultValue = "unknown") String value

// Type conversion
@ComponentData(index = 0) long id
@ComponentData(index = 1) int count
@ComponentData(index = 2) boolean enabled
```

---

## Configuration

### Application Properties

```yaml
spring:
  jda:
    components:
      enabled: true                           # Enable component system
      callback:
        default-ttl: 15m                      # Default expiration time
        max-size: 10000                       # Maximum cached callbacks
        expired-message: "This interaction has expired."
        disable-on-expire: true               # Disable buttons when expired
        log-expirations: false                # Log expired callbacks
      handler:
        unknown-message: "Unknown component." # Message for unregistered handlers
```

### Programmatic Configuration

```java
// Per-component TTL
.expireAfter(Duration.ofMinutes(30))

// Per-component max invocations
.maxInvocations(5)

// Single use (equivalent to maxInvocations(1))
.singleUse()
```

---

## Best Practices

### 1. Choose the Right Strategy

| Use Case | Strategy | Why |
|----------|----------|-----|
| Counter, poll | Callback | Needs closure context |
| Confirmation dialog | Callback | Temporary, short-lived |
| Ticket system | Stateless | Survives restarts |
| Role selector | Stateless | Permanent UI |
| Paginated list | Callback | State changes per user |

### 2. Set Appropriate TTLs

```java
// Short interactions (confirmations)
.expireAfter(Duration.ofMinutes(1))

// Medium interactions (forms)
.expireAfter(Duration.ofMinutes(10))

// Long interactions (complex workflows)
.expireAfter(Duration.ofHours(1))
```

### 3. Handle Expiration Gracefully

The system automatically:
- Responds with `expired-message` to expired callbacks
- Disables buttons when `disable-on-expire` is true
- Logs expirations when `log-expirations` is true

### 4. Use Single-Use for Destructive Actions

```java
var deleteBtn = components.button()
    .danger("Delete Forever")
    .onClick((event, ctx) -> {
        // Perform deletion
        ctx.invalidate();  // Prevent double-clicks
        event.reply("Deleted!").queue();
    })
    .singleUse()
    .build();
```

### 5. Combine Strategies When Needed

```java
// Button uses callback (to open modal), modal uses stateless handler
var openBtn = components.button()
    .primary("Open Report Form")
    .onClick((event, ctx) -> {
        var modal = components.modal()
            .title("Report")
            .shortInput("issue", "Issue", "...")
            .withId("report-modal")           // Stateless handler
            .withData(event.getUser().getId())
            .build();
        event.replyModal(modal).queue();
    })
    .expireAfter(Duration.ofMinutes(10))
    .build();

@ModalHandler(value = "report-modal", pattern = true)
public void onReport(ModalInteractionEvent event, @ComponentData String userId) {
    // This handler survives bot restarts
}
```

---

## See Also

- [Example Commands](../example/src/main/java/es/jojo1542/example/command/)
- [Release Notes v1.1.0](../RELEASE_NOTES_v1.1.0.md)
