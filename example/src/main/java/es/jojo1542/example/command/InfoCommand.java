package es.jojo1542.example.command;

import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.jda.sender.CommandSender;
import es.jojo1542.spring.jda.command.BaseCommand;
import es.jojo1542.spring.jda.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.awt.Color;
import java.time.Instant;

/**
 * Info command with subcommands demonstrating command structure.
 *
 * <p>Usage:
 * <ul>
 *   <li>/info bot - Shows bot info</li>
 *   <li>/info server - Shows server info</li>
 *   <li>/info user - Shows user info</li>
 * </ul>
 */
@SlashCommand
@Command("info")
public class InfoCommand extends BaseCommand {

    @Command("bot")
    public void botInfo(CommandSender sender) {
        JDA jda = sender.getEvent().getJDA();

        var embed = new EmbedBuilder()
                .setTitle("🤖 Bot Information")
                .setColor(Color.CYAN)
                .addField("Name", jda.getSelfUser().getName(), true)
                .addField("Guilds", String.valueOf(jda.getGuilds().size()), true)
                .addField("Users", String.valueOf(jda.getUsers().size()), true)
                .addField("Gateway Ping", jda.getGatewayPing() + "ms", true)
                .setFooter("Powered by Spring Boot + JDA")
                .setTimestamp(Instant.now())
                .build();

        sender.getEvent().replyEmbeds(embed).queue();
    }

    @Command("server")
    public void serverInfo(CommandSender sender) {
        var guild = sender.getGuild();

        if (guild == null) {
            sender.reply("❌ This command can only be used in a server!").setEphemeral(true).queue();
            return;
        }

        var embed = new EmbedBuilder()
                .setTitle("🏠 Server Information")
                .setColor(Color.GREEN)
                .setThumbnail(guild.getIconUrl())
                .addField("Name", guild.getName(), true)
                .addField("ID", guild.getId(), true)
                .addField("Owner", guild.getOwner() != null ? guild.getOwner().getUser().getName() : "Unknown", true)
                .addField("Members", String.valueOf(guild.getMemberCount()), true)
                .addField("Channels", String.valueOf(guild.getChannels().size()), true)
                .addField("Roles", String.valueOf(guild.getRoles().size()), true)
                .setTimestamp(Instant.now())
                .build();

        sender.getEvent().replyEmbeds(embed).queue();
    }

    @Command("user")
    public void userInfo(CommandSender sender) {
        var user = sender.getUser();
        var member = sender.getMember();

        var embed = new EmbedBuilder()
                .setTitle("👤 User Information")
                .setColor(Color.BLUE)
                .setThumbnail(user.getAvatarUrl())
                .addField("Name", user.getName(), true)
                .addField("ID", user.getId(), true)
                .addField("Created", "<t:" + user.getTimeCreated().toEpochSecond() + ":R>", true);

        if (member != null) {
            embed.addField("Joined", "<t:" + member.getTimeJoined().toEpochSecond() + ":R>", true);
            embed.addField("Roles", String.valueOf(member.getRoles().size()), true);
        }

        embed.setTimestamp(Instant.now());
        sender.getEvent().replyEmbeds(embed.build()).queue();
    }
}
