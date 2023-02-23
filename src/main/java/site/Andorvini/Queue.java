package site.Andorvini;

import com.google.api.client.googleapis.services.CommonGoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Queue {

    private static java.util.Queue<String> trackUrlQueue = new LinkedList<String>();

    public static void addTrackToQueue(String track) {
        trackUrlQueue.add(track);
    }

    public static void clearQueue() {
        trackUrlQueue.clear();
    }

    public static EmbedBuilder getQueueEmbed() throws IOException {
        int i = 1;
        int queueSize = trackUrlQueue.size();

        EmbedBuilder queueEmbed;

        if (Player.getAudioTrackNowPlaying() != null) {
            queueEmbed = new EmbedBuilder()
                    .setAuthor("Queue: ")
                    .addField("__**Now Playing:**__", "[" + Player.getAudioTrackNowPlaying().getInfo().title + "]" + "(" + trackUrlQueue.peek() + ")")
                    .setColor(Color.pink)
                    .setFooter("Tracks in queue: " + queueSize);

            try {
                for (String track : trackUrlQueue) {
                    queueEmbed.addField("", "`" + i + "`" + ": " + "[" + getYoutubeVideoTitleFromUrl(track, true) + "]" + "(" + track + ")  | `" + getYoutubeVideoTitleFromUrl(track, false) + "`");
                    i++;
                }
            } catch (NullPointerException e) {
                System.out.println("[WARN] Not youtube link");
            }
        } else {
            queueEmbed = new EmbedBuilder()
                    .setAuthor("No playing track")
                    .setDescription("Use `/play` command to play track")
                    .setColor(Color.RED);
        }
        return queueEmbed;
    }

    public static java.util.Queue<String> getQueueList() {
        return trackUrlQueue;
    }

    public static void skipTrack(DiscordApi api, AudioConnection audioConnection, AtomicBoolean loopVar, SlashCommandCreateEvent slashCommandCreateEvent, boolean isSlash, Server server, AtomicBoolean isPlayingNow) {
        trackUrlQueue.remove();
        Player.stopPlaying();
        System.out.println("skipping");
        queueController(api, audioConnection, loopVar, slashCommandCreateEvent, isSlash, server, isPlayingNow);
    }

    public static void queueController(DiscordApi api, AudioConnection audioConnection, AtomicBoolean loopVar, SlashCommandCreateEvent slashCommandCreateEvent, boolean isSlash, Server server, AtomicBoolean isPlayingNow) {
        String trackUrl = trackUrlQueue.peek();

        if (Player.getAudioTrackNowPlaying() == null) {
            Player.musicPlayer(api, audioConnection, trackUrl, loopVar, slashCommandCreateEvent, isSlash, server);
        } else {

        }
    }

    public static void queueOnTrackEnd(DiscordApi api, AudioConnection audioConnection, AtomicBoolean loopVar, SlashCommandCreateEvent slashCommandCreateEvent, boolean isSlash, Server server) {
        AtomicBoolean isPlayingNow = new AtomicBoolean(false);
        Player.stopPlaying();

        trackUrlQueue.remove();
        queueController(api, audioConnection, loopVar, slashCommandCreateEvent, isSlash, server, isPlayingNow);
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
                .setGoogleClientRequestInitializer(new CommonGoogleClientRequestInitializer(System.getenv("YOUTUBE_API_KEY")))
                .build();

        String methodResult = null;

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
