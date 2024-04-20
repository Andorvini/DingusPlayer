package site.andorvini.handlers;

import io.sentry.Sentry;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import site.andorvini.Main;
import site.andorvini.miscellaneous.AloneInChannelHandler;
import site.andorvini.miscellaneous.MiscMethods;
import site.andorvini.players.GreetingPlayer;
import site.andorvini.players.Player;
import site.andorvini.queue.Queue;

import java.util.HashMap;
import java.util.Set;

public class VoiceChannelLeaveHandler {
    public static void addVoiceChannelLeaveHandler(DiscordApi api, HashMap<Long, Queue> queues, HashMap<Long, Player> players, HashMap<Long, GreetingPlayer> greetingPlayers){
        api.addServerVoiceChannelMemberLeaveListener(serverVoiceChannelMemberLeaveEvent -> {
            Server server = serverVoiceChannelMemberLeaveEvent.getServer();
            Long serverId = server.getId();
            ServerVoiceChannel channel = serverVoiceChannelMemberLeaveEvent.getChannel();

            Player currentPlayer = players.get(serverId);
            Queue currentQueue = queues.get(serverId);
            GreetingPlayer currentGreetingPlayer = greetingPlayers.get(serverId);

            TextChannel lastCommandChannel = Main.getLastCommandChannel(server.getId());

            if (serverVoiceChannelMemberLeaveEvent.isMove() && serverVoiceChannelMemberLeaveEvent.getUser().getId() == api.getYourself().getId()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    if (Main.sentryAvailable) {
                        Sentry.captureException(e);
                    }
                    throw new RuntimeException(e);
                }
                if (api.getYourself().getConnectedVoiceChannel(server).isPresent()) {
                    MiscMethods.disconnectBot(api, server);
                }
            }

            if (api.getYourself().getConnectedVoiceChannel(server).isPresent() && api.getYourself().getConnectedVoiceChannel(server).get() == channel) {

                if (!Main.getAloneInChannelHandlers().containsKey(serverId)){
                    Main.addAloneInChannelHandlers(serverId);
                }

                AloneInChannelHandler currentAloneInChannelHandler = Main.getAloneInChannelHandlers().get(serverId);

                Set<User> users = channel.getConnectedUsers();
                int usersInChannel = users.size();

                if (serverVoiceChannelMemberLeaveEvent.getUser().getId() != api.getYourself().getId()) {
                    if (serverVoiceChannelMemberLeaveEvent.getUser().getId() != 1074801519523807252L) {
                        if (usersInChannel == 1) {
                            if (!currentAloneInChannelHandler.isAloneTimerRunning() && !currentGreetingPlayer.getInProgress()) {
                                currentAloneInChannelHandler.startAloneTimer(lastCommandChannel, server, api, "I'm alone :(", currentQueue, currentPlayer);
                            }
                        }
                    }
                }
            }
        });
    }
}
