package com.trophonix.claimfly.newps;

import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.trophonix.claimfly.api.ClaimChecker;
import com.trophonix.claimfly.api.ClaimFly;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class NewPSChecker implements ClaimChecker {

  private ClaimFly pl;

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