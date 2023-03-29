package site.andorvini.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import site.andorvini.miscellaneous.BatteryChanger;
import site.andorvini.miscellaneous.MiscMethods;

import java.util.NoSuchElementException;

import static site.andorvini.Main.removeGreetingPlayer;

public class ChangeBatteries {
    public static void change(DiscordApi api, Server interactionServer){
        Long interactionServerId = interactionServer.getId();

        BatteryChanger.setEnabled(false);
        try {
            MiscMethods.disconnectBot(api, interactionServer);
        } catch (NoSuchElementException ignored){}

        removeGreetingPlayer(interactionServerId);
    }
}
