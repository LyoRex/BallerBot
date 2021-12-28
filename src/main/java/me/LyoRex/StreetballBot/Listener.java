package me.LyoRex.StreetballBot;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class Listener extends ListenerAdapter
{
    private final CommandManager manager = new CommandManager();

    @Override
    public void onReady(@Nonnull ReadyEvent event)
    {
        System.out.println("API is ready!");
        StreetballBot.initiateBot(event.getJDA());
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event)
    {
        // Add guild to StreetballBot.guilds if not already in there
        if(!StreetballBot.guilds.contains(event.getGuild()))
        {
            StreetballBot.addGuildToData(event.getGuild());
            System.out.println("Added " + event.getGuild().getName() + " to list of guilds!");
            System.out.println("List of guilds: " + StreetballBot.guilds);
        }

        User user = event.getAuthor();

        if(user.isBot() || event.isWebhookMessage())
            return;

        String prefix = System.getenv("PREFIX");
        String msg = event.getMessage().getContentRaw();

        if(msg.startsWith(prefix) && !msg.startsWith("~~"))
        {
            manager.handle(event);
        }
    }
}
