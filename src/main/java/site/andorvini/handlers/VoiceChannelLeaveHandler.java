package site.andorvini.handlers;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import site.andorvini.miscellaneous.AloneInChannelHandler;
import site.andorvini.players.Player;
import site.andorvini.queue.Queue;

import java.util.HashMap;
import java.util.Set;

public class VoiceChannelLeaveHandler {
    public static void addVoiceChannelLeaveHandler(DiscordApi api, HashMap<Long, Queue> queues, HashMap<Long, Player> players, TextChannel lastCommandChannel){
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
