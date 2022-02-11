package fi.oph.koski.ytl

import java.time.{Instant, LocalDateTime}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, OppijaHenkilö}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.organisaatio.Oppilaitostyyppi
import fi.oph.koski.schema.Henkilö
import org.json4s.JsonAST.JValue
import rx.lang.scala.Observable

class YtlService(application: KoskiApplication) extends Logging {
  private val ytlOpiskeluoikeusRepository = new YtlOpiskeluoikeusRepository(application.replicaDatabase.db, application.validatingAndResolvingExtractor)

  def streamOppijat(
    oidit: Seq[String],
    hetut: Seq[String],
    opiskeluoikeuksiaMuuttunutJälkeen: Option[Instant]
  )(implicit user: KoskiSpecificSession): Observable[JValue] = {
    val pyydetytHenkilöt: Seq[OppijaHenkilö] =
      application.opintopolkuHenkilöFacade.findOppijatNoSlaveOids(oidit) ++
        application.opintopolkuHenkilöFacade.findOppijatByHetusNoSlaveOids(hetut)

    val oppijaOidToPyydettyHenkilö: Map[Henkilö.Oid, OppijaHenkilö] = pyydetytHenkilöt.map(h => h.oid -> h).toMap

    val pyydetytOppijaOidit: List[Henkilö.Oid] = oppijaOidToPyydettyHenkilö.keys.toList

    // Haku pitää tehdä master-oppijoiden kautta, jotta saadaan palautettua opiskeluoikeudet myös mahdollisilta
    // sisaruksena olevilta linkitetyiltä oppijoilta.
    val masterHenkilöt: Map[Henkilö.Oid, LaajatOppijaHenkilöTiedot] =
      application.opintopolkuHenkilöFacade.findMasterOppijat(pyydetytOppijaOidit)
    val masterOppijanLinkitetytOppijaOidit: Map[Henkilö.Oid, List[Henkilö.Oid]] = masterHenkilöt.collect {
      case (_, hlö) =>
        (hlö.oid, hlö.linkitetytOidit)
    }

    val masterOppijoidenOidit: Seq[Henkilö.Oid] = masterHenkilöt.values.map(_.oid).toSeq

    def teePalautettavatYtlHenkilöt(oppijaMasterOid: String): Iterable[YtlHenkilö] = {
      // Luo YTLHenkilöt oppijoista, joilla oppijaa on alunperin pyydetty. Lista opiskeluoikeuksia palautetaan jokaiselle
      // niistä, jotta kutsuja saa oikean ja riittävän tiedon siitä, millä kutsujan antamilla oppija-oideilla
      // opiskeluoikeuksia löytyi.
      val etsittävätHenkilöOidit =
        Seq(oppijaMasterOid) ++ masterOppijanLinkitetytOppijaOidit(oppijaMasterOid)

      val pyynnössäEsiintyneetOppijaHenkilöt =
        oppijaOidToPyydettyHenkilö.collect { case (oid, hlö) if etsittävätHenkilöOidit.contains(oid) => hlö }

      pyynnössäEsiintyneetOppijaHenkilöt.map(hlö => {
        val pääoppijaOid = Some(oppijaMasterOid)

        YtlHenkilö(
          hlö = hlö,
          pääoppijaOid = pääoppijaOid,
          äidinkieli = hlö.äidinkieli.flatMap(k => application.koodistoViitePalvelu.validate("kieli", k.toUpperCase))
        )
      })
    }

    def teePalautettavatYtlOppijat(oppijaMasterOid: String, opiskeluoikeusRows: Seq[OppijanOpiskeluoikeusRow]): Iterable[YtlOppija] = {
      val opiskeluoikeudet =
        opiskeluoikeusRows
          .filterNot(_.mitätöity)
          .map(_.opiskeluoikeus)
          .flatMap(siivoaOpiskeluoikeus)

      val mitätöityjäAikaleimanJälkeen =
        opiskeluoikeusRows.filter(_.mitätöityAikaleimanJälkeen).nonEmpty

      if (mitätöityjäAikaleimanJälkeen || opiskeluoikeudet.nonEmpty) {
        teePalautettavatYtlHenkilöt(oppijaMasterOid).map(ytlHenkilö =>
          YtlOppija(
            henkilö = ytlHenkilö,
            opiskeluoikeudet = opiskeluoikeudet.toList
          )
        )
      }
      else {
        Seq.empty
      }
    }

    val opiskeluoikeudet: Seq[OppijanOpiskeluoikeusRow] = {
      ytlOpiskeluoikeusRepository.getOppijanKaikkiOpiskeluoikeudetJosJokinNiistäOnPäivittynytAikaleimanJälkeen(
        palautettavatOpiskeluoikeudenTyypit = YtlSchema.schemassaTuetutOpiskeluoikeustyypit,
        oppijaMasterOids = masterOppijoidenOidit.toList,
        aikaleimaInstant = opiskeluoikeuksiaMuuttunutJälkeen
      )
    }

    Observable.from(
      opiskeluoikeudet.groupBy(_.masterOppijaOid).flatMap {
        case (oppijaMasterOid, opiskeluoikeusRows) => teePalautettavatYtlOppijat(oppijaMasterOid, opiskeluoikeusRows)
      })
      .doOnEach(auditLogOpiskeluoikeusKatsominen(_)(user))
      .map(JsonSerializer.serializeWithUser(user))
  }

  private def siivoaOpiskeluoikeus(oo: YtlOpiskeluoikeus): Option[YtlOpiskeluoikeus] = {
    val tarkistettavatOidit = oo.kaikkiMahdollisetOppilaitosOiditRakenteessa.toSet

    val onErityisoppilaitos = tarkistettavatOidit.exists(oid =>
      application.organisaatioService.organisaatioRepository.findWithOid(oid)
        .exists(_.children.exists(_.oppilaitostyyppi.contains(Oppilaitostyyppi.ammatillisetErityisoppilaitokset)))
    )

    oo.siivoaTiedot(
      poistaOrganisaatiotiedot = onErityisoppilaitos
    )
  }

  private def auditLogOpiskeluoikeusKatsominen(oppija: YtlOppija)(koskiSession: KoskiSpecificSession): Unit =
    AuditLog.log(KoskiAuditLogMessage(KoskiOperation.OPISKELUOIKEUS_KATSOMINEN, koskiSession, Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> oppija.henkilö.oid)))
}

case class OidVersionTimestamp(oid: String, versionumero: Int, aikaleima: LocalDateTime)
