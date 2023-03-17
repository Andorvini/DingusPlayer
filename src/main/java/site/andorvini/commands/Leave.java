package site.andorvini.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandInteraction;

import site.andorvini.miscellaneous.AloneInChannelHandler;
import site.andorvini.miscellaneous.BatteryChanger;
import site.andorvini.players.Player;
import site.andorvini.queue.Queue;

import java.util.HashMap;
import java.util.Optional;

import static site.andorvini.miscellaneous.MiscMethods.respondImmediatelyWithString;

public class Leave {
    public static void leave(DiscordApi api, SlashCommandInteraction interaction, Server interactionServer, Optional<ServerVoiceChannel> optionalBotVoiceChannel, ServerVoiceChannel botVoiceChannel, Long interactionServerId, Player currentPlayer, Queue currentQueue, HashMap<Long, Player> players) {
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
    }
}
