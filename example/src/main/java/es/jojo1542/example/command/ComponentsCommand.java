package es.jojo1542.example.command;

import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.jda.sender.CommandSender;
import es.jojo1542.spring.jda.command.BaseCommand;
import es.jojo1542.spring.jda.command.SlashCommand;
import es.jojo1542.spring.jda.component.annotation.ButtonHandler;
import es.jojo1542.spring.jda.component.annotation.ComponentData;
import es.jojo1542.spring.jda.component.annotation.ModalHandler;
import es.jojo1542.spring.jda.component.annotation.SelectMenuHandler;
import es.jojo1542.spring.jda.component.builder.ComponentBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.awt.Color;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Example command demonstrating the component interaction system.
 *
 * <p>This command showcases:
 * <ul>
 *   <li>Callback-based buttons with TTL</li>
 *   <li>Stateless buttons with annotation handlers</li>
 *   <li>Select menus</li>
 *   <li>Modals with text inputs</li>
 * </ul>
 */
@SlashCommand
@Command("components")
public class ComponentsCommand extends BaseCommand {

    private final ComponentBuilder components;

    public ComponentsCommand(ComponentBuilder components) {
        this.components = components;
    }

    /**
     * Demonstrates callback-based buttons with a counter.
     */
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

        var resetBtn = components.button()
                .danger("Reset")
                .onClick((event, ctx) -> {
                    count.set(0);
                    event.editMessage("Counter: **0**").queue();
                    ctx.invalidate(); // This button can only be used once
                })
                .expireAfter(Duration.ofMinutes(5))
                .singleUse()
                .build();

        sender.getEvent()
                .reply("Counter: **0**")
                .addActionRow(decrementBtn, incrementBtn, resetBtn)
                .queue();
    }

    /**
     * Demonstrates stateless buttons with annotation-based handlers.
     */
    @Command("ticket")
    public void ticket(CommandSender sender) {
        String ticketId = "TKT-" + System.currentTimeMillis() % 10000;

        var claimBtn = components.button()
                .primary("Claim")
                .emoji("\uD83D\uDCE5")
                .withId("ticket-claim")
                .withData(ticketId)
                .build();

        var closeBtn = components.button()
                .danger("Close")
                .emoji("\u274C")
                .withId("ticket-close")
                .withData(ticketId)
                .build();

        var embed = new EmbedBuilder()
                .setTitle("Support Ticket")
                .setDescription("Ticket ID: `" + ticketId + "`\n\nClick a button to interact.")
                .setColor(Color.ORANGE)
                .build();

        sender.getEvent()
                .replyEmbeds(embed)
                .addActionRow(claimBtn, closeBtn)
                .queue();
    }

    // Stateless handlers - work across bot restarts!

    @ButtonHandler(value = "ticket-claim", pattern = true)
    public void onClaimTicket(ButtonInteractionEvent event, @ComponentData String ticketId) {
        event.reply("You claimed ticket: **" + ticketId + "**")
                .setEphemeral(true)
                .queue();
    }

    @ButtonHandler(value = "ticket-close", pattern = true)
    public void onCloseTicket(ButtonInteractionEvent event, @ComponentData String ticketId) {
        event.reply("Closing ticket: **" + ticketId + "**")
                .setEphemeral(true)
                .queue();

        // Disable the buttons on the original message
        event.editComponents(
                event.getMessage().getActionRows().stream()
                        .map(ActionRow::asDisabled)
                        .toList()
        ).queue();
    }

    /**
     * Demonstrates select menus.
     */
    @Command("colors")
    public void colors(CommandSender sender) {
        var colorMenu = components.selectMenu()
                .placeholder("Choose your favorite color")
                .option("Red", "red", "A vibrant red color", "\uD83D\uDD34")
                .option("Blue", "blue", "A cool blue color", "\uD83D\uDD35")
                .option("Green", "green", "A fresh green color", "\uD83D\uDFE2")
                .option("Purple", "purple", "A royal purple color", "\uD83D\uDFE3")
                .option("Orange", "orange", "A warm orange color", "\uD83D\uDFE0")
                .onSelect((event, ctx) -> {
                    String selected = event.getValues().get(0);
                    event.reply("You selected: **" + selected + "** " + getColorEmoji(selected))
                            .setEphemeral(true)
                            .queue();
                })
                .expireAfter(Duration.ofMinutes(5))
                .build();

        sender.getEvent()
                .reply("Select your favorite color:")
                .addActionRow(colorMenu)
                .queue();
    }

    /**
     * Demonstrates modals.
     */
    @Command("feedback")
    public void feedback(CommandSender sender) {
        var openModalBtn = components.button()
                .primary("Open Feedback Form")
                .emoji("\uD83D\uDCDD")
                .onClick((event, ctx) -> {
                    var modal = components.modal()
                            .title("Send Feedback")
                            .shortInput("subject", "Subject", "Brief description of your feedback")
                            .paragraphInput("details", "Details", "Tell us more about your feedback...")
                            .onSubmit((modalEvent, modalCtx) -> {
                                String subject = modalEvent.getValue("subject").getAsString();
                                String details = modalEvent.getValue("details").getAsString();

                                var embed = new EmbedBuilder()
                                        .setTitle("Feedback Received!")
                                        .setColor(Color.GREEN)
                                        .addField("Subject", subject, false)
                                        .addField("Details", details, false)
                                        .setFooter("Thank you for your feedback!")
                                        .build();

                                modalEvent.replyEmbeds(embed).setEphemeral(true).queue();
                            })
                            .expireAfter(Duration.ofMinutes(30))
                            .build();

                    event.replyModal(modal).queue();
                })
                .expireAfter(Duration.ofMinutes(10))
                .build();

        sender.getEvent()
                .reply("Click the button below to open the feedback form:")
                .addActionRow(openModalBtn)
                .queue();
    }

    /**
     * Demonstrates stateless modal with annotation handler.
     */
    @Command("report")
    public void report(CommandSender sender) {
        var openModalBtn = components.button()
                .danger("Report Issue")
                .emoji("\u26A0\uFE0F")
                .onClick((event, ctx) -> {
                    var modal = components.modal()
                            .title("Report an Issue")
                            .shortInput("title", "Issue Title", "Brief title for the issue")
                            .paragraphInput("description", "Description", "Describe the issue in detail")
                            .withId("issue-report")
                            .withData(sender.getUser().getId())
                            .build();

                    event.replyModal(modal).queue();
                })
                .expireAfter(Duration.ofMinutes(10))
                .build();

        sender.getEvent()
                .reply("Click below to report an issue:")
                .addActionRow(openModalBtn)
                .queue();
    }

    @ModalHandler(value = "issue-report", pattern = true)
    public void onIssueReport(ModalInteractionEvent event, @ComponentData String reporterId) {
        String title = event.getValue("title").getAsString();
        String description = event.getValue("description").getAsString();

        var embed = new EmbedBuilder()
                .setTitle("Issue Report Received")
                .setColor(Color.RED)
                .addField("Title", title, false)
                .addField("Description", description, false)
                .addField("Reporter ID", reporterId, true)
                .setFooter("We'll look into this!")
                .build();

        event.replyEmbeds(embed).setEphemeral(true).queue();
    }

    private String getColorEmoji(String color) {
        return switch (color) {
            case "red" -> "\uD83D\uDD34";
            case "blue" -> "\uD83D\uDD35";
            case "green" -> "\uD83D\uDFE2";
            case "purple" -> "\uD83D\uDFE3";
            case "orange" -> "\uD83D\uDFE0";
            default -> "";
        };
    }
}
