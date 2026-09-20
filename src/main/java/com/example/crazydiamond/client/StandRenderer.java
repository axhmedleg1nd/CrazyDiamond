package com.example.crazydiamond.client;

import com.example.crazydiamond.CrazyDiamondMod;
import com.example.crazydiamond.StandEntity;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class StandRenderer extends EntityRenderer<StandEntity> {
	private static final Identifier TEXTURE =
			new Identifier(CrazyDiamondMod.MOD_ID, "textures/entity/crazy_diamond.png");

	private final StandModel model;

	public StandRenderer(EntityRendererFactory.Context context) {
		super(context);
		this.model = new StandModel(context.getPart(StandModel.LAYER));
		this.shadowRadius = 0.3f;
	}

	@Override
	public Identifier getTexture(StandEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(StandEntity entity, float yaw, float tickDelta, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - yaw));
		matrices.scale(-1.0f, -1.0f, 1.0f);
		matrices.translate(0.0, -1.501, 0.0);

		model.setAngles(entity, 0.0f, 0.0f, entity.age + tickDelta, 0.0f, 0.0f);
		VertexConsumer consumer = vertexConsumers.getBuffer(model.getLayer(TEXTURE));
		model.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);

		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}
}
