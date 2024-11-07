package fi.oph.koski.tiedonsiirto

import fi.oph.koski.documentation.VapaaSivistystyöExample
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, UnverifiedHenkilöOid}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{KoskiMockUser, KoskiSpecificSession, MockUsers}
import fi.oph.koski.schema.{Koodistokoodiviite, LähdejärjestelmäId}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec

class LahdejarjestelmakytkennanPurkaminenSpec extends AnyFreeSpec with KoskiHttpSpec {
  val app = KoskiApplicationForTests
  val oppija = KoskiSpecificMockOppijat.lahdejarjestelmanPurku

  "Käyttöoikeudet" - {
    "Pääkäyttäjällä on oikeus purkaa kytkentä" in {
      puraKytkentä(purettavaOpiskeluoikeus, MockUsers.paakayttaja) should equal(
        Right(true)
      )
    }

    "Oppilaitoksen pääkäyttäjällä on oikeus purkaa kytkentä" in {
      puraKytkentä(purettavaOpiskeluoikeus, MockUsers.varsinaisSuomiPääkäyttäjä) should equal(
        Right(true)
      )
    }

    "Oppilaitoksen tiedonsiirtäjällä ei ole oikeutta purkaa kytkentää" in {
      puraKytkentä(purettavaOpiskeluoikeus, MockUsers.varsinaisSuomiOppilaitosTallentaja) should equal(
        Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      )
    }

    "Väärän oppilaitoksen pääkäyttäjällä ei ole oikeutta purkaa kytkentä" in {
      puraKytkentä(purettavaOpiskeluoikeus, MockUsers.omniaPääkäyttäjä) should equal(
        Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      )
    }

    "Väliaikainen testi, jolla tsekataan että tämä suite tulee varmasti ajettua CI:ssä" in {
      2 + 2 should equal(5)
    }
  }

  private def purettavaOpiskeluoikeus: String = {
    val opiskeluoikeus = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen.copy(
      lähdejärjestelmänId = Some(LähdejärjestelmäId(
        id = Some("123"),
        lähdejärjestelmä = Koodistokoodiviite("primus", "lahdejarjestelma"),
      ))
    )

    val result = app.opiskeluoikeusRepository.createOrUpdate(
      UnverifiedHenkilöOid(oppija.oid, app.henkilöRepository),
      opiskeluoikeus,
      allowUpdate = true,
    )(KoskiSpecificSession.systemUser)

    result.toOption.get.oid
  }

  private def puraKytkentä
    (opiskeluoikeusOid: String, user: KoskiMockUser)
  : Either[HttpStatus, Boolean] = {
    implicit val session: KoskiSpecificSession = user.toKoskiSpecificSession(app.käyttöoikeusRepository)
    app.opiskeluoikeusRepository
      .puraLähdejärjestelmäkytkentä(opiskeluoikeusOid, app.henkilöRepository)
      .map { _ => true }
  }
}
