package es.jojo1542.example.listener;

import es.jojo1542.spring.jda.JdaListener;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example message listener that responds to a simple ping command.
 */
@JdaListener
public class MessageListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignore bots
        if (event.getAuthor().isBot()) {
            return;
        }

        String content = event.getMessage().getContentRaw();

        // Simple text command example (prefer slash commands in production)
        if (content.equalsIgnoreCase("!ping")) {
            long ping = event.getJDA().getGatewayPing();
            event.getChannel().sendMessage("Pong! 🏓 Gateway ping: " + ping + "ms").queue();
            log.debug("Responded to ping command from {}", event.getAuthor().getName());
        }
    }
}
