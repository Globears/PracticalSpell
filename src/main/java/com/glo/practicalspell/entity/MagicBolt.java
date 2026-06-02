package com.glo.practicalspell.entity;

import com.glo.practicalspell.server.SpellTargets;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector3f;

public class MagicBolt extends ThrowableProjectile {

    private static final int MAX_LIFETIME = 100;

    public MagicBolt(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public MagicBolt(Level level, LivingEntity shooter, EntityType<? extends ThrowableProjectile> type) {
        super(type, shooter, level);
        setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            double dx = getX() - xo;
            double dy = getY() - yo;
            double dz = getZ() - zo;
            int steps = (int) Math.ceil(Math.sqrt(dx * dx + dy * dy + dz * dz) / 0.3);
            for (int i = 0; i <= steps; i++) {
                double t = steps == 0 ? 0 : (double) i / steps;
                level().addParticle(
                        new DustColorTransitionOptions(new Vector3f(1f, 0.2f, 0f), new Vector3f(1f, 0.8f, 0f), 1f),
                        xo + dx * t, yo + dy * t, zo + dz * t, 0, 0, 0
                );
            }
        }
        if (!level().isClientSide && tickCount > MAX_LIFETIME) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide
                && getOwner() instanceof ServerPlayer player
                && result.getEntity() instanceof LivingEntity target) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            target.hurt(player.damageSources().indirectMagic(this, player), 4.0f);
            SpellTargets.add(player, target);
        }
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        discard();
    }

    @Override
    protected double getDefaultGravity() {
        return 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }
}
