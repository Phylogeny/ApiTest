package com.phylogeny.apitest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IBitLocation;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.client.ModCreativeTab;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.registry.ModRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Stopwatch;

public class ItemBitTool extends Item
{
	public final String name;
	private static Stopwatch timer;
	
	public ItemBitTool(String name)
	{
		this.name = name;
		setRegistryName(ApiTest.MOD_ID, name);
		setUnlocalizedName(getRegistryName().toString());
		maxStackSize = 1;
		ModCreativeTab tab = ReflectionHelper.getPrivateValue(ModRegistry.class, new ModRegistry(), "creativeTab");
		setCreativeTab(tab);
	}
	
	private Vec3d getBitClicked(BlockPos pos, IBitLocation bitLoc)
	{
		return new Vec3d(bitLoc.getBitX() * ApiTest.PIXEL + pos.getX(),
				bitLoc.getBitY() * ApiTest.PIXEL + pos.getY(),
				bitLoc.getBitZ() * ApiTest.PIXEL + pos.getZ());
	}
	
	/**
	 * Add/remove sphere of bits on left click.
	 */
	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player)
	{
		if ( ApiTest.apiInstance.canBeChiseled(player.getEntityWorld(), pos) )
		{
			if ( itemstack != null && ( timer == null || timer.elapsed( TimeUnit.MILLISECONDS ) > 150 ) )
			{
				timer = Stopwatch.createStarted();
				if ( !player.worldObj.isRemote )
				{
					return true;
				}

				final Pair<Vec3d, Vec3d> PlayerRay = ModUtil.getPlayerRay( player );
				final Vec3d ray_from = PlayerRay.getLeft();
				final Vec3d ray_to = PlayerRay.getRight();

				@SuppressWarnings("deprecation")
				final RayTraceResult mop = player.worldObj.getBlockState( pos ).getBlock().collisionRayTrace( player.worldObj.getBlockState( pos ), player.worldObj, pos, ray_from, ray_to );
				if ( mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK )
				{
					IBitLocation bitLoc = ApiTest.apiInstance.getBitPos((float) mop.hitVec.xCoord - pos.getX(),
							(float) mop.hitVec.yCoord - pos.getY(), (float) mop.hitVec.zCoord - pos.getZ(), mop.sideHit, pos, false);
					final Vec3d center = getBitClicked(pos, bitLoc);
					useTool(player.getEntityWorld(), player, center);
					ApiTest.packetNetwork.sendToServer(new PacketUseBitTool(center));
				}
			}

			return true;
		}

		if ( player.getEntityWorld() != null && player.getEntityWorld().isRemote )
		{
			return true;
		}

		return false;
	}
	
	/**
	 * Add/remove sphere of bits on right click.
	 */
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
		{
			IChiselAndBitsAPI api = ApiTest.apiInstance;
			IBitLocation bitLoc = api.getBitPos(hitX, hitY, hitZ, side, pos, false);
			Vec3d center = getBitClicked(pos, bitLoc);
			useTool(world, player, center);
			ApiTest.packetNetwork.sendToServer(new PacketUseBitTool(center));
		}
		return EnumActionResult.SUCCESS;
	}
	
	public static EnumActionResult useTool(World world, EntityPlayer player, Vec3d center)
	{
		IChiselAndBitsAPI api = ApiTest.apiInstance;
		IBitBrush bit;
		try
		{
			bit = api.createBrushFromState(player.isSneaking() ? Blocks.COBBLESTONE.getDefaultState() : null);
		}
		catch (InvalidBitItem e)
		{
			return EnumActionResult.FAIL;
		}
		try
		{
			api.beginUndoGroup(player);
			createSphere(api, bit, world, center);
		}
		finally
		{
			api.endUndoGroup(player);
		}
		return EnumActionResult.SUCCESS;
	}
	
	private static void createSphere(IChiselAndBitsAPI api, IBitBrush bit, World world, Vec3d center)
	{
		BlockPos pos = new BlockPos(center);
		IBitAccess bitAccess = null;
		int x, y, z;
		for (x = pos.getX() - 2; x < pos.getX() + 2; x++)
		{
			for (y = pos.getY() - 2; y < pos.getY() + 2; y++)
			{
				for (z = pos.getZ() - 2; z < pos.getZ() + 2; z++)
				{
					affectBlock(api, bitAccess, bit, world, center, new BlockPos(x, y, z));
				}
			}
		}
		
	}
	
	private static void affectBlock(IChiselAndBitsAPI api, IBitAccess bitAccess, IBitBrush bit, World world, Vec3d center, BlockPos pos)
	{
		try
		{
			bitAccess = api.getBitAccess(world, pos);
		}
		catch (CannotBeChiseled e)
		{
			return;
		}
		int x, y, z;
		boolean changed = false;
		for (x = 0; x < 16; x++)
		{
			for (y = 0; y < 16; y++)
			{
				for (z = 0; z < 16; z++)
				{
					if (center.distanceTo(new Vec3d(x * ApiTest.PIXEL + pos.getX(), y * ApiTest.PIXEL + pos.getY(), z * ApiTest.PIXEL + pos.getZ())) < 0.75)
					{
						try
						{
							bitAccess.setBitAt(x, y, z, bit);
							changed = true;
						}
						catch (SpaceOccupied e) {}
					}
				}
			}
		}
		if (changed)
			bitAccess.commitChanges(true);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
	{
		tooltip.add("Left/right click to remove a sphere of bits.");
		tooltip.add("Shift left/right click to add a sphere of bits.");
	}
	
}