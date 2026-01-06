package es.jojo1542.example.command;

import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.jda.sender.CommandSender;
import es.jojo1542.spring.jda.command.BaseCommand;
import es.jojo1542.spring.jda.command.SlashCommand;

/**
 * Simple ping slash command.
 *
 * <p>Usage: /ping
 */
@SlashCommand
@Command("ping")
public class PingCommand extends BaseCommand {

    @Command
    public void execute(CommandSender sender) {
        long ping = sender.getEvent().getJDA().getGatewayPing();
        sender.reply("Pong! 🏓 Gateway ping: " + ping + "ms").queue();
    }
}
