package me.chancy.xRpg.listeners.mechanics

import com.jeff_media.customblockdata.CustomBlockData
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import me.chancy.xRpg.XRpg
import me.chancy.xRpg.data.HarvestableBlock
import me.chancy.xRpg.data.HarvestableBlock.Companion.getHarvestableBlockData
import me.chancy.xRpg.data.HarvestableBlock.Companion.isHarvestable
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import java.util.*

class HarvestableBlockListener : Listener {
    private val playerHarvestData = mutableMapOf<UUID, HarvestData>()

    private data class HarvestData(
        val block: Block,
        val harvestableData: HarvestableBlock,
        var hitCount: Int = 0,
        var harvestEntity: Interaction? = null,
        var cleanupTask: BukkitTask? = null
    )

    @EventHandler
    fun BlockPlaceEvent.onBlockPlace() {
        val configKey = itemInHand.itemMeta?.persistentDataContainer?.get(XRpg.harvestableBlockKey, PersistentDataType.STRING)
            ?: return
        CustomBlockData(block, XRpg.instance).set(XRpg.harvestableBlockKey, PersistentDataType.STRING, configKey)
    }

    @EventHandler
    fun PlayerInteractEvent.onBlockInteract() {
        if (hand != EquipmentSlot.HAND || action != Action.LEFT_CLICK_BLOCK) return
        val clickedBlock = clickedBlock ?: return
        if (!clickedBlock.isHarvestable()) return

        val harvestState = player.getHarvestState()
        if (harvestState != null) return

        player.summonHarvestEntity(clickedBlock)
    }

    @EventHandler
    fun PrePlayerAttackEntityEvent.onEntityClick() {
        val clickedEntity = attacked as? Interaction ?: return
        val harvestState = player.getHarvestState() ?: return

        if (harvestState.harvestEntity != clickedEntity) return

        harvestState.hitCount++
        harvestState.cleanupTask?.cancel()
        harvestState.cleanupTask = scheduleHarvestAbort(player)

        player.sendMessage("Hit count: ${harvestState.hitCount}")

        if (harvestState.hitCount >= harvestState.harvestableData.hitCount) {
            harvestBlock(harvestState)
            player.finishHarvesting()
        }
    }

    private fun harvestBlock(harvestState: HarvestData) {
        harvestState.block.type = Material.STONE
        harvestState.harvestableData.dropTable.forEach { drop ->
            if (Math.random() < drop.chance) {
                val amount = (drop.minAmount..drop.maxAmount).random()
                val dropItem = drop.itemStack.clone()
                dropItem.amount = amount
                harvestState.block.world.dropItemNaturally(harvestState.block.location, dropItem)
            }
        }
    }

    private fun Player.summonHarvestEntity(block: Block) {
        val harvestableData = block.getHarvestableBlockData() ?: return
        val entity = world.spawn(block.location, Interaction::class.java, CreatureSpawnEvent.SpawnReason.DEFAULT) {
            it.setGravity(false)
        }

        val cleanupTask = scheduleHarvestAbort(this)
        playerHarvestData[uniqueId] = HarvestData(
            block = block,
            harvestableData = harvestableData,
            harvestEntity = entity,
            cleanupTask = cleanupTask
        )
    }

    private fun scheduleHarvestAbort(player: Player): BukkitTask {
        return Bukkit.getScheduler().runTaskLater(XRpg.instance, Runnable {
            playerHarvestData[player.uniqueId]?.let { harvestState ->
                harvestState.harvestEntity?.remove()
                playerHarvestData.remove(player.uniqueId)
                player.sendMessage("Harvest interaction timed out.")
            }
        }, 140L)
    }

    private fun Player.finishHarvesting() {
        getHarvestState()?.let { state ->
            state.harvestEntity?.remove()
            state.cleanupTask?.cancel()
        }
        playerHarvestData.remove(uniqueId)
    }

    private fun Player.getHarvestState(): HarvestData? = playerHarvestData[uniqueId]
}

