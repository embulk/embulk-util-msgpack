/*
 * This file is based on a copy from MessagePack for Java v0.8.24 with modification on :
 * - moving its Java package to org.embulk.util.msgpack.value.
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
package org.embulk.util.msgpack.value;

/**
 * Representation of MessagePack types.
 * <p>
 * MessagePack uses hierarchical type system. Integer and Float are subypte of Number, Thus {@link #isNumberType()}
 * returns true if type is Integer or Float. String and Binary are subtype of Raw. Thus {@link #isRawType()} returns
 * true if type is String or Binary.
 *
 * @see org.embulk.util.msgpack.core.MessageFormat
 */
public enum ValueType
{
    NIL(false, false),
    BOOLEAN(false, false),
    INTEGER(true, false),
    FLOAT(true, false),
    STRING(false, true),
    BINARY(false, true),
    ARRAY(false, false),
    MAP(false, false),
    EXTENSION(false, false);

    private final boolean numberType;
    private final boolean rawType;

    private ValueType(boolean numberType, boolean rawType)
    {
        this.numberType = numberType;
        this.rawType = rawType;
    }

    public boolean isNilType()
    {
        return this == NIL;
    }

    public boolean isBooleanType()
    {
        return this == BOOLEAN;
    }

    public boolean isNumberType()
    {
        return numberType;
    }

    public boolean isIntegerType()
    {
        return this == INTEGER;
    }

    public boolean isFloatType()
    {
        return this == FLOAT;
    }

    public boolean isRawType()
    {
        return rawType;
    }

    public boolean isStringType()
    {
        return this == STRING;
    }

    public boolean isBinaryType()
    {
        return this == BINARY;
    }

    public boolean isArrayType()
    {
        return this == ARRAY;
    }

    public boolean isMapType()
    {
        return this == MAP;
    }

    public boolean isExtensionType()
    {
        return this == EXTENSION;
    }
}
