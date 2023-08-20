package net.airone01.deco

import net.airone01.deco.commands.DecoCommand
import net.airone01.deco.tasks.HeadsApiTask
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import xyz.xenondevs.invui.window.Window

class Deco: JavaPlugin(), Listener {
    companion object {
        lateinit var plugin: Deco
        lateinit var window: Window.Builder.Normal.Single // window without viewer (aka player)

        fun isWindowsInitialized() = ::window.isInitialized
    }

    override fun onEnable() {
        plugin = this

        getCommand("deco")?.setExecutor(DecoCommand())

        logger.info("Deco enabled!")

        val timing = 20L * 5L // every 5 seconds
        HeadsApiTask().runTaskTimerAsynchronously(plugin, 0L, timing)
    }
}