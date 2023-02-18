package site.Andorvini;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import okhttp3.*;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.activity.Activity;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.*;
import org.javacord.api.entity.activity.ActivityType;

import java.awt.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static site.Andorvini.Player.*;

public class Main {
    public static void main(String[] args) {

        // ============== TOKEN PROCCESING =============

        String token = null;
        token = System.getenv("DP_DISCORD_TOKEN");

        String ssEbloApiToken = null;
        ssEbloApiToken = System.getenv("API_KEY");

        if (token == null) {
            System.out.println("[ERROR] DP_DISCORD_TOKEN environment variable not found");
            System.exit(1);
        }

        if (ssEbloApiToken == null) {
            System.out.println("[ERROR] API_KEY environment variable not found");
            System.exit(1);
        }

        // ============== BOT CREATION ==================

        AtomicBoolean loopVar = new AtomicBoolean(false);

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        // ================ ACVTIVITY SET =====================
        api.updateActivity(ActivityType.LISTENING,"\"Antipathy World\"");

        // ================== SLASH COMMAND CREATION ==================
        SlashCommand play =
                SlashCommand.with("play","Play music from provided Youtube URL",
                                Arrays. asList(
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

        SlashCommand np = SlashCommand.with("np","Show what song is playing now")
                .createGlobal(api)
                .join();

        SlashCommand randomPlayer = SlashCommand.with("random","Pick random user")
                .createGlobal(api)
                .join();

        SlashCommand ping = SlashCommand.with("ping", "Ping!")
                .createGlobal(api)
                .join();

        api.addSlashCommandCreateListener(slashCommandCreateEvent -> {
            SlashCommandInteraction interaction = slashCommandCreateEvent.getSlashCommandInteraction();
            Server server = null;
            String fullCommandName = interaction.getFullCommandName();

            Optional<ServerVoiceChannel> optionalUserVoiceChannel = null;
            ServerVoiceChannel userVoiceChannel = null;

            Optional<ServerVoiceChannel> optionalBotVoiceChannel = null;
            ServerVoiceChannel botVoiceChannel = null;

            try {
                server = slashCommandCreateEvent.getInteraction().getServer().get();
                optionalUserVoiceChannel = interaction.getUser().getConnectedVoiceChannel(server);
                userVoiceChannel = optionalUserVoiceChannel.get();

                optionalBotVoiceChannel = api.getYourself().getConnectedVoiceChannel(server);
                botVoiceChannel = optionalBotVoiceChannel.get();
            } catch (NoSuchElementException e) {
                System.out.println("[WARN] Maybe personal messages use");
            }

            if (!interaction.getChannel().get().getType().isServerChannelType()) {
                interaction.createImmediateResponder()
                        .setContent("Use this bot only on server!")
                        .respond()
                        .join();
            } else if (fullCommandName.equals("phony")) {
                if (optionalUserVoiceChannel.isPresent()) {
                    AtomicReference<String> trackUrl = new AtomicReference<>("https://storage.rferee.dev/assets/media/audio/phony-ru.flac");
                    String interactionOption = interaction.getOptionByName("version").get().getStringValue().get();

                    if (interactionOption.equals("rus")) {
                        trackUrl.set("https://storage.rferee.dev/assets/media/audio/phony-ru.flac");
                    } else if (interactionOption.equals("original")) {
                        trackUrl.set("https://storage.rferee.dev/assets/media/audio/phony-jp.flac");
                    }

                    if (optionalBotVoiceChannel.isEmpty()) {
                        Server finalServer = server;
                        userVoiceChannel.connect().thenAccept(audioConnection -> {
                            musicPlayer(api, audioConnection, trackUrl.get(), loopVar, slashCommandCreateEvent,true, finalServer);
                        });
                    } else {
                        AudioConnection audioConnection = server.getAudioConnection().get();
                        musicPlayer(api, audioConnection, trackUrl.get(), loopVar, slashCommandCreateEvent, true, server);
                    }
                } else {
                    respondImmediately(interaction, "You are not connected to a voice channel");
                }
            } else if (fullCommandName.equals("play")) {
                if (optionalUserVoiceChannel.isPresent()) {
                    String trackUrl = interaction.getOptionByName("url").get().getStringValue().get().replaceAll("\\[", "%5B").replaceAll("]", "%5D");

                    if (optionalBotVoiceChannel.isEmpty()) {
                        Server finalServer = server;
                        userVoiceChannel.connect().thenAccept(audioConnection -> {
                            musicPlayer(api, audioConnection, trackUrl, loopVar, slashCommandCreateEvent,true, finalServer);
                        });
                    } else {
                        AudioConnection audioConnection = server.getAudioConnection().get();
                        musicPlayer(api, audioConnection, trackUrl, loopVar, slashCommandCreateEvent,true, server);
                    }
                } else {
                    respondImmediately(interaction, "You are not connected to a voice channel");
                }
            } else if (fullCommandName.equals("loop")) {
                if (loopVar.get() == false) {
                    loopVar.set(true);
                    respondImmediately(interaction,"Looping is now enabled");
                } else {
                    loopVar.set(false);
                    respondImmediately(interaction,"Looping is now disabled");
                }
            } else if (fullCommandName.equals("leave")) {
                if (optionalBotVoiceChannel.isPresent()) {
                    respondImmediately(interaction, "Leaving voice channel \"" + server.getConnectedVoiceChannel(api.getYourself()).get().getName() + "\"");

                    botVoiceChannel.disconnect();
                    stopPlaying();
                } else {
                    respondImmediately(interaction, "I am not connected to a voice channel");
                }
            } else if (fullCommandName.equals("sseblo")) {
                String textToConvert = interaction.getOptionByName("text").get().getStringValue().get();

                String convertedUrl = getUrl(textToConvert);
                respondImmediately(interaction, "Playing \"" + textToConvert + "\" with Alyona Flirt ");

                Server finalServer = server;
                userVoiceChannel.connect().thenAccept(audioConnection -> {
                    musicPlayer(api,audioConnection,convertedUrl,loopVar,slashCommandCreateEvent,true, finalServer);
                });
            } else if (fullCommandName.equals("clear")) {
                long count = interaction.getOptionByName("count").get().getLongValue().get() + 1;
                TextChannel channel = interaction.getChannel().get();

                MessageSet messagesToDelete = channel.getMessages((int) count).join();

                respondImmediately(interaction, "Deleted " + count + " messages");

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                channel.bulkDelete(messagesToDelete);
            } else if (fullCommandName.equals("pause")) {
                if (Player.getPause()) {
                    respondImmediately(interaction, "Unpaused");

                    Player.setPause(false);
                } else {
                    respondImmediately(interaction, "Paused");

                    Player.setPause(true);
                }
            } else if (fullCommandName.equals("np")) {

                AudioTrack audioTrackNowPlaying = Player.getAudioTrackNowPlaying();

                TextChannel channel = interaction.getChannel().get();

                long duration = 0;
                long position = 0;

                String identifier = null;

                if (audioTrackNowPlaying != null) {
                    duration = audioTrackNowPlaying.getDuration();
                    position = audioTrackNowPlaying.getPosition();

                    identifier = audioTrackNowPlaying.getIdentifier();

                    if (identifier.startsWith("http")) {

                    } else {
                        identifier = "https://www.youtube.com/watch?v=" + identifier;
                    }
                }

                EmbedBuilder embed = null;

                if (audioTrackNowPlaying == null) {
                    embed = new EmbedBuilder()
                            .setAuthor("No playing track")
                            .setDescription("Use `/play` command to play track")
                            .setColor(Color.RED);
                } else {
                    embed = new EmbedBuilder()
                            .setAuthor(audioTrackNowPlaying.getInfo().title, identifier, "https://indiefy.net/static/img/landing/distribution/icons/apple_music_icon.png")
                            .setTitle("Duration")
                            .setDescription(formatDuration(position) + " / " + formatDuration(duration))
                            .addField("A field", "__Some text inside the field__")
                            .setColor(Color.ORANGE);
                }

                interaction.createImmediateResponder()
                        .addEmbeds(embed)
                        .respond()
                        .join();
            } else if (fullCommandName.equals("random")) {
                if (optionalUserVoiceChannel.isPresent()) {
                    Set<User> userSet = interaction.getUser().getConnectedVoiceChannel(server).get().getConnectedUsers();

                    User randomUser = userSet.stream().skip(new Random().nextInt(userSet.size())).findFirst().orElse(null);

                    assert randomUser != null;
                    String trackUrl = getUrl(randomUser.getDisplayName(server));

                    respondImmediately(interaction,randomUser.getName());

                    if (optionalBotVoiceChannel.isEmpty()) {
                        Server finalServer = server;
                        interaction.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                            musicPlayer(api, audioConnection, trackUrl, loopVar, slashCommandCreateEvent,false, finalServer);
                        });
                    } else {
                        AudioConnection audioConnection = server.getAudioConnection().get();
                        musicPlayer(api, audioConnection, trackUrl, loopVar, slashCommandCreateEvent,false, server);
                    }
                } else {
                    respondImmediately(interaction, "You are not connected to a voice channel");
                }
            } else if (fullCommandName.equals("ping")) {
                respondImmediately(interaction, "Pong!");
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

            HashMap<Long, String> userAudio = new HashMap<>();
            userAudio.put(998958761618190421L, "https://storage.rferee.dev/assets/media/audio/sukran.mp3");
            userAudio.put(394085232266969090L, "https://storage.rferee.dev/assets/media/audio/dokaswam.mp3");
            userAudio.put(483991031306780683L, "https://storage.rferee.dev/assets/media/audio/v_nalicii_yubico.mp3");
            userAudio.put(731939675438317588L, "https://storage.rferee.dev/assets/media/audio/clown_short.mp3");

            if (userAudio.containsKey(user.getId())) {
                String trackUrl = userAudio.get(user.getId());
                if (api.getYourself().getConnectedVoiceChannel(server).isEmpty()) {
                    String finalTrackUrl = trackUrl;
                    serverVoiceChannelMemberJoinEvent.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                        musicPlayer(api, audioConnection, finalTrackUrl, loopVar, null, false, server);
                    });
                } else {
                    AudioConnection audioConnection = server.getAudioConnection().get();
                    musicPlayer(api, audioConnection, trackUrl, loopVar, null, false, server);
                }
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

    public static String formatDuration(long millis) {
        // Convert milliseconds to minutes and seconds
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);

        // Format the duration as a string
        return String.format("%d:%02d", minutes, seconds);
    }

    public static void respondImmediately(SlashCommandInteraction interaction,String text){
        interaction.createImmediateResponder()
                .setContent(text)
                .respond()
                .join();
    }
}