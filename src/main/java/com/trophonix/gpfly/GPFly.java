package com.trophonix.gpfly;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.ess3.api.events.FlyStatusChangeEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GPFly extends JavaPlugin implements Listener {

  private String configNotAllowed;
  private String configFlightDisabled;

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
    if (!getDataFolder().exists()) {
      saveDefaultConfig();
    }
    reloadConfig();
  }

  @Override
  public void reloadConfig() {
    super.reloadConfig();
    configNotAllowed = ChatColor.translateAlternateColorCodes('&', getConfig().getString("notAllowed"));
    configFlightDisabled = ChatColor.translateAlternateColorCodes('&', getConfig().getString("flightDisabled"));
  }

  @EventHandler
  public void onFlyChange(FlyStatusChangeEvent event) {
    if (event.getAffected().getBase().hasPermission("gpfly.bypass")) return;
    Claim claim = GriefPrevention.instance.dataStore.getClaimAt(event.getAffected().getBase().getLocation(), true, null);
    if (claim == null || !claim.getOwnerName().equalsIgnoreCase(event.getAffected().getName())) {
      boolean couldFly = event.getAffected().getBase().getAllowFlight();
      if (!couldFly) {
        event.setCancelled(true);
        event.getAffected().getBase().sendMessage(configNotAllowed);
      }
    }
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ() && event.getFrom().getBlockY() == event.getTo().getBlockY()) {
      return;
    }
    if (event.getPlayer().hasPermission("gpfly.bypas")) return;
    Claim claim = GriefPrevention.instance.dataStore.getClaimAt(event.getTo(), true, null);
    if (event.getPlayer().getAllowFlight() && (claim == null || !claim.getOwnerName().equals(event.getPlayer().getName()))) {
      event.getPlayer().setAllowFlight(false);
      event.getPlayer().setFlying(false);
      event.getPlayer().sendMessage(configFlightDisabled);
    }
  }

}
