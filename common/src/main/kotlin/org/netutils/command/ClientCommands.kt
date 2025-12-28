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
     * Represents the result of a suggestion request.
     */
    data class SuggestionsResult(val suggestions: List<String>, val startIndex: Int)

    /**
     * Get command suggestions for autocomplete.
     * Provides both command name completion and parameter-level completion.
     */
    fun getSuggestions(input: String): SuggestionsResult {
        if (!input.startsWith(PREFIX)) return SuggestionsResult(emptyList(), 0)
        
        val command = input.substring(PREFIX.length)
        
        // If empty, return all commands
        if (command.isEmpty()) {
            return SuggestionsResult(commandNames.map { it }, PREFIX.length)
        }
        
        // Split into command and args
        // Using a regex that preserves whitespace to correctly calculate indices
        val parts = command.split(Regex("(?<=\\s)|(?=\\s+)"))
        val cmdName = parts[0].trim().lowercase()
        
        // If still typing the command name (no space yet), suggest matching commands
        if (parts.size == 1 && !command.endsWith(" ")) {
            return SuggestionsResult(
                commandNames.filter { it.startsWith(cmdName, ignoreCase = true) },
                PREFIX.length
            )
        }
        
        // Get the current argument being typed and its start index
        var currentOffset = PREFIX.length
        var argIndex = 0
        var currentArg = ""
        var lastArgStartIndex = PREFIX.length
        
        val argParts = command.split(Regex("\\s+"))
        val isTrailingSpace = input.endsWith(" ")
        
        if (isTrailingSpace) {
            argIndex = argParts.size
            currentArg = ""
            lastArgStartIndex = input.length
        } else {
            argIndex = argParts.size - 1
            currentArg = argParts.last()
            lastArgStartIndex = input.lastIndexOf(currentArg)
        }

        // Get parameter suggestions for the command
        val cmdForParams = argParts[0].lowercase()
        val paramSuggestions = getParameterSuggestions(cmdForParams, argIndex, currentArg)
        
        return SuggestionsResult(paramSuggestions, lastArgStartIndex)
    }
    
    /**
     * Get parameter suggestions based on command and argument position.
     */
    private fun getParameterSuggestions(cmdName: String, argIndex: Int, currentArg: String): List<String> {
        return when (cmdName) {
            // ^click <slot> <button> [type]
            "click" -> when (argIndex) {
                1 -> getSlotSuggestions(currentArg)
                2 -> listOf("0", "1").filter { it.startsWith(currentArg) } // 0=left, 1=right
                3 -> listOf("pickup", "shift", "quick_move", "swap", "clone", "throw", "quick_craft", "pickup_all")
                    .filter { it.startsWith(currentArg, ignoreCase = true) }
                else -> emptyList()
            }
            
            // ^trade <id>
            "trade" -> when (argIndex) {
                1 -> getTradeSuggestions(currentArg)
                else -> emptyList()
            }
            
            // ^button <id>
            "button" -> when (argIndex) {
                1 -> (0..10).map { it.toString() }.filter { it.startsWith(currentArg) }
                else -> emptyList()
            }
            
            // ^rawsend <times> <packet> [args...]
            "rawsend" -> when (argIndex) {
                1 -> listOf("1", "5", "10", "50", "100").filter { it.startsWith(currentArg) }
                2 -> PacketRegistry.getAllPacketKeys().filter { it.startsWith(currentArg, ignoreCase = true) }
                else -> getPacketArgSuggestions(getArgAt(argIndex - 2), argIndex - 2, currentArg)
            }
            
            // ^loop <times> <command> [args...]
            "loop" -> when (argIndex) {
                1 -> listOf("1", "5", "10", "20", "50", "100").filter { it.startsWith(currentArg) }
                2 -> commandNames.filter { it != "loop" && it.startsWith(currentArg, ignoreCase = true) }
                else -> emptyList()
            }
            
            // ^swing [hand]
            "swing" -> when (argIndex) {
                1 -> listOf("main_hand", "off_hand").filter { it.startsWith(currentArg, ignoreCase = true) }
                else -> emptyList()
            }
            
            // ^drop [all]
            "drop" -> when (argIndex) {
                1 -> listOf("all").filter { it.startsWith(currentArg, ignoreCase = true) }
                else -> emptyList()
            }
            
            // ^save [name] / ^load [name]
            "save" -> when (argIndex) {
                1 -> getSavedScreenSuggestions(currentArg) + listOf("default").filter { it.startsWith(currentArg, ignoreCase = true) }
                else -> emptyList()
            }
            "load" -> when (argIndex) {
                1 -> getSavedScreenSuggestions(currentArg)
                else -> emptyList()
            }
            
            else -> emptyList()
        }
    }
    
    /**
     * Get slot number suggestions based on current container.
     */
    private fun getSlotSuggestions(currentArg: String): List<String> {
        val menu = mc.player?.containerMenu
        val slotCount = menu?.slots?.size ?: 45
        
        // Common slots based on input prefix
        val suggestions = mutableListOf<String>()
        
        // If empty, show common slot ranges
        if (currentArg.isEmpty()) {
            suggestions.addAll(listOf("0", "1", "2", "36", "37", "38", "39", "40", "44"))
        } else {
            // Filter slots that start with the current input
            for (i in 0 until slotCount) {
                if (i.toString().startsWith(currentArg)) {
                    suggestions.add(i.toString())
                }
                if (suggestions.size >= 10) break
            }
        }
        
        return suggestions.take(10)
    }
    
    /**
     * Get trade ID suggestions based on current merchant menu.
     */
    private fun getTradeSuggestions(currentArg: String): List<String> {
        val menu = mc.player?.containerMenu
        if (menu is MerchantMenu) {
            return menu.offers.indices.map { it.toString() }.filter { it.startsWith(currentArg) }
        }
        return (0..10).map { it.toString() }.filter { it.startsWith(currentArg) }
    }
    
    /**
     * Get saved screen names for load/save commands.
     */
    private fun getSavedScreenSuggestions(currentArg: String): List<String> {
        return try {
            ScreenSaver.listSlots().filter { it.startsWith(currentArg, ignoreCase = true) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get packet-specific argument suggestions.
     */
    private fun getPacketArgSuggestions(packetKey: String?, argIndex: Int, currentArg: String): List<String> {
        if (packetKey == null) return emptyList()
        
        return when (packetKey) {
            "play.hand_swing" -> when (argIndex) {
                0 -> listOf("main_hand", "off_hand").filter { it.startsWith(currentArg, ignoreCase = true) }
                else -> emptyList()
            }
            "play.update_selected_slot" -> when (argIndex) {
                0 -> (0..8).map { it.toString() }.filter { it.startsWith(currentArg) }
                else -> emptyList()
            }
            "play.close_handled_screen" -> when (argIndex) {
                0 -> listOf((mc.player?.containerMenu?.containerId ?: 0).toString())
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
    
    /**
     * Helper to safely get argument at index from stored context.
     * Used for rawsend packet arg completion.
     */
    private var lastRawsendPacket: String? = null
    private fun getArgAt(index: Int): String? {
        // This is a simplified implementation
        // In a full implementation, you'd track the full command context
        return lastRawsendPacket
    }

    private fun sendMessage(message: String) {
        mc.player?.displayClientMessage(Component.literal(message), false)
    }
}
