package site.andorvini.commands;

import org.javacord.api.interaction.SlashCommandInteraction;
import site.andorvini.miscellaneous.MiscMethods;

import java.util.ArrayList;
import java.util.Random;

public class RandomPrompt {
    public static void randomPrompt(SlashCommandInteraction interaction){
        ArrayList<String> prompts = DevCommand.readFileToList("prompts.txt");

        int size = prompts.size();

        String randomPrompt = prompts.get(new Random().nextInt(size));
        MiscMethods.respondImmediatelyWithString(interaction, randomPrompt);
    }
}
