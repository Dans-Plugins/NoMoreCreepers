package dansplugins.nomorecreepers.eventhandlers;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class SpawnHandler implements Listener {

    @EventHandler()
    public void handle(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Creeper) {
            event.setCancelled(true);
        }
    }

}