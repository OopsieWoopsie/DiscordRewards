package eu.mcdb.discordrewards.bungee;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import eu.mcdb.discordrewards.Account;
import eu.mcdb.discordrewards.LinkManager;
import eu.mcdb.discordrewards.config.Config.Rewards;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.event.EventHandler;

public class JoinListener implements Listener {

    public static void main(String[] args) {
        String n = "[aaaaa]dsdewfe[dede][dwed]";
        n = n.substring(1, n.indexOf(']'));
        System.out.println(n);
    }
    private Rewards rewards;
    private TaskScheduler sch;

    public JoinListener(Rewards rewards) {
        this.rewards = rewards;
        this.sch = ProxyServer.getInstance().getScheduler();
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent e) {
        handleJoin(e.getPlayer());
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent e) {
        handleJoin(e.getPlayer());
    }

    private void handleJoin(ProxiedPlayer player) {
        UUID uuid = player.getUniqueId();

        if (rewards.isCached(uuid)) {
            Account acc = LinkManager.getInstance().getAccountByUniqueId(uuid);
            sch.schedule(BungeePlugin.ins, () -> {



                rewards.getCachedRewards(uuid).forEach(r -> r.give(acc));
                rewards.cleanCache(uuid);



            }, 4, TimeUnit.SECONDS);
        }
    }
}
