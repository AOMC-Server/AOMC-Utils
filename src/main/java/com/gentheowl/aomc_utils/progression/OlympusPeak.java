package com.gentheowl.aomc_utils.progression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parliament.api.Parliament;
import parliament.api.entity.Player;
import parliament.api.event.BlockUseEvent;
import parliament.api.world.Position;

import static com.gentheowl.aomc_utils.progression.ProgressionIds.*;

/**
 * The peak of the tree: the olympus gate opens when all three branches are
 * done, and the summit (BMPOAT) is earned by clicking the beacon of a
 * completed olympus temple.
 */
final class OlympusPeak {

    private static final Logger LOGGER = LoggerFactory.getLogger("aomc_utils");
    private static final String TEMPLATE_ID = "aomc_utils:olympus";
    /** The beacon's position inside the olympus template. */
    private static final Position BEACON_LOCAL = new Position(5, 1, 5);

    private final Parliament parliament;

    OlympusPeak(Parliament parliament) {
        this.parliament = parliament;
    }

    void register() {
        // Backfill: players who finished all branches before this existed.
        parliament.events().onPlayerJoin(this::grantGateIfReady);

        parliament.events().onBlockUse(event -> {
            if (event.hand() != BlockUseEvent.Hand.MAIN_HAND
                    || !event.blockId().equals("minecraft:beacon")) {
                return;
            }
            Player player = event.player();
            if (!parliament.advancements().isDone(player, OLYMPUS_GATE)
                    || parliament.advancements().isDone(player, BMPOAT)) {
                return;
            }
            Position origin = event.position().offset(
                    -BEACON_LOCAL.x(), -BEACON_LOCAL.y(), -BEACON_LOCAL.z());
            if (event.world().matchesTemplate(TEMPLATE_ID, origin)) {
                LOGGER.info("[BMPOAT] temple matched for {}, starting ceremony", player.name());
                DragonEggCeremony.start(parliament, event.world(), event.position());
                parliament.advancements().grant(player, BMPOAT);
                event.consume();
            }
        });
    }

    /** Called when any of the three branches completes. */
    void onBranchEnd(Player player) {
        grantGateIfReady(player);
    }

    private void grantGateIfReady(Player player) {
        if (parliament.advancements().isDone(player, OLYMPUS_GATE)) {
            return;
        }
        if (parliament.advancements().isDone(player, KILL_MANY_MOBS)
                && parliament.advancements().isDone(player, VISIT_STRONGHOLDS)
                && parliament.advancements().isDone(player, SHULKER_STAR)) {
            parliament.advancements().grant(player, OLYMPUS_GATE);
        }
    }
}
