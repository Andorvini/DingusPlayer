package site.andorvini.commands;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.Main;

import static site.andorvini.miscellaneous.MiscMethods.respondImmediatelyWithString;

public class Clear {
    public static void clear(SlashCommandInteraction interaction){
        long count = interaction.getOptionByName("count").get().getLongValue().get() + 1;
        TextChannel channel = interaction.getChannel().get();

        MessageSet messagesToDelete = channel.getMessages((int) count).join();

        respondImmediatelyWithString(interaction, "Deleted " + count + " messages");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();

            throw new RuntimeException(e);
        }

        channel.bulkDelete(messagesToDelete);
    }
}
