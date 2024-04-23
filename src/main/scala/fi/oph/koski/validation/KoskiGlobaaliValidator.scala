package fi.oph.koski.validation

import com.typesafe.config.Config
import fi.oph.koski.fixture.ValidationTestContext
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, OpintopolkuHenkilöFacade}

import java.time.LocalDate
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
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
  oppijanumerorekisteri: OpintopolkuHenkilöFacade,
  validationConfig: ValidationTestContext,
  config: Config
) extends Timing
{
  def validateOpiskeluoikeus(
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    oppijanHenkilötiedot: Option[LaajatOppijaHenkilöTiedot],
    oppijanOid: String)(implicit user: KoskiSpecificSession): HttpStatus =
  {
    val oppijanSyntymäpäivä = oppijanHenkilötiedot.flatMap(_.syntymäaika)

    if (opiskeluoikeus.mitätöity || !validationConfig.validoiOpiskeluoikeudet) {
      HttpStatus.ok
    } else {
      val timedBlockname = s"validateOpiskeluoikeus tyyppi=${opiskeluoikeus.tyyppi.koodiarvo}"

      timed(timedBlockname)(
        HttpStatus.fold(Seq(
          timed(s"${timedBlockname} MaksuttomuusValidation.checkOpiskeluoikeudenMaksuttomuus") {
            MaksuttomuusValidation.checkOpiskeluoikeudenMaksuttomuus(
              opiskeluoikeus,
              oppijanHenkilötiedot,
              oppijanOid,
              opiskeluoikeusRepository,
              rajapäivät,
              oppijanumerorekisteri,
            )
          },
          timed(s"${timedBlockname} Lukio2015Validation.validateAlkamispäivä") {
            Lukio2015Validation.validateAlkamispäivä(
              opiskeluoikeus,
              oppijanSyntymäpäivä,
              oppijanOid,
              opiskeluoikeusRepository,
              rajapäivät
            )
          },
          timed(s"${timedBlockname} DuplikaattiValidation.validateDuplikaatit") {
            oppijanHenkilötiedot match {
              case Some(h) =>
                DuplikaattiValidation.validateDuplikaatit(
                  opiskeluoikeus,
                  h,
                  opiskeluoikeusRepository,
                  config
                )
              case _ => HttpStatus.ok
            }

          }
          // TODO: Siirrä EB-ESH olemassaolovalidaatio tänne
          // TODO: Siirrä osaamismerkkien duplikaattivalidaatio tänne
          // TODO: Siirrä "vastaavanRinnakkaisenOpiskeluoikeudenLisääminenSallittu"-metodista duplikaattivalidaatiot tänne, jotta toimivat myös lähdejärjestelmän id:llä
          // TODO: Jos validaatiot osoittautuvat hitaiksi, koodaa ne SQL:llä (ainakin voimassaoloaikojen päällekkäisyyttä tutkivat validaatiot voinee tehdä SQL:llä kokonaan pienehköllä vaivalla)
        ))
      )
    }
  }
}
