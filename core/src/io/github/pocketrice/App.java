package io.github.pocketrice;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class App extends ApplicationAdapter {
	Cursor cursorIdle, cursorActive;
	SpriteBatch gameSprBatch, startSprBatch, fadeoutSprBatch, fadeinSprBatch;
	SpriteBatch ui;
	Sprite bg, vig, copyright, fade, pressStart, star;
	//TextureAtlas sparkleTa;
	//ParticleEffectDescriptor sparkleP;
//	ParticleEffectInstance sparkleI;
	Pixmap handIdle, handActive;
	PerspectiveCamera gameCamera;
	OrthographicCamera hudCamera;
	//SpriteBatchParticleRenderer particleRenderer;
	Viewport vp;
	CameraInputController cic;
	Model avgprog, avglogo;
	ModelInstance avgprogInst, avglogoInst;
	ModelBatch mainBatch, startBatch;
	Environment env;
	BitmapFont font;
	Pair<Float, Vector3>[] vertAnims;
	Vector3 nearestVert;
	Vector3[] nearestVerts;
	Sound sfxGrab, sfxMalleo, sfxRelease, sfxEnter;
	Music bgmSiivaTitle;

	boolean isVertActive = false, isVertDragged = false, isMouseDown = false, isStart = true, isStartFadeOut = false, isSceneFadeIn = false;
	boolean malleoHasPlayed = false, enterHasPlayed = false; // TEMP
	float rotAmt = 0, logoScaleCount = 0, progScaleCount = 0, starScaleCount = 0, fadeAlpha = 0f;
	float scale = 1f, targetScale = 10f, duration = 0.8f, elapsedTime = 0f;
	long startTime, frameCount = 0;

	final Vector3 CAMERA_POS = new Vector3(0f, 0.5f, 2.3f), CAMERA_LOOK = new Vector3(0.05f, 0.5f, 0f);
	final int VIEWPORT_HEIGHT = 680, VIEWPORT_WIDTH = 960, NEARVERT_COUNT = 0, LOGO_SCALE_LIMIT = 6, PROG_SCALE_LIMIT = 9, STAR_SCALE_LIMIT = 300;
	final float MAX_VERT_DISP = 3f, FADEOUT_SPEED = 1.1f;



	@Override
	public void create() {
		startTime = System.currentTimeMillis();
		env = new Environment();
		env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.7f, 1f));
		env.add(new DirectionalLight().set(0.4f, 0.1f, 0.8f, 1f, -0.5f, 0f));
		env.add(new DirectionalLight().set(0.6f, 0.1f, 0.2f, 0f, 0.5f, 0f));

		gameCamera = new PerspectiveCamera(50, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
		hudCamera = new OrthographicCamera(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
		vp = new FillViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
		gameCamera.position.set(CAMERA_POS);
		gameCamera.lookAt(CAMERA_LOOK);
		gameCamera.near = 1f;
		gameCamera.far = 100f;
		gameCamera.update();

		gameSprBatch = new SpriteBatch();
		ui = new SpriteBatch();
		bg = new Sprite(resizeTexture(new Texture("img/avgprog_tiled.png"), 950, 663));//resizeTexture(new Texture("img/avgprog64.png"), 241, 221));
		vig = new Sprite(new Texture("img/crt2.png"), 960, 680);
		vig.setScale(0.5f);
		pressStart = new Sprite(resizeTexture(new Texture("img/image.png"), 170, 170));

		startSprBatch = new SpriteBatch();
		copyright = new Sprite(resizeTexture(new Texture("img/copyright.png"), 400, 80));

		fadeoutSprBatch = new SpriteBatch();
		fade = new Sprite(new Texture("img/fade.png"), 1000,1000);

		fadeinSprBatch = new SpriteBatch();
		Texture starTexture = new Texture("img/star.png");
		star = new Sprite(resizeTexture(starTexture, starTexture.getWidth() * 2, starTexture.getHeight() * 2));

		//particleRenderer = new SpriteBatchParticleRenderer();
		/*TextureRegion tr = new TextureRegion(new Texture(Gdx.files.internal("img/sparkle1.png")));
		sparkleTa = new TextureAtlas();
		sparkleTa.addRegion("sparkle1", tr);*/

		//sparkleTa = //generateAtlas("sparkle", Gdx.files.internal("img/sparkle1.png"), Gdx.files.internal("img/sparkle2.png"), Gdx.files.internal("img/sparkle3.png"), Gdx.files.internal("img/sparkle4.png"), Gdx.files.internal("img/sparkle5.png"));
	//	sparkleP = new ParticleEffectDescriptor(Gdx.files.internal("vfx/sparkle.p"), sparkleTa);


		font = new BitmapFont(Gdx.files.internal("fonts/sm64.fnt"));
		font.getData().setScale(0.7f);
		font.setColor(Color.valueOf("#b9baf5"));
		avgprog = new GLTFLoader().load(Gdx.files.internal("models/ap/avgprog.gltf")).scene.model;
		avglogo = new GLTFLoader().load(Gdx.files.internal("models/logo/avglogo2.gltf")).scene.model;
		//jmtext = new GLTFLoader().load(Gdx.files.internal("models/javaman/jman.gltf")).scene.model;

		/*ggScene = new Scene(avgprog.scene);
		ggSceneManager = new SceneManager();
		ggSceneManager.addScene(ggScene);*/
		//model = mbuilder.createCylinder(5,10,5,10,///*mbuilder.createBox(5,5,5,*/mbuilder.createSphere(10f, 10f, 10f, 10, 10,
				/*new Material(ColorAttribute.createAmbientLight(Color.ROYAL)),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);*/

	    avgprogInst = new ModelInstance(avgprog);
		avgprogInst.transform.scale(0.001f, 0.001f, 0.001f);
		avglogoInst = new ModelInstance(avglogo);
		avglogoInst.transform.scale(0.009f, 0.009f, 0.009f);


		cic = new CameraInputController(gameCamera);
		Gdx.input.setInputProcessor(cic);

		nearestVert = null;
		nearestVerts = new Vector3[NEARVERT_COUNT];
		Mesh mesh = avgprog.meshes.get(0);
		float[] verts = mesh.getVertices(new float[mesh.getNumVertices() * mesh.getVertexSize() / 4]);
		short[] indices = new short[mesh.getNumIndices()];
		mesh.getIndices(indices);

		int vertSize = IntStream.range(0, mesh.getVertexAttributes().size()).map(i -> mesh.getVertexAttributes().get(i).numComponents).sum(); // optimised
		Vector3[] vertCoords = getVerts(verts, vertSize);

		vertAnims = new Pair[vertCoords.length];
		for (int i = 0; i < vertAnims.length; i++) {
			vertAnims[i] = new Pair<>(0f, null);
		}


		sfxGrab = Gdx.audio.newSound(Gdx.files.internal("audio/grab.mp3"));
		sfxMalleo = Gdx.audio.newSound(Gdx.files.internal("audio/its-a-me-mario.mp3"));
		sfxRelease = Gdx.audio.newSound(Gdx.files.internal("audio/release.mp3"));
		sfxEnter = Gdx.audio.newSound(Gdx.files.internal("audio/transition.mp3"));
		bgmSiivaTitle = Gdx.audio.newMusic(Gdx.files.internal("audio/siivatitle.mp3"));
		bgmSiivaTitle.setLooping(true);
		bgmSiivaTitle.setVolume(0.3f);

		handIdle = new Pixmap(Gdx.files.internal("img/hand1.png"));
		handActive = new Pixmap(Gdx.files.internal("img/hand2.png"));
		cursorIdle = Gdx.graphics.newCursor(handIdle, 13, 30);
		cursorActive = Gdx.graphics.newCursor(handActive, 13, 30);

		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(0, 0, 0, 0); // Set color to fully transparent
		pixmap.fill();
		Cursor empty = Gdx.graphics.newCursor(pixmap, 0, 0);

		Gdx.graphics.setCursor(empty);
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


		hudCamera.update();
		gameCamera.update();
		vp.apply();
		cic.update();


		if (isStart) {
			if (System.currentTimeMillis() - startTime > 1000) { // general delay
				if (!malleoHasPlayed) {
					sfxMalleo.play();
					malleoHasPlayed = true;
				}

				gameCamera.lookAt(0f, -0.2f, 0f);
				gameCamera.position.set(0f, -3.2f, 5f);

				startBatch = new ModelBatch();
				startBatch.begin(gameCamera);
				startBatch.render(avglogoInst, env);
				startBatch.end();

				startSprBatch.setProjectionMatrix(hudCamera.combined);
				startSprBatch.begin();
				startSprBatch.draw(copyright, -220f, -260f);
				startSprBatch.draw(vig, -VIEWPORT_WIDTH / 2f, -VIEWPORT_HEIGHT / 2f);
				startSprBatch.end();

				// fix: optimise code, preferably make it generic; no need for magic #s in LOGOSCALELIMIT, offsets, and base scale amt. Huge yikes. But it works!!1! :D
				if (logoScaleCount < LOGO_SCALE_LIMIT) {
					float scl = 1 + 0.9f * (float) EasingFunction.EASE_IN_SINE.getValue((double) logoScaleCount / LOGO_SCALE_LIMIT);
					avglogoInst.transform.scale(scl, scl, scl);
					logoScaleCount += 0.3f;
				} else if (logoScaleCount < LOGO_SCALE_LIMIT + 2) {
					float scl = 1 - 0.9f * (float) EasingFunction.EASE_IN_SINE.getValue((double) (logoScaleCount - LOGO_SCALE_LIMIT) / LOGO_SCALE_LIMIT);
					avglogoInst.transform.scale(scl, scl, scl);
					logoScaleCount += 0.3f;
				} else if (logoScaleCount < LOGO_SCALE_LIMIT + 4) {
					float scl = 1f + 0.6f * (float) EasingFunction.EASE_IN_SINE.getValue((double) (logoScaleCount - (LOGO_SCALE_LIMIT + 2)) / LOGO_SCALE_LIMIT);
					avglogoInst.transform.scale(scl, scl, scl);
					logoScaleCount += 0.3f;
				} else if (logoScaleCount < LOGO_SCALE_LIMIT + 6) {
					float scl = 1 - 0.5f * (float) EasingFunction.EASE_IN_SINE.getValue((double) (logoScaleCount - (LOGO_SCALE_LIMIT + 4)) / LOGO_SCALE_LIMIT);
					avglogoInst.transform.scale(scl, scl, scl);
					logoScaleCount += 0.3f;
				} else if (logoScaleCount < LOGO_SCALE_LIMIT + 8) {
					float scl = 1 + 0.1f * (float) EasingFunction.EASE_IN_SINE.getValue((double) (logoScaleCount - (LOGO_SCALE_LIMIT + 6)) / LOGO_SCALE_LIMIT);
					avglogoInst.transform.scale(scl, scl, scl);
					logoScaleCount += 0.3f;
				} else if (logoScaleCount < LOGO_SCALE_LIMIT + 10) {
					float scl = 1 - 0.05f * (float) EasingFunction.EASE_IN_SINE.getValue((double) (logoScaleCount - (LOGO_SCALE_LIMIT + 8)) / LOGO_SCALE_LIMIT);
					avglogoInst.transform.scale(scl, scl, scl);
					logoScaleCount += 0.3f;
				} else {
					if (System.currentTimeMillis() - startTime > 4500) {// delay for sfx finish
						isStartFadeOut = true;
					}
				}

				if (isStartFadeOut) {
					fadeAlpha += FADEOUT_SPEED * Gdx.graphics.getDeltaTime();
					if (fadeAlpha >= 1f) {
						fadeAlpha = 1f;
						isStart = false;
						startTime = System.currentTimeMillis(); // reset for later use
					}

					fadeoutSprBatch.begin();
					fadeoutSprBatch.setColor(0, 0, 0, fadeAlpha); // Set color with adjusted alpha
					fadeoutSprBatch.draw(fade, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
					fadeoutSprBatch.setColor(1, 1, 1, 1); // Reset color to default
					fadeoutSprBatch.end();
				}
			}
		}
		else {
			gameCamera.position.set(CAMERA_POS);
			gameCamera.lookAt(CAMERA_LOOK);
			Gdx.graphics.setCursor(cursorIdle);

			// MOVE THESE SO NOT UPDATING EVERY FRAME
			Mesh mesh = avgprog.meshes.get(0);

			float[] verts = mesh.getVertices(new float[mesh.getNumVertices() * mesh.getVertexSize() / 4]);
			short[] indices = new short[mesh.getNumIndices()];
			mesh.getIndices(indices);

			int vertSize = IntStream.range(0, mesh.getVertexAttributes().size()).map(i -> mesh.getVertexAttributes().get(i).numComponents).sum(); // optimised
			Vector3[] vertCoords = getVerts(verts, vertSize);

			// END MOVE


			// https://stackoverflow.com/questions/23106093/how-to-get-a-stream-from-a-float
			if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
				//sparkleI = new ParticleEffectInstance(sparkleP);

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

				Vector3 hit = new Vector3();/*
			System.out.println("Ray: " + ray);
			System.out.println("Verts: (" + verts.length + ") " + Arrays.toString(verts));
			System.out.println("Indices: (" + indices.length + ") " + Arrays.toString(indices));
			System.out.println("vert_size: " + mesh.getVertexSize() / 4);*/

				boolean isRaycast = Intersector.intersectRayTriangles(ray, verts, indices, mesh.getVertexSize() / 4, hit);


				if (isRaycast) {
					System.out.println(AnsiCode.ANSI_YELLOW + "HIT\n" + AnsiCode.ANSI_RESET);
					isVertActive = true;
				} else {
					// The ray did not hit the mesh
					System.out.println(AnsiCode.ANSI_WHITE + "NA\n" + AnsiCode.ANSI_RESET);
				}

				Vector3 checkVec = (isRaycast) ? hit : new Vector3(nearPoint.x, nearPoint.y, 1);
				int animIndex = -1;

				if (Arrays.stream(vertCoords).anyMatch(c -> getDistance(c, checkVec) < 1)) {
					for (int i = 0; i < vertCoords.length; i++) {
						Vector3 vert = vertCoords[i];
						if (!isRaycast) checkVec.z = vert.z; // Set z to same for effectively Vector2 comparison.
						if (nearestVert == null || getDistance(checkVec, nearestVert) > getDistance(checkVec, vert)) {
							nearestVert = vert;
							animIndex = i;
							isVertDragged = true;
						}

						Vector3[] vertArr = Arrays.copyOf(vertCoords, vertCoords.length);
						Arrays.stream(vertArr).sorted((v1, v2) -> {
							float distV1 = v1.dst2(nearestVert);
							float distV2 = v2.dst2(nearestVert);

							if (distV1 < distV2) {
								return -1;
							} else if (distV1 > distV2) {
								return 1;
							} else {
								return 0;
							}
						});

						List<Integer> vertsCoords = new ArrayList<>();
						for (int j = 0; j < vertCoords.length; j++) {
							for (int k = 0; k < NEARVERT_COUNT; k += 5) {
								if (vertCoords[j].equals(vertArr[k])) vertsCoords.add(j);
							}
						}

						for (int j = 0; j < NEARVERT_COUNT; j++) {
							nearestVerts[j] = vertArr[j];
						}

						for (Integer vc : vertsCoords) {
							vertAnims[vc] = vertAnims[vc].setAt1(vertCoords[vc]);
						}
					}

					//vertAnims[animIndex] = vertAnims[animIndex].setAt1(nearestVert.cpy());
					System.out.println("NEARVERT " + nearestVert); // debug
				} else isVertDragged = false;

			}
			//sparkleI = new ParticleEffectInstance(sparkleP);
			if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
				if (!isMouseDown) {
					sfxGrab.play();
					isMouseDown = true;
				}
				Gdx.graphics.setCursor(cursorActive);

				Vector3 wsLoc = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0.5f);
				gameCamera.unproject(wsLoc);

				System.out.println(nearestVert);
				if (isVertDragged) {
			/*	float newX = nearestVert.x + Math.max(nearestVert.x - MAX_VERT_DISP, Math.min(nearestVert.x + MAX_VERT_DISP, -nearestVert.x + wsLoc.x)); // clamp change to [-MVD, MVD].
				float newY = nearestVert.y + Math.max(nearestVert.y - MAX_VERT_DISP, Math.min(nearestVert.y + MAX_VERT_DISP, -nearestVert.y + wsLoc.y));*/

					Vector3 newVert = nearestVert.cpy().set(wsLoc.x * 0.3f, wsLoc.y * 0.3f, nearestVert.z);
					mesh.setVertices(modifyVertData(verts, vertSize, modifyVerts(vertCoords, nearestVert, newVert)));
					nearestVert = newVert;

					for (int i = 0; i < NEARVERT_COUNT; i++) {
					/*float nX = nearestVerts[i].x + Math.max(nearestVerts[i].x - MAX_VERT_DISP, Math.min(nearestVerts[i].x + MAX_VERT_DISP, nearestVert.x - wsLoc.x)); // clamp change to [-MVD, MVD].
					float nY = nearestVerts[i].y + Math.max(nearestVerts[i].y - MAX_VERT_DISP, Math.min(nearestVerts[i].y + MAX_VERT_DISP, nearestVert.y - wsLoc.y));
*/
						Vector3 nVert = nearestVerts[i].cpy().set(wsLoc.x * 0.4f, wsLoc.y * 0.4f, /*nearestVerts[i].x + (nearestVerts[i].x - wsLoc.x) * 0.3f, nearestVerts[i].y + (nearestVerts[i].y - wsLoc.y) * 0.3f,*/ nearestVerts[i].z);
						mesh.setVertices(modifyVertData(verts, vertSize, modifyVerts(vertCoords, nearestVerts[i], nVert)));
						nearestVerts[i] = nVert;
					}
					System.out.println("DONE");
				}

			} else {
				if (isMouseDown) {
					Gdx.graphics.setCursor(cursorIdle);
					sfxRelease.play();
					isVertDragged = false;
					isMouseDown = false;
				}


				for (int i = 0; i < vertAnims.length; i++) {
					Pair<Float, Vector3> anim = vertAnims[i];

					float t = anim.getValue0();
					Vector3 oldPos = anim.getValue1();

					if (t > 1) {
						oldPos = null;
						t = 0;
					}
					if (oldPos != null) {
						float interpT = (float) EasingFunction.EASE_IN_OUT_ELASTIC.getValue(t);
						Vector3 newVert = nearestVert.cpy().add((oldPos.x - nearestVert.x) * interpT, (oldPos.y - nearestVert.y) * interpT, (oldPos.z - nearestVert.z) * interpT);
						mesh.setVertices(modifyVertData(verts, vertSize, modifyVerts(vertCoords, nearestVert, newVert)));
						nearestVert = newVert;

						//System.out.println(i + ": " + t);
						t += 0.005f;
					}
					vertAnims[i] = new Pair<>(t, oldPos);
				}
			}


		/*sparkleI.update(delta);
		sparkleI.render(particleRenderer);*/
			gameSprBatch.setProjectionMatrix(hudCamera.combined);
			gameSprBatch.begin();

			gameSprBatch.draw(bg, -470f, -320f); // magic numbers!!! :D
			//drawTileBg(bg);
			gameSprBatch.end();

			//ggSceneManager.setAmbientLight(0.001f);
			//ggSceneManager.render();


			avgprogInst.transform.rotate(new Quaternion(Vector3.Y, -0.3f));

			mainBatch = new ModelBatch();
			mainBatch.begin(gameCamera);
			mainBatch.render(avgprogInst, env);
			mainBatch.end();

			ui.setProjectionMatrix(hudCamera.combined);
			ui.begin();
			//font.draw(ui, "average programmer", -300, 300);
			if (isVertActive) {
			/*font.setColor(Color.YELLOW);
			font.draw(ui, "HIT", 0, 300);
			font.setColor(Color.WHITE);*/
				isVertActive = false;
			}
			if ((frameCount / 95) % 2 == 1)
				ui.draw(pressStart, -430, -320);
			ui.draw(vig, -VIEWPORT_WIDTH / 2f, -VIEWPORT_HEIGHT / 2f);
			ui.end();

			if (frameCount % 2 == 0) {
				if (!enterHasPlayed) { // temp
					sfxEnter.play();
					bgmSiivaTitle.play();
					enterHasPlayed = true;
				}

				if (progScaleCount < PROG_SCALE_LIMIT) {
					float scl = 1 + 0.9f * (float) EasingFunction.EASE_IN_OUT_SINE.getValue((double) progScaleCount / PROG_SCALE_LIMIT);
					avgprogInst.transform.scale(scl, scl, scl);
					progScaleCount += 0.45f;
				} else if (progScaleCount < PROG_SCALE_LIMIT + 2) {
					float scl = 1 - 0.9f * (float) EasingFunction.EASE_IN_OUT_SINE.getValue((double) (progScaleCount - PROG_SCALE_LIMIT) / PROG_SCALE_LIMIT);
					avgprogInst.transform.scale(scl, scl, scl);
					progScaleCount += 0.45f;
				} else if (progScaleCount < PROG_SCALE_LIMIT + 4) {
					float scl = 1f + 0.6f * (float) EasingFunction.EASE_IN_OUT_SINE.getValue((double) (progScaleCount - (PROG_SCALE_LIMIT + 2)) / PROG_SCALE_LIMIT);
					avgprogInst.transform.scale(scl, scl, scl);
					progScaleCount += 0.45f;
				} else if (progScaleCount < PROG_SCALE_LIMIT + 6) {
					float scl = 1 - 0.5f * (float) EasingFunction.EASE_IN_OUT_SINE.getValue((double) (progScaleCount - (PROG_SCALE_LIMIT + 4)) / PROG_SCALE_LIMIT);
					avgprogInst.transform.scale(scl, scl, scl);
					progScaleCount += 0.45f;
				} else if (progScaleCount < PROG_SCALE_LIMIT + 8) {
					float scl = 1 + 0.1f * (float) EasingFunction.EASE_IN_OUT_SINE.getValue((double) (progScaleCount - (PROG_SCALE_LIMIT + 6)) / PROG_SCALE_LIMIT);
					avgprogInst.transform.scale(scl, scl, scl);
					progScaleCount += 0.45f;
				} else if (progScaleCount < PROG_SCALE_LIMIT + 10) {
					float scl = 1 - 0.05f * (float) EasingFunction.EASE_IN_OUT_SINE.getValue((double) (progScaleCount - (PROG_SCALE_LIMIT + 8)) / PROG_SCALE_LIMIT);
					avgprogInst.transform.scale(scl, scl, scl);
					progScaleCount += 0.45f;
				}
			}
			if (!isSceneFadeIn) {
				elapsedTime += Gdx.graphics.getDeltaTime();
				float t = Math.min(elapsedTime / duration, 1f); // Ensure t doesn't exceed 1

				scale = Interpolation.linear.apply(1.0f, targetScale, t);

				Texture starT = star.getTexture();
				fadeinSprBatch.begin();
				fadeinSprBatch.draw(starT, -80, 20, starT.getWidth() / 2, starT.getHeight() / 2,
						starT.getWidth(), starT.getHeight(), scale, scale, 0, 0, 0,
						starT.getWidth(), starT.getHeight(), false, false);
				fadeinSprBatch.end();
			}
			frameCount++;
		}
	}

	@Override
	public void dispose() {
		gameSprBatch.dispose();
		avgprog.dispose();
		ui.dispose();
		mainBatch.dispose();
		startBatch.dispose();
		startSprBatch.dispose();
		sfxGrab.dispose();
		fadeoutSprBatch.dispose();
		fadeinSprBatch.dispose();
		sfxMalleo.dispose();
		sfxRelease.dispose();
		sfxEnter.dispose();
		bgmSiivaTitle.dispose();
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
				gameSprBatch.draw(spr, i - VIEWPORT_WIDTH / 2f, j - VIEWPORT_HEIGHT / 2f);
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

	public float[] unwrapVecs(Vector3[] vecs) {
		float[] arr = new float[vecs.length * 3];
		int i = 0;

		for (Vector3 v : vecs) {
			arr[i] = v.x;
			arr[i+1] = v.y;
			arr[i+2] = v.z;
			i += 3;
		}

		return arr;
	}
	public Vector3[] getVerts(float[] vertData, int vertSize) { // note: vertArr is raw vert data, this func compiles them into just the vert locations!
		return IntStream.range(0, vertData.length / vertSize).map(i -> i * vertSize).mapToObj(i -> new Vector3(vertData[i], vertData[i+1], vertData[i+2])).collect(Collectors.toList()).toArray(new Vector3[0]);
	}

	public float[] modifyVertData(float[] vertData, int vertSize, Vector3[] verts) {
		float[] newData = unwrapVecs(verts);
		int j = 0;

		for (int i = 0; i < verts.length; i++) {
			vertData[i * vertSize] = verts[i].x;
			vertData[i * vertSize + 1] = verts[i].y;
			vertData[i * vertSize + 2] = verts[i].z;
		}

		return vertData;
	}

	// not very useful other than changing one vert
	public Vector3[] modifyVerts(Vector3[] verts, Vector3 oldVert, Vector3 newVert) {
        for (int i = 0; i < verts.length; i++) {
			if (verts[i].equals(oldVert)) verts[i] = newVert;
		}

		return verts;
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
		return getDistance(new Vector3(v1.x, v1.y, 0), new Vector3(v2.x, v2.y, 0));
	}
}
