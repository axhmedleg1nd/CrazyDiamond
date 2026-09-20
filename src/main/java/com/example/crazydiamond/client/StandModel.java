package com.example.crazydiamond.client;

import com.example.crazydiamond.Ability;
import com.example.crazydiamond.CrazyDiamondMod;
import com.example.crazydiamond.StandEntity;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/** A plain humanoid model (same proportions as a player). Arms animate per ability. */
public class StandModel extends EntityModel<StandEntity> {
	public static final EntityModelLayer LAYER =
			new EntityModelLayer(new Identifier(CrazyDiamondMod.MOD_ID, "crazy_diamond"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public StandModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.rightArm = root.getChild("right_arm");
		this.leftArm = root.getChild("left_arm");
		this.rightLeg = root.getChild("right_leg");
		this.leftLeg = root.getChild("left_leg");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData r = data.getRoot();

		r.addChild("head",
				ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f),
				ModelTransform.pivot(0.0f, 0.0f, 0.0f));
		r.addChild("body",
				ModelPartBuilder.create().uv(16, 16).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f),
				ModelTransform.pivot(0.0f, 0.0f, 0.0f));
		r.addChild("right_arm",
				ModelPartBuilder.create().uv(40, 16).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
				ModelTransform.pivot(-5.0f, 2.0f, 0.0f));
		r.addChild("left_arm",
				ModelPartBuilder.create().uv(40, 16).mirrored().cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
				ModelTransform.pivot(5.0f, 2.0f, 0.0f));
		r.addChild("right_leg",
				ModelPartBuilder.create().uv(0, 16).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f),
				ModelTransform.pivot(-1.9f, 12.0f, 0.0f));
		r.addChild("left_leg",
				ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f),
				ModelTransform.pivot(1.9f, 12.0f, 0.0f));

		return TexturedModelData.of(data, 64, 32);
	}

	@Override
	public void setAngles(StandEntity entity, float limbAngle, float limbDistance, float t, float headYaw, float headPitch) {
		head.pitch = 0.0f;
		head.yaw = 0.0f;
		rightArm.pitch = 0.0f;
		rightArm.yaw = 0.0f;
		rightArm.roll = 0.0f;
		leftArm.pitch = 0.0f;
		leftArm.yaw = 0.0f;
		leftArm.roll = 0.0f;

		float sway = MathHelper.sin(t * 0.1f);
		rightLeg.pitch = sway * 0.12f;
		leftLeg.pitch = -sway * 0.12f;

		Ability ability = entity.getClientAbility();
		if (ability == null) {
			// idle: arms hang and bob a little
			rightArm.pitch = sway * 0.08f;
			leftArm.pitch = -sway * 0.08f;
			rightArm.roll = 0.06f;
			leftArm.roll = -0.06f;
			return;
		}

		float forward = -MathHelper.HALF_PI; // arm pointing straight ahead
		switch (ability) {
			case PUNCH -> {
				float phase = (t % 8.0f) / 8.0f;
				rightArm.pitch = forward - 0.4f * MathHelper.sin(phase * MathHelper.PI);
				leftArm.pitch = -0.4f;
			}
			case BARRAGE -> {
				float swing = MathHelper.sin(t * 2.2f);
				rightArm.pitch = forward + swing * 0.7f;
				leftArm.pitch = forward - swing * 0.7f;
				head.pitch = 0.1f;
			}
			case RETURN_BLOCK -> {
				rightArm.pitch = forward - 0.9f;
				leftArm.pitch = forward - 0.9f;
				rightArm.roll = 0.3f + sway * 0.1f;
				leftArm.roll = -0.3f - sway * 0.1f;
			}
			case HEAL_MODE -> {
				rightArm.pitch = forward + 0.15f * sway;
				leftArm.pitch = forward - 0.15f * sway;
				rightArm.roll = 0.25f;
				leftArm.roll = -0.25f;
			}
			case STONE_SHOT -> {
				float phase = (t % 12.0f) / 12.0f;
				rightArm.pitch = forward - 0.6f + 0.8f * MathHelper.sin(phase * MathHelper.PI);
				leftArm.pitch = forward;
			}
			case DISASSEMBLE -> {
				rightArm.pitch = forward;
				leftArm.pitch = forward;
				rightArm.yaw = -0.4f + 0.2f * sway;
				leftArm.yaw = 0.4f - 0.2f * sway;
			}
			case REPAIR_ITEM -> {
				rightArm.pitch = forward + 0.2f * MathHelper.sin(t * 0.8f);
				leftArm.pitch = forward - 0.2f * MathHelper.sin(t * 0.8f);
				rightArm.yaw = -0.3f;
				leftArm.yaw = 0.3f;
			}
		}
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
			float red, float green, float blue, float alpha) {
		root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}
}
