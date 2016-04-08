package ru.pavkin.ihavemoney.writeback.serializers

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

  def identity[PB <: GeneratedMessage with Message[PB]](compObj: GeneratedMessageCompanion[PB]): ProtobufSuite[PB, PB] =
    new ProtobufSuite[PB, PB] {
      def encode(m: PB): PB = m
      def decode(p: PB): PB = p
      def companion: GeneratedMessageCompanion[PB] = compObj
    }

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
