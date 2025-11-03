package ru.xdproston;

import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Main extends JavaPlugin
{
    public static final NamespacedKey dayTimeKey = new NamespacedKey("xd-time", "daytime");
    public static final NamespacedKey nightTimeKey = new NamespacedKey("xd-time", "nighttime");

    private static BukkitTask timeTask;

    public static BukkitTask getTimeTask() {
        return timeTask;
    }

    @Override
    public void onEnable() {
        timeTask = new TimeRunnable().runTaskTimer(this, 0L, 1L);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            MiniMessage mm = MiniMessage.miniMessage();

            commands.registrar().register(
                Commands.literal("xd-time")
                    .requires(context -> context.getSender().hasPermission("xd-time.control"))
                    .then(
                        Commands.argument("world", ArgumentTypes.world())
                            .then(
                                Commands.literal("timeinfo")
                                    .executes(context -> {
                                        World world = context.getArgument("world", World.class);
                                        CommandSender sender = context.getSource().getSender();

                                        if (world.getEnvironment() != World.Environment.NORMAL) {
                                            sender.sendMessage(mm.deserialize("<red>there is no cycle of day and night in this world, it doesn't make sense."));
                                            return Command.SINGLE_SUCCESS;
                                        }

                                        PersistentDataContainer pdc = world.getPersistentDataContainer();
                                        int dayTime = pdc.getOrDefault(dayTimeKey, PersistentDataType.INTEGER, -1);
                                        int nightTime = pdc.getOrDefault(nightTimeKey, PersistentDataType.INTEGER, -1);
                                        
                                        sender.sendMessage(
                                            mm.deserialize(
                                                "%s:<br>  daytime: (<green>%d<reset>t)<br>  nighttime: (<green>%d<reset>t)"
                                                    .formatted(world.getName(), dayTime, nightTime)
                                            )
                                        );
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                            .then(
                                Commands.literal("timeset")
                                    .then(
                                        Commands.argument("daytime", IntegerArgumentType.integer(1))
                                            .then(
                                                Commands.argument("nighttime", IntegerArgumentType.integer(1))
                                                    .executes(context -> {
                                                        World world = context.getArgument("world", World.class);
                                                        CommandSender sender = context.getSource().getSender();

                                                        if (world.getEnvironment() != World.Environment.NORMAL) {
                                                            sender.sendMessage(mm.deserialize("<red>there is no cycle of day and night in this world, it doesn't make sense."));
                                                            return Command.SINGLE_SUCCESS;
                                                        }

                                                        int dayTime = context.getArgument("daytime", Integer.class);
                                                        int nightTime = context.getArgument("nighttime", Integer.class);

                                                        PersistentDataContainer pdc = world.getPersistentDataContainer();
                                                        pdc.set(dayTimeKey, PersistentDataType.INTEGER, dayTime);
                                                        pdc.set(nightTimeKey, PersistentDataType.INTEGER, nightTime);

                                                        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

                                                        if (timeTask == null || timeTask.isCancelled())
                                                            timeTask = new TimeRunnable().runTaskTimer(this, 0L, 1L);

                                                        sender.sendMessage(mm.deserialize("<green>time set successfully"));
                                                        return Command.SINGLE_SUCCESS;
                                                    })
                                            )
                                    )
                            )
                            .then(
                                Commands.literal("timereset")
                                    .executes(context -> {
                                        World world = context.getArgument("world", World.class);
                                        CommandSender sender = context.getSource().getSender();

                                        if (world.getEnvironment() != World.Environment.NORMAL) {
                                            sender.sendMessage(mm.deserialize("<red>there is no cycle of day and night in this world, it doesn't make sense."));
                                            return Command.SINGLE_SUCCESS;
                                        }

                                        PersistentDataContainer pdc = world.getPersistentDataContainer();
                                        pdc.set(dayTimeKey, PersistentDataType.INTEGER, -1);
                                        pdc.set(nightTimeKey, PersistentDataType.INTEGER, -1);

                                        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                                        timeTask.cancel();

                                        sender.sendMessage(mm.deserialize("<green>time reset successfully"));
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .build()
            );
        });

        getLogger().info("Plugin is enabled.");
    }

    @Override
    public void onDisable() {
        if (timeTask != null && !timeTask.isCancelled())
            timeTask.cancel();
        
        getLogger().info("Plugin is disabled.");
    }
}
