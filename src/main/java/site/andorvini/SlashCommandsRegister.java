package site.andorvini;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.util.Arrays;

public class SlashCommandsRegister {

    public static void registerSlashCommands(DiscordApi api){
        SlashCommand playCommand =
                SlashCommand.with("play","Play music from provided Youtube URL",
                                Arrays. asList(
                                        SlashCommandOption.create(SlashCommandOptionType.STRING, "query", "Link to music, or just name", true)
                                ))
                        .createGlobal(api)
                        .join();

        SlashCommand phonyCommand =
                SlashCommand.with("phony", "Play ANTIPATHY WORLD",
                                Arrays.asList(
                                        SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "version", "Choose version of song", true,
                                                Arrays.asList(
                                                        SlashCommandOptionChoice.create("Russian remix", "rus"),
                                                        SlashCommandOptionChoice.create("Original", "original")))
                                ))
                        .createGlobal(api)
                        .join();

        SlashCommand loopCommand = SlashCommand.with("loop","Lop music")
                .createGlobal(api)
                .join();

        SlashCommand leaveCommand =
                SlashCommand.with("leave","Leave voice channel")
                        .createGlobal(api)
                        .join();

        SlashCommand pauseCommand = SlashCommand.with("pause","Pause music")
                .createGlobal(api)
                .join();

        SlashCommand ssebloCommand = SlashCommand.with("sseblo","Convert text into voice using sseblobotapi",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "text", "Text you want to voice", true)
                        ))
                .createGlobal(api)
                .join();

        SlashCommand clearCommand = SlashCommand.with("clear","Delete specified number of messages",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.LONG,"count","Message count",true)
                        ))
                .createGlobal(api)
                .join();

        SlashCommand npCommand = SlashCommand.with("np","Show what song is playing now")
                .createGlobal(api)
                .join();

        SlashCommand randomUserCommand = SlashCommand.with("random","Pick random user")
                .createGlobal(api)
                .join();

        SlashCommand pingCommand = SlashCommand.with("ping", "Ping!")
                .createGlobal(api)
                .join();

        SlashCommand queueCommand = SlashCommand.with("queue", "Shows all tracks in queue")
                .createGlobal(api)
                .join();

        SlashCommand lyricsCommand = SlashCommand.with("lyrics", "Shows lyrics of currently playing track")
                .createGlobal(api)
                .join();

        SlashCommand volumeCommand = SlashCommand.with("volume","Set the volume",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.LONG, "volumelvl", "Volume level (Max. 1000)", true)
                        ))
                .createGlobal(api)
                .join();

        SlashCommand skipCommand = SlashCommand.with("skip", "Skips currently playing track")
                .createGlobal(api)
                .join();

        SlashCommand seekCommand = SlashCommand.with("seek", "Seeks the song to specified position",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "position", "Positon to seek", true)
                        ))
                .createGlobal(api)
                .join();

        SlashCommand devCommand = SlashCommand.with("dev", "For dev purposes",
                        Arrays.asList(
                                SlashCommandOption.create(SlashCommandOptionType.STRING, "devQuery", "For dev purposes", true)
                        ))
                .createGlobal(api)
                .join();

        SlashCommand changeBatteriesCommand = SlashCommand.with("change","For changing batteries")
                .createGlobal(api)
                .join();
    }
}
