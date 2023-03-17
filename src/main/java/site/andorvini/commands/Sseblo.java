package site.andorvini.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.players.GreetingPlayer;
import site.andorvini.players.Player;

import static site.andorvini.miscellaneous.MiscMethods.respondImmediatelyWithString;
import static site.andorvini.miscellaneous.SosanieEblaMethod.getSosaniaEblaUrl;

public class Sseblo {
    public static void sseblo(DiscordApi api, SlashCommandInteraction interaction, Server interactionServer, Player currentPlayer, GreetingPlayer currentGreetingPlayer, ServerVoiceChannel userVoiceChannel, TextChannel lastCommandChannel){
        lastCommandChannel = interaction.getChannel().get();

        String textToConvert = interaction.getOptionByName("text").get().getStringValue().get();

        String trackUrl = getSosaniaEblaUrl(textToConvert);
        respondImmediatelyWithString(interaction, "Playing \"" + textToConvert + "\" with Alyona Flirt ");

        if (api.getYourself().getConnectedVoiceChannel(interactionServer).isEmpty()) {
            String finalTrackUrl = trackUrl;
            currentPlayer.setPause(true);
            Server finalInteractionServer = interactionServer;

            userVoiceChannel.connect().thenAccept(audioConnection -> {
                currentGreetingPlayer.greetingPlayer(api, audioConnection, finalTrackUrl, currentPlayer, finalInteractionServer, false);
            });
        } else {
            currentPlayer.setPause(true);
            AudioConnection audioConnection = interactionServer.getAudioConnection().get();
            currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl, currentPlayer, interactionServer, false);
        }
    }
}
