# Example Bot

This is an example Discord bot using `spring-boot-starter-jda`.

## Setup

### 1. Create your `.env` file

```bash
cp .env.example .env
```

### 2. Add your bot token

Edit `.env` and add your Discord bot token:

```env
DISCORD_TOKEN=your_bot_token_here
```

> Get your token from the [Discord Developer Portal](https://discord.com/developers/applications)

### 3. Run the bot

From the project root:

```bash
./gradlew :example:bootRun
```

## Features

### Listeners

- **ReadyListener** - Logs when the bot is ready
- **MessageListener** - Responds to `!ping` text command

### Slash Commands

| Command | Description |
|---------|-------------|
| `/ping` | Shows gateway ping |
| `/info` | Shows bot information |
| `/info server` | Shows server information |
| `/info user` | Shows your user information |

## Project Structure

```
example/
├── .env.example                    # Template for environment variables
├── build.gradle.kts
└── src/main/
    ├── java/es/jojo1542/example/
    │   ├── ExampleBotApplication.java
    │   ├── listener/
    │   │   ├── ReadyListener.java
    │   │   └── MessageListener.java
    │   └── command/
    │       ├── PingCommand.java
    │       └── InfoCommand.java
    └── resources/
        └── application.yml
```

## Configuration

See `src/main/resources/application.yml` for all configuration options.

```yaml
spring:
  jda:
    token: ${DISCORD_TOKEN}
    status: ONLINE
    activity:
      type: PLAYING
      text: "with Spring Boot 4"
    intents:
      - GUILD_MESSAGES
      - GUILD_MEMBERS
      - MESSAGE_CONTENT
      - DIRECT_MESSAGES
```
