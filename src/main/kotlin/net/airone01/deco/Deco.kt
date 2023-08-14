package net.airone01.deco

import net.airone01.deco.commands.DecoCommand
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class Deco: JavaPlugin(), Listener {
    override fun onEnable() {
        logger.info("Deco enabled!")
        getCommand("deco")?.setExecutor(DecoCommand())
    }

    override fun onDisable() {
        logger.info("Deco disabled!")
    }
}