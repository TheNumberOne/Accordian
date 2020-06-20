package accordian.internal;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Represents a discord command to be ran.
 */
public interface DiscordCommand {
    /**
     * Returns a flux of names that can be used to call the command.
     * @return The names used to call the command.
     */
    Flux<String> names();

    /**
     * Processes the message.
     * @param messageCreateEvent The event that the message was created within.
     * @param message The message with the prefix and command name removed.
     * @return A mono that doesn't emit and completes when this is done processing the command.
     */
    Mono<Void> process(MessageCreateEvent messageCreateEvent, String message);
}
