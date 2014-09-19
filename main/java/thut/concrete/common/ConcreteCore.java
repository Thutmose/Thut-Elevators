package thut.concrete.common;

import thut.api.CreativeTabThut;
import thut.concrete.common.handlers.BlockHandler;
import thut.concrete.common.handlers.ItemHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod( modid = "ThutConcrete", name="Thut's Concrete", version="1.0.0")
public class ConcreteCore 
{
	@SidedProxy(clientSide = "thut.concrete.client.ClientProxy", serverSide = "thut.concrete.common.CommonProxy")
	public static CommonProxy proxy;
	
	@Instance("ThutConcrete")
	public static ConcreteCore instance;
	
	public static CreativeTabThut tabThut = CreativeTabThut.tabThut;
	
    private static final String[] colourNames = { "White",
        "Orange", "Magenta", "Light Blue",
        "Yellow", "Light Green", "Pink",
        "Dark Grey", "Light Grey", "Cyan",
        "Purple", "Blue", "Brown", "Green",
        "Red", "Black" };
    
	@EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
		BlockHandler.registerBlocks();
		ItemHandler.registerItems();
    }
	
	@EventHandler
    public void load(FMLInitializationEvent evt)
    {
		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
		ItemHandler.registerRecipes();
		proxy.initClient();
    }
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent e)
	{
		
	}
	
	
}
