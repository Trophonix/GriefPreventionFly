package com.trophonix.claimfly.listeners;

import com.trophonix.claimfly.ClaimFlyImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class FlyCommandListener implements Listener {

  private ClaimFlyImpl pl;

  public FlyCommandListener(ClaimFlyImpl pl) {
    this.pl = pl;
    pl.getServer().getPluginManager().registerEvents(this, pl);
  }

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent event) {
    // let them do /fly if they are doing it to disable it
    if (event.getPlayer().getAllowFlight()) return;
    String cmd = event.getMessage();
    // make sure I catch it if they type a space but not if
    // they're trying to let someone else fly (e.g. /fly Lucas)
    if (cmd.equalsIgnoreCase("/fly") || cmd.equalsIgnoreCase("/fly ")) {
      Player player = event.getPlayer();
      if (pl.canBypass(player)) return;
      if (!pl.isAllowedToFly(player)) {
        event.setCancelled(true);
        player.sendMessage(pl.getConfigNotAllowed());
      }
    }
  }

}
