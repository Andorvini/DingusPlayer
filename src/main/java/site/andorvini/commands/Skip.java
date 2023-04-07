package site.andorvini.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.players.Player;
import site.andorvini.queue.Queue;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;

import static site.andorvini.miscellaneous.MiscMethods.respondImmediatelyWithEmbed;
import static site.andorvini.miscellaneous.YoutubeMethods.getYoutubeVideoTitleOrDurationFromUrl;

public class Skip {
    public static void skip(DiscordApi api, SlashCommandInteraction interaction, Server interactionServer, Optional<ServerVoiceChannel> optionalBotVoiceChannel, Queue currentQueue, Player currentPlayer){
        try {
            String secondTrackInQueue = null;

            int i = 0;
            for (String trackUrl : currentQueue.getQueueList()) {
                if (i == 1) {
                    secondTrackInQueue = trackUrl;
                    break;
                }
                i++;
            }

            EmbedBuilder skipEmbed = new EmbedBuilder()
                    .addInlineField("__**Skipping:**__ ", "[" + getYoutubeVideoTitleOrDurationFromUrl(currentQueue.getQueueList().peek(), true) + "](" + currentQueue.getQueueList().peek() + ")")
                    .setColor(Color.GREEN);

            if (secondTrackInQueue != null) {
                skipEmbed.addInlineField("__**Next track:**__ ", "[" + getYoutubeVideoTitleOrDurationFromUrl(secondTrackInQueue, true) + "](" + secondTrackInQueue + ")");
            } else {
                skipEmbed.addInlineField("__**No next track :(**__","");
            }

            respondImmediatelyWithEmbed(interaction, skipEmbed);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (optionalBotVoiceChannel.isEmpty()) {
            Server finalServer = interactionServer;
            interaction.getUser().getConnectedVoiceChannel(interactionServer).get().connect().thenAccept(audioConnection -> {
                currentQueue.skipTrack (api, audioConnection, finalServer, currentPlayer);
            });
        } else {
            AudioConnection audioConnection = interactionServer.getAudioConnection().get();
            currentQueue.skipTrack(api, audioConnection, interactionServer, currentPlayer);
        }
    }
}
