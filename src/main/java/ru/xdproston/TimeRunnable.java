package ru.xdproston;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class TimeRunnable extends BukkitRunnable
{
    private final Map<UUID, Double> worldPreciseTime = new HashMap<>();

    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != World.Environment.NORMAL)
                continue;

            PersistentDataContainer pdc = world.getPersistentDataContainer();
            int dayTime = pdc.getOrDefault(Main.dayTimeKey, PersistentDataType.INTEGER, -1);
            int nightTime = pdc.getOrDefault(Main.nightTimeKey, PersistentDataType.INTEGER, -1);

            if (dayTime <= 0 || nightTime <= 0)
                continue;

            double timeToAdd;
            long currentTime = world.getTime();

            if (currentTime >= 0 && currentTime < 12000)
                timeToAdd = 12000.0 / dayTime;
            else 
                timeToAdd = 12000.0 / nightTime;

            double preciseTime = worldPreciseTime.getOrDefault(world.getUID(), (double)currentTime);
            preciseTime += timeToAdd;

            if (preciseTime >= 24000)
                preciseTime -= 24000;

            world.setTime((long) preciseTime);
            worldPreciseTime.put(world.getUID(), preciseTime);
        }
    }
}
