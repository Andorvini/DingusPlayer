package site.andorvini.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.players.GreetingPlayer;
import site.andorvini.players.Player;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static site.andorvini.miscellaneous.MiscMethods.respondImmediatelyWithString;

public class Phony {
    public static void phony(DiscordApi api, SlashCommandInteraction interaction, Optional<ServerVoiceChannel> optionalUserVoiceChannel, TextChannel lastCommandChannel, Player currentPlayer, Optional<ServerVoiceChannel> optionalBotVoiceChannel, Server interactionServer, ServerVoiceChannel userVoiceChannel, GreetingPlayer currentGreetingPlayer){
        if (optionalUserVoiceChannel.isPresent()) {
            AtomicReference<String> trackUrl = new AtomicReference<>("https://storage.rferee.dev/assets/media/audio/phony-ru.flac");
            String interactionOption = interaction.getOptionByName("version").get().getStringValue().get();

            lastCommandChannel = interaction.getChannel().get();

            if (interactionOption.equals("rus")) {
                trackUrl.set("https://storage.rferee.dev/assets/media/audio/phony-ru.flac");
            } else if (interactionOption.equals("original")) {
                trackUrl.set("https://storage.rferee.dev/assets/media/audio/phony-jp.flac");
            }

            currentPlayer.setPause(true);

            if (optionalBotVoiceChannel.isEmpty()) {
                Server finalServer = interactionServer;
                userVoiceChannel.connect().thenAccept(audioConnection -> {
                    currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl.get(), currentPlayer, finalServer, false);
                });
            } else {
                AudioConnection audioConnection = interactionServer.getAudioConnection().get();
                currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl.get(), currentPlayer, interactionServer, false);
            }
        } else {
            respondImmediatelyWithString(interaction, "You are not connected to a voice channel");
        }
    }
}
