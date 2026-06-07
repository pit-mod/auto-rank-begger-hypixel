package com.example.begger.manager;

import com.example.begger.util.McUtil;
import net.minecraft.client.settings.KeyBinding;

/**
 * Handles anti-AFK movement: periodically steps left/right to avoid idle kicks.
 */
public class AntiAfkManager {

    private final java.util.Random random;

    private long lastAfkMoveTime = System.currentTimeMillis();
    private int afkState = 0;
    private long afkActionStartTime = 0;
    private long nextAfkInterval = 100000;
    private long currentMoveDuration = 500;

    public AntiAfkManager(java.util.Random random) {
        this.random = random;
        randomizeAfk();
    }

    private void randomizeAfk() {
        nextAfkInterval = 80000 + random.nextInt(40000);
        currentMoveDuration = 300 + random.nextInt(500);
    }

    public void tick(long now) {
        if (now - lastAfkMoveTime >= nextAfkInterval) {
            afkState = 1;
            lastAfkMoveTime = now;
            afkActionStartTime = now;
            randomizeAfk();
        }
        if (afkState == 1) {
            KeyBinding.setKeyBindState(McUtil.mc().gameSettings.keyBindRight.getKeyCode(), true);
            if (now - afkActionStartTime >= currentMoveDuration) {
                KeyBinding.setKeyBindState(McUtil.mc().gameSettings.keyBindRight.getKeyCode(), false);
                afkState = 2;
                afkActionStartTime = now;
            }
        } else if (afkState == 2) {
            KeyBinding.setKeyBindState(McUtil.mc().gameSettings.keyBindLeft.getKeyCode(), true);
            if (now - afkActionStartTime >= currentMoveDuration) {
                KeyBinding.setKeyBindState(McUtil.mc().gameSettings.keyBindLeft.getKeyCode(), false);
                afkState = 0;
            }
        }
    }

    public void releaseAllKeys() {
        KeyBinding.setKeyBindState(McUtil.mc().gameSettings.keyBindRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(McUtil.mc().gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(McUtil.mc().gameSettings.keyBindUseItem.getKeyCode(), false);
        afkState = 0;
    }
}
