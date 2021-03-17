package fi.oph.koski.valpas

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.{AuditLog, KoskiMessageField, Logging}
import fi.oph.koski.validation.{ValidatingAndResolvingExtractor, ValidationAndResolvingContext}
import fi.oph.koski.valpas.hakukooste.ValpasHakukoosteService
import fi.oph.koski.valpas.log.{ValpasAuditLogMessage, ValpasOperation}
import fi.oph.koski.valpas.repository._
import fi.oph.koski.valpas.ValpasOppijaService.ValpasOppijaRowConversionOps
import fi.oph.koski.valpas.valpasuser.ValpasSession

case class OppijaHakutilanteilla(oppija: ValpasOppija, haut: Seq[ValpasHakutilanne])

object ValpasOppijaService {
  private[valpas] implicit class ValpasOppijaRowConversionOps(thiss: ValpasOppijaRow) {
    def asValpasOppija()(implicit context: ValidationAndResolvingContext): Either[HttpStatus, ValpasOppija] = {
      ValidatingAndResolvingExtractor
        .extract[List[ValpasOpiskeluoikeus]](thiss.opiskeluoikeudet, context)
        .map(opiskeluoikeudet =>
          ValpasOppija(
            henkilö = ValpasHenkilö(
              oid = thiss.oppijaOid,
              hetu = thiss.hetu,
              syntymäaika = thiss.syntymäaika,
              etunimet = thiss.etunimet,
              sukunimi = thiss.sukunimi
            ),
            oikeutetutOppilaitokset = thiss.oikeutetutOppilaitokset,
            opiskeluoikeudet = opiskeluoikeudet
          )
        )
    }
  }
}

class ValpasOppijaService(
  application: KoskiApplication,
  hakukoosteService: ValpasHakukoosteService,
) extends Logging {
  private val dbService = new ValpasDatabaseService(application)

  private val accessResolver = new ValpasAccessResolver(application.organisaatioRepository)

  private val rajapäivät: () => Rajapäivät = Rajapäivät(Environment.isLocalDevelopmentEnvironment)

  private implicit val validationAndResolvingContext: ValidationAndResolvingContext =
    ValidationAndResolvingContext(application.koodistoViitePalvelu, application.organisaatioRepository)

  // TODO: Tästä puuttuu oppijan tietoihin käsiksi pääsy seuraavilta käyttäjäryhmiltä:
  // (1) muut kuin peruskoulun hakeutumisen valvojat (esim. nivelvaihe ja aikuisten perusopetus)
  // (4) OPPILAITOS_SUORITTAMINEN-, OPPILAITOS_MAKSUTTOMUUS- ja KUNTA -käyttäjät.
  def getOppijat(oppilaitosOids: Set[ValpasOppilaitos.Oid])(implicit session: ValpasSession): Either[HttpStatus, Seq[OppijaHakutilanteilla]] =
    accessResolver.organisaatiohierarkiaOids(oppilaitosOids)
      .map(oids => dbService.getPeruskoulunValvojalleNäkyvätOppijat(Some(oids.toSeq), rajapäivät()))
      .flatMap(results => HttpStatus.foldEithers(results.map(_.asValpasOppija)))
      .flatMap(fetchHaut)
      .map(withAuditLogOppilaitostenKatsominen(oppilaitosOids))

  // TODO: Tästä puuttuu oppijan tietoihin käsiksi pääsy seuraavilta käyttäjäryhmiltä:
  // (1) muut kuin peruskoulun hakeutumisen valvojat (esim. nivelvaihe ja aikuisten perusopetus)
  // (4) OPPILAITOS_SUORITTAMINEN-, OPPILAITOS_MAKSUTTOMUUS- ja KUNTA -käyttäjät.
  def getOppija(oppijaOid: ValpasHenkilö.Oid)(implicit session: ValpasSession): Either[HttpStatus, OppijaHakutilanteilla] =
    dbService.getPeruskoulunValvojalleNäkyväOppija(oppijaOid, rajapäivät())
      .toRight(ValpasErrorCategory.forbidden.oppija())
      .flatMap(_.asValpasOppija)
      .flatMap(accessResolver.withOppijaAccess)
      .flatMap(fetchHaku)
      .map(withAuditLogOppijaKatsominen)

  private def fetchHaku(oppija: ValpasOppija): Either[HttpStatus, OppijaHakutilanteilla] = {
    hakukoosteService.getHakukoosteet(Set(oppija.henkilö.oid))
      .map(hakukoosteet => OppijaHakutilanteilla(oppija, hakukoosteet.map(ValpasHakutilanne.apply)))
  }

  private def fetchHaut(oppijat: Seq[ValpasOppija]): Either[HttpStatus, Seq[OppijaHakutilanteilla]] = {
    hakukoosteService.getHakukoosteet(oppijat.map(_.henkilö.oid).toSet)
      .map(_.groupBy(_.oppijaOid))
      .map(groups => oppijat.map(oppija =>
        OppijaHakutilanteilla(oppija, groups.getOrElse(oppija.henkilö.oid, Seq()).map(ValpasHakutilanne.apply))
      ))
  }

  private def withAuditLogOppijaKatsominen(result: OppijaHakutilanteilla)(implicit session: ValpasSession): OppijaHakutilanteilla = {
    AuditLog.log(ValpasAuditLogMessage(
      ValpasOperation.VALPAS_OPPIJA_KATSOMINEN,
      Map(KoskiMessageField.oppijaHenkiloOid -> result.oppija.henkilö.oid)
    ))
    result
  }

  private def withAuditLogOppilaitostenKatsominen[T](oppilaitosOids: Set[ValpasOppilaitos.Oid])(result: T)(implicit session: ValpasSession): T = {
    oppilaitosOids.foreach { oppilaitosOid =>
      AuditLog.log(ValpasAuditLogMessage(
        ValpasOperation.VALPAS_OPPILAITOKSET_OPPIJAT_KATSOMINEN,
        Map(KoskiMessageField.juuriOrganisaatio -> oppilaitosOid)
      ))
    }
    result
  }
}
