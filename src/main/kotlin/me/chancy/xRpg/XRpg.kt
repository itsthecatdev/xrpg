package me.chancy.xRpg

import com.jeff_media.customblockdata.CustomBlockData
import me.chancy.xRpg.commands.GiveHarvestableCommand
import me.chancy.xRpg.listeners.mechanics.HarvestableBlockListener
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class XRpg : JavaPlugin() {
    companion object {
        lateinit var instance: XRpg
            private set
        lateinit var harvestableBlockKey: NamespacedKey
            private set
    }

    override fun onLoad() {
        instance = this
        harvestableBlockKey = NamespacedKey(this, "harvestable_block")
        saveDefaultConfig()
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(HarvestableBlockListener(), this)
        CustomBlockData.registerListener(this)
        getCommand("xrpg")?.setExecutor(GiveHarvestableCommand())
    }
}

