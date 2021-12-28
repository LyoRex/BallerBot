package me.LyoRex.StreetballBot.command.commands;

import me.LyoRex.StreetballBot.CommonFunctions;
import me.LyoRex.StreetballBot.StreetballBot;
import me.LyoRex.StreetballBot.command.CommandContext;
import me.LyoRex.StreetballBot.command.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Arrays;
import java.util.List;

public class StopStreetball implements ICommand
{
    private static final String COMMAND_NAME = "stopstreetball";
    private static final String[] ALIASES = {
            "stop",
            "forfeit"
    };

    @Override
    public void handle(CommandContext ctx)
    {
        Guild guild = ctx.getGuild();                   // Guild message was sent in
        String guildId = guild.getId();                 // Id of guild message was sent in
        TextChannel textChannel = ctx.getChannel();     // Text channel message was sent in

        // Get Streetball.guilds index of guild message was sent in
        int guildIndex = CommonFunctions.getGuildIndex(guildId);
        // guild not in list of guilds
        if(guildIndex < 0)
            return;

        // Return if game hasn't been started in the guild
        if(!StreetballGame.gameStarted.get(guildIndex))
        {
            textChannel.sendMessage("There are no games to stop at the moment.").queue();
            return;
        }
        // Send message that game has been ended
        textChannel.sendMessage("Now stopping match between " + StreetballGame.player1.get(guildIndex).getEffectiveName() + " and " + StreetballGame.player2.get(guildIndex).getEffectiveName()).queue();
        String player1Name = StreetballGame.player1.get(guildIndex).getEffectiveName();
        String player2Name = StreetballGame.player2.get(guildIndex).getEffectiveName();
        String guildName = StreetballBot.guilds.get(guildIndex).getName();
        String endMessage = String.format("**%s** vs **%s** match in **%s** server was ended!", player1Name, player2Name, guildName);
        StreetballGame.player1.get(guildIndex).getUser().openPrivateChannel().queue((channel) ->
        {
            channel.sendMessage(endMessage).queue();
        });
        StreetballGame.player2.get(guildIndex).getUser().openPrivateChannel().queue((channel) ->
        {
            channel.sendMessage(endMessage).queue();
        });

        // reset game in corresponding guild
        StreetballGame.resetGame(guildIndex);
    }

    @Override
    public String getName()
    {
        return COMMAND_NAME;
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList(ALIASES);
    }
}
