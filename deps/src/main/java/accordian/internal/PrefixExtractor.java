package accordian.internal;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public interface PrefixExtractor {
    /**
     * Returns the message inside messageCreateEvent without a prefix.
     *
     * @param messageCreateEvent The message create event.
     * @return A mono that completes with the prefix removed.
     * If the message doesn't start with a prefix, should return an empty mono.
     */
    Mono<String> removePrefix(MessageCreateEvent messageCreateEvent);
}
