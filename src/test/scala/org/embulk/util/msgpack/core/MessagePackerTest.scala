/*
 * This file is based on a copy from MessagePack for Java v0.8.24 with modification on :
 * - moving its Java package to org.embulk.util.msgpack.core.
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
package org.embulk.util.msgpack.core

import java.io.{ByteArrayOutputStream, File, FileInputStream, FileOutputStream}

import org.embulk.util.msgpack.core.MessagePack.{UnpackerConfig, PackerConfig}
import org.embulk.util.msgpack.core.buffer.{ChannelBufferOutput, OutputStreamBufferOutput}
import org.embulk.util.msgpack.value.ValueFactory
import xerial.core.io.IOUtil

import scala.util.Random

/**
  *
  */
class MessagePackerTest extends MessagePackSpec {

  def verifyIntSeq(answer: Array[Int], packed: Array[Byte]) {
    val unpacker = MessagePack.newDefaultUnpacker(packed)
    val b        = Array.newBuilder[Int]
    while (unpacker.hasNext) {
      b += unpacker.unpackInt()
    }
    val result = b.result
    result.size shouldBe answer.size
    result shouldBe answer
  }

  def createTempFile = {
    val f = File.createTempFile("msgpackTest", "msgpack")
    f.deleteOnExit
    f
  }

  def createTempFileWithOutputStream = {
    val f   = createTempFile
    val out = new FileOutputStream(f)
    (f, out)
  }

  def createTempFileWithChannel = {
    val (f, out) = createTempFileWithOutputStream
    val ch       = out.getChannel
    (f, ch)
  }

  "MessagePacker" should {

    "reset the internal states" in {
      val intSeq = (0 until 100).map(i => Random.nextInt).toArray

      val b      = new ByteArrayOutputStream
      val packer = MessagePack.newDefaultPacker(b)
      intSeq foreach packer.packInt
      packer.close
      verifyIntSeq(intSeq, b.toByteArray)

      val intSeq2 = intSeq.reverse
      val b2      = new ByteArrayOutputStream
      packer
        .reset(new OutputStreamBufferOutput(b2))
      intSeq2 foreach packer.packInt
      packer.close
      verifyIntSeq(intSeq2, b2.toByteArray)

      val intSeq3 = intSeq2.sorted
      val b3      = new ByteArrayOutputStream
      packer
        .reset(new OutputStreamBufferOutput(b3))
      intSeq3 foreach packer.packInt
      packer.close
      verifyIntSeq(intSeq3, b3.toByteArray)
    }

    "improve the performance via reset method" taggedAs ("reset") in {

      val N = 1000
      val t = time("packer", repeat = 10) {
        block("no-buffer-reset") {
          val out = new ByteArrayOutputStream
          IOUtil.withResource(MessagePack.newDefaultPacker(out)) { packer =>
            for (i <- 0 until N) {
              val outputStream = new ByteArrayOutputStream()
              packer
                .reset(new OutputStreamBufferOutput(outputStream))
              packer.packInt(0)
              packer.flush()
            }
          }
        }

        block("buffer-reset") {
          val out = new ByteArrayOutputStream
          IOUtil.withResource(MessagePack.newDefaultPacker(out)) { packer =>
            val bufferOut =
              new OutputStreamBufferOutput(new ByteArrayOutputStream())
            for (i <- 0 until N) {
              val outputStream = new ByteArrayOutputStream()
              bufferOut.reset(outputStream)
              packer.reset(bufferOut)
              packer.packInt(0)
              packer.flush()
            }
          }
        }
      }

      t("buffer-reset").averageWithoutMinMax should be <= t("no-buffer-reset").averageWithoutMinMax
    }

    "pack larger string array than byte buf" taggedAs ("larger-string-array-than-byte-buf") in {
      // Based on https://github.com/msgpack/msgpack-java/issues/154

      def test(bufferSize: Int, stringSize: Int): Boolean = {
        val str       = "a" * stringSize
        val rawString = ValueFactory.newString(str.getBytes("UTF-8"))
        val array     = ValueFactory.newArray(rawString)
        val out       = new ByteArrayOutputStream(bufferSize)
        val packer    = MessagePack.newDefaultPacker(out)
        packer.packValue(array)
        packer.close()
        out.toByteArray
        true
      }

      val testCases = Seq(
        32 -> 30,
        33 -> 31,
        32 -> 31,
        34 -> 32
      )
      testCases.foreach {
        case (bufferSize, stringSize) => test(bufferSize, stringSize)
      }
    }

    "reset OutputStreamBufferOutput" in {
      val (f0, out0) = createTempFileWithOutputStream
      val packer     = MessagePack.newDefaultPacker(out0)
      packer.packInt(99)
      packer.close

      val up0 = MessagePack
        .newDefaultUnpacker(new FileInputStream(f0))
      up0.unpackInt shouldBe 99
      up0.hasNext shouldBe false
      up0.close

      val (f1, out1) = createTempFileWithOutputStream
      packer
        .reset(new OutputStreamBufferOutput(out1))
      packer.packInt(99)
      packer.flush
      packer
        .reset(new OutputStreamBufferOutput(out1))
      packer.packString("hello")
      packer.close

      val up1 = MessagePack
        .newDefaultUnpacker(new FileInputStream(f1))
      up1.unpackInt shouldBe 99
      up1.unpackString shouldBe "hello"
      up1.hasNext shouldBe false
      up1.close
    }

    "reset ChannelBufferOutput" in {
      val (f0, out0) = createTempFileWithChannel
      val packer     = MessagePack.newDefaultPacker(out0)
      packer.packInt(99)
      packer.close

      val up0 = MessagePack
        .newDefaultUnpacker(new FileInputStream(f0))
      up0.unpackInt shouldBe 99
      up0.hasNext shouldBe false
      up0.close

      val (f1, out1) = createTempFileWithChannel
      packer
        .reset(new ChannelBufferOutput(out1))
      packer.packInt(99)
      packer.flush
      packer
        .reset(new ChannelBufferOutput(out1))
      packer.packString("hello")
      packer.close

      val up1 = MessagePack
        .newDefaultUnpacker(new FileInputStream(f1))
      up1.unpackInt shouldBe 99
      up1.unpackString shouldBe "hello"
      up1.hasNext shouldBe false
      up1.close
    }

    "pack a lot of String within expected time" in {
      val count = 20000

      def measureDuration(outputStream: java.io.OutputStream) = {
        val packer = MessagePack.newDefaultPacker(outputStream)
        var i      = 0
        while (i < count) {
          packer.packString("0123456789ABCDEF")
          i += 1
        }
        packer.close
      }

      val t = time("packString into OutputStream", repeat = 10) {
        block("byte-array-output-stream") {
          measureDuration(new ByteArrayOutputStream())
        }

        block("file-output-stream") {
          val (_, fileOutput) = createTempFileWithOutputStream
          measureDuration(fileOutput)
        }
      }
      t("file-output-stream").averageWithoutMinMax shouldBe <(t("byte-array-output-stream").averageWithoutMinMax * 5)
    }
  }

  "compute totalWrittenBytes" in {
    val out = new ByteArrayOutputStream
    val packerTotalWrittenBytes =
      IOUtil.withResource(MessagePack.newDefaultPacker(out)) { packer =>
        packer
          .packByte(0) // 1
          .packBoolean(true) // 1
          .packShort(12) // 1
          .packInt(1024) // 3
          .packLong(Long.MaxValue) // 5
          .packString("foobar") // 7
          .flush()

        packer.getTotalWrittenBytes
      }

    out.toByteArray.length shouldBe packerTotalWrittenBytes
  }

  "support read-only buffer" taggedAs ("read-only") in {
    val payload = Array[Byte](1)
    val out     = new ByteArrayOutputStream()
    val packer = MessagePack
      .newDefaultPacker(out)
      .packBinaryHeader(1)
      .writePayload(payload)
      .close()
  }

  "pack small string with STR8" in {
    val packer = new PackerConfig().newBufferPacker()
    packer.packString("Hello. This is a string longer than 32 characters!")
    val b = packer.toByteArray

    val unpacker = MessagePack.newDefaultUnpacker(b)
    val f        = unpacker.getNextFormat
    f shouldBe MessageFormat.STR8
  }

  "be able to disable STR8 for backward compatibility" in {
    val config = new PackerConfig()
      .withStr8FormatSupport(false)

    val packer = config.newBufferPacker()
    packer.packString("Hello. This is a string longer than 32 characters!")
    val unpacker = MessagePack.newDefaultUnpacker(packer.toByteArray)
    val f        = unpacker.getNextFormat
    f shouldBe MessageFormat.STR16
  }

  "be able to disable STR8 when using CharsetEncoder" in {
    val config = new PackerConfig()
      .withStr8FormatSupport(false)
      .withSmallStringOptimizationThreshold(0) // Disable small string optimization

    val packer = config.newBufferPacker()
    packer.packString("small string")
    val unpacker = MessagePack.newDefaultUnpacker(packer.toByteArray)
    val f        = unpacker.getNextFormat
    f shouldNot be(MessageFormat.STR8)
    val s = unpacker.unpackString()
    s shouldBe "small string"
  }

  "write raw binary" taggedAs ("raw-binary") in {
    val packer = new MessagePack.PackerConfig().newBufferPacker()
    val msg =
      Array[Byte](-127, -92, 116, 121, 112, 101, -92, 112, 105, 110, 103)
    packer.writePayload(msg)
  }

  "append raw binary" taggedAs ("append-raw-binary") in {
    val packer = new MessagePack.PackerConfig().newBufferPacker()
    val msg =
      Array[Byte](-127, -92, 116, 121, 112, 101, -92, 112, 105, 110, 103)
    packer.addPayload(msg)
  }

}
