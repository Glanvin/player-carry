package dev.glavin.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {
	@Shadow
	@Final
	private Entity entity;

	/**
	 * Vanilla only broadcasts passenger changes to players tracking the vehicle,
	 * never to the vehicle player themselves. That is fine for normal vehicles, but
	 * a player can now be the vehicle — so also send the packet to their own client.
	 * This covers both mounting and dismounting.
	 */
	@Redirect(
			method = "sendChanges()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerEntity$Synchronizer;sendToTrackingPlayers(Lnet/minecraft/network/protocol/Packet;)V",
					ordinal = 0
			)
	)
	private void sendPassengerChangesToVehicle(ServerEntity.Synchronizer synchronizer, Packet<ClientGamePacketListener> packet) {
		if (this.entity instanceof ServerPlayer) {
			synchronizer.sendToTrackingPlayersAndSelf(packet);
		} else {
			synchronizer.sendToTrackingPlayers(packet);
		}
	}
}
