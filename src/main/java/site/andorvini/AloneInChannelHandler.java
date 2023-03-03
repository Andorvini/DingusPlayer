package site.andorvini;

import org.javacord.api.DiscordApi;
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

    public static void stopAloneTimer(){
        timer.cancel();
        isTimerRunning = false;
    }

    private static boolean isAloneTimerRunning(){
        return isTimerRunning;
    }

    public static void startAloneTimer(TextChannel channel, Server server, DiscordApi api) {

        timer = new Timer();
        isTimerRunning = true;

        int leaveSeconds = 60;

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setAuthor("I'm alone :(")
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
                    timer.cancel();
                }
                i--;
            }
        },0,1000);
    }
}
