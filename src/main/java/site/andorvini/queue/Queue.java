package site.andorvini.queue;

import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;

import site.andorvini.Main;
import site.andorvini.miscellaneous.AloneInChannelHandler;
import site.andorvini.players.Player;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;

import static site.andorvini.miscellaneous.YoutubeMethods.getYoutubeVideoTitleFromUrl;

public class Queue {

    private java.util.Queue<String> trackUrlQueue = new LinkedList<String>();

    private Player player;

    public void addTrackToQueue(String track) {

        if (AloneInChannelHandler.isAloneTimerRunning()) {
            AloneInChannelHandler.stopAloneTimer(true);
        }

        trackUrlQueue.add(track);
    }

    public void clearQueue() {
        trackUrlQueue.clear();
    }

    public void removeTrackFromQueue() {
        trackUrlQueue.remove();
    }

    public EmbedBuilder getQueueEmbed() throws IOException {
        int i = 1;
        int queueSize = trackUrlQueue.size();

        EmbedBuilder queueEmbed = null;

        String nowPlayingTitle = null;
        try {
            nowPlayingTitle = Objects.requireNonNullElse(player.getAudioTrackNowPlaying().getInfo().title, "Nothing is playing now");
        } catch (NullPointerException e) {
            nowPlayingTitle = "Nothing is playing now";
        }

            queueEmbed = new EmbedBuilder()
                    .setAuthor("Queue: ")
                    .addField("__**Now Playing:**__", "[" + nowPlayingTitle + "]" + "(" + trackUrlQueue.peek() + ")")
                    .setColor(Color.blue)
                    .setFooter("Tracks in queue: " + queueSize);

                for (String track : trackUrlQueue) {
                    try {
                        queueEmbed.addField("", "`" + i + "`: " + "[" + getYoutubeVideoTitleFromUrl(track, true) + "]" + "(" + track + ")  | `" + getYoutubeVideoTitleFromUrl(track, false) + "`");
                    } catch (NullPointerException e) {
                        System.out.println("[WARN] Not youtube link");
                        queueEmbed.addField("", "`" + i + "`: " + track);
                    }
                    i++;
                }
        return queueEmbed;
    }

    public java.util.Queue<String> getQueueList() {
        return trackUrlQueue;
    }

    public void skipTrack(DiscordApi api, AudioConnection audioConnection, Server server, Player playerFrom) {
        player = playerFrom;

        trackUrlQueue.remove();
        playerFrom.stopPlaying();
        queueController(api, audioConnection, server, player);
    }

    public void queueController(DiscordApi api, AudioConnection audioConnection, Server server, Player playerFrom) {
        String trackUrl = trackUrlQueue.peek();

        player = playerFrom;

        if (getQueueList().size() == 0){
            AloneInChannelHandler.startAloneTimer(Main.getLastTextChannel(), server, api, "No tracks in queue", null, this, player);
        } else if (playerFrom.getAudioTrackNowPlaying() == null) {
            playerFrom.musicPlayer(api, audioConnection, trackUrl, server, this);
        }
    }

    public void queueOnTrackEnd(DiscordApi api, AudioConnection audioConnection, Server server) {
        trackUrlQueue.remove();
        queueController(api, audioConnection, server, player);
    }

}
