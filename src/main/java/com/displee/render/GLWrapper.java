package com.displee.render;

import static org.lwjgl.opengl.AMDDebugOutput.glDebugMessageCallbackAMD;
import static org.lwjgl.opengl.GL11.GL_DONT_CARE;
import static org.lwjgl.opengl.KHRDebug.GL_DEBUG_SEVERITY_MEDIUM;
import static org.lwjgl.opengl.KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION;
import static org.lwjgl.opengl.KHRDebug.GL_DEBUG_SOURCE_API;
import static org.lwjgl.opengl.KHRDebug.GL_DEBUG_TYPE_PERFORMANCE;
import static org.lwjgl.opengl.KHRDebug.glDebugMessageCallback;
import static org.lwjgl.opengl.KHRDebug.glDebugMessageControl;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;
import org.lwjgl.util.stream.RenderStream;
import org.lwjgl.util.stream.StreamHandler;
import org.lwjgl.util.stream.StreamUtil;
import org.lwjgl.util.stream.StreamUtil.RenderStreamFactory;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;

/**
 * A class serving as a wrapper to render OpenGL in JavaFX.
 * @param <T> The render context (i.e what we're rendering).
 * @author Displee
 */
public abstract class GLWrapper<T> {

	/**
	 * The zoom factor.
	 */
	private static final float ZOOM_FACTOR = 0.2F;

	/**
	 * The interval used to calculate the FPS.
	 */
	private static final long FPS_UPD_INTERVAL = (1000L * 1000L * 1000L);

	/**
	 * The render context.
	 */
	@Getter
	@Setter
	protected T context;

	/**
	 * The buffer.
	 */
	private final Pbuffer pbuffer;

	/**
	 * The render factory.
	 */
	private RenderStreamFactory renderStreamFactory;

	/**
	 * The actual render buffer.
	 */
	private RenderStream renderStream;

	/**
	 * The property holding the current FPS.
	 */
	protected final ReadOnlyIntegerWrapper fps;

	/**
	 * The image view.
	 */
	protected final ImageView view;

	/**
	 * The image we are rendering.
	 */
	private WritableImage renderImage;

	/**
	 * The FPS limit;
	 */
	@Setter
	private int fpsLimit = 60;

	/**
	 * If we are running.
	 */
	private boolean running = true;

	/**
	 * The horizontal mouse position.
	 */
	protected double mousePosX;

	/**
	 * The vertical mouse position.
	 */
	protected double mousePosY;

	/**
	 * The scale.
	 */
	protected float scale = 1.0F;

	/**
	 * The last known width of the {@code view}.
	 */
	private double lastWidth;

	/**
	 * The last known height of the {@code view}.
	 */
	private double lastHeight;

	/**
	 * Create a new {@code GLWrapper} {@code Object}.
	 * @param view The image view.
	 */
	public GLWrapper(final ImageView view) {
		this.view = view;
		if ((Pbuffer.getCapabilities() & Pbuffer.PBUFFER_SUPPORTED) == 0) {
			throw new UnsupportedOperationException("Support for pbuffers is required.");
		}
		this.fps = new ReadOnlyIntegerWrapper(this, "fps", 0);
		try {
			//not sure if this pixel format is 100%
			pbuffer = new Pbuffer(1, 1, new PixelFormat(), null, null, new ContextAttribs().withDebug(true));
			pbuffer.makeCurrent();
		} catch (LWJGLException e) {
			throw new RuntimeException(e);
		}
		final ContextCapabilities caps = GLContext.getCapabilities();
		if (caps.GL_KHR_debug) {
			glDebugMessageCallback(new KHRDebugCallback());
			glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DEBUG_SEVERITY_NOTIFICATION, null, false);//Buffer object mallocs
			glDebugMessageControl(GL_DEBUG_SOURCE_API, GL_DEBUG_TYPE_PERFORMANCE, GL_DEBUG_SEVERITY_MEDIUM, null, false);//Pixel-transfer synchronized with rendering
		} else if (caps.GL_AMD_debug_output) {
			glDebugMessageCallbackAMD(new AMDDebugOutputCallback());
		}
		this.renderStreamFactory = StreamUtil.getRenderStreamImplementation();
		this.renderStream = renderStreamFactory.create(getReadHandler(), 1, 2);
		view.setOnMousePressed((MouseEvent event) -> {
			mousePosX = event.getSceneX();
			mousePosY = event.getSceneY();
		});
		view.setOnMouseDragged((MouseEvent event) -> {
			mousePosX = event.getSceneX();
			mousePosY = event.getSceneY();
		});
		view.setOnScroll(e -> {
			final double delta = e.getDeltaY();
			if (delta > 1.0F) {
				scale += ZOOM_FACTOR;
			} else if (delta < -1.0F && scale - ZOOM_FACTOR >= ZOOM_FACTOR) {
				scale -= ZOOM_FACTOR;
			}
			e.consume();
		});
		view.setOnKeyTyped((e) -> {
			switch (e.getCode()) {
			case UP:
				mousePosY--;
				break;
			case RIGHT:
				mousePosX--;
				break;
			case DOWN:
				mousePosY++;
				break;
			case LEFT:
				mousePosX++;
				break;
			default:
				System.err.println("Unhandled keycode=" + e.getCode());
				break;
			}
			e.consume();
		});
	}

	/**
	 * Initialize OpenGL.
	 */
	protected abstract void init();

	/**
	 * Event called when the {@code view} is being resized.
	 */
	protected void onResize() {

	}

	/**
	 * The render loop.
	 */
	protected abstract void render();

	/**
	 * Use the argued nodes to notify updates.
	 * @param nodes The nodes.
	 */
	protected abstract void update(Node... nodes);

	/**
	 * An event fired when this wrapper gets terminated.
	 */
	protected abstract void onTerminate();

	/**
	 * Run this wrapper.
	 * @param nodes The update nodes.
	 */
	public void run(Node... nodes) {
		/*try {
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.create();
		} catch(LWJGLException e) {
			e.printStackTrace();
		}*/

		init();

		long nextFPSUpdateTime = System.nanoTime() + FPS_UPD_INTERVAL;

		int frames = 0;

		while (running) {
			if (lastWidth != view.getFitWidth() || lastHeight != view.getFitHeight()) {
				onResize();
				lastWidth = view.getFitWidth();
				lastHeight = view.getFitHeight();
			}
			if (context != null) {
				renderStream.bind();
				render();
				renderStream.swapBuffers();
			}

			//Display.update();

			Display.sync(fpsLimit);

			final long currentTime = System.nanoTime();
			frames++;
			if (nextFPSUpdateTime <= currentTime ) {
				long timeUsed = FPS_UPD_INTERVAL + (currentTime - nextFPSUpdateTime);
				nextFPSUpdateTime = currentTime + FPS_UPD_INTERVAL;
				final int fpsAverage = (int)(frames * FPS_UPD_INTERVAL / (timeUsed));
				fps.set(fpsAverage);
				frames = 0;
			}
			Platform.runLater(() -> update(nodes));
		}

		onTerminate();
		renderStream.destroy();
		pbuffer.destroy();
		Display.destroy();
		context = null;
	}

	/**
	 * Terminate this wrapper.
	 */
	public void terminate() {
		running = false;
	}

	/**
	 * Set the scale size.
	 * @param scale The scale size to set.
	 */
	public void scale(float scale) {
		this.scale = scale;
	}

	/**
	 * Rotate the model.
	 * @param x The horizontal rotation.
	 * @param y The vertical rotation.
	 */
	public void rotate(int x, int y) {
		mousePosX = x;
		mousePosY = y;
	}

	/**
	 * Get the read handler which uploads a snapshot to JavaFX.
	 * @return A new {@code StreamHandler} instance.
	 */
	private StreamHandler getReadHandler() {
		return new StreamHandler() {

			private long frame;
			private long lastUpload;

			{
				new AnimationTimer() {
					@Override
					public void handle(final long now) {
						frame++;
					}
				}.start();
			}

			@Override
			public int getWidth() {
				return (int) view.getFitWidth();
			}

			@Override
			public int getHeight() {
				return (int) view.getFitHeight();
			}

			@Override
			public void process(final int width, final int height, final ByteBuffer data, final int stride, final Semaphore signal) {
				// This method runs in the background rendering thread
				// TODO: Run setPixels on the PlatformImage in this thread, run pixelsDirty on JFX application thread with runLater.
				Platform.runLater(() -> {
                    try {
                        if (!view.isVisible()) {
                            return;
                        }
                        if (renderImage == null || (int) renderImage.getWidth() != width || (int) renderImage.getHeight() != height) {
                            renderImage = new WritableImage(width, height);
                            view.setImage(renderImage);
                        }
                        if (frame <= lastUpload + 1) {
                            return;
                        }
                        lastUpload = frame;
                        PixelWriter pw = renderImage.getPixelWriter();
                        pw.setPixels(0, 0, width, height, pw.getPixelFormat(), data, stride);
                    } finally {
                        signal.release();
                    }
                });
			}
		};
	}

}