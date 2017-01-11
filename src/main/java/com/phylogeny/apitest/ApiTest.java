package com.phylogeny.apitest;

import com.phylogeny.apitest.proxy.ProxyCommon;

import mod.chiselsandbits.api.ChiselsAndBitsAddon;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.IChiselsAndBitsAddon;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@ChiselsAndBitsAddon
@Mod(modid = ApiTest.MOD_ID,
	 version = ApiTest.VERSION,
	 dependencies = "required-after:chiselsandbits" + "@[13.0,)")
public class ApiTest implements IChiselsAndBitsAddon
{
	public static final String MOD_ID = "apitest";
	public static final String MOD_NAME = "API Test";
	public static final String MOD_PATH = "com.phylogeny." + MOD_ID;
	public static final String VERSION = "@VERSION@";
	public static final double PIXEL = 1 / 16.0D;
	
	@SidedProxy(clientSide = MOD_PATH + ".proxy.ProxyClient", serverSide = MOD_PATH + ".proxy.ProxyCommon")
	public static ProxyCommon proxy;
	
	public static SimpleNetworkWrapper packetNetwork = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
	public static ItemBitTool bitTool;
	public static IChiselAndBitsAPI apiInstance;
	
	@EventHandler
	public void preinit(FMLPreInitializationEvent event)
	{
		proxy.preInit(event);
	}
	
	@Override
	public void onReadyChiselsAndBits(IChiselAndBitsAPI api)
	{
		apiInstance = api;
	}
	
}