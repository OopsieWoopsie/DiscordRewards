package eu.mcdb.discordrewards;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import eu.mcdb.discordrewards.util.RandomUtils;
import eu.mcdb.universal.player.UniversalPlayer;

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
        OfflinePlayer p = Bukkit.getServer().getOfflinePlayer(uuid);
        return p == null ? "" : p.getName();
    }

    default UUID getPlayerId(String name) {
        for (OfflinePlayer p : Bukkit.getServer().getOfflinePlayers()) {
            if (p.getName().equals(name))
                return p.getUniqueId();
        }
        return null;
    }

    default boolean isValidName(String name) {
        return name.length() >= 3
                && name.length() <= 16
                && name.replaceAll("[A-Za-z0-9_]", "").length() == 0;
    }

    void removeCode(String code);
}
