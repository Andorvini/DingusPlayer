package site.andorvini.players;

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
import site.andorvini.Main;
import site.andorvini.miscellaneous.LavaplayerAudioSource;

import java.util.concurrent.atomic.AtomicBoolean;

public class GreetingPlayer {

    private AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    private AudioPlayer player = playerManager.createPlayer();

    private Server server;

    private DiscordApi api;

    public void greetingPlayer(DiscordApi apiFrom, AudioConnection audioConnection, String trackUrl, AtomicBoolean loopVar, SlashCommandCreateEvent slashCommandCreateEvent, boolean isSlash, Server serverFrom, Player playerFrom){
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());

        player.addListener(new AudioEventAdapter() {
            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                if (endReason == AudioTrackEndReason.FINISHED) {
                    System.out.println("[MSG] Greeting Player finished playing");
                    playerFrom.setPause(false);
                    if (playerFrom.getAudioTrackNowPlaying() == null) {
                        server.getConnectedVoiceChannel(api.getYourself()).get().disconnect();
                        Main.removeGreetingPlayer(serverFrom.getId());
                        Main.removePlayerFromPlayers(serverFrom.getId());
                    }
                    playerFrom.setSource();
                }
            }
        });

        AudioSource source = new LavaplayerAudioSource(apiFrom, player);

        server = serverFrom;
        api = apiFrom;

        audioConnection.setAudioSource(source);
        playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
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
            }

        });
    }
}
