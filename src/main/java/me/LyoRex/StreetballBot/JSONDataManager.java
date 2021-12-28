package me.LyoRex.StreetballBot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class JSONDataManager
{
    public static final String STREETBALLBOT_DATA_PATH = "C:\\Users\\liamr\\Desktop\\StreetballBotData";       // FOLDER PATH OF STREETBALL DATA
    public static final String STREETBALLBOT_GUILD_DATA_FILENAME = "guildData.json";                            // FILE NAME FOR GUILD DATA
    public static final String STREETBALLBOT_PLAYER_DATA_FILENAME = "playerData.json";

    /**
     * JSON DATA KEYS FOR GUILD
     */
    public static final String GUILD_JSON_KEY_ID = "id";
    public static final String GUILD_JSON_KEY_NAME = "name";
    public static final String GUILD_JSON_KEY_MATCHES = "matches";

    /**
     * JSON DATA KEYS FOR PLAYERS
     */
    public static final String PLAYER_JSON_KEY_ID = "id";
    public static final String PLAYER_JSON_KEY_NAME = "name";
    public static final String PLAYER_JSON_KEY_WINS = "wins";
    public static final String PLAYER_JSON_KEY_LOSSES = "losses";
    public static final String PLAYER_JSON_KEY_FORFEITS = "forfeits";
    public static final String PLAYER_JSON_KEY_POINTS = "points";
    public static final String PLAYER_JSON_KEY_TWOS = "twos";
    public static final String PLAYER_JSON_KEY_THREES = "threes";
    public static final String PLAYER_JSON_KEY_TWOSA = "twosatt";
    public static final String PLAYER_JSON_KEY_THREESA = "threesatt";
    public static final String PLAYER_JSON_KEY_BLOCKS = "blocks";
    public static final String PLAYER_JSON_KEY_STEALS = "steals";
    public static final String PLAYER_JSON_KEY_AND1S = "and1s";
    public static final String PLAYER_JSON_KEY_FOULS = "fouls";

    /** Initiate the guild data folders for streetball
     *
     * @param jda   JDA object of the bot
     */
    public static void initiateGuilds(JDA jda)
    {
        List<Guild> guilds = StreetballBot.guilds;      // List of guilds registered in the StreetballBot
        File dir = new File(STREETBALLBOT_DATA_PATH);   // File Path for StreetballBot data

        System.out.println("***Initializing folders for guilds...***");

        // Loop through guilds
        for(Guild guild : guilds)
        {
            // Make folder for each guild
            String guildID = guild.getId();
            String guildFolderPath = dir + "\\" + guildID;
            File guildFolder = new File(guildFolderPath);

            // resetPlayerData(guildFolderPath, guild);

            // Stop if the guild's folder already exists
            if(Files.exists(Paths.get(guildFolderPath)))
            {
                System.out.println(guild.getName() + "'s Streetball Data folder already exists!");
                continue;
            }
            // Create guild folder
            boolean createdFolder = guildFolder.mkdir();
            if(createdFolder)
            {
                System.out.println(guild.getName() + "'s Streetball Data folder was successfully created!");
                // Create Guild and Player JSON files
                createGuildData(guildFolderPath, guild);
                createPlayerData(guildFolderPath, guild);
            }
            else
                System.out.println(guild.getName() + "'s Streetball Data folder could not be created!");
        }
    }

    /** Reset guild data for specific guild
     *
     * @param guildFolderPath   String path to guild's folder
     * @param guild             Guild object with guild data to reset
     */
    public static void resetGuildData(String guildFolderPath, Guild guild)
    {
        File guildStreetballGuildData = new File(guildFolderPath + "\\" + STREETBALLBOT_GUILD_DATA_FILENAME);
        System.out.println(guild.getName() + "'s Streetball Guild Data does not exist! Creating file now...");
        JSONObject guildData = new JSONObject();
        guildData.put(GUILD_JSON_KEY_ID, guild.getId());
        guildData.put(GUILD_JSON_KEY_NAME, guild.getName());
        guildData.put(GUILD_JSON_KEY_MATCHES, 0);
        try
        {
            FileWriter file = new FileWriter(guildStreetballGuildData);
            file.write(guildData.toString());
            file.flush();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Create guild data for a guild and add to database
     *
     * @param guildFolderPath   String path to guild's folder
     * @param guild             Guild object with guild data to create
     */
    public static void createGuildData(String guildFolderPath, Guild guild)
    {
        File guildStreetballGuildData = new File(guildFolderPath + "\\" + STREETBALLBOT_GUILD_DATA_FILENAME);
        if(guildStreetballGuildData.exists())
        {
            System.out.println(guild.getName() + "'s Streetball Guild Data already exists!");
        }
        else
        {
            resetGuildData(guildFolderPath, guild);
        }
    }

    /** Reset player data for specific guild
     *
     * @param guildFolderPath   String path to guild's folder
     * @param guild             Guild object with player data to reset
     */
    private static void resetPlayerData(String guildFolderPath, Guild guild)
    {
        File guildStreetballPlayerData = new File(guildFolderPath + "\\" + STREETBALLBOT_PLAYER_DATA_FILENAME);
        System.out.println(guild.getName() + "'s Streetball Player Data does not exist! Creating file now...");
        JSONArray memberList = new JSONArray();

        for(Member member : guild.getMembers())
        {
            if(member.getUser().isBot())
                continue;
            System.out.println("Writing " + member.getEffectiveName() + "'s Streetball Player Data from guild '" + guild.getName() + "'");
            JSONObject memberJSON = new JSONObject();
            memberJSON.put(PLAYER_JSON_KEY_ID, member.getId());
            memberJSON.put(PLAYER_JSON_KEY_NAME, member.getEffectiveName());
            memberJSON.put(PLAYER_JSON_KEY_WINS, 0);
            memberJSON.put(PLAYER_JSON_KEY_LOSSES, 0);
            memberJSON.put(PLAYER_JSON_KEY_FORFEITS, 0);
            memberJSON.put(PLAYER_JSON_KEY_POINTS, 0);
            memberJSON.put(PLAYER_JSON_KEY_TWOS, 0);
            memberJSON.put(PLAYER_JSON_KEY_THREES, 0);
            memberJSON.put(PLAYER_JSON_KEY_TWOSA, 0);
            memberJSON.put(PLAYER_JSON_KEY_THREESA, 0);
            memberJSON.put(PLAYER_JSON_KEY_BLOCKS, 0);
            memberJSON.put(PLAYER_JSON_KEY_STEALS, 0);
            memberJSON.put(PLAYER_JSON_KEY_AND1S, 0);
            memberJSON.put(PLAYER_JSON_KEY_FOULS, 0);
            memberList.add(memberJSON);
        }
        try
        {
            FileWriter file = new FileWriter(guildStreetballPlayerData);
            file.write(memberList.toString());
            file.flush();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Create player data for specific guild
     *
     * @param guildFolderPath   String path to guild's folder
     * @param guild             Guild object with player data to reset
     */
    public static void createPlayerData(String guildFolderPath, Guild guild)
    {
        File guildStreetballPlayerData = new File(guildFolderPath + "\\" + STREETBALLBOT_PLAYER_DATA_FILENAME);
        if(guildStreetballPlayerData.exists())
        {
            System.out.println(guild.getName() + "'s Streetball Player Data already exists!");
        }
        else
        {
            resetPlayerData(guildFolderPath, guild);
        }
    }

    /** Retrieve path
     *
     * @param guild Guild object to get folder path of
     * @return      String path of guild's folder
     */
    public static String getGuildFolderPath(Guild guild)
    {
        String guildId = guild.getId();

        if(!StreetballBot.guilds.contains(guild))
            return "";
        File dir = new File(STREETBALLBOT_DATA_PATH);
        File guildFolder = null;
        File[] files;
        FileFilter fileFilter = File::isDirectory;
        files = dir.listFiles(fileFilter);
        for(File file : files)
        {
            String fileDir = file.toString();
            String fileName = fileDir.replace((STREETBALLBOT_DATA_PATH + "\\"), "");
            if(fileName.equals(guildId))
            {
                guildFolder = new File(fileDir);
                break;
            }
        }
        if(guildFolder == null)
        {
            System.out.println(guild.getName() + "'s Streetball Data folder could not be opened!");
            return "";
        }
        else
            return guildFolder.toString();
    }

    /** Get JSONObject data of a specified guild
     *
     * @param guild             Guild object to get JSON data from
     * @return                  JSONObject containing the guild's guild data
     * @throws IOException
     * @throws ParseException
     */
    private static JSONObject getJSONObjectGuildData(Guild guild) throws IOException, ParseException
    {
        String guildFolder = getGuildFolderPath(guild);
        if(guildFolder.equals(""))
            return null;
        JSONParser jsonParser = new JSONParser();
        String guildDataFilePath = guildFolder + "\\" + STREETBALLBOT_GUILD_DATA_FILENAME;
        if(!(new File(guildDataFilePath)).exists())
            return null;
        FileReader guildDataFileReader = new FileReader(guildDataFilePath);
        Object obj = jsonParser.parse(guildDataFileReader);

        return (JSONObject) obj;
    }

    /** Get JSONObject data of a specified player from a specified guild using the player's ID
     *
     * @param guild             Guild object to get JSON data from
     * @param playerId          String Id of player to get data of
     * @return                  JSONObject containing the player data of a specified player
     * @throws IOException
     * @throws ParseException
     */
    private static JSONObject getJSONObjectPlayerData(Guild guild, String playerId) throws IOException, ParseException
    {
        String guildFolder = getGuildFolderPath(guild);
        if(guildFolder.equals(""))
            return null;
        JSONParser jsonParser = new JSONParser();
        String playerDataFilePath = guildFolder + "\\" + STREETBALLBOT_PLAYER_DATA_FILENAME;
        if(!(new File(playerDataFilePath)).exists())
            return null;
        FileReader guildDataFileReader = new FileReader(playerDataFilePath);
        Object obj = jsonParser.parse(guildDataFileReader);
        JSONArray playerArray = (JSONArray) obj;

        JSONObject playerJSON;
        for(Object player : playerArray)
        {
            playerJSON = (JSONObject) player;
            if(playerJSON.get("id").equals(playerId))
            {
                return playerJSON;
            }
        }
        return null;
    }

    /** Write data object to a file
     *
     * @param filePath      String path of file to write
     * @param data          Data to write to file
     * @throws IOException
     */
    private static void writeDataToFile(String filePath, Object data) throws IOException
    {
        FileWriter file = new FileWriter(filePath);
        file.write(data.toString());
        file.flush();
    }

    /** Set value of selected key in specified guild's guild data
     *
     * @param guild     Guild object to set guild data
     * @param key       Key of data to set
     * @param value     Value of data to set
     */
    public static void setGuildData(Guild guild, String key, Object value)
    {
        String guildFolder = getGuildFolderPath(guild);
        if(guildFolder.equals(""))
            return;

        try
        {
            JSONObject guildData = getJSONObjectGuildData(guild);
            if(guildData == null)
                return;
            guildData.put(key, value);

            writeDataToFile(guildFolder + "\\" + STREETBALLBOT_GUILD_DATA_FILENAME, guildData);
        }
        catch(ParseException | IOException e)
        {
            e.printStackTrace();
        }
    }

    /** Get value of selected key in specified guild's guild data
     *
     * @param guild     Guild object to get get guild data
     * @param key       Key of data to get
     * @return          Object containing data from file
     */
    public static Object getGuildData(Guild guild, String key)
    {
        String guildFolder = getGuildFolderPath(guild);
        if(guildFolder.equals(""))
            return -1;

        try
        {
            JSONObject guildData = getJSONObjectGuildData(guild);
            if(guildData == null)
                return null;

            return guildData.get(key);
        }
        catch(ParseException | IOException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    /** Set value of selected key in specified guild's specific player's data
     *
     * @param guild     Guild object to set guild data
     * @param playerId  String id of player with data to set
     * @param key       Key of data to set
     * @param value     Value of data to set
     */
    public static void setPlayerData(Guild guild, String playerId, String key, Object value)
    {
        String guildFolder = getGuildFolderPath(guild);
        if(guildFolder.equals(""))
            return;

        try
        {
            JSONParser jsonParser = new JSONParser();
            String playerDataFilePath = guildFolder + "\\" + STREETBALLBOT_PLAYER_DATA_FILENAME;
            if(!(new File(playerDataFilePath)).exists())
                return;
            FileReader guildDataFileReader = new FileReader(playerDataFilePath);
            Object obj = jsonParser.parse(guildDataFileReader);
            JSONArray playerArray = (JSONArray) obj;

            JSONObject playerJSON;
            for(Object player : playerArray)
            {
                playerJSON = (JSONObject) player;
                if(playerJSON.get("id").equals(playerId))
                {
                    playerJSON.put(key, value);
                    writeDataToFile(guildFolder + "\\" + STREETBALLBOT_PLAYER_DATA_FILENAME, playerArray);
                }
            }
            return;
        }
        catch(ParseException | IOException e)
        {
            e.printStackTrace();
        }
    }

    /** Get value of selected key in specified guild's specified player's data
     *
     * @param guild         Guild object to get get guild data
     * @param playerId      String id of player with data to get
     * @param key           Key of data to get
     * @return              Object containing data from file
     */
    public static Object getPlayerData(Guild guild, String playerId, String key)
    {
        String guildFolder = getGuildFolderPath(guild);
        if(guildFolder.equals(""))
            return null;
        try
        {
            JSONObject playerData = getJSONObjectPlayerData(guild, playerId);
            if(playerData == null)
                return null;
            return playerData.get(key);
        }
        catch(ParseException | IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
