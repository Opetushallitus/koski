package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.{JsonSerializer, SensitiveDataAllowed}
import fi.oph.koski.koskiuser.RequiresTilastokeskus
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueries
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, NoCache, ObservableSupport}
import org.json4s.JValue

class TilastokeskusServlet(implicit val application: KoskiApplication) extends ApiServlet with ObservableSupport with NoCache with OpiskeluoikeusQueries with RequiresTilastokeskus {

  override protected val maxNumberOfItemsPerPage: Int = 1000

  get("/") {
    if (!getOptionalIntegerParam("v").contains(1)) {
      haltWithStatus(KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
    }

    val serialize: TilastokeskusOppija => JValue = OperatingContextForStreaming(koskiSession).serialize _
    val oppijaStream = queryOpiskeluoikeudet.map {
      _.map { case (h, oos) => mkOppija(h, oos) }
       .map(serialize)
    }

    streamResponse(oppijaStream, koskiSession)
  }

  private def mkOppija(henkilö: LaajatOppijaHenkilöTiedot, opiskeluoikeudet: List[KoskeenTallennettavaOpiskeluoikeus]) = TilastokeskusOppija(
    henkilö = mkTilastokeskusHenkilö(henkilö),
    opiskeluoikeudet = opiskeluoikeudet
  )

  private def mkTilastokeskusHenkilö(laajat: LaajatOppijaHenkilöTiedot): TilastokeskusHenkilötiedot = {
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
      turvakielto = laajat.turvakielto,
      linkitetytOidit = laajat.linkitetytOidit
    )
  }
}

// To ensure that none of the  Scalatra threadlocals are used.
case class OperatingContextForStreaming(user: SensitiveDataAllowed) {
  private implicit val u: SensitiveDataAllowed = user
  def serialize(oppija: TilastokeskusOppija): JValue = JsonSerializer.serialize(oppija)
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
  turvakielto: Boolean,
  linkitetytOidit: List[Oid]
)
