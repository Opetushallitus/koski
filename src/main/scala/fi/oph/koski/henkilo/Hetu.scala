package fi.oph.koski.henkilo

import java.time.{DateTimeException, LocalDate}

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}

import scala.util.matching.Regex

object Hetu {
  val checkChars = List('0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','H','J','K','L','M','N','P','R','S','T','U','V','W','X','Y')
  val hetuRegex: Regex = "^([012][0-9]|3[01])(0[1-9]|1[0-2])([0-9]{2})(A|B|C|D|E|F|Y|X|W|V|U|-|\\+)([0-9]{3})([0-9A-FHJ-NPR-Y])$".r

  def century(hetu: String): Option[Int] = hetu.lift(6) match {
    case Some('+') => Some(1800)
    case Some('-') | Some('X') | Some('Y') | Some('W') | Some('V') | Some('U') => Some(1900)
    case Some('A') | Some('B') | Some('C') | Some('D') | Some('E') | Some('F') => Some(2000)
    case _  => None
  }

  def birthday(hetu: String, century: Int): LocalDate = {
    LocalDate.of(century + hetu.slice(4, 6).toInt, hetu.slice(2, 4).toInt, hetu.slice(0, 2).toInt)
  }

  def validFormat(hetu: String): Either[HttpStatus, String] = {
    hetuRegex.findFirstIn(hetu) match {
      case Some(_) => Right(hetu)
      case None => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Virheellinen muoto hetulla: " + hetu))
    }
  }

  private def validate(hetu: String, acceptSynthetic: Boolean): Either[HttpStatus, String] = {
    def validDate(hetu: String) = {
      try {
        century(hetu).flatMap { century =>
          if (birthday(hetu, century).isBefore(LocalDate.now)) Some(hetu) else None
        } match {
          case Some(_) =>
            if (!acceptSynthetic && hetu.substring(7, 8) == "9") {
              Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Keinotekoinen henkilötunnus: " + hetu))
            } else {
              Right(hetu)
            }
          case None => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Syntymäpäivä hetussa: " + hetu + " on tulevaisuudessa"))
        }
      } catch {
        case ex: DateTimeException => Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Virheellinen syntymäpäivä hetulla: " + hetu))
      }
    }

    def validCheckChar(hetu: String) = {
      val checkChar = checkChars(Math.round(((hetu.slice(0,6) + hetu.slice(7,10)).toInt / 31.0 % 1) * 31).toInt)
      if (checkChar == hetu.last) Right(hetu) else Left(KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Virheellinen tarkistusmerkki hetussa: " + hetu))
    }
    validFormat(hetu).right.flatMap(validDate(_).right.flatMap(validCheckChar))
  }

  def toBirthday(hetu: String): Option[LocalDate] = {
    century(hetu).flatMap { century => Some(birthday(hetu, century)) }
  }
}

class Hetu(acceptSyntheticHetus: Boolean) {
  def validate(hetu: String): Either[HttpStatus, String] = Hetu.validate(hetu, acceptSyntheticHetus)
}
