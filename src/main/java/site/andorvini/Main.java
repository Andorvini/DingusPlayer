package site.andorvini;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.*;
import org.javacord.api.entity.activity.ActivityType;

import site.andorvini.commands.*;
import site.andorvini.commands.Random;
import site.andorvini.handlers.ButtonHandler;
import site.andorvini.miscellaneous.AloneInChannelHandler;
import site.andorvini.miscellaneous.BatteryChanger;
import site.andorvini.players.GreetingPlayer;
import site.andorvini.players.Player;
import site.andorvini.queue.Queue;

import java.io.IOException;
import java.util.*;

import static site.andorvini.miscellaneous.MiscMethods.*;

public class Main {

    //  ============ Variables Declaration ============

    private static TextChannel lastCommandChannel;

    private static HashMap<Long, Player> players = new HashMap<>();
    private static HashMap<Long, site.andorvini.queue.Queue> queues = new HashMap<>();
    private static HashMap<Long, GreetingPlayer> greetingPlayers = new HashMap<>();

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

        String token = System.getenv("DP_DISCORD_TOKEN");
        String ssEbloApiToken = System.getenv("DP_SOSANIE_API_KEY");
        String youtubeApiToken = System.getenv("DP_YOUTUBE_API_KEY");
        String youtubeLogin = System.getenv("DP_YOUTUBE_LOGIN");
        String youtubePassword = System.getenv("DP_YOUTUBE_PASSWORD");


        if (token == null) {
            System.out.println("[ERROR] DP_DISCORD_TOKEN environment variable not found");
            System.exit(1);
        }

        if (ssEbloApiToken == null) {
            System.out.println("[ERROR] DP_SOSANIE_API_KEY environment variable not found");
            System.exit(1);
        }

        if (youtubeApiToken == null) {
            System.out.println("[ERROR] DP_YOUTUBE_API_KEY environment variable not found");
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

            Queue currentQueue = queues.get(interactionServerId);
            GreetingPlayer currentGreetingPlayer = greetingPlayers.get(interactionServerId);
            Player currentPlayer = players.get(interactionServerId);

            if (!interaction.getChannel().get().getType().isServerChannelType()) {
                interaction.createImmediateResponder()
                        .setContent("Use this bot only on server!")
                        .respond()
                        .join();
            } else if (fullCommandName.equals("phony")) {
                Phony.phony(api, interaction, optionalUserVoiceChannel, lastCommandChannel, currentPlayer, optionalBotVoiceChannel, interactionServer, userVoiceChannel, currentGreetingPlayer);
            } else if (fullCommandName.equals("play")) {
                Play.play(api, interaction, interactionServer, optionalUserVoiceChannel, currentQueue, optionalBotVoiceChannel, currentPlayer, userVoiceChannel, lastCommandChannel);
            } else if (fullCommandName.equals("loop")) {
                Loop.loop(interaction, currentPlayer);
            } else if (fullCommandName.equals("leave")) {
                Leave.leave(api, interaction, interactionServer, optionalBotVoiceChannel, botVoiceChannel, interactionServerId, currentPlayer, currentQueue, players);
            } else if (fullCommandName.equals("sseblo")) {
                Sseblo.sseblo(api, interaction, interactionServer, currentPlayer, currentGreetingPlayer, userVoiceChannel, lastCommandChannel);
            } else if (fullCommandName.equals("clear")) {
                Clear.clear(interaction);
            } else if (fullCommandName.equals("pause")) {
                Pause.pause(interaction, currentPlayer);
            } else if (fullCommandName.equals("np")) {
                NowPlaying.np(interaction, currentPlayer);
            } else if (fullCommandName.equals("random")) {
                Random.random(api, interaction, interactionServer, lastCommandChannel, currentPlayer, currentGreetingPlayer, optionalBotVoiceChannel, optionalUserVoiceChannel);
            } else if (fullCommandName.equals("ping")) {
                respondImmediatelyWithString(interaction, "Pong!");
            } else if (fullCommandName.equals("lyrics")) {
                respondImmediatelyWithString(interaction, "Not implemented yet. (Because musixmatch shit)");
            } else if (fullCommandName.equals("volume")) {
                Volume.volume(api, interaction, currentPlayer, interactionServer, currentGreetingPlayer);
            } else if (fullCommandName.equals("queue")) {
                try {
                    respondImmediatelyWithEmbed(interaction, currentQueue.getQueueEmbed());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (fullCommandName.equals("skip")) {
                Skip.skip(api, interaction, interactionServer, optionalBotVoiceChannel, currentQueue, currentPlayer);
            } else if (fullCommandName.equals("seek")) {
                Seek.seek(interaction, currentPlayer);
            } else if (fullCommandName.equals("dev")) {
                DevCommand.triggerDevCommand(interaction, api);
            } else if (fullCommandName.equals("change")) {
                ChangeBatteries.change(api, interactionServer);
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
                        currentGreetingPlayer.greetingPlayer(api, audioConnection, finalTrackUrl, currentPlayer, server, false);
                    });
                } else {
                    currentPlayer.setPause(true);
                    AudioConnection audioConnection = server.getAudioConnection().get();
                    currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl, currentPlayer, server, false);
                }
            }

            if (BatteryChanger.getEnabled() && BatteryChanger.getIsFireAlarmSystemEnabled()) {
                String trackUrl = "https://ln.vprw.ru/f/jkuaeo40.mp3";
                if (api.getYourself().getConnectedVoiceChannel(server).isEmpty()) {
                    String finalTrackUrl = trackUrl;

                    serverVoiceChannelMemberJoinEvent.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                        currentGreetingPlayer.greetingPlayer(api, audioConnection, finalTrackUrl, currentPlayer, server, true);
                    });
                } else {
                    AudioConnection audioConnection = server.getAudioConnection().get();
                    currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl, currentPlayer, server, true);
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
