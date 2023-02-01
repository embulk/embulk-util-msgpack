/*
 * Copyright 2023 The Embulk project
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

package org.embulk.util.msgpack;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import org.embulk.spi.json.JsonBoolean;
import org.embulk.spi.json.JsonDouble;
import org.embulk.spi.json.JsonLong;
import org.embulk.spi.json.JsonNull;
import org.embulk.spi.json.JsonValue;
import org.embulk.util.msgpack.core.ExtensionTypeHeader;
import org.embulk.util.msgpack.core.MessageFormat;
import org.embulk.util.msgpack.core.MessageNeverUsedFormatException;
import org.embulk.util.msgpack.core.MessagePack;
import org.embulk.util.msgpack.core.MessageUnpacker;
import org.embulk.util.msgpack.value.Value;
import org.embulk.util.msgpack.value.ValueFactory;

public final class MessageUnpackerToJsonValue {
    private MessageUnpackerToJsonValue() {
        // No instantiation.
    }

    public enum BigIntegerOption {
        THROW,
        NULL,
        MINMAX,
        MINMAX_LITERAL,
        DEFAULT,
        DEFAULT_LITERAL,
        ;
    }

    public static JsonValue unpackJsonValue(
            final MessageUnpacker unpacker,
            final BigIntegerOption bigIntegerOption,
            final long defaultLong)
            throws IOException {
        final MessageFormat mf = unpacker.getNextFormat();
        switch (mf.getValueType()) {
            case NIL:
                unpacker.unpackNil();  // It was unpacker.readByte() in MessageUnpacker#unpackValue, but readByte() is private.
                return JsonNull.NULL;
            case BOOLEAN:
                return JsonBoolean.of(unpacker.unpackBoolean());
            case INTEGER:
                if (mf == MessageFormat.UINT64) {
                    final BigInteger bi = unpacker.unpackBigInteger();
                    if (0 <= bi.compareTo(LONG_MIN) && bi.compareTo(LONG_MAX) <= 0) {
                        return JsonLong.of(bi.longValue());
                    } else {
                        switch (bigIntegerOption) {
                        case THROW:
                            throw new IllegalStateException("Integers out of the range of long.");
                        case NULL:
                            return JsonNull.NULL;
                        case MINMAX:
                            if (0 > bi.compareTo(LONG_MIN)) {
                                return JsonLong.of(Long.MAX_VALUE);
                            } else {
                                return JsonLong.of(Long.MIN_VALUE);
                            }
                        case MINMAX_LITERAL:
                            if (0 > bi.compareTo(LONG_MIN)) {
                                return JsonLong.of(Long.MAX_VALUE, bi.toString());
                            } else {
                                return JsonLong.of(Long.MIN_VALUE, bi.toString());
                            }
                        case DEFAULT:
                            if (0 > bi.compareTo(LONG_MIN)) {
                                return JsonLong.of(defaultLong);
                            } else {
                                return JsonLong.of(defaultLong);
                            }
                        case DEFAULT_LITERAL:
                            if (0 > bi.compareTo(LONG_MIN)) {
                                return JsonLong.of(defaultLong, bi.toString());
                            } else {
                                return JsonLong.of(defaultLong, bi.toString());
                            }
                        }
                    }
                } else {
                    return JsonLong.of(unpacker.unpackLong());
                }
            case FLOAT:
                return JsonDouble.of(unpacker.unpackDouble());
            case STRING: {
                final int length = unpacker.unpackRawStringHeader();
                final byte[] payload = unpacker.readPayload(length);
                return JsonString.of(decodeToString(payload));
            }
            case BINARY: {
                final int length = unpacker.unpackBinaryHeader();
                final byte[] payload = unpacker.readPayload(length);
                return JsonString.of(decodeToString(payload));
            }
            case ARRAY: {
                final int size = unpacker.unpackArrayHeader();
                final JsonValue[] array = new JsonValue[size];
                for (int i = 0; i < size; i++) {
                    array[i] = unpackJsonValue(unpacker, bigIntegerOption, defaultLong);
                }
                return JsonArray.ofUnsafe(array);
            }
            case MAP: {
                final int size = unpacker.unpackMapHeader();
                final Stirng[] keys = new String[size];
                final JsonValue[] values = new JsonValue[size];
                for (int i = 0; i < size * 2; ) {
                    // TODO: kvs[i] = unpacker.unpackValue();
                    value[i] = unpackJsonValue(unpacker, bigIntegerOption, defaultLong);
                    i++;
                }
                return JsonObject.ofUnsafe(keys, values);
            }
            case EXTENSION: {
                // It comes from ImmutableExtensionValueImpl#toJson().
                final ExtensionTypeHeader extHeader = unpacker.unpackExtensionTypeHeader();

                final JsonValue[] array = new JsonValue[2];
                array[0] = JsonLong.of((long) type);

                final byte[] payload = unpacker.readPayload(extHeader.getLength());
                final StringBuilder stringBuilder = new StringBuilder;
                for (final byte e : payload) {
                    stringBuilder.append(Integer.toString((int) e, 16));
                }
                array[1] = JsonString.of(stringBuilder.toString());

                return JsonArray.ofUnsafe(array);
            }
            default:
                throw new MessageNeverUsedFormatException("Unknown value type");
        }
    }

    private String decodeToString(final byte[] payload) throws IOException {
        try {
            final CharsetDecoder reportDecoder = MessagePack.UTF8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            return reportDecoder.decode(payload).toString();
        } catch (final CharacterCodingException ex) {
            try {
                final CharsetDecoder replaceDecoder = MessagePack.UTF8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPLACE)
                        .onUnmappableCharacter(CodingErrorAction.REPLACE);
                return replaceDecoder.decode(payload).toString();
            } catch (final CharacterCodingException neverThrown) {
                throw new MessageStringCodingException(neverThrown);
            }
        }
    }

    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
}
