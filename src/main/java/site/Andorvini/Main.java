package site.Andorvini;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import okhttp3.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


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
                SlashCommand.with("play","Play music from provided Youtube URL",
                                Arrays.asList(
                                        SlashCommandOption.create(SlashCommandOptionType.STRING, "url", "Youtube url", true)
                                ))
                .createGlobal(api)
                .join();

        SlashCommand command =
                SlashCommand.with("phony", "Play ANTIPATHY WORLD",
                                Arrays.asList(
                                            SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "version", "Choose version of song", true,
                                                    Arrays.asList(
                                                            SlashCommandOptionChoice.create("Russian remix", "rus"),
                                                            SlashCommandOptionChoice.create("Original", "original")))
                                            ))
                        .createGlobal(api)
                        .join();

        SlashCommand loop = SlashCommand.with("loop","Loop music")
                .createGlobal(api)
                .join();

        SlashCommand mp3 =
                SlashCommand.with("mp3","Play MP3 from Direct URL",
                                Arrays.asList(
                                    SlashCommandOption.create(SlashCommandOptionType.STRING, "urlMP3", "Direct URL to MP3 or WAV file(ONLY HTTPS)", true)
                                ))
                .createGlobal(api)
                .join();

        SlashCommand leave =
                SlashCommand.with("leave","Leave voice channel")
                .createGlobal(api)
                .join();

        SlashCommand pause = SlashCommand.with("pause","Pause music")
                .createGlobal(api)
                .join();

        SlashCommand sseblo = SlashCommand.with("sseblo","Convert text into voice using sseblobotapi",
                Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "text", "Text you want to voice", true)
                ))
                .createGlobal(api)
                .join();

        SlashCommand clear = SlashCommand.with("clear","Delete specified number of messages",
                Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.LONG,"count","Message count",true)
                ))
                .createGlobal(api)
                .join();

        api.addSlashCommandCreateListener(slashCommandCreateEvent -> {
            SlashCommandInteraction interaction = slashCommandCreateEvent.getSlashCommandInteraction();
            Server server = slashCommandCreateEvent.getInteraction().getServer().get();

            if (interaction.getFullCommandName().equals("phony")) {
                if (interaction.getUser().getConnectedVoiceChannel(server).isPresent()) {
                    AtomicReference<String> trackUrl = new AtomicReference<>("https://storage.rferee.dev/assets/media/audio/phony-ru.flac");
                    String interactionOption = interaction.getOptionByName("version").get().getStringValue().get();

                    if (interactionOption.equals("rus")) {
                        System.out.println("Playing russian remix");
                        trackUrl.set("https://storage.rferee.dev/assets/media/audio/phony-ru.flac");
                    } else if (interactionOption.equals("original")) {
                        System.out.println("Playing original");
                        trackUrl.set("https://storage.rferee.dev/assets/media/audio/phony-jp.flac");
                    }

                    if (api.getYourself().getConnectedVoiceChannel(server).isEmpty()) {
                        interaction.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                            System.out.println("connecting");
                            mp3Player(api, audioConnection, trackUrl.get(), loopVar, slashCommandCreateEvent,0, server);
                        });
                    } else {
                        AudioConnection audioConnection = server.getAudioConnection().get();
                        System.out.println("already connected");
                        mp3Player(api, audioConnection, trackUrl.get(), loopVar, slashCommandCreateEvent, 0, server);
                    }
                } else {
                    interaction.createImmediateResponder()
                            .setContent("You are not connected to a voice channel")
                            .respond()
                            .join();
                }
            } else if (interaction.getFullCommandName().equals("play")) {
                if (interaction.getUser().getConnectedVoiceChannel(server).isPresent()) {
                    String trackUrl = interaction.getOptionByName("url").get().getStringValue().get().replaceAll("\\[", "%5B").replaceAll("]", "%5D");

                    if (api.getYourself().getConnectedVoiceChannel(server).isEmpty()) {
                        interaction.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                            youtubePlayer(api, audioConnection, trackUrl, slashCommandCreateEvent, loopVar);
                        });
                    } else {
                        AudioConnection audioConnection = server.getAudioConnection().get();
                        youtubePlayer(api, audioConnection, trackUrl, slashCommandCreateEvent, loopVar);
                    }
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
                AtomicReference<String> trackUrl = new AtomicReference<>(interaction.getOptionByName("urlMP3").get().getStringValue().get().replaceAll("\\[", "%5B").replaceAll("]", "%5D"));
                    if (api.getYourself().getConnectedVoiceChannel(server).isEmpty()) {
                        interaction.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                            System.out.println("connecting");
                            mp3Player(api, audioConnection, trackUrl.get(), loopVar, slashCommandCreateEvent,0, server);
                        });
                    } else {
                        AudioConnection audioConnection = server.getAudioConnection().get();
                        System.out.println("already connected");
                        mp3Player(api, audioConnection, trackUrl.get(), loopVar, slashCommandCreateEvent, 0, server);
                    }

            } else if (interaction.getFullCommandName().equals("leave")) {
                if (server.getConnectedVoiceChannel(api.getYourself()).isPresent()) {
                    interaction.createImmediateResponder()
                            .setContent("Leaving voice channel \"" + server.getConnectedVoiceChannel(api.getYourself()).get().getName() + "\"" )
                            .respond()
                            .join();
                        server.getConnectedVoiceChannel(api.getYourself()).get().disconnect();
                } else {
                    interaction.createImmediateResponder()
                            .setContent("I am not connected to a voice channel")
                            .respond()
                            .join();

                }
            } else if (interaction.getFullCommandName().equals("sseblo")) {
                String textToConvert = interaction.getOptionByName("text").get().getStringValue().get();

                String convertedUrl = getUrl(textToConvert);
                interaction.createImmediateResponder().setContent("Playing \"" + textToConvert + "\" with Alyona Flirt ").respond();

                interaction.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                    mp3Player(api,audioConnection,convertedUrl,loopVar,slashCommandCreateEvent,0, server);
                });
            } else if (interaction.getFullCommandName().equals("clear")) {
                long count = interaction.getOptionByName("count").get().getLongValue().get() + 1;
                TextChannel channel = interaction.getChannel().get();

                MessageSet messagesToDelete = channel.getMessages((int) count).join();

                interaction.createImmediateResponder()
                        .setContent("Deleted " + count + " messages")
                        .respond()
                        .join();

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                channel.bulkDelete(messagesToDelete);
            }
        });

        api.addServerVoiceChannelMemberJoinListener(serverVoiceChannelMemberJoinEvent -> {
            Server server = serverVoiceChannelMemberJoinEvent.getServer();

            User user = serverVoiceChannelMemberJoinEvent.getUser();

            /*
            * 998958761618190421L = Sukran = rferee = https://storage.rferee.dev/assets/media/audio/sukran.mp3
            * 394085232266969090L = doka swarm = andorvini = https://storage.rferee.dev/assets/media/audio/dokaswam.mp3
            * 483991031306780683L = yubico = vapronwa = https://storage.rferee.dev/assets/media/audio/v_nalicii_yubico.mp3
            * 731939675438317588 = clown = clown(sasha) = https://storage.rferee.dev/assets/media/audio/clown_short.mp3
             */

            if (user.getId() == 998958761618190421L || user.getId() == 394085232266969090L || user.getId() == 483991031306780683L || user.getId() == 731939675438317588L) {
                serverVoiceChannelMemberJoinEvent.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                    String trackUrl = null;
                    if (user.getId() == 998958761618190421L) {
                        trackUrl = "https://storage.rferee.dev/assets/media/audio/sukran.mp3";
                    } else if (user.getId() == 394085232266969090L) {
                        trackUrl = "https://storage.rferee.dev/assets/media/audio/dokaswam.mp3";
                    } else if (user.getId() == 483991031306780683L) {
                        trackUrl = "https://storage.rferee.dev/assets/media/audio/v_nalicii_yubico.mp3";
                    } else if (user.getId() == 731939675438317588L) {
                        trackUrl = "https://storage.rferee.dev/assets/media/audio/clown_short.mp3";
                    }

                    mp3Player(api, audioConnection, trackUrl, loopVar, null, 1,server);
                });
            }
        });
    }

    // ============= YOUTUBE PLAYER METHOD ===============

    public static void youtubePlayer(DiscordApi api, AudioConnection audioConnection, String trackUrl, SlashCommandCreateEvent slashCommandCreateEvent, AtomicBoolean loopVar){
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
    }

    // =============== MP3 OR OTHER FORMATS PLAYER ===================

    public static void mp3Player(DiscordApi api, AudioConnection audioConnection, String trackUrl, AtomicBoolean loopVar, SlashCommandCreateEvent slashCommandCreateEvent,int a, Server server){
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
                            if (loopVar.get() == true) {
                                player.playTrack(track.makeClone());
                            } else if (a == 1){
                                server.getConnectedVoiceChannel(api.getYourself()).get().disconnect();
                            }
                        }
                    }
                });

                if (a == 0) {
                slashCommandCreateEvent.getInteraction()
                        .respondLater()
                        .thenAccept(message -> {
                            message.setContent("Now playing \"" + track.getInfo().title + "\"").update();
                        });
                }
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
    }




    public static String getUrl(String text) {
        try {

            OkHttpClient okHttpClient = new OkHttpClient();

            String requestUrl = "https://api.sosanie-ebla-bot-premium.vapronva.pw/tts/request/wav";

            RequestBody body = RequestBody.create(MediaType.parse("application/json"),
                    "{\"query\":\"" + text + "\"," +
                            "\"user_id\": 1,"  +
                            "\"voice\": {"+
                            "          \"speakerLang\": \"ru\"," +
                            "          \"speakerName\": \"alyona\"," +
                            "          \"speakerEmotion\": \"flirt\"," +
                            "          \"company\": \"tinkoff\"" +
                            "}}");

            Request.Builder requestBuilder = new Request.Builder()
                    .url(requestUrl)
                    .post(body)
                    .addHeader("X-API-key", System.getenv("API_KEY"));

            Call call = okHttpClient.newCall(requestBuilder.build());

            Response response = call.execute();
            String responseBody = response.body().string();

            return responseBody;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "1";
    }
}