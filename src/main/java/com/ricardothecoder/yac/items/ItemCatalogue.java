package com.ricardothecoder.yac.items;

import java.util.List;

import com.ricardothecoder.yac.References;
import com.ricardothecoder.yac.YetAnotherCore;
import com.ricardothecoder.yac.guis.GuiScreenCatalogue;
import com.ricardothecoder.yac.util.ColorUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCatalogue extends ItemWrittenBook
{
	protected boolean isCreative = false;
	
	public ItemCatalogue(String modid, String itemid, boolean creative)
	{
		super();
		
		setCreativeTab(CreativeTabs.MISC);
		setRegistryName(new ResourceLocation(modid, itemid));
		setUnlocalizedName(itemid);
		
		isCreative = creative;
	}
	
	public void addEntry(String entry, ItemStack stack)
	{
		if (stack == null)
			return;
		
		if (!stack.hasTagCompound())
		{
			NBTTagCompound newTag = new NBTTagCompound();
			stack.setTagCompound(newTag);
		}
		
		NBTTagCompound tag = stack.getTagCompound();
		
		int lastIndex = 0;
		if (tag.hasKey("totalEntries"))
			lastIndex = tag.getInteger("totalEntries");
		
		boolean exists = false;
		for (int i = 0; i < lastIndex; i++)
		{
			exists = tag.getString("entry" + i).equals(entry);
			if (exists) break;
		}
		
		if (!exists)
		{		
			tag.setString("entry" + lastIndex, entry);
			tag.setInteger("totalEntries", lastIndex + 1);
		}
		
		stack.setTagCompound(tag);
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack)
    {
       return ("" + I18n.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name")).trim();
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("owner"))
			tooltip.add(TextFormatting.DARK_AQUA + "Belongs To: " + TextFormatting.GRAY + stack.getTagCompound().getString("owner"));
		else
			tooltip.add(TextFormatting.GRAY + "No Owner Yet");
    }
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (!worldIn.isRemote)
        {
            this.resolveContents(itemStackIn, playerIn);
        }

        if (!isCreative)
        {
	        if (itemStackIn.hasTagCompound() && !itemStackIn.getTagCompound().hasKey("owner"))
	        {
	        	NBTTagCompound tag = itemStackIn.getTagCompound();
	        	tag.setString("owner", playerIn.getName());
	        	itemStackIn.setTagCompound(tag);
	        }
	        else if (!itemStackIn.hasTagCompound())
	        {
	        	NBTTagCompound newTag = new NBTTagCompound();
	        	newTag.setString("owner", playerIn.getName());
	        	itemStackIn.setTagCompound(newTag);
	        }
        }
     
        openCatalogue(itemStackIn, hand, playerIn);
        return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
    }
	
	private void openCatalogue(ItemStack stack, EnumHand hand, EntityPlayer player)
    {
		YetAnotherCore.proxy.displayCatalogue(stack);
    }
	
	private void resolveContents(ItemStack stack, EntityPlayer player)
    {
        if (stack != null && stack.getTagCompound() != null)
        {
            if (player instanceof EntityPlayerMP && player.getHeldItemMainhand() == stack)
            {
                Slot slot = player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
                ((EntityPlayerMP)player).connection.sendPacket(new SPacketSetSlot(0, slot.slotNumber, stack));
            }
        }
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack)
	{
		return isCreative;
	}
	
	@SideOnly(Side.CLIENT)
	public ResourceLocation getGUITexture()
	{
		return new ResourceLocation(References.MODID, "textures/gui/catalogue.png");
	}
	
	@SideOnly(Side.CLIENT)
	public String getTitle()
	{
		return "Sample Catalogue";
	}
	
	@SideOnly(Side.CLIENT)
	public int getTitleColor()
	{
		return ColorUtil.getRGBInteger(96, 192, 0);
	}
	
	@SideOnly(Side.CLIENT)
	public String getEntryBullet()
	{
		return "+ ";
	}
}
