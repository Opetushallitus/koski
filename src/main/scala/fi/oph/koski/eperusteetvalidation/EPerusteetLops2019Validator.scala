package fi.oph.koski.eperusteetvalidation

import fi.oph.koski.eperusteet.{EPerusteKokoRakenne, EPerusteetRepository}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.scalaschema.Serializer.format
import org.json4s.JsonAST.JObject
import org.json4s._

class EPerusteetLops2019Validator(ePerusteet: EPerusteetRepository) extends Logging {

  def validate(oo: KoskeenTallennettavaOpiskeluoikeus): HttpStatus =
    HttpStatus.fold(oo.suoritukset.map(s => validate(oo.oid.getOrElse("???"), s)))

  def validate(oid: String, pts: PäätasonSuoritus): HttpStatus =
    pts match {
      case pts: LukionPäätasonSuoritus2019 =>
        // TODO TOR-1119: Palauta `result` ja poista varoituksen tulostus
        val result = validatePäätasonSuoritus(pts, lops2019Validointirakenne.get)
        if (result.isError) {
          logger.warn(s"Opiskeluoikeuden $oid lops 2019 ePeruste-rakennevalidointi ei menisi läpi: ${result.errorString.getOrElse("virheviesti puuttuu")}")
        }
        HttpStatus.ok
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
          case _: LukionUskonto2019 => rakenne
          case _: LukionMuuValtakunnallinenOppiaine2019 => rakenne
          // TODO TOR-1165: Validoidaan äidinkieli, kunhan koodisto ja ePerusteet saadaan niiden moduulien kannalta korjattua yhteneväisiksi
          // case o: LukionÄidinkieliJaKirjallisuus2019 => oppimääränRakenne.get(o.kieli.koodiarvo)
          case _: Any => None
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
        val leaf = rakenne.findLeaf(moduuli)
        if (
           leaf.isDefined // Moduuli on osa tutkittavaa oppiainetta tai -määrää
            || !kaikkiPerusteetTuntematModuulit.contains(moduuli) // Moduulit, jotka eivät ole ePerusteessa, jätetään varmuuden vuoksi käsittelemättä
        ) {
          if (leaf.exists(_.toisenRakenteenOsa) && s.koulutusmoduuli.pakollinen) {
            KoskiErrorCategory.badRequest.validation.rakenne(s"Moduulia $moduuli ei voi siirtää oppiaineen ${rakenne.arvo} alle pakollisena.")
          } else {
            HttpStatus.ok
          }
        } else {
          val oppiaineExpected = lops2019Validointirakenne
            .flatMap(_.findParentOf(moduuli))
            .map(_.arvo)
            .getOrElse("???")
          val moduulitExpected = rakenne.leafs.mkString(", ")
          val oppiaineActual = rakenne.arvo
          KoskiErrorCategory.badRequest.validation.rakenne(
            s"Moduulia $moduuli ei voi siirtää oppiaineen/-määrän $oppiaineActual alle, koska se on oppiaineen/-määrän $oppiaineExpected moduuli. Sallittuja $oppiaineActual-moduuleja ovat $moduulitExpected."
          )
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
      .map(rakenne => {
        rakenne.copy(osat = rakenne.osat.flatMap {
          // Uskontoa ei validoida oppimäärän tarkkuudella, vaan kaikki uskonnon ja elämänkatsomustiedon moduulit kelpaavat uskonnon oppiaineen suoritukseen
          // Lisäksi uskonnon alle voi siirtää ET-moduuleja valinnaisena
          case uskonto: OsasuoritustenValidointirakenne if uskonto.arvo == "KT" => Some(uskonto.flatten.withModuulitValinnaisena(rakenne.get("ET")))
          // ET:n alle voi siirtää uskonnon moduuleja valinnaisena
          case et: OsasuoritustenValidointirakenne if et.arvo == "ET" => Some(et.withModuulitValinnaisena(rakenne.get("KT").map(_.flatten)))
          // Pitkän matematiikan alle voi siirtää lyhyen matematiikan moduuleja valinnaisena ja toisinpäin.
          // Matematiikan yhteisen opinnon moduulin voi siirtää sekä lyhyeen että pitkään matematiikkaan.
          case matematiikka: OsasuoritustenValidointirakenne if matematiikka.arvo == "MA" =>
            Some(matematiikka.copy(osat = matematiikka.osat.map {
              case maa: OsasuoritustenValidointirakenne if maa.arvo == "MAA" => maa
                .withModuulit(matematiikka.get("MAY"))
                .withModuulitValinnaisena(matematiikka.get("MAB"))
              case mab: OsasuoritustenValidointirakenne if mab.arvo == "MAB" => mab
                .withModuulit(matematiikka.get("MAY"))
                .withModuulitValinnaisena(matematiikka.get("MAA"))
              case ma: OsasuoritustenValidointirakenne => ma
            }))
          // Diplomimoduuleja ei validoida
          case osa: OsasuoritustenValidointirakenne if osa.arvo == "LD" => None
          // Muihin moduuleihin ei liity erityissääntöjä
          case osa: OsasuoritustenValidointirakenne => Some(osa)
        })
      })

  lazy val kaikkiPerusteetTuntematModuulit: Seq[String] = lops2019Validointirakenne.map(_.leafs).getOrElse(Nil)
}

case class OsasuoritustenValidointirakenne(
  arvo: String,
  osat: List[OsasuoritustenValidointirakenne] = Nil,
  toisenRakenteenOsa: Boolean = false,
) {
  def get(a: String): Option[OsasuoritustenValidointirakenne] = osat.find(_.arvo == a)
//  def findNode(a: String): Option[OsasuoritustenValidointirakenne] =
//    if (arvo == a) Some(this) else osat.flatMap(_.findNode(a)).headOption
  def findLeaf(leaf: String): Option[OsasuoritustenValidointirakenne] =
    if (osat.isEmpty) {
      if (arvo == leaf) Some(this) else None
    } else {
      osat.flatMap(_.findLeaf(leaf)).headOption
    }
  def leafs: List[String] = if (osat.isEmpty) List(arvo) else osat.flatMap(_.leafs)
  def findParentOf(a: String): Option[OsasuoritustenValidointirakenne] =
    osat.find(_.arvo == a) match {
      case Some(_) => Some(this)
      case None => osat.flatMap(_.findParentOf(a)).headOption
    }

  def flatten: OsasuoritustenValidointirakenne = copy(osat = osat ++ osat.map(_.flatten))
  def merge(lisäosat: List[OsasuoritustenValidointirakenne]): OsasuoritustenValidointirakenne = copy(osat = osat ++ lisäosat)

  def withModuulit(rakenne: Option[OsasuoritustenValidointirakenne]): OsasuoritustenValidointirakenne =
    merge(rakenne.map(_.osat).getOrElse(Nil))

  def withModuulitValinnaisena(rakenne: Option[OsasuoritustenValidointirakenne]): OsasuoritustenValidointirakenne =
    merge(rakenne.map(_.osat).getOrElse(Nil).map(_.asToisenRakenteenOsa))
  def asToisenRakenteenOsa: OsasuoritustenValidointirakenne = copy(toisenRakenteenOsa = true, osat = osat.map(_.asToisenRakenteenOsa))
}
