package fi.oph.tor.util

object GroupingIterator {
  def groupBy[A, G](iterator: Iterator[A])(grouper: A => G): Iterator[(G, Iterator[A])] = {
    new Iterator[(G, Iterator[A])] {
      var group: Option[(G, List[A])] = None

      override def hasNext = group.isDefined || iterator.hasNext

      override def next(): (G, Iterator[A]) = {
        if (!iterator.hasNext) {
          val (key, list) = group.get
          group = None
          (key, list.iterator)
        } else {
          val nextValue: A = iterator.next
          val nextKey: G = grouper(nextValue)
          group match {
            case None =>
              group = Some((nextKey, List(nextValue)))
              return next
            case Some((prevKey, groupedValues)) if prevKey == nextKey =>
              group = Some((prevKey, groupedValues ++ List(nextValue)))
              return next
            case Some((prevKey, groupedValues)) =>
              val prevGroup = group
              group = Some((nextKey, List(nextValue)))
              (prevKey, groupedValues.iterator)
          }
        }
      }
    }
  }
}
