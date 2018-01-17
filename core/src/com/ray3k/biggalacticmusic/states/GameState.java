/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ray3k.biggalacticmusic.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import com.ray3k.biggalacticmusic.Core;
import com.ray3k.biggalacticmusic.Entity;
import com.ray3k.biggalacticmusic.EntityManager;
import com.ray3k.biggalacticmusic.InputManager;
import com.ray3k.biggalacticmusic.State;
import com.ray3k.biggalacticmusic.entities.EnemySpawner;
import com.ray3k.biggalacticmusic.entities.ItemEntity;
import com.ray3k.biggalacticmusic.entities.ItemSpawner;
import com.ray3k.biggalacticmusic.entities.PlatformEntity;
import com.ray3k.biggalacticmusic.entities.PlayerEntity;

public class GameState extends State {
    private static GameState instance;
    private int score;
    private static int highscore = 0;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    private Table table;
    private Label scoreLabel;
    public static EntityManager entityManager;
    public static TextureAtlas spineAtlas;
    public static final float GAME_WIDTH = 800.0f;
    public static final float GAME_HEIGHT = 600.0f;
    private TextureRegion background;
    private Array<ItemSpawner> itemSpawners;
    private float difficulty;
    private Music music;
    
    public static GameState inst() {
        return instance;
    }
    
    public GameState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        difficulty = 1;
        instance = this;
        
        spineAtlas = Core.assetManager.get(Core.DATA_PATH + "/spine/Big Galactic Music.atlas", TextureAtlas.class);
        
        score = 0;
        
        inputManager = new InputManager();
        
        gameCamera = new OrthographicCamera();
        gameViewport = new StretchViewport(GameState.GAME_WIDTH, GameState.GAME_HEIGHT, gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getWidth(), true);
        gameViewport.apply();
        
        gameCamera.position.set(gameCamera.viewportWidth / 2, gameCamera.viewportHeight / 2, 0);
        
        skin = Core.assetManager.get(Core.DATA_PATH + "/ui/Big Galactic Music.json", Skin.class);
        stage = new Stage(new StretchViewport(GameState.GAME_WIDTH, GameState.GAME_HEIGHT));
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        entityManager = new EntityManager();
        
        createStageElements();
        
        background = spineAtlas.findRegion("background");
        
        playMusic();
        
        itemSpawners = new Array<ItemSpawner>();

        loadLevel();
        
        spawnItem();
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        scoreLabel = new Label("0", skin);
        root.add(scoreLabel).expandY().padTop(25.0f).top().expandX();
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(57.0f / 255.0f, 114.0f / 255.0f, 85.0f / 255.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        gameCamera.update();
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        spriteBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        spriteBatch.draw(background, 0.0f, 0.0f);
        entityManager.draw(spriteBatch, delta);
        spriteBatch.end();
        spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        stage.draw();
    }

    @Override
    public void act(float delta) {
        difficulty += .025 * delta;
        entityManager.act(delta);
        
        stage.act(delta);
        
        if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
            Core.stateManager.loadState("menu");
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void stop() {
        stopMusic();
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        scoreLabel.setText(Integer.toString(score));
        if (score > highscore) {
            highscore = score;
        }
    }
    
    public void addScore(int score) {
        this.score += score;
        scoreLabel.setText(Integer.toString(this.score));
        if (this.score > highscore) {
            highscore = this.score;
        }
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public void setGameCamera(OrthographicCamera gameCamera) {
        this.gameCamera = gameCamera;
    }

    public Skin getSkin() {
        return skin;
    }

    public Stage getStage() {
        return stage;
    }
    
    public void playSound(String name) {
        playSound(name, 1.0f, 1.0f);
    }
    
    public void playSound (String name, float volume) {
        playSound(name, volume, 1.0f);
    }
    
    /**
     * 
     * @param name
     * @param volume
     * @param pitch .5 to 2. 1 is default
     */
    public void playSound(String name, float volume, float pitch) {
        Core.assetManager.get(Core.DATA_PATH + "/sfx/" + name + ".wav", Sound.class).play(volume, pitch, 0.0f);
    }
    
    public void playMusic() {
        FileHandle fileHandle = Gdx.files.local(Core.DATA_PATH + "/bgm/");
        Array<FileHandle> files = new Array<FileHandle>(fileHandle.list());
        
        music = Gdx.audio.newMusic(files.random());
        music.setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                playMusic();
            }
        });
        music.setVolume(.5f);
        music.play();
    }
    
    public void stopMusic() {
        music.stop();
    }
    
    public void loadLevel() {
        SkeletonData skeletonData = Core.assetManager.get(Core.DATA_PATH + "/spine/level1.json", SkeletonData.class);
        Skeleton skeleton = new Skeleton(skeletonData);
        
        for (Slot slot : skeleton.getSlots()) {
            String name = slot.getAttachment().getName();
            RegionAttachment region = ((RegionAttachment) slot.getAttachment());
            Entity entity = null;
            
            if (name.equals("player")) {
                entity = new PlayerEntity();
                entity.setPosition(region.getX(), region.getY() - 45.0f);
            } else if (name.equals("alien")) {
                entity = new EnemySpawner();
                ((EnemySpawner) entity).setRotation(region.getRotation());
                entity.setPosition(region.getX(), region.getY());
            } else if (name.equals("block-brick")) {
                entity = new PlatformEntity(PlatformEntity.Type.BRICK);
                entity.setPosition(region.getX(), region.getY());
            } else if (name.equals("block-metal")) {
                entity = new PlatformEntity(PlatformEntity.Type.METAL);
                entity.setPosition(region.getX(), region.getY());
            } else if (name.equals("platform")) {
                entity = new PlatformEntity(PlatformEntity.Type.PLATFORM);
                entity.setPosition(region.getX(), region.getY());
            } else if (name.equals("guitar")) {
                entity = new ItemSpawner();
                entity.setPosition(region.getX(), region.getY());
                itemSpawners.add((ItemSpawner) entity);
            }
            
            if (entity != null) {
                GameState.entityManager.addEntity(entity);
            }
        }
    }
    
    public void spawnItem() {
        ItemSpawner spawner = itemSpawners.random();
        
        ItemEntity item = new ItemEntity();
        item.setPosition(spawner.getX(), spawner.getY());
        entityManager.addEntity(item);
    }

    public float getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }
}