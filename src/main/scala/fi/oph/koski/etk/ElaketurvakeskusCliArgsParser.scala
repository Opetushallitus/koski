package fi.oph.koski.etk


object ElaketurvakeskusCliArgsParser {

  def parse(args: Array[String]): Map[Argument, String] = {
    parse(Map(), args.toList)
  }

  private def parse(result: Map[Argument, String], args: List[String]): Map[Argument, String] = args match {
    case Nil => result
    case "-csv" :: filepath :: tail => parse(result + (CsvFile -> filepath), tail)
    case "-api" :: request :: tail => parse(result + (ApiCall -> request), tail)
    case "-user" :: usernameAndPassword :: tail => parse(result + (CliUser -> usernameAndPassword), tail)
    case "-port" :: port :: tail => parse(result + (KoskiPort -> port), tail)
    case unknown@_ => throw new Error(s"Unknown parameters for argument $unknown")
  }
}

sealed abstract trait Argument

case object CsvFile extends Argument
case object ApiCall extends Argument
case object CliUser extends Argument
case object KoskiPort extends Argument
