package site.Andorvini;

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
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;

import java.util.concurrent.atomic.AtomicBoolean;

public class Player {

    private static AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    private static AudioPlayer player = playerManager.createPlayer();

    private static AudioTrack audioTrackNowPlaying;

    public static void setPause(boolean paused) {
        player.setPaused(paused);
    }

    public static boolean getPause() {
        return player.isPaused();
    }

    public static AudioTrack getAudioTrackNowPlaying() {
        return audioTrackNowPlaying;
    }

    public static void stopPlaying() {
        player.destroy();
    }

//    public static boolean isPlaying() {
//        boolean isPlaying;;
//
//        if (player.getPlayingTrack() == null) {
//            isPlaying = false;
//        } else {
//            isPlaying = true;
//        }
//
//        return isPlaying;
//    }

    public static void musicPlayer(DiscordApi api, AudioConnection audioConnection, String trackUrl, AtomicBoolean loopVar, SlashCommandCreateEvent slashCommandCreateEvent, int a, Server server){
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        AudioSource source = new LavaplayerAudioSource(api, player);
        audioConnection.setAudioSource(source);
        playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                System.out.println("Track loaded");
                if (a == 0) {
                    slashCommandCreateEvent.getInteraction()
                            .respondLater()
                            .thenAccept(message -> {
                                message.setContent("Now playing `\"" + track.getInfo().title + "\"`").update();
                            });
                }
                player.playTrack(track);

                audioTrackNowPlaying = track;

                player.addListener(new AudioEventAdapter() {
                    @Override
                    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                        if (a == 0) {
                            if (endReason == AudioTrackEndReason.FINISHED) {
                                if (loopVar.get() == true) {
                                    player.playTrack(track.makeClone());
                                    System.out.println("loop engaged");
                                } else {
                                    System.out.println("loop disengaged");
                                }
                            }

                        } else if (a == 1) {
                            server.getConnectedVoiceChannel(api.getYourself()).get().disconnect();
                            player.destroy();
                        }
                    }
                });
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("Failed to load track");
            }

        });
    }
}
