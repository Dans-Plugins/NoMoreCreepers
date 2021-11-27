package dansplugins.nomorecreepers.eventhandlers;

import dansplugins.nomorecreepers.NoMoreCreepers;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class ExplosionHandler implements Listener {

    @EventHandler()
    public void handle(ExplosionPrimeEvent event) {
        if (NoMoreCreepers.getInstance().isExplodingAllowed()) {
            return;
        }
        if (event.getEntity() instanceof Creeper) {
            event.setCancelled(false);
        }
    }

}
