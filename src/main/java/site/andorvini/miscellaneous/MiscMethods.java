package site.andorvini.miscellaneous;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.Main;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscMethods {

//  =============== Respond With Plain Text ===============

    public static void respondImmediatelyWithString(SlashCommandInteraction interaction, String text){
        interaction.createImmediateResponder()
                .setContent(text)
                .respond()
                .join();
    }

//  =============== Respond With Embed ===============

    public static void respondImmediatelyWithEmbed(SlashCommandInteraction interaction, EmbedBuilder embed){
        interaction.createImmediateResponder()
                .addEmbeds(embed)
                .respond()
                .join();
    }

//  =============== Format duration from milis to minutes:seconds ===============

    public static String formatDuration(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);

        return String.format("%d:%02d", minutes, seconds);
    }

// =============== Returns true if str is url ===============

    public static boolean isUrl(String str) {
        Pattern urlPattern = Pattern.compile("^((https?|ftp)://)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,12}(:[0-9]{1,5})?(/.*)?$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = urlPattern.matcher(str);
        return matcher.matches();
    }

//  =============== Converts from minutes:seconds to milis ===============

    public static long convertToMilliseconds(String time) {
        String[] tokens = time.split(":");
        int minutes = Integer.parseInt(tokens[0]);
        int seconds = Integer.parseInt(tokens[1]);
        return (minutes * 60 + seconds) * 1000;
    }

//  =============== Checks if time is in minutes:seconds format ===============

    public static boolean isValidTimeFormat(String time) {
        String regex = "^\\d+:[0-5]\\d$";
        return time.matches(regex);
    }

//  =============== Disconnects the bot and deletes all hashmaps ===============

    public static void disconnectBot(DiscordApi api, Server server) {
        Long serverId = server.getId();

        api.getYourself().getConnectedVoiceChannel(server).get().disconnect();
        Main.removeAllHashmaps(serverId);
    }

    public static boolean isValidURLForGreeting(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            url.openConnection().connect();
            String contentType = url.openConnection().getContentType();
            if (contentType.startsWith("audio/")) {
                return true;
            } else if (YoutubeMethods.isYouTubeLink(urlString)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
