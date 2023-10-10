/*
 * This file is based on a copy from MessagePack for Java v0.8.24 with modification on :
 * - moving its Java package to org.embulk.util.msgpack.value.
 * - removing a test "Value" should "tell most succinct integer type"
 * - removing checkSuccinctType()
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
package org.embulk.util.msgpack.value

import java.math.BigInteger
import org.embulk.util.msgpack.core._
import org.scalacheck.Prop.{forAll, propBoolean}

import scala.util.parsing.json.JSON

class ValueTest extends MessagePackSpec {
  "Value" should {
    "produce json strings" in {

      import ValueFactory._

      newNil().toJson shouldBe "null"
      newNil().toString shouldBe "null"

      newBoolean(true).toJson shouldBe "true"
      newBoolean(false).toJson shouldBe "false"
      newBoolean(true).toString shouldBe "true"
      newBoolean(false).toString shouldBe "false"

      newInteger(3).toJson shouldBe "3"
      newInteger(3).toString shouldBe "3"
      newInteger(BigInteger.valueOf(1324134134134L)).toJson shouldBe "1324134134134"
      newInteger(BigInteger.valueOf(1324134134134L)).toString shouldBe "1324134134134"

      newFloat(0.1).toJson shouldBe "0.1"
      newFloat(0.1).toString shouldBe "0.1"

      newArray(newInteger(0), newString("hello")).toJson shouldBe "[0,\"hello\"]"
      newArray(newInteger(0), newString("hello")).toString shouldBe "[0,\"hello\"]"
      newArray(newArray(newString("Apple"), newFloat(0.2)), newNil()).toJson shouldBe """[["Apple",0.2],null]"""

      // Map value
      val m = newMapBuilder()
        .put(newString("id"), newInteger(1001))
        .put(newString("name"), newString("leo"))
        .put(newString("address"), newArray(newString("xxx-xxxx"), newString("yyy-yyyy")))
        .put(newString("name"), newString("mitsu"))
        .build()
      val i1 = JSON.parseFull(m.toJson)
      val i2 = JSON.parseFull(m.toString) // expect json value
      val a1 = JSON.parseFull("""{"id":1001,"name":"mitsu","address":["xxx-xxxx","yyy-yyyy"]}""")
      // Equals as JSON map
      i1 shouldBe a1
      i2 shouldBe a1

      // toJson should quote strings
      newString("1").toJson shouldBe "\"1\""
      // toString is for extracting string values
      newString("1").toString shouldBe "1"

    }

    "check appropriate range for integers" in {
      import ValueFactory._
      import java.lang.Byte
      import java.lang.Short

      newInteger(Byte.MAX_VALUE).asByte() shouldBe Byte.MAX_VALUE
      newInteger(Byte.MIN_VALUE).asByte() shouldBe Byte.MIN_VALUE
      newInteger(Short.MAX_VALUE).asShort() shouldBe Short.MAX_VALUE
      newInteger(Short.MIN_VALUE).asShort() shouldBe Short.MIN_VALUE
      newInteger(Integer.MAX_VALUE).asInt() shouldBe Integer.MAX_VALUE
      newInteger(Integer.MIN_VALUE).asInt() shouldBe Integer.MIN_VALUE
      intercept[MessageIntegerOverflowException] {
        newInteger(Byte.MAX_VALUE + 1).asByte()
      }
      intercept[MessageIntegerOverflowException] {
        newInteger(Byte.MIN_VALUE - 1).asByte()
      }
      intercept[MessageIntegerOverflowException] {
        newInteger(Short.MAX_VALUE + 1).asShort()
      }
      intercept[MessageIntegerOverflowException] {
        newInteger(Short.MIN_VALUE - 1).asShort()
      }
      intercept[MessageIntegerOverflowException] {
        newInteger(Integer.MAX_VALUE + 1.toLong).asInt()
      }
      intercept[MessageIntegerOverflowException] {
        newInteger(Integer.MIN_VALUE - 1.toLong).asInt()
      }
    }
  }
}
