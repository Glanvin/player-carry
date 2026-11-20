package dev.glavin;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerCarry implements ModInitializer {
	public static final String MOD_ID = "player-carry";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		UseEntityCallback.EVENT.register(PlayerCarry::tryCarry);
		LOGGER.info("Player Carry initialized");
	}

	/**
	 * Sneak + right-click a targeted player with an empty hand to make them ride you.
	 *
	 * <p>The callback also runs on the logical client, but there the target is a
	 * client-side player rather than a {@link ServerPlayer}, so the type checks
	 * let vanilla send the interact packet to the server where the work happens.
	 *
	 * <p>Dismounting needs no custom code: in 26.3 {@code Player.rideTick()}
	 * already calls {@code stopRiding()} while the passenger holds Shift.
	 */
	private static InteractionResult tryCarry(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
		if (!(player instanceof ServerPlayer carrier) || !(entity instanceof ServerPlayer target)) {
			return InteractionResult.PASS;
		}

		if (carrier.isSpectator() || !carrier.isShiftKeyDown() || carrier == target) {
			return InteractionResult.PASS;
		}

		// Only an empty hand can grab someone; let vanilla handle item interactions.
		if (!carrier.getItemInHand(hand).isEmpty()) {
			return InteractionResult.PASS;
		}

		// One passenger per carrier, and never chain carries or steal a rider.
		if (carrier.isPassenger() || carrier.isVehicle() || target.isPassenger() || target.isVehicle()) {
			return InteractionResult.PASS;
		}

		// Force the mount (the target may be sneaking) and emit the normal mount event.
		if (!target.startRiding(carrier, true, true)) {
			return InteractionResult.PASS;
		}

		target.sendSystemMessage(Component.literal("You are being carried by " + carrier.getName().getString() + ". Press Shift to dismount."));
		carrier.sendSystemMessage(Component.literal("You are carrying " + target.getName().getString() + "."));
		return InteractionResult.SUCCESS;
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
