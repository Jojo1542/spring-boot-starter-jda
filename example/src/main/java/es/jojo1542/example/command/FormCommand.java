package es.jojo1542.example.command;

import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.jda.sender.CommandSender;
import es.jojo1542.spring.jda.command.BaseCommand;
import es.jojo1542.spring.jda.command.SlashCommand;
import es.jojo1542.spring.jda.component.builder.ComponentBuilder;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;

/**
 * Example command demonstrating modals with form data.
 */
@SlashCommand
@Command("form")
public class FormCommand extends BaseCommand {

    private final ComponentBuilder components;

    public FormCommand(ComponentBuilder components) {
        this.components = components;
    }

    @Command("register")
    public void register(CommandSender sender) {
        var openFormBtn = components.button()
                .success("📝 Open Registration Form")
                .onClick((event, ctx) -> {
                    var modal = components.modal()
                            .title("Registration Form")
                            .shortInput("name", "Full Name", "e.g. John Doe")
                            .shortInput("age", "Age", "e.g. 25", true, 1, 3)
                            .shortInput("email", "Email Address", "e.g. john@email.com")
                            .paragraphInput("bio", "Tell us about yourself", "Write a brief description...", false)
                            .onSubmit((modalEvent, modalCtx) -> {
                                // Get form values
                                String name = modalEvent.getValue("name").getAsString();
                                String age = modalEvent.getValue("age").getAsString();
                                String email = modalEvent.getValue("email").getAsString();
                                String bio = modalEvent.getValue("bio") != null
                                        ? modalEvent.getValue("bio").getAsString()
                                        : "Not provided";

                                // Create embed with the data
                                var embed = new EmbedBuilder()
                                        .setTitle("✅ Registration Complete")
                                        .setColor(Color.GREEN)
                                        .setThumbnail(modalEvent.getUser().getAvatarUrl())
                                        .addField("👤 Name", name, true)
                                        .addField("🎂 Age", age + " years", true)
                                        .addField("📧 Email", email, false)
                                        .addField("📝 Bio", bio.isEmpty() ? "Not provided" : bio, false)
                                        .setFooter("Registered by " + modalEvent.getUser().getName(),
                                                modalEvent.getUser().getAvatarUrl())
                                        .setTimestamp(Instant.now())
                                        .build();

                                modalEvent.replyEmbeds(embed).queue();
                            })
                            .expireAfter(Duration.ofMinutes(10))
                            .build();

                    event.replyModal(modal).queue();
                })
                .expireAfter(Duration.ofMinutes(5))
                .build();

        var embed = new EmbedBuilder()
                .setTitle("📋 Registration System")
                .setDescription("Click the button below to open the registration form.\n\n" +
                        "**Form fields:**\n" +
                        "• Full name\n" +
                        "• Age\n" +
                        "• Email address\n" +
                        "• Bio (optional)")
                .setColor(Color.BLUE)
                .build();

        sender.getEvent()
                .replyEmbeds(embed)
                .addActionRow(openFormBtn)
                .queue();
    }

    @Command("contact")
    public void contact(CommandSender sender) {
        var openContactBtn = components.button()
                .primary("💬 Send Message")
                .onClick((event, ctx) -> {
                    var modal = components.modal()
                            .title("Contact Form")
                            .shortInput("subject", "Subject", "What do you want to talk about?")
                            .paragraphInput("message", "Your message", "Write your message here...", true, 10, 1000)
                            .onSubmit((modalEvent, modalCtx) -> {
                                String subject = modalEvent.getValue("subject").getAsString();
                                String message = modalEvent.getValue("message").getAsString();

                                var embed = new EmbedBuilder()
                                        .setTitle("📨 Message Received")
                                        .setColor(Color.decode("#5865F2"))
                                        .addField("📌 Subject", subject, false)
                                        .addField("💬 Message", message, false)
                                        .addField("👤 Sent by", modalEvent.getUser().getAsMention(), true)
                                        .addField("📅 Date", "<t:" + Instant.now().getEpochSecond() + ":F>", true)
                                        .setFooter("Thank you for contacting us!")
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
                .reply("**📞 Contact Form**\n\nClick the button to send us a message.")
                .addActionRow(openContactBtn)
                .queue();
    }

    @Command("survey")
    public void survey(CommandSender sender) {
        var openSurveyBtn = components.button()
                .secondary("📊 Take Survey")
                .onClick((event, ctx) -> {
                    var modal = components.modal()
                            .title("Satisfaction Survey")
                            .shortInput("rating", "Rating (1-10)", "e.g. 8", true, 1, 2)
                            .shortInput("best", "What did you like most?", "The best part of the service...")
                            .shortInput("improve", "What would you improve?", "Suggestions for improvement...")
                            .paragraphInput("comments", "Additional comments", "Any other feedback...", false)
                            .onSubmit((modalEvent, modalCtx) -> {
                                String rating = modalEvent.getValue("rating").getAsString();
                                String best = modalEvent.getValue("best").getAsString();
                                String improve = modalEvent.getValue("improve").getAsString();
                                String comments = modalEvent.getValue("comments") != null
                                        ? modalEvent.getValue("comments").getAsString()
                                        : "No comments";

                                // Determine color based on rating
                                int score = Integer.parseInt(rating);
                                Color color = score >= 8 ? Color.GREEN : score >= 5 ? Color.ORANGE : Color.RED;
                                String emoji = score >= 8 ? "🌟" : score >= 5 ? "👍" : "😕";

                                var embed = new EmbedBuilder()
                                        .setTitle(emoji + " Survey Response")
                                        .setColor(color)
                                        .addField("📊 Rating", "**" + rating + "/10**", true)
                                        .addField("✅ Best Part", best, false)
                                        .addField("🔧 To Improve", improve, false)
                                        .addField("💭 Comments", comments.isEmpty() ? "None" : comments, false)
                                        .setFooter("Thank you for your feedback, " + modalEvent.getUser().getName() + "!")
                                        .setTimestamp(Instant.now())
                                        .build();

                                modalEvent.replyEmbeds(embed).queue();
                            })
                            .expireAfter(Duration.ofMinutes(10))
                            .build();

                    event.replyModal(modal).queue();
                })
                .expireAfter(Duration.ofMinutes(5))
                .build();

        var embed = new EmbedBuilder()
                .setTitle("📊 Satisfaction Survey")
                .setDescription("Your feedback is very important to us.\n\n" +
                        "Click the button to take a brief survey.")
                .setColor(Color.decode("#9B59B6"))
                .build();

        sender.getEvent()
                .replyEmbeds(embed)
                .addActionRow(openSurveyBtn)
                .queue();
    }
}
