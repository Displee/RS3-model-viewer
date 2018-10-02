package com.displee.render.impl;

import com.displee.cache.ModelDefinition;
import com.displee.render.GLWrapper;
import com.displee.util.Utilities;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

/**
 * An implementation for rendering RS3 models using OpenGL.
 * @author Displee
 */
public class DefaultGLRenderer extends GLWrapper<ModelDefinition> {

	/**
	 * Constructs a new {@code DefaultGLRenderer} {@code Object}.
	 * @param view The image view.
	 */
	public DefaultGLRenderer(ImageView view) {
		super(view);
	}

	@Override
	protected void init() {
		glShadeModel(GL_SMOOTH);
		glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		glEnable(GL_DEPTH_TEST);
		glClearDepth(1.0F);
		glDepthFunc(GL_LEQUAL);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		glEnable(GL_NORMALIZE);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_BLEND);
		glEnable(GL_POINT_SMOOTH);
		glEnable(GL_POLYGON_SMOOTH);
		glEnable(GL_LINE_SMOOTH);
		glEnable(GL_COLOR_MATERIAL);
		glEnable(GL_ALPHA_TEST);
		glEnable(GL_CULL_FACE);
		glEnable(GL_COLOR_MATERIAL);
		glColorMaterial(GL_FRONT, GL_DIFFUSE);
		glCullFace(GL_BACK);
	}

	@Override
	protected void onResize() {
		int width = (int) view.getFitWidth();
		int height = (int) view.getFitHeight();

		glViewport(0, 0, width, height);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		float c = (float) Math.sqrt((double) (width * width) + (double) (height * height));
		glOrtho(0.0F, width, 0.0F, height, -c, c);//0.01F
		//gluPerspective(60.0F, (float) width / (float) height, 0.01F, 10000.0F);
		glMatrixMode(GL_MODELVIEW);
	}

	@Override
	protected void render() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		float width = (float) view.getFitWidth();
		float height = (float) view.getFitHeight();

		float roll = 0.0F;
		float scale = super.scale;

		render(context, width / 2.0F, height / 2.0F, 0.0F, (float) mousePosY, (float) mousePosX, roll, scale, scale, scale);
	}

	@Override
	protected void update(Node... nodes) {
		Label label = (Label) nodes[0];
		label.setText("FPS: " + fps.get());
	}

	@Override
	protected void onTerminate() {

	}

	private void render(ModelDefinition model, float x, float y, float z, float rx, float ry, float rz, float sx, float sy, float sz) throws IllegalStateException {
		glLoadIdentity();
		glTranslatef(x, y, z);
		glRotatef(rx, 1.0F, 0.0F, 0.0F);
		glRotatef(ry, 0.0F, 1.0F, 0.0F);
		glRotatef(rz, 0.0F, 0.0F, 1.0F);
		glScalef(sx, sy, sz);

		int numFaces = model.getNumFaces();
		byte[] faceTypes = model.getFaceTypes();
		short[] faceIndicesA = model.getFaceIndicesA();
		short[] faceIndicesB = model.getFaceIndicesB();
		short[] faceIndicesC = model.getFaceIndicesC();
		short[] textureMappingP = model.getTextureMappingP();
		short[] textureMappingM = model.getTextureMappingM();
		short[] textureMappingN = model.getTextureMappingN();
		short[] textures = model.getFaceTextures();
		int[] verticesX = model.getVerticesX();
		int[] verticesY = model.getVerticesY();
		int[] verticesZ = model.getVerticesZ();
		short[] colors = model.getFaceColors();

		boolean hasAlpha = model.getFaceAlphas() != null;
		boolean hasFaceTypes = faceTypes != null;

		for (int i = 0; i < numFaces; i++) {
			byte alpha = hasAlpha ? model.getFaceAlphas()[i] : 0;
			if (alpha == -1) {
				continue;
			}
			alpha = (byte) (~alpha & 0xFF);
			final int faceType = hasFaceTypes ? faceTypes[i] & 0x3 : 0;
			int faceA;
			int faceB;
			int faceC;
			switch(faceType) {
				case 0:
				case 1:
					faceA = faceIndicesA[i];
					faceB = faceIndicesB[i];
					faceC = faceIndicesC[i];
					break;
				case 2:
				case 3:
					faceA = textureMappingP[i];
					faceB = textureMappingM[i];
					faceC = textureMappingN[i];
					break;
				default:
					throw new IllegalStateException("Unknown face type=" + faceType);
			}

			int textureId = textures == null ? -1 : textures[i] & 0xffff;
			if (textureId != -1) {
				//TODO Textures
			}

			int color = Utilities.forHSBColor(colors[i]);
			glDisable(GL_TEXTURE_2D);
			glBegin(GL_TRIANGLES);
			glColor4ub((byte) (color >> 16), (byte) (color >> 8), (byte) color, alpha);

			float scale = 4.0F;
			glVertex3f(verticesX[faceA] / scale, verticesY[faceA] / scale, verticesZ[faceA] / scale);
			glVertex3f(verticesX[faceB] / scale, verticesY[faceB] / scale, verticesZ[faceB] / scale);
			glVertex3f(verticesX[faceC] / scale, verticesY[faceC] / scale, verticesZ[faceC] / scale);
			glEnd();
		}
	}

}
