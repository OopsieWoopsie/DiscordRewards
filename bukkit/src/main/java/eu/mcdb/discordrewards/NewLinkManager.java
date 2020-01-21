package eu.mcdb.discordrewards;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.mcdb.universal.player.UniversalPlayer;
import lombok.Getter;

public class NewLinkManager implements ILinkManager {

    private final Gson gson;
    private final Map<Long, Account> accounts;
    private final File linkedFile;

    @Getter
    private static NewLinkManager instance;

    public NewLinkManager(File linkedFile) {
        if (instance != null) {
            throw new IllegalStateException("already initialized, use getInstance()");
        }

        instance = this;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
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
        return false;
    }

    @Override
    public String generateCode(UniversalPlayer player) {
        return null;
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
    public boolean isVerified(Long discordId) {
        return getAccountByDiscordId(discordId) != null;
    }

    @Override
    public boolean isVerified(UniversalPlayer player) {
        return false;
    }

    @Override
    public void addPendingPlayer(UniversalPlayer player, String code) {
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
        return false;
    }

    @Override
    public Account link(Long discordId, String name) {
        final UUID uuid = isValidName(name) ? getPlayerId(name) : null;

        if (uuid != null) {
            Account acc = new Account(discordId, name, uuid.toString(), 0);
            accounts.put(discordId, acc);
            save();
            return acc;
        }

        return null;
    }

    @Override
    public void removeCode(String code) {
    }
}
