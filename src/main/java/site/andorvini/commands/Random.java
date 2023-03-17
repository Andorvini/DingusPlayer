package site.andorvini.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.players.GreetingPlayer;
import site.andorvini.players.Player;

import java.util.Optional;
import java.util.Set;

import static site.andorvini.miscellaneous.MiscMethods.respondImmediatelyWithString;
import static site.andorvini.miscellaneous.SosanieEblaMethod.getSosaniaEblaUrl;

public class Random {
    public static void random(DiscordApi api, SlashCommandInteraction interaction, Server interactionServer, TextChannel lastCommandChannel, Player currentPlayer, GreetingPlayer currentGreetingPlayer, Optional<ServerVoiceChannel> optionalBotVoiceChannel, Optional<ServerVoiceChannel> optionalUserVoiceChannel){
        lastCommandChannel = interaction.getChannel().get();
        if (optionalUserVoiceChannel.isPresent()) {
            Set<User> userSet = interaction.getUser().getConnectedVoiceChannel(interactionServer).get().getConnectedUsers();

            User randomUser = userSet.stream().skip(new java.util.Random().nextInt(userSet.size())).findFirst().orElse(null);

            assert randomUser != null;
            String trackUrl = getSosaniaEblaUrl(randomUser.getDisplayName(interactionServer));

            respondImmediatelyWithString(interaction,randomUser.getName());

            currentPlayer.setPause(true);

            if (optionalBotVoiceChannel.isEmpty()) {
                Server finalServer = interactionServer;
                interaction.getUser().getConnectedVoiceChannel(interactionServer).get().connect().thenAccept(audioConnection -> {
                    currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl, currentPlayer, finalServer, false);
                });
            } else {
                AudioConnection audioConnection = interactionServer.getAudioConnection().get();
                currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl, currentPlayer, interactionServer, false);
            }
        } else {
            respondImmediatelyWithString(interaction, "You are not connected to a voice channel");
        }
    }
}
