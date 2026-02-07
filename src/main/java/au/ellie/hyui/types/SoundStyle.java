/*
 *     Copyright (C) 2026 EllieAU
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package au.ellie.hyui.types;

import au.ellie.hyui.utils.BsonDocumentHelper;

/**
 * SoundStyle type definition.
 */
public class SoundStyle implements HyUIBsonSerializable {
    private String soundPath;
    private Float minPitch;
    private Float maxPitch;
    private Float volume;

    public SoundStyle withSoundPath(String soundPath) {
        this.soundPath = soundPath;
        return this;
    }

    public SoundStyle withMinPitch(float minPitch) {
        this.minPitch = minPitch;
        return this;
    }

    public SoundStyle withMaxPitch(float maxPitch) {
        this.maxPitch = maxPitch;
        return this;
    }

    public SoundStyle withVolume(float volume) {
        this.volume = volume;
        return this;
    }

    @Override
    public void applyTo(BsonDocumentHelper doc) {
        if (soundPath != null) doc.set("SoundPath", soundPath);
        if (minPitch != null) doc.set("MinPitch", minPitch.doubleValue());
        if (maxPitch != null) doc.set("MaxPitch", maxPitch.doubleValue());
        if (volume != null) doc.set("Volume", volume.doubleValue());
    }
}
