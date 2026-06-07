package com.example.begger.context;

import com.example.begger.system.Rank;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BeggerContext {

    public long lastGiftTime = 0;
    public long lastBegTime = 0;
    public long currentJitter = 0;
    public long sessionStartTime = 0;
    public long lastGiftAcceptedTime = 0;
    public long lastIdMessageTime = 0;
    public long lastRankUpdateTime = 0;

    public String targetUsername = null;
    public String lastSentMessage = "None";
    public String lastSentIdMessage = "970872";
    public Rank currentRank = Rank.NON;

    public FailsafeState failsafeState = FailsafeState.BEGGING;
    public boolean idMessageSent = false;
    public boolean firstBegSent = false;
    public boolean reachedLobby1 = false;
    public boolean wasToggled = false;

    public int messagesSent = 0;
    public int messagesSinceLastGift = 0;

    public final Map<String, Long> messageHistory = new HashMap<>();

    public final Random random = new Random();

    public void reset() {
        targetUsername = null;
        idMessageSent = false;
        firstBegSent = false;
        reachedLobby1 = false;
        failsafeState = FailsafeState.BEGGING;
    }
}
