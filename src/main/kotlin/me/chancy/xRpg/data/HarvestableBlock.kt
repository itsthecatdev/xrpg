package me.chancy.xRpg.data

import com.jeff_media.customblockdata.CustomBlockData
import com.nexomc.nexo.api.NexoItems
import me.chancy.xRpg.XRpg
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

data class HarvestableBlock(
    val key: String,
    val hitCount: Int,
    val miningLevelRequirement: Int,
    val minimumTool: Material,
    val dropTable: List<ItemDrop> = emptyList()
) {
    data class ItemDrop(
        val itemStack: ItemStack,
        val chance: Double,
        val minAmount: Int = 1,
        val maxAmount: Int = 1
    )

    companion object {
        fun fromConfig(configSection: ConfigurationSection): HarvestableBlock? {
            return HarvestableBlock(
                key = configSection.name,
                hitCount = configSection.getInt("hit_count", 3),
                miningLevelRequirement = configSection.getInt("mining_level_requirement", 0),
                minimumTool = Material.valueOf(configSection.getString("minimum_pickaxe", "WOODEN_PICKAXE")!!.uppercase()),
                dropTable = parseDropTable(configSection)
            )
        }

        private fun parseDropTable(config: ConfigurationSection): List<ItemDrop> {
            return config.getConfigurationSection("drops")
                ?.getKeys(false)
                ?.mapNotNull { dropKey ->
                    val dropConfig = config.getConfigurationSection("drops.$dropKey") ?: return@mapNotNull null
                    val itemStack = createItemStack(dropKey) ?: return@mapNotNull null
                    ItemDrop(
                        itemStack = itemStack,
                        chance = dropConfig.getDouble("chance", 1.0),
                        minAmount = dropConfig.getInt("min_amount", 1),
                        maxAmount = dropConfig.getInt("max_amount", 1)
                    )
                } ?: emptyList()
        }

        private fun createItemStack(key: String): ItemStack? {
            return when {
                key.startsWith("nexo:") -> NexoItems.itemFromId(key.removePrefix("nexo:"))?.build()
                else -> Material.matchMaterial(key)?.let { ItemStack(it) }
            }
        }

        fun Block.getHarvestableBlockData(): HarvestableBlock? {
            val key = CustomBlockData(this, XRpg.instance).get(XRpg.harvestableBlockKey, PersistentDataType.STRING) ?: return null
            return XRpg.instance.config.getConfigurationSection(key)?.let { fromConfig(it) }
        }

        fun Block.isHarvestable(): Boolean = getHarvestableBlockData() != null
    }

    fun createItem(): ItemStack? = createItemStack(key)
}

