/*
 * This file is based on a copy from MessagePack for Java v0.8.24 with modification on :
 * - moving its Java package to org.embulk.util.msgpack.value.impl.
 *
 * It is licensed under the Apache License, Version 2.0.
 */

//
// MessagePack for Java
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package org.embulk.util.msgpack.value.impl;

import org.embulk.util.msgpack.core.MessagePacker;
import org.embulk.util.msgpack.value.ImmutableNilValue;
import org.embulk.util.msgpack.value.Value;
import org.embulk.util.msgpack.value.ValueType;

import java.io.IOException;

/**
 * {@code ImmutableNilValueImpl} Implements {@code ImmutableNilValue}.
 *
 * This class is a singleton. {@code ImmutableNilValueImpl.get()} is the only instances of this class.
 *
 * @see org.embulk.util.msgpack.value.NilValue
 */
public class ImmutableNilValueImpl
        extends AbstractImmutableValue
        implements ImmutableNilValue
{
    private static ImmutableNilValue instance = new ImmutableNilValueImpl();

    public static ImmutableNilValue get()
    {
        return instance;
    }

    private ImmutableNilValueImpl()
    {
    }

    @Override
    public ValueType getValueType()
    {
        return ValueType.NIL;
    }

    @Override
    public ImmutableNilValue immutableValue()
    {
        return this;
    }

    @Override
    public ImmutableNilValue asNilValue()
    {
        return this;
    }

    @Override
    public void writeTo(MessagePacker pk)
            throws IOException
    {
        pk.packNil();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Value)) {
            return false;
        }
        return ((Value) o).isNilValue();
    }

    @Override
    public int hashCode()
    {
        return 0;
    }

    @Override
    public String toString()
    {
        return toJson();
    }

    @Override
    public String toJson()
    {
        return "null";
    }
}
