package me.LyoRex.StreetballBot.command.commands;

import me.LyoRex.StreetballBot.CommonFunctions;
import me.LyoRex.StreetballBot.StreetballBot;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class StreetballListener extends ListenerAdapter
{
    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event)
    {
        Guild guild = event.getGuild();                         // Guild message was sent in
        String guildId = guild.getId();                         // Id of guild message was sent in
        String message = event.getMessage().getContentRaw();    // String message that was sent
        TextChannel textChannel = event.getChannel();           // Text Channel the message was sent in

        // Find index of guild in StreetballBot.guilds list
        int guildIndex = CommonFunctions.getGuildIndex(guildId);
        if(guildIndex == -1)
            return;

        Member member = event.getMember();                              // Member that sent the message
        Member challengee = StreetballGame.player2.get(guildIndex);     // Member that is being challenged

        // Return if member is a bot or if Game hasn't been started in the guild or if the match isn't at stage 0
        if(member.getUser().isBot())
            return;
        if(!StreetballGame.gameStarted.get(guildIndex) && StreetballGame.stage.get(guildIndex) != 0)
            return;
        if(StreetballGame.stage.get(guildIndex) == 0)
        {
            if(member.equals(challengee))
            {
                // Return if message sent wasn't 'accept' or 'reject'
                if(!message.equalsIgnoreCase("accept") && !message.equalsIgnoreCase("reject"))
                    return;
                // Start game if 'accept'
                if(message.equalsIgnoreCase("accept"))
                {
                    StreetballGame.gameChannel.set(guildIndex, textChannel);
                    String embedMessage = member.getEffectiveName() + " has accepted the match!";
                    EmbedBuilder embed = EmbedUtils.embedMessage(embedMessage);
                    textChannel.sendMessage(embed.build()).queue();
                    StreetballGame.gameStarted.set(guildIndex, true);
                    StreetballGame.curOffense.set(guildIndex, StreetballGame.player1.get(guildIndex));
                    StreetballGame.curDefense.set(guildIndex, StreetballGame.player2.get(guildIndex));
                    StreetballGame.nextStage(guildIndex);
                }
                // End game if 'reject'
                else if(message.equalsIgnoreCase("reject"))
                {
                    String embedMessage = member.getEffectiveName() + " has rejected the match!";
                    EmbedBuilder embed = EmbedUtils.embedMessage(embedMessage);
                    textChannel.sendMessage(embed.build()).queue();
                    StreetballGame.resetGame(guildIndex);
                }
                // Stop the 'stop game' task
                StreetballGame.challengeTask.cancel();
            }
        }
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event)
    {
        // Only runs is message is a DM
        if(event.isFromGuild())
            return;

        String message = event.getMessage().getContentRaw();    // Message received
        User user = event.getPrivateChannel().getUser();        // User that send the message
        Member curOffense = null;                               // Member currently on offense
        Member curDefense = null;                               // Member currently on defense

        // Find index in StreetballBot.guilds
        int guildIndex = -1;
        for(int i = 0; i < StreetballBot.guilds.size(); i++)
        {
            if(StreetballGame.curOffense.get(i) == null)
                continue;
            // Set curOffense and curDefense to corresponding members in StreetballBot curOffense and curDefense lists
            if(StreetballGame.curOffense.get(i).getUser().getId().equals(user.getId()) || StreetballGame.curDefense.get(i).getUser().getId().equals(user.getId()))
            {
                guildIndex = i;
                curOffense = StreetballGame.curOffense.get(guildIndex);
                curDefense = StreetballGame.curDefense.get(guildIndex);
                break;
            }
        }
        if(guildIndex < 0)
            return;

        // Return if message in guild OR user is a bot OR a game hasn't started in the respective server
        if(event.getChannelType().isGuild())
        {
            return;
        }
        if(user.isBot())
        {
            return;
        }
        if(!StreetballGame.gameStarted.get(guildIndex))
        {
            return;
        }

        // Stop game
        if(message.equalsIgnoreCase("stop") || message.equalsIgnoreCase("forfeit"))
        {
            String player1Name = StreetballGame.player1.get(guildIndex).getEffectiveName();
            String player2Name = StreetballGame.player2.get(guildIndex).getEffectiveName();
            String guildName = StreetballBot.guilds.get(guildIndex).getName();
            String endMessage = String.format("**%s** vs **%s** match in **%s** server was ended!", player1Name, player2Name, guildName);

            EmbedBuilder endEmbed = EmbedUtils.embedMessage(endMessage);

            StreetballGame.gameChannel.get(guildIndex).sendMessage("Now stopping match between " + player1Name + " and " + player2Name).queue();
            CommonFunctions.sendPrivateEmbed(StreetballGame.player1.get(guildIndex), endEmbed);
            CommonFunctions.sendPrivateEmbed(StreetballGame.player2.get(guildIndex), endEmbed);

            StreetballGame.resetGame(guildIndex);

            return;
        }

        // current stage of the match
        int stage = StreetballGame.stage.get(guildIndex);
        // Return if stage is 0 (Can't stop if game hasn't started
        if(stage == 0)
            return;
        List<String> offenseOptions = new ArrayList<>();
        List<String> defenseOptions = new ArrayList<>();
        // Top of the arc options:
        //      Offense: shoot & cross
        //      Defense: block & steal
        if(stage == 1)
        {
            offenseOptions.add("shoot");
            offenseOptions.add("cross");
            defenseOptions.add("block");
            defenseOptions.add("steal");
            if(!offenseOptions.contains(message.toLowerCase()) && !defenseOptions.contains(message.toLowerCase()))
            {
                return;
            }
        }
        // Paint options:
        //      Offense: shoot & cross
        //      Defense: block & steal
        else if(stage == 2)
        {
            offenseOptions.add("shoot");
            offenseOptions.add("fake");
            defenseOptions.add("block");
            defenseOptions.add("steal");
            if(!offenseOptions.contains(message.toLowerCase()) && !defenseOptions.contains(message.toLowerCase()))
            {
                return;
            }
        }

        // Set offense and defense choices corresponding to message sent
        if(user.getId().equals(curOffense.getUser().getId()))
        {
            if(!offenseOptions.contains(message.toLowerCase()))
                return;
            if(message.equalsIgnoreCase(offenseOptions.get(0)))
            {
                StreetballGame.offenseChoice.set(guildIndex, 0);
            }
            else if(message.equalsIgnoreCase(offenseOptions.get(1)))
            {
                StreetballGame.offenseChoice.set(guildIndex, 1);
            }
            StreetballGame.nextStage(guildIndex);
        }
        else if(user.getId().equals(curDefense.getUser().getId()))
        {
            if(!defenseOptions.contains(message.toLowerCase()))
                return;
            if(message.equalsIgnoreCase(defenseOptions.get(0)))
            {
                StreetballGame.defenseChoice.set(guildIndex, 0);
            }
            else if(message.equalsIgnoreCase(defenseOptions.get(1)))
            {
                StreetballGame.defenseChoice.set(guildIndex, 1);
            }
            StreetballGame.nextStage(guildIndex);
        }
    }
}
