package eu.mcdb.discordrewards;

import java.util.UUID;
import eu.mcdb.discordrewards.util.RandomUtils;
import eu.mcdb.universal.player.UniversalPlayer;
import eu.mcdb.util.Server;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface ILinkManager {

    boolean isPending(UniversalPlayer p);

    Account getAccountByDiscordId(Long id);

    Account getAccountByUniqueId(UUID uuid);

    boolean isVerified(Long discordId);

    boolean isVerified(UniversalPlayer player);

    void addPendingPlayer(UniversalPlayer player, String code);

    void save();

    boolean isValidCode(String code);

    Account link(Long discordId, String code);

    default String generateCode(UniversalPlayer player) {
        String seed = player.getUniqueId().toString().replace("-", "").toUpperCase();
        return RandomUtils.randomString(8, seed);
    }

    default String getPlayerName(UUID uuid) {
        switch (Server.getServerType()) {
        case BUNGEECORD:
            ProxiedPlayer p1 = ProxyServer.getInstance().getPlayer(uuid);
            return p1 == null ? "" : p1.getName();
        default:
            return "";
        }
    }

    default UUID getPlayerId(String name) {
        switch (Server.getServerType()) {
        case BUNGEECORD:
            ProxiedPlayer p1 = ProxyServer.getInstance().getPlayer(name);
            return p1 == null ? null : p1.getUniqueId();
        default:
            return null;
        }
    }

    default boolean isValidName(String name) {
        return name.length() >= 3
                && name.length() <= 16
                && name.replaceAll("[A-Za-z0-9_]", "").length() == 0;
    }

    void removeCode(String code);
}
