package eu.mcdb.discordrewards.bukkit.connector;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.common.io.Files;

public class BukkitPlugin extends JavaPlugin {

    private String key;

    @Override
    public void onEnable() {
        getDataFolder().mkdir();
        final File file = new File(getDataFolder(), "command.key");

        try {
            if (file.exists()) {
                this.key = Files.readFirstLine(file, Charset.defaultCharset());
                new MessageListener(this);
            } else {
                getLogger().severe("=====================================================");
                getLogger().severe("== READ THE TUTORIAL ==");
                getLogger().severe("The 'command.key' file was not found, this plugin will not work");
                getLogger().severe("The 'command.key' file was not found, this plugin will not work");
                getLogger().severe("The 'command.key' file was not found, this plugin will not work");
                getLogger().severe("The 'command.key' file was not found, this plugin will not work");
                getLogger().severe("== READ THE TUTORIAL ==");
                getLogger().severe("=====================================================");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getKey() {
        return key;
    }
}
