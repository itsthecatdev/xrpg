package me.chancy.xRpg.commands

import me.chancy.xRpg.XRpg
import me.chancy.xRpg.data.HarvestableBlock
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class GiveHarvestableCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args[0].lowercase() != "give" || sender !is Player) return false

        val itemKey = args[1].lowercase()
        val harvestableBlock = XRpg.instance.config.getConfigurationSection(itemKey)
            ?.let { HarvestableBlock.fromConfig(it) } ?: return false

        val itemStack = harvestableBlock.createItem() ?: return false
        itemStack.editMeta { meta ->
            meta.persistentDataContainer.set(XRpg.harvestableBlockKey, PersistentDataType.STRING, itemKey)
        }

        sender.inventory.addItem(itemStack)
        sender.sendMessage("You have received a ${harvestableBlock.key} harvestable block.")
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> listOf("give")
            2 -> XRpg.instance.config.getKeys(false).toList()
            else -> emptyList()
        }
    }
}