package site.andorvini;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.activity.ActivityType;

import site.andorvini.handlers.ButtonHandler;
import site.andorvini.handlers.SlashCommandHandler;
import site.andorvini.handlers.VoiceChannelJoinHandler;
import site.andorvini.handlers.VoiceChannelLeaveHandler;
import site.andorvini.players.GreetingPlayer;
import site.andorvini.players.Player;
import site.andorvini.queue.Queue;

import java.util.*;

public class Main {

    //  ============ Variables Declaration ============

    private static TextChannel lastCommandChannel;

    private static HashMap<Long, Player> players = new HashMap<>();
    private static HashMap<Long, site.andorvini.queue.Queue> queues = new HashMap<>();
    private static HashMap<Long, GreetingPlayer> greetingPlayers = new HashMap<>();
    private static HashMap<Long, TextChannel> lastTextChannels = new HashMap<>();

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

        // ============ TOKEN PROCESSING ============

        String token = System.getenv("DP_DISCORD_TOKEN");
        String ssEbloApiToken = System.getenv("DP_SOSANIE_API_KEY");
        String youtubeApiToken = System.getenv("DP_YOUTUBE_API_KEY");
        String youtubeLogin = System.getenv("DP_YOUTUBE_LOGIN");
        String youtubePassword = System.getenv("DP_YOUTUBE_PASSWORD");

        if (token == null) {
            System.out.println("[ERROR] DP_DISCORD_TOKEN environment variable not found");
            System.exit(1);
        } else if (ssEbloApiToken == null) {
            System.out.println("[ERROR] DP_SOSANIE_API_KEY environment variable not found");
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
        }

        // ============ BOT CREATION ============

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();
        apiGlobal = api;

        // ============ ACTIVITY SET ============
        api.updateActivity(ActivityType.LISTENING,"\"Antipathy World\"");

        // ============ SLASH COMMAND CREATION ============
        SlashCommandsRegister.registerSlashCommands(api);

        // ============ HANDLERS REG ============
        SlashCommandHandler.addSlashCommandsHadler(api, players, queues, greetingPlayers, lastCommandChannel);
        VoiceChannelJoinHandler.addVoiceChannelJoinHandler(api, players, greetingPlayers);
        VoiceChannelLeaveHandler.addVoiceChannelLeaveHandler(api, queues, players, lastCommandChannel);
        ButtonHandler.buttonHandler(api);
    }
}
