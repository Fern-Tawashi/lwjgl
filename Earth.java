import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import java.awt.image.DataBufferByte;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.Sys;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;

import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.PI;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;



class NVERTICES {
    float x;
    float y;
    float z;
    float ex;
    float ey;
    float ez;
    float u;
    float v;
}


/**
 * LWJGL球体テクスチャ貼りサンプル
 */
class GL {

	int texture;
	float angle = 0;
	final float TWOPI = (float)PI * 2;
	final int space = 20;

	public static long elapsed;
	public static int delta;
	
	/** time at last frame */
	public static long lastFrame;
	
	/** frames per second */
	int fps;
	
	/** last fps time */
	long lastFPS;

	
	NVERTICES[] VERTS_1;
	NVERTICES[] VERTS_2;

	int vert_index = 0;


	FloatBuffer qaAmbientLight  =  BufferUtils.createFloatBuffer(4).put(new float[] { 0.1f, 0.1f, 0.1f, 1.0f });
	FloatBuffer qaDiffuseLight  =  BufferUtils.createFloatBuffer(4).put(new float[] { 0.9f, 0.9f, 0.9f, 1.0f });
	FloatBuffer qaSpecularLight =  BufferUtils.createFloatBuffer(4).put(new float[] { 0.3f, 0.3f, 0.3f, 1.0f });
	
	FloatBuffer qaLightPosi =  BufferUtils.createFloatBuffer(4).put(new float[] { 0.0f, 0.0f, 1.0f, 0.0f });
	
	FloatBuffer qaRed =  BufferUtils.createFloatBuffer(4).put(new float[] { 0.9f, 0.1f, 0.1f, 1.0f });
	FloatBuffer qaGray =  BufferUtils.createFloatBuffer(4).put(new float[] { 0.7f, 0.7f, 0.7f, 1.0f });
	
	private Qua4 quaP = new Qua4(0, 0, 0, 1f);
	boolean drug;
	float fscale = 1.0f;
	
	
	public void start() {
		
		long lastFrame = 0;

		qaAmbientLight.flip();
		qaDiffuseLight.flip();
		qaSpecularLight.flip();
		qaLightPosi.flip();

		qaRed.flip();
		qaGray.flip();
		
		try {
			Display.setDisplayMode(new DisplayMode(480, 480));
			Display.setResizable(true);
			Display.setLocation(0, 0);
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		System.err.println("GL_VENDOR: " + GL11.glGetString(GL11.GL_VENDOR));
		System.err.println("GL_RENDERER: " + GL11.glGetString(GL11.GL_RENDERER));
		System.err.println("GL_VERSION: " + GL11.glGetString(GL11.GL_VERSION));
		
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_LIGHT0);
		
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT,  qaAmbientLight);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, qaSpecularLight);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE,  qaDiffuseLight);
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, qaLightPosi);
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		//texture = loadPNGTexture("textures/land_ocean_ice_cloud_2048.png", GL13.GL_TEXTURE0);
		texture = loadPNGTexture("textures/lroc_color_poles_2k_256.png", GL13.GL_TEXTURE0);
		
		CreateSphereArray(10, 20);
		
		
		reshape();

		getDelta(); // call once before loop to initialise lastFrame
		lastFPS = getTime(); // call before loop to initialise fps timer

		while (!Display.isCloseRequested()) {
			delta = getDelta();
			elapsed += delta;

			if (Display.wasResized()) {
				reshape();
			}
			
			render();
			
			pollInput();

			updateFPS();
			
			Display.update();
			Display.sync(60);
			
			long currentFrame = (Sys.getTime() * 1000) / Sys.getTimerResolution();
			if (currentFrame - lastFrame > 10) {
				lastFrame = currentFrame;
				vert_index++;
			}
		}
		
		Display.destroy();
	}
	
	
	
	public void reshape() {
		float width = Display.getWidth();
		float height = Display.getHeight();
		float h = height / width;
//System.out.printf("reshape %d x %d : %f\n", (int)width, (int)height, h);
		
		GL11.glViewport(0, 0, (int)width, (int)height);
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glFrustum(-1.0f, 1.0f, -h, h, 5.0f, 120.0f);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0f, 0.0f, -50.0f);
		
		float[] m = new float[16];
		quaP.toMatrix(m);
		MultMatrix(m);
		
		m[0]  = fscale; m[1]  = 0.0f;   m[2]  = 0.0f;   m[3]  = 0.0f;
		m[4]  = 0.0f;   m[5]  = fscale; m[6]  = 0.0f;   m[7]  = 0.0f;
		m[8]  = 0.0f;   m[9]  = 0.0f;   m[10] = fscale; m[11] = 0.0f;
		m[12] = 0.0f;   m[13] = 0.0f;   m[14] = 0.0f;   m[15] = 1.0f;
		MultMatrix(m);
	}
	
	
	
	public void render() {
		// Clear the screen and depth buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		GL11.glPushMatrix();
		DisplaySphere(50, texture);
		GL11.glPopMatrix();
	}
	
	
	
	public void pollInput() {
		while (Mouse.next()) {
			if (Mouse.getEventButton() == 0) {
				if (Mouse.getEventButtonState()) {
					vert_index += 1;
				}
			}

			if (Mouse.getEventButton() == 1) {
				drug = Mouse.getEventButtonState();
			}
			
			if (drug) {
				float dx = (float)Mouse.getEventDX() / Display.getWidth();
				float dy = (float)Mouse.getEventDY() / Display.getHeight();
				
				float ax = 0.0f;
				float ay = 0.0f;
				
				float v = (float)Math.sqrt(dx*dx + dy*dy);
				if (v > 0.0f) {
					ax = (float)dy / v * -1;
					ay = (float)dx / v;
				}
				
//System.out.printf("%f, %f, %f\n", ax, ay, v);
				quaP = Qua4.rot(quaP, ax, ay, 0, (float)Math.toRadians(v * 360.0f));
				reshape();
			}
			
			int dwh = Mouse.getDWheel();
			if (dwh > 0) {
				fscale *= 1.1f;
				reshape();
			}
			if (dwh < 0) {
				fscale *= 0.9f;
				reshape();
			}
			
		}
	
	}
	
	
	
	void DisplaySphere(float R, int texture){
	    GL11.glScalef(0.0125f * R, 0.0125f * R, 0.0125f * R);
	    GL11.glRotatef(90, 1, 0, 0);
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

	    GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE, qaGray);
	    GL11.glPointSize(1);

	    GL11.glBegin(GL11.GL_QUAD_STRIP);
//	    GL11.glBegin(GL11.GL_POINTS);
//	    GL11.glBegin(GL11.GL_LINE_LOOP);

		for (int i = 0; i < VERTS_1.length && i < vert_index; i++) {
			GL11.glNormal3f(VERTS_1[i].ex, VERTS_1[i].ey, VERTS_1[i].ez);
			GL11.glTexCoord2f(VERTS_1[i].u, VERTS_1[i].v);
			GL11.glVertex3f(VERTS_1[i].x, VERTS_1[i].y, VERTS_1[i].z);

			GL11.glNormal3f(VERTS_2[i].ex, VERTS_2[i].ey, VERTS_2[i].ez);
			GL11.glTexCoord2f(VERTS_2[i].u, VERTS_2[i].v);
			GL11.glVertex3f(VERTS_2[i].x, VERTS_2[i].y, VERTS_2[i].z);
		}

	    GL11.glEnd();
	    
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}


	private int loadPNGTexture(String filename, int textureUnit) {
		ByteBuffer buf = null;
		int tWidth = 0;
		int tHeight = 0;

		try (
			// Open the PNG file as an InputStream
			InputStream in = new FileInputStream(filename)
		) {
			// Link the PNG decoder to this stream
			PNGDecoder decoder = new PNGDecoder(in);
			
			// Get the width and height of the texture
			tWidth = decoder.getWidth();
			tHeight = decoder.getHeight();
			
			// Decode the PNG file in a ByteBuffer
			buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
			decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
			buf.flip();
			
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		// Create a new texture object in memory and bind it
		int texId = GL11.glGenTextures();
		GL13.glActiveTexture(textureUnit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);

		// All RGB bytes are aligned to each other and each component is 1 byte
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

		// Upload the texture data and generate mip maps (for scaling)
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, tWidth, tHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
		//GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

		// Setup the ST coordinate system
		//GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		//GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

		// Setup what to do when the texture has to be scaled
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		return texId;
	}



	static void MultMatrix(float[] m) {
		FloatBuffer floatBuf = BufferUtils.createFloatBuffer(16);
		floatBuf.clear();
		floatBuf.put(m).flip();
		GL11.glMultMatrix(floatBuf);
	}



	/**
	 * @param fRadius
	 * @param nDiv
	 */
	void CreateSphereArray(float fRadius, int nDiv) {
		int n = 0;

		VERTS_1 = new NVERTICES[nDiv * (nDiv + 1)];
		VERTS_2 = new NVERTICES[nDiv * (nDiv + 1)];

		for (int j = 0; j < nDiv; j++) {
			float phi1 = j * TWOPI / nDiv;
			float phi2 = (j + 1) * TWOPI / nDiv;	//next phi

			for (int i = 0; i <= nDiv; i++) {
				float theta = (float)i * (float)PI / nDiv;

				VERTS_1[n] = new NVERTICES();
				
				VERTS_1[n].ex = (float)sin ( theta ) * (float)cos ( phi2 );
				VERTS_1[n].ey = (float)sin ( theta ) * (float)sin ( phi2 );
				VERTS_1[n].ez = (float)cos ( theta );

				VERTS_1[n].x = fRadius * VERTS_1[n].ex;
				VERTS_1[n].y = fRadius * VERTS_1[n].ey;
				VERTS_1[n].z = fRadius * VERTS_1[n].ez;

				float s = phi2 / TWOPI;				// column
				float t = 1 - theta / (float)PI;	// row

				VERTS_1[n].u = s;
				VERTS_1[n].v = t;

				VERTS_2[n] = new NVERTICES();

				VERTS_2[n].ex = (float)sin ( theta ) * (float)cos ( phi1 );
				VERTS_2[n].ey = (float)sin ( theta ) * (float)sin ( phi1 );
				VERTS_2[n].ez = (float)cos ( theta );

				VERTS_2[n].x = fRadius * VERTS_2[n].ex;
				VERTS_2[n].y = fRadius * VERTS_2[n].ey;
				VERTS_2[n].z = fRadius * VERTS_2[n].ez;

				s = phi1 / TWOPI;			// column
				t = 1 - theta / (float)PI;	// row

				VERTS_2[n].u = s;
				VERTS_2[n].v = t;

				n++;
			}
		}
		System.out.println("verts size x 2: " + n);
	}

	/** 
	 * Calculate how many milliseconds have passed 
	 * since last frame.
	 * 
	 * @return milliseconds passed since last frame 
	 */
	private int getDelta() {
		long time = getTime();
		int delta = (int) (time - lastFrame);
		lastFrame = time;
		
		return delta;
	}

	/**
	 * Get the accurate system time
	 * 
	 * @return The system time in milliseconds
	 */
	public static long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

	/**
	 * Calculate the FPS and set it in the title bar
	 */
	private void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			Display.setTitle("FPS: " + fps + " ELA: " + elapsed);
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}
}



public class Earth {
	public static void main(String[] argv) {
	    GL gl = new GL();
	    gl.start();
	}
}
