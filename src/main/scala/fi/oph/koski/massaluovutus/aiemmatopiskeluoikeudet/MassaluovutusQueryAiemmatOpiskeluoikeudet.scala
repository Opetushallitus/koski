package fi.oph.koski.massaluovutus.aiemmatopiskeluoikeudet

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.KoskiOpiskeluoikeusRowImplicits._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db._
import fi.oph.koski.henkilo.{Hetu, KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.{SensitiveAndRedundantDataFilter, SensitiveDataAllowed}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession, Rooli, Session}
import fi.oph.koski.log.AuditLogMessage.AuditLogMessageField
import fi.oph.koski.log.{KoskiAuditLogMessageField, KoskiOperation, Logging, OpiskeluoikeusOiditAuditLog}
import fi.oph.koski.massaluovutus.MassaluovutusUtils.{QueryResourceManager, auditlokinKentät}
import fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusOppija
import fi.oph.koski.massaluovutus.{KoulutuksenjärjestäjienMassaluovutusQueryParameters, MassaluovutusException, MassaluovutusUtils, QueryFormat, QueryResultWriter}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryContext
import fi.oph.koski.organisaatio.{MockOrganisaatiot, OrganisaationAlaisetOrganisaatiot}
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.{EnumValues, HiddenFromAiemmatOpiskeluoikeudet}
import fi.oph.koski.util.Retry.retryWithInterval
import fi.oph.scalaschema.Serializer
import fi.oph.scalaschema.annotation.{Description, Title}
import org.json4s.JValue

import java.time.LocalDate
import scala.concurrent.duration.DurationInt

@Title("Nykyisten opiskelijoiden opiskeluoikeudet")
@Description("Palauttaa annettujen, oppilaitoksen nykyisten opiskelijoiden kaikki esi-, perus-, ammatillisen, lukio-, TUVA-, IB-, EB-, DIA-, International School- ja European School of Helsinki -koulutuksen opiskeluoikeudet: aiemmat, nykyiset ja tulevat, sekä muiden organisaatioiden että oppilaitoksen omat.")
@Description("Oppijat annetaan henkilötunnuksina tai oppijanumeroina. Jokaisen annetun oppijan on oltava oppilaitoksen nykyinen opiskelija ja alle 18-vuotias tai kyselyn tekovuonna 18 vuotta täyttävä, muuten koko kysely hylätään ja vastaus kertoo, mitkä oppijat eivät täytä ehtoja. Nykyinen opiskelija on oppija, jolla on kyselyn oppilaitoksessa päättymätön, myös vasta tulevaisuudessa alkava, jonkin näistä koulutusmuodoista opiskeluoikeus. Kysely tehdään aina yhdelle oppilaitokselle.")
@Description("Esiopetustasoiset tiedot eli esiopetuksen opiskeluoikeudet sekä International Schoolin explorer-vuosiluokka ja Helsingin eurooppalaisen koulun nursery-vuosiluokat palautetaan vain, jos oppija on kyselyn oppilaitoksessa esi- tai perusopetustasoinen opiskelija. International Schoolissa ja eurooppalaisessa koulussa taso päätellään nykyisen opiskeluoikeuden uusimmasta vuosiluokasta: PYP ja MYP 6–9 sekä nursery, primary ja S1–S4 ovat perusopetustasoa, MYP 10, Diploma ja S5–S7 toista astetta. Oppilaitoksen omaa nykyistä opiskeluoikeutta ei karsita.")
@Description("Kysely on käytettävissä palvelukäyttäjälle, jolla on kyselyn oppilaitoksessa lukuoikeus ja AIEMMAT_OPISKELUOIKEUDET-käyttöoikeus, sekä Opetushallituksen pääkäyttäjälle. Palautettavat koulutusmuodot eivät riipu käyttäjän omista koulutusmuotokohtaisista käyttöoikeuksista, vaan ainoastaan tästä roolista. Arkaluontoiset tiedot palautetaan aina. Skeemassa HiddenFromAiemmatOpiskeluoikeudet-merkinnällä varustetut kentät (esim. vankilaopetuksessa ja käyttäytymisenArvio) jätetään pois.")
@Description("Tulostiedostot vastaavat organisaationOpiskeluoikeudet-kyselyn json-muotoa: yksi tiedosto oppijaa kohden.")
case class MassaluovutusQueryAiemmatOpiskeluoikeudet(
  @EnumValues(Set("aiemmatOpiskeluoikeudet"))
  `type`: String = "aiemmatOpiskeluoikeudet",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  @Description("Oppilaitoksen oid, jonka nykyisten opiskelijoiden opiskeluoikeudet haetaan. Koulutustoimijan tai kunnan oidia ei hyväksytä.")
  organisaatioOid: Organisaatio.Oid,
  @Description("Haettavat oppijat henkilötunnuksina. Kentässä hetut tai oppijaOidit on annettava vähintään yksi oppija.")
  hetut: Option[List[String]] = None,
  @Description("Haettavat oppijat oppijanumeroina.")
  oppijaOidit: Option[List[String]] = None,
  @Description("Palautettavat koulutusmuodot. Oletuksena kaikki sallitut: esiopetus, perusopetus, ammatillinenkoulutus, lukiokoulutus, tuva, ibtutkinto, ebtutkinto, diatutkinto, internationalschool, europeanschoolofhelsinki.")
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
          val kaikki = haeOpiskeluoikeudet(db, henkilö).map(MassaluovutusUtils.toKoskeenTallennettavaOpiskeluoikeus(application, logger))
          val oma = (oo: KoskeenTallennettavaOpiskeluoikeus) => oo.oid.exists(opiskelija.omatOpiskeluoikeusOidit)
          val esiopetustasoisetSallittu = kaikki.exists(oo => oma(oo) && oikeuttaaEsiopetustasoisiinTietoihin(oo))
          val opiskeluoikeudet = if (esiopetustasoisetSallittu) kaikki else kaikki.flatMap(oo => if (oma(oo)) Some(oo) else ilmanEsiopetustasoisiaTietoja(oo))
          if (opiskeluoikeudet.isEmpty) {
            writer.skipFile()
          } else {
            writer.putJson(henkilö.oid, serialize(MassaluovutusOppija(
              henkilö = application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(henkilö),
              linkitetytOidit = henkilö.linkitetytOidit,
              opiskeluoikeudet = opiskeluoikeudet,
            )))
            auditLog(henkilö.oid, opiskeluoikeudet.flatMap(_.oid), writer)
          }
        }
      }
    }

  override def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean = withKoskiSpecificSession { u =>
    def onOppilaitos = application.organisaatioRepository.getOrganisaatio(organisaatioOid).exists(_.toOppilaitos.isDefined)
    // Suljettu tuotannossa, kunnes OPH on vahvistanut palautettavat kentät ja ensimmäinen käyttöoikeusryhmä on perustettu
    !Environment.isProdEnvironment(application.config) && onOppilaitos && (
      u.isRoot || (
        u.isPalvelukäyttäjä
          && u.organisationOids(AccessType.read).contains(organisaatioOid)
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

  // Arkaluontoiset tiedot palautetaan käyttäjän rooleista riippumatta, vain HiddenFromAiemmatOpiskeluoikeudet-kentät jätetään pois
  private def serialize(oppija: MassaluovutusOppija): JValue = {
    val filter = SensitiveAndRedundantDataFilter(SensitiveDataAllowed.SystemUser)
    val context = filter.serializationContext.copy(propertyProcessor = (schema, property) =>
      if (property.metadata.exists(_.isInstanceOf[HiddenFromAiemmatOpiskeluoikeudet])) Nil else filter.filterSensitiveData(schema, property)
    )
    Serializer.serialize(oppija, context)
  }

  private def annetutHetut: List[String] = hetut.toList.flatten
  private def annetutOidit: List[String] = oppijaOidit.toList.flatten

  // Sama tarkistus ajetaan kyselyä luotaessa (hylkäys 400-vastauksena) ja uudelleen kyselyä suoritettaessa
  private def haeNykyisetOpiskelijat(application: KoskiApplication, tänään: LocalDate, onrYritykset: Int)(implicit user: KoskiSpecificSession): Either[HttpStatus, List[NykyinenOpiskelija]] =
    HttpStatus.foldEithers(annetutHetut.map(application.hetu.validate)).flatMap { _ =>
      val onr = application.opintopolkuHenkilöFacade
      def onrHaku[T](haku: => T): T = retryWithInterval(onrYritykset, 5.seconds.toMillis)(haku)
      // Kaksi eräkutsua oppijanumerorekisteriin hetukohtaisten kutsujen sijaan
      val oidHetulla: Map[String, String] = annetutHetut.grouped(onrEräkoko)
        .flatMap(erä => onrHaku(onr.findOppijatByHetusNoSlaveOids(erä)))
        .flatMap(h => h.hetu.map(_ -> h.oid))
        .toMap
      val masteritOidilla: Map[String, LaajatOppijaHenkilöTiedot] = (oidHetulla.values.toList ++ annetutOidit).distinct.grouped(onrEräkoko)
        .flatMap(erä => onrHaku(onr.findMasterOppijatWithSlaveOids(erä)))
        .toMap
      val annetut: List[(String, Option[LaajatOppijaHenkilöTiedot])] =
        annetutHetut.map(hetu => hetu -> oidHetulla.get(hetu).flatMap(masteritOidilla.get)) ++
          annetutOidit.map(oid => oid -> masteritOidilla.get(oid))
      val henkilöt = annetut.flatMap(_._2).distinctBy(_.oid)

      val organisaatiot = application.organisaatioService.organisaationAlaisetOrganisaatiot(organisaatioOid)
      val omatOpiskeluoikeudet = haeOmatOpiskeluoikeudet(application.replicaDatabase.db, organisaatiot, henkilöt.flatMap(_.kaikkiOidit), tänään)
      val opiskelijat = henkilöt.flatMap { henkilö =>
        val omat = omatOpiskeluoikeudet.filter(oo => henkilö.kaikkiOidit.contains(oo.oppijaOid))
        Option.when(omat.nonEmpty)(NykyinenOpiskelija(henkilö, omat.map(_.oid).toSet))
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
    val koulutusmuodot = sallitutKoulutusmuodot.intersect(user.allowedOpiskeluoikeudetJaPäätasonSuoritukset.toOpiskeluoikeudenTyypit.toList)
    QueryMethods.runDbSync(db, SQLHelpers.concatMany(
      Some(sql"""
        SELECT oid, oppija_oid
        FROM opiskeluoikeus
        WHERE NOT poistettu AND NOT mitatoity
          AND oppija_oid = ANY($oppijaOidit)
          AND koulutusmuoto = ANY($koulutusmuodot)
          AND (paattymispaiva IS NULL OR paattymispaiva > $tänään)
          AND
      """),
      Some(MassaluovutusUtils.organisaatioFilter(organisaatiot)),
    ).as[(String, String)])
      .map { case (oid, oppijaOid) => OmaOpiskeluoikeus(oid, oppijaOid) }
  }

  // Kaikki oppijan opiskeluoikeudet palautettavissa koulutusmuodoissa päivämääristä riippumatta: aiemmat, nykyiset ja tulevat.
  // Tarkoituksella ei rajata käyttäjän omilla koulutusmuoto-oikeuksilla (toisin kuin nykyisten opiskelijoiden haussa):
  // esim. lukio tarvitsee opiskelijoidensa perusopetuksen tiedot, vaikka sillä ei ole perusopetuksen käyttöoikeutta.
  private def haeOpiskeluoikeudet(db: DB, henkilö: LaajatOppijaHenkilöTiedot): Seq[KoskiOpiskeluoikeusRow] = {
    val koulutusmuodot: List[String] = koulutusmuoto.toList.flatten
    QueryMethods.runDbSync(db, sql"""
        SELECT *
        FROM opiskeluoikeus
        WHERE NOT poistettu AND NOT mitatoity
          AND oppija_oid = ANY(${henkilö.kaikkiOidit})
          AND koulutusmuoto = ANY($koulutusmuodot)
        ORDER BY alkamispaiva
      """.as[KoskiOpiskeluoikeusRow])
  }

  private def auditLog(oppijaOid: String, opiskeluoikeusOidit: Seq[String], writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Unit =
    OpiskeluoikeusOiditAuditLog.log(
      KoskiOperation.AIEMMAT_OPISKELUOIKEUDET_KATSOMINEN,
      oppijaOid,
      opiskeluoikeusOidit,
      auditlokinKentät(writer.queryId.toString, Some(writer.jsonFileName(oppijaOid))) ++ Map[AuditLogMessageField, String](
        KoskiAuditLogMessageField.juuriOrganisaatio -> organisaatioOid,
        KoskiAuditLogMessageField.hakuEhto -> OpiskeluoikeusQueryContext.queryForAuditLog(Map(
          "organisaatio" -> List(organisaatioOid),
          "koulutusmuoto" -> koulutusmuoto.toList.flatten,
        )),
      ),
    )
}

object MassaluovutusQueryAiemmatOpiskeluoikeudet {
  // Sekä palautettavat koulutusmuodot että ne, joiden opiskelijana oppija on kyselyn oppilaitoksen nykyinen opiskelija
  val sallitutKoulutusmuodot: List[String] = List("esiopetus", "perusopetus", "ammatillinenkoulutus", "lukiokoulutus", "tuva", "ibtutkinto", "ebtutkinto", "diatutkinto", "internationalschool", "europeanschoolofhelsinki")
  val onrEräkoko = 1000
  // Esiopetustasoiset tiedot palautetaan vain, jos oppija on kyselyn oppilaitoksessa esi- tai perusopetustasoinen opiskelija.
  // International Schoolissa ja ESH:ssa taso päätellään uusimmasta vuosiluokasta samoin rajoin kuin Valppaassa: MYP 10 ja S5 ovat jo toista astetta.
  def oikeuttaaEsiopetustasoisiinTietoihin(oo: KoskeenTallennettavaOpiskeluoikeus): Boolean = oo match {
    case _: EsiopetuksenOpiskeluoikeus | _: PerusopetuksenOpiskeluoikeus => true
    case oo: InternationalSchoolOpiskeluoikeus => uusinSuoritus(oo.suoritukset).exists {
      case _: PYPVuosiluokanSuoritus => true
      case s: MYPVuosiluokanSuoritus => s.koulutusmoduuli.tunniste.koodiarvo != "10"
      case _ => false
    }
    case oo: EuropeanSchoolOfHelsinkiOpiskeluoikeus => uusinSuoritus(oo.suoritukset).exists {
      case _: NurseryVuosiluokanSuoritus | _: PrimaryVuosiluokanSuoritus => true
      case s: SecondaryLowerVuosiluokanSuoritus => s.koulutusmoduuli.tunniste.koodiarvo != "S5"
      case _ => false
    }
    case _ => false
  }

  private def uusinSuoritus[S <: PäätasonSuoritus](suoritukset: List[S]): Option[S] =
    suoritukset.sortBy(s => (s.alkamispäivä.map(_.toEpochDay).getOrElse(Long.MinValue), s.vahvistus.isEmpty)).lastOption

  // Esiopetustasoiset tiedot: esiopetuksen opiskeluoikeus kokonaan, International Schoolin explorer- ja ESH:n nursery-vuosiluokat
  def ilmanEsiopetustasoisiaTietoja(oo: KoskeenTallennettavaOpiskeluoikeus): Option[KoskeenTallennettavaOpiskeluoikeus] = oo match {
    case _: EsiopetuksenOpiskeluoikeus => None
    case oo: InternationalSchoolOpiskeluoikeus =>
      val suoritukset = oo.suoritukset.filterNot(_.koulutusmoduuli.tunniste.koodiarvo == "explorer")
      Option.when(suoritukset.nonEmpty)(oo.copy(suoritukset = suoritukset))
    case oo: EuropeanSchoolOfHelsinkiOpiskeluoikeus =>
      val suoritukset = oo.suoritukset.filterNot(_.isInstanceOf[NurseryVuosiluokanSuoritus])
      Option.when(suoritukset.nonEmpty)(oo.copy(suoritukset = suoritukset))
    case oo => Some(oo)
  }

  private case class OmaOpiskeluoikeus(oid: String, oppijaOid: String)
  private case class NykyinenOpiskelija(henkilö: LaajatOppijaHenkilöTiedot, omatOpiskeluoikeusOidit: Set[String])
}

object MassaluovutusQueryAiemmatOpiskeluoikeudetDocumentation {
  def example: MassaluovutusQueryAiemmatOpiskeluoikeudet = MassaluovutusQueryAiemmatOpiskeluoikeudet(
    organisaatioOid = MockOrganisaatiot.stadinAmmattiopisto,
    hetut = KoskiSpecificMockOppijat.aiemmatEsiJaPerusopetus.hetu.map(List(_)),
    oppijaOidit = Some(List(KoskiSpecificMockOppijat.aiemmatLukio.oid)),
    koulutusmuoto = Some(List("perusopetus", "lukiokoulutus")),
  )
}
