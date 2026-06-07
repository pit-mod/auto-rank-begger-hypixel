package com.example.begger.context;

/**
 * All possible states for the lobby-navigation failsafe system.
 * Extracted as a top-level enum so every manager can reference it
 * without depending on BeggerHandler.
 */
public enum FailsafeState {
    BEGGING,
    LIMBO,
    LOBBY_WAIT,
    LOBBY_OPEN_COMPASS,
    LOBBY_SELECT_BW,
    BW_LOBBY_WAIT,
    BW_OPEN_SELECTOR,
    BW_SELECT_LOBBY1
}
