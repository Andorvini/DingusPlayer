package site.andorvini.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandInteraction;

import site.andorvini.miscellaneous.MiscMethods;
import site.andorvini.miscellaneous.YoutubeMethods;
import site.andorvini.players.Player;
import site.andorvini.queue.Queue;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;

import static site.andorvini.miscellaneous.MiscMethods.*;
import static site.andorvini.miscellaneous.YoutubeMethods.*;
import static site.andorvini.miscellaneous.YoutubeMethods.getYoutubeVideoTitleOrDurationFromUrl;

public class Play {
    public static void play(DiscordApi api, SlashCommandInteraction interaction, Server interactionServer, Optional<ServerVoiceChannel> optionalUserVoiceChannel, Queue currentQueue, Optional<ServerVoiceChannel> optionalBotVoiceChannel, Player currentPlayer, ServerVoiceChannel userVoiceChannel){
        if (optionalUserVoiceChannel.isPresent()) {
            String commandOption = interaction.getOptionByName("query").get().getStringValue().get().replaceAll("\\[", "%5B").replaceAll("]", "%5D");
            String trackUrl = null;

            if (isUrl(commandOption)) {
                trackUrl = commandOption;
            } else {
                try {
                    trackUrl = getVideoUrlFromName(commandOption);
                } catch (Exception e){
                    EmbedBuilder searchFailEmbed = new EmbedBuilder()
                            .setColor(Color.RED)
                            .setAuthor("Nothing was found with your query")
                            .addField("Try another query", "");

                    MiscMethods.respondImmediatelyWithEmbed(interaction, searchFailEmbed);
                }
            }

            if (trackUrl != null) {
                currentQueue.addTrackToQueue(trackUrl);
                if (currentQueue.getQueueList().size() > 1) {
                    EmbedBuilder embed;

                    if (isYouTubeLink(trackUrl)) {
                        String title = null;
                        String duration = null;

                        try {
                            title = getYoutubeVideoTitleOrDurationFromUrl(trackUrl, true);
                            duration = getYoutubeVideoTitleOrDurationFromUrl(trackUrl, false);
                        } catch (IOException ignored) {
                        }

                        embed = new EmbedBuilder()
                                .setAuthor("Added to queue: ")
                                .addField("", "[" + title + "](" + trackUrl + ") | `" + duration + "`")
                                .setColor(Color.GREEN)
                                .setFooter("Track in queue: " + currentQueue.getQueueList().size());

                    } else {
                        embed = new EmbedBuilder()
                                .setAuthor("Added to queue: ")
                                .addField("", trackUrl)
                                .setColor(Color.GREEN)
                                .setFooter("Track in queue: " + currentQueue.getQueueList().size());
                    }

                    interaction.createImmediateResponder()
                            .addEmbed(embed)
//                                    .addComponents(ActionRow.of(Button.success("pause", "⏸"),
//                                            Button.create("skip", ButtonStyle.SUCCESS, "Skip")))
                            .respond()
                            .join();
                } else {
                    try {
                        if (isYouTubeLink(trackUrl)) {
                            EmbedBuilder playEmbed = new EmbedBuilder()
                                    .setAuthor("Playing: ")
                                    .addField("", "[" + YoutubeMethods.getYoutubeVideoTitleOrDurationFromUrl(trackUrl, true) + "](" + trackUrl + ") | `" + getYoutubeVideoTitleOrDurationFromUrl(trackUrl, false) + "`")
                                    .setColor(Color.GREEN)
                                    .setFooter("Track in queue: " + currentQueue.getQueueList().size());

                            respondImmediatelyWithEmbed(interaction, playEmbed);
                        } else {
                            EmbedBuilder playEmbed = new EmbedBuilder()
                                    .setAuthor("Playing: ")
                                    .addField("", trackUrl)
                                    .setColor(Color.GREEN)
                                    .setFooter("Track in queue: " + currentQueue.getQueueList().size());

                            respondImmediatelyWithEmbed(interaction, playEmbed);
                        }
                    } catch (IOException ignored){}
                }
            }

            if (optionalBotVoiceChannel.isEmpty()) {
                Server finalServer = interactionServer;
                userVoiceChannel.connect().thenAccept(audioConnection -> {
                    currentQueue.queueController(api, audioConnection, finalServer, currentPlayer);
                });
            } else {
                AudioConnection audioConnection = interactionServer.getAudioConnection().get();
                currentQueue.queueController(api, audioConnection, interactionServer, currentPlayer);
            }
        } else {
            respondImmediatelyWithString(interaction, "You are not connected to a voice channel");
        }
    }
}
