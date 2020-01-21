package eu.mcdb.discordrewards.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import eu.mcdb.discordrewards.Account;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.md_5.bungee.api.ChatColor;

@Getter
@Setter
public class Config {

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private static File dataFolder;

    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private Gson gson;

    private List<String> verifyInstructions;
    private String prefix;
    private boolean broadcastEnabled;
    private List<String> broadcastMessage;
    private boolean rewardEnabled;
    private List<String> rewardCommands;
    private String alreadyVerifiedMessage;
    private Discord discord;
    private Rewards rewards;

    public static String key;

    public Config(File dataFolder, String key) {
        Config.dataFolder = dataFolder;
        Config.key = key;

        this.gson = new GsonBuilder().setPrettyPrinting().create();

        this.discord = new Discord();
        this.rewards = new Rewards();
    }

    public void applyAVMFilter() {
        this.alreadyVerifiedMessage = alreadyVerifiedMessage.replace("{prefix}", prefix);
        this.alreadyVerifiedMessage = ChatColor.translateAlternateColorCodes('&', alreadyVerifiedMessage);
    }

    public void applyBCFilter() {
        Function<String, String> filter = str -> {
            str = str.replace("{prefix}", prefix);
            str = ChatColor.translateAlternateColorCodes('&', str);
            return str;
        };

        this.broadcastMessage = broadcastMessage.stream().map(filter).collect(Collectors.toList());
    }

    public List<String> getVerifyInstructions(String code) {
        Function<String, String> filter = str -> {
            str = str.replace("{prefix}", prefix);
            str = str.replace("{code}", code);
            str = ChatColor.translateAlternateColorCodes('&', str);
            return str;
        };

        return verifyInstructions.stream().map(filter).collect(Collectors.toList());
    }

    @Getter
    @Setter
    public class Discord {

        @Getter(value = AccessLevel.NONE)
        private boolean addRole;
        @Getter(value = AccessLevel.NONE)
        private boolean renameUser;
        @Getter(value = AccessLevel.NONE)
        private boolean sendMessage;

        private Long channelId;
        private String nameTemplate;
        private String roleType;
        private String role;

        private Discord() {}

        public boolean shouldAddRole() {
            return addRole;
        }

        public boolean shouldRenameUser() {
            return renameUser;
        }

        public boolean shouldSendMessage() {
            return sendMessage;
        }

        public Role getRole(Guild guild) {
            if (roleType.equals("name")) {
                List<Role> roles = guild.getRolesByName(role, false);
                return roles.size() > 0 ? roles.get(0) : null;
            } else {
                return guild.getRoleById(role);
            }
        }

        public void checkRoleType() {
            if (!(roleType.equals("name") || roleType.equals("id"))) {
                throw new IllegalArgumentException("'add-role.type' should be 'name' or 'id', you have put '" + roleType + "'!");
            }
        }
    }

    public class Rewards {

        private Map<String, Reward> rewards;
        private Map<UUID, Set<Integer>> cached;
        private File cachedFile;
        private RewardProvider provider = new RewardProvider();

        @Setter
        private boolean sendDiscordMessage;

        @Setter
        private List<?> messageRewards;

        private Rewards() {
            this.rewards = new HashMap<String, Reward>();
        }

        public void setup() {
            Gson gson = new Gson();

            Type type = new TypeToken<Map<String, Reward>>() {}.getType();

            for (Object i : messageRewards) {
                String json = gson.toJson(i);

                Map<String, Reward> obj = gson.fromJson(json, type);
                Entry<String, Reward> data = obj.entrySet().iterator().next();
                rewards.put(data.getKey(), data.getValue());
            }

            this.cachedFile = new File(dataFolder, "cached-rewards.json");
            loadCached();
        }

        private void loadCached() {
            try {
                if (cachedFile.exists()) {
                    byte[] b = Files.readAllBytes(cachedFile.toPath());
                    String json = new String(b);
                    Type type = new TypeToken<Map<UUID, Set<Integer>>>() {}.getType();
                    this.cached = gson.fromJson(json, type);
                    if (this.cached == null)
                        this.cached = new HashMap<UUID, Set<Integer>>();
                } else {
                    cachedFile.createNewFile();
                    this.cached = new HashMap<UUID, Set<Integer>>();
                }
                saveCached();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void saveCached() {
            try (OutputStream fos = new FileOutputStream(cachedFile)) {
                String json = gson.toJson(cached);
                fos.write(json.getBytes());
                fos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Stream<Reward> getCachedRewards(UUID uuid) {
            return cached.get(uuid).stream().map(this::getReward);
        }

        public boolean appliesForReward(int msgcount) {
            String msgc = String.valueOf(msgcount);

            for (Entry<String, Reward> r : rewards.entrySet()) {
                if (msgc.equals(r.getKey())) {
                    return true;
                }
            }
            return false;
        }

        public boolean shouldSendDiscordMessage() {
            return sendDiscordMessage;
        }

        public boolean isCached(UUID uuid) {
            return cached.containsKey(uuid);
        }

        public void cache(Account acc, int count) {
            if (cached.containsKey(acc.getUniqueId())) {
                cached.get(acc.getUniqueId()).add(count);
            } else {
                Set<Integer> s = new HashSet<Integer>();
                s.add(count);
                cached.put(acc.getUniqueId(), s);
            }
            saveCached();
        }

        public void cleanCache(UUID uuid) {
            cached.remove(uuid);
            saveCached();
        }

        public Reward getReward(int msgcount) {
            return rewards.get(String.valueOf(msgcount));
        }

        public class Reward {

            @Getter
            private String[] commands;

            public void give(Account acc) {
                provider.sendCommand(this, acc);
            }

            @Override
            public Reward clone() {
                final Reward r = new Reward();
                r.commands = commands.clone();
                return r;
            }
        }
    }
}
