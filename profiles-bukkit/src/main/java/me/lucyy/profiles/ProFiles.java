package me.lucyy.profiles;

import me.lucyy.common.command.Command;
import me.lucyy.common.command.HelpSubcommand;
import me.lucyy.common.update.UpdateChecker;
import me.lucyy.profiles.api.ProfileManager;
import me.lucyy.profiles.command.ClearSubcommand;
import me.lucyy.profiles.command.SetSubcommand;
import me.lucyy.profiles.command.ShowSubcommand;
import me.lucyy.profiles.field.factory.PlaceholderFieldFactory;
import me.lucyy.profiles.field.factory.ProNounsFieldFactory;
import me.lucyy.profiles.field.factory.SimpleFieldFactory;
import me.lucyy.profiles.storage.Storage;
import me.lucyy.profiles.storage.YamlStorage;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class ProFiles extends JavaPlugin {

    private final OptionalDependencyHandler depHandler = new OptionalDependencyHandler(this);
	private ProfileManagerImpl profileManager;
    private Storage storage;

	private ConfigHandler config;

	public ProfileManagerImpl getProfileManager() {
		return profileManager;
	}

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onEnable() {
		Metrics metrics = new Metrics(this,10559);

		config = new ConfigHandler(this);

		storage = new YamlStorage(this);
        profileManager = new ProfileManagerImpl(this, storage);

        getServer().getServicesManager().register(ProfileManager.class, profileManager, this, ServicePriority.Normal);

		Command cmd = new Command(config);
        cmd.register(new HelpSubcommand(cmd, config, this));
        cmd.register(new SetSubcommand(this));
		cmd.register(new ClearSubcommand(this));
		ShowSubcommand defaultSub = new ShowSubcommand(this);
        cmd.register(defaultSub);
        cmd.setDefaultSubcommand(defaultSub);

        getCommand("profile").setExecutor(cmd);
		getCommand("profile").setTabCompleter(cmd);

		profileManager.register("simple", new SimpleFieldFactory(profileManager));
        profileManager.register("pronouns", new ProNounsFieldFactory(depHandler));
		profileManager.register("placeholder", new PlaceholderFieldFactory());

        if (getConfigHandler().checkForUpdates()) {
        	getLogger().warning("Update checking is disabled. You might be running an old version!");
		} else {
        	new UpdateChecker(this,
					"", //"https://api.spigotmc.org/legacy/update.php?resource=86199" TODO set url
					getConfigHandler().getPrefix() +
							"A new version of ProFiles is available!\nFind it at "
							+ getConfigHandler().getAccentColour() + "https://lucyy.me/profiles",
					"profiles.admin"
			);
		}

        new BukkitRunnable() {
        	@Override
			public void run() {
        		try {
					profileManager.loadFields();
				} catch (InvalidConfigurationException e) {
        			getLogger().severe("Failed to load config: " + e.getMessage());
				}
			}
		}.runTaskLater(this, 1);
    }

    @Override
    public void onDisable() {
        storage.close();
    }

	public ConfigHandler getConfigHandler() {
		return config;
	}
}
