package site.andorvini.commands;

import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.database.GreetingsDatabase;
import site.andorvini.miscellaneous.MiscMethods;

import java.awt.*;

public class RemoveGreetingCommand {
    public static void removeGreeting(SlashCommandInteraction interaction) {
        Long serverId = interaction.getServer().get().getId();
        Long userId = interaction.getUser().getId();

        if (GreetingsDatabase.checkIfGreetingExists(userId, serverId)) {
            GreetingsDatabase.removeGreeting(serverId, userId);

            EmbedBuilder succEmbed = new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setAuthor("Your greeting was deleted successfully!");

            MiscMethods.respondImmediatelyWithEmbed(interaction, succEmbed);
        } else {
            EmbedBuilder failEmbed = new EmbedBuilder()
                    .setColor(Color.red)
                    .setAuthor("No personal greeting found");

            MiscMethods.respondImmediatelyWithEmbed(interaction, failEmbed);
        }
    }
}
