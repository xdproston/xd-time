package ru.xdproston;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class SleepListener implements Listener
{
    private final JavaPlugin pluginInstance;

    public SleepListener(JavaPlugin pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK)
            return;

        Player player = event.getPlayer();
        World world = player.getWorld();

        if (world.getEnvironment() != World.Environment.NORMAL)
            return;

        PersistentDataContainer pdc = world.getPersistentDataContainer();
        int dayTime = pdc.getOrDefault(Main.dayTimeKey, PersistentDataType.INTEGER, -1);
        int nightTime = pdc.getOrDefault(Main.nightTimeKey, PersistentDataType.INTEGER, -1);

        if (dayTime <= 0 || nightTime <= 0)
            return;
        
        Bukkit.getScheduler().runTaskLater(pluginInstance, () -> {
            int sleepingPercentage = world.getGameRuleValue(org.bukkit.GameRule.PLAYERS_SLEEPING_PERCENTAGE);
            long sleepingPlayers = world.getPlayers().stream().filter(Player::isSleeping).count();
            long totalPlayersInWorld = world.getPlayers().size();

            long requiredPlayers = (long)Math.ceil(totalPlayersInWorld * (sleepingPercentage / 100.0));

            if (sleepingPlayers >= requiredPlayers) {
                TimeRunnable.worldPreciseTime.put(world.getUID(), 0.0);

                if (world.hasStorm()) world.setStorm(false);
                if (world.isThundering()) world.setThundering(false);
            }
        }, 1L);
    }
}
