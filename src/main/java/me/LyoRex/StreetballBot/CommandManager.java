package me.LyoRex.StreetballBot;

import me.LyoRex.StreetballBot.command.CommandContext;
import me.LyoRex.StreetballBot.command.ICommand;
import me.LyoRex.StreetballBot.command.commands.StopStreetball;
import me.LyoRex.StreetballBot.command.commands.StreetballGame;
import me.LyoRex.StreetballBot.command.commands.StreetballHelp;
import me.LyoRex.StreetballBot.command.commands.StreetballStats;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CommandManager
{
    private final List<ICommand> commands = new ArrayList<>();

    public CommandManager()
    {
        addCommand(new StreetballGame());
        addCommand(new StopStreetball());
        addCommand(new StreetballHelp());
        addCommand(new StreetballStats());
    }

    private void addCommand(ICommand cmd)
    {
        boolean nameFound = this.commands.stream().anyMatch((it) -> it.getName().equalsIgnoreCase(cmd.getName()));
        if(nameFound)
            throw new IllegalArgumentException("A command with this name already exists");

        commands.add(cmd);
    }

    @Nullable
    private ICommand getCommand(String cmdName)
    {
        String nameLower = cmdName.toLowerCase();

        for(ICommand cmd : this.commands)
        {
            if(cmd.getName().equals(nameLower) || cmd.getAliases().contains(nameLower))
                return cmd;
        }

        return null;
    }

    void handle(GuildMessageReceivedEvent event)
    {
        String[] split = event.getMessage().getContentRaw().replaceFirst("(?i)" + Pattern.quote(System.getenv("PREFIX")), "").split("\\s");

        String invoke = split[0].toLowerCase();
        ICommand cmd = this.getCommand(invoke);

        if(cmd != null)
        {
            List<String> args = Arrays.asList(split).subList(1, split.length);

            CommandContext ctx = new CommandContext(event, invoke, args);

            cmd.handle(ctx);
        }
        else
        {
            event.getChannel().sendMessageFormat("The command '%s' does not exist", invoke).queue();
        }
    }
}
