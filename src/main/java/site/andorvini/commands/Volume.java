package site.andorvini.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.players.GreetingPlayer;
import site.andorvini.players.Player;

import static site.andorvini.miscellaneous.MiscMethods.respondImmediatelyWithString;

public class Volume {
    public static void volume(DiscordApi api, SlashCommandInteraction interaction, Player currentPlayer, Server interactionServer, GreetingPlayer currentGreetingPlayer){
        Long volumeLevel = interaction.getOptionByName("volumelvl").get().getLongValue().get();
        int volumeBefore = currentPlayer.getVolume();
        currentPlayer.setVolume(volumeLevel);
        String trackUrl = "https://storage.rferee.dev/assets/media/audio/alyona_volume_warning.wav";
        AudioConnection audioConnection = interactionServer.getAudioConnection().get();

        if (volumeLevel - volumeBefore >= 100) {
            if (volumeBefore < 700) {
                currentPlayer.setPause(true);
                currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl, currentPlayer, interactionServer, false);
            }
        }

        if (volumeLevel > 900) {
            respondImmediatelyWithString(interaction, "ТЫ ЧЕ ЕБАНУТЫЙ? КАКОЙ " + volumeLevel + "? ТЕБЕ ЧЕ ЖИТЬ НАДОЕЛО?");
        } else {
            respondImmediatelyWithString(interaction, "Volume set to " + volumeLevel);
        }
    }
}
