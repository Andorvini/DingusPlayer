package site.andorvini.queue;

import com.google.api.client.googleapis.services.CommonGoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;

import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;

import site.andorvini.Main;
import site.andorvini.miscellaneous.AloneInChannelHandler;
import site.andorvini.players.Player;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Queue {

    private java.util.Queue<String> trackUrlQueue = new LinkedList<String>();

    private Player player;

    public void addTrackToQueue(String track) {

        if (AloneInChannelHandler.isAloneTimerRunning()) {
            AloneInChannelHandler.stopAloneTimer();
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

    public void skipTrack(DiscordApi api, AudioConnection audioConnection, AtomicBoolean loopVar, SlashCommandCreateEvent slashCommandCreateEvent, boolean isSlash, Server server, AtomicBoolean isPlayingNow, Player playerFrom) {
        player = playerFrom;

        trackUrlQueue.remove();
        playerFrom.stopPlaying();
        queueController(api, audioConnection, loopVar, slashCommandCreateEvent, isSlash, server, player);
    }

    public void queueController(DiscordApi api, AudioConnection audioConnection, AtomicBoolean loopVar, SlashCommandCreateEvent slashCommandCreateEvent, boolean isSlash, Server server, Player playerFrom) {
        String trackUrl = trackUrlQueue.peek();

        player = playerFrom;

        if (getQueueList().size() == 0){
            AloneInChannelHandler.startAloneTimer(Main.getTextChannel(), server, api, "No tracks in queue", null, this, player);
        } else if (playerFrom.getAudioTrackNowPlaying() == null) {
            playerFrom.musicPlayer(api, audioConnection, trackUrl, loopVar, slashCommandCreateEvent, isSlash, server, this);
        }
    }

    public void queueOnTrackEnd(DiscordApi api, AudioConnection audioConnection, AtomicBoolean loopVar, SlashCommandCreateEvent slashCommandCreateEvent, boolean isSlash, Server server) {
        trackUrlQueue.remove();
        queueController(api, audioConnection, loopVar, slashCommandCreateEvent, isSlash, server, player);
    }

    public static String getYoutubeVideoTitleFromUrl(String url, boolean isTitle) throws IOException {

        String newUrl = null;

        String regexVideoId = "^[^v]+v=(.{11}).*";

        Pattern pattern = Pattern.compile(regexVideoId);
        Matcher matcher = pattern.matcher(url);

        if (url.contains("youtube") || url.contains("youtu.be")) {
            if (matcher.matches()) {
                newUrl = matcher.group(1);
            }
        }

        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();

        YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory, null)
                .setApplicationName("Dingus Player")
                .setGoogleClientRequestInitializer(new CommonGoogleClientRequestInitializer(System.getenv("DP_YOUTUBE_API_KEY")))
                .build();

        String methodResult;

        if (isTitle) {
            try {
                YouTube.Videos.List request = youtube.videos().list(Collections.singletonList("snippet"));
                request.setId(Collections.singletonList(newUrl));

                VideoListResponse response = request.execute();
                List<Video> videos = response.getItems();

                Video video = videos.get(0);

                methodResult = video.getSnippet().getTitle();
            } catch (Exception e) {
                System.out.println("[WARN] Not youtube link");
                methodResult = "Uknown title";
            }
            
        } else {
            YouTube.Videos.List request = youtube.videos().list(Collections.singletonList("contentDetails"));
            request.setId(Collections.singletonList(newUrl));

            VideoListResponse response = request.execute();
            List<Video> videos = response.getItems();

            Video video = videos.get(0);

            String isoDuration = video.getContentDetails().getDuration();

            Duration duration = Duration.parse(isoDuration);
            long minutes = duration.toMinutes();
            long seconds = duration.getSeconds() % 60;
            methodResult = String.format("%d:%02d", minutes, seconds);
        }
        return methodResult;
    }

}
