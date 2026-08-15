package dansplugins.nomorecreepers.listeners;

import dansplugins.nomorecreepers.NoMoreCreepers;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Characterizes the plugin's sole behaviour: creeper spawns are cancelled unless
 * spawning has been allowed in the configuration.
 */
public class SpawnListenerTest {
    private final NoMoreCreepers noMoreCreepers = mock(NoMoreCreepers.class);
    private final SpawnListener spawnListener = new SpawnListener(noMoreCreepers);

    @Test
    public void creeperSpawnIsCancelledWhenSpawningIsNotAllowed() {
        when(noMoreCreepers.isSpawningAllowed()).thenReturn(false);
        EntitySpawnEvent event = spawnEventFor(mock(Creeper.class));

        spawnListener.handle(event);

        assertTrue(event.isCancelled());
    }

    @Test
    public void creeperSpawnIsNotCancelledWhenSpawningIsAllowed() {
        when(noMoreCreepers.isSpawningAllowed()).thenReturn(true);
        EntitySpawnEvent event = spawnEventFor(mock(Creeper.class));

        spawnListener.handle(event);

        assertFalse(event.isCancelled());
    }

    @Test
    public void nonCreeperSpawnIsNotCancelledWhenSpawningIsNotAllowed() {
        when(noMoreCreepers.isSpawningAllowed()).thenReturn(false);
        EntitySpawnEvent event = spawnEventFor(mock(Zombie.class));

        spawnListener.handle(event);

        assertFalse(event.isCancelled());
    }

    @Test
    public void alreadyCancelledCreeperSpawnStaysCancelledWhenSpawningIsAllowed() {
        when(noMoreCreepers.isSpawningAllowed()).thenReturn(true);
        EntitySpawnEvent event = spawnEventFor(mock(Creeper.class));
        event.setCancelled(true);

        spawnListener.handle(event);

        assertTrue(event.isCancelled());
    }

    private EntitySpawnEvent spawnEventFor(Entity entity) {
        return new EntitySpawnEvent(entity);
    }
}
