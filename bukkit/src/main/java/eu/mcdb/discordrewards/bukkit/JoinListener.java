package eu.mcdb.discordrewards.bukkit;

import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import eu.mcdb.discordrewards.Account;
import eu.mcdb.discordrewards.LinkManager;
import eu.mcdb.discordrewards.config.Config.Rewards;

public class JoinListener implements Listener {

    private Rewards rewards;

    public JoinListener(Rewards rewards) {
        this.rewards = rewards;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();

        if (rewards.isCached(uuid)) {
            Account acc = LinkManager.getInstance().getAccountByUniqueId(uuid);
            rewards.getCachedRewards(uuid).forEach(r -> r.give(acc));
            rewards.cleanCache(uuid);
        }
    }
}
