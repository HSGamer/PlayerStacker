package me.hsgamer.playerstacker;

import me.hsgamer.hscore.bukkit.baseplugin.BasePlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public final class PlayerStacker extends BasePlugin implements Listener {
    @Override
    public void enable() {
        registerListener(this);
    }

    @EventHandler
    public void onExpPickup(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        if (player.isEmpty()) {
            return;
        }
        Entity mainPassenger = getTopMainPassengerAsVehicle(player);
        if (!(mainPassenger instanceof Player)) {
            return;
        }
        int xp = event.getAmount();
        event.setAmount(0);
        ((Player) mainPassenger).giveExp(xp);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.isEmpty()) {
            return;
        }
        Entity mainPassenger = getTopMainPassengerAsVehicle(entity);
        if (!(mainPassenger instanceof InventoryHolder)) {
            return;
        }
        InventoryHolder inventoryHolder = (InventoryHolder) mainPassenger;
        ItemStack itemStack = event.getItem().getItemStack();
        event.setCancelled(true);
        event.getItem().remove();
        inventoryHolder.getInventory().addItem(itemStack);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();
        if (damager.getPassengers().contains(damagee)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                && event.hasItem()
                && event.getMaterial().isEdible()) {
            return;
        }
        if (!event.getPlayer().getPassengers().isEmpty()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (entity.getVehicle() != null) {
            return;
        }
        if (!(entity instanceof Player)) {
            return;
        }
        event.setCancelled(true);
        Entity mainPassenger = getTopMainPassengerAsVehicle(player);
        mainPassenger.addPassenger(entity);
    }

    private Entity getTopMainPassengerAsVehicle(Entity vehicle) {
        Entity mainPassenger = vehicle;
        while (!mainPassenger.isEmpty()) {
            mainPassenger = mainPassenger.getPassengers().get(0);
        }
        return mainPassenger;
    }
}
