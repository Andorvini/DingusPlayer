package site.andorvini.commands;

import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.players.Player;

import static site.andorvini.miscellaneous.MiscMethods.respondImmediatelyWithString;

public class Pause {
    public static void pause(SlashCommandInteraction interaction, Player currentPlayer){
        if (currentPlayer.getPause()) {
            respondImmediatelyWithString(interaction, "Unpaused");

            currentPlayer.setPause(false);
        } else {
            respondImmediatelyWithString(interaction, "Paused");

            currentPlayer.setPause(true);
        }
    }
}
