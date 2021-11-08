package org.geysermc.discordbot.listeners;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Class to listen to and delegate interaction (button or selection menu) responses
 */
@SuppressWarnings({"SwitchStatementWithTooFewBranches", "StatementWithEmptyBody"})
public class InteractionHandler extends ListenerAdapter {
    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        // Handle button clicks here
        switch (event.getComponentId()) {
            case "listeners:error_analyzer:clear" -> ErrorAnalyzer.clearMessage(event);
        }
    }

    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        // Handle selection menu choices here
        switch (event.getComponentId()) {
        }
    }
}