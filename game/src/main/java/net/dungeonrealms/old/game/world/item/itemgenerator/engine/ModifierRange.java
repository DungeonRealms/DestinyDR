package net.dungeonrealms.old.game.world.item.itemgenerator.engine;

import java.util.Random;

public class ModifierRange {

	private ModifierType type;
	private int low;
	private int lowHigh;
	private int high;
	private int valLow, valHigh;
	private boolean halve;

    public ModifierRange(ModifierType type, int low, int high){
		this.type = type;
		this.low = low;
		this.high = high;
	}
	
	public ModifierRange(ModifierType type, int low, int high, boolean halve){
        this.type = type;
        this.low = low;
        this.high = high;
        this.halve = halve;
    }
	
	public ModifierRange(ModifierType type, int low, int lowHigh, int high){
		this.type = type;
		this.low = low;
		this.lowHigh = lowHigh;
		this.high = high;
	}
	
	public String generateRandom(){
		String random = "";
		Random r = new Random();
		
		int first = (high - low > 0 ? r.nextInt(high - low) + low : low);
		int second = high;
		
		if(type == ModifierType.RANGE){
			
			if(high - first > 0){
				second = r.nextInt(high - first) + first;
			}
			
			valLow = first;
			valHigh = second;
			
			random += String.valueOf(first);
			random += " - ";
			random += String.valueOf(second);
		}else if(type == ModifierType.TRIPLE){
		
			if(lowHigh - low > 0){
				first = r.nextInt(lowHigh - low) + low;
			}else{
				first = low;
			}
			
			if(high - first > 0){
				second = r.nextInt(high - first) + first;
			}
			
			valLow = first;
			valHigh = second;
			
			random += String.valueOf(first);
			random += " - ";
			random += String.valueOf(second);
			
		}else{
		    if (halve) {
		        random += (first / 2 >= 1 ? String.valueOf(first / 2) : "1");
		        valLow = first / 2 >= 1 ? first / 2 : 1;
		    }
		    else {
	            valLow = first;
		        random += String.valueOf(first);
		    }
		}
		
		return random;
	}
	
	public ModifierType getModifierType() {
	    return type;
	}
	
	   /**
     * @return the valLow
     */
    public int getValLow() {
        return valLow;
    }

    /**
     * @return the valHigh
     */
    public int getValHigh() {
        return valHigh;
    }
}
