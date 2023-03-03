package site.andorvini;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Player {

    private static AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    private static AudioPlayer player = playerManager.createPlayer();

    private static AudioTrack audioTrackNowPlaying;

    private static AudioSource source;

    private static DiscordApi api;

    private static AudioConnection audioConnection;

    public static void setPause(boolean paused) {
        System.out.println("[MSG] Seeting pause to " + paused);
        player.setPaused(paused);
    }

    public static boolean getPause() {
        return player.isPaused();
    }

    public static AudioTrack getAudioTrackNowPlaying() {
        return audioTrackNowPlaying = player.getPlayingTrack();
    }

    public static void stopPlaying() {
        player.destroy();
    }

    public static void setVolume(Long volumeLevel) {
        player.setVolume(Math.toIntExact(volumeLevel));
    }

    public static int getVolume() {
        return player.getVolume();
    }

    public static void setSource() {
        source = new LavaplayerAudioSource(api, player);
        audioConnection.setAudioSource(source);
    }

    public static void musicPlayer(DiscordApi apiFrom, AudioConnection audioConnectionFrom, String trackUrl, AtomicBoolean loopVar, SlashCommandCreateEvent slashCommandCreateEvent, boolean isSlash, Server server){
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());

        api = apiFrom;
        audioConnection = audioConnectionFrom;

        source = new LavaplayerAudioSource(api, player);
        audioConnection.setAudioSource(source);

        playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                String trackTitle = track.getInfo().title;

                System.out.println("[MSG] Track: " + trackTitle +  " loaded");

                if (isSlash) {
                    slashCommandCreateEvent.getInteraction()
                        .respondLater()
                        .thenAccept(message -> {
                            EmbedBuilder embed = new EmbedBuilder()
                                    .setAuthor("Playing: ")
                                    .addField("", "[" + trackTitle + "](" + trackUrl + ") | `" + Main.formatDuration(track.getDuration()) + "`")
                                    .setColor(Color.GREEN)
                                    .setFooter("Track in queue: " + Queue.getQueueList().size());

                            message.addEmbed(embed)
                                    .update();
                        });
                }

                player.addListener(new AudioEventAdapter() {
                    @Override
                    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                        System.out.println("[MSG] Track ended with reason " + endReason);
                        if (endReason == AudioTrackEndReason.FINISHED) {
                            if (isSlash) {
                                System.out.println("[MSG] Detected slash command");

                                if (loopVar.get()) {
                                    player.playTrack(track.makeClone());
                                    System.out.println("[MSG] Loop engaged");
                                } else {
                                    System.out.println("[MSG] Loop disengaged");
                                    Queue.queueOnTrackEnd(api, audioConnection, loopVar, slashCommandCreateEvent,true, server);
                                    player.destroy();
                                }
                            } else {
                                System.out.println("[MSG] No slash command detected");
                                server.getConnectedVoiceChannel(api.getYourself()).get().disconnect();
                                player.destroy();
                            }
                        } else if (endReason == AudioTrackEndReason.LOAD_FAILED) {
                            player.playTrack(track.makeClone());
                        }
                    }
                });
                player.playTrack(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("[MSG] Failed to load track: ");
                exception.printStackTrace();
                Queue.removeTrackFromQueue();
            }
        });
    }
}
