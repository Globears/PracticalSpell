package com.glo.practicalspell.spells;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TntSpell implements Spell {

    @Override
    public String name() {
        return "TNT Spell";
    }

    @Override
    public void castServer(ServerPlayer player) {
        Level level = player.level();
        Vec3 eyePos = player.getEyePosition();          // 玩家眼睛位置
        Vec3 lookVec = player.getLookAngle();           // 视线方向
        double distance = 5.0;                          // 前方距离
        Vec3 targetBase = eyePos.add(lookVec.scale(distance));

        // 在目标点周围随机生成 3 个点燃的 TNT
        for (int i = 0; i < 3; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0; // 水平偏移 [-1, 1]
            double offsetY = level.random.nextDouble() * 1.0;        // 垂直偏移 [0, 1]
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0; // 水平偏移 [-1, 1]

            double x = targetBase.x + offsetX;
            double y = targetBase.y + offsetY;
            double z = targetBase.z + offsetZ;

            // 创建点燃的 TNT，玩家为引爆源
            PrimedTnt tnt = new PrimedTnt(level, x, y, z, player);
            level.addFreshEntity(tnt);
        }
    }
}