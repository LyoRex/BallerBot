package me.LyoRex.StreetballBot.command;

import me.duncte123.botcommons.commands.ICommandContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class CommandContext implements ICommandContext
{
    private final GuildMessageReceivedEvent event;
    private final String invoke;
    private final List<String> args;

    public CommandContext(GuildMessageReceivedEvent event, String invoke, List<String> args)
    {
        this.event = event;
        this.invoke = invoke;
        this.args = args;
    }

    @Override
    public Guild getGuild()
    {
        return this.getEvent().getGuild();
    }

    @Override
    public GuildMessageReceivedEvent getEvent()
    {
        return this.event;
    }

    public List<String> getArgs()
    {
        return this.args;
    }

    public String getInvoke()
    {
        return this.invoke;
    }
}
