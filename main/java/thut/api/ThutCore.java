package thut.api;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thut.api.blocks.fluids.dusts.BlockDust;
import thut.api.blocks.fluids.dusts.BlockDustInactive;
import thut.api.items.ItemDusts;
import thut.api.items.ItemDusts.Dust;
import thut.api.items.ItemGrinder;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;


@Mod( modid = ThutCore.MOD_ID, name="Thut API", version="1.0.0")
public class ThutCore 
{
	public static final String MOD_ID = "thutcore";
	public static final String TEXTURE_PATH = MOD_ID.toLowerCase() + ":";
	
	@SidedProxy(clientSide = "thut.api.ClientProxy", serverSide = "thut.api.CommProxy")
	public static CommProxy proxy;
	
	@Instance(MOD_ID)
	public static ThutCore instance;
	public static CreativeTabThut tabThut = CreativeTabThut.tabThut;
	
    
	@EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
		new BlockDust();
		new BlockDustInactive();
		Item dusts = new ItemDusts();
		addDusts();
		new ItemGrinder();
		GameRegistry.registerBlock(ThutBlocks.dust, "dustBlock");
		GameRegistry.registerBlock(ThutBlocks.inactiveDust, "inactiveDustBlock");
		GameRegistry.registerItem(ItemDusts.instance, "dustsItem");
		ThutItems.dust = new ItemStack(dusts);
		ThutItems.trass = new ItemStack(dusts, 1, 3);
    }
	
	  void addDusts() {
		    ItemDusts.addDust(new Dust("dust", ThutCore.MOD_ID) {
		      public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		        if(!world.isRemote && stack.getItemDamage() == 0) {
		          int x1 = ForgeDirection.getOrientation(side).offsetX + x, y1 = ForgeDirection.getOrientation(side).offsetY + y, z1 = ForgeDirection.getOrientation(side).offsetZ + z;
		          int meta = world.getBlockMetadata(x1, y1, z1);
		          Block block = world.getBlock(x1, y1, z1);

		          if(player.isSneaking() && ItemDye.applyBonemeal(stack, world, x, y, z, player)) {
		            if(!world.isRemote) {
		              world.playAuxSFX(2005, x, y, z, 0);
		            }

		            return true;
		          }

		          if(block instanceof BlockDust || block instanceof BlockDustInactive && meta != 15) {
		            world.setBlockMetadataWithNotify(x1, y1, z1, meta + 1, 3);
		            if(!player.capabilities.isCreativeMode) {
		              stack.splitStack(1);
		            }
		            return true;
		          } else if(world.getBlock(x1, y1, z1) instanceof BlockDust || world.getBlock(x1, y1, z1) instanceof BlockDustInactive && meta != 15) {
		            world.setBlockMetadataWithNotify(x1, y1, z1, meta + 1, 3);
		            if(!player.capabilities.isCreativeMode) {
		              stack.splitStack(1);
		            }
		            return true;
		          } else if(block == Blocks.air || block.getMaterial().isReplaceable()) {
		            world.setBlock(x1, y1, z1, ThutBlocks.dust, Math.min(15, stack.stackSize), 3);
		            if(!player.capabilities.isCreativeMode) {
		              stack.splitStack(Math.min(stack.stackSize, 16));
		            }
		            return true;
		          }
		        }
		        return false;
		      }
		    });
		    ItemDusts.addDust(new Dust("dustCaCO3", ThutCore.MOD_ID));
		    ItemDusts.addDust(new Dust("dustCaO", ThutCore.MOD_ID));
		    ItemDusts.addDust(new Dust("dustTrass", ThutCore.MOD_ID));
		    ItemDusts.addDust(new Dust("dustCement", ThutCore.MOD_ID));
		    ItemDusts.addDust(new Dust("dustSulfur", ThutCore.MOD_ID));

		  }

	
	@EventHandler
    public void load(FMLInitializationEvent evt)
    {
		
    }
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent e)
	{
		
	}
	
	
}
