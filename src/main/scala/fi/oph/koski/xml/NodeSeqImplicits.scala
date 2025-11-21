package fi.oph.koski.xml

import scala.collection.immutable
import scala.xml.{Node, NodeSeq}

object NodeSeqImplicits {
  implicit def nodeBufferToImmutableSeq(buffer: scala.xml.NodeBuffer): immutable.Seq[Node] =
    buffer.toSeq

  implicit def nodeSeqToImmutableSeq(seq: NodeSeq): immutable.Seq[Node] = seq
}
