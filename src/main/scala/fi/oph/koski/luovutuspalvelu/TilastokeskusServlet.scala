package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.{JsonSerializer, SensitiveDataAllowed}
import fi.oph.koski.koskiuser.RequiresTilastokeskus
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueries
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, NoCache, ObservableSupport}
import org.json4s.JValue

import scala.collection.immutable

class TilastokeskusServlet(implicit val application: KoskiApplication) extends ApiServlet with ObservableSupport with NoCache with OpiskeluoikeusQueries with RequiresTilastokeskus {

  override protected val maxNumberOfItemsPerPage: Int = 1000

  get("/") {
    if (!getOptionalIntegerParam("v").contains(1)) {
      haltWithStatus(KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
    }

    val serializer = TilastokeskusSensitiveDataFilter(koskiSession).rowSerializer

    val oppijat = performOpiskeluoikeudetQueryLaajoillaHenkilötiedoilla.map(observable => observable
      .map(x => (laajatHenkilötiedotToTilastokeskusHenkilötiedot(x._1), x._2))
      .map(serializer)
    )

    streamResponse(oppijat, koskiSession)
  }

  private def laajatHenkilötiedotToTilastokeskusHenkilötiedot(laajat: LaajatOppijaHenkilöTiedot): TilastokeskusHenkilötiedot = {
    val täydelliset: TäydellisetHenkilötiedot = application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(laajat)

    TilastokeskusHenkilötiedot(
      oid = täydelliset.oid,
      hetu = täydelliset.hetu,
      syntymäaika = täydelliset.syntymäaika,
      etunimet = täydelliset.etunimet,
      kutsumanimi = täydelliset.kutsumanimi,
      sukunimi = täydelliset.sukunimi,
      sukupuoli = laajat.sukupuoli,
      kotikunta = laajat.kotikunta,
      äidinkieli = täydelliset.äidinkieli,
      kansalaisuus = täydelliset.kansalaisuus,
      turvakielto = täydelliset.turvakielto
    )
  }
}

case class TilastokeskusSensitiveDataFilter(user: SensitiveDataAllowed) {
  private implicit val u = user
  def rowSerializer: ((TilastokeskusHenkilötiedot, immutable.Seq[OpiskeluoikeusRow])) => JValue = {
    def ser(tuple: (TilastokeskusHenkilötiedot, immutable.Seq[OpiskeluoikeusRow])) = {
      JsonSerializer.serialize(TilastokeskusOppija(tuple._1, tuple._2.map(_.toOpiskeluoikeus)))
    }
    ser
  }
}

case class TilastokeskusOppija(
  henkilö: TilastokeskusHenkilötiedot,
  opiskeluoikeudet: Seq[Opiskeluoikeus]
)

case class TilastokeskusHenkilötiedot(
  oid: Henkilö.Oid,
  hetu: Option[Henkilö.Hetu],
  syntymäaika: Option[LocalDate],
  etunimet: String,
  kutsumanimi: String,
  sukunimi: String,
  sukupuoli: Option[String],
  kotikunta: Option[String],
  äidinkieli: Option[Koodistokoodiviite],
  kansalaisuus: Option[List[Koodistokoodiviite]],
  turvakielto: Option[Boolean] = None
)
