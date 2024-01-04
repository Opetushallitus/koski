package fi.oph.koski.validation

import fi.oph.koski.config.ValidationContext
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot

import java.time.LocalDate
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository
import fi.oph.koski.raportointikanta.RaportointiDatabase
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
  rajapäivät: ValpasRajapäivätService,
  validationConfig: ValidationContext,
) extends Timing
{
  def validateOpiskeluoikeus(
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    oppijanHenkilötiedot: Option[LaajatOppijaHenkilöTiedot],
    oppijanOid: String): HttpStatus =
  {
    val oppijanSyntymäpäivä = oppijanHenkilötiedot.flatMap(_.syntymäaika)

    if (opiskeluoikeus.mitätöity || !validationConfig.validoiOpiskeluoikeudet) {
      HttpStatus.ok
    } else {
      timed("validateOpiskeluoikeus")(
        HttpStatus.fold(Seq(
          MaksuttomuusValidation.checkOpiskeluoikeudenMaksuttomuus(
            opiskeluoikeus,
            oppijanHenkilötiedot,
            oppijanOid,
            opiskeluoikeusRepository,
            rajapäivät,
          ),
          Lukio2015Validation.validateAlkamispäivä(
            opiskeluoikeus,
            oppijanSyntymäpäivä,
            oppijanOid,
            opiskeluoikeusRepository,
            rajapäivät
          )
        ))
      )
    }
  }
}
