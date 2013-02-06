package jp.kshoji.stlviewer.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.kshoji.stlviewer.object.STLObject;
import jp.kshoji.stlviewer.util.Log;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

public class STLRenderer implements Renderer {
	public static final int FRAME_BUFFER_COUNT = 2;
	
	public float angleX;
	public float angleY;
	public float positionX = 0f;
	public float positionY = 0f;
	public float distanceZ = 100f;
	
	public static float red;
	public static float green;
	public static float blue;
	public static float alpha;
	public static boolean displayAxes = true;
	public static boolean displayGrids = true;
	private static int bufferCounter = 2;

	private STLObject stlObject;
	
	public STLRenderer(STLObject stlObject) {
		this.stlObject = stlObject;
	}

	public static void requestRedraw() {
		bufferCounter = FRAME_BUFFER_COUNT;
	}

	private void drawGrids(GL10 gl) {
		List<Float> lineList = new ArrayList<Float>();
		
		for (int x = -100; x <= 100; x += 5) {
			lineList.add((float) x);
			lineList.add(-100f);
			lineList.add(0f);
			lineList.add((float)x);
			lineList.add(100f);
			lineList.add(0f);
		}
		for (int y = -100; y <= 100; y += 5) {
			lineList.add(-100f);
			lineList.add((float) y);
			lineList.add(0f);
			lineList.add(100f);
			lineList.add((float) y);
			lineList.add(0f);
		}

		FloatBuffer lineBuffer = getFloatBufferFromList(lineList);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineBuffer);

		gl.glLineWidth(1f);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[]{0.5f, 0.5f, 0.5f, 1.0f}, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[]{0.5f, 0.5f, 0.5f, 1.0f}, 0);
		gl.glDrawArrays(GL10.GL_LINES, 0, lineList.size() / 3);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		if (bufferCounter < 1) {
			return;
		}
		bufferCounter--;

		gl.glLoadIdentity();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glTranslatef(positionX, -positionY, 0);

		// rotation and apply Z-axis
		if (stlObject != null) {
			gl.glTranslatef(-(stlObject.maxY + stlObject.minY) / 2, -(stlObject.maxX + stlObject.minX) / 2, -(stlObject.maxZ + stlObject.minZ) - distanceZ);
		} else {
			gl.glTranslatef(0, 0, -distanceZ);
		}
		gl.glRotatef(angleX, 0, 1, 0);
		gl.glRotatef(angleY, 1, 0, 0);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		// draw X-Y field
		if (displayGrids) {
			drawGrids(gl);
		}

		// draw axis
		if (displayAxes) {
			gl.glLineWidth(3f);
			float[] vertexArray = { -100, 0, 0, 100, 0, 0, 0, -100, 0, 0, 100, 0, 0, 0, -100, 0, 0, 100 };
			FloatBuffer lineBuffer = getFloatBufferFromArray(vertexArray);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineBuffer);
		
			// X : red
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[] { 1.0f, 0f, 0f, 0.75f }, 0);
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[] { 1.0f, 0f, 0f, 0.5f }, 0);
			gl.glDrawArrays(GL10.GL_LINES, 0, 2);

			// Y : blue
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[] { 0f, 0f, 1.0f, 0.75f }, 0);
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[] { 0f, 0f, 1.0f, 0.5f }, 0);
			gl.glDrawArrays(GL10.GL_LINES, 2, 2);

			// Z : green
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[] { 0f, 1.0f, 0f, 0.75f }, 0);
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[] { 0f, 1.0f, 0f, 0.5f }, 0);
			gl.glDrawArrays(GL10.GL_LINES, 4, 2);
		}

		// draw object
		if (stlObject != null) {
			// FIXME transparency applying does not correctly
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, new float[] { 0.75f, 0.75f, 0.75f, 0.95f }, 0);
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, new float[] { red, green, blue, alpha }, 0);
			stlObject.draw(gl);
		}
	}
	
	private FloatBuffer getFloatBufferFromArray(float[] vertexArray) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertexArray.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		FloatBuffer triangleBuffer = vbb.asFloatBuffer();
		triangleBuffer.put(vertexArray);
		triangleBuffer.position(0);
		return triangleBuffer;
	}

	private FloatBuffer getFloatBufferFromList(List<Float> vertexList) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertexList.size() * 4);
		vbb.order(ByteOrder.nativeOrder());
		FloatBuffer triangleBuffer = vbb.asFloatBuffer();
		float[] array = new float[vertexList.size()];
		for (int i = 0; i < vertexList.size(); i++) {
			array[i] = vertexList.get(i);
		}
		triangleBuffer.put(array);
		triangleBuffer.position(0);
		return triangleBuffer;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		float aspectRatio = (float) width / height;

		gl.glViewport(0, 0, width, height);
		
		gl.glLoadIdentity();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		if (stlObject != null) {
			Log.i("maxX:" + stlObject.maxX);
			Log.i("minX:" + stlObject.minX);
			Log.i("maxY:" + stlObject.maxY);
			Log.i("minY:" + stlObject.minY);
			Log.i("maxZ:" + stlObject.maxZ);
			Log.i("minZ:" + stlObject.minZ);
		}

		GLU.gluPerspective(gl, 45f, aspectRatio, 1f, 5000f);// (stlObject.maxZ - stlObject.minZ) * 10f + 100f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		GLU.gluLookAt(gl, 0, 0, 100f, 0, 0, 0, 0, 1f, 0);
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		gl.glClearColor(0f, 0f, 0f, 0.5f);

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		// FIXME This line seems not to be needed?
		gl.glEnable(GL10.GL_DEPTH_TEST);

		gl.glShadeModel(GL10.GL_SMOOTH);

		gl.glMatrixMode(GL10.GL_PROJECTION);

		// Lighting
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, new float[]{0.3f, 0.3f, 0.3f, 0.85f}, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, new float[]{1f, 1f, 1f, 0.75f}, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, new float[] { 0f, 0f, 1000f, 1f }, 0); // light comes above of screen
	}
}
