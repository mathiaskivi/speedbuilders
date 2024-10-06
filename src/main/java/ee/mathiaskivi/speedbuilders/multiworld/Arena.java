package ee.mathiaskivi.speedbuilders.multiworld;

import ee.mathiaskivi.speedbuilders.utility.GameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;

public class Arena {
	public static ArrayList<Arena> arenaObjects = new ArrayList<>();
	public Scoreboard gameScoreboard;

	private String name;
	private int neededPlayers;
	private final ArrayList<String> players = new ArrayList<>();

	private int startTime;
	private int startTimerID;
	private float gameStartTime;
	private int gameStartTimerID;
	private int showcaseTime;
	private int showcaseTimerID;
	private float buildTime;
	private int buildTimerID;
	private float judgeTime;
	private int judgeTimerID1;
	private int judgeTimerID2;
	private int judgeTimerID3;
	private int judgeTimerID4;
	private int judgeTimerID5;
	private int judgeTimerID6;

	private ArmorStand judgedPlayerArmorStand = null;
	private ArrayList<String> unusedTemplates = new ArrayList<>();
	private ArrayList<String> usedTemplates = new ArrayList<>();
	private HashMap<Integer, String> currentBuildBlocks = new HashMap<>();
	private HashMap<String, Float> playersDoubleJumpCooldowned = new HashMap<>();
	private HashMap<String, Integer> playerPercent = new HashMap<>();
	private HashMap<String, Scoreboard> playerStartScoreboard = new HashMap<>();
	private HashMap<String, String> playersKit = new HashMap<>();
	private HashMap<String, String> plots = new HashMap<>();
	private int buildTimeSubtractor = 0;
	private int currentRound = 0;
	private int maxPlayers;
	private ElderGuardian elderGuardian = null;
	private String currentBuildDisplayName = null;
	private String currentBuildRawName = null;
	private String judgedPlayerName = null;
	private String firstPlace = null;
	private String secondPlace = null;
	private String thirdPlace = null;
	private GameState gameState;

	public Arena (String name, int startTime, int gameStartTime, int showcaseTime, int buildTime, int judgeTime, int neededPlayers) {
		this.name = name;
		this.startTime = startTime;
		this.gameStartTime = gameStartTime;
		this.showcaseTime = showcaseTime;
		this.buildTime = buildTime;
		this.judgeTime = judgeTime;
		this.neededPlayers = neededPlayers;

		arenaObjects.add(this);
	}

	public Scoreboard getGameScoreboard() {
		return gameScoreboard;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNeededPlayers() {
		return neededPlayers;
	}

	public void setNeededPlayers(int neededPlayers) {
		this.neededPlayers = neededPlayers;
	}

	public ArrayList<String> getPlayers() {
		return players;
	}

	public ElderGuardian getElderGuardian() {
		return elderGuardian;
	}

	public void setElderGuardian(ElderGuardian elderGuardian) {
		this.elderGuardian = elderGuardian;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getStartTimerID() {
		return startTimerID;
	}

	public void setStartTimerID(int startTimerID) {
		this.startTimerID = startTimerID;
	}

	public float getGameStartTime() {
		return gameStartTime;
	}

	public void setGameStartTime(float gameStartTime) {
		this.gameStartTime = gameStartTime;
	}

	public int getGameStartTimerID() {
		return gameStartTimerID;
	}

	public void setGameStartTimerID(int gameStartTimerID) {
		this.gameStartTimerID = gameStartTimerID;
	}

	public int getShowCaseTime() {
		return showcaseTime;
	}

	public void setShowCaseTime(int showcaseTime) {
		this.showcaseTime = showcaseTime;
	}

	public int getShowCaseTimerID() {
		return showcaseTimerID;
	}

	public void setShowCaseTimerID(int showcaseTimerID) {
		this.showcaseTimerID = showcaseTimerID;
	}

	public float getBuildTime() {
		return buildTime;
	}

	public void setBuildTime(float buildTime) {
		this.buildTime = buildTime;
	}

	public int getBuildTimerID() {
		return buildTimerID;
	}

	public void setBuildTimerID(int buildTimerID) {
		this.buildTimerID = buildTimerID;
	}

	public float getJudgeTime() {
		return judgeTime;
	}

	public void setJudgeTime(float judgeTime) {
		this.judgeTime = judgeTime;
	}

	public int getJudgeTimerID1() {
		return judgeTimerID1;
	}

	public void setJudgeTimerID1(int judgeTimerID1) {
		this.judgeTimerID1 = judgeTimerID1;
	}

	public int getJudgeTimerID2() {
		return judgeTimerID2;
	}

	public void setJudgeTimerID2(int judgeTimerID2) {
		this.judgeTimerID2 = judgeTimerID2;
	}

	public int getJudgeTimerID3() {
		return judgeTimerID3;
	}

	public void setJudgeTimerID3(int judgeTimerID3) {
		this.judgeTimerID3 = judgeTimerID3;
	}

	public int getJudgeTimerID4() {
		return judgeTimerID4;
	}

	public void setJudgeTimerID4(int judgeTimerID4) {
		this.judgeTimerID4 = judgeTimerID4;
	}

	public int getJudgeTimerID5() {
		return judgeTimerID5;
	}

	public void setJudgeTimerID5(int judgeTimerID5) {
		this.judgeTimerID5 = judgeTimerID5;
	}

	public int getJudgeTimerID6() {
		return judgeTimerID6;
	}

	public void setJudgeTimerID6(int judgeTimerID6) {
		this.judgeTimerID6 = judgeTimerID6;
	}

	public String getJudgedPlayerName() {
		return judgedPlayerName;
	}

	public void setJudgedPlayerName(String judgedPlayerName) {
		this.judgedPlayerName = judgedPlayerName;
	}

	public ArmorStand getJudgedPlayerArmorStand() {
		return judgedPlayerArmorStand;
	}

	public void setJudgedPlayerArmorStand(ArmorStand judgedPlayerArmorStand) {
		this.judgedPlayerArmorStand = judgedPlayerArmorStand;
	}

	public ArrayList<String> getUnusedTemplates() {
		return unusedTemplates;
	}

	public ArrayList<String> getUsedTemplates() {
		return usedTemplates;
	}

	public HashMap<Integer, String> getCurrentBuildBlocks() {
		return currentBuildBlocks;
	}

	public HashMap<String, Float> getPlayersDoubleJumpCooldowned() {
		return playersDoubleJumpCooldowned;
	}

	public HashMap<String, Integer> getPlayerPercent() {
		return playerPercent;
	}

	public HashMap<String, Scoreboard> getPlayerStartScoreboard() {
		return playerStartScoreboard;
	}

	public HashMap<String, String> getPlayersKit() {
		return playersKit;
	}

	public HashMap<String, String> getPlots() {
		return plots;
	}

	public int getBuildTimeSubtractor() {
		return buildTimeSubtractor;
	}

	public void setBuildTimeSubtractor(int buildTimeSubtractor) {
		this.buildTimeSubtractor = buildTimeSubtractor;
	}

	public int getCurrentRound() {
		return currentRound;
	}

	public void setCurrentRound(int currentRound) {
		this.currentRound = currentRound;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public String getCurrentBuildDisplayName() {
		return currentBuildDisplayName;
	}

	public void setCurrentBuildDisplayName(String currentBuildDisplayName) {
		this.currentBuildDisplayName = currentBuildDisplayName;
	}

	public String getCurrentBuildRawName() {
		return currentBuildRawName;
	}

	public void setCurrentBuildRawName(String currentBuildRawName) {
		this.currentBuildRawName = currentBuildRawName;
	}

	public String getFirstPlace() {
		return firstPlace;
	}

	public void setFirstPlace(String firstPlace) {
		this.firstPlace = firstPlace;
	}

	public String getSecondPlace() {
		return secondPlace;
	}

	public void setSecondPlace(String secondPlace) {
		this.secondPlace = secondPlace;
	}

	public String getThirdPlace() {
		return thirdPlace;
	}

	public void setThirdPlace(String thirdPlace) {
		this.thirdPlace = thirdPlace;
	}

	public GameState getGameState() {
		return gameState;
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	public void sendMessage(String message) {
		for (String arenaPlayer : players) {
			Bukkit.getPlayer(arenaPlayer).sendMessage(message);
		}
	}
}
