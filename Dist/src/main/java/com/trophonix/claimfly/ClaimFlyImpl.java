package com.trophonix.claimfly;

import com.trophonix.claimfly.api.ClaimChecker;
import com.trophonix.claimfly.api.ClaimFly;
import com.trophonix.claimfly.checker.*;
import com.trophonix.claimfly.legacyps.LegacyPSChecker;
import com.trophonix.claimfly.legacywg.LegacyWGChecker;
import com.trophonix.claimfly.listeners.EssentialsListener;
import com.trophonix.claimfly.listeners.FlyCommandListener;
import com.trophonix.claimfly.newps.NewPSChecker;
import com.trophonix.newwg.NewWGChecker;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ClaimFlyImpl extends JavaPlugin implements ClaimFly, Listener {

  private boolean permissionRequired;

  private boolean otherTrustedClaims;
  private boolean adminClaims;
  private boolean freeWorld;

  private boolean drop;
  private boolean gamemodeBypass;
  private boolean onFlyCommand;

  private String configNotAllowed;
  private String configFlightDisabled;

  private List<ClaimChecker> checkers = new ArrayList<>();
  private Listener flyListener;

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
    getConfig().options().copyDefaults(true);
    saveConfig();
    load();

    if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) {
      getLogger().info("Hooking into GriefPrevention.");
      checkers.add(new GPChecker(this));
    }

    if (Bukkit.getPluginManager().isPluginEnabled("Residence")) {
      getLogger().info("Hooking into Residence.");
      checkers.add(new ResidenceChecker(this));
    }

    if (Bukkit.getPluginManager().isPluginEnabled("PlayerPlot")) {
      getLogger().info("Hooking into PlayerPlot.");
      checkers.add(new PlayerPlotChecker(this));
    }

    if (Bukkit.getPluginManager().isPluginEnabled("PlotSquared")) {
      try {
        Class.forName("com.intellectualcrafters.plot.object.Location");
        getLogger().info("Hooking into Legacy Plotsquared.");
        checkers.add(new LegacyPSChecker(this));
      } catch (Exception ignored) {
        getLogger().info("Hooking into Plotsquared.");
        checkers.add(new NewPSChecker(this));
      }
    }

    if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
      Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
      String version = worldGuard.getDescription().getVersion();
      // if legacy, assume one of the latest versions.
      if (version.startsWith("6.1") || version.startsWith("6.2")) {
        getLogger().info("Hooking into Legacy Worldgaurd.");
        checkers.add(new LegacyWGChecker(this));
      }
      // else, assume new worldguard
      else {
        getLogger().info("Hooking into Worldguard.");
        checkers.add(new NewWGChecker(this));
      }
    }

    if (checkers.isEmpty()) {
      getLogger().warning("No claim plugin found. Supported: WorldGuard, GriefPrevention, Residence, PlotSquared, PlayerPlot.");
      getServer().getPluginManager().disablePlugin(this);
    }
  }

  private void load() {
    permissionRequired = getConfig().getBoolean("permissionRequired", false);
    otherTrustedClaims = getConfig().getBoolean("otherTrustedClaims", true);
    adminClaims = getConfig().getBoolean("adminClaims", true);
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
    if (permissionRequired && !player.hasPermission("claimfly.fly")) return false;
    for (int i = 0; i < checkers.size(); i++) {
      ClaimChecker checker = checkers.get(i);
      if (otherTrustedClaims) {
        if (checker.isInTrustedClaim(player, to)) {
          return true;
        }
      } else {
        if (checker.isInOwnClaim(player, to)) {
          return true;
        }
      }
    }
    return isFreeWorld();
  }

  public boolean isAllowedToFly(Player player) {
    return isAllowedToFly(player.getLocation(), player);
  }

  public boolean canFly(GameMode gamemode) {
    return gamemode == GameMode.CREATIVE || gamemode == GameMode.SPECTATOR;
  }

}
