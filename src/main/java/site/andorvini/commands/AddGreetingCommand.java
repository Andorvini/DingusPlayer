package site.andorvini.commands;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.database.GreetingsDatabase;
import site.andorvini.miscellaneous.MiscMethods;
import site.andorvini.miscellaneous.YoutubeMethods;

import java.awt.*;

public class AddGreetingCommand {
    public static void greetingCommand(SlashCommandInteraction interaction) {
        Long userId = interaction.getUser().getId();
        Long serverId = interaction.getServer().get().getId();
        String url = interaction.getOptionByName("greetingUrl").get().getStringValue().get();

        if (!GreetingsDatabase.checkIfGreetingExists(userId, serverId)) {
            if (MiscMethods.isUrl(url)) {
                if (YoutubeMethods.isYouTubeLink(url)) {
                    try {
                        if (MiscMethods.convertToMilliseconds(YoutubeMethods.getYoutubeVideoTitleFromUrl(url, false)) <= 30000) {
                            GreetingsDatabase.addUrl(userId, serverId, url);

                            EmbedBuilder addEmbed = new EmbedBuilder()
                                    .setColor(Color.green)
                                    .setAuthor("Added Greeting for " + interaction.getUser().getName());

                            MiscMethods.respondImmediatelyWithEmbed(interaction, addEmbed);
                        } else {
                            EmbedBuilder invalidDurationEmbed = new EmbedBuilder()
                                    .setColor(Color.red)
                                    .setAuthor("Video is too long, must be less then 30 seconds");

                            MiscMethods.respondImmediatelyWithEmbed(interaction, invalidDurationEmbed);
                        }
                    } catch (Exception ignored){}
                } else {
                    GreetingsDatabase.addUrl(userId, serverId, url);

                    EmbedBuilder addEmbed = new EmbedBuilder()
                            .setColor(Color.green)
                            .setAuthor("Added Greeting for " + interaction.getUser().getName());

                    MiscMethods.respondImmediatelyWithEmbed(interaction, addEmbed);
                }
            } else {
                EmbedBuilder notUrlEmbed = new EmbedBuilder()
                        .setColor(Color.red)
                        .setAuthor("Invalid URL");

                MiscMethods.respondImmediatelyWithEmbed(interaction, notUrlEmbed);
            }
        } else {
            EmbedBuilder alreadyExistsEmbed = new EmbedBuilder()
                    .setAuthor("Greeting dedicated to your username is already exists")
                            .setColor(Color.red);

            MiscMethods.respondImmediatelyWithEmbed(interaction, alreadyExistsEmbed);
        }
    }
}
