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

import org.embulk.util.msgpack.core.MessageTypeCastException;
import org.embulk.util.msgpack.value.ImmutableArrayValue;
import org.embulk.util.msgpack.value.ImmutableBinaryValue;
import org.embulk.util.msgpack.value.ImmutableBooleanValue;
import org.embulk.util.msgpack.value.ImmutableExtensionValue;
import org.embulk.util.msgpack.value.ImmutableFloatValue;
import org.embulk.util.msgpack.value.ImmutableIntegerValue;
import org.embulk.util.msgpack.value.ImmutableMapValue;
import org.embulk.util.msgpack.value.ImmutableNilValue;
import org.embulk.util.msgpack.value.ImmutableNumberValue;
import org.embulk.util.msgpack.value.ImmutableRawValue;
import org.embulk.util.msgpack.value.ImmutableStringValue;
import org.embulk.util.msgpack.value.ImmutableValue;

abstract class AbstractImmutableValue
        implements ImmutableValue
{
    @Override
    public boolean isNilValue()
    {
        return getValueType().isNilType();
    }

    @Override
    public boolean isBooleanValue()
    {
        return getValueType().isBooleanType();
    }

    @Override
    public boolean isNumberValue()
    {
        return getValueType().isNumberType();
    }

    @Override
    public boolean isIntegerValue()
    {
        return getValueType().isIntegerType();
    }

    @Override
    public boolean isFloatValue()
    {
        return getValueType().isFloatType();
    }

    @Override
    public boolean isRawValue()
    {
        return getValueType().isRawType();
    }

    @Override
    public boolean isBinaryValue()
    {
        return getValueType().isBinaryType();
    }

    @Override
    public boolean isStringValue()
    {
        return getValueType().isStringType();
    }

    @Override
    public boolean isArrayValue()
    {
        return getValueType().isArrayType();
    }

    @Override
    public boolean isMapValue()
    {
        return getValueType().isMapType();
    }

    @Override
    public boolean isExtensionValue()
    {
        return getValueType().isExtensionType();
    }

    @Override
    public ImmutableNilValue asNilValue()
    {
        throw new MessageTypeCastException();
    }

    @Override
    public ImmutableBooleanValue asBooleanValue()
    {
        throw new MessageTypeCastException();
    }

    @Override
    public ImmutableNumberValue asNumberValue()
    {
        throw new MessageTypeCastException();
    }

    @Override
    public ImmutableIntegerValue asIntegerValue()
    {
        throw new MessageTypeCastException();
    }

    @Override
    public ImmutableFloatValue asFloatValue()
    {
        throw new MessageTypeCastException();
    }

    @Override
    public ImmutableRawValue asRawValue()
    {
        throw new MessageTypeCastException();
    }

    @Override
    public ImmutableBinaryValue asBinaryValue()
    {
        throw new MessageTypeCastException();
    }

    @Override
    public ImmutableStringValue asStringValue()
    {
        throw new MessageTypeCastException();
    }

    @Override
    public ImmutableArrayValue asArrayValue()
    {
        throw new MessageTypeCastException();
    }

    @Override
    public ImmutableMapValue asMapValue()
    {
        throw new MessageTypeCastException();
    }

    @Override
    public ImmutableExtensionValue asExtensionValue()
    {
        throw new MessageTypeCastException();
    }
}
