package me.LyoRex.StreetballBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

public class CommonFunctions
{
    // Send embed message to certain member
    public static void sendPrivateEmbed(Member member, EmbedBuilder embed)
    {
        member.getUser().openPrivateChannel().queue((userChannel) ->
        {
            userChannel.sendMessage(embed.build()).queue();
        });
    }

    // Get the index in StreetballGame.guilds of the guild that the message was sent in
    public static int getGuildIndex(String guildId)
    {
        int guildIndex = -1;
        for(int i = 0; i < StreetballBot.guilds.size(); i++)
        {
            if(StreetballBot.guilds.get(i) == null)
                continue;
            if(StreetballBot.guilds.get(i).getId().equals(guildId))
            {
                guildIndex = i;
                break;
            }
        }
        if(guildIndex < 0)
            return -1;
        return guildIndex;
    }
}
