package me.LyoRex.StreetballBot.command.commands;

import me.LyoRex.StreetballBot.command.CommandContext;
import me.LyoRex.StreetballBot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StreetballHelp implements ICommand
{
    public static final String COMMAND_NAME = "streetballhelp";

    private static final String[] rulesText = {
            "Streetball is played 1 v 1." +
            "\nOnly 1 match can be played at a time." +
            "\n" +
            "\nTo start a match, type '~playstreetball @[opponent_name]'" +
            "\n(or type ~play or ~challenge)" +
            "\n" +
            "\nTo accept a match, type 'accept'" +
            "\nTo reject a match, type 'reject'" +
            "\nYou can only accept/reject a match if you were challenged." +
            "\n" +
            "\nYou CANNOT challenged yourself/bots!" +
            "\n" +
            "\nTo end a match, either player can type 'stopstreetball'." +
            "\nTHIS WILL FORFEIT THE GAME AND THE OTHER PLAYER WINS!",
            
            "The challenger will always start on offense." +
            "\n" +
            "\nEach turn starts at the three point line at the top of the key." +
            "\n" +
            "\nThe offense has two choices: to shoot the ball, or to cross over and drive into the paint." +
            "\nThe defense has two choices: to attempt a block, or to attempt a steal." +
            "\n" +
            "\nIf the offense shoots the ball, and the defense attempts a block, " +
            "\nthe defense successfully blocks the ball, and now it's the defense's turn." +
            "\n" +
            "\nIf the offense shoots the ball, and the defense attempts a steal," +
            "\nthe offense successfully makes a three pointer (+3 points default), and now it's the defense's turn." +
            "\n" +
            "\nIf the offense crosses over, and the defense attempts a steal," +
            "\nthe defense successfully steals the ball, and now it's the defense's turn." +
            "\n" +
            "\nIf the offense crosses over, and the defense attempts a block," +
            "\nthe offense successfully crosses over into the paint, and the two players must make more decisions." +
            "\n" +
            "\n***************************************************************************************************" +
            "\n" +
            "\nThe offense now has two new choices: to shoot the ball, or to fake a shot." +
            "\nThe defense now has two new choices: to attempt a block, or to attempt a steal." +
            "\n" +
            "\nIf the offense shoots the ball, and the defense attempts a block, " +
            "\nthe defense successfully blocks the ball, and now it's the defense's turn." +
            "\n" +
            "\nIf the offense shoots the ball, and the defense attempts a steal," +
            "\nthe offense successfully makes a two pointer (+2 points default) while being fouled, so it is still the offense's turn." +
            "\n" +
            "\nIf the offense fakes a shot, and the defense attempts a steal," +
            "\nthe defense successfully steals the ball, and now it's the defense's turn." +
            "\n" +
            "\nIf the offense fakes a shot, and the defense attempts a block," +
            "\nthe offense successfully fakes a shot and draws a foul while sinking a two pointer (+2 points default), so it is still the offense's turn."
    };

    @Override
    public void handle(CommandContext ctx)
    {
        TextChannel channel = ctx.getChannel();
        Member member = ctx.getMember();

        if(member.getUser().isBot())
            return;
        if(ctx.getArgs().size() > 0)
            return;

        for(int i = 0; i < rulesText.length; i++)
        {
            EmbedBuilder helpEmbed = EmbedUtils.embedMessageWithTitle("Streetball Help [" + (i + 1) + "/" + rulesText.length + "]", rulesText[i]);
            if(i == 1)
            {
                File file = new File("src/pics/TurnTable.png");
                helpEmbed.setImage("attachment://table.png");
                channel.sendMessage(helpEmbed.build()).addFile(file, "table.png").queue();
            }
            else
                channel.sendMessage(helpEmbed.build()).queue();
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
        ArrayList<String> aliases = new ArrayList<>();
        aliases.add("help");

        return aliases;
    }
}
