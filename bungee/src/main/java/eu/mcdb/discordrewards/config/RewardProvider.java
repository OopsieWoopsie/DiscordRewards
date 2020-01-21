package eu.mcdb.discordrewards.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import eu.mcdb.discordrewards.Account;
import eu.mcdb.discordrewards.config.Config.Rewards.Reward;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class RewardProvider {

    private ProxyServer server;

    public RewardProvider() {
        server = ProxyServer.getInstance();
    }

    public void sendCommand(Reward reward, Account acc) {
        final Function<String, String> placeholders = c -> c.replace("{player_name}", acc.getName());

        final ProxiedPlayer p = server.getPlayer(acc.getUniqueId());

        if (p == null) {
            // err: player is offline
            return;
        }

        final Map<String, Map<UUID, List<String>>> svs = new HashMap<>();

        final ServerInfo serv = p.getServer().getInfo();

        if (serv != null && serv.getName() != null) {
            for (String c : reward.getCommands()) {
                String n = serv.getName();
                int l = c.indexOf(']');

                if (c.startsWith("[") && l != -1) {
                    c = placeholders.apply(c);
                    String svname = c.substring(1, l);
                    if (svname.equals(n)) {
                        send(serv, c);
                    } else {
                        if (svs.containsKey(svname)) {
                            Map<UUID, List<String>> map = svs.get(svname);
                            if (map.containsKey(acc.getUniqueId())) {
                                List<String> list = map.get(acc.getUniqueId());
                                list.add(c);
                            } else {
                                List<String> list = new ArrayList<String>();
                                list.add(c);
                                map.put(acc.getUniqueId(), list);
                            }
                        } else {
                            Map<UUID, List<String>> map = new HashMap<>();
                            List<String> list = new ArrayList<String>();
                            list.add(c);
                            map.put(acc.getUniqueId(), list);
                            svs.put(svname, map);
                        }
                    }
                } else {
                    // err: commands needs to specify a server
                    return;
                }
            }
        } else {
            // err: server is null
            return;
        }

        String[] sss = svs.keySet().toArray(new String[] {});
        String str = String.join(", ", sss);

        p.sendMessage(new TextComponent(ChatColor.GOLD + "You have pending rewards on the following servers: " + str));
    }

    private void send(ServerInfo serv, String command) {
        try {
            final ByteArrayDataOutput out = ByteStreams.newDataOutput();

            out.writeUTF("DiscordRewards");
            out.writeUTF(Config.key);
            out.writeUTF("GiveReward");
            out.writeUTF(command);

            serv.sendData("BungeeCord", out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
