package site.andorvini.handlers;

import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import site.andorvini.miscellaneous.AloneInChannelHandler;
import site.andorvini.miscellaneous.BatteryChanger;
import site.andorvini.players.GreetingPlayer;
import site.andorvini.players.Player;

import java.util.HashMap;

public class VoiceChannelJoinHandler {
    public static void addVoiceChannelJoinHandler(DiscordApi api, HashMap<Long, Player> players, HashMap<Long, GreetingPlayer> greetingPlayers){

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
                    System.out.println("asdasd4");
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
    }
}
