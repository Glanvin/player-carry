package dev.glavin.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {
	/**
	 * Players are registered with {@code noSave()}, so vanilla normally rejects them
	 * as a vehicle on the logical server. Allow that one case so a player can carry
	 * another player; every other entity type keeps the original behaviour.
	 */
	@Redirect(
			method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z")
	)
	private boolean allowPlayerAsVehicle(EntityType<?> type) {
		return type == EntityTypes.PLAYER || type.canSerialize();
	}
}
