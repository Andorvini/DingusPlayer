package site.andorvini;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

import static site.andorvini.Queue.clearQueue;

public class AloneInChannelHandler {

    private static Timer timer;

    private static boolean isTimerRunning;

    private static TextChannel lastChannel;

    private static String reason;

    private static ServerVoiceChannel voiceChannel;

    public static ServerVoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

    public static void stopAloneTimer(){
        timer.cancel();
        isTimerRunning = false;
        String text = null;

        if (reason.equals("No tracks in queue")){
            text = "Someone added track";
        } else if (reason.equals("I'm alone :(")) {
            text = "I'm not alone anymore!";
        }

        EmbedBuilder emded = new EmbedBuilder()
                .setColor(Color.yellow)
                        .addField(text, "I won't leave you now!");

        lastChannel.sendMessage(emded);
    }

    public static boolean isAloneTimerRunning(){
        return isTimerRunning;
    }

    public static void startAloneTimer(TextChannel channel, Server server, DiscordApi api, String reasonFrom, ServerVoiceChannel voiceChannelFrom) {

        timer = new Timer();
        isTimerRunning = true;
        lastChannel = channel;
        reason = reasonFrom;

        if (voiceChannelFrom != null) {
            voiceChannel = voiceChannelFrom;
        }

        int leaveSeconds = 10;

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
                    Player.stopPlaying();
                    clearQueue();
                    server.getConnectedVoiceChannel(api.getYourself()).get().disconnect();
                    isTimerRunning = false;
                    timer.cancel();
                }
                i--;
            }
        },0,1000);
    }
}