package nu.nerd.easyrider.commands;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import nu.nerd.easyrider.Ability;
import nu.nerd.easyrider.EasyRider;
import nu.nerd.easyrider.IPendingInteraction;
import nu.nerd.easyrider.db.SavedHorse;

// ----------------------------------------------------------------------------
/**
 * Executor for the /horse-set-level admin command.
 */
public class HorseSetLevelExecutor extends ExecutorBase {
    // ------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public HorseSetLevelExecutor() {
        super("horse-set-level", "health", "jump", "speed", "help");
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender,
     *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be in game to use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
                return false;
            }

            Ability ability = EasyRider.CONFIG.getAbility(args[0].toLowerCase());
            if (ability == null) {
                sender.sendMessage(ChatColor.RED + "That is not a valid ability name.");
                return true;
            }

            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "You need to specify the new level.");
                return true;
            }

            try {
                int newLevel = Integer.parseInt(args[1]);
                if (newLevel < 1) {
                    sender.sendMessage(ChatColor.RED + "The new level must be at least 1.");
                    return true;
                }
                if (newLevel > ability.getMaxLevel()) {
                    sender.sendMessage(ChatColor.RED + "WARNING: The new level exceeds the maximum trainable level, " +
                                       ability.getMaxLevel() + ".");
                }

                sender.sendMessage(ChatColor.GOLD + "Right click on a horse to set " +
                                   ability.getName() + " level " + newLevel + ". (To cancel, relog.)");
                EasyRider.PLUGIN.getState(player).setPendingInteraction(new IPendingInteraction() {
                    @Override
                    public void onPlayerInteractEntity(PlayerInteractEntityEvent event, SavedHorse savedHorse) {
                        Horse horse = (Horse) event.getRightClicked();
                        Player player = event.getPlayer();
                        player.sendMessage(ChatColor.GOLD + "Horse: " + ChatColor.YELLOW + horse.getUniqueId());
                        showLevel(player, "Old ", ability, savedHorse);
                        ability.setLevel(savedHorse, newLevel);
                        ability.setEffort(savedHorse, ability.getEffortForLevel(newLevel));
                        ability.updateAttributes(savedHorse, horse);
                        showLevel(player, "New ", ability, savedHorse);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    }
                });
            } catch (NumberFormatException ex) {
                sender.sendMessage(ChatColor.RED + "The new level must be an integer.");
            }
            return true;
        }
        return false;
    } // onCommand

    // ------------------------------------------------------------------------
    /**
     * Show the new or old level, effort and display value of a horse's ability.
     * 
     * @param player the player to be send messages.
     * @param prefix the prefix string before the Ability display name.
     * @param abililty the {@link Ability}.
     * @param savedHorse the database state of the horse.
     */
    protected void showLevel(Player player, String prefix, Ability ability, SavedHorse savedHorse) {
        player.sendMessage(ChatColor.GOLD + prefix + ability.getDisplayName() + " Level: " +
                           ChatColor.WHITE + ability.getLevel(savedHorse) +
                           ChatColor.GOLD + " - " +
                           ChatColor.YELLOW + ability.getFormattedValue(savedHorse) +
                           ChatColor.GRAY + " (" + ability.getFormattedEffort(savedHorse) + ")");
    }
} // class HorseSetLevelExecutor