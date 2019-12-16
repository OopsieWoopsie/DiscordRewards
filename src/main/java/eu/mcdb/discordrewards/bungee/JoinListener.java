package eu.mcdb.discordrewards.bungee;

import java.util.UUID;
import eu.mcdb.discordrewards.Account;
import eu.mcdb.discordrewards.LinkManager;
import eu.mcdb.discordrewards.config.Config.Rewards;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class JoinListener implements Listener {

    private Rewards rewards;

    public JoinListener(Rewards rewards) {
        this.rewards = rewards;
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();

        if (rewards.isCached(uuid)) {
            Account acc = LinkManager.getInstance().getAccountByUniqueId(uuid);
            rewards.getCachedRewards(uuid).forEach(r -> r.give(acc));
            rewards.cleanCache(uuid);
        }
    }
}
