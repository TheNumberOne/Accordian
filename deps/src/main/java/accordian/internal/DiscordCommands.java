package accordian.internal;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collection;
import java.util.Map;

public final class DiscordCommands {
    private final PrefixExtractor prefixExtractor;
    private final Mono<Map<String, DiscordCommand>> commands;

    public DiscordCommands(PrefixExtractor prefixExtractor, Collection<DiscordCommand> commands) {
        this.prefixExtractor = prefixExtractor;
        this.commands = Flux.fromStream(
                commands
                        .stream()
                        .map(command -> command.names().map(name -> Tuples.of(name, command))))
                .flatMap(a -> a)
                .collectMap(Tuple2::getT1, Tuple2::getT2)
                .cache();
    }

    public Mono<Void> on(MessageCreateEvent messageCreateEvent) {
        return prefixExtractor
                .removePrefix(messageCreateEvent)
                .zipWith(commands)
                .flatMap(tuple -> {
                    String message = tuple.getT1();
                    Map<String, DiscordCommand> commands = tuple.getT2();
                    Tuple2<String, String> commandNameAndParams = splitByCommandName(message);
                    DiscordCommand command = commands.get(commandNameAndParams.getT1());
                    if (command == null) {
                        return Mono.empty();
                    } else {
                        return command.process(messageCreateEvent, commandNameAndParams.getT2());
                    }
                });
    }

    private Tuple2<String, String> splitByCommandName(String message) {
        int firstSpace = message.indexOf(' ');
        if (firstSpace == -1) {
            return Tuples.of(message, "");
        }
        int firstLetterAfterSpace = firstSpace + 1;
        // move firstLetterAfterSpace forward while it is still a space.
        while (firstLetterAfterSpace < message.length() && message.charAt(firstLetterAfterSpace) == ' ') {
            firstLetterAfterSpace++;
        }
        return Tuples.of(message.substring(0, firstSpace), message.substring(firstLetterAfterSpace));
    }

}
