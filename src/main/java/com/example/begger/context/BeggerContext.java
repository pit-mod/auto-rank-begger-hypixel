package com.example.begger.context;

import com.example.begger.system.Rank;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Centralised runtime state shared across all managers.
 * Managers receive this via constructor injection — no getter/setter spam.
 * Fields are public for direct access (intentional, same-project usage only).
 */
public class BeggerContext {

    // ── Timing ────────────────────────────────────────────────────
    public long lastGiftTime = 0;
    public long lastBegTime = 0;
    public long currentJitter = 0;
    public long sessionStartTime = 0;
    public long lastGiftAcceptedTime = 0;
    public long lastIdMessageTime = 0;
    public long lastRankUpdateTime = 0;

    // ── Identity / targeting ──────────────────────────────────────
    public String targetUsername = null;
    public String lastGifterName = null;
    public String lastSentMessage = "None";
    public String lastSentIdMessage = "970872";
    public Rank currentRank = Rank.NON;

    // ── State flags ───────────────────────────────────────────────
    public FailsafeState failsafeState = FailsafeState.BEGGING;
    public boolean idMessageSent = false;
    public boolean firstBegSent = false;
    public boolean reachedLobby1 = false;
    public boolean wasToggled = false;

    // ── Counters ──────────────────────────────────────────────────
    public int messagesSent = 0;
    public int messagesSinceLastGift = 0;

    // ── Shared collections ────────────────────────────────────────
    public final Map<String, Long> messageHistory = new HashMap<>();

    // ── Shared random (single instance for entire mod) ────────────
    public final Random random = new Random();

    /**
     * Full state reset — called when the mod is toggled off.
     */
    public void reset() {
        targetUsername = null;
        idMessageSent = false;
        firstBegSent = false;
        reachedLobby1 = false;
        failsafeState = FailsafeState.BEGGING;
    }
}
