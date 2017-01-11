package com.phylogeny.apitest;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketUseBitTool implements IMessage
{
	private Vec3d center;
	
	public PacketUseBitTool() {}
	
	public PacketUseBitTool(Vec3d center)
	{
		this.center = center;
	}
	
	@Override
	public void toBytes(ByteBuf buffer)
	{
		buffer.writeDouble(center.xCoord);
		buffer.writeDouble(center.yCoord);
		buffer.writeDouble(center.zCoord);
	}
	
	@Override
	public void fromBytes(ByteBuf buffer)
	{
		center = new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
	}
	
	public static class Handler implements IMessageHandler<PacketUseBitTool, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketUseBitTool message, final MessageContext ctx)
		{
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world;
			mainThread.addScheduledTask(new Runnable()
			{
				@Override
				public void run()
				{
					EntityPlayer player = ctx.getServerHandler().playerEntity;
					ItemBitTool.useTool(player.world, player, message.center);
				}
			});
			return null;
		}
		
	}
	
}