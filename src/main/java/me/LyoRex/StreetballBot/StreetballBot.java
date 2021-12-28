package me.LyoRex.StreetballBot;

import me.LyoRex.StreetballBot.command.commands.StreetballGame;
import me.LyoRex.StreetballBot.command.commands.StreetballListener;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;

public class StreetballBot
{
    public static ArrayList<Guild> guilds = new ArrayList<>();

    public static JDA jda;

    private static final int DEFAULT_EMBED_COLOR = 0xdb9c1d;

    private StreetballBot() throws LoginException
    {
        EmbedUtils.setEmbedBuilder(
                () -> new EmbedBuilder()
                        .setColor(DEFAULT_EMBED_COLOR)
        );

        jda = JDABuilder.create(
                System.getenv("TOKEN"),
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_EMOJIS
        )
                .setActivity(Activity.watching("~help"))
                .addEventListeners(
                        new Listener(),
                        new StreetballListener()
                )
                .build();
    }

    public static void main(String[] args) throws LoginException
    {
        new StreetballBot();
    }

    public static void initiateBot(JDA jda)
    {
        for(Guild g : jda.getGuilds())
        {
            guilds.add(g);
        }
        StreetballGame.initiateGameData();
        // JSONDataManager.initiateGuilds(jda);
    }


    /*
     *
     * Add a guild to the list of guild data
     */
    public static void addGuildToData(Guild guild)
    {
        if(guilds.contains(guild))
            return;

        guilds.add(guild);

        StreetballGame.gameChannel.add(null);
        StreetballGame.gameStarted.add(false);
        StreetballGame.player1.add(null);
        StreetballGame.player2.add(null);
        StreetballGame.curOffense.add(null);
        StreetballGame.curDefense.add(null);
        StreetballGame.offenseChoice.add(-1);
        StreetballGame.defenseChoice.add(-1);
        StreetballGame.player1Score.add(0);
        StreetballGame.player2Score.add(0);
        StreetballGame.stage.add(0);

        // JSONDataManager.initiateGuilds(jda);
    }

    /*
     *
     * Remove guild from list of guild data
     */
    public static void removeGuildFromData(JDA jda, int guildIndex)
    {
        Guild guild = guilds.get(guildIndex);
        if(jda.getGuilds().contains(guild))
        {
            System.out.println("Tried to remove " + guilds.get(guildIndex).getName() + " from list of guild data, but the guild is still using this bot!");
            return;
        }

        guilds.remove(guildIndex);

        StreetballGame.gameChannel.remove(guildIndex);
        StreetballGame.gameStarted.remove(guildIndex);
        StreetballGame.player1.remove(guildIndex);
        StreetballGame.player2.remove(guildIndex);
        StreetballGame.curOffense.remove(guildIndex);
        StreetballGame.curDefense.remove(guildIndex);
        StreetballGame.offenseChoice.remove(guildIndex);
        StreetballGame.defenseChoice.remove(guildIndex);
        StreetballGame.player1Score.remove(guildIndex);
        StreetballGame.player2Score.remove(guildIndex);
        StreetballGame.stage.remove(guildIndex);
    }
}
