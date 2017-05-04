package fi.oph.koski.util

object OptionalLists {
  def list2Optional[A, B](list: List[A], f: List[A] => B): Option[B] = list match {
    case Nil => None
    case xs => Some(f(xs))
  }

  def optionalList[A](list: List[A]): Option[List[A]] = list2Optional[A, List[A]](list, identity)
}
