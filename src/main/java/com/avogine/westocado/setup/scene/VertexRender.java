package com.avogine.westocado.setup.scene;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.avogine.westocado.entities.Entities;
import com.avogine.westocado.entities.bodies.Body;
import com.avogine.westocado.entities.bodies.CameraBody;
import com.avogine.westocado.entities.models.Model;
import com.avogine.westocado.render.shaders.VertexShader;

public class VertexRender {

	public static final float FOV = 70;
	public static final float NEAR_PLANE = 0.1f;
	public static final float FAR_PLANE = 1000f;

	private CameraBody camera;
	
	private VertexShader shader;
	private Matrix4f projectionMatrix;

	public VertexRender(CameraBody camera) {
		this.camera = camera;
		
		shader = new VertexShader("vertexShader.glsl", "fragmentShader.glsl", "position");
		createProjectionMatrix();
		shader.start();
		shader.projection.loadMatrix(projectionMatrix);
		shader.stop();
	}

	public void renderScene() {
		for(Model model : Entities.modelComponentMap.values()) {
			render(model);
		}
	}
	
	public void render(Model model) {
		prepareInstance(model);
		
		// TODO Make a custom ModelComponent for this? Probably tie it to shit like the collision shape in RigidBodies?
		Body body = Entities.bodyComponentMap.get(model.getEntity());
		if(body == null || body.getDebugMesh() == null) {
			return;
		}
		body.getDebugMesh().getVao().bind(0);
		//GL11.glDrawElements(GL11.GL_LINE_LOOP, entity.getBody().getDebugMesh().getVao().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		GL11.glDrawElements(GL11.GL_TRIANGLES, body.getDebugMesh().getVao().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		body.getDebugMesh().getVao().unbind(0);
		
		finish();
	}

	private void prepareInstance(Model model) {
		shader.start();
		Matrix4f transform = new Matrix4f();
		Body body = Entities.bodyComponentMap.get(model.getEntity());
		if(body != null) {
			transform.translate(body.getPosition());
			transform.scale(body.getSize());
		}
		
		shader.model.loadMatrix(transform);
		if(camera != null) {
			shader.view.loadMatrix(camera.getViewMatrix());
		} else {
			shader.view.loadMatrix(new Matrix4f());
		}
		shader.color.loadVec3(1, 0, 0);
	}

	public void finish() {
		shader.stop();
	}
	
	public void cleanUp() {
		shader.cleanUp();
	}

	public void setCameraEntity(CameraBody camera) {
		if(camera == null || !(camera instanceof CameraBody)) {
			System.err.println("Invalid camera entity, make sure to implement the CameraBody before setting it to renderer.");
			return;
		}
		this.camera = camera;
	}
	
	private void createProjectionMatrix() {
		projectionMatrix = new Matrix4f();
		float aspectRatio = (float) 1280 / (float) 720;
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;
		
		projectionMatrix.m00(x_scale);
		projectionMatrix.m11(y_scale);
		projectionMatrix.m22(-((FAR_PLANE + NEAR_PLANE) / frustum_length));
		projectionMatrix.m23(-1);
		projectionMatrix.m32(-((2 * NEAR_PLANE * FAR_PLANE) / frustum_length));
		projectionMatrix.m33(0);
	}
	
}
