package site.andorvini.handlers;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageUpdater;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.interaction.MessageComponentInteraction;
import site.andorvini.Main;
import site.andorvini.players.Player;

import java.util.HashMap;

public class ButtonHandler {
    public static void buttonHandler(){
        DiscordApi api = Main.getApi();

        api.addMessageComponentCreateListener(messageComponentCreateEvent -> {
            MessageComponentInteraction messageComponentInteraction = messageComponentCreateEvent.getMessageComponentInteraction();
            String customId = messageComponentInteraction.getCustomId();
            HashMap<Long, Player> players = Main.getPlayers();
            Player currentPlayer = players.get(messageComponentInteraction.getServer().get().getId());
            Message message = messageComponentInteraction.getMessage();

            if (customId.equals("pause")){
                currentPlayer.setPause(true);

                messageComponentInteraction.acknowledge();

                MessageUpdater messageUpdater = new MessageUpdater(message);

                messageUpdater.addComponents(ActionRow.of(Button.success("play", "▶, ▶, ▶, ▶, ▶, ▶, ▶")));
                messageUpdater.applyChanges();


            } else if (customId.equals("play")) {
                currentPlayer.setPause(false);

                messageComponentInteraction.acknowledge();
            }
        });
    }
}
