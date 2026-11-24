package fi.oph.koski.ibis

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExamplesIBDiploma
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.schema.{IBDiplomaOpiskeluoikeus, IBOpiskeluoikeus}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import scala.io.Source

class IbisServletSpec extends AnyFreeSpec with KoskiHttpSpec with PutOpiskeluoikeusTestMethods[IBOpiskeluoikeus] with BeforeAndAfterAll {
  override def tag = implicitly[reflect.runtime.universe.TypeTag[IBOpiskeluoikeus]]
  override def defaultOpiskeluoikeus: IBOpiskeluoikeus = ibKoulutus
  // IB Diplomien oppilaitos on IBO, johon oletuskäyttäjällä kallella ei ole oikeuksia.
  override def defaultUser = MockUsers.paakayttaja

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    resetFixtures()
  }

  private val tunniste = "new123"
  private lazy val ibKoulutus = ExamplesIBDiploma.ibKoulutuksenOpiskeluoikeus.copy(iboTunniste = Some(tunniste))

  "IBIS-tuonnin rajapinta" - {
    "on sallittu vain OPH-pääkäyttäjälle" in {
      postTiedosto("esikatselu", Some(tiedostoTunnisteella(tunniste)), MockUsers.kalle) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden("Sallittu vain OPH-pääkäyttäjälle"))
      }
    }

    "vaatii tiedoston" in {
      postTiedosto("esikatselu", None) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest("Tiedosto puuttuu"))
      }
    }

    "esikatselu palauttaa tuloksen tallentamatta diplomaa" in {
      val oppijaOid = luoOppijaIbKoulutuksella()

      postTiedosto("esikatselu", Some(tiedostoTunnisteella(tunniste))) {
        verifyResponseStatusOk()
        val tulos = JsonSerializer.parse[IbisTuonninTulos](body)
        tulos.tallennettu should be(false)
        tulos.rivit.map(rivi => (rivi.iboTunniste, rivi.tila, rivi.oppijaOid, rivi.virheet)) should be(
          List((tunniste, IbisTuonninRivinTila.Uusi, Some(oppijaOid), Nil))
        )
      }
      ibDiplomat(oppijaOid) should be(empty)
    }

    "tuonti tallentaa uuden diploman" in {
      val oppijaOid = luoOppijaIbKoulutuksella()

      postTiedosto("tuonti", Some(tiedostoTunnisteella(tunniste))) {
        verifyResponseStatusOk()
        val tulos = JsonSerializer.parse[IbisTuonninTulos](body)
        tulos.tallennettu should be(true)
        tulos.rivit.map(rivi => (rivi.tila, rivi.virheet)) should be(List((IbisTuonninRivinTila.Uusi, Nil)))
        ibDiplomat(oppijaOid).flatMap(_.oid) should be(tulos.rivit.flatMap(_.diplomanOid))
      }
    }
  }

  private def luoOppijaIbKoulutuksella(): String =
    setupOppijaWithOpiskeluoikeus(ibKoulutus, henkilö = KoskiSpecificMockOppijat.tyhjä) {
      verifyResponseStatusOk()
      readPutOppijaResponse.henkilö.oid
    }

  private def ibDiplomat(oppijaOid: String): Seq[IBDiplomaOpiskeluoikeus] =
    getOpiskeluoikeudet(oppijaOid).collect { case diploma: IBDiplomaOpiskeluoikeus => diploma }

  // Esimerkkitiedoston ensimmäinen rivi on myönnetty diploma.
  private def tiedostoTunnisteella(iboTunniste: String): String =
    Source.fromInputStream(getClass.getResourceAsStream("/ibis/esimerkki.csv"), "UTF-8").getLines().next()
      .replace("\"abc123\"", s"\"$iboTunniste\"") + "\n"

  private def postTiedosto[A](polku: String, sisältö: Option[String], käyttäjä: UserWithPassword = MockUsers.paakayttaja)(f: => A): A =
    post(
      s"api/ibis/$polku",
      params = Seq.empty,
      files = sisältö.map(s => "tiedosto" -> tiedostoksi(s)).toSeq,
      headers = authHeaders(käyttäjä),
    )(f)

  private def tiedostoksi(sisältö: String): File = {
    val tiedosto = File.createTempFile("ibis", ".csv")
    tiedosto.deleteOnExit()
    Files.write(tiedosto.toPath, sisältö.getBytes(StandardCharsets.UTF_8))
    tiedosto
  }
}
