package site.Andorvini;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;


public class Main {
    public static void main(String[] args) {

        String token = null;
        token = System.getenv("DP_DISCORD_TOKEN");
        if (token == null) {
            System.out.println("DP_DISCORD_TOKEN environment variable not set");
            System.exit(1);
        }

        AtomicBoolean loopVar = new AtomicBoolean(false);

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        SlashCommand play =
                SlashCommand.with("play","Play music from provided Yotube URL",
                                Arrays.asList(
                                        SlashCommandOption.create(SlashCommandOptionType.STRING, "url", "Youtube url", true)
                                ))
                .createGlobal(api)
                .join();

        SlashCommand phony = SlashCommand.with("phony","Play ANTIPOTHY WORLD")
                .createGlobal(api)
                .join();

        SlashCommand loop = SlashCommand.with("loop","Loop music")
                .createGlobal(api)
                .join();

        SlashCommand test =
                SlashCommand.with("mp3","Play MP3 from Direct URL",
                                Arrays.asList(
                                    SlashCommandOption.create(SlashCommandOptionType.STRING, "urlMP3", "Direct URL to MP3 file(ONLY HTTPS)", true)
                                ))
                .createGlobal(api)
                .join();

        SlashCommand leave =
                SlashCommand.with("leave","Leave voice channel")
                .createGlobal(api)
                .join();

        api.addSlashCommandCreateListener(slashCommandCreateEvent -> {
            SlashCommandInteraction interaction = slashCommandCreateEvent.getSlashCommandInteraction();
            Server interactionServer = slashCommandCreateEvent.getInteraction().getServer().get();

            if (interaction.getFullCommandName().equals("join")) {

            } else if (interaction.getFullCommandName().equals("phony")) {
                System.out.println("Playing phony");
                if (interaction.getUser().getConnectedVoiceChannel(interactionServer).isPresent()) {
                    String trackUrl = "https://www.youtube.com/watch?v=9QLT1Aw_45s";
                    interaction.getUser().getConnectedVoiceChannel(interactionServer).get().connect().thenAccept(audioConnection -> {
                        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
                        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
                        AudioPlayer player = playerManager.createPlayer();
                        AudioSource source = new LavaplayerAudioSource(api, player);
                        audioConnection.setAudioSource(source);

                        interaction.createImmediateResponder()
                                .setContent("Now Playing " )
                                .respond()
                                .join();

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

                            }
                        });
                    });
                } else {
                    interaction.createImmediateResponder()
                            .setContent("You are not connected to a voice channel")
                            .respond()
                            .join();
                }
            } else if (interaction.getFullCommandName().equals("play")) {
                if (interaction.getUser().getConnectedVoiceChannel(interactionServer).isPresent()) {
                    String trackUrl = interaction.getOptionByName("url").get().getStringValue().get();
                    interaction.getUser().getConnectedVoiceChannel(interactionServer).get().connect().thenAccept(audioConnection -> {
                        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
                        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
                        AudioPlayer player = playerManager.createPlayer();
                        AudioSource source = new LavaplayerAudioSource(api, player);
                        audioConnection.setAudioSource(source);

                        playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
                            @Override
                            public void trackLoaded(AudioTrack track) {
                                System.out.println("Track loaded");
                                slashCommandCreateEvent.getInteraction()
                                                .respondLater()
                                                .thenAccept(message -> {
                                                    message.setContent("Now playing \"" + track.getInfo().title + "\"").update();
                                                });
                                player.playTrack(track);

                                player.addListener(new AudioEventAdapter() {
                                    @Override
                                    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                                        if (endReason == AudioTrackEndReason.FINISHED) {
                                            if (loopVar.get() == true) {
                                                player.playTrack(track.makeClone());
                                                System.out.println("loop engaged");
                                            } else {
                                                System.out.println("loop disengaged");
                                            }
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
                    });
                } else {
                    interaction.createImmediateResponder()
                            .setContent("You are not connected to a voice channel")
                            .respond()
                            .join();
                }
            } else if (interaction.getFullCommandName().equals("loop")) {
                if (loopVar.get() == false) {
                    loopVar.set(true);
                    interaction.createImmediateResponder()
                            .setContent("Looping is now enabled")
                            .respond()
                            .join();
                } else if (loopVar.get() == true) {
                    loopVar.set(false);
                    interaction.createImmediateResponder()
                            .setContent("Looping is now disabled")
                            .respond()
                            .join();
                }
            } else if (interaction.getFullCommandName().equals("mp3")) {
                Server server = interaction.getServer().get();
                interaction.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                    String trackUrl = interaction.getOptionByName("urlMP3").get().getStringValue().get();
                    AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
                    AudioSourceManager sourceManager = new HttpAudioSourceManager();
                    playerManager.registerSourceManager(sourceManager);
                    AudioPlayer player = playerManager.createPlayer();

                    AudioSource source = new LavaplayerAudioSource(api, player);
                    audioConnection.setAudioSource(source);

                    playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            player.addListener(new AudioEventAdapter() {
                                @Override
                                public void onPlayerPause(AudioPlayer player) {
                                    // Player was paused
                                }

                                @Override
                                public void onPlayerResume(AudioPlayer player) {
                                    // Player was resumed
                                }

                                @Override
                                public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                                    // Track finished playing
                                    if (endReason == AudioTrackEndReason.FINISHED.FINISHED) {
                                        server.getConnectedVoiceChannel(api.getYourself()).get().disconnect();
                                    }
                                }
                            });

                            slashCommandCreateEvent.getInteraction()
                                                    .respondLater()
                                                    .thenAccept(message -> {
                                                        message.setContent("Now playing \"" + track.getInfo().title + "\"").update();
                                                    });
                            player.playTrack(track);
                        }

                        @Override
                        public void noMatches() {
                            System.out.println("No matches found for the provided URL.");
                        }

                        @Override
                        public void loadFailed(FriendlyException e) {
                            System.out.println("Failed to load track.");
                            e.printStackTrace();
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            System.out.println("Cannot load playlist from direct URL.");
                        }
                    });
                });
            } else if (interaction.getFullCommandName().equals("leave")) {
                Server server = interaction.getServer().get();
                interaction.createImmediateResponder()
                        .setContent("Leaving voice channel \"" + server.getConnectedVoiceChannel(api.getYourself()).get().getName() + "\"" )
                        .respond()
                        .join();
                server.getConnectedVoiceChannel(api.getYourself()).get().disconnect();
            }

        });

        api.addServerVoiceChannelMemberJoinListener(serverVoiceChannelMemberJoinEvent -> {
            Server server = serverVoiceChannelMemberJoinEvent.getServer();
            if (serverVoiceChannelMemberJoinEvent.getUser().getId() == 998958761618190421L ) {
                serverVoiceChannelMemberJoinEvent.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                    AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
                    AudioSourceManager sourceManager = new HttpAudioSourceManager();
                    playerManager.registerSourceManager(sourceManager);
                    AudioPlayer player = playerManager.createPlayer();

                    AudioSource source = new LavaplayerAudioSource(api, player);
                    audioConnection.setAudioSource(source);

                    String trackUrl = "https://storage.rferee.dev/assets/media/audio/sukran.mp3";
                    playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            player.addListener(new AudioEventAdapter() {
                                @Override
                                public void onPlayerPause(AudioPlayer player) {
                                    // Player was paused
                                }

                                @Override
                                public void onPlayerResume(AudioPlayer player) {
                                    // Player was resumed
                                }

                                @Override
                                public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                                    // Track finished playing
                                    if (endReason == AudioTrackEndReason.FINISHED.FINISHED) {
                                        server.getConnectedVoiceChannel(api.getYourself()).get().disconnect();
                                    }
                                }
                            });

                            player.playTrack(track);
                        }

                        @Override
                        public void noMatches() {
                            System.out.println("No matches found for the provided URL.");
                        }

                        @Override
                        public void loadFailed(FriendlyException e) {
                            System.out.println("Failed to load track.");
                            e.printStackTrace();
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            System.out.println("Cannot load playlist from direct URL.");
                        }
                    });
                });
            }
        });
    }
}