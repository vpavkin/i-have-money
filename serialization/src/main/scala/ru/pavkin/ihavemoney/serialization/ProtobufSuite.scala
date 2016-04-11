package ru.pavkin.ihavemoney.serialization

import com.trueaccord.scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}

trait ProtobufSuite[M, PB <: GeneratedMessage with Message[PB]] {
  def encode(m: M): PB
  def decode(p: PB): M
  def companion: GeneratedMessageCompanion[PB]
  def protobufFromBytes(bytes: Array[Byte]): PB = companion.parseFrom(bytes)
  def fromBytes(bytes: Array[Byte]): M = decode(protobufFromBytes(bytes))
  def toBytes(message: M): Array[Byte] = encode(message).toByteArray
}

object ProtobufSuite {

  object syntax {
    implicit class MessageOps[M, PB <: GeneratedMessage with Message[PB]](m: M)(implicit ev: ProtobufSuite[M, PB]) {
      def encode: PB = ev.encode(m)
      def toBytes: Array[Byte] = ev.toBytes(m)
    }

    implicit class ProtobufOps[M, PB <: GeneratedMessage with Message[PB]](p: PB)(implicit ev: ProtobufSuite[M, PB]) {
      def decode: M = ev.decode(p)
    }
  }
}
