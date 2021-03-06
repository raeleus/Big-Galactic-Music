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
import com.ray3k.biggalacticmusic.Core;
import com.ray3k.biggalacticmusic.Entity;
import com.ray3k.biggalacticmusic.SpineEntity;
import com.ray3k.biggalacticmusic.states.GameState;

public class ItemEntity extends SpineEntity {
    private static final float GRAVITY = 3000.0f;

    public ItemEntity() {
        super(Core.DATA_PATH + "/spine/box.json", "animation");
    }

    @Override
    public void actSub(float delta) {
        addMotion(GRAVITY * delta, 270.0f);
        
        float originalX = getX();
            float originalY = getY();

            setX(originalX + getXspeed() * delta);
            setY(originalY + getYspeed() * delta);

            getSkeleton().setPosition(getX(), getY());
            getSkeleton().updateWorldTransform();
            getSkeletonBounds().update(getSkeleton(), true);

            setPosition(originalX, originalY);

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
                    }
                }
            }

            getSkeleton().setPosition(getX(), getY());
            getSkeleton().updateWorldTransform();
            getSkeletonBounds().update(getSkeleton(), true);
    }

    @Override
    public void drawSub(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void create() {
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

}
