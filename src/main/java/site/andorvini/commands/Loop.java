package site.andorvini.commands;

import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.players.Player;

import static site.andorvini.miscellaneous.MiscMethods.respondImmediatelyWithString;

public class Loop {
    public static void loop(SlashCommandInteraction interaction, Player currentPlayer){
        if (!currentPlayer.getLoopVar()){
            currentPlayer.setLoopVar(true);
            respondImmediatelyWithString(interaction,"Looping is now enabled");
        } else {
            currentPlayer.setLoopVar(false);
            respondImmediatelyWithString(interaction,"Looping is now disabled");
        }
    }
}
