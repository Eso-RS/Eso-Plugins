package net.unethicalite.plugins.esomagic;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.magic.SpellBook;
import org.pf4j.Extension;

import javax.inject.Inject;

@Extension
@PluginDescriptor(
		name = "Eso Stun Alch",
		enabledByDefault = false
)
@Slf4j
public class EsoMagicPlugin extends Plugin
{
	@Inject
	private EsoMagicConfig config;

	private boolean doAlch;

	private int tickDelay;

	@Override
	protected void shutDown()
	{
		doAlch = false;
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		NPC targetNpc = NPCs.getNearest(config.npcId());
		Item targetItem = Inventory.getFirst(config.itemId());

		if (targetNpc == null || targetItem == null)
		{
			return;
		}

		if (noRunesLeft())
		{
			if (config.logoutNoRunes())
			{
				Game.logout();
				return;
			}
			return;
		}

		if (tickDelay > 0)
		{
			tickDelay--;
			return;
		}

		if (!doAlch)
		{
			Magic.cast(config.spell().getSpell(), targetNpc);
			doAlch = true;
			return;
		}

		Magic.cast(SpellBook.Standard.HIGH_LEVEL_ALCHEMY, targetItem);
		doAlch = false;
		tickDelay = 3;
	}

	private boolean noRunesLeft()
	{
		if (!SpellBook.Standard.HIGH_LEVEL_ALCHEMY.haveRunesAvailable())
		{
			return true;
		}

		if (config.spell().getSpell() == SpellBook.Standard.CURSE)
		{
			return !SpellBook.Standard.CURSE.haveRunesAvailable();
		}

		return !Inventory.contains("Soul rune");
	}

	@Provides
	EsoMagicConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EsoMagicConfig.class);
	}
}
