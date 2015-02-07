package cyano.basemetals.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import cyano.basemetals.BaseMetals;
import cyano.basemetals.init.Materials;
import cyano.basemetals.material.MetalMaterial;

public class ItemMetalArmor extends net.minecraft.item.ItemArmor {

	protected final String customTexture;
	protected final MetalMaterial metal;
	protected final String repairOreDictName;
	
	protected ItemMetalArmor(MetalMaterial metal, ArmorMaterial armorMat, int renderIndex,
			int slot) {
		super(armorMat, renderIndex, slot);
		this.metal = metal;
		this.repairOreDictName = "ingot"+metal.getCapitalizedName();
		this.customTexture = BaseMetals.MODID+":textures/models/armor/"+metal.getName()+"_layer_"+(slot == 2 ? 2 : 1)+".png";
	}
	
	private static final int UPDATE_INTERVAL = 19;
	private static final Map<EntityPlayer,AtomicLong> playerUpdateTimestampMap = new HashMap<>();
	private static final Map<EntityPlayer,AtomicInteger> playerUpdateCountMap = new HashMap<>();
	@Override
	public void onArmorTick(World w, EntityPlayer player, ItemStack armor){
		if(playerUpdateTimestampMap.containsKey(player) == false){
			playerUpdateTimestampMap.put(player, new AtomicLong(0));
			playerUpdateCountMap.put(player, new AtomicInteger(0));
			return;
		}
		if(!w.isRemote && w.getTotalWorldTime() > playerUpdateTimestampMap.get(player).get()){
	//		FMLLog.info("onArmorTick update triggered at time="+w.getTotalWorldTime()+" (update target was "+playerUpdateTimestampMap.get(player).get()+") for player "+player.getName()+" by item "+armor); // debug code
			playerUpdateTimestampMap.get(player).set(w.getTotalWorldTime() + UPDATE_INTERVAL);
			int updateCount = playerUpdateCountMap.get(player).getAndIncrement();
			for(int i = 0; i < 4; i++){
				if(player.getCurrentArmor(i) != null && player.getCurrentArmor(i).getItem() instanceof ItemMetalArmor){
					doArmorUpdate(w,player,player.getCurrentArmor(i),updateCount);
				}
			}
		}
	}

	private static final Map<EntityPlayer,AtomicInteger> starsteelUpdateCache = new HashMap<>();
	private static final Map<EntityPlayer,AtomicInteger> adamantineUpdateCache = new HashMap<>();
	private static final Map<EntityPlayer,AtomicInteger> leadUpdateCache = new HashMap<>();
	private static final int EFFECT_DURATION = UPDATE_INTERVAL * 3;
	protected void doArmorUpdate(World w, EntityPlayer player, ItemStack armor,
			int i) {
	//	FMLLog.info("doArmorUpdate "+i+" for "+player.getName()+" on item "+armor); // debug code
		Item armorItem = armor.getItem();
		if(i % 2 == 0){
			// count armor pieces
			if(((ItemMetalArmor)armorItem).metal.equals(Materials.starsteel)){
				starsteel:{
					// used to count up the starsteel armor items
					if(starsteelUpdateCache.containsKey(player) == false){
						starsteelUpdateCache.put(player, new AtomicInteger(0));
					}
					starsteelUpdateCache.get(player).incrementAndGet();
					break starsteel;
				}
			}
			if(((ItemMetalArmor)armorItem).metal.equals(Materials.lead)){
				lead:{
					// used to count up the starsteel armor items
					if(leadUpdateCache.containsKey(player) == false){
						leadUpdateCache.put(player, new AtomicInteger(0));
					}
					leadUpdateCache.get(player).incrementAndGet();
					break lead;
				}
			}
			if(((ItemMetalArmor)armorItem).metal.equals(Materials.adamantine)){
				adamantine:{
					// used to count up the starsteel armor items
					if(adamantineUpdateCache.containsKey(player) == false){
						adamantineUpdateCache.put(player, new AtomicInteger(0));
					}
					adamantineUpdateCache.get(player).incrementAndGet();
					break adamantine;
				}
			}
		} else {
			// apply potion effects
			starsteel:{
				if(starsteelUpdateCache.containsKey(player) == false) break starsteel;
				int num = starsteelUpdateCache.get(player).getAndSet(0);
				if(num == 0) break starsteel;
				final PotionEffect jumpBoost = new PotionEffect(8,EFFECT_DURATION,num);
				player.addPotionEffect(jumpBoost);
				if(num > 1){
					final PotionEffect speedBoost = new PotionEffect(1,EFFECT_DURATION,num-1);
					player.addPotionEffect(speedBoost);
				}
				break starsteel;
			}
			lead:{
				if(leadUpdateCache.containsKey(player) == false) break lead;
				int level = leadUpdateCache.get(player).getAndSet(0) / 2;
				if(level == 0) break lead;
				if(level > 0){
					final PotionEffect speedLoss = new PotionEffect(2,EFFECT_DURATION,level);
					player.addPotionEffect(speedLoss);
				}
				break lead;
			}
			adamantine:{
				if(adamantineUpdateCache.containsKey(player) == false) break adamantine;
				int level = adamantineUpdateCache.get(player).getAndSet(0) / 2;
				if(level == 0) break adamantine;
				if(level > 0){
					final PotionEffect protection = new PotionEffect(11,EFFECT_DURATION,level);
					player.addPotionEffect(protection);
				}
				break adamantine;
			}
		}
	}

	public static ItemMetalArmor createHelmet(MetalMaterial metal){
		ArmorMaterial material = cyano.basemetals.init.Materials.getArmorMaterialFor(metal);
		if(material == null){
			// uh-oh
			FMLLog.severe("Failed to load armor material enum for "+metal);
		}
		return new ItemMetalArmor(metal,material,material.ordinal(),0);
	}
	
	public static ItemMetalArmor createChestplate(MetalMaterial metal){
		ArmorMaterial material = cyano.basemetals.init.Materials.getArmorMaterialFor(metal);
		if(material == null){
			// uh-oh
			FMLLog.severe("Failed to load armor material enum for "+metal);
		}
		return new ItemMetalArmor(metal,material,material.ordinal(),1);
	}
	
	public static ItemMetalArmor createLeggings(MetalMaterial metal){
		ArmorMaterial material = cyano.basemetals.init.Materials.getArmorMaterialFor(metal);
		if(material == null){
			// uh-oh
			FMLLog.severe("Failed to load armor material enum for "+metal);
		}
		return new ItemMetalArmor(metal,material,material.ordinal(),2);
	}

	
	public static ItemMetalArmor createBoots(MetalMaterial metal){
		ArmorMaterial material = cyano.basemetals.init.Materials.getArmorMaterialFor(metal);
		if(material == null){
			// uh-oh
			FMLLog.severe("Failed to load armor material enum for "+metal);
		}
		return new ItemMetalArmor(metal,material,material.ordinal(),3);
	}


	@Override
	public void onCreated(final ItemStack item, final World world, final EntityPlayer crafter) {
		super.onCreated(item, world, crafter);
		extraEffectsOnCrafting(item, world, crafter);
	}

	public void extraEffectsOnCrafting(final ItemStack item, final World world, final EntityPlayer crafter){
		//
	}
	
	/**
     * Return whether this item is repairable in an anvil.
     */
    @Override public boolean getIsRepairable(ItemStack srcItemStack, ItemStack repairMaterial)
    {
    	// repair with string or wool
    	List<ItemStack> acceptableItems = OreDictionary.getOres(repairOreDictName);
    	for(ItemStack i : acceptableItems ){
    		if(ItemStack.areItemsEqual(i, repairMaterial)) return true;
    	}
    	return false;
    }
	
    @SideOnly(Side.CLIENT)
	@Override public String getArmorTexture(ItemStack stack, Entity e, int slot, String layer){
    	return customTexture;
    }



}