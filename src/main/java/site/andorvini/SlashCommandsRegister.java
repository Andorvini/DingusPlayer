package site.andorvini;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SlashCommandsRegister {

    public static void registerSlashCommands(DiscordApi api){
        ArrayList<SlashCommandBuilder> commands = new ArrayList<>();

        //TODO

        SlashCommandBuilder playCommand =
                SlashCommand.with("play","Play music from provided Youtube URL",
                                Arrays. asList(
                                        SlashCommandOption.create(SlashCommandOptionType.STRING, "query", "Link to music, or just name", true)
                                ));

        SlashCommandBuilder phonyCommand =
                SlashCommand.with("phony", "Play ANTIPATHY WORLD",
                                Arrays.asList(
                                        SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "version", "Choose version of song", true,
                                                Arrays.asList(
                                                        SlashCommandOptionChoice.create("Russian remix", "rus"),
                                                        SlashCommandOptionChoice.create("Original", "original"),
                                                        SlashCommandOptionChoice.create("German", "german")))
                                ));

        SlashCommandBuilder loopCommand = SlashCommand.with("loop","Lop music");

        SlashCommandBuilder leaveCommand =
                SlashCommand.with("leave","Leave voice channel");

        SlashCommandBuilder pauseCommand = SlashCommand.with("pause","Pause music");

        SlashCommandBuilder ssebloCommand = SlashCommand.with("sseblo","Convert text into voice using sseblobotapi",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "text", "Text you want to voice", true)
                        ));

        ArrayList<SlashCommandOptionChoice> voiceTitles = new ArrayList<>();
//        int i = 0;
//        try {
//            for (SsebloVoicesData data : SsebloVoicesListModel.getSsebloVoices().getData()) {
//                voiceTitles.add(SlashCommandOptionChoice.create(data.getTitle(), data.getTitle()));
//                if (i == 24){
//                    break;
//                }
//                i++;
//            }
//        } catch (Exception ignored){}

        SlashCommandBuilder sseblo = SlashCommand.with("sseblo", "Convert text",
                Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "text", "text", true),
                        SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "voice", "voice", false,
                               voiceTitles)
                ));

        SlashCommandBuilder clearCommand = SlashCommand.with("clear","Delete specified number of messages",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.LONG,"count","Message count",true)
                        ));

        SlashCommandBuilder npCommand = SlashCommand.with("np","Show what song is playing now");

        SlashCommandBuilder randomUserCommand = SlashCommand.with("random","Pick random user");

        SlashCommandBuilder pingCommand = SlashCommand.with("ping", "Ping!");

        SlashCommandBuilder queueCommand = SlashCommand.with("queue", "Shows all tracks in queue");

        SlashCommandBuilder lyricsCommand = SlashCommand.with("lyrics", "Shows lyrics of currently playing track");

        SlashCommandBuilder volumeCommand = SlashCommand.with("volume","Set the volume",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.LONG, "volumelvl", "Volume level (Max. 1000)", true)
                        ));

        SlashCommandBuilder skipCommand = SlashCommand.with("skip", "Skips currently playing track");

        SlashCommandBuilder seekCommand = SlashCommand.with("seek", "Seeks the song to specified position",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "position", "Positon to seek", true)
                        ));

        SlashCommandBuilder devCommand = SlashCommand.with("dev", "For dev purposes",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "devQuery", "For dev purposes", true)
                        ));

        SlashCommandBuilder reklamaCommand = SlashCommand.with("reklama", "Sets reaction",
                Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "messageId", "Message ID (Should be copied with Shift)", true)
                ));

        SlashCommandBuilder changeBatteriesCommand = SlashCommand.with("change","For changing batteries");

        SlashCommandBuilder addGreetingCommand = SlashCommand.with("addgreeting", "Adds your personal greeting when joining voice channel",
                Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "greetingUrl", "URL to the direct audio file", true)
                ));

        SlashCommandBuilder removeGreetingCommand = SlashCommand.with("greetingdel", "Remove your greeting for current server");

        SlashCommandBuilder greetingListCommand = SlashCommand.with("greetingls", "Shows your greeting for current server");

        Set<SlashCommandBuilder> builders = new HashSet<>();

        builders.add(playCommand);
        builders.add(phonyCommand);
        builders.add(loopCommand);
        builders.add(leaveCommand);
        builders.add(pauseCommand);
        builders.add(clearCommand);
        builders.add(npCommand);
        builders.add(randomUserCommand);
        builders.add(pingCommand);
        builders.add(queueCommand);
        builders.add(lyricsCommand);
        builders.add(volumeCommand);
        builders.add(skipCommand);
        builders.add(seekCommand);
        builders.add(devCommand);
        builders.add(changeBatteriesCommand);
        builders.add(reklamaCommand);
        builders.add(addGreetingCommand);
        builders.add(removeGreetingCommand);
        builders.add(greetingListCommand);

        // ============ ADD /SSEBLO IF ENABLED ============
        if (System.getenv("DP_SOSANIE_TTS_ENABLED").equals("true")) {
            builders.add(sseblo);
        }

        api.bulkOverwriteGlobalApplicationCommands(builders)
                .join();
    }
}
