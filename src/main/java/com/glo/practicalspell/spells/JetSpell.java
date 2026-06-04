package com.glo.practicalspell.spells;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class JetSpell implements Spell {

    // 可配置参数
    private static final double MAX_DISTANCE = 8.0;      // 锥体最大距离
    private static final double HALF_ANGLE_DEG = 30.0;   // 半顶角度数
    private static final double COS_HALF_ANGLE = Math.cos(Math.toRadians(HALF_ANGLE_DEG));
    private static final double KNOCKBACK_STRENGTH = 1.5; // 击退强度
    private static final double UPWARD_FACTOR = 0.4;      // 向上的速度分量

    @Override
    public String name() {
        return "jet";
    }

    @Override
    public void castServer(ServerPlayer player) {
        // 玩家眼睛位置和视线方向
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle(); // 已经是单位向量

        // 1. 构建一个粗略的 AABB 包围盒进行初筛
        //    以眼睛位置为中心，向视线方向延伸 MAX_DISTANCE，并向四周扩展 MAX_DISTANCE
        Vec3 center = eyePos.add(lookVec.scale(MAX_DISTANCE / 2.0));
        double halfSize = MAX_DISTANCE;
        AABB roughBox = new AABB(
            center.x - halfSize, center.y - halfSize, center.z - halfSize,
            center.x + halfSize, center.y + halfSize, center.z + halfSize
        );

        // 2. 遍历该区域内的所有实体
        for (Entity entity : player.level().getEntities(player, roughBox, 
                e -> e instanceof LivingEntity && e.isAlive())) {
            // 获取实体的碰撞箱中心
            Vec3 entityPos = entity.getBoundingBox().getCenter();
            Vec3 toEntity = entityPos.subtract(eyePos);

            double distance = toEntity.length();
            if (distance > MAX_DISTANCE) continue; // 距离过滤

            // 归一化方向向量
            Vec3 dirToEntity = toEntity.normalize();
            // 计算与视线方向的点积
            double dot = lookVec.dot(dirToEntity);
            if (dot < COS_HALF_ANGLE) continue; // 角度过滤

            // 3. 施加吹飞速度
            //    方向：水平方向为从玩家指向实体的单位向量，并加上向上的分量
            Vec3 knockbackDir = new Vec3(dirToEntity.x, 0, dirToEntity.z).normalize();
            Vec3 velocity = knockbackDir.scale(KNOCKBACK_STRENGTH)
                    .add(0, UPWARD_FACTOR, 0);
            entity.setDeltaMovement(entity.getDeltaMovement().add(velocity));
            entity.hurtMarked = true; // 标记为已受伤，客户端会同步运动
        }
    }
}