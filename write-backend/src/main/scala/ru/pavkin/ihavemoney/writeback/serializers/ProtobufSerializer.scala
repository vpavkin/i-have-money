package ru.pavkin.ihavemoney.writeback.serializers

import akka.serialization.SerializerWithStringManifest
import com.trueaccord.scalapb.{GeneratedMessage, Message}

import scala.reflect.ClassTag


abstract class ProtobufSerializer[M <: AnyRef : ClassTag, PB <: GeneratedMessage with Message[PB]](override val identifier: Int)
                                                                                                  (implicit val ev: ProtobufSuite[M, PB]) extends SerializerWithStringManifest {

  final val Manifest = implicitly[ClassTag[M]].runtimeClass.getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    if (Manifest == manifest) {
      ev.fromBytes(bytes)
    } else throw new IllegalArgumentException("Unable to handle manifest: " + manifest)

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case m: M ⇒ ev.toBytes(m)
    case _ ⇒ throw new IllegalStateException("Cannot serialize: " + o.getClass.getName)
  }
}
