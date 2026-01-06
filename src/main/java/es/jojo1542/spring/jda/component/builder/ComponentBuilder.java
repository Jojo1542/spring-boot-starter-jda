package es.jojo1542.spring.jda.component.builder;

import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;

import java.util.Arrays;
import java.util.List;

/**
 * Main entry point for building Discord components.
 *
 * <p>This interface provides a fluent API for creating interactive Discord
 * components with automatic callback registration and ID management.
 *
 * <p>Obtain via dependency injection:
 * <pre>{@code
 * @Autowired
 * private ComponentBuilder components;
 * }</pre>
 *
 * <p>Example usage:
 * <pre>{@code
 * // Callback-based button (temporary, with TTL)
 * var confirmButton = components.button()
 *     .primary("Confirm")
 *     .onClick((event, ctx) -> event.reply("Confirmed!").queue())
 *     .expireAfter(Duration.ofMinutes(5))
 *     .build();
 *
 * // Stateless button (routes to @ButtonHandler)
 * var deleteButton = components.button()
 *     .danger("Delete")
 *     .withId("confirm-delete")
 *     .withData(itemId)
 *     .build();
 *
 * // Create action row and send
 * event.reply("Are you sure?")
 *     .addActionRow(confirmButton, deleteButton)
 *     .queue();
 * }</pre>
 *
 * @author jojo1542
 * @see ButtonBuilder
 * @see SelectMenuBuilder
 * @see ModalBuilder
 */
public interface ComponentBuilder {

    /**
     * Start building a button.
     *
     * @return a new button builder
     */
    ButtonBuilder button();

    /**
     * Start building a string select menu.
     *
     * @return a new select menu builder
     */
    SelectMenuBuilder selectMenu();

    /**
     * Start building an entity select menu (users, roles, channels).
     *
     * @return a new entity select builder
     */
    EntitySelectBuilder entitySelect();

    /**
     * Start building a modal dialog.
     *
     * @return a new modal builder
     */
    ModalBuilder modal();

    /**
     * Create an action row from components.
     *
     * @param components the components to include
     * @return an action row containing the components
     */
    default ActionRow actionRow(ItemComponent... components) {
        return ActionRow.of(components);
    }

    /**
     * Create multiple action rows.
     *
     * @param rows the action rows
     * @return list of action rows
     */
    default List<ActionRow> actionRows(ActionRow... rows) {
        return Arrays.asList(rows);
    }
}
