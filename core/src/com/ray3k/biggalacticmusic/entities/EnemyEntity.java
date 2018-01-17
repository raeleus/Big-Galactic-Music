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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.AnimationState;
import com.ray3k.biggalacticmusic.Core;
import com.ray3k.biggalacticmusic.Entity;
import com.ray3k.biggalacticmusic.SpineEntity;
import com.ray3k.biggalacticmusic.states.GameState;

public class EnemyEntity extends SpineEntity {
    private float health;
    private static final float MAX_HEALTH = 100.0f;
    private static final float ACCELERATION = 2500.0f;
    private static final float MAX_SPEED = 300.0f;
    private static final float GRAVITY = 3000.0f;
    
    public EnemyEntity() {
        super();
        setSkeletonData(Core.DATA_PATH + "/spine/alien.json", "run");
        getAnimationState().addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void complete(AnimationState.TrackEntry entry) {
                if (entry.getAnimation().getName().equals("beam")) {
                    EnemyEntity.this.dispose();
                }
            }
            
        });
    }

    @Override
    public void actSub(float delta) {
        if (getHealth() > 0) {
            processPhysics(delta);
        }
    }

    @Override
    public void drawSub(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void create() {
        health = MAX_HEALTH;
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
    
    private void processPhysics(float delta) {
        if (getSkeleton().getFlipX()) {
            addMotion(ACCELERATION * delta, 0.0f);
        } else {
            addMotion(ACCELERATION * delta, 180.0f);
        }
        
        setXspeed(MathUtils.clamp(getXspeed(), -MAX_SPEED, MAX_SPEED));
        
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
            getSkeleton().setFlipX(true);
        } else if (getSkeletonBounds().getMaxX() > GameState.GAME_WIDTH) {
            setXspeed(0.0f);
            addX(GameState.GAME_WIDTH - getSkeletonBounds().getMaxX());
            getSkeleton().setFlipX(false);
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
            dispose();
        }
    }

    public float getHealth() {
        return health;
    }

    public boolean setHealth(float health) {
        this.health = health;
        
        if (health <= 0) {
            getAnimationState().setAnimation(0, "dance", true);
            getAnimationState().setAnimation(1, "beam", false);
            setMotion(0.0f, 0.0f);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean damage(float health) {
        return setHealth(getHealth() - health);
    }
}
