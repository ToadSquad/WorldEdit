/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.extent;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.function.operation.Operation;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implements "fast mode" which may skip physics, lighting, etc.
 */
public class FastModeExtent extends ExtentDelegate {

    private final LocalWorld world;
    private final Set<BlockVector2D> dirtyChunks = new HashSet<BlockVector2D>();
    private boolean enabled = true;

    /**
     * Create a new instance with fast mode enabled.
     *
     * @param world the world
     */
    public FastModeExtent(LocalWorld world) {
        this(world, true);
    }

    /**
     * Create a new instance.
     *
     * @param world the world
     * @param enabled true to enable fast mode
     */
    public FastModeExtent(LocalWorld world, boolean enabled) {
        super(world);
        checkNotNull(world);
        this.world = world;
        this.enabled = enabled;
    }

    /**
     * Return whether fast mode is enabled.
     *
     * @return true if fast mode is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set fast mode enable status.
     *
     * @param enabled true to enable fast mode
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean setBlock(Vector location, BaseBlock block) throws WorldEditException {
        dirtyChunks.add(new BlockVector2D(location.getBlockX() >> 4, location.getBlockZ() >> 4));
        return world.setBlock(location, block, !enabled);
    }

    @Override
    protected Operation commitBefore() {
        if (dirtyChunks.size() > 0) {
            return new Operation() {
                @Override
                public Operation resume() throws WorldEditException {
                    world.fixAfterFastMode(dirtyChunks);
                    return null;
                }

                @Override
                public void cancel() {
                }
            };
        } else {
            return null;
        }
    }

}
