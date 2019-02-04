package com.displee.render.impl;

import com.displee.Constants;
import com.displee.cache.ModelDefinition;
import com.displee.cache.TextureDefinition;
import com.displee.render.GLWrapper;
import com.displee.util.Utilities;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.util.HashMap;
import java.util.Map;

import static com.displee.Constants.ENABLE_TEXTURES;
import static org.lwjgl.opengl.GL11.*;

/**
 * An implementation for rendering RS3 models using OpenGL.
 *
 * @author Displee
 */
public class DefaultGLRenderer extends GLWrapper<ModelDefinition> {

	/**
	 * The model scale constant.
	 */
	private static final float MODEL_SCALE = 4.0F;

	/**
	 * Holding all used textures in this renderer.
	 */
	private Map<Integer, Integer> textureMap = new HashMap<>();

	/**
	 * If we have to show the polygons of a model.
	 */
	private boolean showPolygons = false;

	/**
	 * Constructs a new {@code DefaultGLRenderer} {@code Object}.
	 *
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
		if (showPolygons != Constants.SHOW_POLYGONS) {
			showPolygons = Constants.SHOW_POLYGONS;
			togglePolygons();
		}

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

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
		short[] faceTextures = model.getFaceMaterials();
		int[] verticesX = model.getVerticesX();
		int[] verticesY = model.getVerticesY();
		int[] verticesZ = model.getVerticesZ();

		boolean hasAlpha = model.getFaceAlphas() != null;
		boolean hasFaceTypes = faceTypes != null;

		for (int i = 0; i < numFaces; i++) {
			int alpha = hasAlpha ? model.getFaceAlphas()[i] : 0;
			if (alpha == -1) {
				continue;
			}
			alpha = ~alpha & 0xFF;
			final int faceType = hasFaceTypes ? faceTypes[i] & 0x3 : 0;
			int faceA;
			int faceB;
			int faceC;
			switch (faceType) {
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

			short textureId = faceTextures == null ? -1 : faceTextures[i];
			float[] u = null;
			float[] v = null;
			int color = Utilities.forHSBColor(model.getFaceColors()[i]);
			if (ENABLE_TEXTURES && textureId != -1) {

				glEnable(GL_TEXTURE_2D);

				TextureDefinition texture = model.getTextures()[i];

				int openGlId = getTexture(texture);

				if (model.getTexturedUCoordinates() == null || model.getTexturedVCoordinates() == null) {
					model.computeTextureCoordinates();
				}
				u = model.getTexturedUCoordinates()[i];
				v = model.getTexturedVCoordinates()[i];

				glBindTexture(GL_TEXTURE_2D, openGlId);
			}
			glBegin(GL_TRIANGLES);
			glColor4ub((byte)(color >> 16), (byte) (color >> 8), (byte) color, (byte) alpha);
			if (ENABLE_TEXTURES && textureId != -1) {
				glTexCoord2f(u[0], v[0]);
			}
			glVertex3f(verticesX[faceA] / MODEL_SCALE, verticesY[faceA] / MODEL_SCALE, verticesZ[faceA] / MODEL_SCALE);
			if (ENABLE_TEXTURES && textureId != -1) {
				glTexCoord2f(u[1], v[1]);
			}
			glVertex3f(verticesX[faceB] / MODEL_SCALE, verticesY[faceB] / MODEL_SCALE, verticesZ[faceB] / MODEL_SCALE);
			if (ENABLE_TEXTURES && textureId != -1) {
				glTexCoord2f(u[2], v[2]);
			}
			glVertex3f(verticesX[faceC] / MODEL_SCALE, verticesY[faceC] / MODEL_SCALE, verticesZ[faceC] / MODEL_SCALE);
			glEnd();
			if (ENABLE_TEXTURES && textureId != -1) {
				glDisable(GL_TEXTURE_2D);
			}
		}
	}

	private int getTexture(TextureDefinition textureDefinition) {
		Integer openGlId = textureMap.get(textureDefinition.getId());
		if (openGlId != null) {
			return openGlId;
		}
		int width = textureDefinition.getImage().getWidth();
		int height = textureDefinition.getImage().getHeight();

		int glTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, glTexture);

		//Setup filtering, i.e. how OpenGL will interpolate the pixels when scaling up or down
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		//Setup wrap mode, i.e. how OpenGL will handle pixels outside of the expected range
		//Note: GL_CLAMP_TO_EDGE is part of GL12
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, textureDefinition.toByteBuffer());

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR); // Linear Filtering
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR); // Linear Filtering

		textureMap.put(textureDefinition.getId(), glTexture);
		return glTexture;
	}

	private void togglePolygons() {
		if (Constants.SHOW_POLYGONS) {
			glEnable(GL_POLYGON_SMOOTH);
		} else {
			glDisable(GL_POLYGON_SMOOTH);
		}
	}

}
