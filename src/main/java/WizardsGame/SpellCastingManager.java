package WizardsGame;

import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class SpellCastingManager {
//    Mana Mana = new Mana();
    // fireball cast
    void castFireball(Player player) {
//        player.sendMessage(ChatColor.GREEN + "before");
//        if (Mana.hasEnoughMana(player, 10)){
//            player.sendMessage(ChatColor.GREEN + "yes");
            // Deduct mana cost
//            Mana.setCurrentMana(player,(Mana.getCurrentMana(player) - 10));
            double speed = 1;
            Vector direction = player.getLocation().getDirection().multiply(speed);
            player.launchProjectile(org.bukkit.entity.Fireball.class, direction);
            player.sendMessage(ChatColor.GREEN + "You cast the Fireball spell!");
//        }else {
//            player.sendMessage(ChatColor.RED + "Not enough mana to cast Fireball.");
//        }
    }
    ///using a fireball as the lightning strike spot
//    private void castLightningSpell(Player player) {
//        double projectileSpeed = 0.5; // Adjust the speed as needed
//        Vector direction = player.getLocation().getDirection().multiply(projectileSpeed);
//        player.launchProjectile(org.bukkit.entity.Fireball.class, direction);
//    }

//    @EventHandler
//    public void onProjectileHit(ProjectileHitEvent event) {
//        if (event.getEntity() instanceof Fireball) {
//            Fireball fireball = (Fireball) event.getEntity();
//            Location hitLocation = fireball.getLocation();
//
//            // Perform lightning strike at the hit location
//            Cast.strikeLightning(hitLocation);
//        }
//    }

    void strikeLightning(Location location) {
        location.getWorld().strikeLightning(location); // Summon lightning at the location
    }

    void castLightningSpell(Player player) {
        double particleDistance = 1000;
        Vector direction = player.getLocation().getDirection().multiply(particleDistance);
        Location destination = player.getLocation().add(direction);

        // Find the first block in the spell's path
        Location blockLocation = findSolidBlockInPath(player.getLocation().add(0, 1.5, 0), destination.add(0, 1, 0));

        // if a solid block is found, strike lightning where particle ends
        if (blockLocation != null) {
            destination = blockLocation;
            spawnAndMoveParticleTrail(player.getLocation().add(0, 1.5, 0), destination.add(0, 1, 0)); // particle adjustment to look better
            strikeLightning(destination);
        }
    }
    private void spawnAndMoveParticleTrail(Location startLocation, Location endLocation) {
        int particleCount = 100; // number of generated trail particles
        Vector direction = endLocation.toVector().subtract(startLocation.toVector()).normalize();
        double distanceBetweenParticles = startLocation.distance(endLocation) / particleCount;

        for (int i = 0; i < particleCount; i++) {
            Location particleLocation = startLocation.clone().add(direction.clone().multiply(distanceBetweenParticles * i));
            startLocation.getWorld().spawnParticle(Particle.CRIT, particleLocation, 1, 0, 0, 0, 0);
        }
    }
    private Location findSolidBlockInPath(Location startLocation, Location endLocation) {
        // Check for the first non-air block in spell's path
        RayTraceResult result = startLocation.getWorld().rayTraceBlocks(startLocation, endLocation.toVector().subtract(startLocation.toVector()).normalize(),
                startLocation.distance(endLocation), FluidCollisionMode.NEVER, true);

        if (result != null && result.getHitBlock() != null) {
            return result.getHitBlock().getLocation(); //returns location of block in spells path
        }

        return null; // No solid block found
    }

}