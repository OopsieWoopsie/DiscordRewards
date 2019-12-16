package eu.mcdb.discordrewards.command;

import eu.mcdb.discordrewards.LinkManager;
import eu.mcdb.discordrewards.config.Config;
import eu.mcdb.universal.command.UniversalCommand;
import eu.mcdb.universal.command.UniversalCommandSender;
import eu.mcdb.universal.player.UniversalPlayer;

public class LinkCommand extends UniversalCommand {

    private final LinkManager linkManager;
    private final Config config;

    public LinkCommand(LinkManager linkManager, Config config) {
        super("link", null, "discord");
        this.linkManager = linkManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(UniversalCommandSender sender, String[] args) {
        if (sender.isPlayer()) {
            UniversalPlayer player = sender.getPlayer();

            if (linkManager.isVerified(player)) {
                player.sendMessage(config.getAlreadyVerifiedMessage());
            } else {
                String code = linkManager.generateCode(player);

                for (String s : config.getVerifyInstructions(code)) {
                    player.sendMessage(s);
                }

                linkManager.addPendingPlayer(player, code);
            }
        } else {
            sender.sendMessage("You need to be a player to run this command!");
        }
        return true;
   }
}
