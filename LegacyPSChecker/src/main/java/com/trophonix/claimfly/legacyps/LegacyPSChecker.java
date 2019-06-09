package com.trophonix.claimfly.legacyps;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.trophonix.claimfly.api.ClaimChecker;
import com.trophonix.claimfly.api.ClaimFly;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class LegacyPSChecker implements ClaimChecker {

  private final ClaimFly pl;

  @Override public boolean isInOwnClaim(Player player, org.bukkit.Location loc) {
    PlotPlayer pp = PlotPlayer.wrap(player);
    Plot plot = pp.getCurrentPlot();
    if (plot == null) return pl.isFreeWorld();
    return plot.isOwner(player.getUniqueId());
  }

  @Override public boolean isInTrustedClaim(Player player, org.bukkit.Location loc) {
    PlotPlayer pp = PlotPlayer.wrap(player);
    Plot plot = pp.getCurrentPlot();
    if (plot == null) return pl.isFreeWorld();
    return plot.isAdded(player.getUniqueId());
  }

}