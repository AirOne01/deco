package net.airone01.deco.tasks

import net.airone01.deco.commands.HeadItem
import net.airone01.deco.utils.Web.Companion.readJsonFromUrl
import org.bukkit.scheduler.BukkitRunnable
import net.airone01.deco.Deco
import net.airone01.deco.commands.BackItem
import net.airone01.deco.commands.ForwardItem
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.window.Window

class HeadsApiTask : BukkitRunnable() {
    override fun run() {
        val json = readJsonFromUrl("https://deco-web.vercel.app/api/heads")

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
            .addIngredient('<', BackItem())
            .addIngredient('>', ForwardItem())
            .setContent(heads)
            .build()

        Deco.window = Window.single()
            .setTitle("Deco")
            .setGui(gui)
    }
}