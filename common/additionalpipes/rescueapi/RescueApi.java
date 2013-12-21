package additionalpipes.rescueapi;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import buildcraft.core.CreativeTabBuildCraft;

public class RescueApi {
	public static CreativeTabs getCreativeTabs(){
//		CreativeTabs[] tabArray = CreativeTabs.creativeTabArray;
//		CreativeTabs tab = null;
//		for(CreativeTabs t : tabArray){
//			if(t.getTabLabel().equals("buildcraft")){
//				tab = t;
//			}
//		}
//		return tab;
		return CreativeTabBuildCraft.MACHINES.get();
	}
	
	public static float getPipeNormalSpeed(){
		return 0.01F;
	}
	
	public static float getPipeFloorOf(){
		return 0.25F;
	}
	
	public static void markBlockForUpdate(TileEntity e){
		World worldObj = e.getWorldObj();
		worldObj.markBlockForUpdate(e.xCoord, e.yCoord, e.zCoord);
	}
}
