package fi.oph.koski.ytl

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{KoskiTables, OpiskeluoikeusRow}
import fi.oph.koski.henkilo.{LaajatOppijaHenkilöTiedot, OppijaHenkilö}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.{JsonSerializer, SensitiveDataAllowed}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log._
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryContext, OpiskeluoikeusQueryFilter, QueryOppijaHenkilö}
import fi.oph.koski.organisaatio.Oppilaitostyyppi
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{Henkilö, KoskiSchema}
import org.json4s.JsonAST.JValue
import org.json4s.MappingException
import rx.lang.scala.Observable

class YtlService(application: KoskiApplication) extends Logging {

  private lazy val opiskeluoikeudenTyyppiFilter =
    OpiskeluoikeusQueryFilter.OneOfOpiskeluoikeudenTyypit(
      YtlSchema.schemassaTuetutOpiskeluoikeustyypit.map(
        tyyppi => OpiskeluoikeusQueryFilter.OpiskeluoikeudenTyyppi(
          application.koodistoViitePalvelu.validateRequired("opiskeluoikeudentyyppi", tyyppi)
        )
      )
    )

  def streamOppijat(oidit: Seq[String], hetut: Seq[String], opiskeluoikeuksiaMuuttunutJälkeen: Option[Instant])(implicit user: KoskiSpecificSession): Observable[JValue] = {
    val pyydetytHenkilöt: Seq[OppijaHenkilö] =
      application.opintopolkuHenkilöFacade.findOppijatNoSlaveOids(oidit) ++
        application.opintopolkuHenkilöFacade.findOppijatByHetusNoSlaveOids(hetut)

    val oidToPyydettyHenkilö: Map[Henkilö.Oid, OppijaHenkilö] = pyydetytHenkilöt.map(h => h.oid -> h).toMap

    val pyydetytOidit: List[Henkilö.Oid] = oidToPyydettyHenkilö.keys.toList
    val masterHenkilöt: Map[Henkilö.Oid, LaajatOppijaHenkilöTiedot] =
      application.opintopolkuHenkilöFacade.findMasterOppijat(pyydetytOidit)
    val masterOppijanSlaveOidit: Map[Henkilö.Oid, List[Henkilö.Oid]] = masterHenkilöt.collect {
      case (_, hlö) =>
        (hlö.oid, hlö.linkitetytOidit)
    }

    val masterOppijoidenOidit: Seq[Henkilö.Oid] = masterHenkilöt.values.map(_.oid).toSeq

    val haettavatOppijaOidit: Seq[Henkilö.Oid] =
      masterOppijoidenOidit ++ application.henkilöCache.resolveLinkedOids(masterOppijoidenOidit)

    val queryFilters = List(
      opiskeluoikeudenTyyppiFilter,
      OpiskeluoikeusQueryFilter.OppijaOidHaku(haettavatOppijaOidit)
    )

    OpiskeluoikeusQueryContext.streamingQueryGroupedByOid(application, queryFilters, None)
      .map {
        case (oppijaHenkilö: QueryOppijaHenkilö, opiskeluoikeusRows: List[OpiskeluoikeusRow]) =>
          val oppijaOnLinkitetty = masterOppijanSlaveOidit(oppijaHenkilö.oid).nonEmpty

          lazy val oppijallaOnMuuttuneitaOpiskeluoikeuksia = opiskeluoikeuksiaMuuttunutJälkeen
            .map(Timestamp.from)
            .forall(opiskeluoikeuksiaMuuttunutJälkeen =>
              opiskeluoikeusRows.exists(!_.aikaleima.before(opiskeluoikeuksiaMuuttunutJälkeen))
            )

          lazy val haetaanAikaleimallaJaOppijallaOnUusiaMitätöityjäOpiskeluoikeuksia = opiskeluoikeuksiaMuuttunutJälkeen
            .map(Timestamp.from)
            .exists(opiskeluoikeuksiaMuuttunutJälkeen =>
              opiskeluoikeusRows.exists(oo => oo.mitätöity && !oo.aikaleima.before(opiskeluoikeuksiaMuuttunutJälkeen))
            )

          lazy val opiskeluoikeudet =
            opiskeluoikeusRows
              .filterNot(_.mitätöity)
              .map(toYtlOpiskeluoikeus)
              .flatMap(oo => {
                val tarkistettavatOidit = oo.kaikkiMahdollisetOppilaitosOiditRakenteessa.toSet

                // TODO: organisaatiohierarkian läpikäynti saattaa olla sen verran hidasta, että tämä kannattaisi
                // siirtää organisaatioRepositoryyn ja tehdä sinne erillinen cache. Suorituskykytestataan nyt kuitenkin
                // ensin.
                val onErityisoppilaitos = tarkistettavatOidit.exists(oid =>
                  application.organisaatioService.organisaatioRepository.findWithOid(oid)
                    .exists(_.children.exists(_.oppilaitostyyppi.contains(Oppilaitostyyppi.ammatillisetErityisoppilaitokset)))
                )
                oo.siivoaTiedot(
                  poistaOrganisaatiotiedot = onErityisoppilaitos
                )
              })

          if (oppijaOnLinkitetty || (
              oppijallaOnMuuttuneitaOpiskeluoikeuksia &&
              (opiskeluoikeudet.nonEmpty || haetaanAikaleimallaJaOppijallaOnUusiaMitätöityjäOpiskeluoikeuksia))
          ) {
            // Etsi kaikki oppija-oidit, joilla tätä henkilöä on alunperin kyselty. Vastaus palautetaam jokaiselle
            // niistä, jotta kutsuja saa oikean ja riittävän tiedon siitä, millä oideilla opiskeluikeuksia löytyi.
            val hlöt = {
              // Epäloogisesti oppijaHenkilössä eivät palaudu oppijan kaikkia slave-oidit, vaikka se linkitetytOid:it
              // kentän sisältääkin. Siksi pitää käyttää erikseen masterOppijanSlaveOidit-mappiä.
              val etsittävätHenkilöOidit = Seq(oppijaHenkilö.oid) ++ masterOppijanSlaveOidit(oppijaHenkilö.oid)
              oidToPyydettyHenkilö.collect { case (oid, hlö) if etsittävätHenkilöOidit.contains(oid) => hlö }
            }
            hlöt.map(hlö =>
              Some(YtlOppija(
                henkilö = YtlHenkilö(hlö, hlö.äidinkieli.flatMap(k => application.koodistoViitePalvelu.validate("kieli", k.toUpperCase))),
                opiskeluoikeudet = opiskeluoikeudet
              ))
            )
          } else {
            Seq.empty
          }
      }
      .filter(_.nonEmpty)
      .flatMap(o => Observable.from(o))
      .map(_.getOrElse(throw new InternalError("Internal error")))
      .doOnEach(auditLogOpiskeluoikeusKatsominen(_)(user))
      .map(JsonSerializer.serializeWithUser(user))
  }

  private def toYtlOpiskeluoikeus(row: OpiskeluoikeusRow)(implicit user: SensitiveDataAllowed): YtlOpiskeluoikeus = {
    deserializeYtlOpiskeluoikeus(row.data, row.oid, row.versionumero, row.aikaleima) match {
      case Right(oo) => oo
      case Left(errors) =>
        throw new MappingException(s"Error deserializing YTL opiskeluoikeus ${row.oid} for oppija ${row.oppijaOid}: ${errors}")
    }
  }

  private def deserializeYtlOpiskeluoikeus(data: JValue, oid: String, versionumero: Int, aikaleima: Timestamp): Either[HttpStatus, YtlOpiskeluoikeus] = {
    val json = KoskiTables.OpiskeluoikeusTable.readAsJValue(data, oid, versionumero, aikaleima)

    application.validatingAndResolvingExtractor.extract[YtlOpiskeluoikeus](
      KoskiSchema.lenientDeserializationWithIgnoringNonValidatingListItems
    )(json)
  }

  private def auditLogOpiskeluoikeusKatsominen(oppija: YtlOppija)(koskiSession: KoskiSpecificSession): Unit =
    AuditLog.log(KoskiAuditLogMessage(KoskiOperation.OPISKELUOIKEUS_KATSOMINEN, koskiSession, Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> oppija.henkilö.oid)))
}

case class OidVersionTimestamp(oid: String, versionumero: Int, aikaleima: LocalDateTime)
