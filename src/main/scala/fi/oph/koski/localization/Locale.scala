package fi.oph.koski.localization

import java.text.Collator
import scala.annotation.nowarn

@nowarn("msg=Locale in class Locale is deprecated")
object Locale {
  val finnish = new java.util.Locale("fi", "FI")

  val finnishCollator: Collator = Collator.getInstance(finnish)

  def finnishComparator(a: String, b: String): Int = finnishCollator.compare(a, b)

  val finnishAlphabeticalOrdering: Ordering[String] = Ordering.comparatorToOrdering[String](finnishComparator)
}
