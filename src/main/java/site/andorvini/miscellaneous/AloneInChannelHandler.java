package site.andorvini.miscellaneous;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;

import site.andorvini.Main;
import site.andorvini.queue.Queue;
import site.andorvini.players.Player;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class AloneInChannelHandler {

    private Timer timer;
    private boolean isTimerRunning;
    private TextChannel lastChannel;
    private String reason;
    private ServerVoiceChannel voiceChannel;

    public ServerVoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

    public void stopAloneTimer(boolean isWithReason){
        timer.cancel();
        if (isWithReason) {
            isTimerRunning = false;
            String text = null;

            if (reason.equals("No tracks in queue")) {
                text = "Someone added a track";
            } else if (reason.equals("I'm alone :(")) {
                text = "I'm not alone anymore!";
            }

            EmbedBuilder emded = new EmbedBuilder()
                    .setColor(Color.yellow)
                    .addField(text, "I won't leave you now!");

            lastChannel.sendMessage(emded);
        }
    }

    public boolean isAloneTimerRunning(){
        return isTimerRunning;
    }

    public void startAloneTimer(TextChannel channel, Server server, DiscordApi api, String reasonFrom, ServerVoiceChannel voiceChannelFrom, Queue queue, Player player) {

        timer = new Timer();
        isTimerRunning = true;
        lastChannel = channel;
        reason = reasonFrom;

        if (voiceChannelFrom != null) {
            voiceChannel = voiceChannelFrom;
        }

        int leaveSeconds = 60;

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setAuthor(reasonFrom)
                .addField("", "Leaving in `" + leaveSeconds + "` seconds");

        channel.sendMessage(embed);

        timer.scheduleAtFixedRate(new TimerTask() {
            int i = leaveSeconds;
            @Override
            public void run() {
                if (i == 0) {
                    player.stopPlaying();
                    queue.clearQueue();
                    server.getConnectedVoiceChannel(api.getYourself()).get().disconnect();
                    isTimerRunning = false;
                    timer.cancel();

                    Main.removeQueue(server.getId());
                    Main.removeGreetingPlayer(server.getId());
                    Main.removePlayerFromPlayers(server.getId());

                    if (BatteryChanger.getIsFireAlarmSystemEnabled()) {
                        BatteryChanger.startFireAlarmTimer();
                    }
                }
                i--;
            }
        },0,1000);
    }
}
