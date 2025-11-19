package fi.oph.koski.xml

import scala.collection.Seq
import scala.collection.immutable
import scala.xml.{Node, NodeBuffer, NodeSeq}

object NodeSeqImplicits {
  implicit def nodeBufferToNodeSeq(buffer: NodeBuffer): NodeSeq =
    NodeSeq.fromSeq(buffer.toSeq)

  implicit def nodeSeqToImmutableSeq(seq: NodeSeq): immutable.Seq[Node] =
    seq.toList

  implicit def seqOfNodesToNodeSeq(seq: Seq[Node]): NodeSeq =
    seq match {
      case ns: NodeSeq => ns
      case other       => NodeSeq.fromSeq(other.toSeq)
    }
}
