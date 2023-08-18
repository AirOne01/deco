package net.airone01.deco.commands

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import net.airone01.deco.Reflections
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.json.JSONException
import java.io.InputStreamReader
import org.json.JSONObject

class BackItem : PageItem(false) {
    override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
        val builder = ItemBuilder(Material.RED_STAINED_GLASS_PANE)
        builder.setDisplayName("§7Previous page")
            .addLoreLines(
                if (gui.hasPreviousPage())
                    "§7Go to page §e" + gui.currentPage + "§7/§e" + gui.pageAmount
                else "§cYou can't go further back"
            )
        return builder
    }
}

class ForwardItem : PageItem(true) {
    override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
        val builder = ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
        builder.setDisplayName("§7Next page")
            .addLoreLines(
                if (gui.hasNextPage())
                    "§7Go to page §e" + (gui.currentPage + 2) + "§7/§e" + gui.pageAmount
                else "§cThere are no more pages"
            )
        return builder
    }
}

class HeadItem(key: String) : AbstractItem() {
    val key2 = key

    @OptIn(ExperimentalEncodingApi::class)
    fun getCustomSkull(url: String?): ItemStack {
        val profile = GameProfile(UUID.randomUUID(), null)
        val propertyMap: PropertyMap = profile.properties
            ?: throw IllegalStateException("Profile doesn't contain a property map")

        val encodedData: ByteArray =
            Base64.encodeToByteArray(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).toByteArray())
        propertyMap.put("textures", Property("textures", String(encodedData)))
        val head = ItemStack(Material.PLAYER_HEAD, 1)
        val headMeta = head.itemMeta as SkullMeta
        val headMetaClass: Class<*> = headMeta.javaClass
        Reflections.getField(headMetaClass, "profile", GameProfile::class.java)[headMeta] = profile
        head.setItemMeta(headMeta)
        return head
    }

    private fun getHead(): ItemStack {
        return getCustomSkull("http://textures.minecraft.net/texture/$key2")
    }

    override fun getItemProvider(): ItemProvider {
        val item = ItemBuilder(getHead())

        return item
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        if (clickType === ClickType.LEFT) {
            player.inventory.addItem(getHead())
        } else if (clickType === ClickType.SHIFT_LEFT) {
            val item = getHead()
            item.amount = 64
            player.inventory.addItem(item)
        }
    }
}

class DecoCommand: CommandExecutor {
    @Throws(IOException::class)
    private fun readAll(rd: Reader): String {
        val sb = StringBuilder()
        var cp: Int
        while (rd.read().also { cp = it } != -1) {
            sb.append(cp.toChar())
        }
        return sb.toString()
    }

    @Throws(IOException::class, JSONException::class)
    fun readJsonFromUrl(url: String?): JSONObject {
        val `is` = URL(url).openStream()
        return try {
            val rd = BufferedReader(InputStreamReader(`is`, Charset.forName("UTF-8")))
            val jsonText = readAll(rd)
            JSONObject(jsonText)
        } finally {
            `is`.close()
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage("You must be a player to use this command!")
            return true
        }

        val p = sender as Player

        val border = SimpleItem(ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName("§r"))

        val json = readJsonFromUrl("https://deco-web.vercel.app/api/heads");
        val apiHeads = json.getJSONArray("heads")
        val heads = apiHeads.map { HeadItem(it.toString()) }

        // create the gui
        val gui = PagedGui.items()
            .setStructure(
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "< x x x x x x x >")
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL) // where paged items should be put
            .addIngredient('#', border)
            .addIngredient('<', BackItem())
            .addIngredient('>', ForwardItem())
            .setContent(heads)
            .build()

        val window = Window.single()
            .setViewer(p)
            .setTitle("Deco")
            .setGui(gui)
            .build()

        window.open()

        return true
    }
}