package fi.oph.koski.tiedonsiirto

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExamplesLukio2019.oppimääränSuoritus
import fi.oph.koski.documentation.LukioExampleData.{katsotaanEronneeksi, opiskeluoikeusAktiivinen}
import fi.oph.koski.documentation.{ExampleData, ExamplesLukio2019, VapaaSivistystyöExample}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot, UnverifiedHenkilöOid}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{KoskiMockUser, KoskiSpecificSession, MockUsers}
import fi.oph.koski.schema.{Koodistokoodiviite, LukionOpiskeluoikeudenTila, LukionOpiskeluoikeus, LukionOpiskeluoikeusjakso, LähdejärjestelmäId, LähdejärjestelmäkytkennänPurkaminen, VapaanSivistystyönOpiskeluoikeus}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec

import java.time.{LocalDate, LocalDateTime}
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
      puraKytkentä(purettavaVstOpiskeluoikeusOid, MockUsers.paakayttaja) should equal(
        Right(true)
      )
    }

    "Oppilaitoksen pääkäyttäjällä on oikeus purkaa kytkentä" in {
      puraKytkentä(purettavaVstOpiskeluoikeusOid, MockUsers.varsinaisSuomiPääkäyttäjä) should equal(
        Right(true)
      )
    }

    "Oppilaitoksen tiedonsiirtäjällä ei ole oikeutta purkaa kytkentää" in {
      puraKytkentä(purettavaVstOpiskeluoikeusOid, MockUsers.varsinaisSuomiOppilaitosTallentaja) should equal(
        Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      )
    }

    "Väärän oppilaitoksen pääkäyttäjällä ei ole oikeutta purkaa kytkentä" in {
      puraKytkentä(purettavaVstOpiskeluoikeusOid, MockUsers.omniaPääkäyttäjä) should equal(
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
      val oid = purettavaVstOpiskeluoikeusOid
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
        aktiivinenLukioOpiskeluoikeus,
        allowUpdate = true,
      )(KoskiSpecificSession.systemUser)
        .toOption.get.oid

      puraKytkentä(opiskeluoikeusOid, MockUsers.paakayttaja) should equal(Left(
        KoskiErrorCategory.forbidden.lähdejärjestelmäkytkennänPurkaminenEiSallittu("Lähdejärjestelmäkytkentää ei voi purkaa aktiiviselta opiskeluoikeudelta")
      ))
    }
  }

  "Puretun opiskeluoikeuden validaatiot" - {
    implicit val user: KoskiSpecificSession = KoskiSpecificSession.systemUser
    val oid = purettavaLukioOpiskeluoikeusOid
    puraKytkentä(oid, MockUsers.paakayttaja)

    val opiskeluoikeus = app.opiskeluoikeusRepository
      .findByOid(oid)
      .toOption.get.toOpiskeluoikeusUnsafe
      .asInstanceOf[LukionOpiskeluoikeus]

    "Terminaalitilaa ei pysty vaihtamaan ei-terminaalitilaksi" in {
      val päivitettyOpiskeluoikeus = opiskeluoikeus.copy(
        tila = opiskeluoikeus.tila.copy(opiskeluoikeusjaksot = opiskeluoikeus.tila.opiskeluoikeusjaksot.init)
      )
      putOpiskeluoikeus(päivitettyOpiskeluoikeus, oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.terminaalitilaaEiSaaPurkaa())
      }
    }

    "Terminaalitilan voi vaihtaa toiseksi terminaalitilaksi" in {
      val päivitettyOpiskeluoikeus = opiskeluoikeus.copy(
        tila = LukionOpiskeluoikeudenTila(
          List(
            LukionOpiskeluoikeusjakso(alku = LocalDate.of(2019, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
            LukionOpiskeluoikeusjakso(alku = LocalDate.of(2021, 8, 1), tila = katsotaanEronneeksi, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
          )
        ),
        suoritukset = List(oppimääränSuoritus.copy(vahvistus = None)),
      )
      putOpiskeluoikeus(päivitettyOpiskeluoikeus, oppija) {
        verifyResponseStatusOk()
      }
    }

  }

  def lähdejärjestelmäId: Option[LähdejärjestelmäId] = Some(LähdejärjestelmäId(
    id = Some("123"),
    lähdejärjestelmä = Koodistokoodiviite("primus", "lahdejarjestelma"),
  ))

  def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = VapaaSivistystyöExample.opiskeluoikeusVapaatavoitteinen.copy(lähdejärjestelmänId = lähdejärjestelmäId)
  def aktiivinenLukioOpiskeluoikeus: LukionOpiskeluoikeus = ExamplesLukio2019.aktiivinenOpiskeluoikeus.copy(lähdejärjestelmänId = lähdejärjestelmäId)
  def päättynytLukioOpiskeluoikeus: LukionOpiskeluoikeus = ExamplesLukio2019.opiskeluoikeus.copy(lähdejärjestelmänId = lähdejärjestelmäId)

  def opiskeluoikeusPurkamisella: VapaanSivistystyönOpiskeluoikeus = defaultOpiskeluoikeus.copy(
    lähdejärjestelmäkytkentäPurettu = Some(LähdejärjestelmäkytkennänPurkaminen(purettu = LocalDateTime.now()))
  )

  private def purettavaVstOpiskeluoikeusOid: String = {
    val result = app.opiskeluoikeusRepository.createOrUpdate(
      UnverifiedHenkilöOid(oppija.oid, app.henkilöRepository),
      defaultOpiskeluoikeus,
      allowUpdate = true,
    )(KoskiSpecificSession.systemUser)

    result.toOption.get.oid
  }

  private def purettavaLukioOpiskeluoikeusOid: String = {
    val result = app.opiskeluoikeusRepository.createOrUpdate(
      UnverifiedHenkilöOid(oppija.oid, app.henkilöRepository),
      päättynytLukioOpiskeluoikeus,
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
