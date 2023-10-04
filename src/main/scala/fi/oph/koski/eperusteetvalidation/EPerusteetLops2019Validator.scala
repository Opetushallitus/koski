package fi.oph.koski.eperusteetvalidation

import fi.oph.koski.eperusteet.{EPerusteKokoRakenne, EPerusteetRepository}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._
import fi.oph.scalaschema.Serializer.format
import org.json4s.JsonAST.JObject
import org.json4s._

class EPerusteetLops2019Validator(ePerusteet: EPerusteetRepository) {

  def validate(oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    HttpStatus.fold(oo.suoritukset.map(validate))

  def validate(pts: PäätasonSuoritus): HttpStatus =
    pts match {
      case pts: LukionPäätasonSuoritus2019 => validatePäätasonSuoritus(pts, lops2019Validointirakenne.get)
      case _ => HttpStatus.ok
    }

  def validatePäätasonSuoritus(pts: LukionPäätasonSuoritus2019, rakenne: OsasuoritustenValidointirakenne): HttpStatus =
    pts.osasuoritukset match {
      case Some(osasuoritukset) =>
        HttpStatus.fold(osasuoritukset.map(os => validateOppimääränOsasuoritus(os, rakenne.get(os.koulutusmoduuli.tunniste.koodiarvo))))
      case _ =>
        HttpStatus.ok
    }

  def validateOppimääränOsasuoritus(os: LukionOppimääränOsasuoritus2019, rakenne: Option[OsasuoritustenValidointirakenne]): HttpStatus =
    (os.osasuoritukset, rakenne) match {
      case (Some(osasuoritukset), Some(oppimääränRakenne)) =>
        (os.koulutusmoduuli match {
          case o: LukionMatematiikka2019 => oppimääränRakenne.get(o.oppimäärä.koodiarvo)
          case o: LukionUskonto2019 => o.uskonnonOppimäärä.map(k => oppimääränRakenne.get(k.koodiarvo))
          case o: LukionÄidinkieliJaKirjallisuus2019 => oppimääränRakenne.get(o.kieli.koodiarvo)
          case _ => None
        }).fold(HttpStatus.ok) {
          case r: OsasuoritustenValidointirakenne => HttpStatus.fold(osasuoritukset.map(s => validateModuulinSuoritus(s, r)))
          case _ => HttpStatus.ok
        }
      case _ => HttpStatus.ok
    }

  def validateModuulinSuoritus(s: LukionModuulinTaiPaikallisenOpintojaksonSuoritus2019, rakenne: OsasuoritustenValidointirakenne): HttpStatus =
    s match {
      case s: LukionModuulinSuoritusOppiaineissa2019 =>
        val moduuli = s.koulutusmoduuli.tunniste.koodiarvo
        if (rakenne.containsLeaf(moduuli) || !kaikkiPerusteetTuntematModuulit.contains(moduuli)) {
          HttpStatus.ok
        } else {
          KoskiErrorCategory.badRequest.validation.rakenne(s"Moduulia $moduuli ei voi siirtää oppiaineen ${rakenne.arvo} alle. Sallittuja moduuleja ovat: ${rakenne.leafs.mkString(", ")}")
        }
      case _ => HttpStatus.ok
    }

  private def parseLops2019(lops2019: Any): List[OsasuoritustenValidointirakenne] =
    lops2019 match {
      case o: JObject => parseLops2019Oppiaineet(o \\ "oppiaineet")
      case _: Any => Nil
    }

  private def parseLops2019Oppiaineet(oppiaineet: Any): List[OsasuoritustenValidointirakenne] =
    oppiaineet match {
      case a: JArray => a.arr.map { osa =>
        val oppimaarat = parseLops2019Oppimäärät(osa \ "oppimaarat")
        val moduulit = parseLops2019Moduulit(osa \ "moduulit")
        OsasuoritustenValidointirakenne(
          arvo = (osa \ "koodi" \ "arvo").extract[String],
          osat = oppimaarat ++ moduulit,
        )
      }
      case _ => Nil
    }

  private def parseLops2019Oppimäärät(oppimäärä: Any): List[OsasuoritustenValidointirakenne] =
    oppimäärä match {
      case a: JArray => a.arr.map { osa =>
        OsasuoritustenValidointirakenne(
          arvo = (osa \ "koodi" \ "arvo").extract[String],
          osat = parseLops2019Moduulit(osa \ "moduulit"),
        )
      }
      case _ => Nil
    }

  private def parseLops2019Moduulit(rakennetaso: Any): List[OsasuoritustenValidointirakenne] =
    rakennetaso match {
      case a: JArray => a.arr.map { osa =>
        OsasuoritustenValidointirakenne(arvo = (osa \ "koodi" \ "arvo").extract[String])
      }
      case _ => Nil
    }

  lazy val lops2019Validointirakenne: Option[OsasuoritustenValidointirakenne] =
    ePerusteet.findTarkatRakenteet("OPH-2263-2019", None) // TODO TOR-1119: Lisää päivä
      .collect { case r: EPerusteKokoRakenne => r }
      .find(_.lops2019.isDefined)
      .flatMap(_.lops2019)
      .map(parseLops2019)
      .map(osat => OsasuoritustenValidointirakenne("lops2019", osat))

  lazy val kaikkiPerusteetTuntematModuulit: Seq[String] = lops2019Validointirakenne.map(_.leafs).getOrElse(Nil)
}

case class OsasuoritustenValidointirakenne(
  arvo: String,
  osat: List[OsasuoritustenValidointirakenne] = Nil,
) {
  def get(a: String): Option[OsasuoritustenValidointirakenne] = osat.find(_.arvo == a)
  def containsLeaf(leaf: String): Boolean = if (osat.isEmpty) arvo == leaf else osat.exists(_.containsLeaf(leaf))
  def leafs: List[String] = if (osat.isEmpty) List(arvo) else osat.flatMap(_.leafs)
}
