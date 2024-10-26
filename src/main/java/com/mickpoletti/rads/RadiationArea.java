package com.mickpoletti.rads;

import cpw.mods.modlauncher.api.ITransformationService.Resource;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class RadiationArea {
    private final AABB boundingBox;
    private final float damageAmount;
    private final float DEFAULT_DAMAGE = 1;
    private int pX1, pX2, pY1, pY2, pZ1, pZ2;
    public static final ResourceKey<DamageType> RADS_DAMAGE =
                        ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(Rads.MODID, "rads"));

    public RadiationArea(int pX1, int pY1, int pZ1, int pX2, int pY2, int pZ2) {
        this.boundingBox = new AABB(pX1, pY1, pZ1, pX2, pY2, pZ2);
        this.pX1 = pX1;
        this.pX2 = pX2;
        this.pY1 = pY1;
        this.pY2 = pY2;
        this.pZ1 = pZ1;
        this.pZ2 = pZ2;
        this.damageAmount = DEFAULT_DAMAGE; 
    }

    public int pX1() {
        return this.pX1;
    }

    public int pX2() {
        return this.pX2;
    }

    public int pY1() {
        return this.pY1;
    }

    public int pY2() {
        return this.pY2;
    }

    public int pZ1() {
        return this.pZ1;
    }

    public int pZ2() {
        return this.pZ2;
    }


    // This constructor takes a defined boundingBox
    // and accepts a damage amount to override default
    public RadiationArea(AABB boundingBox, float damageAmount) {
        this.boundingBox = boundingBox;
        this.damageAmount = damageAmount;
        this.pX1 = (int)boundingBox.minX;
        this.pX2 = (int)boundingBox.maxX;
        this.pY1 = (int)boundingBox.minY;
        this.pY2 = (int)boundingBox.maxY;
        this.pZ1 = (int)boundingBox.minZ;
        this.pZ2 = (int)boundingBox.maxZ;
    }

    // Use this constructor to define a boundingBox
    // and use DEFAULT_DAMAGE value
    public RadiationArea(AABB boundingBox) {
        this.boundingBox = boundingBox;
        this.damageAmount = DEFAULT_DAMAGE;
        this.pX1 = (int)boundingBox.minX;
        this.pX2 = (int)boundingBox.maxX;
        this.pY1 = (int)boundingBox.minY;
        this.pY2 = (int)boundingBox.maxY;
        this.pZ1 = (int)boundingBox.minZ;
        this.pZ2 = (int)boundingBox.maxZ;
    }

    // Check if player intersects RadiationArea
    public boolean isInside(Player player) {
        return boundingBox.intersects(player.getBoundingBox());
    }

    public AABB getBoundingBox() {
        return boundingBox;
    }

    // Get the amount to damage the player
    public float getDamageAmount() {
        return damageAmount;
    }

    public DamageSource getDamageSource(Player player) {
        RegistryAccess registryAccess = player.level().registryAccess();
        
        return new DamageSource(
                registryAccess.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(RADS_DAMAGE),
                null,
                null,
                null);
    }

}
