package site.andorvini.commands;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.players.Player;

import java.awt.*;

import static site.andorvini.miscellaneous.MiscMethods.*;
import static site.andorvini.miscellaneous.MiscMethods.respondImmediatelyWithEmbed;

public class Seek {
    public static void seek(SlashCommandInteraction interaction, Player currentPlayer){
        String position = interaction.getOptionByName("position").get().getStringValue().get();

        if (isValidTimeFormat(position)) {
            long milis = convertToMilliseconds(position);
            if (!(milis > currentPlayer.getAudioTrackNowPlaying().getDuration())) {
                EmbedBuilder seekEmbed = new EmbedBuilder()
                        .addField("__**Seeking track: **__", currentPlayer.getAudioTrackNowPlaying().getInfo().title)
                        .addInlineField("Previous position: ", "`" + formatDuration(currentPlayer.getAudioTrackNowPlaying().getPosition()) + "`")
                        .addInlineField("New position: ", "`" + position + "`")
                        .setColor(Color.GREEN);

                currentPlayer.setPosition(milis);
                respondImmediatelyWithEmbed(interaction, seekEmbed);
            } else {
                EmbedBuilder seekLongFailureEmbed = new EmbedBuilder()
                        .addField("__**Incorrect position**__", "Track duration is `" + formatDuration(currentPlayer.getAudioTrackNowPlaying().getDuration()) + "`")
                        .setColor(Color.red);

                respondImmediatelyWithEmbed(interaction, seekLongFailureEmbed);
            }
        } else {
            EmbedBuilder seekFailureEmbed = new EmbedBuilder()
                    .addField("__**Incorrect position format**__", "Use `minutes:seconds` format")
                    .setColor(Color.red);

            respondImmediatelyWithEmbed(interaction, seekFailureEmbed);
        }
    }
}
