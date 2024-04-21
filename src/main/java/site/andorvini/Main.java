package site.andorvini;

import io.sentry.Sentry;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.activity.ActivityType;

import org.javacord.api.entity.intent.Intent;
import site.andorvini.database.GreetingsDatabase;
import site.andorvini.handlers.ButtonHandler;
import site.andorvini.handlers.SlashCommandHandler;
import site.andorvini.handlers.VoiceChannelJoinHandler;
import site.andorvini.handlers.VoiceChannelLeaveHandler;
import site.andorvini.miscellaneous.AloneInChannelHandler;
import site.andorvini.players.GreetingPlayer;
import site.andorvini.players.Player;
import site.andorvini.queue.Queue;

import java.util.*;

public class Main {

    //  ============ Variables Declaration ============
    private static HashMap<Long, Player> players = new HashMap<>();
    private static HashMap<Long, site.andorvini.queue.Queue> queues = new HashMap<>();
    private static HashMap<Long, GreetingPlayer> greetingPlayers = new HashMap<>();
    private static HashMap<Long, TextChannel> lastTextChannels = new HashMap<>();
    private static HashMap<Long, AloneInChannelHandler> aloneInChannelHandlers = new HashMap<>();

    // ============ Setters ============

    public static void removePlayerFromPlayers(Long serverId) {
        players.remove(serverId);
    }

    public static void removeQueue(Long serverId) {
        queues.remove(serverId);
    }

    public static void removeGreetingPlayer(Long serverId) {
        greetingPlayers.remove(serverId);
    }

    public static void addLastTextChannel(Long serverId, TextChannel channel) {
        lastTextChannels.put(serverId, channel);
    }

    public static void addAloneInChannelHandlers(Long serverId) {
        aloneInChannelHandlers.put(serverId, new AloneInChannelHandler());
    }

    public static void removeAllHashmaps(Long serverId) {
        players.remove(serverId);
        queues.remove(serverId);
        greetingPlayers.remove(serverId);
        lastTextChannels.remove(serverId);
        aloneInChannelHandlers.remove(serverId);
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

    public static TextChannel getLastCommandChannel(Long id){
        return lastTextChannels.get(id);
    }

    public static HashMap<Long, AloneInChannelHandler> getAloneInChannelHandlers(){
        return aloneInChannelHandlers;
    }

    // ============ Main ============
    public static boolean sentryAvailable = false;

    public static void main(String[] args) {

        // ============ TOKEN PROCESSING ============
        String token = System.getenv("DP_DISCORD_TOKEN");
        String ssEbloApiToken = System.getenv("DP_SOSANIE_API_KEY");
        String youtubeApiToken = System.getenv("DP_YOUTUBE_API_KEY");
        String youtubeLogin = System.getenv("DP_YOUTUBE_LOGIN");
        String youtubePassword = System.getenv("DP_YOUTUBE_PASSWORD");
        String ttsEnabled = System.getenv("DP_SOSANIE_TTS_ENABLED");
        String prosloykaApiUrl = System.getenv("DP_PROSLOYKA_API_URL");

        String sentryDsn = System.getenv("DP_SENTRY_DSN");
        if (sentryDsn != null) {
            Sentry.init(options -> {
                options.setDsn(sentryDsn);
                options.setTracesSampleRate(1.0);
                options.setDebug(true);
            });
            sentryAvailable = true;
        }

        if (token == null) {
            System.out.println("[ERROR] DP_DISCORD_TOKEN environment variable not found");
            System.exit(1);
        } else if (youtubeApiToken == null) {
            System.out.println("[ERROR] DP_YOUTUBE_API_KEY environment variable not found");
            System.exit(1);
        } else if (youtubeLogin == null) {
            System.out.println("[ERROR] DP_YOUTUBE_LOGIN environment variable not found");
            System.exit(1);
        } else if (youtubePassword == null) {
            System.out.println("[ERROR] DP_YOUTUBE_PASSWORD environment variable not found");
            System.exit(1);
        } else if (ttsEnabled == null) {
            System.out.println("[ERROR] DP_SOSANIE_TTS_ENABLED environment variable not found");
            System.exit(1);
        } else if (prosloykaApiUrl == null) {
            System.out.println("[ERROR] DP_PROSLOYKA_API_URL environment variable not found");
            System.exit(1);
        } else if (System.getenv("DP_SOSANIE_TTS_ENABLED").equals("true")){
             if (ssEbloApiToken == null) {
                 System.out.println("[ERROR] DP_SOSANIE_API_KEY environment variable not found");
                 System.exit(1);
             }
        }

        GreetingsDatabase.checkExist();

        // ============ BOT CREATION ============
        DiscordApi api = new DiscordApiBuilder().setToken(token).addIntents(Intent.MESSAGE_CONTENT,Intent.GUILD_MEMBERS).login().join();

        // ============ ACTIVITY SET ============
        api.updateActivity(ActivityType.LISTENING,"\"Antipathy World\"");

        // ============ SLASH COMMAND CREATION ============
        SlashCommandsRegister.registerSlashCommands(api);

        // ============ HANDLERS REG ============
        SlashCommandHandler.addSlashCommandsHadler(api, players, queues, greetingPlayers);
        VoiceChannelJoinHandler.addVoiceChannelJoinHandler(api, players, greetingPlayers);
        VoiceChannelLeaveHandler.addVoiceChannelLeaveHandler(api, queues, players, greetingPlayers);
        ButtonHandler.buttonHandler(api);
    }
}
