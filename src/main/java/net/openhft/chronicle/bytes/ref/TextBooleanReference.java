/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.chronicle.bytes.ref;

import net.openhft.chronicle.bytes.Byteable;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.core.util.WeakReferenceCleaner;
import net.openhft.chronicle.core.values.BooleanValue;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of a reference to a 32-bit in in text wire format.
 */
public class TextBooleanReference implements BooleanValue, Byteable {

    private BytesStore bytes;
    private long offset;

    private static final int FALSE = 'f' | ('a' << 8) | ('l' << 16) | ('s' << 24);
    private static final int TRUE = ' ' | ('t' << 8) | ('r' << 16) | ('u' << 24);

    private final StoreRef ref = new StoreRef();

    public TextBooleanReference()
    {
        WeakReferenceCleaner.newCleaner(this, ref::clean);
    }

    @Override
    public void bytesStore(@NotNull BytesStore bytes, long offset, long length) {
        acceptNewBytesStore(bytes);
        this.offset = offset;
    }

    @Override
    public BytesStore bytesStore() {
        return bytes;
    }

    @Override
    public long offset() {
        return offset;
    }

    @Override
    public long maxSize() {
        return 5;
    }

    @NotNull
    public String toString() {
        return "value: " + getValue();
    }

    @Override
    public boolean getValue() {
        return bytes.readVolatileInt(offset) == TRUE;
    }

    @Override
    public void setValue(final boolean value) {
        write(value, bytes, offset);
    }

    public static void write(final boolean value, final BytesStore bytes, long offset) {
        bytes.writeVolatileInt(offset, value ? TRUE : FALSE);
        bytes.writeByte(offset + 4, (byte) 'e');
    }

    private void acceptNewBytesStore(final BytesStore bytes) {
        if (this.bytes != null) {
            this.bytes.release();
        }
        this.bytes = bytes.bytesStore();
        ref.b = this.bytes;
        this.bytes.reserve();
    }
}
