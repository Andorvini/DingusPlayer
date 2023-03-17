package site.andorvini.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.miscellaneous.MiscMethods;
import site.andorvini.players.Player;

import java.awt.*;

import static site.andorvini.miscellaneous.MiscMethods.formatDuration;

public class NowPlaying {
    public static void np(SlashCommandInteraction interaction, Player currentPlayer){
        AudioTrack audioTrackNowPlaying = currentPlayer.getAudioTrackNowPlaying();

        String loop = null;
        String pause = null;

        long duration = 0;
        long position = 0;
        int volume = 0;

        String identifier = null;

        if (currentPlayer.getLoopVar()) {
            loop = "Loop enabled";
        } else {
            loop = "Loop disabled";
        }

        if (currentPlayer.getPause()) {
            pause = "Now paused";
        } else {
            pause = "Playing";
        }

        if (audioTrackNowPlaying != null) {
            duration = audioTrackNowPlaying.getDuration();
            position = audioTrackNowPlaying.getPosition();
            volume = currentPlayer.getVolume();

            identifier = audioTrackNowPlaying.getIdentifier();

            if (!identifier.startsWith("http")) {
                identifier = "https://www.youtube.com/watch?v=" + identifier;
            }
        }

        EmbedBuilder embed = null;

        if (audioTrackNowPlaying == null) {
            embed = new EmbedBuilder()
                    .setAuthor("No playing track")
                    .setDescription("Use `/play` command to play track")
                    .setColor(Color.RED);
        } else {
            embed = new EmbedBuilder()
                    .setAuthor(audioTrackNowPlaying.getInfo().title, identifier, "https://indiefy.net/static/img/landing/distribution/icons/apple_music_icon.png")
                    .setTitle("Duration")
                    .setDescription(formatDuration(position) + " / " + formatDuration(duration))
                    .addInlineField("Volume", String.valueOf(volume))
                    .addInlineField("Loop", loop)
                    .addInlineField("Pause", pause)
                    .setColor(Color.ORANGE);
        }

        MiscMethods.respondImmediatelyWithEmbed(interaction, embed);
    }
}
