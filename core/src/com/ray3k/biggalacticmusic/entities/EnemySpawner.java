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
import com.ray3k.biggalacticmusic.Entity;
import com.ray3k.biggalacticmusic.states.GameState;

public class EnemySpawner extends Entity {
    private float rotation;
    private float timer;
    private static final float SPAWN_TIME_MIN = 5.0f;
    private static final float SPAWN_TIME_MAX = 7.0f;

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    @Override
    public void create() {
        timer = MathUtils.random(SPAWN_TIME_MIN, SPAWN_TIME_MAX);
    }

    @Override
    public void act(float delta) {
        timer -= delta;
        if (timer < 0) {
            timer = MathUtils.random(SPAWN_TIME_MIN / GameState.inst().getDifficulty(), SPAWN_TIME_MAX / GameState.inst().getDifficulty());
            
            EnemyEntity enemy = new EnemyEntity();
            enemy.setPosition(getX(), getY());
            if (MathUtils.isEqual(rotation, 180.0f)) {
                enemy.getSkeleton().setFlipX(true);
            }
            GameState.entityManager.addEntity(enemy);
        }
    }

    @Override
    public void actEnd(float delta) {
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }
}
