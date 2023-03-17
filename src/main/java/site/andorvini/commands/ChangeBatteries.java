package site.andorvini.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import site.andorvini.miscellaneous.BatteryChanger;

import java.util.NoSuchElementException;

import static site.andorvini.Main.removeGreetingPlayer;

public class ChangeBatteries {
    public static void change(DiscordApi api, Server interactionServer){
        Long interactionServerId = interactionServer.getId();

        BatteryChanger.setEnabled(false);
        try {
            api.getYourself().getConnectedVoiceChannel(interactionServer).get().disconnect();
        } catch (NoSuchElementException ignored){}

        removeGreetingPlayer(interactionServerId);
    }
}
