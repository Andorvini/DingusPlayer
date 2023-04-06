package site.andorvini.commands;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.database.GreetingsDatabase;
import site.andorvini.miscellaneous.MiscMethods;

import java.awt.*;

public class GreetingListCommand {
    public static void greetingList(SlashCommandInteraction interaction) {
        Long serverId = interaction.getServer().get().getId();
        Long userId = interaction.getUser().getId();

        if (GreetingsDatabase.checkIfGreetingExists(userId, serverId)) {
            String url = GreetingsDatabase.getGreetingUrl(serverId, userId);

            EmbedBuilder listEmbed = new EmbedBuilder()
                    .setColor(Color.green)
                    .setAuthor("Greeting found")
                    .addField("URL:", url);

            MiscMethods.respondImmediatelyWithEmbed(interaction, listEmbed);
        } else {
            EmbedBuilder failEmbed = new EmbedBuilder()
                    .setColor(Color.red)
                    .setAuthor("No personal greeting found");

            MiscMethods.respondImmediatelyWithEmbed(interaction, failEmbed);
        }
    }
}
