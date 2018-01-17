/*
 * The MIT License
 *
 * Copyright 2018 Raymond Buckley.
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

package com.ray3k.biggalacticmusic.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationState.TrackEntry;
import com.esotericsoftware.spine.attachments.PointAttachment;
import com.ray3k.biggalacticmusic.Core;
import com.ray3k.biggalacticmusic.Entity;
import com.ray3k.biggalacticmusic.SpineEntity;
import com.ray3k.biggalacticmusic.states.GameState;

public class PlayerEntity extends SpineEntity {
    private static final float FRICTION = 3000.0f;
    private static final float ACCELERATION = 2500.0f;
    private static final float MAX_SPEED = 600.0f;
    private static final float JUMP_SPEED = 1000.0f;
    private static final float GRAVITY = 3000.0f;
    private static final float SAX_VARIANCE = 35.0f;
    
    public static enum WeaponType {
        GUITAR, SAX, FLUTE
    }
    private WeaponType weapon;
    private float weaponTimer;
    private static final Vector2 temp = new Vector2();

    public PlayerEntity() {
        super(Core.DATA_PATH + "/spine/player.json", "stand");
    }

    @Override
    public void actSub(float delta) {
        if (!getAnimationState().getCurrent(0).getAnimation().getName().equals("die")) {
            weaponTimer -= delta;
            if (Gdx.input.isKeyPressed(Keys.SPACE)) {
                if (weaponTimer < 0) {
                    PointAttachment point = (PointAttachment) getSkeleton().findSlot("shooter").getAttachment();
                    point.computeWorldPosition(getSkeleton().findBone("shooter"), temp);
                    switch (weapon) {
                        case GUITAR: {
                            TrackEntry track = getAnimationState().setAnimation(1, "guitar-shoot", false);
                            track.setAlpha(1.0f);
                            track.setMixDuration(0.0f);
                            GameState.inst().playSound("guitar", 1.0f, MathUtils.random(.75f, 1.25f));
                            weaponTimer = 1.5f;
                            ProjectileEntity projectile = new ProjectileEntity();
                            projectile.setPosition(temp.x, temp.y);
                            if (!getSkeleton().getFlipX()) {
                                projectile.setMotion(450.0f, 0.0f);
                            } else {
                                projectile.setMotion(450.0f, 180.0f);
                            }
                            projectile.setDamage(100.0f);
                            projectile.setSplit(50);
                            GameState.entityManager.addEntity(projectile);
                            break;
                        }
                        case FLUTE: {
                            TrackEntry track = getAnimationState().setAnimation(1, "flute-shoot", false);
                            track.setAlpha(1.0f);
                            track.setMixDuration(0.0f);
                            GameState.inst().playSound("flute", .19f, MathUtils.random(1.0f, 2.0f));
                            weaponTimer = .2f;
                            ProjectileEntity projectile = new ProjectileEntity();
                            projectile.setPosition(temp.x, temp.y);
                            if (!getSkeleton().getFlipX()) {
                                projectile.setMotion(900.0f, 0.0f);
                            } else {
                                projectile.setMotion(900.0f, 180.0f);
                            }
                            projectile.setDamage(50.0f);
                            GameState.entityManager.addEntity(projectile);
                            break;
                        }
                        case SAX: {
                            TrackEntry track = getAnimationState().setAnimation(1, "sax-shoot", false);
                            track.setAlpha(1.0f);
                            track.setMixDuration(0.0f);
                            GameState.inst().playSound("sax", .50f, MathUtils.random(.5f, 1.0f));
                            weaponTimer = .7f;
                            
                            for (int i = 0; i < 10; i++) {
                                ProjectileEntity projectile = new ProjectileEntity();
                                projectile.setPosition(temp.x, temp.y);
                                if (!getSkeleton().getFlipX()) {
                                    projectile.setMotion(MathUtils.random(500.0f, 700.0f),  -SAX_VARIANCE / 2.0f + MathUtils.random(SAX_VARIANCE));
                                } else {
                                    projectile.setMotion(MathUtils.random(500.0f, 700.0f), 180.0f - SAX_VARIANCE / 2.0f + MathUtils.random(SAX_VARIANCE));
                                }
                                projectile.setDamage(20.0f);
                                projectile.setLife(.5f);
                                GameState.entityManager.addEntity(projectile);
                            }
                            break;
                        }
                    }
                }
            }
            
            if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
                addMotion(ACCELERATION * delta, 0.0f);
                getSkeleton().setFlipX(false);
                if (!getAnimationState().getCurrent(0).getAnimation().getName().equals("run")) {
                    getAnimationState().setAnimation(0, "run", true);
                }
            } else if (Gdx.input.isKeyPressed(Keys.LEFT)) {
                addMotion(ACCELERATION * delta, 180.0f);
                getSkeleton().setFlipX(true);
                if (!getAnimationState().getCurrent(0).getAnimation().getName().equals("run")) {
                    getAnimationState().setAnimation(0, "run", true);
                }
            } else {
                if (getXspeed() > 0.0f) {
                    setXspeed(getXspeed() - FRICTION * delta);
                    if (getXspeed() < 0.0f) {
                        setXspeed(0.0f);
                    }
                } else if (getXspeed() < 0.0f) {
                    setXspeed(getXspeed() + FRICTION * delta);
                    if (getXspeed() > 0.0f) {
                        setXspeed(0.0f);
                    }
                }

                if (!getAnimationState().getCurrent(0).getAnimation().getName().equals("stand")) {
                    getAnimationState().setAnimation(0, "stand", true);
                }
            }

            if (getXspeed() > MAX_SPEED) {
                setXspeed(MAX_SPEED);
            } else if (getXspeed() < -MAX_SPEED) {
                setXspeed(-MAX_SPEED);
            }

            if (Gdx.input.isKeyPressed(Keys.UP) && isBottomClear()) {
                addMotion(JUMP_SPEED, 90.0f);
            }

            addMotion(GRAVITY * delta, 270.0f);

            float originalX = getX();
            float originalY = getY();

            setX(originalX + getXspeed() * delta);
            setY(originalY + getYspeed() * delta);

            getSkeleton().setPosition(getX(), getY());
            getSkeleton().updateWorldTransform();
            getSkeletonBounds().update(getSkeleton(), true);

            setPosition(originalX, originalY);

            if (getSkeletonBounds().getMinX() < 0.0f) {
                setXspeed(0.0f);
                addX(-getSkeletonBounds().getMinX());
            } else if (getSkeletonBounds().getMaxX() > GameState.GAME_WIDTH) {
                setXspeed(0.0f);
                addX(GameState.GAME_WIDTH - getSkeletonBounds().getMaxX());
            }

            if (getSkeletonBounds().getMaxY() > GameState.GAME_HEIGHT) {
                setYspeed(0.0f);
                addY(GameState.GAME_HEIGHT - getSkeletonBounds().getMaxY());
            }

            for (Entity entity : GameState.entityManager.getEntities()) {
                if (entity instanceof PlatformEntity) {
                    PlatformEntity platform = (PlatformEntity) entity;
                    if (platform.getSkeletonBounds().aabbIntersectsSkeleton(getSkeletonBounds())) {
                        getSkeleton().setPosition(originalX + getXspeed() * delta, originalY);
                        getSkeleton().updateWorldTransform();
                        getSkeletonBounds().update(getSkeleton(), true);

                        if (!platform.getSkeletonBounds().aabbIntersectsSkeleton(getSkeletonBounds())) {
                            if (getYspeed() < 0) {
                                addY(platform.getSkeletonBounds().getMaxY() - getSkeletonBounds().getMinY());
                            } else {
                                addY(platform.getSkeletonBounds().getMinY() - getSkeletonBounds().getMaxY());
                            }
                            setYspeed(0.0f);
                            break;
                        }

                        getSkeleton().setPosition(originalX, originalY + getYspeed() * delta);
                        getSkeleton().updateWorldTransform();
                        getSkeletonBounds().update(getSkeleton(), true);

                        if (!platform.getSkeletonBounds().aabbIntersectsSkeleton(getSkeletonBounds())) {
                            if (getXspeed() < 0) {
                                addX(platform.getSkeletonBounds().getMaxX() - getSkeletonBounds().getMinX());
                            } else {
                                addX(platform.getSkeletonBounds().getMinX() - getSkeletonBounds().getMaxX());
                            }
                            setXspeed(0.0f);
                            break;
                        }
                    }
                }
            }

            getSkeleton().setPosition(getX(), getY());
            getSkeleton().updateWorldTransform();
            getSkeletonBounds().update(getSkeleton(), true);

            if (getSkeletonBounds().getMaxY() < 0.0f) {
                GameState.entityManager.addEntity(new GameOverTimerEntity(2.0f));
                GameState.inst().playSound("lose");
                dispose();
            }

            for (Entity entity : GameState.entityManager.getEntities()) {
                if (entity instanceof EnemyEntity) {
                    EnemyEntity enemy = (EnemyEntity) entity;

                    if (enemy.getHealth() > 0 && enemy.getSkeletonBounds().aabbIntersectsSkeleton(getSkeletonBounds())) {
                        GameState.inst().playSound("lose");
                        getAnimationState().setAnimation(0, "die", false);
                    }
                } else if (entity instanceof ItemEntity) {
                    ItemEntity item = (ItemEntity) entity;
                    
                    if (item.getSkeletonBounds().aabbIntersectsSkeleton(getSkeletonBounds())) {
                        item.dispose();
                        GameState.inst().spawnItem();
                        GameState.inst().addScore(1);
                        
                        Array<WeaponType> weapons = new Array<WeaponType>(WeaponType.values());
                        weapons.removeValue(weapon, false);
                        setWeapon(weapons.random());
                    }
                }
            }
        }
    }
    
    public boolean isBottomClear() {
        for (Entity entity : GameState.entityManager.getEntities()) {
            if (entity instanceof PlatformEntity) {
                float x1 = getSkeletonBounds().getMinX();
                float y1 = getSkeletonBounds().getMinY() - 1;
                float x2 = getSkeletonBounds().getMaxX();
                float y2 = getSkeletonBounds().getMinY() - 1;
                
                if (((PlatformEntity) entity).getSkeletonBounds().aabbIntersectsSegment(x1, y1, x2, y2)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    public void drawSub(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void create() {
        getAnimationState().addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void complete(AnimationState.TrackEntry entry) {
                if (entry.getAnimation().getName().equals("die")) {
                    GameState.entityManager.addEntity(new GameOverTimerEntity(2.0f));
                }
            }
            
        });
        setWeapon(WeaponType.FLUTE);
    }

    @Override
    public void actEnd(float delta) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }

    public WeaponType getWeapon() {
        return weapon;
    }

    public void setWeapon(WeaponType weapon) {
        this.weapon = weapon;
        switch (weapon) {
            case GUITAR:
                GameState.inst().playSound("guitar");
                getAnimationState().setAnimation(1, "guitar", true);
                break;
            case FLUTE:
                GameState.inst().playSound("flute");
                getAnimationState().setAnimation(1, "flute", true);
                break;
            case SAX:
                GameState.inst().playSound("sax");
                getAnimationState().setAnimation(1, "sax", true);
                break;
        }
    }

}
