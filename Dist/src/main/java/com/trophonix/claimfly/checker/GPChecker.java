package com.trophonix.claimfly.checker;

import com.trophonix.claimfly.api.ClaimChecker;
import com.trophonix.claimfly.api.ClaimFly;
import lombok.AllArgsConstructor;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class GPChecker implements ClaimChecker {

  private ClaimFly pl;

  private Claim getClaim(Location loc) {
    return GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);
  }

  @Override public boolean isInOwnClaim(Player player, Location loc) {
    Claim claim = getClaim(loc);
    if (claim == null) return pl.isFreeWorld();
    return (claim.isAdminClaim() && player.hasPermission("griefprevention.adminclaims")) || player.getUniqueId().equals(claim.ownerID);
  }

  @Override public boolean isInTrustedClaim(Player player, Location loc) {
    Claim claim = getClaim(loc);
    if (claim == null) return pl.isFreeWorld();
    return claim.allowBuild(player, Material.DIRT) == null;
  }

}
