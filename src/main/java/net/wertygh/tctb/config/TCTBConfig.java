package net.wertygh.tctb.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class TCTBConfig {
    public static final ForgeConfigSpec SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        SPEC = pair.getRight();
    }

    public enum SpeedCombineMode {
        MULTIPLICATIVE,
        ADDITIVE
    }

    public static class Common {
        public final ForgeConfigSpec.EnumValue<SpeedCombineMode> attackSpeedCombineMode;
        public final ForgeConfigSpec.DoubleValue tetraAttackSpeedBase;
        public final ForgeConfigSpec.DoubleValue tconAttackSpeedBase;
        public final ForgeConfigSpec.BooleanValue mergeDoubleHeadSlotsForSpeed;
        public final ForgeConfigSpec.DoubleValue attackSpeedStackPenalty;
        public final ForgeConfigSpec.DoubleValue minAttackSpeed;
        public final ForgeConfigSpec.DoubleValue damageStackBonus;
        public final ForgeConfigSpec.BooleanValue damageMultiplierAffectsTconBase;
        public final ForgeConfigSpec.DoubleValue durabilityStackBonus;
        public final ForgeConfigSpec.DoubleValue durabilityScale;
        public final ForgeConfigSpec.DoubleValue durabilityMaxIncreaseRatio;
        public final ForgeConfigSpec.DoubleValue miningEfficiencyScale;
        public final ForgeConfigSpec.DoubleValue miningBridgeThreshold;
        public final ForgeConfigSpec.DoubleValue minMiningSpeed;
        public final ForgeConfigSpec.DoubleValue miningSpeedCombineDivisor;
        public final ForgeConfigSpec.DoubleValue extraAttributeStackBonus;
        public final ForgeConfigSpec.DoubleValue limbStack;
        public final ForgeConfigSpec.DoubleValue drawSpeedMin;
        public final ForgeConfigSpec.DoubleValue projectileBaseDamage;
        public final ForgeConfigSpec.DoubleValue projectileStrengthDivisor;
        public final ForgeConfigSpec.DoubleValue velocityPercentDivisor;
        public final ForgeConfigSpec.BooleanValue bowProjectileOverridesTcon;
        public final ForgeConfigSpec.DoubleValue bowDrawTickBase;
        public final ForgeConfigSpec.DoubleValue bowMinDrawTick;
        public final ForgeConfigSpec.DoubleValue bowMinVelocityFactor;
        public final ForgeConfigSpec.DoubleValue bowInaccuracyTconScale;
        public final ForgeConfigSpec.DoubleValue bowMinAccuracy;
        public final ForgeConfigSpec.DoubleValue bowTetraAccuracyBase;
        public final ForgeConfigSpec.DoubleValue bowInaccuracyOffset;
        public final ForgeConfigSpec.DoubleValue bowMinArrowVelocity;
        public final ForgeConfigSpec.DoubleValue bowArrowBaseDamageOffset;
        public final ForgeConfigSpec.DoubleValue bowArrowVelocityScale;
        public final ForgeConfigSpec.DoubleValue bowStrengthVelocityBase;
        public final ForgeConfigSpec.DoubleValue bowStrengthVelocityDivisor;
        public final ForgeConfigSpec.BooleanValue bowStrengthAffectsVelocity;
        public final ForgeConfigSpec.DoubleValue bowSuspendVelocityMultiplier;
        public final ForgeConfigSpec.IntValue bowFullChargeTicks;
        public final ForgeConfigSpec.IntValue bowOverbowedLimitMultiplier;
        public final ForgeConfigSpec.IntValue bowOverbowedCooldownTicks;
        public final ForgeConfigSpec.IntValue bowMultishotMax;
        public final ForgeConfigSpec.IntValue bowUseDuration;
        public final ForgeConfigSpec.DoubleValue attackDamageAttributeBase;
        public final ForgeConfigSpec.IntValue integrityMaxTier;
        public final ForgeConfigSpec.DoubleValue integrityDurabilityFactor;
        public final ForgeConfigSpec.DoubleValue integrityMiningFactor;
        public final ForgeConfigSpec.DoubleValue integrityAttackFactor;
        public final ForgeConfigSpec.DoubleValue integrityHandleFactor;
        public final ForgeConfigSpec.IntValue honeTierMultiplier;
        public final ForgeConfigSpec.IntValue honeMinBase;
        public final ForgeConfigSpec.IntValue honeIntegrityMultiplier;

        Common(ForgeConfigSpec.Builder b) {
            b.push("melee");
            b.comment(
                    "攻速合成模式：MULTIPLICATIVE = (B*M) * Tetra相对攻速；ADDITIVE = (B*M) + Tetra相对差值",
                    "Attack speed combine mode: MULTIPLICATIVE = (B*M) * Tetra relative speed; ADDITIVE = (B*M) + Tetra relative delta"
            );
            attackSpeedCombineMode = b.defineEnum("attackSpeedCombineMode", SpeedCombineMode.MULTIPLICATIVE);

            b.comment(
                    "Tetra 攻速属性基准（玩家基础 4.0）",
                    "Tetra attack speed attribute base (player base is 4.0)"
            );
            tetraAttackSpeedBase = b.defineInRange("tetraAttackSpeedBase", 4.0, 0.001, 1000.0);

            b.comment(
                    "匠魂 ATTACK_SPEED stat 基准（1.0 = 正常）",
                    "Tinkers' Construct ATTACK_SPEED stat base (1.0 = normal)"
            );
            tconAttackSpeedBase = b.defineInRange("tconAttackSpeedBase", 1.0, 0.001, 1000.0);

            b.comment(
                    "double/head_left 和 double/head_right 合并为同一个攻速组，双头吃速度惩罚",
                    "Merge double/head_left and double/head_right into one speed group so double heads receive the speed penalty"
            );
            mergeDoubleHeadSlotsForSpeed = b.define("mergeDoubleHeadSlotsForSpeed", true);

            b.comment(
                    "同组每多一个攻速模块的惩罚：1 - penalty*(n-1)。0.1 = 双头 ×0.9",
                    "Penalty per additional attack speed module in the same group: 1 - penalty*(n-1). 0.1 = double head x0.9"
            );
            attackSpeedStackPenalty = b.defineInRange("attackSpeedStackPenalty", 0.1, 0.0, 1.0);

            b.comment(
                    "攻速下限",
                    "Minimum attack speed"
            );
            minAttackSpeed = b.defineInRange("minAttackSpeed", 0.1, 0.001, 1000.0);

            b.comment(
                    "同组每多一个模块的伤害堆叠奖励：1 + bonus*(n-1)",
                    "Damage stack bonus per additional module in the same group: 1 + bonus*(n-1)"
            );
            damageStackBonus = b.defineInRange("damageStackBonus", 0.1, -1.0, 100.0);

            b.comment(
                    "Tetra 的 MULTIPLY_TOTAL 是否也乘匠魂基础伤害。false = 只乘 Tetra 加值",
                    "Whether Tetra MULTIPLY_TOTAL also multiplies the Tinkers' base damage. false = only multiply the Tetra bonus"
            );
            damageMultiplierAffectsTconBase = b.define("damageMultiplierAffectsTconBase", false);

            b.comment(
                    "同组每多一个模块的耐久堆叠奖励",
                    "Durability stack bonus per additional module in the same group"
            );
            durabilityStackBonus = b.defineInRange("durabilityStackBonus", 0.1, -1.0, 100.0);

            b.comment(
                    "Tetra 部件耐久进入匠魂的比例",
                    "Ratio of Tetra part durability transferred to Tinkers' durability"
            );
            durabilityScale = b.defineInRange("durabilityScale", 0.3, 0.0, 100.0);

            b.comment(
                    "Tetra 耐久最多增加匠魂基础耐久的比例",
                    "Maximum ratio by which Tetra durability may increase the Tinkers' base durability"
            );
            durabilityMaxIncreaseRatio = b.defineInRange("durabilityMaxIncreaseRatio", 0.8, 0.0, 100.0);

            b.comment(
                    "桥接挖掘速度下限",
                    "Minimum bridged mining speed"
            );
            minMiningSpeed = b.defineInRange("minMiningSpeed", 1.0, 0.0, 100.0);

            b.comment(
                    "Tetra 工具效率对挖掘速度的缩放",
                    "Scaling applied to Tetra tool efficiency when converting to mining speed"
            );
            miningEfficiencyScale = b.defineInRange("miningEfficiencyScale", 1.0, 0.0, 100.0);

            b.comment(
                    "挖掘速度合成除数：最终 = (匠魂原生最终速度 + Tetra 模块贡献) / 此值",
                    "Mining speed combine divisor: final = (Tinkers' native final speed + Tetra module contribution) / this value"
            );
            miningSpeedCombineDivisor = b.defineInRange("miningSpeedCombineDivisor", 1.5, 1.0, 100.0);

            b.comment(
                    "TConMiningBridge 的 original 阈值：original > 此值时不覆盖",
                    "TConMiningBridge original threshold: do not override when original > this value"
            );
            miningBridgeThreshold = b.defineInRange("miningBridgeThreshold", 1.0, 0.0, 100.0);

            b.comment(
                    "同组额外属性（攻击距离等）的堆叠奖励；0 = 纯平均",
                    "Stack bonus for extra attributes such as attack reach in the same group; 0 = pure average"
            );
            extraAttributeStackBonus = b.defineInRange("extraAttributeStackBonus", 0.0, -1.0, 100.0);
            b.pop();

            b.push("bow");
            b.comment(
                    "同组每多一个弓臂的堆叠：0.5 = 两臂 ×1.5；1.0 = ×2；0 = 平均",
                    "Stacking per additional bow limb in the same group: 0.5 = two limbs x1.5; 1.0 = x2; 0 = average"
            );
            limbStack = b.defineInRange("limbStack", 0.5, 0.0, 100.0);

            b.comment(
                    "拉弓速度因子下限",
                    "Minimum draw speed factor"
            );
            drawSpeedMin = b.defineInRange("drawSpeedMin", 0.1, 0.001, 1000.0);

            b.comment(
                    "箭的基础伤害",
                    "Base arrow damage"
            );
            projectileBaseDamage = b.defineInRange("projectileBaseDamage", 2.0, 0.0, 1000.0);

            b.comment(
                    "drawStrength 转弹伤的除数",
                    "Divisor used to convert drawStrength into projectile damage"
            );
            projectileStrengthDivisor = b.defineInRange("projectileStrengthDivisor", 3.0, 0.001, 1000.0);

            b.comment(
                    "velocity 效果转百分比的除数",
                    "Divisor used to convert the velocity effect into a percentage"
            );
            velocityPercentDivisor = b.defineInRange("velocityPercentDivisor", 100.0, 0.001, 10000.0);

            b.comment(
                    "true = Tetra drawStrength 存在时用 2 + s/3 覆盖匠魂弹伤；false = 在匠魂当前弹伤上加 s/3",
                    "true = when Tetra drawStrength exists, override Tinkers' projectile damage with 2 + s/3; false = add s/3 to current Tinkers' projectile damage"
            );
            bowProjectileOverridesTcon = b.define("bowProjectileOverridesTcon", true);

            b.comment(
                    "正常拉满所需 tick 数",
                    "Ticks required for a normal full draw"
            );
            bowDrawTickBase = b.defineInRange("bowDrawTickBase", 20.0, 1.0, 720000.0);

            b.comment(
                    "拉弓时间下限",
                    "Minimum draw time"
            );
            bowMinDrawTick = b.defineInRange("bowMinDrawTick", 1.0, 0.1, 720000.0);

            b.comment(
                    "TConBowFusion 的 velocityFactor 下限",
                    "Minimum velocityFactor for TConBowFusion"
            );
            bowMinVelocityFactor = b.defineInRange("bowMinVelocityFactor", 0.1, 0.0, 1000.0);

            b.comment(
                    "TCon 精度的不精度换算系数：scale * (1/acc - 1)",
                    "Tinkers' accuracy to inaccuracy scale: scale * (1/acc - 1)"
            );
            bowInaccuracyTconScale = b.defineInRange("bowInaccuracyTconScale", 3.0, 0.0, 10000.0);

            b.comment(
                    "TCon 精度最小值",
                    "Minimum Tinkers' accuracy"
            );
            bowMinAccuracy = b.defineInRange("bowMinAccuracy", 0.1, 0.001, 1.0);

            b.comment(
                    "Tetra spread/focus 的满分基准",
                    "Full-score baseline for Tetra spread/focus"
            );
            bowTetraAccuracyBase = b.defineInRange("bowTetraAccuracyBase", 100.0, 0.0, 10000.0);

            b.comment(
                    "Tetra 精度项减去的常数",
                    "Constant subtracted from the Tetra accuracy term"
            );
            bowInaccuracyOffset = b.defineInRange("bowInaccuracyOffset", 1.0, -10000.0, 10000.0);

            b.comment(
                    "箭速下限",
                    "Minimum arrow velocity"
            );
            bowMinArrowVelocity = b.defineInRange("bowMinArrowVelocity", 0.1, 0.0, 1000.0);

            b.comment(
                    "箭基础伤害减去的原版值",
                    "Vanilla value subtracted from base arrow damage"
            );
            bowArrowBaseDamageOffset = b.defineInRange("bowArrowBaseDamageOffset", 2.0, 0.0, 1000.0);

            b.comment(
                    "shootFromRotation 的箭速倍率",
                    "Arrow velocity multiplier passed to shootFromRotation"
            );
            bowArrowVelocityScale = b.defineInRange("bowArrowVelocityScale", 3.0, 0.0, 1000.0);

            b.comment(
                    "力量→箭速的基准力量值",
                    "Baseline strength value for strength-to-velocity conversion"
            );
            bowStrengthVelocityBase = b.defineInRange("bowStrengthVelocityBase", 6.0, 0.0, 10000.0);

            b.comment(
                    "力量→箭速的除数",
                    "Divisor for strength-to-velocity conversion"
            );
            bowStrengthVelocityDivisor = b.defineInRange("bowStrengthVelocityDivisor", 8.0, 0.001, 10000.0);

            b.comment(
                    "是否启用力量→箭速",
                    "Whether strength affects arrow velocity"
            );
            bowStrengthAffectsVelocity = b.define("bowStrengthAffectsVelocity", true);

            b.comment(
                    "suspend 满蓄力时的箭速倍率",
                    "Arrow velocity multiplier at full charge while using suspend"
            );
            bowSuspendVelocityMultiplier = b.defineInRange("bowSuspendVelocityMultiplier", 2.0, 0.0, 1000.0);

            b.comment(
                    "满蓄力 tick 数",
                    "Full charge ticks"
            );
            bowFullChargeTicks = b.defineInRange("bowFullChargeTicks", 20, 1, 720000);

            b.comment(
                    "overbowed 的额外时间倍率",
                    "Extra time multiplier for overbowed"
            );
            bowOverbowedLimitMultiplier = b.defineInRange("bowOverbowedLimitMultiplier", 2, 0, 1000);

            b.comment(
                    "overbowed 冷却 tick",
                    "Overbowed cooldown in ticks"
            );
            bowOverbowedCooldownTicks = b.defineInRange("bowOverbowedCooldownTicks", 10, 0, 720000);

            b.comment(
                    "multishot 上限",
                    "Maximum multishot count"
            );
            bowMultishotMax = b.defineInRange("bowMultishotMax", 64, 1, 10000);

            b.comment(
                    "弓的使用时长",
                    "Bow use duration"
            );
            bowUseDuration = b.defineInRange("bowUseDuration", 72000, 1, 7200000);
            b.pop();

            b.push("tetra_view");
            b.comment(
                    "TCon ATTACK_DAMAGE stat 含的玩家基础伤害",
                    "Player base damage included in the Tinkers' ATTACK_DAMAGE stat"
            );
            attackDamageAttributeBase = b.defineInRange("attackDamageAttributeBase", 1.0, 0.0, 1000.0);
            b.pop();

            b.push("integrity");
            b.comment(
                    "计入完整度的材料最高等级上限",
                    "Maximum material tier counted toward integrity"
            );
            integrityMaxTier = b.defineInRange("integrityMaxTier", 4, 0, 1000);

            b.comment(
                    "头部材料耐久对完整度基础值的系数",
                    "Factor applied to head material durability when calculating base integrity"
            );
            integrityDurabilityFactor = b.defineInRange("integrityDurabilityFactor", 0.001, 0.0, 1000.0);

            b.comment(
                    "头部材料挖掘速度对完整度基础值的系数",
                    "Factor applied to head material mining speed when calculating base integrity"
            );
            integrityMiningFactor = b.defineInRange("integrityMiningFactor", 1.0, 0.0, 1000.0);

            b.comment(
                    "头部材料攻击伤害对完整度基础值的系数",
                    "Factor applied to head material attack damage when calculating base integrity"
            );
            integrityAttackFactor = b.defineInRange("integrityAttackFactor", 1.0, 0.0, 1000.0);

            b.comment(
                    "手柄材料属性对完整度基础值的系数",
                    "Factor applied to handle material stats when calculating base integrity"
            );
            integrityHandleFactor = b.defineInRange("integrityHandleFactor", 1.0, 0.0, 1000.0);
            b.pop();

            b.push("hone");
            b.comment(
                    "采集等级换算打磨基础值的倍率",
                    "Multiplier used to convert harvest tier into hone base value"
            );
            honeTierMultiplier = b.defineInRange("honeTierMultiplier", 100, 1, 1000000);

            b.comment(
                    "打磨基础值下限",
                    "Minimum hone base value"
            );
            honeMinBase = b.defineInRange("honeMinBase", 50, 1, 1000000);

            b.comment(
                    "完整度成本换算打磨上限的倍率",
                    "Multiplier used to convert integrity cost into the hone limit"
            );
            honeIntegrityMultiplier = b.defineInRange("honeIntegrityMultiplier", 1, 1, 10000);
            b.pop();
        }
    }
}