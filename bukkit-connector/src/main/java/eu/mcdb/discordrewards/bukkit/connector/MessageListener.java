package eu.mcdb.discordrewards.bukkit.connector;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

public class MessageListener implements PluginMessageListener {

    private static final String CHANNEL_NAME = "BungeeCord";
    private static final String SUBCHANNEL_NAME = "DiscordRewards";
    private final BukkitPlugin plugin;

    public MessageListener(BukkitPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL_NAME, this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(CHANNEL_NAME))
            return;

        final ByteArrayDataInput in = ByteStreams.newDataInput(message);
        final String sub = in.readUTF();

        if (!sub.equals(SUBCHANNEL_NAME))
            return;

        final String key = in.readUTF();

        if (!plugin.getKey().equals(key)) {
            plugin.getLogger().warning("Cancelled Reward because an invalid key was given. Player: " + player.getName());
            return;
        }

        final String action = in.readUTF();

        switch (action) {
        case "GiveReward":
            final String command = in.readUTF();
            plugin.getLogger().info("Received command: /" + command);
            dispatchCommand(command);
            break;
        default:
            plugin.getLogger().warning("Unknown action: " + sub);
            break;
        }
    }

    private boolean dispatchCommand(String command) {
        return plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
