package site.andorvini;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import okhttp3.*;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.*;
import org.javacord.api.entity.activity.ActivityType;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static site.andorvini.GreetingPlayer.greetingPlayer;
import static site.andorvini.Player.*;

public class Main {

    private static TextChannel lastCommandChannel;

    public static TextChannel getTextChannel(){
        return lastCommandChannel;
    }

    public static void main(String[] args) {

        /*
         * 998958761618190421L = Sukran = rferee = https://storage.rferee.dev/assets/media/audio/sukran.mp3
         * 394085232266969090L = doka swarm = andorvini = https://storage.rferee.dev/assets/media/audio/dokaswam.mp3
         * 483991031306780683L = yubico = vapronwa = https://storage.rferee.dev/assets/media/audio/v_nalicii_yubico.mp3
         * 731939675438317588L = clown = clown(sasha) = https://storage.rferee.dev/assets/media/audio/clown_short.mp3
         * 412537382152306688L = Dogs in the house with huge bASS = ThisPilot = https://storage.rferee.dev/assets/media/audio/pilot.mp3
         */

        HashMap<Long, String> userAudio = new HashMap<>();
        userAudio.put(998958761618190421L, "https://storage.rferee.dev/assets/media/audio/sukran.mp3");
        userAudio.put(394085232266969090L, "https://storage.rferee.dev/assets/media/audio/dokaswam.mp3");
        userAudio.put(483991031306780683L, "https://storage.rferee.dev/assets/media/audio/v_nalicii_yubico.mp3");
        userAudio.put(731939675438317588L, "https://storage.rferee.dev/assets/media/audio/clown_short.mp3");
        userAudio.put(412537382152306688L, "https://storage.rferee.dev/assets/media/audio/pilot.mp3");

        // ============== TOKEN PROCCESING =============

        String token = null;
        token = System.getenv("DP_DISCORD_TOKEN");

        String ssEbloApiToken = null;
        ssEbloApiToken = System.getenv("DP_SOSANIE_API_KEY");

        String youtubeApiToken = null;
        youtubeApiToken = System.getenv("DP_YOUTUBE_API_KEY");

        String youtubeLogin = null;
        youtubeLogin = System.getenv("DP_YOUTUBE_LOGIN");

        String youtubePassword = null;
        youtubePassword = System.getenv("DP_YOUTUBE_PASSWORD");

        if (token == null) {
            System.out.println("[ERROR] DP_DISCORD_TOKEN environment variable not found");
            System.exit(1);
        }

        if (ssEbloApiToken == null) {
            System.out.println("[ERROR] API_KEY environment variable not found");
            System.exit(1);
        }

        if (youtubeApiToken == null) {
            System.out.println("[ERROR] YOUTUBE_API_KEY environment variable not found");
            System.exit(1);
        }

        if (youtubeLogin == null) {
            System.out.println("[ERROR] DP_YOUTUBE_LOGIN environment variable not found");
            System.exit(1);
        }

        if (youtubePassword == null) {
            System.out.println("[ERROR] DP_YOUTUBE_PASSWORD environment variable not found");
            System.exit(1);
        }

        // ============== BOT CREATION ==================

        AtomicBoolean loopVar = new AtomicBoolean(false);
        AtomicBoolean isPlaying = new AtomicBoolean(false);

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        Player.addOnTrackEndEventToPlayer();
        GreetingPlayer.addOnTrackEndToGreetingPlayer();

        // ================ ACVTIVITY SET =====================
        api.updateActivity(ActivityType.LISTENING,"\"Antipathy World\"");

        // ================== SLASH COMMAND CREATION ==================
        SlashCommand playCommand =
                SlashCommand.with("play","Play music from provided Youtube URL",
                                Arrays. asList(
                                        SlashCommandOption.create(SlashCommandOptionType.STRING, "query", "Link to music, or just name", true)
                                ))
                .createGlobal(api)
                .join();

        SlashCommand phonyCommand =
                SlashCommand.with("phony", "Play ANTIPATHY WORLD",
                                Arrays.asList(
                                            SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "version", "Choose version of song", true,
                                                    Arrays.asList(
                                                            SlashCommandOptionChoice.create("Russian remix", "rus"),
                                                            SlashCommandOptionChoice.create("Original", "original")))
                                            ))
                        .createGlobal(api)
                        .join();

        SlashCommand loopCommand = SlashCommand.with("loop","Lop music")
                .createGlobal(api)
                .join();

        SlashCommand leaveCommand =
                SlashCommand.with("leave","Leave voice channel")
                .createGlobal(api)
                .join();

        SlashCommand pauseCommand = SlashCommand.with("pause","Pause music")
                .createGlobal(api)
                .join();

        SlashCommand ssebloCommand = SlashCommand.with("sseblo","Convert text into voice using sseblobotapi",
                Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "text", "Text you want to voice", true)
                ))
                .createGlobal(api)
                .join();

        SlashCommand clearCommand = SlashCommand.with("clear","Delete specified number of messages",
                Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.LONG,"count","Message count",true)
                ))
                .createGlobal(api)
                .join();

        SlashCommand npCommand = SlashCommand.with("np","Show what song is playing now")
                .createGlobal(api)
                .join();

        SlashCommand randomUserCommand = SlashCommand.with("random","Pick random user")
                .createGlobal(api)
                .join();

        SlashCommand pingCommand = SlashCommand.with("ping", "Ping!")
                .createGlobal(api)
                .join();

        SlashCommand queueCommand = SlashCommand.with("queue", "Shows all tracks in queue")
                .createGlobal(api)
                .join();

        SlashCommand lyricsCommand = SlashCommand.with("lyrics", "Shows lyrics of currently playing track")
                .createGlobal(api)
                .join();

        SlashCommand volumeCommand = SlashCommand.with("volume","Set the volume",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.LONG, "volumelvl", "Volume level (Max. 1000)", true)
                        ))
                .createGlobal(api)
                .join();

        SlashCommand skipCommand = SlashCommand.with("skip", "Skips currently playing track")
                .createGlobal(api)
                .join();

        SlashCommand seekCommand = SlashCommand.with("seek", "Seeks the song to specified position",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "position", "Positon to seek", true)
                        ))
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

            if (Player.getAudioTrackNowPlaying() == null) {
                isPlaying.set(false);
            } else {
                isPlaying.set(true);
            }

            try {
                server = slashCommandCreateEvent.getInteraction().getServer().get();
                optionalUserVoiceChannel = interaction.getUser().getConnectedVoiceChannel(server);
                userVoiceChannel = optionalUserVoiceChannel.get();

                optionalBotVoiceChannel = api.getYourself().getConnectedVoiceChannel(server);
                botVoiceChannel = optionalBotVoiceChannel.get();
            } catch (NoSuchElementException e) {
                System.out.println("[WARN] Maybe personal messages use or no value present");
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

                    lastCommandChannel = interaction.getChannel().get();

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
                    respondImmediatelyWithString(interaction, "You are not connected to a voice channel");
                }
            } else if (fullCommandName.equals("play")) {
                if (optionalUserVoiceChannel.isPresent()) {
                    String commandOption = interaction.getOptionByName("query").get().getStringValue().get().replaceAll("\\[", "%5B").replaceAll("]", "%5D");
                    String trackUrl = null;

                    System.out.println("CUMMMMMMMMMMMMMMMM");

                    lastCommandChannel = interaction.getChannel().get();

                    if (isUrl(commandOption)) {
                        trackUrl = commandOption;
                    } else {
                        try {
                            trackUrl = Queue.getVideoUrlFromName(commandOption);
                        } catch (IOException ignored){}
                    }

                    Queue.addTrackToQueue(trackUrl);

                    if (Queue.getQueueList().size() > 1) {
                        EmbedBuilder embed;

                        if (isYouTubeLink(trackUrl)) {
                            String title = null;
                            String duration = null;

                            try {
                                title = Queue.getYoutubeVideoTitleFromUrl(trackUrl, true);
                                duration = Queue.getYoutubeVideoTitleFromUrl(trackUrl, false);
                            } catch (IOException ignored) {}

                            embed = new EmbedBuilder()
                                    .setAuthor("Added to queue: ")
                                    .addField("", "[" + title + "](" + trackUrl + ") | `" + duration + "`")
                                    .setColor(Color.GREEN)
                                    .setFooter("Track in queue: " + Queue.getQueueList().size());

                        } else {
                            embed = new EmbedBuilder()
                                    .setAuthor("Added to queue: ")
                                    .addField("", trackUrl)
                                    .setColor(Color.GREEN)
                                    .setFooter("Track in queue: " + Queue.getQueueList().size());
                        }
                        respondImmediatelyWithEmbed(interaction, embed);
                    }

                    if (optionalBotVoiceChannel.isEmpty()) {
                        Server finalServer = server;
                        userVoiceChannel.connect().thenAccept(audioConnection -> {
                            Queue.queueController(api, audioConnection, loopVar, slashCommandCreateEvent,true, finalServer);
                        });
                    } else {
                        AudioConnection audioConnection = server.getAudioConnection().get();
                        Queue.queueController(api, audioConnection, loopVar, slashCommandCreateEvent,true, server);
                    }
                } else {
                    respondImmediatelyWithString(interaction, "You are not connected to a voice channel");
                }
            } else if (fullCommandName.equals("loop")) {
                if (loopVar.get() == false) {
                    loopVar.set(true);
                    respondImmediatelyWithString(interaction,"Looping is now enabled");
                } else {
                    loopVar.set(false);
                    respondImmediatelyWithString(interaction,"Looping is now disabled");
                }
            } else if (fullCommandName.equals("leave")) {
                if (optionalBotVoiceChannel.isPresent()) {
                    respondImmediatelyWithString(interaction, "Leaving voice channel \"" + server.getConnectedVoiceChannel(api.getYourself()).get().getName() + "\"");

                    botVoiceChannel.disconnect();
                    stopPlaying();
                    Queue.clearQueue();
                } else {
                    respondImmediatelyWithString(interaction, "I am not connected to a voice channel");
                }
            } else if (fullCommandName.equals("sseblo")) {
                lastCommandChannel = interaction.getChannel().get();

                String textToConvert = interaction.getOptionByName("text").get().getStringValue().get();

                String convertedUrl = getSosaniaEblaUrl(textToConvert);
                respondImmediatelyWithString(interaction, "Playing \"" + textToConvert + "\" with Alyona Flirt ");

                Server finalServer = server;
                userVoiceChannel.connect().thenAccept(audioConnection -> {
                    musicPlayer(api, audioConnection, convertedUrl, loopVar, slashCommandCreateEvent, true,     finalServer);
                });
            } else if (fullCommandName.equals("clear")) {
                long count = interaction.getOptionByName("count").get().getLongValue().get() + 1;
                TextChannel channel = interaction.getChannel().get();

                MessageSet messagesToDelete = channel.getMessages((int) count).join();

                respondImmediatelyWithString(interaction, "Deleted " + count + " messages");

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                channel.bulkDelete(messagesToDelete);
            } else if (fullCommandName.equals("pause")) {
                if (Player.getPause()) {
                    respondImmediatelyWithString(interaction, "Unpaused");

                    setPause(false);
                } else {
                    respondImmediatelyWithString(interaction, "Paused");

                    setPause(true);
                }
            } else if (fullCommandName.equals("np")) {
                AudioTrack audioTrackNowPlaying = Player.getAudioTrackNowPlaying();

                String loop = null;
                String pause = null;

                long duration = 0;
                long position = 0;
                int volume = 0;

                String identifier = null;

                if (loopVar.get()) {
                    loop = "Loop enabled";
                } else {
                    loop = "Loop disabled";
                }

                if (Player.getPause()) {
                    pause = "Now paused";
                } else {
                    pause = "Playing";
                }

                if (audioTrackNowPlaying != null) {
                    duration = audioTrackNowPlaying.getDuration();
                    position = audioTrackNowPlaying.getPosition();
                    volume = Player.getVolume();

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
                            .addInlineField("Volume", String.valueOf(volume))
                            .addInlineField("Loop", loop)
                            .addInlineField("Pause", pause)
                            .setColor(Color.ORANGE);
                }

                interaction.createImmediateResponder()
                        .addEmbeds(embed)
                        .respond()
                        .join();
            } else if (fullCommandName.equals("random")) {
                lastCommandChannel = interaction.getChannel().get();
                if (optionalUserVoiceChannel.isPresent()) {
                    Set<User> userSet = interaction.getUser().getConnectedVoiceChannel(server).get().getConnectedUsers();

                    User randomUser = userSet.stream().skip(new Random().nextInt(userSet.size())).findFirst().orElse(null);

                    assert randomUser != null;
                    String trackUrl = getSosaniaEblaUrl(randomUser.getDisplayName(server));

                    respondImmediatelyWithString(interaction,randomUser.getName());

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
                    respondImmediatelyWithString(interaction, "You are not connected to a voice channel");
                }
            } else if (fullCommandName.equals("ping")) {
                respondImmediatelyWithString(interaction, "Pong!");
            } else if (fullCommandName.equals("lyrics")) {
                respondImmediatelyWithString(interaction, "Not implemented yet. (Because musixmatch shit)");
            } else if (fullCommandName.equals("volume")) {
                Long volumeLevel = interaction.getOptionByName("volumelvl").get().getLongValue().get();
                int volumeBefore = Player.getVolume();
                setVolume(volumeLevel);
                String trackUrl = "https://storage.rferee.dev/assets/media/audio/alyona_volume_warning.wav";
                AudioConnection audioConnection = server.getAudioConnection().get();

                if (volumeLevel - volumeBefore >= 100) {
                    setPause(true);
                    greetingPlayer(api, audioConnection, trackUrl, loopVar, null, false, server);
                }

                if (volumeLevel > 900) {
                    respondImmediatelyWithString(interaction, "ТЫ ЧЕ ЕБАНУТЫЙ? КАКОЙ " + volumeLevel + "? ТЕБЕ ЧЕ ЖИТЬ НАДОЕЛО?");
                } else {
                    respondImmediatelyWithString(interaction, "Volume set to " + volumeLevel);
                }
            } else if (fullCommandName.equals("queue")) {
                try {
                    respondImmediatelyWithEmbed(interaction, Queue.getQueueEmbed());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (fullCommandName.equals("skip")) {
                try {
                    String secondTrackInQueue = null;

                    int i = 0;
                    for (String trackUrl : Queue.getQueueList()) {
                        if (i == 1) {
                            secondTrackInQueue = trackUrl;
                            break;
                        }
                        i++;
                    }

                    EmbedBuilder skipEmbed = new EmbedBuilder()
                            .addInlineField("__**Skipping:**__ ", "[" + Queue.getYoutubeVideoTitleFromUrl(Queue.getQueueList().peek(), true) + "](" + Queue.getQueueList().peek() + ")")
                            .setColor(Color.GREEN);

                    if (secondTrackInQueue != null) {
                        skipEmbed.addInlineField("__**Next track:**__ ", "[" + Queue.getYoutubeVideoTitleFromUrl(secondTrackInQueue, true) + "](" + secondTrackInQueue + ")");
                    } else {
                        skipEmbed.addInlineField("__**No next track :(**__","");
                    }

                    respondImmediatelyWithEmbed(interaction, skipEmbed);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (optionalBotVoiceChannel.isEmpty()) {
                    Server finalServer = server;
                    interaction.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                        Queue.skipTrack (api, audioConnection, loopVar, slashCommandCreateEvent,true, finalServer, isPlaying);
                    });
                } else {
                    AudioConnection audioConnection = server.getAudioConnection().get();
                    Queue.skipTrack(api, audioConnection, loopVar, slashCommandCreateEvent,true, server, isPlaying);
                }
            } else if (fullCommandName.equals("seek")) {
                String position = interaction.getOptionByName("position").get().getStringValue().get();

                if (isValidTimeFormat(position)) {
                    long milis = convertToMilliseconds(position);
                    if (!(milis > Player.getAudioTrackNowPlaying().getDuration())) {
                        EmbedBuilder seekEmbed = new EmbedBuilder()
                                .addField("__**Seeking track: **__", getAudioTrackNowPlaying().getInfo().title)
                                .addInlineField("Previous position: ", "`" + formatDuration(getAudioTrackNowPlaying().getPosition()) + "`")
                                .addInlineField("New position: ", "`" + position + "`")
                                .setColor(Color.GREEN);

                        Player.setPosition(milis);
                        respondImmediatelyWithEmbed(interaction, seekEmbed);
                    } else {
                        EmbedBuilder seekLongFailureEmbed = new EmbedBuilder()
                                .addField("__**Incorrect position**__", "Track duration is `" + formatDuration(Player.getAudioTrackNowPlaying().getDuration()) + "`")
                                .setColor(Color.red);

                        respondImmediatelyWithEmbed(interaction, seekLongFailureEmbed);
                    }
                } else {
                    EmbedBuilder seekFailureEmbed = new EmbedBuilder()
                            .addField("__**Incorrect position format**__", "Use `minutes:seconds` format")
                            .setColor(Color.red);

                    respondImmediatelyWithEmbed(interaction, seekFailureEmbed);
                }
            }
        });

        api.addServerVoiceChannelMemberJoinListener(serverVoiceChannelMemberJoinEvent -> {
            Server server = serverVoiceChannelMemberJoinEvent.getServer();
            User user = serverVoiceChannelMemberJoinEvent.getUser();

            if (AloneInChannelHandler.isAloneTimerRunning()){
                if (serverVoiceChannelMemberJoinEvent.getChannel().getId() == AloneInChannelHandler.getVoiceChannel().getId()) {
                    AloneInChannelHandler.stopAloneTimer();
                }
            }

            if (userAudio.containsKey(user.getId())) {
                String trackUrl = userAudio.get(user.getId());
                if (api.getYourself().getConnectedVoiceChannel(server).isEmpty()) {
                    String finalTrackUrl = trackUrl;
                    setPause(true);
                    serverVoiceChannelMemberJoinEvent.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                        greetingPlayer(api, audioConnection, finalTrackUrl, loopVar, null, false, server);
                    });
                } else {
                    setPause(true);
                    AudioConnection audioConnection = server.getAudioConnection().get();
                    greetingPlayer(api, audioConnection, trackUrl, loopVar, null, false, server);
                }
            }
        });

        api.addServerVoiceChannelMemberLeaveListener(serverVoiceChannelMemberLeaveEvent -> {
            Server server = serverVoiceChannelMemberLeaveEvent.getServer();
            ServerVoiceChannel channel = serverVoiceChannelMemberLeaveEvent.getChannel();

            if (api.getYourself().getConnectedVoiceChannel(server).isPresent() && api.getYourself().getConnectedVoiceChannel(server).get() == channel) {

                Set<User> users = channel.getConnectedUsers();
                int usersInChannel = users.size();

                if (serverVoiceChannelMemberLeaveEvent.getUser().getId() != api.getYourself().getId()) {
                    if (serverVoiceChannelMemberLeaveEvent.getUser().getId() != 1074801519523807252L) {
                        System.out.println("Not myself and not Prod bot");
                        if (usersInChannel == 1) {
                            AloneInChannelHandler.startAloneTimer(lastCommandChannel, server, api, "I'm alone :(", channel);
                        }
                    }
                }
            }
        });

    }

    public static String getSosaniaEblaUrl(String text) {
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
                    .addHeader("X-API-key", System.getenv("DP_SOSANIE_API_KEY"));

            Call call = okHttpClient.newCall(requestBuilder.build());

            Response response = call.execute();
            String responseBody = response.body().string();

            return responseBody;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String formatDuration(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);

        return String.format("%d:%02d", minutes, seconds);
    }

    public static void respondImmediatelyWithString(SlashCommandInteraction interaction, String text){
        interaction.createImmediateResponder()
                .setContent(text)
                .respond()
                .join();
    }

    public static void respondImmediatelyWithEmbed(SlashCommandInteraction interaction,EmbedBuilder embed){
        interaction.createImmediateResponder()
                .addEmbeds(embed)
                .respond()
                .join();
    }

    public static boolean isYouTubeLink(String str) {
        String pattern = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+";
        Pattern youtubePattern = Pattern.compile(pattern);
        Matcher matcher = youtubePattern.matcher(str);
        return matcher.matches();
    }

    public static boolean isUrl(String str) {
        Pattern urlPattern = Pattern.compile("^((https?|ftp)://)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(/.*)?$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = urlPattern.matcher(str);
        return matcher.matches();
    }

    public static long convertToMilliseconds(String time) {
        String[] tokens = time.split(":");
        int minutes = Integer.parseInt(tokens[0]);
        int seconds = Integer.parseInt(tokens[1]);
        return (minutes * 60 + seconds) * 1000;
    }

    public static boolean isValidTimeFormat(String time) {
        String regex = "^\\d+:[0-5]\\d$";
        return time.matches(regex);
    }

}
