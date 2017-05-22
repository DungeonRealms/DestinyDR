package net.dungeonrealms.common.game.util;

import lombok.AllArgsConstructor;

/**
 * A replacement for ChatColor so bungee and kryo don't throw ClassNotDefinedExceptions
 * Created May 22nd, 2017.
 * @author Kneesnap
 */
@AllArgsConstructor
public enum ChatColor {
	
	BLACK('0'),
	DARK_BLUE('1'),
	DARK_GREEN('2'),
	DARK_AQUA('3'),
	DARK_RED('4'),
	DARK_PURPLE('5'),
	GOLD('6'),
	GRAY('7'),
	DARK_GRAY('8'),
	BLUE('9'),
	GREEN('a'),
	AQUA('b'),
	RED('c'),
	LIGHT_PURPLE('d'),
	YELLOW('e'),
	WHITE('f'),
	
	MAGIC('k'),
	BOLD('l'),
	STRIKETHROUGH('m'),
	UNDERLINE('n'),
	ITALIC('o'),
	RESET('r');
	
	private char code;
	
	@Override
	public String toString() {
		return "\247" + this.code;
	}
}
