package com.mcmoddev.lib.block;

import com.mcmoddev.lib.material.IMMDObject;
import com.mcmoddev.lib.material.MMDMaterial;

import net.minecraft.block.state.IBlockState;

public class BlockMMDBookshelf extends net.minecraft.block.BlockBookshelf implements IMMDObject {

	final MMDMaterial material;
	private boolean fullBlock = true;

	public BlockMMDBookshelf(MMDMaterial material) {
		this.material = material;
		this.setSoundType(this.material.getSoundType());
		this.blockHardness = this.material.getBlockHardness();
		this.blockResistance = this.material.getBlastResistance();
		this.setHarvestLevel("axe", this.material.getRequiredHarvestLevel());
	}

	public void setFullBlock(boolean val) {
		this.fullBlock = val;
	}

	@Override
	public MMDMaterial getMMDMaterial() {
		return this.material;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return this.fullBlock;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return this.fullBlock;
	}

}
