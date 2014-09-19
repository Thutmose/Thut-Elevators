package thut.tech.common.items;

import java.util.UUID;

import thut.api.ThutBlocks;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.tileentity.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class ItemLinker extends Item
{
	public static Item instance;
	
	public ItemLinker() 
	{
		super();
        this.setHasSubtypes(true);
		this.setUnlocalizedName("deviceLinker");
		this.setCreativeTab(TechCore.tabThut);
		instance = this;
	}
	
    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    public ItemStack onItemRightClick(ItemStack itemstack, World worldObj, EntityPlayer player)
    {
    	if(itemstack.stackTagCompound == null)
    	{
    		return itemstack;
    	}
        return itemstack;
    }
	
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World worldObj, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	boolean ret = false;

    	if(itemstack.stackTagCompound == null)
    	{
	       	return false;
    	}
       	else
       	{
	       	Block id = worldObj.getBlock(x, y, z);
	       	int meta = worldObj.getBlockMetadata(x, y, z);
	       	
			if(id==ThutBlocks.lift&&meta==1 && !player.isSneaking())
			{
				TileEntityLiftAccess te = (TileEntityLiftAccess)worldObj.getTileEntity(x, y, z);
				te.setSide(side, true);
				return true;
			}
	 	
	       	UUID liftID;
			try {
				liftID = UUID.fromString(itemstack.stackTagCompound.getString("lift"));
			} catch (Exception e) {
				return false;
			}
	       	
	       	
	       	EntityLift lift = EntityLift.getLiftFromUUID(liftID);
	       	
			if(player.isSneaking()&&lift!=null&&id==ThutBlocks.lift&&meta==1)
			{
				TileEntityLiftAccess te = (TileEntityLiftAccess)worldObj.getTileEntity(x, y, z);
				te.setLift(lift);
				int floor = te.getButtonFromClick(side, hitX, hitY, hitZ);
				te.setFloor(floor);
				if(worldObj.isRemote)
				player.addChatMessage(new ChatComponentText("Set this Floor to "+floor));
				return true;
			}
       	}
    	return false;
    }
    
    public void setLift(EntityLift lift, ItemStack stack)
    {
       	if(stack.stackTagCompound == null)
    	{
    		stack.setTagCompound(new NBTTagCompound() );
    	}
       	stack.stackTagCompound.setString("lift", lift.id.toString());
    }
    
    /**
     * If this function returns true (or the item is damageable), the ItemStack's NBT tag will be sent to the client.
     */
    public boolean getShareTag()
    {
        return true;
    }
    
	@SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon("thuttech:liftController");
    }
}
