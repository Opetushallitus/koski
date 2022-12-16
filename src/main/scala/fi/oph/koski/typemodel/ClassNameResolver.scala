package fi.oph.koski.typemodel

object ClassNameResolver {
  def packageName(fullClassName: String): String = fullClassName.splitAt(fullClassName.lastIndexOf("."))._1
  def className(fullClassName: String): String = fullClassName.splitAt(fullClassName.lastIndexOf(".") + 1)._2
  def nameToAbsolutePath(fullClassName: String): String =
    s"/${fullClassName.replace(".", "/")}"
  def nameToRelativePath(fullClassName: String): String =
    s"${fullClassName.replace(".", "/")}"
  def safeFilename(name: String): String = name.map {
    case 'Ä' | 'Å' => 'A'
    case 'Ö' => 'O'
    case 'ä' | 'å' => 'a'
    case 'ö' => 'o'
    case '.' => '.'
    case c: Char if c.isLetter || c.isDigit => c
    case _ => ""
  }.mkString
  def safePath(name: String): String = name.map {
    case '/' => '/'
    case t: Char => safeFilename(t.toString)
  }.mkString
}
