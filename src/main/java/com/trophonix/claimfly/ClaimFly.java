package com.trophonix.claimfly;

import com.trophonix.claimfly.checker.ClaimChecker;
import com.trophonix.claimfly.checker.GPChecker;
import com.trophonix.claimfly.checker.ResidenceChecker;
import com.trophonix.claimfly.listeners.EssentialsListener;
import com.trophonix.claimfly.listeners.FlyCommandListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class ClaimFly extends JavaPlugin implements Listener {

  private boolean otherTrustedClaims;
  private boolean freeWorld;

  private boolean drop;
  private boolean gamemodeBypass;
  private boolean onFlyCommand;

  private String configNotAllowed;
  private String configFlightDisabled;

  private ClaimChecker checker;
  private Listener flyListener;

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
    getConfig().options().copyDefaults(true);
    saveConfig();
    load();
    if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) {
      checker = new GPChecker(this);
    } else if (Bukkit.getPluginManager().isPluginEnabled("Residence")) {
      checker = new ResidenceChecker(this);
    } else {
      getLogger().warning("No claim plugin found. Supported: GriefPrevention, Residence.");
      getServer().getPluginManager().disablePlugin(this);
    }
  }

  private void load() {
    otherTrustedClaims = getConfig().getBoolean("otherTrustedClaims", true);
    freeWorld = getConfig().getBoolean("freeWorld", false);
    drop = getConfig().getBoolean("drop", false);
    gamemodeBypass = getConfig().getBoolean("gamemodeBypass", true);
    onFlyCommand = getConfig().getBoolean("onFlyCommand", false);
    configNotAllowed = ChatColor.translateAlternateColorCodes('&', getConfig().getString("notAllowed"));
    configFlightDisabled = ChatColor.translateAlternateColorCodes('&', getConfig().getString("flightDisabled"));
    if ((flyListener == null) == onFlyCommand) {
      if (flyListener == null) {
        if (getServer().getPluginManager().isPluginEnabled("Essentials")) {
          flyListener = new EssentialsListener(this);
        } else {
          flyListener = new FlyCommandListener(this);
        }
      } else {
        HandlerList.unregisterAll(flyListener);
        flyListener = null;
      }
    }
  }

  @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 1 && args[0].equalsIgnoreCase("rl")) {
      reloadConfig();
      load();
      sender.sendMessage(ChatColor.GREEN + "Config reloaded!");
      return true;
    }

    sender.sendMessage(ChatColor.BLUE + "ClaimFly by Trophonix...");
    sender.sendMessage(ChatColor.AQUA + "/claimfly rl " + ChatColor.WHITE + "Reload the config.");
    return true;
  }

  public boolean canBypass(Player player) {
    if ((gamemodeBypass && canFly(player.getGameMode()))) return true;
    if (player.hasPermission("claimfly.bypass")) return true;
    return false;
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    if (event.getTo() == null) return;
    Player player = event.getPlayer();
    if (!player.isFlying()) return;
    if (canBypass(player)) return;
    if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ() && event.getFrom().getBlockY() == event.getTo().getBlockY()) {
      return;
    }
    if (!isAllowedToFly(event.getTo(), event.getPlayer())) {
      if (drop) {
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setFallDistance(0 - player.getLocation().getBlockY());
      } else event.setCancelled(true);
      player.sendMessage(configFlightDisabled);
    }
  }

  @EventHandler
  public void onFlyChange(PlayerToggleFlightEvent event) {
    if (!event.isFlying()) return;
    if (canBypass(event.getPlayer())) return;
    if (!isAllowedToFly(event.getPlayer())) {
      event.setCancelled(true);
      event.getPlayer().sendMessage(configNotAllowed);
    }
  }

  public boolean isAllowedToFly(Location to, Player player) {
    return (otherTrustedClaims && checker.isInTrustedClaim(player, to)) ||
               checker.isInOwnClaim(player, to);
  }

  public boolean isAllowedToFly(Player player) {
    return isAllowedToFly(player.getLocation(), player);
  }

  public boolean canFly(GameMode gamemode) {
    return gamemode == GameMode.CREATIVE || gamemode == GameMode.SPECTATOR;
  }

}
