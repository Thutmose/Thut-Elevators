package thut.api.blocks.fluids.dusts;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import thut.api.ThutBlocks;
import thut.api.ThutCore;
import thut.api.blocks.BlockFluid;
import thut.api.maths.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.*;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;


public class BlockDust extends BlockFluid
{

	static Integer[][] data;
	private static int thisID;
	long time = 0;
	
	@SideOnly(Side.CLIENT)
	private IIcon iconFloatingDust;

    public BlockDust()
    {
    	super(getFluidType("dust"),Material.sand);
		setBlockName("dust");
		setHardness(0.1f);
		setResistance(0.0f);
		ThutBlocks.dust = this;
		this.setTickRandomly(true);
		this.hasFloatState = true;
    }
  
	public void setData(){
	}
    
	public int tickRate(World worldObj)
	{
		return 40;
	}
    
    ///////////////////////////////////////////////////////////////////Block Ticking Stuff Above Here///////////////////////////////////////
    
    @SideOnly(Side.CLIENT)

    /**
     * When this method is called, your block should register all the icons it needs with the given IconRegister. This
     * is the only chance you get to register icons.
     */
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon(ThutCore.TEXTURE_PATH + "dust");
        this.iconFloatingDust = par1IconRegister.registerIcon(ThutCore.TEXTURE_PATH + "dustCloud");
    }
    
    @Override
    @SideOnly(Side.CLIENT)

    /**
     * Retrieves the block texture to use based on the display side. Args: iBlockAccess, x, y, z, side
     */
    public IIcon getIcon(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
            Material material = par1IBlockAccess.getBlock(par2, par3 - 1, par4).getMaterial();
            Block id = par1IBlockAccess.getBlock(par2, par3 - 1, par4);
            int meta = par1IBlockAccess.getBlockMetadata(par2, par3 - 1, par4);

            return isFloating(par1IBlockAccess, par2, par3, par4)? this.iconFloatingDust : this.blockIcon;
            
    }
    
//    /**TODO drops
//     * Returns the ID of the items to drop on destruction.
//     */
//    public int idDropped(int par1, Random par2Random, int par3)
//    {
//        return Items.dust.itemID;
//    }
//    
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
    {
    	int meta = par1World.getBlockMetadata(par2, par3, par4);
        int l = 15-par1World.getBlockMetadata(par2, par3, par4);
        Block block = par1World.getBlock(par2, par3 - 1, par4);
        
        float f = 0.0625F;
        if(!(new Vector3(par2,par3-1,par4).isFluid(par1World)||
        		par1World.isAirBlock(par2, par3-1, par4)||(block instanceof BlockFluid&&meta!=0))){
        return AxisAlignedBB.getBoundingBox((double)par2 + this.minX, (double)par3 + this.minY, (double)par4 + this.minZ, (double)par2 + this.maxX, (double)((float)par3 + (float)l * f), (double)par4 + this.maxZ);
        }
        else{
        	return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
        }
    }
    
    /**
     * Adds all intersecting collision boxes to a list. (Be sure to only add boxes to the list if they intersect the
     * mask.) Parameters: World, X, Y, Z, mask, list, colliding entity
     */
	  @Override
    public void addCollisionBoxesToList(World worldObj, int x, int y, int z, AxisAlignedBB aaBB, List list, Entity par7Entity)
    {
		if(hasFloatState && isFloating(worldObj, x, y, z)) return;
		Vector3 vec1 = new Vector3(x,y-1,z);
		if(vec1.isFluid(worldObj)&&vec1.getBlock(worldObj)!=this) 
		{
			return;
		}
		for(AxisAlignedBB box : getBoxes(worldObj, x, y, z))
		{
			if(aaBB.intersectsWith(box))
				list.add(box);
		}
    }
    
	@Override
	public void updateTick(World worldObj, int x, int y, int z, Random r)
	{ 
    	this.setSolid();
		Vector3 here = new Vector3(x,y,z);
		super.doFluidTick(worldObj, here);
		if(worldObj.getBlock(x, y, z)==this)
		{
		int meta = worldObj.getBlockMetadata(x, y, z);

		Block idUp = worldObj.getBlock(x, y+1, z);
		int metaUp = worldObj.getBlockMetadata(x, y+1, z);
		if(meta==15)
		worldObj.scheduleBlockUpdate(x, y, z, idUp, 5);
			if(isFloating(worldObj, x, y, z))
			{
				here.setAir(worldObj);
			}
			if(worldObj.getBlock(x, y, z)==this)
			{
				worldObj.setBlock(x, y, z, ThutBlocks.inactiveDust, meta, 2);
			}
		}
		
    	if(this.isFloating(worldObj, x, y, z))
    	{
    		worldObj.setBlockToAir(x, y, z);
    		return;
    	}
    }

    ////////////////////////////////////////////Plant stuff////////////////////////////////////////////////////////////////
    

    /**
     * Determines if this block can support the passed in plant, allowing it to be planted and grow.
     * Some examples:
     *   Reeds check if its a reed, or if its sand/dirt/grass and adjacent to water
     *   Cacti checks if its a cacti, or if its sand
     *   Nether types check for soul sand
     *   Crops check for tilled soil
     *   Caves check if it's a colid surface
     *   Plains check if its grass or dirt
     *   Water check if its still water
     *
     * @param world The current world
     * @param x X Position
     * @param y Y Position
     * @param z Z position
     * @param direction The direction relative to the given position the plant wants to be, typically its UP
     * @param plant The plant that wants to check
     * @return True to allow the plant to be planted/stay.
     */
    public boolean canSustainPlant(World world, int x, int y, int z, ForgeDirection direction, IPlantable plant)
    {
       return world.getBlockMetadata(x, y, z)==15;
    }

    /**
     * Checks if this soil is fertile, typically this means that growth rates
     * of plants on this soil will be slightly sped up.
     * Only vanilla case is tilledField when it is within range of water.
     *
     * @param world The current world
     * @param x X Position
     * @param y Y Position
     * @param z Z position
     * @return True if the soil should be considered fertile.
     */
    public boolean isFertile(World world, int x, int y, int z)
    {
    	 return world.getBlockMetadata(x, y, z)==15;
    }
    
}