package com.trophonix.gpfly;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.ess3.api.events.FlyStatusChangeEvent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GPFly extends JavaPlugin implements Listener {

  private boolean otherTrustedClaims;
  private boolean freeWorld;
  private boolean gamemodeBypass;

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
    otherTrustedClaims = getConfig().getBoolean("otherTrustedClaims", true);
    freeWorld = getConfig().getBoolean("freeWorld", false);
    gamemodeBypass  = getConfig().getBoolean("gamemodeBypass", true);
    configNotAllowed = ChatColor.translateAlternateColorCodes('&', getConfig().getString("notAllowed"));
    configFlightDisabled = ChatColor.translateAlternateColorCodes('&', getConfig().getString("flightDisabled"));
  }

  @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 1 && args[0].equalsIgnoreCase("rl")) {
      reloadConfig();
      sender.sendMessage(ChatColor.GREEN + "Config reloaded!");
      return true;
    }

    sender.sendMessage(ChatColor.BLUE + "GPFly by Trophonix...");
    sender.sendMessage(ChatColor.AQUA + "/gpfly rl " + ChatColor.WHITE + "Reload the config.");
    return true;
  }

  @EventHandler
  public void onFlyChange(FlyStatusChangeEvent event) {
    Player player = event.getAffected().getBase();
    if (!event.getValue() || (gamemodeBypass && canFly(player.getGameMode())) || player.hasPermission("gpfly.bypass")) return;
    Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
    if (!isAllowed(claim, player)) {
      event.setCancelled(true);
      event.getAffected().getBase().sendMessage(configNotAllowed);
    }
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ() && event.getFrom().getBlockY() == event.getTo().getBlockY()) {
      return;
    }
    if (!event.getPlayer().getAllowFlight() || (gamemodeBypass && canFly(event.getPlayer().getGameMode())) || event.getPlayer().hasPermission("gpfly.bypas")) return;
    Claim claim = GriefPrevention.instance.dataStore.getClaimAt(event.getTo(), true, null);
    if (!isAllowed(claim, event.getPlayer())) {
      event.setCancelled(true);
      event.getPlayer().sendMessage(configFlightDisabled);
    }
  }

  private boolean isAllowed(Claim claim, Player player) {
    return claim == null ? freeWorld : claim.ownerID.equals(player.getUniqueId()) ||
                                           (otherTrustedClaims && claim.allowEdit(player) == null);
  }

  private boolean canFly(GameMode gamemode) {
    return gamemode == GameMode.CREATIVE || gamemode == GameMode.SPECTATOR;
  }

}
