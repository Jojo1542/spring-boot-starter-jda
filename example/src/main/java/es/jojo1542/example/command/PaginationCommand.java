package es.jojo1542.example.command;

import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.cmd.jda.sender.CommandSender;
import es.jojo1542.spring.jda.command.BaseCommand;
import es.jojo1542.spring.jda.command.SlashCommand;
import es.jojo1542.spring.jda.component.builder.ComponentBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.Color;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Example command demonstrating pagination with buttons.
 */
@SlashCommand
@Command("pagination")
public class PaginationCommand extends BaseCommand {

    private final ComponentBuilder components;

    // List of 15 example items
    private static final List<String> ITEMS = List.of(
            "🍎 Apple - A red and delicious fruit",
            "🍌 Banana - Rich in potassium",
            "🍊 Orange - Full of vitamin C",
            "🍇 Grapes - Perfect for wine",
            "🍓 Strawberry - Sweet and aromatic",
            "🍑 Peach - Soft and juicy",
            "🍍 Pineapple - Tropical and refreshing",
            "🥭 Mango - The king of fruits",
            "🍒 Cherry - Small but tasty",
            "🥝 Kiwi - Green on the inside",
            "🍋 Lemon - Sour and versatile",
            "🍉 Watermelon - Perfect for summer",
            "🫐 Blueberry - Full of antioxidants",
            "🍐 Pear - Sweet and soft",
            "🥥 Coconut - Tropical and nutritious"
    );

    private static final int ITEMS_PER_PAGE = 5;
    private static final int TOTAL_PAGES = (int) Math.ceil((double) ITEMS.size() / ITEMS_PER_PAGE);

    public PaginationCommand(ComponentBuilder components) {
        this.components = components;
    }

    @Command("list")
    public void list(CommandSender sender) {
        AtomicInteger currentPage = new AtomicInteger(0);

        // Create navigation buttons
        Button prevBtn = createPrevButton(currentPage);
        Button nextBtn = createNextButton(currentPage);
        Button pageIndicator = createPageIndicator(currentPage);

        sender.getEvent()
                .replyEmbeds(createPageEmbed(currentPage.get()))
                .addActionRow(prevBtn, pageIndicator, nextBtn)
                .queue();
    }

    private Button createPrevButton(AtomicInteger currentPage) {
        return components.button()
                .secondary("◀ Previous")
                .disabled(currentPage.get() == 0)
                .onClick((event, ctx) -> {
                    int newPage = Math.max(0, currentPage.decrementAndGet());

                    // Recreate buttons with new state
                    Button newPrev = createPrevButton(currentPage);
                    Button newNext = createNextButton(currentPage);
                    Button newIndicator = createPageIndicator(currentPage);

                    event.editMessageEmbeds(createPageEmbed(newPage))
                            .setActionRow(newPrev, newIndicator, newNext)
                            .queue();
                })
                .expireAfter(Duration.ofMinutes(5))
                .build();
    }

    private Button createNextButton(AtomicInteger currentPage) {
        return components.button()
                .secondary("Next ▶")
                .disabled(currentPage.get() >= TOTAL_PAGES - 1)
                .onClick((event, ctx) -> {
                    int newPage = Math.min(TOTAL_PAGES - 1, currentPage.incrementAndGet());

                    // Recreate buttons with new state
                    Button newPrev = createPrevButton(currentPage);
                    Button newNext = createNextButton(currentPage);
                    Button newIndicator = createPageIndicator(currentPage);

                    event.editMessageEmbeds(createPageEmbed(newPage))
                            .setActionRow(newPrev, newIndicator, newNext)
                            .queue();
                })
                .expireAfter(Duration.ofMinutes(5))
                .build();
    }

    private Button createPageIndicator(AtomicInteger currentPage) {
        return components.button()
                .primary("Page " + (currentPage.get() + 1) + "/" + TOTAL_PAGES)
                .disabled(true) // Indicator only, not clickable
                .onClick((event, ctx) -> {}) // Will never execute
                .expireAfter(Duration.ofMinutes(5))
                .build();
    }

    private net.dv8tion.jda.api.entities.MessageEmbed createPageEmbed(int page) {
        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, ITEMS.size());

        StringBuilder description = new StringBuilder();
        description.append("**Fruit List**\n\n");

        for (int i = start; i < end; i++) {
            description.append("`").append(i + 1).append(".` ")
                    .append(ITEMS.get(i))
                    .append("\n\n");
        }

        return new EmbedBuilder()
                .setTitle("🛒 Fruit Catalog")
                .setDescription(description.toString())
                .setColor(Color.decode("#FF6B6B"))
                .setFooter("Showing " + (start + 1) + "-" + end + " of " + ITEMS.size() + " items")
                .build();
    }
}
