package fi.oph.koski.util

class ChunkReader[T](pageSize: Int, read: Chunk => Option[Seq[T]]) extends Iterator[Seq[T]] {
  private var chunk: Option[Seq[T]] = None
  private var finished: Boolean = false
  private var offset: Int = 0

  override def hasNext: Boolean = nextChunk.nonEmpty

  override def next(): Seq[T] = {
    val consumed = nextChunk
    chunk = None
    consumed
  }

  private def nextChunk: Seq[T] =
    chunk match {
      case None if !finished => readNext()
      case Some(data) => data
      case None if finished => Seq.empty
    }


  private def readNext(): Seq[T] = {
    if (!finished) {
      val data = read(Chunk(offset, pageSize))
      offset += pageSize
      data match {
        case None =>
          finished = true
          chunk = None
        case Some(data) =>
          chunk = Some(data)
      }
    }
    chunk.getOrElse(Seq.empty)
  }
}

case class Chunk(
  offset: Int,
  pageSize: Int,
)
