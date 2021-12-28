package me.LyoRex.StreetballBot.command.commands;

import me.LyoRex.StreetballBot.CommonFunctions;
import me.LyoRex.StreetballBot.JSONDataManager;
import me.LyoRex.StreetballBot.StreetballBot;
import me.LyoRex.StreetballBot.command.CommandContext;
import me.LyoRex.StreetballBot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.*;

/*
    Offense Shoot & Defense Blocks --> Defense Turn
    Offense Shoot & Defense Steals --> Offense gets +2
    Offense Cross & Defense Steals --> Defense Turn
    Offense Cross & Defense Blocks --> Offense gets to paint
        Offense Shoot & Defense Blocks --> Defense Turn
        Offense Shoot & Defence Steals --> Offense gets +1 and keep possession (fouled)
        Offense Pump Fake & Defense Blocks --> Offense gets +1 and keep possession (fouled)
        Offense Pump Fake & Defense Steals --> Defense Turn

    Shoot 0, Cross/Pump-fake 1
    Block 0, Steal 1
 */

public class StreetballGame implements ICommand
{
    private static final String COMMAND_NAME = "playstreetball";
    private static final String[] ALIASES = {
            "play",
            "challenge"
    };

    /* STAGE 1
     * OFFENSE 0 & DEFENSE 0 : NEXT TURN (Switch) ; Blocked
     * OFFENSE 0 & DEFENSE 1 : NEXT TURN (Switch) ; Offense +3 pts
     * OFFENSE 1 & DEFENSE 0 : NEXT STAGE (Stage 2) ; Offense gets into paint
     * OFFENSE 1 & DEFENSE 1 : NEXT TURN (Switch) ; Stolen
     */
    private static final String[] STAGE_1_OFFENSE_0_DEFENSE_0 = {
            "**%s's shot was sent flying!**",
            "**%s's 3 pointer was blocked out of bounds!**",
            "**DENIED! %s gets their shot blocked!**",
    };
    private static final String[] STAGE_1_OFFENSE_0_DEFENSE_1 = {
            "**%s drains the 3 pointer!**",
            "**%s hits the tough 3 with a hand in their face!**",
            "**%s steps back and drains the 3 pointer!**",
    };
    private static final String[] STAGE_1_OFFENSE_1_DEFENSE_0 = {
            "**%s goes behind the back to drive into the paint.**",
            "**%s spins away from the defense to get into the paint!**",
            "**Insane dribble moves! %s works their way to the basket!**"
    };
    private static final String[] STAGE_1_OFFENSE_1_DEFENSE_1 = {
            "**%s fumbles the ball out of bounds!**",
            "**The defense pokes the ball free, and %s turns it over.**",
            "**%s's loose handling causes them to turn the ball over!**"
    };

    /* STAGE 2
     * OFFENSE 0 & DEFENSE 0 : NEXT TURN (Switch) ; Blocked
     * OFFENSE 0 & DEFENSE 1 : NEXT TURN (Stay) ; Offense +2 pts (dunk/layup)
     * OFFENSE 1 & DEFENSE 0 : NEXT STAGE (Stay) ; Offense +2 pts (cross over floater/jump shot)
     * OFFENSE 1 & DEFENSE 1 : NEXT TURN (Switch) ; Stolen
     */
    private static final String[] STAGE_2_OFFENSE_0_DEFENSE_0 = {
            "**%s goes for the dunk but is stuffed at the rim!**",
            "**The defense swats %s's layup off the backboard!**",
            "**REJECTED! %s's layup was sent away!**"
    };
    private static final String[] STAGE_2_OFFENSE_0_DEFENSE_1 = {
            "**SLAM DUNK!! %s yams it all over the defense and gets the foul!**",
            "**AND 1! %s's reverse goes in while getting fouled by the defense!**",
            "**IF YOU DON'T LIKE THAT, YOU DON'T LIKE STREETBALL! %s gets the dunk AND one!!**"
    };
    private static final String[] STAGE_2_OFFENSE_1_DEFENSE_0 = {
            "**%s's pump fake gets the defense in the air and hits the tough floater with the foul!**",
            "**%s goes up and under and gets hit while draining the reverse layup!**",
            "**The pump fake gets the defense in the air! %s hits the fadeaway and gets bumped by the defense!**"
    };
    private static final String[] STAGE_2_OFFENSE_1_DEFENSE_1 = {
            "**%s's pump fake was read beautifully by the defense, leading to a turnover!**",
            "**The defense didn't bite on %s's pump fake, and the ball was stolen!**",
            "**COOKIES! %s gets the ball stolen right out of their hands!**"
    };

    public static TimerTask challengeTask = null;

    public static final String GAME_ALREADY_STARTED_ERROR = "There is already a Streetball match started in this server!";
    public static final String PLAYER_ALREADY_PLAYING_ERROR = "**%s** is already playing a match in another server!";

    private static int targetScore = 21;

    public static int threePointWeight = 3;
    public static int twoPointWeight = 2;

    public static ArrayList<TextChannel> gameChannel = new ArrayList<>();

    public static ArrayList<Boolean> gameStarted = new ArrayList<>();
    public static ArrayList<Member> player1 = new ArrayList<>();
    public static ArrayList<Member> player2 = new ArrayList<>();
    public static ArrayList<Member> curOffense = new ArrayList<>();
    public static ArrayList<Member> curDefense = new ArrayList<>();
    public static ArrayList<Integer> offenseChoice = new ArrayList<>();
    public static ArrayList<Integer> defenseChoice = new ArrayList<>();
    public static ArrayList<Integer> player1Score = new ArrayList<>();
    public static ArrayList<Integer> player2Score = new ArrayList<>();
    public static ArrayList<Integer> stage = new ArrayList<>();

    @Override
    public void handle(CommandContext ctx)
    {
        Guild guild = ctx.getGuild();                   // server message was sent in
        String guildId = guild.getId();                 // id of server message was sent in
        TextChannel textChannel = ctx.getChannel();     // text channel that message was sent in

        int guildIndex = CommonFunctions.getGuildIndex(guildId);
        if(guildIndex < 0)
            return;

        // GAME ALREADY STARTED IN GUILD
        if(gameStarted.get(guildIndex))
        {
            EmbedBuilder embed = EmbedUtils.embedMessage(GAME_ALREADY_STARTED_ERROR);
            textChannel.sendMessage(embed.build()).queue();
            return;
        }
        // Return if not 1 arg (Need at of player to challenge as arg)
        if(ctx.getArgs().size() != 1 && ctx.getArgs().size() != 2)
            return;
        String arg = ctx.getArgs().get(0).replaceAll("!", "");
        List<Member> members = ctx.getGuild().getMembers();
        if(ctx.getArgs().size() == 2)
        {
            String targetScoreString = ctx.getArgs().get(1);
            try {
                targetScore = Integer.parseInt(targetScoreString);
                if(targetScore < 5 || targetScore > 21)
                {
                    EmbedBuilder embed = EmbedUtils.embedMessage("The target score must be a number between 5 and 21!\nThe target score will default to 21...");
                    textChannel.sendMessage(embed.build()).queue();
                    targetScore = 21;
                }
            }
            catch (NumberFormatException e) {
                EmbedBuilder embed = EmbedUtils.embedMessage("The target score must be a number between 5 and 21!\nThe target score will default to 21...");
                textChannel.sendMessage(embed.build()).queue();
                targetScore = 21;
            }
        }
        else targetScore = 21;

        // Find player that was mentioned to go against
        for(Member m : members)
        {
            if(arg.equalsIgnoreCase(m.getAsMention()))
            {
                // make sure player being challenged isn't already being challenged
                // and they aren't already in a game
                for(Member member : player2)
                {
                    if(member == null)
                        continue;
                    if(member.getId().equals(m.getId()))
                    {
                        EmbedBuilder embed = EmbedUtils.embedMessage(String.format(PLAYER_ALREADY_PLAYING_ERROR, m.getEffectiveName()));
                        textChannel.sendMessage(embed.build()).queue();
                        resetGame(guildIndex);
                        return;
                    }
                }
                // set player2 at guildIndex to member that matches @mention in arg
                player2.set(guildIndex, m);
            }
        }

        // Make sure player that sent challenge message isn't already being challenged
        // and isn't already in a game
        for(Member member : player1)
        {
            if(member == null)
                continue;
            if(member.getId().equals(ctx.getMember().getId()))
            {
                EmbedBuilder embed = EmbedUtils.embedMessage(String.format(PLAYER_ALREADY_PLAYING_ERROR, ctx.getMember().getEffectiveName()));
                textChannel.sendMessage(embed.build()).queue();
                resetGame(guildIndex);
                return;
            }
        }

        // Make sure player being challenged is not a bot
        if(player2.get(guildIndex) == null || player2.get(guildIndex).getUser().isBot())
        {
            textChannel.sendMessage("There is no member named " + arg).queue();
            resetGame(guildIndex);
            return;
        }
        // Set player1 at guildIndex to player that sent challenge
        player1.set(guildIndex, ctx.getMember());
        if(player2.get(guildIndex) == player1.get(guildIndex))
        {
            textChannel.sendMessage("You can't challenege yourself").queue();
            resetGame(guildIndex);
            return;
        }

        String player1Name = player1.get(guildIndex).getAsMention();    // @ mention of player1
        String player2Name = arg;                                       // @ mention of player2

        // Send challenge message
        String challengeMessage = player2Name + " you've been challenged by " + player1Name + "\n" + "**The target score is " + targetScore + "**\n" + "Type 'accept' to accept\n" + "Type 'reject' to reject";

        EmbedBuilder embed = EmbedUtils.embedMessage(challengeMessage);

        textChannel.sendMessage(embed.build()).queue();

        // Set timer to cancel challenge after 10 seconds
        int finalGuildIndex = guildIndex;
        challengeTask = new TimerTask()
        {
            @Override
            public void run()
            {
                resetGame(finalGuildIndex);
                textChannel.sendMessage("The challenge (" + player1Name + " vs. " + player2Name + ") has timed out!").queue();
            }
        };

        new Timer().schedule(challengeTask, 20000);
    }

    /*
        Initiate game variable ArrayLists to default values
     */
    public static void initiateGameData()
    {
        for(int i = 0; i < StreetballBot.guilds.size(); i++)
        {
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
        }
    }

    /*
     * Reset game variables for specific guild
     */
    public static void resetGame(int guildIndex)
    {
        gameChannel.set(guildIndex, null);
        gameStarted.set(guildIndex, false);
        player1.set(guildIndex, null);
        player2.set(guildIndex, null);
        curOffense.set(guildIndex, null);
        curDefense.set(guildIndex, null);
        offenseChoice.set(guildIndex, -1);
        defenseChoice.set(guildIndex, -1);
        player1Score.set(guildIndex, 0);
        player2Score.set(guildIndex, 0);
        stage.set(guildIndex, 0);
    }

    /*
     *
     * Proceed to next stage of the game for specific guild
     * Stages:
     *  0 - challenging
     *  1 - top of key
     *  2 - in paint
     */
    public static void nextStage(int guildIndex)
    {
        // STAGE 0: Challenge accepted
        if(stage.get(guildIndex) == 0)
        {
            // SEND FIRST TURN MESSAGES
            String channelMessage = "It's " + curOffense.get(guildIndex).getEffectiveName() + "'s turn on offense.\n" + "They are starting at the top of the key";
            String offenseMessage = "It is your turn on offense at the top of the key." +
                    "\nType 'shoot' to shoot" +
                    "\nType 'cross' to drive";
            String defenseMessage = "It is " + curOffense.get(guildIndex).getEffectiveName() + " turn on offense at the top of the key.\n" + "Type 'block' to attempt to block the ball\n" + "Type 'steal' to attempt to steal the ball\n";

            EmbedBuilder channelEmbed = EmbedUtils.embedMessage(channelMessage);
            EmbedBuilder offenseEmbed = EmbedUtils.embedMessage(offenseMessage);
            EmbedBuilder defenseEmbed = EmbedUtils.embedMessage(defenseMessage);

            gameChannel.get(guildIndex).sendMessage(channelEmbed.build()).queue();

            CommonFunctions.sendPrivateEmbed(curOffense.get(guildIndex), offenseEmbed);
            CommonFunctions.sendPrivateEmbed(curDefense.get(guildIndex), defenseEmbed);
            stage.set(guildIndex, 1);
        }
        else
        {
            // Only continue if both players have made their decisions
            if((offenseChoice.get(guildIndex) == -1) || (defenseChoice.get(guildIndex) == -1))
            {
                return;
            }
            String offenseName = curOffense.get(guildIndex).getEffectiveName();
            String defenseName = curDefense.get(guildIndex).getEffectiveName();

            Guild curGuild = StreetballBot.guilds.get(guildIndex);
            String curDefenseId = curDefense.get(guildIndex).getId();
            String curOffenseId = curOffense.get(guildIndex).getId();

            final String KEY_ID = JSONDataManager.PLAYER_JSON_KEY_ID;
            final String KEY_NAME = JSONDataManager.PLAYER_JSON_KEY_NAME;
            final String KEY_WINS = JSONDataManager.PLAYER_JSON_KEY_WINS;
            final String KEY_LOSSES = JSONDataManager.PLAYER_JSON_KEY_LOSSES;
            final String KEY_FORFEITS = JSONDataManager.PLAYER_JSON_KEY_FORFEITS;
            final String KEY_POINTS = JSONDataManager.PLAYER_JSON_KEY_POINTS;
            final String KEY_TWOS = JSONDataManager.PLAYER_JSON_KEY_TWOS;
            final String KEY_THREES = JSONDataManager.PLAYER_JSON_KEY_THREES;
            final String KEY_TWOSA = JSONDataManager.PLAYER_JSON_KEY_TWOSA;
            final String KEY_THREESA = JSONDataManager.PLAYER_JSON_KEY_THREESA;
            final String KEY_BLOCKS = JSONDataManager.PLAYER_JSON_KEY_BLOCKS;
            final String KEY_STEALS = JSONDataManager.PLAYER_JSON_KEY_STEALS;
            final String KEY_AND1S = JSONDataManager.PLAYER_JSON_KEY_AND1S;
            final String KEY_FOULS = JSONDataManager.PLAYER_JSON_KEY_FOULS;

            // STAGE 1: Players at the top of the key
            if(stage.get(guildIndex) == 1)
            {
                String output;  // Message to send about turn outcome
                /*
                 * OFFENSE 0 & DEFENSE 0 : NEXT TURN (Switch) ; Blocked
                 * OFFENSE 0 & DEFENSE 1 : NEXT TURN (Switch) ; Offense +3 pts
                 * OFFENSE 1 & DEFENSE 0 : NEXT STAGE (Stage 2) ; Offense gets into paint
                 * OFFENSE 1 & DEFENSE 1 : NEXT TURN (Switch) ; Stolen
                 */
                if(offenseChoice.get(guildIndex) == 0 && defenseChoice.get(guildIndex) == 0)
                {
                    // long threesAtt = (long) JSONDataManager.getPlayerData(curGuild, curOffenseId, KEY_THREESA);
                    // JSONDataManager.setPlayerData(curGuild, curOffenseId, KEY_THREESA, threesAtt + 1);

                    // long blocks = (long) JSONDataManager.getPlayerData(curGuild, curDefenseId, KEY_BLOCKS);
                    // JSONDataManager.setPlayerData(curGuild, curDefenseId, KEY_BLOCKS, blocks + 1);

                    output = String.format(STAGE_1_OFFENSE_0_DEFENSE_0[(new Random()).nextInt(3)], offenseName);
                    sendTurnMessage(guildIndex, output);
                    nextTurn(guildIndex, 1);
                }
                else if(offenseChoice.get(guildIndex) == 0 && defenseChoice.get(guildIndex) == 1)
                {
                    // long threesAtt = (long) JSONDataManager.getPlayerData(curGuild, curOffenseId, KEY_THREESA);
                    // JSONDataManager.setPlayerData(curGuild, curOffenseId, KEY_THREESA, threesAtt + 1);

                    // long threes = (long) JSONDataManager.getPlayerData(curGuild, curOffenseId, KEY_THREES);
                    // JSONDataManager.setPlayerData(curGuild, curOffenseId, KEY_THREES, threes + 1);

                    // long points = (long) JSONDataManager.getPlayerData(curGuild, curOffenseId, KEY_POINTS);
                    // JSONDataManager.setPlayerData(curGuild, curOffenseId, KEY_POINTS, points + threePointWeight);

                    output = String.format(STAGE_1_OFFENSE_0_DEFENSE_1[(new Random()).nextInt(3)], offenseName);
                    sendTurnMessage(guildIndex, output);
                    if(player1.get(guildIndex).equals(curOffense.get(guildIndex)))
                        player1Score.set(guildIndex, player1Score.get(guildIndex) + threePointWeight);
                    else if(player2.get(guildIndex).equals(curOffense.get(guildIndex)))
                        player2Score.set(guildIndex, player2Score.get(guildIndex) + threePointWeight);
                    nextTurn(guildIndex, 1);
                }
                else if(offenseChoice.get(guildIndex) == 1 && defenseChoice.get(guildIndex) == 0)
                {
                    output = String.format(STAGE_1_OFFENSE_1_DEFENSE_0[(new Random()).nextInt(3)], offenseName);
                    sendTurnMessage(guildIndex, output);
                    stage.set(guildIndex, 2);

                    // Send messages prompting for decisions in the paint from both players
                    String channelMessage = offenseName + " has made their way into the paint.\n" + "What will the players do next?";

                    String offenseMessage = "You are now in the paint!" +
                            "\nType 'shoot' to attempt a shot" +
                            "\nType 'fake' to attempt a fake";

                    String defenseMessage = offenseName + " is now in the paint!\n" + "Type 'block' to attempt a block\n" + "Type 'steal' to attempt a steal";

                    EmbedBuilder channelEmbed = EmbedUtils.embedMessage(channelMessage);
                    EmbedBuilder offenseEmbed = EmbedUtils.embedMessage(offenseMessage);
                    EmbedBuilder defenseEmbed = EmbedUtils.embedMessage(defenseMessage);

                    gameChannel.get(guildIndex).sendMessage(channelEmbed.build()).queue();

                    CommonFunctions.sendPrivateEmbed(curOffense.get(guildIndex), offenseEmbed);

                    CommonFunctions.sendPrivateEmbed(curDefense.get(guildIndex), defenseEmbed);
                }
                else if(offenseChoice.get(guildIndex) == 1 && defenseChoice.get(guildIndex) == 1)
                {
                    // long steals = (long) JSONDataManager.getPlayerData(curGuild, curDefenseId, KEY_STEALS);
                    // JSONDataManager.setPlayerData(curGuild, curDefenseId, KEY_STEALS, steals + 1);

                    output = String.format(STAGE_1_OFFENSE_1_DEFENSE_1[(new Random()).nextInt(3)], offenseName);
                    sendTurnMessage(guildIndex, output);
                    nextTurn(guildIndex, 1);
                }
            }
            // STAGE 2: Players in the paint
            else if(stage.get(guildIndex) == 2)
            {
                String output;  // Message to send about turn outcome
                /*
                 * OFFENSE 0 & DEFENSE 0 : NEXT TURN (Switch) ; Blocked
                 * OFFENSE 0 & DEFENSE 1 : NEXT TURN (Stay) ; Offense +2 pts
                 * OFFENSE 1 & DEFENSE 0 : NEXT TURN (Stay) ; Offense +2 pts
                 * OFFENSE 1 & DEFENSE 1 : NEXT TURN (Switch) ; Stolen
                 */
                if(offenseChoice.get(guildIndex) == 0 && defenseChoice.get(guildIndex) == 0)
                {
                    // long twosAtt = (long) JSONDataManager.getPlayerData(curGuild, curOffenseId, KEY_TWOSA);
                    // JSONDataManager.setPlayerData(curGuild, curOffenseId, KEY_TWOSA, twosAtt + 1);

                    // long blocks = (long) JSONDataManager.getPlayerData(curGuild, curDefenseId, KEY_BLOCKS);
                    // JSONDataManager.setPlayerData(curGuild, curDefenseId, KEY_BLOCKS, blocks + 1);

                    output = String.format(STAGE_2_OFFENSE_0_DEFENSE_0[(new Random()).nextInt(3)], offenseName);
                    sendTurnMessage(guildIndex, output);
                    nextTurn(guildIndex, 1);
                }
                else if(offenseChoice.get(guildIndex) == 0 && defenseChoice.get(guildIndex) == 1)
                {
                    // long twosAtt = (long) JSONDataManager.getPlayerData(curGuild, curOffenseId, KEY_TWOSA);
                    // JSONDataManager.setPlayerData(curGuild, curOffenseId, KEY_TWOSA, twosAtt + 1);

                    // long twos = (long) JSONDataManager.getPlayerData(curGuild, curOffenseId, KEY_TWOS);
                    // JSONDataManager.setPlayerData(curGuild, curOffenseId, KEY_TWOS, twos + 1);

                    // long and1s = (long) JSONDataManager.getPlayerData(curGuild, curOffenseId, KEY_AND1S);
                    // JSONDataManager.setPlayerData(curGuild, curOffenseId, KEY_AND1S, and1s + 1);

                    // long fouls = (long) JSONDataManager.getPlayerData(curGuild, curDefenseId, KEY_FOULS);
                    // JSONDataManager.setPlayerData(curGuild, curDefenseId, KEY_FOULS, fouls + 1);

                    // long points = (long) JSONDataManager.getPlayerData(curGuild, curOffenseId, KEY_POINTS);
                    // JSONDataManager.setPlayerData(curGuild, curOffenseId, KEY_POINTS, points + twoPointWeight);

                    output = String.format(STAGE_2_OFFENSE_0_DEFENSE_1[(new Random()).nextInt(3)], offenseName);
                    sendTurnMessage(guildIndex, output);
                    if(player1.get(guildIndex).equals(curOffense.get(guildIndex)))
                        player1Score.set(guildIndex, player1Score.get(guildIndex) + twoPointWeight);
                    else if(player2.get(guildIndex).equals(curOffense.get(guildIndex)))
                        player2Score.set(guildIndex, player2Score.get(guildIndex) + twoPointWeight);
                    nextTurn(guildIndex, 0);
                }
                else if(offenseChoice.get(guildIndex) == 1 && defenseChoice.get(guildIndex) == 0)
                {
                    // long twosAtt = (long) JSONDataManager.getPlayerData(curGuild, curOffenseId, KEY_TWOSA);
                    // JSONDataManager.setPlayerData(curGuild, curOffenseId, KEY_TWOSA, twosAtt + 1);

                    // long twos = (long) JSONDataManager.getPlayerData(curGuild, curOffenseId, KEY_TWOS);
                    // JSONDataManager.setPlayerData(curGuild, curOffenseId, KEY_TWOS, twos + 1);

                    // long and1s = (long) JSONDataManager.getPlayerData(curGuild, curOffenseId, KEY_AND1S);
                    // JSONDataManager.setPlayerData(curGuild, curOffenseId, KEY_AND1S, and1s + 1);

                    // long fouls = (long) JSONDataManager.getPlayerData(curGuild, curDefenseId, KEY_FOULS);
                    // JSONDataManager.setPlayerData(curGuild, curDefenseId, KEY_FOULS, fouls + 1);

                    // long points = (long) JSONDataManager.getPlayerData(curGuild, curOffenseId, KEY_POINTS);
                    // JSONDataManager.setPlayerData(curGuild, curOffenseId, KEY_POINTS, points + twoPointWeight);

                    output = String.format(STAGE_2_OFFENSE_1_DEFENSE_0[(new Random()).nextInt(3)], offenseName);
                    sendTurnMessage(guildIndex, output);
                    if(player1.get(guildIndex).equals(curOffense.get(guildIndex)))
                        player1Score.set(guildIndex, player1Score.get(guildIndex) + twoPointWeight);
                    else if(player2.get(guildIndex).equals(curOffense.get(guildIndex)))
                        player2Score.set(guildIndex, player2Score.get(guildIndex) + twoPointWeight);
                    nextTurn(guildIndex, 0);
                }
                else if(offenseChoice.get(guildIndex) == 1 && defenseChoice.get(guildIndex) == 1)
                {
                    // long steals = (long) JSONDataManager.getPlayerData(curGuild, curDefenseId, KEY_STEALS);
                    // JSONDataManager.setPlayerData(curGuild, curDefenseId, KEY_STEALS, steals + 1);

                    output = String.format(STAGE_2_OFFENSE_1_DEFENSE_1[(new Random()).nextInt(3)], offenseName);
                    sendTurnMessage(guildIndex, output);
                    nextTurn(guildIndex, 1);
                }
            }
            // Reset turn variables
            offenseChoice.set(guildIndex, -1);
            defenseChoice.set(guildIndex, -1);
        }
    }

    // Send message indicating turn decisions
    public static void sendTurnMessage(int guildIndex, String output)
    {
        String guildMessageTitle = String.format("**%s** vs. **%s**", player1.get(guildIndex).getEffectiveName(), player2.get(guildIndex).getEffectiveName());
        String individualMessageTitle = String.format("**%s** vs. **%s** in **%s** server", player1.get(guildIndex).getEffectiveName(), player2.get(guildIndex).getEffectiveName(), StreetballBot.guilds.get(guildIndex).getName());

        EmbedBuilder guildMessageEmbed = EmbedUtils.embedMessageWithTitle(guildMessageTitle, output);
        EmbedBuilder individualMessageEmbed = EmbedUtils.embedMessageWithTitle(individualMessageTitle, output);

        // Send message to game channel
        gameChannel.get(guildIndex).sendMessage(guildMessageEmbed.build()).queue();

        // Send message to players
        CommonFunctions.sendPrivateEmbed(curOffense.get(guildIndex), individualMessageEmbed);
        CommonFunctions.sendPrivateEmbed(curDefense.get(guildIndex), individualMessageEmbed);
    }

    /* Move onto next turn
     *
     * guildIndex   :   int
     *      -index of guild of game being played
     * choice       :   int
     *      -indicates whether the same player stays on offense (0) or if offense and defense switch (0)
     */
    public static void nextTurn(int guildIndex, int choice)
    {
        // Check if a player reached the target score
        if(player1Score.get(guildIndex) >= targetScore || player2Score.get(guildIndex) >= targetScore)
        {
            // Base message to send
            String baseMessage = "GAME OVER!\n\n" + "The final score was: \n" + player1.get(guildIndex).getEffectiveName() + ": " + player1Score.get(guildIndex) + " - " + player2.get(guildIndex).getEffectiveName() + ": " + player2Score.get(guildIndex) + "\n";
            String channelMessage = baseMessage;    // Message to send to game channel
            String player1Message = baseMessage;    // Message to send to player 1
            String player2Message = baseMessage;    // Message to send to player 2

            Guild curGuild = StreetballBot.guilds.get(guildIndex);
            String player1Id = player1.get(guildIndex).getId();
            String player2Id = player2.get(guildIndex).getId();

            // Send winning message to player 1 if player 1 won
            // and losing message to player 2
            if(player1Score.get(guildIndex) >= targetScore)
            {
                channelMessage += player1.get(guildIndex).getEffectiveName() + " wins!!!";
                player1Message += "Congratulations!";
                player2Message += "Better luck next time :(";

                // long wins = (long) JSONDataManager.getPlayerData(curGuild, player1Id, JSONDataManager.PLAYER_JSON_KEY_WINS);
                // JSONDataManager.setPlayerData(curGuild, player1Id, JSONDataManager.PLAYER_JSON_KEY_WINS, wins);

                // long losses = (long) JSONDataManager.getPlayerData(curGuild, player2Id, JSONDataManager.PLAYER_JSON_KEY_LOSSES);
                // JSONDataManager.setPlayerData(curGuild, player2Id, JSONDataManager.PLAYER_JSON_KEY_LOSSES, losses);
            }
            // Send winning message to player 2 if player 2 won
            // and losing message to player 1
            else
            {
                channelMessage += player2.get(guildIndex).getEffectiveName() + " wins!!!";
                player1Message += "Better luck next time :(";
                player2Message += "Congratulations!";

                // long wins = (long) JSONDataManager.getPlayerData(curGuild, player2Id, JSONDataManager.PLAYER_JSON_KEY_WINS);
                // JSONDataManager.setPlayerData(curGuild, player2Id, JSONDataManager.PLAYER_JSON_KEY_WINS, wins);

                // long losses = (long) JSONDataManager.getPlayerData(curGuild, player1Id, JSONDataManager.PLAYER_JSON_KEY_LOSSES);
                // JSONDataManager.setPlayerData(curGuild, player1Id, JSONDataManager.PLAYER_JSON_KEY_LOSSES, losses);
            }
            // Send messages
            EmbedBuilder channelEmbed = EmbedUtils.embedMessage(channelMessage);
            EmbedBuilder player1Embed = EmbedUtils.embedMessage(player1Message);
            EmbedBuilder player2Embed = EmbedUtils.embedMessage(player2Message);

            gameChannel.get(guildIndex).sendMessage(channelEmbed.build()).queue();

            CommonFunctions.sendPrivateEmbed(player1.get(guildIndex), player1Embed);
            CommonFunctions.sendPrivateEmbed(player2.get(guildIndex), player2Embed);

            resetGame(guildIndex);
            return;
        }
        // Switch offense and defense if choice is 1
        if(choice == 1)
        {
            Member temp = curOffense.get(guildIndex);
            curOffense.set(guildIndex, curDefense.get(guildIndex));
            curDefense.set(guildIndex, temp);
        }
        // Reset stage to 0
        stage.set(guildIndex, 0);

        // Send messages
        String turnMessage = "The score is now\n" + player1.get(guildIndex).getEffectiveName() + ": " + player1Score.get(guildIndex) + " - " + player2.get(guildIndex).getEffectiveName() + ": " + player2Score.get(guildIndex) + "\n";
        String offenseMessage = "The score is now\n" + player1.get(guildIndex).getEffectiveName() + ": " + player1Score.get(guildIndex) + " - " + player2.get(guildIndex).getEffectiveName() + ": " + player2Score.get(guildIndex) + "\n";
        String defenseMessage = "The score is now\n" + player1.get(guildIndex).getEffectiveName() + ": " + player1Score.get(guildIndex) + " - " + player2.get(guildIndex).getEffectiveName() + ": " + player2Score.get(guildIndex) + "\n";

        EmbedBuilder turnEmbed = EmbedUtils.embedMessage(turnMessage);
        EmbedBuilder offenseEmbed = EmbedUtils.embedMessage(offenseMessage);
        EmbedBuilder defenseEmbed = EmbedUtils.embedMessage(defenseMessage);

        gameChannel.get(guildIndex).sendMessage(turnEmbed.build()).queue();

        CommonFunctions.sendPrivateEmbed(curOffense.get(guildIndex), offenseEmbed);
        CommonFunctions.sendPrivateEmbed(curDefense.get(guildIndex), defenseEmbed);

        nextStage(guildIndex);
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
