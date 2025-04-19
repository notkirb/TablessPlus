package com.notkirb.tablessPlus

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.*
import net.kyori.adventure.Adventure
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.eclipse.sisu.space.asm.Handle

class TablessPlus : JavaPlugin(), Listener {

    override fun onEnable() {

        saveDefaultConfig()

        server.pluginManager.registerEvents(this, this)

        if (!config.getBoolean("enabled"))  {
            server.pluginManager.disablePlugin(this)
        }

        val tabMessageConfig = config.getString("text")!!
        val tabMessage = MiniMessage.miniMessage().deserialize(tabMessageConfig)
        val adventureJson = GsonComponentSerializer.gson().serialize(tabMessage)

        val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

        protocolManager.addPacketListener(object : PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
            override fun onPacketSending(event: PacketEvent) {
                val packet = event.packet
                val actions = packet.playerInfoActions.read(0)

                if (actions.contains(EnumWrappers.PlayerInfoAction.ADD_PLAYER)) {
                    // Block adding players to the tab list
                    event.isCancelled = true
                }
                setTabListHeaderAdventure(event.player, tabMessage)
            }
        })
        logger.info("Started TablessPlus")
    }

    override fun onDisable() {
        logger.info("Goodbye!")
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerLoginEvent) {

    }

    fun setTabListHeader(player: Player, header: Component, footer: Component = Component.text("")) {
        val protocolManager = ProtocolLibrary.getProtocolManager()

        val packet = PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER)

        // Convert plain text to JSON chat component
        val headerComponent = WrappedChatComponent.fromHandle(header)
        val footerComponent = WrappedChatComponent.fromHandle(footer)

        // Write them into the packet
        packet.chatComponents.write(0, headerComponent) // Header
        packet.chatComponents.write(1, footerComponent) // Footer

        // Send to player
        protocolManager.sendServerPacket(player, packet)
    }
    fun setTabListHeaderAdventure(player: Player, header: Component, footer: Component  = Component.text("")) {
        player.sendPlayerListHeader { header }
        player.sendPlayerListFooter { footer }
    }
}
