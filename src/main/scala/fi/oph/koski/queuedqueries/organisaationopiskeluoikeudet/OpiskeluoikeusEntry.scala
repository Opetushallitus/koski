package fi.oph.koski.queuedqueries.organisaationopiskeluoikeudet

import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus
import org.json4s.{JNothing, JValue}

import java.time.LocalDateTime

case class OpiskeluoikeusEntry(
  opiskeluoikeus_oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  sisaltyy_opiskeluoikeuteen_oid: Option[String],
  oppija_oid: String,
  oppilaitos_oid: Option[String],
  oppilaitos_nimi: Option[String],
//  oppilaitosKotipaikka: Option[String],
//  oppilaitosnumero: Option[String],
//  koulutustoimijaOid: String,
//  koulutustoimijaNimi: String,
//  koulutusmuoto: String,
//  alkamispäivä: Option[Date],
//  päättymispäivä: Option[Date],
//  viimeisinTila: Option[String],
//  lisätiedotHenkilöstökoulutus: Boolean,
//  lisätiedotKoulutusvienti: Boolean,
//  tuvaJärjestämislupa: Option[String],
//  lähdejärjestelmäKoodiarvo: Option[String],
//  lähdejärjestelmäId: Option[String],
//  oppivelvollisuudenSuorittamiseenKelpaava: Boolean,
  data: JValue,
)

object OpiskeluoikeusEntry {
  def apply(henkilö: LaajatOppijaHenkilöTiedot, oo: KoskeenTallennettavaOpiskeluoikeus, lang: String = "fi"): OpiskeluoikeusEntry = OpiskeluoikeusEntry(
    opiskeluoikeus_oid = oo.oid,
    versionumero = oo.versionumero,
    aikaleima = oo.aikaleima,
    sisaltyy_opiskeluoikeuteen_oid = oo.sisältyyOpiskeluoikeuteen.map(_.oid),
    oppija_oid = henkilö.oid,
    oppilaitos_oid = oo.oppilaitos.map(_.oid),
    oppilaitos_nimi = oo.oppilaitos.flatMap(_.nimi).map(_.get(lang)),
    data = JNothing,
  )
}
