package fi.oph.koski.validation

import java.time.LocalDate

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository
import fi.oph.koski.schema._
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService

// Tähän voi lisätä validointeja, joissa oppijan henkilöllisyys on jo tiedossa, minkä avulla voi tutkia
// esimerkiksi oppijan muiden opiskeluoikeuksien ominaisuuksia. Kaikki muut validaatiot pitää lisätä
// KoskiValidator-luokkaan.
//
// Globaalien validaatioiden lisäämisessä on syytä olla erittäin varovainen, sillä:
// (1) voi syntyä tilanne, jossa jonkun muun oppilaitoksen väärin siirtämä opiskeluoikeus estää toista oppilaitosta
// tallentamaan datansa oikein. Pahimmassa tapauksessa voi käydä niin, että jos opiskeluoikeudet ovat riippuvaisia
// toisistaan ristiin, niin kumpaakaan ei voi tehdä muutoksia.
// (2) voi syntyä yllättäviä suorituskykyheikennyksiä.
//
class KoskiGlobaaliValidator(
  opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository,
  rajapäivät: ValpasRajapäivätService
) extends Timing
{
  def validateOpiskeluoikeus(
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    oppijanSyntymäpäivä: Option[LocalDate],
    oppijanOid: String): HttpStatus =
  {
    timed("validateOpiskeluoikeus")(
      MaksuttomuusValidation.checkOpiskeluoikeudenMaksuttomuus(opiskeluoikeus, oppijanSyntymäpäivä, oppijanOid, opiskeluoikeusRepository, rajapäivät)
    )
  }
}
