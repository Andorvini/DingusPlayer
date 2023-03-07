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

    private static SlashCommandCreateEvent slashCommandCreateEvent;

    private static AtomicBoolean loopVar;

    private static Server server;

//    ================= SETTERS ==================
    public static void setPause(boolean paused) {
        System.out.println("[MSG] Seeting pause to " + paused);
        player.setPaused(paused);
    }

    public static void setVolume(Long volumeLevel) {
        player.setVolume(Math.toIntExact(volumeLevel));
    }

    public static void setSource() {
        source = new LavaplayerAudioSource(api, player);
        audioConnection.setAudioSource(source);
    }

    public static void setPosition(long milis){
        audioTrackNowPlaying.setPosition(milis);
    }
//    ==================== GETTERS ===================

    public static boolean getPause() {
        return player.isPaused();
    }

    public static AudioTrack getAudioTrackNowPlaying() {
        return audioTrackNowPlaying = player.getPlayingTrack();
    }

    public static void stopPlaying() {
        player.destroy();
    }

    public static int getVolume() {
        return player.getVolume();
    }

//    ================ MAIN METHODS ===================

    public static void addOnTrackEndEventToPlayer(){
        player.addListener(new AudioEventAdapter() {

            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                System.out.println("[MSG] Track ended with reason " + endReason);
                if (endReason == AudioTrackEndReason.FINISHED) {
                        if (loopVar.get()) {
                            player.playTrack(track.makeClone());
                            System.out.println("[MSG] Loop engaged");
                        } else {
                            System.out.println("[MSG] Loop disengaged");
                            Queue.queueOnTrackEnd(api, audioConnection, loopVar, slashCommandCreateEvent,true, server);
                            player.destroy();
                        }
                } else if (endReason == AudioTrackEndReason.LOAD_FAILED) {
                    player.playTrack(track.makeClone());
                }
            }
        });

    }

    public static void musicPlayer(DiscordApi apiFrom, AudioConnection audioConnectionFrom, String trackUrl, AtomicBoolean loopVarFrom, SlashCommandCreateEvent slashCommandCreateEventFrom, boolean isSlashFrom, Server serverFrom){
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());

        slashCommandCreateEvent = slashCommandCreateEventFrom;
        loopVar = loopVarFrom;
        server = serverFrom;

        api = apiFrom;
        audioConnection = audioConnectionFrom;

        source = new LavaplayerAudioSource(api, player);
        audioConnection.setAudioSource(source);

        playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                String trackTitle = track.getInfo().title;

                System.out.println("[MSG] Track: " + trackTitle +  " loaded");

                    slashCommandCreateEventFrom.getInteraction()
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
