package site.andorvini.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;

import site.andorvini.Main;
import site.andorvini.miscellaneous.BatteryChanger;
import site.andorvini.miscellaneous.MiscMethods;
import site.andorvini.players.GreetingPlayer;
import site.andorvini.players.Player;
import site.andorvini.queue.Queue;

import java.awt.*;
import java.util.HashMap;
import java.util.Set;

public class DevCommand {
    public static void triggerDevCommand(SlashCommandInteraction interaction, DiscordApi api){
        User interactionUser = interaction.getUser();
        Server interactionServer = interaction.getServer().get();
        Long interactionServerId = interactionServer.getId();

        if (interactionUser.getId() == 394085232266969090L || interactionUser.getId() == 483991031306780683L){
            String devQuery = interaction.getOptionByName("devQuery").get().getStringValue().get();
            Set<Server> servers = api.getServers();
            
            if (devQuery.equals("servers")) {

                EmbedBuilder serversEmbed = new EmbedBuilder()
                        .setAuthor("ANALYTICS (SERVERS)")
                        .setColor(Color.BLACK)
                        .addField("Total servers: " + servers.size(), "");

                for (Server server : servers) {
                    serversEmbed.addField("", server.getName());
                }

                MiscMethods.respondImmediatelyWithEmbed(interaction, serversEmbed);
            } else if (devQuery.equals("hashmaps")) {
                HashMap<Long, Player> players = Main.getPlayers();
                HashMap<Long, Queue> queues = Main.getQueues();
                HashMap<Long, GreetingPlayer> greetingPlayers = Main.getGreetingPlayers();

                EmbedBuilder hashmapsEmbed = new EmbedBuilder()
                        .setColor(Color.black)
                        .setAuthor("ANALYTICS (HASHMAPS)");

                hashmapsEmbed.addField("Players", players.get(interactionServerId).toString());
                hashmapsEmbed.addField("GreetingPlayer", greetingPlayers.get(interactionServerId).toString());
                hashmapsEmbed.addField("Queue", queues.get(interactionServerId).toString());

                MiscMethods.respondImmediatelyWithEmbed(interaction, hashmapsEmbed);
            } else if (devQuery.equals("fireAlarmSwitch")) {
                if (BatteryChanger.getIsFireAlarmSystemEnabled()) {
                    BatteryChanger.setIsFireAlarmSystemEnabled(false);
                    MiscMethods.respondImmediatelyWithString(interaction, "Turned off fireAlarmSystem");
                    BatteryChanger.stopFireAlarmTimer();
                } else {
                    BatteryChanger.setIsFireAlarmSystemEnabled(true);
                    MiscMethods.respondImmediatelyWithString(interaction, "Turned on fireAlarmSystem");
                }
            } else {
                MiscMethods.respondImmediatelyWithString(interaction, "There is no such query");
            }
        } else {
            MiscMethods.respondImmediatelyWithString(interaction, "You can't use ANALYTICS");
        }
    }
}
