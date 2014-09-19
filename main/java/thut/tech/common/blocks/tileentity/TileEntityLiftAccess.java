package thut.tech.common.blocks.tileentity;


import static net.minecraftforge.common.util.ForgeDirection.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import javax.print.attribute.standard.SheetCollate;

//import appeng.api.WorldCoord;
//import appeng.api.events.GridTileLoadEvent;
//import appeng.api.events.GridTileUnloadEvent;
//import appeng.api.me.tiles.IDirectionalMETile;
//import appeng.api.me.tiles.IGridMachine;
//import appeng.api.me.tiles.IGridTileEntity;
//import appeng.api.me.util.IGridInterface;
//
//import dan200.computer.api.IComputerAccess;
//import dan200.computer.api.ILuaContext;
//import dan200.computer.api.IPeripheral;


//import universalelectricity.core.block.IElectricityStorage;












import cpw.mods.fml.common.FMLCommonHandler;
import scala.actors.threadpool.Arrays;
import thut.api.ThutBlocks;
import thut.api.maths.Vector3;
import thut.tech.common.entity.EntityLift;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Direction;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;

public class TileEntityLiftAccess extends TileEntity// implements IPeripheral//, IGridMachine, IDirectionalMETile
{
	
	public int power = 0;
	public int prevPower = 1;
	public EntityLift lift;
	
	boolean listNull = false;
	List<Entity> list = new ArrayList<Entity>();
	Vector3 here;
	
	public Vector3 root = new Vector3();
	public TileEntityLiftAccess rootNode;
	public Vector<TileEntityLiftAccess> connected = new Vector<TileEntityLiftAccess>();
	ForgeDirection sourceSide;
	public double energy;
	
	public long time = 0;
	public int metaData = 0;
	public Block blockID = Blocks.air;
	
	boolean loaded = false;
	
	public boolean called = false;
	public int floor = 0;
	public int calledYValue = -1;
	public int calledFloor = 0;
	public int currentFloor = 0;
	UUID liftID = null;
	UUID empty = new UUID(0,0);
	private byte[] sides = new byte[6];
	private byte[] sidePages = new byte[6];
	
	int tries = 0;
	
	public boolean toClear = false;
	
	public boolean first = true;
	public boolean read = false;
	public boolean redstone = true;
	public boolean powered = false;
	
	public void updateEntity()
	{
		if(first)
		{
			blockID = worldObj.getBlock(xCoord, yCoord, zCoord);
			metaData = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
			here = new Vector3(this);
			first = false;
		}
		
		if(blockID == ThutBlocks.liftRail && lift!=null)
		{
			boolean check = (int)lift.posY == yCoord;
			setCalled(check);
			time++;
			return;
		}
		
		if(lift!=null && floor>0)
		{
			int calledFloorOld = calledFloor;
			
			if((int)lift.posY == yCoord - 2)
			{
				lift.setCurrentFloor(floor);
			}
			if(lift.floors[floor-1]<0)
			{
				lift.setFoor(this, floor);
			}
			
			calledFloor = lift.getDestinationFloor();
			
			if(calledFloor == floor)
			{
				setCalled(true);
			}
			else
			{
				setCalled(false);
			}
			
			if(calledFloor!=calledFloorOld)
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			currentFloor = lift.getCurrentFloor();
			
		}
		if(liftID!=null && !liftID.equals(empty) &&lift==null)
		{
			lift = EntityLift.getLiftFromUUID(liftID);
			if(lift==null)
				liftID = empty;
		}
		if(getRoot().floor!=floor)
		{
			this.floor = getRoot().floor;
			this.lift = getRoot().lift;
			markDirty();
			updateBlock();
		}
		if(floor>0 && (lift==null || lift.isDead))
		{
			lift = null;
			floor = 0;
		}
		if(blockID == ThutBlocks.lift && lift==null)
		{
			for(ForgeDirection side: VALID_DIRECTIONS)
			{
				TileEntity t = here.getTileEntity(worldObj, side);
				Block b = here.getBlock(worldObj, side);
				if(b == blockID && t instanceof TileEntityLiftAccess)
				{
					TileEntityLiftAccess te = (TileEntityLiftAccess) t;
					if(te.lift!=null)
					{
						lift = te.lift;
						floor = te.floor;
						markDirty();
						updateBlock();
						break;
					}
				}
			}
		}
		time++;
	}
	
	public void checkPower()
	{

	}
	
	public void clearConnections()
	{
		
	}
	
	public double getEnergy()
	{
			return 0;
	}
	
	public void setEnergy(double energy)
	{
	}
	
	public String connectionInfo()
	{
		String ret = "";
		return ret;
	}
	
    /**
     * invalidates a tile entity
     */
    public void invalidate()
    {
        this.tileEntityInvalid = true;
        clearConnections();
    }
    
    /**
     * validates a tile entity
     */
    public void validate()
    {
        this.tileEntityInvalid = false;
    }
	
	public boolean checkSides()
	{
		List<Entity> check = worldObj.getEntitiesWithinAABB(EntityLift.class, AxisAlignedBB.getBoundingBox(xCoord+0.5-1, yCoord, zCoord+0.5-1, xCoord+0.5+1, yCoord+1, zCoord+0.5+1));
		if(check!=null&&check.size()>0)
		{
			lift = (EntityLift)check.get(0);
			liftID = lift.id;
		}
		return !(check == null || check.isEmpty());
	}
	
	public void setFloor(int floor)
	{
		if(lift!=null&&floor <=64 && floor > 0)
		{
			lift.setFoor(getRoot(), floor);
			getRoot().floor = floor;
			getRoot().markDirty();
			getRoot().updateBlock();
		}
	}
	
	public void setLift(EntityLift lift)
	{
		this.lift = lift;
	}
	
	public void writeToNBT(NBTTagCompound par1)
	   {
		   super.writeToNBT(par1);
		   par1.setInteger("meta", metaData);
		   par1.setString("block id", blockID.getLocalizedName());
		   par1.setInteger("floor", floor);
		   par1.setByteArray("sides", sides);
		   par1.setByteArray("sidePages", sidePages);
		   if(root!=null)
			   root.writeToNBT(par1, "root");
		   if(lift!=null)
		   {
			   liftID = lift.id;
		   }
		   if(liftID!=null)
		   {
			   par1.setLong("idLess", liftID.getLeastSignificantBits());
			   par1.setLong("idMost", liftID.getMostSignificantBits());
		   }
	   }
	
	   public void readFromNBT(NBTTagCompound par1)
	   {
	      super.readFromNBT(par1);
	      metaData = par1.getInteger("meta");
	      blockID = Block.getBlockFromName(par1.getString("block id"));
	      floor = par1.getInteger("floor");
	      liftID = new UUID(par1.getLong("idMost"), par1.getLong("idLess"));
	      root = new Vector3();
	      root = root.readFromNBT(par1, "root");
	      sides = par1.getByteArray("sides");
	      if(sides.length!=6)
	    	  sides = new byte[6];
	      sidePages = par1.getByteArray("sidePages");
	      if(sidePages.length!=6)
	    	  sidePages = new byte[6];
	      if(liftID!=null && worldObj!=null)
	      {
	    	  lift = EntityLift.getLiftFromUUID(liftID);
	      }
	   }

	   public void doButtonClick( int side, float hitX, float hitY, float hitZ)
	   {
		   if(liftID!=null && !liftID.equals(empty) && lift==null)
		   {
			   lift = EntityLift.getLiftFromUUID(liftID);
		   }
		   int button = getButtonFromClick(side, hitX, hitY, hitZ);

		   if(!worldObj.isRemote&&lift!=null)
		   {
			   if(isSideOn(side))
			   {
				   buttonPress(button);
				   calledFloor = lift.getDestinationFloor();
			   }
		   }
	   }
	   
	   public void callYValue(int yValue)
	   {
		   if(lift!=null)
		   {
			   lift.callYValue(yValue);
		   }
	   }
	   
	   public void buttonPress(int button)
	   {
		   if(button!=0&&button<=64&&lift!=null&&lift.floors[button-1]>0)
		   {
			   if(button==floor)
				   this.called = true;
			   lift.call(button);
		   }
	   }
	   
	   public void setCalled(boolean called)
	   {
		   if(called!=this.called)
		   {
			   this.called = called;
			   updateBlock();
			   for(ForgeDirection side : VALID_DIRECTIONS)
			   {
				   if(side!=UP && side!=DOWN)
				   {
					   updateBlock(side);
				   }
			   }
		   }
	   }
	   
	   public void setSide(int side, boolean flag)
	   {
		   	int state = 1;
		   	byte byte0 = sides[side];
		   	
		   	if(side<2)
		   		return;
		   
	        if (flag)
	        {
	            sides[side] = (byte) (byte0 | state);
	        }
	        else
	        {
	            sides[side] = (byte) (byte0 & -state-1);
	        }
	        markDirty();
	        setBlock(UNKNOWN, blockID, metaData);
	        updateBlock();
	   }
	   
	   public boolean isSideOn(int side)
	   {
		   	int state = 1;
		   	byte byte0 = sides[side];
		   	return (byte0  & state)!=0;
	   }

	   public int getSidePage(int side)
	   {
		   return sidePages[side];
	   }


	   public void setSidePage(int side, int page)
	   {
		   sidePages[side] = (byte) page;
	   }
	   
	   public int getButtonFromClick(int side, float hitX, float hitY, float hitZ)
	   {
		   int ret = 0;
		   int page = getSidePage(side);
           switch (side)
           {
	           case 0:
	           {
	        	   return 0 + 16*page;
	           }
	           case 1:
	           {
	        	   return 0 + 16*page;
	           }
	           case 2:
	           {
	        	   ret = 1+(int)(((1-hitX)*4)%4) + 4*(int)(((1-hitY)*4)%4);
	        	   return ret + 16*page;
	           }
	           case 3:
	           {	        	   
	        	   ret = 1+(int)(((hitX)*4)%4) + 4*(int)(((1-hitY)*4)%4);
	        	   return ret + 16*page;
	           }
	           case 4:
	           {
	        	   ret =1+4*(int)(((1-hitY)*4)%4) + (int)(((hitZ)*4)%4);
	        	   return ret + 16*page;
	           }
	           case 5:
	           {
	        	   ret = 1+4*(int)(((1-hitY)*4)%4) + (int)(((1-hitZ)*4)%4);
	        	   return ret + 16*page;
	           }
               default:
               {
            	   return 0 + 16*page;
               }
           
           }
		   
	   }
	   
	    /**
	     * Overriden in a sign to provide the text.
	     */
		@Override
	    public Packet getDescriptionPacket()
	    {
	        NBTTagCompound nbttagcompound = new NBTTagCompound();
	        this.writeToNBT(nbttagcompound);
	        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 3, nbttagcompound);
	    }
	    @Override
	    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
	    {
	    	NBTTagCompound nbttagcompound = pkt.func_148857_g();
	    	this.readFromNBT(nbttagcompound);
	    }

	    public Block thisBlock()
	    {
	    	if(worldObj!=null&&blockType==null)
	    	{
	    		blockType = worldObj.getBlock(xCoord, yCoord, zCoord);
	    	}
	    	return blockType;
	    }
	    
	    public int getBlockMetadata()
	    {
	    	if(worldObj!=null)
	    	return worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
	    	else
	    		return 0;
	    }

	    public Block getBlock(ForgeDirection side)
	    {
	    	return worldObj.getBlock(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ);
	    }
	    public int getBlockMetadata(ForgeDirection side)
	    {
	    	return worldObj.getBlockMetadata(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ);
	    }
	    public void updateBlock(ForgeDirection side)
	    {
	    	worldObj.notifyBlocksOfNeighborChange(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ,getBlock(side));
	    }
	    public void notifySurroundings()
	    {
	    	worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord,getBlockType(),0);
	    }
	    
	    public void updateBlock()
	    {
	    	worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, getBlockType(),5);
	    }
	    public TileEntity getBlockTE(ForgeDirection side)
	    {
	    	return worldObj.getTileEntity(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ);
	    }
	    public void setBlock(ForgeDirection side, Block id, int meta)
	    {
	    	worldObj.setBlock(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ, id, meta, 3);
	    }
	    
	    public void setRoot(TileEntityLiftAccess root)
	    {
	    	this.rootNode = root;
	    }
	    
	    public TileEntityLiftAccess getRoot()
	    {
			if(here==null||here.isEmpty())
			{
				here = new Vector3(this);
			}
			
			if(rootNode!=null)
				return rootNode;
			
			Block b = here.getBlock(worldObj, DOWN);
			if(b==blockID)
			{
				TileEntityLiftAccess te = (TileEntityLiftAccess) here.getTileEntity(worldObj, DOWN);
				if(te!=null)
				{
					return rootNode = te.getRoot();
				}
			}
			return rootNode = this;
	    }
}

