package site.andorvini.handlers;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandInteraction;

import site.andorvini.Main;
import site.andorvini.commands.*;
import site.andorvini.players.GreetingPlayer;
import site.andorvini.players.Player;
import site.andorvini.queue.Queue;

import java.io.IOException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Optional;

import static site.andorvini.miscellaneous.MiscMethods.respondImmediatelyWithEmbed;
import static site.andorvini.miscellaneous.MiscMethods.respondImmediatelyWithString;

public class SlashCommandHandler {
    public static void addSlashCommandsHadler(DiscordApi api, HashMap<Long, Player> players, HashMap<Long, site.andorvini.queue.Queue> queues, HashMap<Long, GreetingPlayer> greetingPlayers){
        api.addSlashCommandCreateListener(slashCommandCreateEvent -> {
            SlashCommandInteraction interaction = slashCommandCreateEvent.getSlashCommandInteraction();

            Server interactionServer = null;
            Long interactionServerId = null;

            String fullCommandName = interaction.getFullCommandName();

            Optional<ServerVoiceChannel> optionalUserVoiceChannel = null;
            ServerVoiceChannel userVoiceChannel = null;

            Optional<ServerVoiceChannel> optionalBotVoiceChannel = null;
            ServerVoiceChannel botVoiceChannel = null;

            try {
                interactionServer = slashCommandCreateEvent.getInteraction().getServer().get();
                interactionServerId = interactionServer.getId();

                optionalUserVoiceChannel = interaction.getUser().getConnectedVoiceChannel(interactionServer);
                userVoiceChannel = optionalUserVoiceChannel.get();

                optionalBotVoiceChannel = api.getYourself().getConnectedVoiceChannel(interactionServer);
                botVoiceChannel = optionalBotVoiceChannel.get();
            } catch (NoSuchElementException ignored) {}

            if (!queues.containsKey(interactionServerId)) {
                queues.put(interactionServerId, new site.andorvini.queue.Queue());
            }

            if (!greetingPlayers.containsKey(interactionServerId)) {
                greetingPlayers.put(interactionServerId, new GreetingPlayer());
            }

            if (!players.containsKey(interactionServerId)) {
                players.put(interactionServerId, new Player());
            }

            Queue currentQueue = queues.get(interactionServerId);
            GreetingPlayer currentGreetingPlayer = greetingPlayers.get(interactionServerId);
            Player currentPlayer = players.get(interactionServerId);

            Main.addLastTextChannel(interactionServerId, interaction.getChannel().get());

            if (!interaction.getChannel().get().getType().isServerChannelType()) {
                interaction.createImmediateResponder()
                        .setContent("Use this bot only on server!")
                        .respond()
                        .join();
            } else if (fullCommandName.equals("phony")) {
                Phony.phony(api, interaction, optionalUserVoiceChannel, currentPlayer, optionalBotVoiceChannel, interactionServer, userVoiceChannel, currentGreetingPlayer);
            } else if (fullCommandName.equals("play")) {
                Play.play(api, interaction, interactionServer, optionalUserVoiceChannel, currentQueue, optionalBotVoiceChannel, currentPlayer, userVoiceChannel);
            } else if (fullCommandName.equals("loop")) {
                Loop.loop(interaction, currentPlayer);
            } else if (fullCommandName.equals("leave")) {
                Leave.leave(api, interaction, interactionServer, optionalBotVoiceChannel, botVoiceChannel, interactionServerId, currentPlayer, currentQueue, players);
            } else if (fullCommandName.equals("sseblo")) {
                if (System.getenv("DP_SOSANIE_TTS_ENABLED").equals("true")){
                    Sseblo.sseblo(api, interaction, interactionServer, currentPlayer, currentGreetingPlayer, userVoiceChannel);
                }
            } else if (fullCommandName.equals("clear")) {
                Clear.clear(interaction);
            } else if (fullCommandName.equals("pause")) {
                Pause.pause(interaction, currentPlayer);
            } else if (fullCommandName.equals("np")) {
                NowPlaying.np(interaction, currentPlayer);
            } else if (fullCommandName.equals("random")) {
                Random.random(api, interaction, interactionServer, currentPlayer, currentGreetingPlayer, optionalBotVoiceChannel, optionalUserVoiceChannel);
            } else if (fullCommandName.equals("ping")) {
                respondImmediatelyWithString(interaction, "Pong!");
            } else if (fullCommandName.equals("lyrics")) {
                respondImmediatelyWithString(interaction, "Not implemented yet. (Because musixmatch shit)");
            } else if (fullCommandName.equals("volume")) {
                Volume.volume(api, interaction, currentPlayer, interactionServer, currentGreetingPlayer);
            } else if (fullCommandName.equals("queue")) {
                try {
                    respondImmediatelyWithEmbed(interaction, currentQueue.getQueueEmbed());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (fullCommandName.equals("skip")) {
                Skip.skip(api, interaction, interactionServer, optionalBotVoiceChannel, currentQueue, currentPlayer);
            } else if (fullCommandName.equals("seek")) {
                Seek.seek(interaction, currentPlayer);
            } else if (fullCommandName.equals("dev")) {
                DevCommand.triggerDevCommand(interaction, api);
            } else if (fullCommandName.equals("change")) {
                ChangeBatteries.change(api, interactionServer);
            } else if (fullCommandName.equals("randomprompt")){
                RandomPrompt.randomPrompt(interaction);
            } else if (fullCommandName.equals("reklama")) {
                ReklamaCommand.reklama(api, interaction);
            } else if (fullCommandName.equals("addgreeting")) {
                AddGreetingCommand.greetingCommand(interaction);
            } else if (fullCommandName.equals("greetingdel")) {
                RemoveGreetingCommand.removeGreeting(interaction);
            } else if (fullCommandName.equals("greetingls")) {
                GreetingListCommand.greetingList(interaction);
            }
        });
    }
}
