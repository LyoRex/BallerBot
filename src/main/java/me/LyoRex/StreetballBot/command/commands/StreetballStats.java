package me.LyoRex.StreetballBot.command.commands;

import me.LyoRex.StreetballBot.JSONDataManager;
import me.LyoRex.StreetballBot.StreetballBot;
import me.LyoRex.StreetballBot.command.CommandContext;
import me.LyoRex.StreetballBot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Arrays;
import java.util.List;

public class StreetballStats implements ICommand
{
    private static final String COMMAND_NAME = "streetballstats";
    private static final String[] ALIASES = {
            "stats",
            "stat"
    };

    @Override
    public void handle(CommandContext ctx)
    {
        Member member = ctx.getMember();
        String playerId = member.getUser().getId();
        Guild guild = ctx.getGuild();
        TextChannel textChannel = ctx.getChannel();
        String[] args = ctx.getArgs().toArray(new String[0]);

        if(member.getUser().isBot())
            return;
        if(!StreetballBot.guilds.contains(guild))
            return;
        if(args.length == 0)
        {
            // String id = (String) JSONDataManager.getPlayerData(guild, playerId, JSONDataManager.PLAYER_JSON_KEY_ID);
            // Long wins = (Long) JSONDataManager.getPlayerData(guild, playerId, JSONDataManager.PLAYER_JSON_KEY_WINS);
            // Long losses = (Long) JSONDataManager.getPlayerData(guild, playerId, JSONDataManager.PLAYER_JSON_KEY_LOSSES);
            // Long forfeits = (Long) JSONDataManager.getPlayerData(guild, playerId, JSONDataManager.PLAYER_JSON_KEY_FORFEITS);
            // Long points = (Long) JSONDataManager.getPlayerData(guild, playerId, JSONDataManager.PLAYER_JSON_KEY_POINTS);
            // Long twos = (Long) JSONDataManager.getPlayerData(guild, playerId, JSONDataManager.PLAYER_JSON_KEY_TWOS);
            // Long threes = (Long) JSONDataManager.getPlayerData(guild, playerId, JSONDataManager.PLAYER_JSON_KEY_THREES);
            // Long twosatt = (Long) JSONDataManager.getPlayerData(guild, playerId, JSONDataManager.PLAYER_JSON_KEY_TWOSA);
            // Long threesatt = (Long) JSONDataManager.getPlayerData(guild, playerId, JSONDataManager.PLAYER_JSON_KEY_THREESA);
            // Long blocks = (Long) JSONDataManager.getPlayerData(guild, playerId, JSONDataManager.PLAYER_JSON_KEY_BLOCKS);
            // Long steals = (Long) JSONDataManager.getPlayerData(guild, playerId, JSONDataManager.PLAYER_JSON_KEY_STEALS);
            // Long and1s = (Long) JSONDataManager.getPlayerData(guild, playerId, JSONDataManager.PLAYER_JSON_KEY_AND1S);
            // Long fouls = (Long) JSONDataManager.getPlayerData(guild, playerId, JSONDataManager.PLAYER_JSON_KEY_FOULS);

            // id = id == null ? "-1" : id;
            // wins = wins == null ? -1 : wins;
            // losses = losses == null ? -1 : losses;
            // forfeits = forfeits == null ? -1 : forfeits;
            // points = points == null ? -1 : points;
            // twos = twos == null ? -1 : twos;
            // threes = threes == null ? -1 : threes;
            // twosatt = twosatt == null ? -1 : twosatt;
            // threesatt = threesatt == null ? -1 : threesatt;
            // blocks = blocks == null ? -1 : blocks;
            // steals = steals == null ? -1 : steals;
            // and1s = and1s == null ? -1 : and1s;
            // fouls = fouls == null ? -1 : fouls;

            // EmbedBuilder statsEmbed = EmbedUtils.embedMessageWithTitle("Streetball Stats for " + member.getEffectiveName(), "User ID: " + playerId);
            // statsEmbed.addField("Wins", wins.toString(), true);
            // statsEmbed.addField("Losses", losses.toString(), true);
            // statsEmbed.addField("Forfeits", forfeits.toString(), true);
            // statsEmbed.addField("Points", points.toString(), true);
            // statsEmbed.addField("Twos", twos.toString(), true);
            // statsEmbed.addField("Twos Att.", twosatt.toString(), true);
            // statsEmbed.addField("Threes", threes.toString(), true);
            // statsEmbed.addField("Threes Att.", threesatt.toString(), true);
            // statsEmbed.addField("Blocks", blocks.toString(), true);
            // statsEmbed.addField("Steals", steals.toString(), true);
            // statsEmbed.addField("And 1s", and1s.toString(), true);
            // statsEmbed.addField("Fouls", fouls.toString(), true);

            // textChannel.sendMessage(statsEmbed.build()).queue();
        }
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
