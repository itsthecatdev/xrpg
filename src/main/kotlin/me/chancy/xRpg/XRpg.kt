package me.chancy.xRpg

import org.bukkit.plugin.java.JavaPlugin

class XRpg : JavaPlugin() {
    companion object {
        private lateinit var xRpg: XRpg

        fun getInstance(): XRpg = xRpg
    }

    override fun onEnable() {
        xRpg = this
        saveDefaultConfig()
    }
}
