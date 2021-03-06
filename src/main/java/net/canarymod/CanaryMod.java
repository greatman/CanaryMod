package net.canarymod;

import net.canarymod.api.CanaryServer;
import net.canarymod.api.channels.CanaryChannelManager;
import net.canarymod.api.commandsys.CanaryPlayerSelector;
import net.canarymod.api.factory.CanaryFactory;
import net.canarymod.api.scoreboard.CanaryScoreboardManager;
import net.canarymod.bansystem.BanManager;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.commandsys.CommandList;
import net.canarymod.commandsys.CommandManager;
import net.canarymod.config.Configuration;
import net.canarymod.database.DatabaseLoader;
import net.canarymod.help.HelpManager;
import net.canarymod.hook.HookExecutor;
import net.canarymod.kit.KitProvider;
import net.canarymod.motd.CanaryMessageOfTheDayListener;
import net.canarymod.motd.MessageOfTheDay;
import net.canarymod.permissionsystem.PermissionManager;
import net.canarymod.plugin.PluginManager;
import net.canarymod.user.OperatorsProvider;
import net.canarymod.user.ReservelistProvider;
import net.canarymod.user.UserAndGroupsProvider;
import net.canarymod.user.WhitelistProvider;
import net.canarymod.util.CanaryJsonNBTUtility;
import net.canarymod.warp.WarpProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * The implementation of Canary, the new catch-all etc replacement, only much better :P
 *
 * @author Chris (damagefilter)
 * @author Jos Kuijpers
 * @author Brian (WWOL)
 * @author Jason (darkdiplomat)
 */
public class CanaryMod extends Canary {

    /**
     * Creates a new CanaryMod
     */
    public CanaryMod() {
        Canary.instance = this;

        // This must be the first thing to call!
        DatabaseLoader.load();
        NativeTranslate.initialize(); // Intialize native translation bridge
        setLoggerLevelDynamic(); //Once we know if debug is enabled, you can change the level accordingly

        this.jsonNBT = new CanaryJsonNBTUtility(); // Set up the Json to/from NBT utility
        this.motd = new MessageOfTheDay();
        // Initialize the subsystems that do not rely on others
        this.commandManager = new CommandManager();
        // this.permissionManager = new PermissionManager();
        this.hookExecutor = new HookExecutor();
        this.helpManager = new HelpManager();
        this.banManager = new BanManager();
        this.whitelist = new WhitelistProvider();
        this.ops = new OperatorsProvider();
        this.reservelist = new ReservelistProvider();
        this.factory = new CanaryFactory();
        this.playerSelector = new CanaryPlayerSelector();
        this.channelManager = new CanaryChannelManager();
        // Initialize the plugin loader and scan for plugins
        this.pluginManager = new PluginManager();
        this.scoreboardManager = new CanaryScoreboardManager();

        pluginManager.scanForPlugins();
    }

    /**
     * Separately set users and groups provider
     */
    public void initUserAndGroupsManager() {
        this.userAndGroupsProvider = new UserAndGroupsProvider();
    }

    /**
     * Separately set the warps provider
     */
    public void initWarps() {
        this.warpProvider = new WarpProvider();
    }

    public void initKits() {
        this.kitProvider = new KitProvider();
    }

    public void initCommands() {
        try {
            this.commandManager.registerCommands(new CommandList(), Canary.getServer(), false);
        }
        catch (CommandDependencyException e) {
            // Silently ignore this. If that happens someone intended to override system commands,
            // which is perfectly fine.
        }
    }

    public void initPermissions() {
        this.permissionManager = new PermissionManager();
    }

    public void initMOTDListener() {
        motd().registerMOTDListener(new CanaryMessageOfTheDayListener(), (net.canarymod.motd.MOTDOwner) getServer(), false);
    }

    @Override
    public void reload() {
        super.reload();
        setLoggerLevelDynamic();
        // Reload minecraft variables
        // ((CanaryConfigurationManager) instance.server.getConfigurationManager()).reload();
        // TODO RCON + QUERY?
        ((CanaryServer) instance.server).getHandle().reload();
    }

    static void setLoggerLevelDynamic() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        LoggerConfig logger = ctx.getConfiguration().getLoggers().get(LogManager.ROOT_LOGGER_NAME);
        logger.setLevel(Configuration.getServerConfig().getLoggerLevel());
        ctx.updateLoggers();
    }
}
