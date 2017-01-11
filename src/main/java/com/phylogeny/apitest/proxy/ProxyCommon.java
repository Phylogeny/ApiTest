package com.phylogeny.apitest.proxy;

import com.phylogeny.apitest.ApiTest;
import com.phylogeny.apitest.ItemBitTool;
import com.phylogeny.apitest.PacketUseBitTool;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class ProxyCommon
{
	
	public void preInit(@SuppressWarnings("unused") FMLPreInitializationEvent event)
	{
		ApiTest.bitTool = new ItemBitTool("bit_tool"); 
		GameRegistry.register(ApiTest.bitTool);
		ApiTest.packetNetwork.registerMessage(PacketUseBitTool.Handler.class, PacketUseBitTool.class, 0, Side.SERVER);
	}
	
}