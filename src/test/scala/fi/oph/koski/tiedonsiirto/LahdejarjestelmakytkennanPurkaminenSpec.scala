package fi.oph.koski.tiedonsiirto

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.{ExamplesLukio2019, VapaaSivistystyöExample}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot, UnverifiedHenkilöOid}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{KoskiMockUser, KoskiSpecificSession, MockUsers}
import fi.oph.koski.schema.{Koodistokoodiviite, LähdejärjestelmäId, LähdejärjestelmäkytkennänPurkaminen, VapaanSivistystyönOpiskeluoikeus}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDateTime
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.TypeTag

class LahdejarjestelmakytkennanPurkaminenSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus] {
  val app: KoskiApplicationForTests.type = KoskiApplicationForTests
  val oppija: LaajatOppijaHenkilöTiedot = KoskiSpecificMockOppijat.lahdejarjestelmanPurku

  "Käyttöoikeudet" - {
    "Pääkäyttäjällä on oikeus purkaa kytkentä" in {
      puraKytkentä(purettavaOpiskeluoikeusOid, MockUsers.paakayttaja) should equal(
        Right(true)
      )
    }

    "Oppilaitoksen pääkäyttäjällä on oikeus purkaa kytkentä" in {
      puraKytkentä(purettavaOpiskeluoikeusOid, MockUsers.varsinaisSuomiPääkäyttäjä) should equal(
        Right(true)
      )
    }

    "Oppilaitoksen tiedonsiirtäjällä ei ole oikeutta purkaa kytkentää" in {
      puraKytkentä(purettavaOpiskeluoikeusOid, MockUsers.varsinaisSuomiOppilaitosTallentaja) should equal(
        Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      )
    }

    "Väärän oppilaitoksen pääkäyttäjällä ei ole oikeutta purkaa kytkentä" in {
      puraKytkentä(purettavaOpiskeluoikeusOid, MockUsers.omniaPääkäyttäjä) should equal(
        Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      )
    }
  }

  "Tiedonsiirron validaatiot" - {
    val headers = authHeaders(MockUsers.varsinaisSuomiPalvelukäyttäjä) ++ jsonContent

    "Estä purkaminen tiedonsiirron avulla" in {
      putOpiskeluoikeus(opiskeluoikeusPurkamisella, oppija, headers) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänPurkaminenEiSallittu())
      }
    }

    "Puretun opiskeluoikeuden tietoja ei voi enää siirtää" in {
      val oid = purettavaOpiskeluoikeusOid
      puraKytkentä(oid, MockUsers.paakayttaja)
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(oid = Some(oid)), oppija, headers) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänMuuttaminenEiSallittu())
      }
    }
  }

  "Purkamisen validaatiot" - {
    "Opiskeluoikeutta ei voi purkaa, jos sillä ei ole lähdejärjestelmätunnistetta" in {
      val opiskeluoikeusOid = oppijanEnsimmäsenOpiskeluoikeudenOid(KoskiSpecificMockOppijat.aikuisOpiskelijaMuuRahoitus)
      puraKytkentä(opiskeluoikeusOid, MockUsers.paakayttaja) should equal(Left(
        KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänPurkaminenEiSallittu("Opiskeluoikeudella ei ole lähdejärjestelmätunnistetta")
      ))
    }

    "Lähdejärjestelmäkytkentää ei voi purkaa aktiiviselta opiskeluoikeudelta" in {
      val opiskeluoikeusOid = app.opiskeluoikeusRepository.createOrUpdate(
        UnverifiedHenkilöOid(oppija.oid, app.henkilöRepository),
        ExamplesLukio2019.aktiivinenOpiskeluoikeus.copy(lähdejärjestelmänId = lähdejärjestelmäId),
        allowUpdate = true,
      )(KoskiSpecificSession.systemUser)
        .toOption.get.oid

      puraKytkentä(opiskeluoikeusOid, MockUsers.paakayttaja) should equal(Left(
        KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänPurkaminenEiSallittu("Lähdejärjestelmäkytkentää ei voi purkaa aktiiviselta opiskeluoikeudelta")
      ))
    }
  }

  def lähdejärjestelmäId: Option[LähdejärjestelmäId] = Some(LähdejärjestelmäId(
    id = Some("123"),
    lähdejärjestelmä = Koodistokoodiviite("primus", "lahdejarjestelma"),
  ))

  def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen.copy(
    lähdejärjestelmänId = lähdejärjestelmäId,
  )

  def opiskeluoikeusPurkamisella: VapaanSivistystyönOpiskeluoikeus = defaultOpiskeluoikeus.copy(
    lähdejärjestelmäkytkentäPurettu = Some(LähdejärjestelmäkytkennänPurkaminen(purettu = LocalDateTime.now()))
  )

  private def purettavaOpiskeluoikeusOid: String = {
    val result = app.opiskeluoikeusRepository.createOrUpdate(
      UnverifiedHenkilöOid(oppija.oid, app.henkilöRepository),
      defaultOpiskeluoikeus,
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

  private def oppijanEnsimmäsenOpiskeluoikeudenOid(henkilö: LaajatOppijaHenkilöTiedot): String =
    app.oppijaFacade
      .findOppija(henkilö.oid)(KoskiSpecificSession.systemUser)
      .toOption.get.get
      .opiskeluoikeudet.head.oid.get


  override def tag: universe.TypeTag[VapaanSivistystyönOpiskeluoikeus] = implicitly[TypeTag[VapaanSivistystyönOpiskeluoikeus]]
  override def header = response.header
}
