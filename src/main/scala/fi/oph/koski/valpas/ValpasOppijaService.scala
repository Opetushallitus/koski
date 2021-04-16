package fi.oph.koski.valpas

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.henkilo.Yhteystiedot
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.{AuditLog, KoskiMessageField, Logging}
import fi.oph.koski.util.DateOrdering.localDateTimeOrdering
import fi.oph.koski.util.Timing
import fi.oph.koski.validation.{ValidatingAndResolvingExtractor, ValidationAndResolvingContext}
import fi.oph.koski.valpas.ValpasOppijaService.ValpasOppijaRowConversionOps
import fi.oph.koski.valpas.hakukooste.{Hakukooste, ValpasHakukoosteService}
import fi.oph.koski.valpas.log.{ValpasAuditLogMessage, ValpasOperation}
import fi.oph.koski.valpas.repository._
import fi.oph.koski.valpas.valpasuser.ValpasSession

case class OppijaHakutilanteillaLaajatTiedot(
  oppija: ValpasOppijaLaajatTiedot,
  hakutilanteet: Seq[ValpasHakutilanneLaajatTiedot],
  hakutilanneError: Option[String],
  yhteystiedot: Seq[ValpasYhteystiedot]
)

object OppijaHakutilanteillaLaajatTiedot {
  def apply(oppija: ValpasOppijaLaajatTiedot, haut: Either[HttpStatus, Seq[Hakukooste]]): OppijaHakutilanteillaLaajatTiedot = {
    OppijaHakutilanteillaLaajatTiedot(
      oppija = oppija,
      hakutilanteet = haut.map(_.map(ValpasHakutilanneLaajatTiedot.apply)).getOrElse(Seq()),
      // TODO: Pitäisikö virheet mankeloida jotenkin eikä palauttaa sellaisenaan fronttiin?
      hakutilanneError = haut.left.toOption.flatMap(_.errorString),
      yhteystiedot = Seq.empty
    )
  }
}

case class OppijaHakutilanteillaSuppeatTiedot(
  oppija: ValpasOppijaSuppeatTiedot,
  hakutilanteet: Seq[ValpasHakutilanneSuppeatTiedot],
  hakutilanneError: Option[String]
)

object OppijaHakutilanteillaSuppeatTiedot {
  def apply(laajatTiedot: OppijaHakutilanteillaLaajatTiedot): OppijaHakutilanteillaSuppeatTiedot = {
    OppijaHakutilanteillaSuppeatTiedot(
      ValpasOppijaSuppeatTiedot(laajatTiedot.oppija),
      laajatTiedot.hakutilanteet.map(ValpasHakutilanneSuppeatTiedot.apply),
      laajatTiedot.hakutilanneError
    )
  }
}

object ValpasOppijaService {

  private[valpas] implicit class ValpasOppijaRowConversionOps(thiss: ValpasOppijaRow) {
    def asValpasOppijaLaajatTiedot()(implicit context: ValidationAndResolvingContext): Either[HttpStatus, ValpasOppijaLaajatTiedot] = {
      ValidatingAndResolvingExtractor
        .extract[List[ValpasOpiskeluoikeusLaajatTiedot]](thiss.opiskeluoikeudet, context)
        .map(opiskeluoikeudet =>
          ValpasOppijaLaajatTiedot(
            henkilö = ValpasHenkilöLaajatTiedot(
              oid = thiss.oppijaOid,
              hetu = thiss.hetu,
              syntymäaika = thiss.syntymäaika,
              etunimet = thiss.etunimet,
              sukunimi = thiss.sukunimi,
              turvakielto = thiss.turvakielto,
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
) extends Logging with Timing {
  private val dbService = new ValpasDatabaseService(application)
  private val oppijanumerorekisteri = application.opintopolkuHenkilöFacade
  private val localizationRepository = application.valpasLocalizationRepository
  private val koodistoviitepalvelu = application.koodistoViitePalvelu

  private val accessResolver = new ValpasAccessResolver(application.organisaatioRepository)

  private val rajapäivät: () => Rajapäivät = Rajapäivät(Environment.isLocalDevelopmentEnvironment)
  private implicit val validationAndResolvingContext: ValidationAndResolvingContext =
    ValidationAndResolvingContext(application.koodistoViitePalvelu, application.organisaatioRepository)

  // TODO: Tästä puuttuu oppijan tietoihin käsiksi pääsy seuraavilta käyttäjäryhmiltä:
  // (1) muut kuin peruskoulun hakeutumisen valvojat (esim. nivelvaihe ja aikuisten perusopetus)
  // (4) OPPILAITOS_SUORITTAMINEN-, OPPILAITOS_MAKSUTTOMUUS- ja KUNTA -käyttäjät.
  def getOppijatSuppeatTiedot(oppilaitosOids: Set[ValpasOppilaitos.Oid])(implicit session: ValpasSession): Either[HttpStatus, Seq[OppijaHakutilanteillaSuppeatTiedot]] =
    getOppijatLaajatTiedot(oppilaitosOids)
      .map(_.map(OppijaHakutilanteillaSuppeatTiedot.apply))
      .map(withAuditLogOppilaitostenKatsominen(oppilaitosOids))

  private def getOppijatLaajatTiedot(oppilaitosOids: Set[ValpasOppilaitos.Oid])(implicit session: ValpasSession): Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    val errorClue = oppilaitosOids.size match {
      case 1 =>  s"oppilaitos:${oppilaitosOids.head}"
      case n if n > 1 => s"oppilaitokset[${n}]:${oppilaitosOids.head}, ..."
      case _ => ""
    }

    accessResolver.organisaatiohierarkiaOids(oppilaitosOids)
      .map(dbService.getPeruskoulunValvojalleNäkyvätOppijat(rajapäivät()))
      .flatMap(results => HttpStatus.foldEithers(results.map(_.asValpasOppijaLaajatTiedot)))
      .map(fetchHaut(errorClue))
  }

  // TODO: Tästä puuttuu oppijan tietoihin käsiksi pääsy seuraavilta käyttäjäryhmiltä:
  // (1) muut kuin peruskoulun hakeutumisen valvojat (esim. nivelvaihe ja aikuisten perusopetus)
  // (4) OPPILAITOS_SUORITTAMINEN-, OPPILAITOS_MAKSUTTOMUUS- ja KUNTA -käyttäjät.
  def getOppijaLaajatTiedot(oppijaOid: ValpasHenkilö.Oid)(implicit session: ValpasSession): Either[HttpStatus, OppijaHakutilanteillaLaajatTiedot] =
    dbService.getPeruskoulunValvojalleNäkyväOppija(rajapäivät())(oppijaOid)
      .toRight(ValpasErrorCategory.forbidden.oppija())
      .flatMap(_.asValpasOppijaLaajatTiedot)
      .flatMap(accessResolver.withOppijaAccess)
      .map(fetchHaku)
      .flatMap(o => fetchVirallisetYhteystiedot(o.oppija).map(yhteystiedot => o.copy(
        yhteystiedot = o.yhteystiedot ++ yhteystiedot.map(yt => ValpasYhteystiedot.virallinenYhteystieto(yt, localizationRepository.get("oppija__viralliset_yhteystiedot")))
      )))
      .map(withAuditLogOppijaKatsominen)

  private def fetchHaku(oppija: ValpasOppijaLaajatTiedot): OppijaHakutilanteillaLaajatTiedot = {
    val hakukoosteet = hakukoosteService.getYhteishakujenHakukoosteet(oppijaOids = Set(oppija.henkilö.oid), ainoastaanAktiivisetHaut =  false, errorClue = s"oppija:${oppija.henkilö.oid}")
    val yhteystiedot = hakukoosteet.map(ilmoitetutYhteystiedot).getOrElse(Seq.empty)
    OppijaHakutilanteillaLaajatTiedot.apply(oppija, hakukoosteet).copy(yhteystiedot = yhteystiedot)
  }

  private def fetchHaut(errorClue: String)(oppijat: Seq[ValpasOppijaLaajatTiedot]): Seq[OppijaHakutilanteillaLaajatTiedot] = {
    hakukoosteService.getYhteishakujenHakukoosteet(oppijaOids = oppijat.map(_.henkilö.oid).toSet, ainoastaanAktiivisetHaut =  true, errorClue = errorClue)
      .map(_.groupBy(_.oppijaOid))
      .fold(
        error => oppijat.map(oppija => OppijaHakutilanteillaLaajatTiedot.apply(oppija, Left(error))),
        groups => oppijat.map(oppija =>
          OppijaHakutilanteillaLaajatTiedot.apply(oppija, Right(groups.getOrElse(oppija.henkilö.oid, Seq()))))
      )
  }

  private def fetchVirallisetYhteystiedot(oppija: ValpasOppijaLaajatTiedot): Either[HttpStatus, Seq[Yhteystiedot]] = {
    if (oppija.henkilö.turvakielto) {
      Right(Seq.empty)
    } else {
      timed("fetchVirallisetYhteystiedot", 10) {
        oppijanumerorekisteri.findOppijaJaYhteystiedotByOid(oppija.henkilö.oid)
          .map(_.yhteystiedot.flatMap(yt => {
            val alkuperä = koodistoviitepalvelu.validate(yt.alkuperä)
              .filter(_.koodiarvo == "alkupera1") // Filtteröi pois muut kuin VTJ:ltä peräisin olevat yhteystiedot
            val tyyppi = koodistoviitepalvelu.validate(yt.tyyppi)
            if (alkuperä.isDefined && tyyppi.isDefined) {
              Some(yt.copy(alkuperä = alkuperä.get, tyyppi = tyyppi.get))
            } else {
              None
            }
          }))
          .toRight(ValpasErrorCategory.internalError())
      }
    }
  }

  private def ilmoitetutYhteystiedot(hakukoosteet: Seq[Hakukooste]): Seq[ValpasYhteystiedot] =
    hakukoosteet
      .sortBy(_.hakemuksenMuokkauksenAikaleima)
      .lastOption
      .map(haku => List(
        ValpasYhteystiedot.oppijanIlmoittamatYhteystiedot(haku, localizationRepository.get("oppija__yhteystiedot")),
      ))
      .getOrElse(List.empty)

  private def withAuditLogOppijaKatsominen(result: OppijaHakutilanteillaLaajatTiedot)(implicit session: ValpasSession): OppijaHakutilanteillaLaajatTiedot = {
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
