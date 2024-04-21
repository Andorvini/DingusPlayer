package site.andorvini.handlers;

import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import site.andorvini.Main;
import site.andorvini.database.GreetingsDatabase;
import site.andorvini.miscellaneous.AloneInChannelHandler;
import site.andorvini.miscellaneous.BatteryChanger;
import site.andorvini.miscellaneous.MiscMethods;
import site.andorvini.players.GreetingPlayer;
import site.andorvini.players.Player;

import java.util.HashMap;

public class VoiceChannelJoinHandler {
    public static void addVoiceChannelJoinHandler(DiscordApi api, HashMap<Long, Player> players, HashMap<Long, GreetingPlayer> greetingPlayers){
        api.addServerVoiceChannelMemberJoinListener(serverVoiceChannelMemberJoinEvent -> {
            Server server = serverVoiceChannelMemberJoinEvent.getServer();
            Long serverId = server.getId();
            User user = serverVoiceChannelMemberJoinEvent.getUser();

            String userAudio = GreetingsDatabase.getGreetingUrl(serverId, user.getId());

            if (!players.containsKey(server.getId())){
                players.put(server.getId(), new Player());
            }

            if (!greetingPlayers.containsKey(server.getId())){
                greetingPlayers.put(server.getId(), new GreetingPlayer());
            }

            if (!Main.getAloneInChannelHandlers().containsKey(serverId)){
                Main.addAloneInChannelHandlers(serverId);
            }

            if (serverVoiceChannelMemberJoinEvent.isMove() && serverVoiceChannelMemberJoinEvent.getUser().getId() == api.getYourself().getId()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (api.getYourself().getConnectedVoiceChannel(server).isPresent()) {
                    MiscMethods.disconnectBot(api, server);
                }
            }

            AloneInChannelHandler currentAloneInChannelHandler = Main.getAloneInChannelHandlers().get(serverId);

            Player currentPlayer = players.get(server.getId());
            GreetingPlayer currentGreetingPlayer = greetingPlayers.get(server.getId());

            if (currentAloneInChannelHandler.isAloneTimerRunning()){
                if (serverVoiceChannelMemberJoinEvent.getChannel().getId() == api.getYourself().getConnectedVoiceChannel(server).get().getId()) {
                    currentAloneInChannelHandler.stopAloneTimer(true);
                }
            }

            if (userAudio != null && !BatteryChanger.getIsFireAlarmSystemEnabled()) {
                String trackUrl = userAudio;
                if (api.getYourself().getConnectedVoiceChannel(server).isEmpty()) {
                    String finalTrackUrl = trackUrl;
                    currentPlayer.setPause(true);

                    currentGreetingPlayer.setInProgress(true);
                    serverVoiceChannelMemberJoinEvent.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                        currentGreetingPlayer.greetingPlayer(api, audioConnection, finalTrackUrl, currentPlayer, server, false);
                    });
                } else {
                    currentPlayer.setPause(true);
                    currentGreetingPlayer.setInProgress(true);
                    AudioConnection audioConnection = server.getAudioConnection().get();
                    currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl, currentPlayer, server, false);
                }
            }

            if (BatteryChanger.getEnabled() && BatteryChanger.getIsFireAlarmSystemEnabled()) {
                String trackUrl = "https://ln.vprw.ru/f/jkuaeo40.mp3";
                if (api.getYourself().getConnectedVoiceChannel(server).isEmpty()) {
                    String finalTrackUrl = trackUrl;
                    currentGreetingPlayer.setInProgress(true);
                    serverVoiceChannelMemberJoinEvent.getUser().getConnectedVoiceChannel(server).get().connect().thenAccept(audioConnection -> {
                        currentGreetingPlayer.greetingPlayer(api, audioConnection, finalTrackUrl, currentPlayer, server, true);
                    });
                } else {
                    currentGreetingPlayer.setInProgress(true);
                    AudioConnection audioConnection = server.getAudioConnection().get();
                    currentGreetingPlayer.greetingPlayer(api, audioConnection, trackUrl, currentPlayer, server, true);
                }
            }
        });
    }
}
