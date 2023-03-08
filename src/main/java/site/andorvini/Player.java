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

    private AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private AudioPlayer playerGlobal = playerManager.createPlayer();
    private AudioTrack audioTrackNowPlaying;
    private AudioSource source;
    private DiscordApi api;
    private AudioConnection audioConnection;
    private SlashCommandCreateEvent slashCommandCreateEvent;
    private AtomicBoolean loopVar;
    private Server server;

//    ================= SETTERS ==================
    public void setPause(boolean paused) {
        System.out.println("[MSG] Seeting pause to " + paused);
        playerGlobal.setPaused(paused);
    }

    public void setVolume(Long volumeLevel) {
        playerGlobal.setVolume(Math.toIntExact(volumeLevel));
    }

    public void setSource() {
        source = new LavaplayerAudioSource(api, playerGlobal);
        audioConnection.setAudioSource(source);
    }

    public void setPosition(long milis){
        audioTrackNowPlaying.setPosition(milis);
    }
//    ==================== GETTERS ===================

    public boolean getPause() {
        return playerGlobal.isPaused();
    }

    public AudioTrack getAudioTrackNowPlaying() {
        return audioTrackNowPlaying = playerGlobal.getPlayingTrack();
    }

    public void stopPlaying() {
        playerGlobal.destroy();
    }

    public int getVolume() {
        return playerGlobal.getVolume();
    }

//    ================ MAIN METHODS ===================

    public void musicPlayer(DiscordApi apiFrom, AudioConnection audioConnectionFrom, String trackUrl, AtomicBoolean loopVarFrom, SlashCommandCreateEvent slashCommandCreateEventFrom, boolean isSlashFrom, Server serverFrom, Queue queue){
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        AudioEventAdapter adapter = new AudioEventAdapter() {
            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                System.out.println("[MSG] Track ended with reason " + endReason + " in server `" + serverFrom.getName() + "`");
                if (endReason == AudioTrackEndReason.FINISHED) {
                    if (loopVar.get()) {
                        player.playTrack(track.makeClone());
                        System.out.println("[MSG] Loop engaged");
                    } else {
                        System.out.println("[MSG] Loop disengaged");
                        queue.queueOnTrackEnd(api, audioConnection, loopVar, slashCommandCreateEvent,true, server);
                        player.destroy();
                        playerGlobal.removeListener(this);
                    }
                } else if (endReason == AudioTrackEndReason.LOAD_FAILED) {
                    player.playTrack(track.makeClone());
                }
            }
        };

        playerGlobal.addListener(adapter);

        playerManager.registerSourceManager(new YoutubeAudioSourceManager(false, System.getenv("DP_YOUTUBE_LOGIN"), System.getenv("DP_YOUTUBE_PASSWORD")));
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());

        slashCommandCreateEvent = slashCommandCreateEventFrom;
        loopVar = loopVarFrom;
        server = serverFrom;

        api = apiFrom;
        audioConnection = audioConnectionFrom;

        source = new LavaplayerAudioSource(api, playerGlobal);
        audioConnection.setAudioSource(source);

        playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                String trackTitle = track.getInfo().title;

                System.out.println("[MSG] Track: " + trackTitle +  " loaded in server '" + serverFrom.getName() + "`");

                    slashCommandCreateEventFrom.getInteraction()
                        .respondLater()
                        .thenAccept(message -> {
                            EmbedBuilder embed = new EmbedBuilder()
                                    .setAuthor("Playing: ")
                                    .addField("", "[" + trackTitle + "](" + trackUrl + ") | `" + Main.formatDuration(track.getDuration()) + "`")
                                    .setColor(Color.GREEN)
                                    .setFooter("Track in queue: " + queue.getQueueList().size());

                            message.addEmbed(embed)
                                    .update();
                        });
                playerGlobal.playTrack(track);
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
                queue.removeTrackFromQueue();
            }
        });
    }
}
