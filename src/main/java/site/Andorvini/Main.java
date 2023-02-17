package site.Andorvini;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import okhttp3.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.*;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static site.Andorvini.Player.*;

public class Main {
    public static void main(String[] args) {

        String token = null;
        token = System.getenv("DP_DISCORD_TOKEN");

        String ssEbloApiToken = null;
        ssEbloApiToken = System.getenv("API_KEY");

        if (token == null) {
            System.out.println("DP_DISCORD_TOKEN environment variable not set");
            System.exit(1);
        }

        if (ssEbloApiToken == null) {
            System.out.println("API_KEY environment variable not set");
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

        api.addSlashCommandCreateListener(slashCommandCreateEvent -> {
            SlashCommandInteraction interaction = slashCommandCreateEvent.getSlashCommandInteraction();
            Server server = slashCommandCreateEvent.getInteraction().getServer().get();
            String fullCommandName = interaction.getFullCommandName();

            if (fullCommandName.equals("phony")) {
                if (interaction.getUser().getConnectedVoiceChannel(server).isPresent()) {
                    AtomicReference<String> trackUrl = new AtomicReference<>("https://storage.rferee.dev/assets/media/audio/phony-ru.flac");
                    String interactionOption = interaction.getOptionByName("version").get().getStringValue().get();

                    if (interactionOption.equals("rus")) {
                        trackUrl.set("https://storage.rferee.dev/assets/media/audio/phony-ru.flac");
                    } else if (interactionOption.equals("original")) {
                        trackUrl.set("https://storage.rferee.dev/assets/media/audio/phony-jp.flac");
                    }

                    if (api.getYourself().getConnectedVoiceChannel(server).isEmpty()) {
                        interaction.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                            musicPlayer(api, audioConnection, trackUrl.get(), loopVar, slashCommandCreateEvent,0, server);
                        });
                    } else {
                        AudioConnection audioConnection = server.getAudioConnection().get();
                        musicPlayer(api, audioConnection, trackUrl.get(), loopVar, slashCommandCreateEvent, 0, server);
                    }
                } else {
                    respondImmediately(interaction, "You are not connected to a voice channel");
                }
            } else if (fullCommandName.equals("play")) {
                if (interaction.getUser().getConnectedVoiceChannel(server).isPresent()) {
                    String trackUrl = interaction.getOptionByName("url").get().getStringValue().get().replaceAll("\\[", "%5B").replaceAll("]", "%5D");

                    if (api.getYourself().getConnectedVoiceChannel(server).isEmpty()) {
                        interaction.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                            musicPlayer(api, audioConnection, trackUrl, loopVar, slashCommandCreateEvent,0, server);
                        });
                    } else {
                        AudioConnection audioConnection = server.getAudioConnection().get();
                        musicPlayer(api, audioConnection, trackUrl, loopVar, slashCommandCreateEvent,0, server);
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
                if (server.getConnectedVoiceChannel(api.getYourself()).isPresent()) {
                    respondImmediately(interaction, "Leaving voice channel \"" + server.getConnectedVoiceChannel(api.getYourself()).get().getName() + "\"");

                    server.getConnectedVoiceChannel(api.getYourself()).get().disconnect();
                    stopPlaying();
                } else {
                    respondImmediately(interaction, "I am not connected to a voice channel");
                }
            } else if (fullCommandName.equals("sseblo")) {
                String textToConvert = interaction.getOptionByName("text").get().getStringValue().get();

                String convertedUrl = getUrl(textToConvert);
                interaction.createImmediateResponder().setContent("Playing \"" + textToConvert + "\" with Alyona Flirt ").respond();

                interaction.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                    musicPlayer(api,audioConnection,convertedUrl,loopVar,slashCommandCreateEvent,0, server);
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

                long duration = audioTrackNowPlaying.getDuration();
                long position = audioTrackNowPlaying.getPosition();

                String identifier = audioTrackNowPlaying.getIdentifier();
                if (identifier.startsWith("http")) {

                } else {
                    identifier = "https://www.youtube.com/watch?v=" + identifier;
                }

                EmbedBuilder embed = new EmbedBuilder()
                        .setAuthor(audioTrackNowPlaying.getInfo().title, identifier , "https://indiefy.net/static/img/landing/distribution/icons/apple_music_icon.png")
                        .setTitle("Duration")
                        .setDescription(formatDuration(position) + " / " + formatDuration(duration))
                        .addField("A field", "__Some text inside the field__")
                        .setColor(Color.ORANGE);

                interaction.createImmediateResponder()
                        .addEmbeds(embed)
                        .respond()
                        .join();
            } else if (fullCommandName.equals("random")) {
                if (interaction.getUser().getConnectedVoiceChannel(server).isPresent()) {
                    Set<User> userSet = interaction.getUser().getConnectedVoiceChannel(server).get().getConnectedUsers();

                    User randomUser = userSet.stream().skip(new Random().nextInt(userSet.size())).findFirst().orElse(null);

                    assert randomUser != null;
                    String trackUrl = getUrl(randomUser.getDisplayName(server));

                    if (api.getYourself().getConnectedVoiceChannel(server).isEmpty()) {
                        interaction.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                            musicPlayer(api, audioConnection, trackUrl, loopVar, slashCommandCreateEvent,0, server);
                        });
                    } else {
                        AudioConnection audioConnection = server.getAudioConnection().get();
                        musicPlayer(api, audioConnection, trackUrl, loopVar, slashCommandCreateEvent,0, server);
                    }
                } else {
                    respondImmediately(interaction, "You are not connected to a voice channel");
                }
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
                if (api.getYourself().getConnectedVoiceChannel(server).isEmpty()) {
                    String finalTrackUrl = trackUrl;
                    serverVoiceChannelMemberJoinEvent.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                        musicPlayer(api, audioConnection, finalTrackUrl, loopVar, null,1, server);
                    });
                } else {
                    AudioConnection audioConnection = server.getAudioConnection().get();
                    musicPlayer(api, audioConnection, trackUrl, loopVar, null,1, server);
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