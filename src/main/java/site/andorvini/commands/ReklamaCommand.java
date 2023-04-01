package site.andorvini.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.interaction.SlashCommandInteraction;

public class ReklamaCommand {
    public static void reklama(DiscordApi api, SlashCommandInteraction interaction) {
        String id = interaction.getOptionByName("messageId").get().getStringValue().get();

        String[] ids = id.split("-");

        String textChannelId = ids[0];
        String messageId = null;

        try {
            messageId = ids[1];
        } catch (Exception e) {
            interaction.createImmediateResponder()
                    .setFlags(MessageFlag.EPHEMERAL)
                    .setContent("Invalid ID format (hold shift while copying ID)")
                    .respond()
                    .join();
        }
        if (messageId != null) {
            TextChannel channel = null;
            try {
                channel = api.getTextChannelById(textChannelId).get();
            } catch (Exception e) {
                interaction.createImmediateResponder()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .setContent("Invalid TextChannel ID")
                        .respond()
                        .join();
            }

            Message message = null;

            if (channel != null) {
                try {
                    message = api.getMessageById(messageId, channel).join().getAuthor().getMessage();
                } catch (Exception e) {
                    interaction.createImmediateResponder()
                            .setFlags(MessageFlag.EPHEMERAL)
                            .setContent("Invalid Message ID")
                            .respond()
                            .join();
                }

                if (message != null) {
                    message.addReaction("1_zdes:1085264087111450704");
                    message.addReaction("2_mogla:1085264114001137756");
                    message.addReaction("3_byt:1085264143885533195");
                    message.addReaction("4_vasha:1085264165947588658");
                    message.addReaction("5_reklam:1085264190157111368");
                    message.addReaction("6_a:1085264216539267153");

                    interaction.createImmediateResponder()
                            .setFlags(MessageFlag.EPHEMERAL)
                            .setContent("Reklama")
                            .respond()
                            .join();
                }
            }
        }
    }
}
