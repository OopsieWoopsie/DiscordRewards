package eu.mcdb.discordrewards.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.google.common.io.Files;
import eu.mcdb.discordrewards.DiscordRewards;
import eu.mcdb.discordrewards.LinkManager;
import eu.mcdb.discordrewards.command.LinkCommand;
import eu.mcdb.discordrewards.config.Config;
import eu.mcdb.discordrewards.config.Config.Discord;
import eu.mcdb.discordrewards.config.Config.Rewards;
import eu.mcdb.discordrewards.util.RandomUtils;
import eu.mcdb.discordrewards.util.ZipExtractor;
import eu.mcdb.spicord.Spicord;
import eu.mcdb.universal.MCDB;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeePlugin extends Plugin {

    @Getter
    private LinkManager linkManager;

    private Config config;
    private boolean firstRun = true;

    public static BungeePlugin ins;

    @Override
    public void onEnable() {
        ins = this;

        extractEmbeds();
        saveResource("config.yml", false);
        saveResource("discord.yml", false);
        saveResource("rewards.yml", false);

        String key = createKey();

        File linked = new File(getDataFolder(), "linked.json");
        this.linkManager = new LinkManager(linked);

        Configuration c = getConfig("config.yml");
        Configuration d = getConfig("discord.yml");
        Configuration r = getConfig("rewards.yml");

        this.config = new Config(getDataFolder(), key);

        // set config values
        config.setPrefix(c.getString("prefix"));
        config.setVerifyInstructions(c.getStringList("verify-instructions"));
        config.setBroadcastEnabled(c.getBoolean("broadcast.enabled"));
        config.setBroadcastMessage(c.getStringList("broadcast.message"));
        config.applyBCFilter();
        config.setRewardEnabled(c.getBoolean("reward.enabled"));
        config.setRewardCommands(c.getStringList("reward.commands"));
        config.setAlreadyVerifiedMessage(c.getString("already-verified-message"));
        config.applyAVMFilter();

        // set discord config values
        Discord dis = config.getDiscord();
        dis.setAddRole(d.getBoolean("add-role.enabled", false));
        dis.setRoleType(d.getString("add-role.type"));
        dis.setRole(d.getString("add-role.role"));
        dis.setChannelId(d.getLong("channel-id"));
        dis.setSendMessage(d.getBoolean("send-message", false));
        dis.setRenameUser(d.getBoolean("rename-user", false));
        dis.setNameTemplate(d.getString("new-name"));
        dis.checkRoleType();

        // set rewards config values
        Rewards rew = config.getRewards();
        rew.setSendDiscordMessage(r.getBoolean("send-discord-message"));
        rew.setMessageRewards(r.getList("message-rewards"));
        rew.setup();

        if (firstRun) {
            firstRun = false;
            Spicord.getInstance().onLoad(s -> {
                s.getAddonManager().registerAddon(new DiscordRewards(getDataFolder(), config));
                getProxy().getPluginManager().registerListener(this, new JoinListener(rew));
            });
        }

        MCDB.registerCommand(this, new LinkCommand(linkManager, config));

        getProxy().getScheduler().schedule(this, () -> linkManager.save(), 5, 5, TimeUnit.MINUTES);
    }

    private String createKey() {
        final File file = new File(getDataFolder(), "command.key");
        if (!file.exists()) {
            final String key = genKey();
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(key.getBytes());
                fos.flush();
                fos.close();
                return key;
            } catch (Exception e) {
                getLogger().severe("An error ocurred while saving the 'command.key' file. " + e.getCause());
                e.printStackTrace();
            }
        } else {
            try {
                return Files.readFirstLine(file, Charset.defaultCharset());
            } catch (Exception e) {
                getLogger().severe("An error ocurred while reading the 'command.key' file. " + e.getCause());
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onDisable() {
        linkManager.save();
    }

    public void saveResource(String resourcePath, boolean replace) {
        try {
            getDataFolder().mkdir();
            File out = new File(getDataFolder(), resourcePath);
            if (!out.exists()) {
                out.createNewFile();
                InputStream is = getClass().getResourceAsStream("/" + resourcePath);
                byte[] buff = new byte[is.available()];
                is.read(buff);
                OutputStream os = new FileOutputStream(out);
                os.write(buff);
                os.flush();
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Configuration getConfig(String name) {
        try {
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), name));
        } catch (IOException e) {
            throw new RuntimeException("Error while loading config file " + name, e);
        }
    }

    public String genKey() {
        String key = "";
        for (int i = 0; i < 4; i++)
            key += RandomUtils.randomString(32, UUID.randomUUID().toString());
        return key;
    }

    private void extractEmbeds() {
        final File out = new File(getDataFolder(), "embed");
        out.mkdirs();

        try (ZipExtractor ex = new ZipExtractor(getFile())) {
            ex.filter("embed\\/.*\\.json");
            ex.extract(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
