package site.andorvini;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

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

import site.andorvini.commands.DevCommand;
import site.andorvini.handlers.ButtonHandler;
import site.andorvini.miscellaneous.AloneInChannelHandler;
import site.andorvini.miscellaneous.BatteryChanger;
import site.andorvini.miscellaneous.MiscMethods;
import site.andorvini.miscellaneous.YoutubeMethods;
import site.andorvini.players.GreetingPlayer;
import site.andorvini.players.Player;
import site.andorvini.queue.Queue;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static site.andorvini.miscellaneous.MiscMethods.*;
import static site.andorvini.miscellaneous.SosanieEblaMethod.getSosaniaEblaUrl;
import static site.andorvini.miscellaneous.YoutubeMethods.*;


public class Main {

    //  ============ Variables Declaration ============

    private static TextChannel lastCommandChannel;

    private static HashMap<Long, Player> players = new HashMap<>();
    private static HashMap<Long, site.andorvini.queue.Queue> queues = new HashMap<>();
    private static HashMap<Long, GreetingPlayer> greetingPlayers = new HashMap<>();
    private static HashMap<Long, AloneInChannelHandler> aloneInChannelHandlers = new HashMap<>();

    private static DiscordApi apiGlobal;

    // ============ Setters ============

    public static void removePlayerFromPlayers(Long serverId){
        players.remove(serverId);
    }

    public static void removeQueue(Long serverId){
        queues.remove(serverId);
    }

    public static void removeGreetingPlayer(Long serverId){
        greetingPlayers.remove(serverId);
    }

    // ============ Getters ============

    public static HashMap<Long, Player> getPlayers() {
        return players;
    }

    public static HashMap<Long, GreetingPlayer> getGreetingPlayers() {
        return greetingPlayers;
    }

    public static HashMap<Long, Queue> getQueues() {
        return queues;
    }

    public static TextChannel getLastTextChannel(){
        return lastCommandChannel;
    }

    public static DiscordApi getApi(){
        return apiGlobal;
    }

    // ============ Main ============

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

        // ============ TOKEN PROCESSING ============

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

        // ============ BOT CREATION ============

        AtomicBoolean isPlaying = new AtomicBoolean(false);

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        apiGlobal = api;

        ButtonHandler.buttonHandler();

        // ============ ACTIVITY SET ============
        api.updateActivity(ActivityType.LISTENING,"\"Antipathy World\"");

        // ============ SLASH COMMAND CREATION ============
        SlashCommandsRegister.registerSlashCommands(api);

        api.addSlashCommandCreateListener(slashCommandCreateEvent -> {
            SlashCommandInteraction interaction = slashCommandCreateEvent.getSlashCommandInteraction();
            Server interactionServer = null;
            Long interactionServerId = null;
            String fullCommandName = interaction.getFullCommandName();

            Optional<ServerVoiceChannel> optionalUserVoiceChannel = null;
            ServerVoiceChannel userVoiceChannel = null;

            Optional<ServerVoiceChannel> optionalBotVoiceChannel = null;
            ServerVoiceChannel botVoiceChannel = null;

            try {
                interactionServer = slashCommandCreateEvent.getInteraction().getServer().get();
                interactionServerId = interactionServer.getId();

                if (!(queues.containsKey(interactionServerId))) {
                    queues.put(interactionServerId, new site.andorvini.queue.Queue());
                }

                if (!greetingPlayers.containsKey(interactionServerId)) {
                    greetingPlayers.put(interactionServerId, new GreetingPlayer());
                }

                if (!players.containsKey(interactionServerId)) {
                    players.put(interactionServerId, new Player());
                }

                optionalUserVoiceChannel = interaction.getUser().getConnectedVoiceChannel(interactionServer);
                userVoiceChannel = optionalUserVoiceChannel.get();

                optionalBotVoiceChannel = api.getYourself().getConnectedVoiceChannel(interactionServer);
                botVoiceChannel = optionalBotVoiceChannel.get();

            } catch (NoSuchElementException ignored) {}

            site.andorvini.queue.Queue currentQueue = queues.get(interactionServerId);
            GreetingPlayer currentGreetingPlayer = greetingPlayers.get(interactionServerId);
            Player currentPlayer = players.get(interactionServerId);

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

                    currentPlayer.setPause(true);

                    if (optionalBotVoiceChannel.isEmpty()) {
                        Server finalServer = interactionServer;
                        userVoiceChannel.connect().thenAccept(audioConnection -> {
                            currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl.get(), slashCommandCreateEvent,true, finalServer, currentPlayer, false);
                        });
                    } else {
                        AudioConnection audioConnection = interactionServer.getAudioConnection().get();
                        currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl.get(), slashCommandCreateEvent, true, interactionServer, currentPlayer, false);
                    }
                } else {
                    respondImmediatelyWithString(interaction, "You are not connected to a voice channel");
                }
            } else if (fullCommandName.equals("play")) {
                if (optionalUserVoiceChannel.isPresent()) {
                    String commandOption = interaction.getOptionByName("query").get().getStringValue().get().replaceAll("\\[", "%5B").replaceAll("]", "%5D");
                    String trackUrl = null;

                    lastCommandChannel = interaction.getChannel().get();

                    if (isUrl(commandOption)) {
                        trackUrl = commandOption;
                    } else {
                        try {
                            trackUrl = getVideoUrlFromName(commandOption);
                        } catch (Exception e){
                            EmbedBuilder searchFailEmbed = new EmbedBuilder()
                                .setColor(Color.RED)
                                    .setAuthor("Nothing was found with your query")
                                    .addField("Try another query", "");

                            MiscMethods.respondImmediatelyWithEmbed(interaction, searchFailEmbed);
                        }
                    }

                    if (trackUrl != null) {
                        currentQueue.addTrackToQueue(trackUrl);
                        if (currentQueue.getQueueList().size() > 1) {
                            EmbedBuilder embed;

                            if (isYouTubeLink(trackUrl)) {
                                String title = null;
                                String duration = null;

                                try {
                                    title = getYoutubeVideoTitleFromUrl(trackUrl, true);
                                    duration = getYoutubeVideoTitleFromUrl(trackUrl, false);
                                } catch (IOException ignored) {
                                }

                                embed = new EmbedBuilder()
                                        .setAuthor("Added to queue: ")
                                        .addField("", "[" + title + "](" + trackUrl + ") | `" + duration + "`")
                                        .setColor(Color.GREEN)
                                        .setFooter("Track in queue: " + currentQueue.getQueueList().size());

                            } else {
                                embed = new EmbedBuilder()
                                        .setAuthor("Added to queue: ")
                                        .addField("", trackUrl)
                                        .setColor(Color.GREEN)
                                        .setFooter("Track in queue: " + currentQueue.getQueueList().size());
                            }

                            interaction.createImmediateResponder()
                                    .addEmbed(embed)
//                                    .addComponents(ActionRow.of(Button.success("pause", "⏸"),
//                                            Button.create("skip", ButtonStyle.SUCCESS, "Skip")))
                                    .respond()
                                    .join();
                        } else {
                            try {
                                if (isYouTubeLink(trackUrl)) {
                                    EmbedBuilder playEmbed = new EmbedBuilder()
                                            .setAuthor("Playing: ")
                                            .addField("", "[" + YoutubeMethods.getYoutubeVideoTitleFromUrl(trackUrl, true) + "](" + trackUrl + ") | `" + getYoutubeVideoTitleFromUrl(trackUrl, false) + "`")
                                            .setColor(Color.GREEN)
                                            .setFooter("Track in queue: " + currentQueue.getQueueList().size());

                                    respondImmediatelyWithEmbed(interaction, playEmbed);
                                } else {
                                    EmbedBuilder playEmbed = new EmbedBuilder()
                                            .setAuthor("Playing: ")
                                            .addField("", trackUrl)
                                            .setColor(Color.GREEN)
                                            .setFooter("Track in queue: " + currentQueue.getQueueList().size());

                                    respondImmediatelyWithEmbed(interaction, playEmbed);
                                }
                            } catch (IOException ignored){}
                        }
                    }

                    if (optionalBotVoiceChannel.isEmpty()) {
                        Server finalServer = interactionServer;
                        userVoiceChannel.connect().thenAccept(audioConnection -> {
                            currentQueue.queueController(api, audioConnection, slashCommandCreateEvent,true, finalServer, currentPlayer);
                        });
                    } else {
                        AudioConnection audioConnection = interactionServer.getAudioConnection().get();
                        currentQueue.queueController(api, audioConnection, slashCommandCreateEvent,true, interactionServer, currentPlayer);
                    }
                } else {
                    respondImmediatelyWithString(interaction, "You are not connected to a voice channel");
                }
            } else if (fullCommandName.equals("loop")) {
                if (!currentPlayer.getLoopVar()){
                    currentPlayer.setLoopVar(true);
                    respondImmediatelyWithString(interaction,"Looping is now enabled");
                } else {
                    currentPlayer.setLoopVar(false);
                    respondImmediatelyWithString(interaction,"Looping is now disabled");
                }
            } else if (fullCommandName.equals("leave")) {
                if (optionalBotVoiceChannel.isPresent()) {
                    respondImmediatelyWithString(interaction, "Leaving voice channel \"" + interactionServer.getConnectedVoiceChannel(api.getYourself()).get().getName() + "\"");

                    botVoiceChannel.disconnect();
                    players.remove(interactionServerId);
                    currentPlayer.destroyPlayer();
                    currentQueue.clearQueue();
                    AloneInChannelHandler.stopAloneTimer(false);

                    if (BatteryChanger.getIsFireAlarmSystemEnabled()){
                        BatteryChanger.startFireAlarmTimer();
                    }
                } else {
                    respondImmediatelyWithString(interaction, "I am not connected to a voice channel");
                }
            } else if (fullCommandName.equals("sseblo")) {
                lastCommandChannel = interaction.getChannel().get();

                String textToConvert = interaction.getOptionByName("text").get().getStringValue().get();

                String trackUrl = getSosaniaEblaUrl(textToConvert);
                respondImmediatelyWithString(interaction, "Playing \"" + textToConvert + "\" with Alyona Flirt ");

                if (api.getYourself().getConnectedVoiceChannel(interactionServer).isEmpty()) {
                    String finalTrackUrl = trackUrl;
                    currentPlayer.setPause(true);
                    Server finalInteractionServer = interactionServer;

                    userVoiceChannel.connect().thenAccept(audioConnection -> {
                        currentGreetingPlayer.greetingPlayer(api, audioConnection, finalTrackUrl, null, false, finalInteractionServer, currentPlayer, false);
                    });
                } else {
                    currentPlayer.setPause(true);
                    AudioConnection audioConnection = interactionServer.getAudioConnection().get();
                    currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl, null, false, interactionServer, currentPlayer, false);
                }
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
                if (currentPlayer.getPause()) {
                    respondImmediatelyWithString(interaction, "Unpaused");

                    currentPlayer.setPause(false);
                } else {
                    respondImmediatelyWithString(interaction, "Paused");

                    currentPlayer.setPause(true);
                }
            } else if (fullCommandName.equals("np")) {
                AudioTrack audioTrackNowPlaying = currentPlayer.getAudioTrackNowPlaying();

                String loop = null;
                String pause = null;

                long duration = 0;
                long position = 0;
                int volume = 0;

                String identifier = null;

                if (currentPlayer.getLoopVar()) {
                    loop = "Loop enabled";
                } else {
                    loop = "Loop disabled";
                }

                if (currentPlayer.getPause()) {
                    pause = "Now paused";
                } else {
                    pause = "Playing";
                }

                if (audioTrackNowPlaying != null) {
                    duration = audioTrackNowPlaying.getDuration();
                    position = audioTrackNowPlaying.getPosition();
                    volume = currentPlayer.getVolume();

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

                MiscMethods.respondImmediatelyWithEmbed(interaction, embed);
            } else if (fullCommandName.equals("random")) {
                lastCommandChannel = interaction.getChannel().get();
                if (optionalUserVoiceChannel.isPresent()) {
                    Set<User> userSet = interaction.getUser().getConnectedVoiceChannel(interactionServer).get().getConnectedUsers();

                    User randomUser = userSet.stream().skip(new Random().nextInt(userSet.size())).findFirst().orElse(null);

                    assert randomUser != null;
                    String trackUrl = getSosaniaEblaUrl(randomUser.getDisplayName(interactionServer));

                    respondImmediatelyWithString(interaction,randomUser.getName());

                    currentPlayer.setPause(true);

                    if (optionalBotVoiceChannel.isEmpty()) {
                        Server finalServer = interactionServer;
                        interaction.getUser().getConnectedVoiceChannel(interactionServer).get().connect().thenAccept(audioConnection -> {
                            currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl, slashCommandCreateEvent,false, finalServer, currentPlayer, false);
                        });
                    } else {
                        AudioConnection audioConnection = interactionServer.getAudioConnection().get();
                        currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl, slashCommandCreateEvent,false, interactionServer, currentPlayer, false);
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
                int volumeBefore = currentPlayer.getVolume();
                currentPlayer.setVolume(volumeLevel);
                String trackUrl = "https://storage.rferee.dev/assets/media/audio/alyona_volume_warning.wav";
                AudioConnection audioConnection = interactionServer.getAudioConnection().get();

                if (volumeLevel - volumeBefore >= 100) {
                    if (volumeBefore < 700) {
                        currentPlayer.setPause(true);
                        currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl, null, false, interactionServer, currentPlayer, false);
                    }
                }

                if (volumeLevel > 900) {
                    respondImmediatelyWithString(interaction, "ТЫ ЧЕ ЕБАНУТЫЙ? КАКОЙ " + volumeLevel + "? ТЕБЕ ЧЕ ЖИТЬ НАДОЕЛО?");
                } else {
                    respondImmediatelyWithString(interaction, "Volume set to " + volumeLevel);
                }
            } else if (fullCommandName.equals("queue")) {
                try {
                    respondImmediatelyWithEmbed(interaction, currentQueue.getQueueEmbed());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (fullCommandName.equals("skip")) {
                try {
                    String secondTrackInQueue = null;

                    int i = 0;
                    for (String trackUrl : currentQueue.getQueueList()) {
                        if (i == 1) {
                            secondTrackInQueue = trackUrl;
                            break;
                        }
                        i++;
                    }

                    EmbedBuilder skipEmbed = new EmbedBuilder()
                            .addInlineField("__**Skipping:**__ ", "[" + getYoutubeVideoTitleFromUrl(currentQueue.getQueueList().peek(), true) + "](" + currentQueue.getQueueList().peek() + ")")
                            .setColor(Color.GREEN);

                    if (secondTrackInQueue != null) {
                        skipEmbed.addInlineField("__**Next track:**__ ", "[" + getYoutubeVideoTitleFromUrl(secondTrackInQueue, true) + "](" + secondTrackInQueue + ")");
                    } else {
                        skipEmbed.addInlineField("__**No next track :(**__","");
                    }

                    respondImmediatelyWithEmbed(interaction, skipEmbed);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (optionalBotVoiceChannel.isEmpty()) {
                    Server finalServer = interactionServer;
                    interaction.getUser().getConnectedVoiceChannel(interactionServer).get().connect().thenAccept(audioConnection -> {
                        currentQueue.skipTrack (api, audioConnection, slashCommandCreateEvent,true, finalServer, isPlaying, currentPlayer);
                    });
                } else {
                    AudioConnection audioConnection = interactionServer.getAudioConnection().get();
                    currentQueue.skipTrack(api, audioConnection, slashCommandCreateEvent,true, interactionServer, isPlaying, currentPlayer);
                }
            } else if (fullCommandName.equals("seek")) {
                String position = interaction.getOptionByName("position").get().getStringValue().get();

                if (isValidTimeFormat(position)) {
                    long milis = convertToMilliseconds(position);
                    if (!(milis > currentPlayer.getAudioTrackNowPlaying().getDuration())) {
                        EmbedBuilder seekEmbed = new EmbedBuilder()
                                .addField("__**Seeking track: **__", currentPlayer.getAudioTrackNowPlaying().getInfo().title)
                                .addInlineField("Previous position: ", "`" + formatDuration(currentPlayer.getAudioTrackNowPlaying().getPosition()) + "`")
                                .addInlineField("New position: ", "`" + position + "`")
                                .setColor(Color.GREEN);

                        currentPlayer.setPosition(milis);
                        respondImmediatelyWithEmbed(interaction, seekEmbed);
                    } else {
                        EmbedBuilder seekLongFailureEmbed = new EmbedBuilder()
                                .addField("__**Incorrect position**__", "Track duration is `" + formatDuration(currentPlayer.getAudioTrackNowPlaying().getDuration()) + "`")
                                .setColor(Color.red);

                        respondImmediatelyWithEmbed(interaction, seekLongFailureEmbed);
                    }
                } else {
                    EmbedBuilder seekFailureEmbed = new EmbedBuilder()
                            .addField("__**Incorrect position format**__", "Use `minutes:seconds` format")
                            .setColor(Color.red);

                    respondImmediatelyWithEmbed(interaction, seekFailureEmbed);
                }
            } else if (fullCommandName.equals("dev")) {
                DevCommand.triggerDevCommand(interaction, api);
            } else if (fullCommandName.equals("change")) {
                BatteryChanger.setEnabled(false);
                try {
                    api.getYourself().getConnectedVoiceChannel(interactionServer).get().disconnect();
                } catch (NoSuchElementException ignored){}

                removeGreetingPlayer(interactionServerId);
            }
        });

        api.addServerVoiceChannelMemberJoinListener(serverVoiceChannelMemberJoinEvent -> {
            Server server = serverVoiceChannelMemberJoinEvent.getServer();
            User user = serverVoiceChannelMemberJoinEvent.getUser();

            if (!players.containsKey(server.getId())){
                players.put(server.getId(), new Player());
            }

            if (!greetingPlayers.containsKey(server.getId())){
                greetingPlayers.put(server.getId(), new GreetingPlayer());
            }

            Player currentPlayer = players.get(server.getId());
            GreetingPlayer currentGreetingPlayer = greetingPlayers.get(server.getId());

            if (AloneInChannelHandler.isAloneTimerRunning()){
                if (serverVoiceChannelMemberJoinEvent.getChannel().getId() == AloneInChannelHandler.getVoiceChannel().getId()) {
                    AloneInChannelHandler.stopAloneTimer(true);
                }
            }

            if (userAudio.containsKey(user.getId()) && !BatteryChanger.getIsFireAlarmSystemEnabled()) {
                String trackUrl = userAudio.get(user.getId());
                if (api.getYourself().getConnectedVoiceChannel(server).isEmpty()) {
                    String finalTrackUrl = trackUrl;
                    currentPlayer.setPause(true);

                    serverVoiceChannelMemberJoinEvent.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                        currentGreetingPlayer.greetingPlayer(api, audioConnection, finalTrackUrl, null, false, server, currentPlayer, false);
                    });
                } else {
                    currentPlayer.setPause(true);
                    AudioConnection audioConnection = server.getAudioConnection().get();
                    currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl, null, false, server, currentPlayer, false);
                }
            }

            if (BatteryChanger.getEnabled() && BatteryChanger.getIsFireAlarmSystemEnabled()) {
                String trackUrl = "https://ln.vprw.ru/f/jkuaeo40.mp3";
                if (api.getYourself().getConnectedVoiceChannel(server).isEmpty()) {
                    String finalTrackUrl = trackUrl;

                    serverVoiceChannelMemberJoinEvent.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                        currentGreetingPlayer.greetingPlayer(api, audioConnection, finalTrackUrl, null, false, server, currentPlayer, true);
                    });
                } else {
                    AudioConnection audioConnection = server.getAudioConnection().get();
                    currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl, null, false, server, currentPlayer, true);
                }
            }
        });

        api.addServerVoiceChannelMemberLeaveListener(serverVoiceChannelMemberLeaveEvent -> {
            Server server = serverVoiceChannelMemberLeaveEvent.getServer();
            ServerVoiceChannel channel = serverVoiceChannelMemberLeaveEvent.getChannel();

            Player currentPlayer = players.get(server.getId());
            Queue currentQueue = queues.get(server.getId());


            if (api.getYourself().getConnectedVoiceChannel(server).isPresent() && api.getYourself().getConnectedVoiceChannel(server).get() == channel) {

                Set<User> users = channel.getConnectedUsers();
                int usersInChannel = users.size();

                if (serverVoiceChannelMemberLeaveEvent.getUser().getId() != api.getYourself().getId()) {
                    if (serverVoiceChannelMemberLeaveEvent.getUser().getId() != 1074801519523807252L) {
                        if (usersInChannel == 1) {
                            AloneInChannelHandler.startAloneTimer(lastCommandChannel, server, api, "I'm alone :(", channel, currentQueue, currentPlayer);
                        }
                    }
                }
            }
        });

    }

}
