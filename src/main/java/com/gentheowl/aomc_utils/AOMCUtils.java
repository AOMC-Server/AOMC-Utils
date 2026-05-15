package com.gentheowl.aomc_utils;

import com.gentheowl.aomc_utils.advancement.core.AdvancementPolicies;
import com.gentheowl.aomc_utils.election.ElectionManager;
import com.gentheowl.aomc_utils.election.VoteEvents;
import com.gentheowl.aomc_utils.motd.MotdManager;
import com.gentheowl.aomc_utils.renaming.commands.CommandRegistry;
import com.gentheowl.aomc_utils.renaming.utils.RenameitConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AOMCUtils implements ModInitializer {
    public static final String MOD_ID = "aomc_utils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final RenameitConfig CONFIG = RenameitConfig.createAndLoad();

    @Override
    public void onInitialize() {
        CommandRegistry.registerCommands();
        ElectionManager.initialize();
        VoteEvents.register();

        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);
    }

    public void onServerStart(MinecraftServer server) {
        MotdManager.initialize(server);
        AdvancementPolicies.register(server);
    }
}
