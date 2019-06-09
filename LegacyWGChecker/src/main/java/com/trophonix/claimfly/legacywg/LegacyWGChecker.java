package com.trophonix.claimfly.legacywg;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.trophonix.claimfly.api.ClaimChecker;
import com.trophonix.claimfly.api.ClaimFly;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class LegacyWGChecker implements ClaimChecker {

  private ClaimFly pl;

  @Override public boolean isInOwnClaim(Player player, Location loc) {
    ApplicableRegionSet regions = WorldGuardPlugin.inst().getRegionManager(player.getWorld()).getApplicableRegions(loc);
    if (regions.getRegions().isEmpty()) return pl.isFreeWorld();
    LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(player);
    for (ProtectedRegion rg : regions) {
      if (rg.isOwner(lp)) {
        return true;
      }
    }
    return false;
  }

  @Override public boolean isInTrustedClaim(Player player, Location loc) {
    ApplicableRegionSet regions = WorldGuardPlugin.inst().getRegionManager(player.getWorld()).getApplicableRegions(loc);
    if (regions.getRegions().isEmpty()) return pl.isFreeWorld();
    LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(player);
    for (ProtectedRegion rg : regions) {
      if (rg.isMember(lp)) {
        return true;
      }
    }
    return false;
  }

}
