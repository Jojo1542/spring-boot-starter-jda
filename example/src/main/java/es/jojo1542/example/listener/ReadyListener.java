package es.jojo1542.example.listener;

import es.jojo1542.spring.jda.JdaListener;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener that logs when the bot is ready.
 */
@JdaListener
public class ReadyListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ReadyListener.class);

    @Override
    public void onReady(ReadyEvent event) {
        var selfUser = event.getJDA().getSelfUser();
        log.info("Bot ready! Logged in as {} ({})", selfUser.getName(), selfUser.getId());
        log.info("Connected to {} guild(s)", event.getGuildAvailableCount());
    }
}
