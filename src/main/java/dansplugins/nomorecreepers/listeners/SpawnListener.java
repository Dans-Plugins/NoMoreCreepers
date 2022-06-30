package dansplugins.nomorecreepers.listeners;

import dansplugins.nomorecreepers.NoMoreCreepers;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class SpawnListener implements Listener {
    private final NoMoreCreepers noMoreCreepers;

    public SpawnListener(NoMoreCreepers noMoreCreepers) {
        this.noMoreCreepers = noMoreCreepers;
    }

    @EventHandler()
    public void handle(EntitySpawnEvent event) {
        if (noMoreCreepers.isSpawningAllowed()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Creeper) {
            event.setCancelled(true);
        }
    }

}