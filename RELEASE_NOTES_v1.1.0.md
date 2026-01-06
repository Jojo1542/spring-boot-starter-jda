# 📦 Release Notes — v1.1.0

## 🎮 Component Interaction System

This release introduces a **complete component interaction system** for Discord buttons, select menus, and modals. The system provides both **callback-based** (stateful) and **annotation-based** (stateless) handlers with automatic memory management.

---

## ✨ New Features

### 🔘 **Button Builder API**
- Fluent API for creating Discord buttons with ease.
- Support for all button styles: `primary`, `secondary`, `success`, `danger`, `link`.
- Built-in emoji support.
- Example:
  ```java
  var button = components.button()
      .success("Click me!")
      .emoji("✅")
      .onClick((event, ctx) -> event.reply("Clicked!").queue())
      .expireAfter(Duration.ofMinutes(5))
      .build();
  ```

### 📋 **Select Menu Builder API**
- Create string select menus with options.
- Support for placeholders, min/max values, and default options.
- Example:
  ```java
  var menu = components.selectMenu()
      .placeholder("Choose an option")
      .option("Option 1", "opt1", "Description", "🔵")
      .option("Option 2", "opt2", "Description", "🟢")
      .onSelect((event, ctx) -> event.reply("Selected: " + event.getValues()).queue())
      .build();
  ```

### 👤 **Entity Select Menu Builder**
- Support for all Discord entity select types:
  - `userSelect()` — Select users
  - `roleSelect()` — Select roles
  - `channelSelect()` — Select channels (with type filtering)
  - `mentionableSelect()` — Select users or roles

### 📝 **Modal Builder API**
- Create modals with text input fields.
- Support for short (single-line) and paragraph (multi-line) inputs.
- Configurable min/max length, required fields, and placeholders.
- Example:
  ```java
  var modal = components.modal()
      .title("Feedback Form")
      .shortInput("subject", "Subject", "Enter subject...")
      .paragraphInput("details", "Details", "Tell us more...", true)
      .onSubmit((event, ctx) -> {
          String subject = event.getValue("subject").getAsString();
          event.reply("Received: " + subject).queue();
      })
      .build();
  ```

### 🔄 **Dual Handler System**

#### Callback-based (Stateful)
- Perfect for temporary interactions with context (polls, confirmations, counters).
- Automatic TTL expiration with configurable duration.
- Memory-safe with Caffeine cache backend.
- Single-use option with `singleUse()` or `maxInvocations(n)`.

#### Annotation-based (Stateless)
- Handlers survive bot restarts.
- Perfect for persistent components (ticket systems, role selectors).
- Annotations: `@ButtonHandler`, `@SelectMenuHandler`, `@ModalHandler`.
- Data extraction with `@ComponentData` annotation.
- Example:
  ```java
  @ButtonHandler(value = "ticket-close", pattern = true)
  public void onClose(ButtonInteractionEvent event, @ComponentData String ticketId) {
      event.reply("Closing ticket: " + ticketId).queue();
  }
  ```

### 🧠 **Smart Memory Management**
- **Caffeine Cache**: High-performance caching with per-entry TTL.
- **Configurable TTL**: Default 15 minutes, customizable per component.
- **Max Size Limit**: Prevents memory bloat (default: 10,000 entries).
- **Max Invocations**: Limit how many times a callback can be used.
- **Auto-disable on Expire**: Automatically disables buttons when callbacks expire.

### ⚙️ **Configuration via `application.yml`**
```yaml
spring:
  jda:
    components:
      enabled: true
      callback:
        default-ttl: 15m
        max-size: 10000
        expired-message: "This interaction has expired."
        disable-on-expire: true
        log-expirations: false
      handler:
        unknown-message: "Unknown component."
```

---

## 📦 New Dependencies

- **Caffeine** (`com.github.ben-manes.caffeine:caffeine:3.1.8`)
  - High-performance caching library for callback storage.

---

## 🏗️ Architecture

### Component ID Strategy
Components use a structured ID format for routing:
```
[prefix]:[type]:[identifier]:[data]

Examples:
- cb:btn:a1b2c3d4        → Callback-based button
- sc:btn:ticket-close:123 → Stateless button with data
```

### New Classes

| Package | Class | Description |
|---------|-------|-------------|
| `component.builder` | `ComponentBuilder` | Main entry point for creating components |
| `component.builder` | `ButtonBuilder` | Fluent button builder |
| `component.builder` | `SelectMenuBuilder` | String select menu builder |
| `component.builder` | `EntitySelectBuilder` | Entity select menu builder |
| `component.builder` | `ModalBuilder` | Modal dialog builder |
| `component.callback` | `CallbackRegistry` | Interface for callback storage |
| `component.callback` | `CaffeineCallbackRegistry` | Caffeine-based implementation |
| `component.callback` | `ComponentCallback` | Functional interface for handlers |
| `component.callback` | `CallbackContext` | Context passed to callbacks |
| `component.handler` | `InteractionRouter` | Routes interactions to handlers |
| `component.handler` | `ComponentHandlerRegistry` | Registry for annotation handlers |
| `component.annotation` | `@ButtonHandler` | Marks button handler methods |
| `component.annotation` | `@SelectMenuHandler` | Marks select menu handler methods |
| `component.annotation` | `@ModalHandler` | Marks modal handler methods |
| `component.annotation` | `@ComponentData` | Extracts data from component IDs |
| `component.config` | `ComponentAutoConfiguration` | Spring Boot auto-configuration |
| `component.config` | `ComponentProperties` | Configuration properties |

---

## 📚 Example Commands

The example module now includes demonstration commands:

| Command | Description |
|---------|-------------|
| `/components counter` | Interactive counter with +/- buttons |
| `/components ticket` | Ticket system with stateless handlers |
| `/components colors` | Color picker with select menu |
| `/components feedback` | Feedback form with modal |
| `/components report` | Issue report with stateless modal |
| `/pagination list` | Paginated list with navigation buttons |
| `/form register` | Registration form modal |
| `/form contact` | Contact form modal |
| `/form survey` | Satisfaction survey modal |

---

## 🔧 Usage

### Inject ComponentBuilder
```java
@SlashCommand
@Command("mycommand")
public class MyCommand extends BaseCommand {

    private final ComponentBuilder components;

    public MyCommand(ComponentBuilder components) {
        this.components = components;
    }

    @Command
    public void execute(CommandSender sender) {
        var button = components.button()
            .primary("Click me")
            .onClick((event, ctx) -> event.reply("Hello!").queue())
            .expireAfter(Duration.ofMinutes(5))
            .build();

        sender.getEvent()
            .reply("Here's a button:")
            .addActionRow(button)
            .queue();
    }
}
```

---

## ⚠️ Breaking Changes

None. This release is fully backwards compatible with v1.0.0.

---

## 🐛 Bug Fixes

- Fixed potential circular dependency issues with JDA and component beans.
- Used `SmartInitializingSingleton` to defer handler scanning.
- Used `ObjectProvider<JDA>` for lazy JDA resolution in auto-disable feature.

---

**Full Changelog**: https://github.com/Jojo1542/spring-boot-starter-jda/compare/v1.0.0...v1.1.0
