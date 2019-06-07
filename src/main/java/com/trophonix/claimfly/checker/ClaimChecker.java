package com.trophonix.claimfly.checker;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ClaimChecker {

  boolean isInOwnClaim(Player player, Location loc);

  boolean isInTrustedClaim(Player player, Location loc);

}
