package org.netutils.command

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import org.netutils.SharedVariables
import org.netutils.NetUtilsCommon
import org.netutils.packet.PacketRegistry
import org.netutils.packet.PacketSender
import org.netutils.screen.ScreenSaver
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MerchantMenu
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket

/**
 * Client-side command system for NetUtils.
 * Commands use . prefix and work in regular chat.
 */
object ClientCommands {
    
    private val mc: Minecraft get() = Minecraft.getInstance()
    
    // The command prefix
    const val PREFIX = "^"
    
    // Registered command names for autocomplete
    private val commandNames = listOf(
        "help", "toggle", "packets", "rawsend", 
        "save", "load", "screens", "menuinfo", "trades",
        "drop", "trade", "button", "leave", "wake",
        "click", "loop", "swing", "close", "desync", "rpack", "clear"
    )
    
    /**
     * Initialize the command system.
     */
    fun register() {
        NetUtilsCommon.LOGGER.info("Registered ${commandNames.size} client commands")
    }
    
    /**
     * Try to execute a command from chat input.
     * @param message The chat message (with prefix)
     * @return true if command was handled, false otherwise
     */
    fun tryExecute(message: String): Boolean {
        if (!message.startsWith(PREFIX)) return false
        
        val command = message.substring(PREFIX.length).trim()
        if (command.isEmpty()) return false
        
        // Parse command and args
        val parts = command.split(Regex("\\s+"), limit = 2)
        val cmdName = parts[0].lowercase()
        val argsStr = if (parts.size > 1) parts[1] else ""
        val args = if (argsStr.isEmpty()) emptyList() else argsStr.split(Regex("\\s+"))
        
        // Execute command
        return when (cmdName) {
            "help" -> { showHelp(); true }
            "toggle" -> { toggle(); true }
            "packets" -> { listPackets(); true }
            "rawsend" -> { rawSend(args); true }
            "save" -> { saveScreen(args); true }
            "load" -> { loadScreen(args); true }
            "screens" -> { listScreens(); true }
            "menuinfo" -> { menuInfo(); true }
            "trades" -> { listTrades(); true }
            "drop" -> { drop(args); true }
            "trade" -> { trade(args); true }
            "button" -> { buttonClick(args); true }
            "leave" -> { leave(); true }
            "wake" -> { wake(); true }

            "click" -> { clickSlot(args); true }
            "loop" -> { loop(args); true }
            "swing" -> { swing(args); true }
            "close" -> { close(); true }
            "desync" -> { desync(); true }
            "rpack" -> { rpack(); true }
            "clear" -> { clear(); true }
            else -> {
                sendMessage("§c[NetUtils] Unknown command: $cmdName. Type ^help for help.")
                true
            }
        }
    }
    
    private fun showHelp() {
        sendMessage("""§e[NetUtils] Commands:
§7^help - Show commands
§7^toggle - Enable/disable
§7^menuinfo - GUI info (class, slots, sync ID)
§7^trades - List villager trades with IDs
§7^trade <id> - Select trade by ID
§7^button <id> - Click button by ID
§7^packets - List packets
§7^rawsend <times> <packet> [args]
§7^save/^load [name] - Save/load screen
§7^drop [all] - Drop items
§7^click <slot> <btn> [type] - Click slot
§7^loop <n> <cmd> - Repeat command
§7^close - Close screen
§7^desync - Desync player
§7^swing [hand] - Swing hand
§7^rpack - Toggle resource pack bypass
§7^clear - Clear chat
§7^leave/^wake""")
    }
    
    private fun toggle() {
        SharedVariables.enabled = !SharedVariables.enabled
        sendMessage("§e[NetUtils] " + if (SharedVariables.enabled) "Enabled" else "Disabled")
    }
    
    private fun listPackets() {
        val keys = PacketRegistry.getAllPacketKeys()
        sendMessage("§e[NetUtils] Packets (${keys.size}):\n§7${keys.joinToString(", ")}")
    }
    
    private fun rawSend(args: List<String>) {
        if (args.size < 2) {
            sendMessage("§c[NetUtils] Usage: ^rawsend <times> <packet> [args...]")
            return
        }
        val times = args[0].toIntOrNull() ?: run {
            sendMessage("§c[NetUtils] Invalid times: ${args[0]}")
            return
        }
        val packet = args[1]
        val packetArgs = if (args.size > 2) args.subList(2, args.size) else emptyList()
        val result = PacketSender.sendPacket(packet, times, packetArgs)
        sendMessage(result)
    }
    
    private fun saveScreen(args: List<String>) {
        val name = args.getOrNull(0) ?: "default"
        try {
            ScreenSaver.saveScreen(name)
            sendMessage("§a[NetUtils] Saved screen to '$name'")
        } catch (e: Exception) {
            sendMessage("§c[NetUtils] ${e.message}")
        }
    }
    
    private fun loadScreen(args: List<String>) {
        val name = args.getOrNull(0) ?: "default"
        try {
            ScreenSaver.loadScreen(name)
            sendMessage("§a[NetUtils] Loaded screen from '$name'")
        } catch (e: Exception) {
            sendMessage("§c[NetUtils] ${e.message}")
        }
    }
    
    private fun listScreens() {
        val slots = ScreenSaver.listSlots()
        sendMessage("§e[NetUtils] Saved screens: §7${slots.joinToString(", ")}")
    }
    
    private fun menuInfo() {
        val screen = mc.screen
        if (screen == null) {
            sendMessage("§c[NetUtils] No screen open")
        } else {
            val className = screen.javaClass.simpleName
            val title = screen.title.string
            val syncId = mc.player?.containerMenu?.containerId ?: -1
            val slotCount = mc.player?.containerMenu?.slots?.size ?: 0
            sendMessage("""§e[NetUtils] Menu Info:
§7Class: $className
§7Title: $title
§7Sync ID: $syncId
§7Slots: $slotCount
§7Use ^trades for villager trade list""")
        }
    }
    
    private fun listTrades() {
        val menu = mc.player?.containerMenu
        if (menu is net.minecraft.world.inventory.MerchantMenu) {
            val offers = menu.offers
            if (offers.isEmpty()) {
                sendMessage("§c[NetUtils] No trades available")
            } else {
                val sb = StringBuilder("§e[NetUtils] Trades (use ^trade <id>):\n")
                offers.forEachIndexed { index, offer ->
                    val cost1 = offer.costA
                    val cost2 = offer.costB
                    val result = offer.result
                    val disabled = if (offer.isOutOfStock) " §c[OUT]" else ""
                    sb.append("§7[$index] ${cost1.count}x ${cost1.displayName.string}")
                    if (!cost2.isEmpty) {
                        sb.append(" + ${cost2.count}x ${cost2.displayName.string}")
                    }
                    sb.append(" -> ${result.count}x ${result.displayName.string}$disabled\n")
                }
                sendMessage(sb.toString().trimEnd())
            }
        } else {
            sendMessage("§c[NetUtils] Not in a villager trade menu")
        }
    }
    
    private fun drop(args: List<String>) {
        val all = args.getOrNull(0)?.lowercase() == "all"
        mc.player?.drop(all)
        sendMessage("§a[NetUtils] Dropped ${if (all) "entire stack" else "single item"}")
    }
    
    private fun trade(args: List<String>) {
        val id = args.getOrNull(0)?.toIntOrNull() ?: run {
            sendMessage("§c[NetUtils] Usage: ^trade <id>")
            return
        }
        
        val menu = mc.player?.containerMenu
        if (menu is MerchantMenu) {
            if (id < 0 || id >= menu.offers.size) {
                sendMessage("§c[NetUtils] Invalid trade ID: $id")
                return
            }
            
            // 1. Update client-side selection to populate slots
            menu.setSelectionHint(id)
            
            // 2. Send selection packet
            PacketSender.sendPacket("play.select_trade", 1, listOf(id.toString()))
            
            // 3. Execute trade (Shift-click result slot 2)
            mc.gameMode?.handleInventoryMouseClick(
                menu.containerId,
                2, // Result slot
                0,
                ClickType.QUICK_MOVE,
                mc.player!!
            )
            sendMessage("§a[NetUtils] Executed trade #$id")
        } else {
            // Fallback for non-merchant menus
            val result = PacketSender.sendPacket("play.select_trade", 1, listOf(id.toString()))
            sendMessage(result)
        }
    }
    
    private fun buttonClick(args: List<String>) {
        val id = args.getOrNull(0)?.toIntOrNull() ?: run {
            sendMessage("§c[NetUtils] Usage: ^button <id>")
            return
        }
        val syncId = mc.player?.containerMenu?.containerId ?: 0
        val result = PacketSender.sendPacket("play.button_click", 1, listOf(syncId.toString(), id.toString()))
        sendMessage(result)
    }
    
    private fun leave() {
        mc.level?.disconnect(Component.literal("Disconnected by NetUtils"))
        sendMessage("§a[NetUtils] Disconnected")
    }
    
    private fun wake() {
        mc.player?.stopSleepInBed(true, true)
        sendMessage("§a[NetUtils] Woke up (client-side)")
    }
    
    private fun clickSlot(args: List<String>) {
        if (args.isEmpty()) {
            sendMessage("§c[NetUtils] Usage: ^click <slot> [button] [type]")
            return
        }
        
        val slot = args[0].toIntOrNull() ?: run {
            sendMessage("§c[NetUtils] Invalid slot: ${args[0]}")
            return
        }
        val button = args.getOrNull(1)?.toIntOrNull() ?: 0
        val typeStr = args.getOrNull(2) ?: "pickup"
        val type = PacketRegistry.parseClickType(typeStr)
        
        val menu = mc.player?.containerMenu
        if (menu != null) {
            mc.gameMode?.handleInventoryMouseClick(
                menu.containerId, slot, button, type, mc.player!!
            )
            sendMessage("§a[NetUtils] Clicked slot $slot (btn=$button, type=$type)")
        } else {
             sendMessage("§c[NetUtils] No container open")
        }
    }
    
    private fun loop(args: List<String>) {
        if (args.size < 2) {
            sendMessage("§c[NetUtils] Usage: ^loop <times> <command> [args...]")
            return
        }
        val times = args[0].toIntOrNull() ?: run {
            sendMessage("§c[NetUtils] Invalid count: ${args[0]}")
            return
        }
        val cmdToRun = args.subList(1, args.size).joinToString(" ")
        
        // Prevent recursive loop calls
        if (cmdToRun.trim().lowercase().startsWith("loop")) {
             sendMessage("§c[NetUtils] Cannot loop a loop command!")
             return
        }
        
        val commandWithPrefix = if (cmdToRun.startsWith(PREFIX)) cmdToRun else PREFIX + cmdToRun
        
        sendMessage("§e[NetUtils] Looping '$commandWithPrefix' $times times...")
        repeat(times) {
            tryExecute(commandWithPrefix)
        }
    }
    
    private fun swing(args: List<String>) {
        val hand = args.getOrNull(0) ?: "main_hand"
        PacketSender.sendPacket("play.hand_swing", 1, listOf(hand))
        sendMessage("§a[NetUtils] Swung $hand")
    }
    
    private fun close() {
        mc.player?.closeContainer()
        sendMessage("§a[NetUtils] Closed container")
    }
    
    private fun desync() {
        if (mc.connection != null && mc.player != null) {
            mc.connection!!.send(ServerboundContainerClosePacket(mc.player!!.containerMenu.containerId))
            sendMessage("§a[NetUtils] Sent close packet (Inventory Desync)")
        } else {
             sendMessage("§c[NetUtils] Error: No connection or player")
        }
    }
    
    private fun rpack() {
        SharedVariables.bypassResourcePack = !SharedVariables.bypassResourcePack
        sendMessage("§e[NetUtils] Bypass Resource Pack: " + if (SharedVariables.bypassResourcePack) "ON" else "OFF")
    }
    
    private fun clear() {
        mc.gui.chat.clearMessages(true)
        sendMessage("§a[NetUtils] Chat cleared")
    }
    
    /**
     * Get command suggestions for autocomplete.
     */
    fun getSuggestions(input: String): List<String> {
        if (!input.startsWith(PREFIX)) return emptyList()
        
        val command = input.substring(PREFIX.length)
        
        // If empty, return all commands
        if (command.isEmpty()) {
            return commandNames.map { PREFIX + it }
        }
        
        // Return matching commands
        return commandNames
            .filter { it.startsWith(command, ignoreCase = true) }
            .map { PREFIX + it }
    }
    
    private fun sendMessage(message: String) {
        mc.player?.displayClientMessage(Component.literal(message), false)
    }
}
