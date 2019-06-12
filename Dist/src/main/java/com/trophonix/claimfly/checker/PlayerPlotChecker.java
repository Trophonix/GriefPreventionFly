package com.trophonix.claimfly.checker;

import com.eclipsekingdom.playerplot.data.PlotManager;
import com.eclipsekingdom.playerplot.plot.Plot;
import com.trophonix.claimfly.api.ClaimChecker;
import com.trophonix.claimfly.api.ClaimFly;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class PlayerPlotChecker implements ClaimChecker {

  private ClaimFly pl;

  @Override public boolean isInOwnClaim(Player player, Location loc) {
    Plot plot = PlotManager.getInstance().getPlot(loc);
    if (plot == null) return false;
    return player.getUniqueId().equals(plot.getOwnerID());
  }

  @Override public boolean isInTrustedClaim(Player player, Location loc) {
    Plot plot = PlotManager.getInstance().getPlot(loc);
    if (plot == null) return false;
    return plot.getFriends().stream().anyMatch(
        fr -> player.getUniqueId().equals(fr.getUuid()));
  }

}
