package com.trophonix.claimfly.checker;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.trophonix.claimfly.api.ClaimChecker;
import com.trophonix.claimfly.api.ClaimFly;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class ResidenceChecker implements ClaimChecker {

  private ClaimFly pl;

  private ClaimedResidence getResidence(Location loc) {
    return ResidenceApi.getResidenceManager().getByLoc(loc);
  }

  @Override public boolean isInOwnClaim(Player player, Location loc) {
    ClaimedResidence res = getResidence(loc);
    if (res == null) return false;
    if (res.isOwner(player)) return true;
    ResidencePlayer resPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
    return resPlayer.getGroup() != null && resPlayer.getGroup().equals(res.getOwnerGroup());
  }

  @Override public boolean isInTrustedClaim(Player player, Location loc) {
    ClaimedResidence res = getResidence(loc);
    if (res == null) return false;
    return res.getPermissions().playerHas(player, Flags.place, false);
  }
}
