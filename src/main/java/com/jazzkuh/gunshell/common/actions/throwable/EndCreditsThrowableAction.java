package com.jazzkuh.gunshell.common.actions.throwable;

import com.jazzkuh.gunshell.api.objects.GunshellThrowable;
import com.jazzkuh.gunshell.common.actions.throwable.abstraction.AbstractThrowableAction;
import com.jazzkuh.gunshell.compatibility.CompatibilityLayer;
import com.jazzkuh.gunshell.compatibility.CompatibilityManager;
import com.jazzkuh.gunshell.compatibility.extensions.WorldGuardExtension;
import com.jazzkuh.gunshell.utils.PluginUtils;
import com.jazzkuh.gunshell.GunshellPlugin;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.flag.WrappedState;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class EndCreditsThrowableAction extends AbstractThrowableAction {
    public EndCreditsThrowableAction(GunshellThrowable throwable) {
        super(throwable);
    }

    @Override
    public void fireAction(Player player, Location location, ConfigurationSection configuration) {
        Set<LivingEntity> livingEntities = location.getWorld().getNearbyEntities(location, getThrowable().getRange(), getThrowable().getRange(), getThrowable().getRange())
                .stream().filter(entity -> {
                    if (entity instanceof ArmorStand || entity instanceof ItemFrame) return false;
                    return entity instanceof LivingEntity;
                }).map(entity -> (LivingEntity) entity).collect(Collectors.toSet());
        ArrayList<Block> blocks = this.getBlocksAroundCenter(location, getThrowable().getRange());

        for (LivingEntity livingEntity : livingEntities) {
            CompatibilityManager compatibilityManager = GunshellPlugin.getInstance().getCompatibilityManager();
            if (compatibilityManager.isExtensionEnabled(CompatibilityManager.Extension.WORLDGUARD)
                    && compatibilityManager.getWorldGuardExtension().isFlagState(player, livingEntity.getLocation(),
                    WorldGuardExtension.GunshellFlag.GUNSHELL_USE_WEAPONS, WrappedState.DENY)) return;

            if (livingEntity instanceof Player) {
                Player playerTarget = (Player) livingEntity;
                if (playerTarget.getGameMode() == GameMode.SPECTATOR
                        || playerTarget.getGameMode() == GameMode.CREATIVE) return;

                CompatibilityLayer compatibilityLayer = GunshellPlugin.getInstance().getCompatibilityLayer();
                compatibilityLayer.showEndCreditScene(playerTarget);
            }

            if (livingEntity.getLocation().getWorld() != null) {
                livingEntity.getLocation().getWorld().playEffect(livingEntity.getEyeLocation(),
                        Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
            }

            PluginUtils.getInstance().performRecoil(livingEntity, 0F, this.getThrowable().getKnockbackAmount());
        }
    }
}
