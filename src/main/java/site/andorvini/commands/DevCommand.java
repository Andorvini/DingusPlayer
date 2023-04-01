package site.andorvini.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class DevCommand {
    public static void triggerDevCommand(SlashCommandInteraction interaction, DiscordApi api) {
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
            } else if (devQuery.equals("sortVoiceChannels")) {
                Server server = interaction.getServer().get();

                List<ServerVoiceChannel> voiceChannels = server.getVoiceChannels();

                ArrayList<ServerVoiceChannel> reverted = new ArrayList<>();

                for (ServerVoiceChannel voiceChannel: voiceChannels) {
                    reverted.add(voiceChannel);
                }

                Collections.reverse(reverted);

                int i = 0;
                for (ServerVoiceChannel channel : voiceChannels) {
                    if(reverted.get(i).getName().equals("github copilot")){
                        channel.delete();
                    } else {
                        channel.updateName(reverted.get(i).getName());
                        channel.updateBitrate(128000);
                    }
                    i++;
                }
            } else if (devQuery.equals("getFromBackup")) {
                Server server = interaction.getServer().get();

                ArrayList<String> lines = readFileToList("backup.txt");

//                for (String name : lines) {
//                    server.createVoiceChannelBuilder().setName(name).create();
//                }

                server.createVoiceChannelBuilder().setName("cumming").create();

            } else if (devQuery.equals("clearVoices")) {
                for (ServerVoiceChannel channel : interactionServer.getVoiceChannels()) {
                    channel.delete();
                }
//            } else if (devQuery.equals("gsonTest")) {
//                TextChannel channel = interaction.getChannel().get();
//                try {
//                   System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(SsebloVoicesListModel.getSsebloVoices()));
//                } catch (IOException e){}
            } else {
                MiscMethods.respondImmediatelyWithString(interaction, "There is no such query");
            }
        } else {
            MiscMethods.respondImmediatelyWithString(interaction, "You can't use ANALYTICS");
        }
    }

    public static ArrayList<String> readFileToList(String filename) {
        ArrayList<String> lines = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return lines;
    }
}
