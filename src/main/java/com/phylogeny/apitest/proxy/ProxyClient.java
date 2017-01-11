package com.phylogeny.apitest.proxy;

import com.phylogeny.apitest.ApiTest;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ProxyClient extends ProxyCommon
{
	
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);
		ModelLoader.setCustomModelResourceLocation(ApiTest.bitTool, 0, new ModelResourceLocation(
				new ResourceLocation(ApiTest.MOD_ID, ApiTest.bitTool.name), "inventory"));
	}
	
}