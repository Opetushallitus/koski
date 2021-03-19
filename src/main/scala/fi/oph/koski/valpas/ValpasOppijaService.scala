package fi.oph.koski.valpas

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.henkilo.Yhteystiedot
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.{AuditLog, KoskiMessageField, Logging}
import fi.oph.koski.util.DateOrdering.localDateTimeOrdering
import fi.oph.koski.validation.{ValidatingAndResolvingExtractor, ValidationAndResolvingContext}
import fi.oph.koski.valpas.ValpasOppijaService.ValpasOppijaRowConversionOps
import fi.oph.koski.valpas.hakukooste.{Hakukooste, ValpasHakukoosteService}
import fi.oph.koski.valpas.log.{ValpasAuditLogMessage, ValpasOperation}
import fi.oph.koski.valpas.repository._
import fi.oph.koski.valpas.valpasuser.ValpasSession

case class OppijaHakutilanteilla(
  oppija: ValpasOppija,
  hakutilanteet: Seq[ValpasHakutilanne],
  hakutilanneError: Option[String],
  yhteystiedot: Seq[ValpasYhteystiedot]
)

object OppijaHakutilanteilla {
  def apply(oppija: ValpasOppija, haut: Either[HttpStatus, Seq[Hakukooste]]): OppijaHakutilanteilla = {
    OppijaHakutilanteilla(
      oppija = oppija,
      hakutilanteet = haut.map(_.map(ValpasHakutilanne.apply)).getOrElse(Seq()),
      // TODO: Pitäisikö virheet mankeloida jotenkin eikä palauttaa sellaisenaan fronttiin?
      hakutilanneError = haut.left.toOption.flatMap(_.errorString),
      yhteystiedot = Seq.empty
    )
  }
}

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
            valvottavatOpiskeluoikeudet = thiss.valvottavatOpiskeluoikeudet,
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
  private val oppijanumerorekisteri = application.opintopolkuHenkilöFacade
  private val localizationRepository = application.valpasLocalizationRepository

  private val accessResolver = new ValpasAccessResolver(application.organisaatioRepository)

  private val rajapäivät: () => Rajapäivät = Rajapäivät(Environment.isLocalDevelopmentEnvironment)

  private implicit val validationAndResolvingContext: ValidationAndResolvingContext =
    ValidationAndResolvingContext(application.koodistoViitePalvelu, application.organisaatioRepository)

  // TODO: Tästä puuttuu oppijan tietoihin käsiksi pääsy seuraavilta käyttäjäryhmiltä:
  // (1) muut kuin peruskoulun hakeutumisen valvojat (esim. nivelvaihe ja aikuisten perusopetus)
  // (4) OPPILAITOS_SUORITTAMINEN-, OPPILAITOS_MAKSUTTOMUUS- ja KUNTA -käyttäjät.
  def getOppijat(oppilaitosOids: Set[ValpasOppilaitos.Oid])(implicit session: ValpasSession): Either[HttpStatus, Seq[OppijaHakutilanteilla]] =
    accessResolver.organisaatiohierarkiaOids(oppilaitosOids)
      .map(dbService.getPeruskoulunValvojalleNäkyvätOppijat(rajapäivät()))
      .flatMap(results => HttpStatus.foldEithers(results.map(_.asValpasOppija)))
      .map(fetchHaut)
      .map(withAuditLogOppilaitostenKatsominen(oppilaitosOids))

  // TODO: Tästä puuttuu oppijan tietoihin käsiksi pääsy seuraavilta käyttäjäryhmiltä:
  // (1) muut kuin peruskoulun hakeutumisen valvojat (esim. nivelvaihe ja aikuisten perusopetus)
  // (4) OPPILAITOS_SUORITTAMINEN-, OPPILAITOS_MAKSUTTOMUUS- ja KUNTA -käyttäjät.
  def getOppija(oppijaOid: ValpasHenkilö.Oid)(implicit session: ValpasSession): Either[HttpStatus, OppijaHakutilanteilla] =
    dbService.getPeruskoulunValvojalleNäkyväOppija(rajapäivät())(oppijaOid)
      .toRight(ValpasErrorCategory.forbidden.oppija())
      .flatMap(_.asValpasOppija)
      .flatMap(accessResolver.withOppijaAccess)
      .map(fetchHaku)
      .flatMap(o => fetchVirallisetYhteystiedot(o.oppija).map(yhteystiedot => o.copy(
        yhteystiedot = o.yhteystiedot ++ yhteystiedot.map(yt => ValpasYhteystiedot.virallinenYhteystieto(yt, localizationRepository.get("oppija__viralliset_yhteystiedot")))
      )))
      .map(withAuditLogOppijaKatsominen)

  private def fetchHaku(oppija: ValpasOppija): OppijaHakutilanteilla = {
    OppijaHakutilanteilla.apply(oppija, hakukoosteService.getHakukoosteet(Set(oppija.henkilö.oid)))
  }

  private def fetchHaut(oppijat: Seq[ValpasOppija]): Seq[OppijaHakutilanteilla] = {
    hakukoosteService.getHakukoosteet(oppijat.map(_.henkilö.oid).toSet)
      .map(_.groupBy(_.oppijaOid))
      .fold(
        error => oppijat.map(oppija => OppijaHakutilanteilla.apply(oppija, Left(error))),
        groups => oppijat.map(oppija =>
          OppijaHakutilanteilla.apply(oppija, Right(groups.getOrElse(oppija.henkilö.oid, Seq()))))
      )
  }

  private def fetchVirallisetYhteystiedot(oppija: ValpasOppija): Either[HttpStatus, Seq[Yhteystiedot]] = {
    oppijanumerorekisteri.findOppijaByOid(oppija.henkilö.oid)
      .map(_.yhteystiedot)
      .toRight(ValpasErrorCategory.internalError())
  }

  private def ilmoitetutYhteystiedot(hakukoosteet: Seq[Hakukooste]): Seq[ValpasYhteystiedot] =
    hakukoosteet
      .sortBy(_.haunAlkamispaivamaara)
      .lastOption
      .map(haku => List(
        Some(ValpasYhteystiedot.oppijanIlmoittamatYhteystiedot(haku, localizationRepository.get("oppija__yhteystiedot"))),
        ValpasYhteystiedot.oppijanIlmoittamatHuoltajanYhteystiedot(haku, localizationRepository.get("oppija__huoltaja")),
      ))
      .getOrElse(List.empty)
      .collect { case Some(s) => s }

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
