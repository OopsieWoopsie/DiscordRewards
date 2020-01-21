package eu.mcdb.discordrewards;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Predicate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.mcdb.universal.player.UniversalPlayer;
import lombok.Getter;

public class LinkManager implements ILinkManager {

    private final Gson gson;
    private final HashMap<UUID, String> pending;
    private final Map<Long, Account> accounts;
    private final File linkedFile;

    @Getter
    private static LinkManager instance;

    public LinkManager(File linkedFile) {
        if (instance != null) {
            throw new IllegalStateException("already initialized, use getInstance()");
        }

        instance = this;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.pending = new HashMap<UUID, String>();
        this.accounts = new HashMap<Long, Account>();
        this.linkedFile = linkedFile;

        try {
            if (linkedFile.exists()) {
                Account[] linked = gson.fromJson(new FileReader(linkedFile), Account[].class);

                if (linked == null || linked.length == 0)
                    return;

                for (Account account : linked) {
                    accounts.put(account.getId(), account);
                }
            } else {
                linkedFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isPending(UniversalPlayer p) {
        return pending.containsKey(p.getUniqueId());
    }

    @Override
    public Account getAccountByDiscordId(Long id) {
        return accounts.get(id);
    }

    @Override
    public Account getAccountByUniqueId(UUID uuid) {
        Predicate<Account> filter = a -> a.getUniqueId().equals(uuid);
        return accounts.values().stream().filter(filter).findFirst().orElse(null);
    }

    @Override
    public boolean isVerified(UniversalPlayer player) {
        UUID uuid = player.getUniqueId();

        return accounts.values().stream()
                .map(Account::getUniqueId)
                .anyMatch(uuid::equals);
    }

    @Override
    public void addPendingPlayer(UniversalPlayer player, String code) {
        pending.put(player.getUniqueId(), code);
    }

    @Override
    public void save() {
        if (accounts.values().size() == 0) return;
        try (OutputStream os = new FileOutputStream(linkedFile)) {
            String json = gson.toJson(accounts.values());
            os.write(json.getBytes());
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isValidCode(String code) {
        return pending.values().contains(code);
    }

    @Override
    public Account link(Long discordId, String code) {
        for (Entry<UUID, String> entry : pending.entrySet()) {
            UUID uuid = entry.getKey();
            String _code = entry.getValue();

            if (_code.equals(code)) {
                Account acc = new Account(discordId, getPlayerName(uuid), uuid.toString(), 0);
                accounts.put(discordId, acc);
                save();
                return acc;
            }
        }
        return null;
    }

    @Override
    public void removeCode(String code) {
        pending.values().remove(code);
    }

    @Override
    public boolean isVerified(Long discordId) {
        return getAccountByDiscordId(discordId) != null;
    }
}
