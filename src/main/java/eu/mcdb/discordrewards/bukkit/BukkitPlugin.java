package eu.mcdb.discordrewards.bukkit;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import eu.mcdb.discordrewards.DiscordRewards;
import eu.mcdb.discordrewards.LinkManager;
import eu.mcdb.discordrewards.command.LinkCommand;
import eu.mcdb.discordrewards.config.Config;
import eu.mcdb.discordrewards.config.Config.Discord;
import eu.mcdb.discordrewards.config.Config.Rewards;
import eu.mcdb.discordrewards.util.ZipExtractor;
import eu.mcdb.spicord.Spicord;
import eu.mcdb.universal.MCDB;

public class BukkitPlugin extends JavaPlugin {

    private LinkManager linkManager;
    private boolean firstRun = true;

    @Override
	public void onEnable() {
        extractEmbeds();
        saveDefaultConfig();
        saveResource("discord.yml", false);
        saveResource("rewards.yml", false);

		File linked = new File(getDataFolder(), "linked.json");
		this.linkManager = new LinkManager(linked);

        FileConfiguration c = getConfig();
        FileConfiguration d = getConfig("discord.yml");
        FileConfiguration r = getConfig("rewards.yml");

        Config config = new Config(getDataFolder());

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
                getServer().getPluginManager().registerEvents(new JoinListener(config.getRewards()), this);
            });
		}

		MCDB.registerCommand(this, new LinkCommand(linkManager, config));

        // 60 = seconds in one min.
        // 5 = the mins
        // 20 = one second in game ticks
        long fiveMinInTicks = (60 * 5) * 20; // 6000 ticks
        getServer().getScheduler().runTaskTimer(this, () -> linkManager.save(), fiveMinInTicks, fiveMinInTicks);
    }

    @Override
    public void onDisable() {
        linkManager.save();
    }

    private FileConfiguration getConfig(String name) {
        return YamlConfiguration.loadConfiguration(new File(getDataFolder(), name));
    }

    private void extractEmbeds() {
        final File out = new File(getDataFolder(), "embed");

        try (ZipExtractor ex = new ZipExtractor(getFile())) {
            ex.filter("embed\\/.*\\.json");
            ex.extract(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
