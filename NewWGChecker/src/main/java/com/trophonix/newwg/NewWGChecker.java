package com.trophonix.newwg;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.trophonix.claimfly.api.ClaimFly;
import com.trophonix.claimfly.api.ClaimChecker;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class NewWGChecker implements ClaimChecker {

  private ClaimFly pl;

  private ApplicableRegionSet getApplicableRegions(Location loc) {
    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionManager manager;
    if (container == null || (manager = container.get(BukkitAdapter.adapt(loc.getWorld()))) == null) {
      return null;
    }
    return manager.getApplicableRegions(
        BukkitAdapter.asBlockVector(loc));
  }

  @Override public boolean isInOwnClaim(Player player, Location loc) {
    ApplicableRegionSet regions = getApplicableRegions(loc);
    if (regions == null || regions.getRegions().isEmpty()) return pl.isFreeWorld();
    for (ProtectedRegion rg : regions) {
      if (rg.getOwners().contains(player.getUniqueId())) {
        return true;
      }
    }
    return false;
  }

  @Override public boolean isInTrustedClaim(Player player, Location loc) {
    ApplicableRegionSet regions = getApplicableRegions(loc);
    if (regions == null || regions.getRegions().isEmpty()) return pl.isFreeWorld();
    for (ProtectedRegion rg : regions) {
      if (rg.getMembers().contains(player.getUniqueId())) {
        return true;
      }
    }
    return false;
  }

}
