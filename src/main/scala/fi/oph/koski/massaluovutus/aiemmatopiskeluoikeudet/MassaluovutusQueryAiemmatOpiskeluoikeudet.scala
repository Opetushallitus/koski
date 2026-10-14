package fi.oph.koski.massaluovutus.aiemmatopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiOpiskeluoikeusRowImplicits._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db._
import fi.oph.koski.henkilo.{Hetu, KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.{SensitiveAndRedundantDataFilter, SensitiveDataAllowed}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession, Rooli, Session}
import fi.oph.koski.log.AuditLogMessage.AuditLogMessageField
import fi.oph.koski.log.{KoskiAuditLogMessageField, KoskiOperation, Logging, OpiskeluoikeusOiditAuditLog}
import fi.oph.koski.massaluovutus.MassaluovutusUtils.QueryResourceManager
import fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusOppija
import fi.oph.koski.massaluovutus.{KoulutuksenjärjestäjienMassaluovutusQueryParameters, MassaluovutusException, MassaluovutusUtils, QueryFormat, QueryResultWriter}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryContext
import fi.oph.koski.organisaatio.{MockOrganisaatiot, OrganisaationAlaisetOrganisaatiot}
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.schema.annotation.{EnumValues, HiddenFromAiemmatOpiskeluoikeudet}
import fi.oph.koski.util.Retry.retryWithInterval
import fi.oph.scalaschema.Serializer
import fi.oph.scalaschema.annotation.{Description, Title}
import org.json4s.JValue

import java.sql.Date
import java.time.LocalDate
import scala.concurrent.duration.DurationInt

@Title("Nykyisten opiskelijoiden aiemmat opiskeluoikeudet")
@Description("Palauttaa annettujen, oppilaitoksen nykyisten opiskelijoiden esi-, perus-, ammatillisen ja lukiokoulutuksen opiskeluoikeudet, jotka ovat alkaneet ennen opiskelijan opintoja kyselyn oppilaitoksessa. Mukana ovat sekä muiden organisaatioiden että oppilaitoksen omat aiemmat opiskeluoikeudet.")
@Description("Oppijat annetaan henkilötunnuksina tai oppijanumeroina. Jokaisen annetun oppijan on oltava oppilaitoksen nykyinen opiskelija ja alle 18-vuotias tai kyselyn tekovuonna 18 vuotta täyttävä, muuten koko kysely hylätään ja vastaus kertoo, mitkä oppijat eivät täytä ehtoja. Nykyinen opiskelija on oppija, jolla on kyselyn oppilaitoksessa päättymätön, myös vasta tulevaisuudessa alkava, esi-, perus-, ammatillisen, lukio-, TUVA- tai IB-koulutuksen opiskeluoikeus. Kysely tehdään aina yhdelle oppilaitokselle.")
@Description("Kysely edellyttää AIEMMAT_OPISKELUOIKEUDET-käyttöoikeutta kyselyn oppilaitoksessa. Palautettavat koulutusmuodot eivät riipu käyttäjän omista koulutusmuotokohtaisista käyttöoikeuksista, vaan ainoastaan tästä roolista. Arkaluontoiset tiedot palautetaan vain, jos käyttäjällä on niihin oikeus. Skeemassa HiddenFromAiemmatOpiskeluoikeudet-merkinnällä varustetut kentät (esim. vankilaopetuksessa ja käyttäytymisenArvio) jätetään pois käyttöoikeuksista riippumatta.")
@Description("Tulostiedostot vastaavat organisaationOpiskeluoikeudet-kyselyn json-muotoa: yksi tiedosto oppijaa kohden.")
case class MassaluovutusQueryAiemmatOpiskeluoikeudet(
  @EnumValues(Set("aiemmatOpiskeluoikeudet"))
  `type`: String = "aiemmatOpiskeluoikeudet",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  @Description("Oppilaitoksen oid, jonka nykyisten opiskelijoiden aiemmat opiskeluoikeudet haetaan. Koulutustoimijan tai kunnan oidia ei hyväksytä.")
  organisaatioOid: Organisaatio.Oid,
  @Description("Haettavat oppijat henkilötunnuksina. Kentässä hetut tai oppijaOidit on annettava vähintään yksi oppija.")
  hetut: Option[List[String]] = None,
  @Description("Haettavat oppijat oppijanumeroina.")
  oppijaOidit: Option[List[String]] = None,
  @Description("Palautettavat koulutusmuodot. Oletuksena kaikki sallitut: esiopetus, perusopetus, ammatillinenkoulutus, lukiokoulutus.")
  koulutusmuoto: Option[List[String]] = None,
) extends KoulutuksenjärjestäjienMassaluovutusQueryParameters with DatabaseConverters with Logging {
  import MassaluovutusQueryAiemmatOpiskeluoikeudet._

  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: Session with SensitiveDataAllowed): Either[String, Unit] =
    withKoskiSpecificSession { implicit u =>
      QueryResourceManager(logger) { _ =>
        val db = application.replicaDatabase.db
        val opiskelijat = haeNykyisetOpiskelijat(application, LocalDate.now(), onrYritykset = 5)
          .fold(status => throw new MassaluovutusException(status.errorString.getOrElse("Kyselyn oppijat eivät täytä ehtoja")), identity)

        writer.predictFileCount(opiskelijat.size)
        opiskelijat.foreach { opiskelija =>
          val henkilö = opiskelija.henkilö
          val aiemmat = haeAiemmatOpiskeluoikeudet(db, henkilö, opiskelija.opintojenAlku)
          if (aiemmat.isEmpty) {
            writer.skipFile()
          } else {
            writer.putJson(henkilö.oid, serialize(MassaluovutusOppija(
              henkilö = application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(henkilö),
              linkitetytOidit = henkilö.linkitetytOidit,
              opiskeluoikeudet = aiemmat.map(MassaluovutusUtils.toKoskeenTallennettavaOpiskeluoikeus(application, logger)),
            )))
            auditLog(henkilö.oid, aiemmat.map(_.oid))
          }
        }
      }
    }

  override def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean = withKoskiSpecificSession { u =>
    val onOppilaitos = application.organisaatioRepository.getOrganisaatio(organisaatioOid).exists(_.toOppilaitos.isDefined)
    onOppilaitos && (
      u.hasGlobalReadAccess || (
        u.organisationOids(AccessType.read).contains(organisaatioOid)
          && u.hasRoleInOrganisaatio(Rooli.AIEMMAT_OPISKELUOIKEUDET, organisaatioOid)
      )
    )
  }

  override def fillAndValidate(implicit user: Session): Either[HttpStatus, MassaluovutusQueryAiemmatOpiskeluoikeudet] = {
    val käyttöoikeusMuuhunKuinOppilaitokseen = user.orgKäyttöoikeudet.exists(k => k.organisaatio.oid == organisaatioOid && k.organisaatio.toOppilaitos.isEmpty)
    val tuntemattomat = koulutusmuoto.toList.flatten.filterNot(sallitutKoulutusmuodot.contains)
    if (käyttöoikeusMuuhunKuinOppilaitokseen) {
      Left(KoskiErrorCategory.badRequest.massaluovutus.eiOppilaitos())
    } else if (annetutHetut.isEmpty && annetutOidit.isEmpty) {
      Left(KoskiErrorCategory.badRequest.massaluovutus.eiOppijoita())
    } else if (tuntemattomat.nonEmpty) {
      Left(KoskiErrorCategory.badRequest(s"Tuntematon koulutusmuoto: ${tuntemattomat.mkString(", ")}. Sallitut arvot: ${sallitutKoulutusmuodot.mkString(", ")}"))
    } else {
      Right(copy(koulutusmuoto = koulutusmuoto.orElse(Some(sallitutKoulutusmuodot))))
    }
  }

  override def validateWithApplication(application: KoskiApplication)(implicit user: Session): Either[HttpStatus, MassaluovutusQueryAiemmatOpiskeluoikeudet] = user match {
    case u: KoskiSpecificSession => haeNykyisetOpiskelijat(application, LocalDate.now(), onrYritykset = 1)(u).map(_ => this)
    case _ => Left(KoskiErrorCategory.forbidden())
  }

  private def serialize(oppija: MassaluovutusOppija)(implicit user: SensitiveDataAllowed): JValue = {
    val sensitiveFilter = SensitiveAndRedundantDataFilter(user)
    val context = sensitiveFilter.serializationContext.copy(propertyProcessor = (schema, property) =>
      if (property.metadata.exists(_.isInstanceOf[HiddenFromAiemmatOpiskeluoikeudet])) Nil else sensitiveFilter.filterSensitiveData(schema, property)
    )
    Serializer.serialize(oppija, context)
  }

  private def annetutHetut: List[String] = hetut.toList.flatten
  private def annetutOidit: List[String] = oppijaOidit.toList.flatten

  // Sama tarkistus ajetaan kyselyä luotaessa (hylkäys 400-vastauksena) ja uudelleen kyselyä suoritettaessa
  private def haeNykyisetOpiskelijat(application: KoskiApplication, tänään: LocalDate, onrYritykset: Int)(implicit user: KoskiSpecificSession): Either[HttpStatus, List[NykyinenOpiskelija]] =
    HttpStatus.foldEithers(annetutHetut.map(application.hetu.validate)).flatMap { _ =>
      val onr = application.opintopolkuHenkilöFacade
      val henkilötHetulla = annetutHetut.map(hetu => hetu -> retryWithInterval(onrYritykset, 5.seconds.toMillis)(onr.findOppijaByHetu(hetu)))
      val masteritOidilla = retryWithInterval(onrYritykset, 5.seconds.toMillis)(onr.findMasterOppijatWithSlaveOids(annetutOidit))
      val annetut: List[(String, Option[LaajatOppijaHenkilöTiedot])] = henkilötHetulla ++ annetutOidit.map(oid => oid -> masteritOidilla.get(oid))
      val henkilöt = annetut.flatMap(_._2).distinctBy(_.oid)

      val organisaatiot = application.organisaatioService.organisaationAlaisetOrganisaatiot(organisaatioOid)
      val omatOpiskeluoikeudet = haeOmatOpiskeluoikeudet(application.replicaDatabase.db, organisaatiot, henkilöt.flatMap(_.kaikkiOidit), tänään)
      val opiskelijat = henkilöt.flatMap { henkilö =>
        val omat = omatOpiskeluoikeudet.filter(oo => henkilö.kaikkiOidit.contains(oo.oppijaOid))
        Option.when(omat.nonEmpty)(NykyinenOpiskelija(henkilö, omat.map(_.alkamispäivä).min, omat.map(_.koulutusmuoto).toSet))
      }
      val opiskelijaOidit = opiskelijat.map(_.henkilö.oid).toSet

      // Tuntematonta tunnistetta ei eroteta muusta kuin nykyisestä opiskelijasta, jottei vastaus paljasta, onko henkilö rekisterissä
      val eiNykyiset = annetut.collect { case (tunniste, henkilö) if !henkilö.exists(h => opiskelijaOidit.contains(h.oid)) => tunniste }
      val täysiIkäiset = annetut.collect { case (tunniste, Some(henkilö)) if täyttänyt18EnnenVuotta(henkilö, tänään.getYear) => tunniste }
      HttpStatus.fold(
        HttpStatus.validate(eiNykyiset.isEmpty)(KoskiErrorCategory.badRequest.massaluovutus.eiNykyinenOpiskelija(s"Ei oppilaitoksen nykyisiä opiskelijoita: ${eiNykyiset.mkString(", ")}")),
        HttpStatus.validate(täysiIkäiset.isEmpty)(KoskiErrorCategory.badRequest.massaluovutus.täyttänyt18EnnenKyselyvuotta(s"Täyttänyt 18 vuotta ennen vuotta ${tänään.getYear}: ${täysiIkäiset.mkString(", ")}")),
      ).toEither.map(_ => opiskelijat)
    }

  // Syntymäaikaa vailla oleva oppija hylätään, koska ehtoa ei voi tarkistaa
  private def täyttänyt18EnnenVuotta(henkilö: LaajatOppijaHenkilöTiedot, vuosi: Int): Boolean =
    !henkilö.syntymäaika.orElse(henkilö.hetu.flatMap(Hetu.toBirthday)).exists(_.getYear >= vuosi - 18)

  private def haeOmatOpiskeluoikeudet(db: DB, organisaatiot: OrganisaationAlaisetOrganisaatiot, oppijaOidit: List[String], tänään: LocalDate)(implicit user: KoskiSpecificSession): Seq[OmaOpiskeluoikeus] = {
    val koulutusmuodot = nykyisenOpiskelijanKoulutusmuodot.intersect(user.allowedOpiskeluoikeudetJaPäätasonSuoritukset.toOpiskeluoikeudenTyypit.toList)
    QueryMethods.runDbSync(db, SQLHelpers.concatMany(
      Some(sql"""
        SELECT oppija_oid, koulutusmuoto, alkamispaiva
        FROM opiskeluoikeus
        WHERE NOT poistettu AND NOT mitatoity
          AND oppija_oid = ANY($oppijaOidit)
          AND koulutusmuoto = ANY($koulutusmuodot)
          AND (paattymispaiva IS NULL OR paattymispaiva > $tänään)
          AND
      """),
      Some(MassaluovutusUtils.organisaatioFilter(organisaatiot)),
    ).as[(String, String, Date)])
      .map { case (oppijaOid, koulutusmuoto, alkamispäivä) => OmaOpiskeluoikeus(oppijaOid, koulutusmuoto, alkamispäivä.toLocalDate) }
  }

  private def haeAiemmatOpiskeluoikeudet(db: DB, henkilö: LaajatOppijaHenkilöTiedot, alkanutEnnen: LocalDate): Seq[KoskiOpiskeluoikeusRow] = {
    // Tarkoituksella ei rajata käyttäjän omilla koulutusmuoto-oikeuksilla (toisin kuin nykyisten opiskelijoiden haussa):
    // esim. lukio tarvitsee opiskelijoidensa perusopetuksen tiedot, vaikka sillä ei ole perusopetuksen käyttöoikeutta.
    val koulutusmuodot: List[String] = koulutusmuoto.toList.flatten
    QueryMethods.runDbSync(db, sql"""
        SELECT *
        FROM opiskeluoikeus
        WHERE NOT poistettu AND NOT mitatoity
          AND oppija_oid = ANY(${henkilö.kaikkiOidit})
          AND koulutusmuoto = ANY($koulutusmuodot)
          AND alkamispaiva < $alkanutEnnen
        ORDER BY alkamispaiva
      """.as[KoskiOpiskeluoikeusRow])
  }

  private def auditLog(oppijaOid: String, opiskeluoikeusOidit: Seq[String])(implicit user: KoskiSpecificSession): Unit =
    OpiskeluoikeusOiditAuditLog.log(
      KoskiOperation.AIEMMAT_OPISKELUOIKEUDET_KATSOMINEN,
      oppijaOid,
      opiskeluoikeusOidit,
      Map[AuditLogMessageField, String](KoskiAuditLogMessageField.hakuEhto -> OpiskeluoikeusQueryContext.queryForAuditLog(Map(
        "organisaatio" -> List(organisaatioOid),
        "koulutusmuoto" -> koulutusmuoto.toList.flatten,
      ))),
    )
}

object MassaluovutusQueryAiemmatOpiskeluoikeudet {
  val sallitutKoulutusmuodot: List[String] = List("esiopetus", "perusopetus", "ammatillinenkoulutus", "lukiokoulutus")
  // Koulutusmuodot, joiden opiskelijana oppija on kyselyn oppilaitoksen nykyinen opiskelija; palautettavat tyypit ovat sallitutKoulutusmuodot
  val nykyisenOpiskelijanKoulutusmuodot: List[String] = sallitutKoulutusmuodot ++ List("tuva", "ibtutkinto")

  private case class OmaOpiskeluoikeus(oppijaOid: String, koulutusmuoto: String, alkamispäivä: LocalDate)
  private case class NykyinenOpiskelija(henkilö: LaajatOppijaHenkilöTiedot, opintojenAlku: LocalDate, koulutusmuodot: Set[String])
}

object MassaluovutusQueryAiemmatOpiskeluoikeudetDocumentation {
  def example: MassaluovutusQueryAiemmatOpiskeluoikeudet = MassaluovutusQueryAiemmatOpiskeluoikeudet(
    organisaatioOid = MockOrganisaatiot.stadinAmmattiopisto,
    hetut = KoskiSpecificMockOppijat.aiemmatEsiJaPerusopetus.hetu.map(List(_)),
    oppijaOidit = Some(List(KoskiSpecificMockOppijat.aiemmatLukio.oid)),
    koulutusmuoto = Some(List("perusopetus", "lukiokoulutus")),
  )
}
