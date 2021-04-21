package fi.oph.koski.util

import scala.util.matching.Regex

object RegexUtils {
  implicit class StringWithRegex(val s: String) extends AnyVal {
    def matches(re: Regex): Boolean = re.pattern.matcher(s).matches

    def =~(re: Regex): Boolean = re.findFirstIn(s).isDefined
  }
}
