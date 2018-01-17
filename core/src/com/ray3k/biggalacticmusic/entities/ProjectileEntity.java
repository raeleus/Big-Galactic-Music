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
import com.ray3k.biggalacticmusic.Core;
import com.ray3k.biggalacticmusic.Entity;
import com.ray3k.biggalacticmusic.SpineEntity;
import com.ray3k.biggalacticmusic.states.GameState;

public class ProjectileEntity extends SpineEntity {
    private float damage;
    private int split;
    private float life;
    private boolean penetration;

    public ProjectileEntity() {
        super(Core.DATA_PATH + "/spine/note.json", "animation");
        
        split = 0;
        life = -1;
        penetration = false;
        
        int choice = MathUtils.random(4);
        
        switch (choice) {
            case 0:
                getSkeleton().setSkin("1");
                break;
            case 1:
                getSkeleton().setSkin("2");
                break;
            case 2:
                getSkeleton().setSkin("3");
                break;
            case 3:
                getSkeleton().setSkin("4");
                break;
            case 4:
                getSkeleton().setSkin("5");
                break;
        }
    }

    @Override
    public void actSub(float delta) {
        if (getX() > GameState.GAME_WIDTH || getX() < 0.0f || getY() < 0.0f || getY() > GameState.GAME_HEIGHT) {
            setX(MathUtils.clamp(getX(), 0.0f, GameState.GAME_WIDTH));
            setY(MathUtils.clamp(getY(), 0.0f, GameState.GAME_HEIGHT));
            split();
            dispose();
        }
        
        for (Entity entity : GameState.entityManager.getEntities()) {
            if (entity instanceof PlatformEntity) {
                PlatformEntity platform = (PlatformEntity) entity;
                
                if (!penetration && platform.getSkeletonBounds().aabbIntersectsSkeleton(getSkeletonBounds())) {
                    split();
                    dispose();
                    break;
                }
            } else if (entity instanceof EnemyEntity) {
                EnemyEntity enemy = (EnemyEntity) entity;
                
                if (enemy.getHealth() > 0 && enemy.getSkeletonBounds().aabbIntersectsSkeleton(getSkeletonBounds())) {
                    
                    if (split == 0) {
                        enemy.damage(damage);
                    }
                    split();
                    dispose();
                    break;
                }
            }
        }
        
        if (life > 0) {
            life -= delta;
            if (life < 0) {
                dispose();
            }
        }
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

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public int getSplit() {
        return split;
    }

    public void setSplit(int split) {
        this.split = split;
    }
    
    private void split() {
        if (split > 0) {
            GameState.inst().playSound("shred", .5f);
        }
        
        for (int i = 0; i < split; i++) {
            ProjectileEntity projectile = new ProjectileEntity();
            projectile.setMotion(getSpeed() * MathUtils.random(.5f, 1.25f), MathUtils.random(360.0f));
            projectile.setDamage(damage);
            projectile.setPosition(getX(), getY());
            projectile.setLife(MathUtils.random(.4f, .6f));
            projectile.setPenetration(true);
            GameState.entityManager.addEntity(projectile);
        }
    }

    public float getLife() {
        return life;
    }

    public void setLife(float life) {
        this.life = life;
    }

    public boolean isPenetration() {
        return penetration;
    }

    public void setPenetration(boolean penetration) {
        this.penetration = penetration;
    }
}
