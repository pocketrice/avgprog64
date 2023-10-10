package io.github.pocketrice;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Arrays;
public class App extends ApplicationAdapter {
	boolean isMouseDown = false;
	Cursor cursorIdle, cursorActive;
	PolygonSpriteBatch batch;
	SpriteBatch ui;
	Sprite bg, vig;
	TextureAtlas sparkleTa;
	//ParticleEffectDescriptor sparkleP;
//	ParticleEffectInstance sparkleI;
	Pixmap handIdle, handActive;
	PerspectiveCamera gameCamera;
	OrthographicCamera hudCamera;
	//SpriteBatchParticleRenderer particleRenderer;
	Viewport vp;
	CameraInputController cic;
	Model model;
	Array<ModelInstance> mi;
	ModelBatch mb;
	Environment env;
	BitmapFont font;
	int VIEWPORT_HEIGHT = 680;
	int VIEWPORT_WIDTH = 960;
	float rotAmt = 0;
	Vector3 CAMERA_POS, CAMERA_LOOK;
	boolean isVertActive = false;


	@Override
	public void create() {
		env = new Environment();
		env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.4f, 0.1f, 1f));
		env.add(new DirectionalLight().set(0.8f, 0.8f, 1.3f, -5f, 5f, -1f));

		gameCamera = new PerspectiveCamera(67, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
		hudCamera = new OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
		vp = new FillViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);

		CAMERA_POS = new Vector3(10f, 10f, 10f);
		CAMERA_LOOK = new Vector3(0f, 0f, 0f);
		gameCamera.position.set(CAMERA_POS);
		gameCamera.lookAt(CAMERA_LOOK);
		gameCamera.near = 1f;
		gameCamera.far = 300f;
		gameCamera.update();

		batch = new PolygonSpriteBatch();
		ui = new SpriteBatch();
		bg = new Sprite(resizeTexture(new Texture("img/avgprog64.png"), 241, 221));
		vig = new Sprite(new Texture("img/crt2.png"), 960, 680);
		vig.setScale(0.5f);

	//particleRenderer = new SpriteBatchParticleRenderer();
		TextureRegion tr = new TextureRegion(new Texture(Gdx.files.internal("img/sparkle1.png")));
		sparkleTa = new TextureAtlas();
		sparkleTa.addRegion("sparkle1", tr);

		//sparkleTa = //generateAtlas("sparkle", Gdx.files.internal("img/sparkle1.png"), Gdx.files.internal("img/sparkle2.png"), Gdx.files.internal("img/sparkle3.png"), Gdx.files.internal("img/sparkle4.png"), Gdx.files.internal("img/sparkle5.png"));
	//	sparkleP = new ParticleEffectDescriptor(Gdx.files.internal("vfx/sparkle.p"), sparkleTa);


		font = new BitmapFont(Gdx.files.internal("fonts/sm64.fnt"));
		font.setColor(Color.WHITE);
		ModelBuilder mbuilder = new ModelBuilder();
		model = mbuilder.createCylinder(5,10,5,10,///*mbuilder.createBox(5,5,5,*/mbuilder.createSphere(10f, 10f, 10f, 10, 10,
				new Material(ColorAttribute.createAmbientLight(Color.ROYAL)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

		mi = new Array<>();
		mi.add(new ModelInstance(model));
		cic = new CameraInputController(gameCamera);
		Gdx.input.setInputProcessor(cic);




		handIdle = new Pixmap(Gdx.files.internal("img/hand1.png"));
		handActive = new Pixmap(Gdx.files.internal("img/hand2.png"));
		cursorIdle = Gdx.graphics.newCursor(handIdle, 13, 30);
		cursorActive = Gdx.graphics.newCursor(handActive, 13, 30);
		Gdx.graphics.setCursor(cursorIdle);
	}

	@Override
	public void resize(int width, int height) {
		gameCamera.update();
		hudCamera.update();
		vp.update(width, height, true);
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Bullet.init();
		final float delta = Math.min(1f/30f, Gdx.graphics.getDeltaTime()); // get Delta time

		cic.update();
		vp.apply();



		//sparkleI = new ParticleEffectInstance(sparkleP);
		if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
			if (!isMouseDown) {
				Gdx.graphics.setCursor(cursorActive);
				isMouseDown = true;
			}
		}
		else {
			if (isMouseDown) {
				Gdx.graphics.setCursor(cursorIdle);
				isMouseDown = false;
			}
		}
// https://stackoverflow.com/questions/23106093/how-to-get-a-stream-from-a-float
		if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
			//sparkleI = new ParticleEffectInstance(sparkleP);
			Mesh mesh = model.meshes.get(0);

			float[] verts = mesh.getVertices(new float[mesh.getNumVertices() * mesh.getVertexSize() / 4]);
			short[] indices = new short[mesh.getNumIndices()];
			mesh.getIndices(indices);

			/*int vertSize = IntStream.range(0, mesh.getVertexAttributes().size()).map(i -> mesh.getVertexAttributes().get(i).numComponents).sum(); // optimised
			Vector3[] vertCoords = IntStream.range(0, verts.length / vertSize).map(i -> i * vertSize).mapToObj(i -> new Vector3(verts[i], verts[i+1], verts[i+2])).collect(Collectors.toList()).toArray(new Vector3[0]);
*/

			Vector3 ssCoord = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
			System.out.println("X: " + Gdx.input.getX() + " | Y: " + Gdx.input.getY());
			System.out.println("SS: " + ssCoord);
			//Vector3 wsCoord = screenspaceToWorldspaceCoords(ssCoord);
			Vector3 wsCoord = gameCamera.unproject(ssCoord);
			System.out.println("WS: " + wsCoord);

			Vector3 nearPoint = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
			Vector3 farPoint = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 1);
			gameCamera.unproject(nearPoint);
			gameCamera.unproject(farPoint);

			Ray ray = new Ray(nearPoint, farPoint.sub(nearPoint).nor());

			Vector3 hit = new Vector3();
			System.out.println("Ray: " + ray);
			System.out.println("Verts: (" + verts.length + ") " + Arrays.toString(verts));
			System.out.println("Indices: (" + indices.length + ") " + Arrays.toString(indices));
			System.out.println("vert_size: " + mesh.getVertexSize() / 4);

			boolean isRaycast = Intersector.intersectRayTriangles(ray, verts, indices, mesh.getVertexSize() / 4, hit);


			if (isRaycast) {
				System.out.println(AnsiCode.ANSI_YELLOW + "HIT\n" + AnsiCode.ANSI_RESET);
				isVertActive = true;
			} else {
				// The ray did not hit the mesh
				System.out.println(AnsiCode.ANSI_WHITE + "NA\n" + AnsiCode.ANSI_RESET);
			}



			/*for (Vector3 vc : vertCoords) {
				if (getDistance(vc, wsCoord) < 3) {
					System.out.println("HIT\n\n");

					isVertActive = true;
				}
			}*/
		}

		/*sparkleI.update(delta);
		sparkleI.render(particleRenderer);*/

		hudCamera.update();
		gameCamera.update();
		batch.setProjectionMatrix(hudCamera.combined);
		batch.begin();

		drawTileBg(bg);
		batch.end();


		Matrix4 rot = new Matrix4().setToRotation(0f, 1f, 0f, rotAmt += 0.5f);
		mi.get(0).transform.set(rot);

		mb = new ModelBatch();
		mb.begin(gameCamera);
		mb.render(mi, env);
		mb.end();

		ui.setProjectionMatrix(hudCamera.combined);
		ui.begin();
		font.draw(ui, "FPS: " + Gdx.graphics.getFramesPerSecond(), -430, 300);
		if (isVertActive) {
			font.setColor(Color.YELLOW);
			font.draw(ui, "HIT", 0, 300);
			font.setColor(Color.WHITE);
			isVertActive = false;
		}
		ui.draw(vig, -VIEWPORT_WIDTH / 2f, -VIEWPORT_HEIGHT / 2f);
		ui.end();
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		ui.dispose();
		model.dispose();
		mb.dispose();
	}

	public Texture resizeTexture(Texture tex, int newWidth, int newHeight) {
		TextureData td = tex.getTextureData();
		td.prepare();
		Pixmap pm = td.consumePixmap();
		Pixmap newPm = new Pixmap(newWidth, newHeight, pm.getFormat());

		newPm.drawPixmap(pm, 0, 0, pm.getWidth(), pm.getHeight(), 0, 0, newWidth, newHeight);
		Texture newTex = new Texture(newPm);

		pm.dispose();
		newPm.dispose();
		return newTex;
	}

	public void drawTileBg(Sprite spr) {
		for (int i = 0; i < VIEWPORT_WIDTH + spr.getWidth(); i += spr.getWidth()) {
			for (int j = 0; j < VIEWPORT_HEIGHT + spr.getHeight(); j += spr.getHeight()) {
				batch.draw(spr, i - VIEWPORT_WIDTH / 2f, j - VIEWPORT_HEIGHT / 2f);
			}
		}
	}

	public TextureAtlas generateAtlas(String base, FileHandle... files) {
		TextureAtlas ta = new TextureAtlas();
		for (int i = 0; i < files.length; i++) {
			FileHandle file = files[i];
			TextureRegion tr = new TextureRegion(new Texture(file));
			ta.addRegion(base + i, tr);
		}

		return ta;
	}

	public Vector3 worldspaceToScreenspaceCoords(Vector3 wsCoord) {
		Matrix4 combinedMatrix = gameCamera.combined;
		return wsCoord.prj(combinedMatrix); // prj = mul, then div by w
	}

	public Vector3 screenspaceToWorldspaceCoords(Vector3 ssCoord) {
		Matrix4 combinedMatrix = new Matrix4();
		combinedMatrix.set(gameCamera.combined);
		combinedMatrix.inv();

		return ssCoord.mul(combinedMatrix);
	}

	public float getDistance(Vector3 v1, Vector3 v2) {
		return (float) Math.abs(Math.sqrt(Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2) + Math.pow(v1.z - v2.z, 2)));
	}

	public float getDistance(Vector2 v1, Vector2 v2) {
		return (float) Math.abs(Math.sqrt(Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2)));
	}
}
