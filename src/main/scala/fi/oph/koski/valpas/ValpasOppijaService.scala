package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.Yhteystiedot
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.util.DateOrdering.localDateTimeOrdering
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.hakukooste.{Hakukooste, ValpasHakukoosteService}
import fi.oph.koski.valpas.opiskeluoikeusrepository._
import fi.oph.koski.valpas.valpasrepository.{ValpasKuntailmoitusLaajatTiedot, ValpasKuntailmoitusSuppeatTiedot}
import fi.oph.koski.valpas.valpasuser.ValpasSession
import fi.oph.koski.valpas.yhteystiedot.ValpasYhteystiedot

case class OppijaHakutilanteillaLaajatTiedot(
  oppija: ValpasOppijaLaajatTiedot,
  hakutilanteet: Seq[ValpasHakutilanneLaajatTiedot],
  hakutilanneError: Option[String],
  yhteystiedot: Seq[ValpasYhteystiedot],
  kuntailmoitukset: Seq[ValpasKuntailmoitusLaajatTiedot],
) {
  def validate(koodistoviitepalvelu: KoodistoViitePalvelu): OppijaHakutilanteillaLaajatTiedot =
    this.copy(hakutilanteet = hakutilanteet.map(_.validate(koodistoviitepalvelu)))
}

object OppijaHakutilanteillaLaajatTiedot {
  def apply(oppija: ValpasOppijaLaajatTiedot, haut: Either[HttpStatus, Seq[Hakukooste]]): OppijaHakutilanteillaLaajatTiedot = {
    OppijaHakutilanteillaLaajatTiedot(
      oppija = oppija,
      hakutilanteet = haut.map(_.map(ValpasHakutilanneLaajatTiedot.apply)).getOrElse(Seq()),
      // TODO: Pitäisikö virheet mankeloida jotenkin eikä palauttaa sellaisenaan fronttiin?
      hakutilanneError = haut.left.toOption.flatMap(_.errorString),
      yhteystiedot = Seq.empty,
      kuntailmoitukset = Seq.empty
    )
  }
}

case class OppijaHakutilanteillaSuppeatTiedot(
  oppija: ValpasOppijaSuppeatTiedot,
  hakutilanteet: Seq[ValpasHakutilanneSuppeatTiedot],
  hakutilanneError: Option[String],
  kuntailmoitukset: Seq[ValpasKuntailmoitusSuppeatTiedot]
)

object OppijaHakutilanteillaSuppeatTiedot {
  def apply(laajatTiedot: OppijaHakutilanteillaLaajatTiedot): OppijaHakutilanteillaSuppeatTiedot = {
    OppijaHakutilanteillaSuppeatTiedot(
      ValpasOppijaSuppeatTiedot(laajatTiedot.oppija),
      laajatTiedot.hakutilanteet.map(ValpasHakutilanneSuppeatTiedot.apply),
      laajatTiedot.hakutilanneError,
      laajatTiedot.kuntailmoitukset.map(ValpasKuntailmoitusSuppeatTiedot.apply)
    )
  }
}

class ValpasOppijaService(
  application: KoskiApplication
) extends Logging with Timing {
  private val hakukoosteService = ValpasHakukoosteService(application.config)
  private val opiskeluoikeusDbService = new ValpasOpiskeluoikeusDatabaseService(application)
  private val oppijanumerorekisteri = application.opintopolkuHenkilöFacade
  private val localizationRepository = application.valpasLocalizationRepository
  private val koodistoviitepalvelu = application.koodistoViitePalvelu

  private val accessResolver = new ValpasAccessResolver(application.organisaatioRepository)

  private val validatingAndResolvingExtractor = application.validatingAndResolvingExtractor

  // TODO: Tästä puuttuu oppijan tietoihin käsiksi pääsy seuraavilta käyttäjäryhmiltä:
  // (1) muut kuin peruskoulun hakeutumisen valvojat (esim. nivelvaihe ja aikuisten perusopetus)
  // (4) OPPILAITOS_SUORITTAMINEN-, OPPILAITOS_MAKSUTTOMUUS- ja KUNTA -käyttäjät.
  def getOppijatSuppeatTiedot(oppilaitosOids: Set[ValpasOppilaitos.Oid])(implicit session: ValpasSession): Either[HttpStatus, Seq[OppijaHakutilanteillaSuppeatTiedot]] =
    getOppijatLaajatTiedot(oppilaitosOids)
      .map(_.map(OppijaHakutilanteillaSuppeatTiedot.apply))

  private def getOppijatLaajatTiedot(oppilaitosOids: Set[ValpasOppilaitos.Oid])(implicit session: ValpasSession): Either[HttpStatus, Seq[OppijaHakutilanteillaLaajatTiedot]] = {
    val errorClue = oppilaitosOids.size match {
      case 1 => s"oppilaitos:${oppilaitosOids.head}"
      case n if n > 1 => s"oppilaitokset[$n]:${oppilaitosOids.head}, ..."
      case _ => ""
    }

    accessResolver.organisaatiohierarkiaOids(oppilaitosOids)
      .map(opiskeluoikeusDbService.getPeruskoulunValvojalleNäkyvätOppijat)
      .flatMap(results => HttpStatus.foldEithers(results.map(asValpasOppijaLaajatTiedot)))
      .map(fetchHaut(errorClue))
  }

  // TODO: Tästä puuttuu oppijan tietoihin käsiksi pääsy seuraavilta käyttäjäryhmiltä:
  // (1) muut kuin peruskoulun hakeutumisen valvojat (esim. nivelvaihe ja aikuisten perusopetus)
  // (4) OPPILAITOS_SUORITTAMINEN-, OPPILAITOS_MAKSUTTOMUUS- ja KUNTA -käyttäjät.
  def getOppijaLaajatTiedot
    (oppijaOid: ValpasHenkilö.Oid)
    (implicit session: ValpasSession)
  : Either[HttpStatus, ValpasOppijaLaajatTiedot] = {
    opiskeluoikeusDbService.getPeruskoulunValvojalleNäkyväOppija(oppijaOid)
      .toRight(ValpasErrorCategory.forbidden.oppija())
      .flatMap(asValpasOppijaLaajatTiedot)
      .flatMap(accessResolver.withOppijaAccess)
  }

  def getOppijaHakutilanteillaLaajatTiedot
    (oppijaOid: ValpasHenkilö.Oid)
    (implicit session: ValpasSession)
  : Either[HttpStatus, OppijaHakutilanteillaLaajatTiedot] = {
    getOppijaLaajatTiedot(oppijaOid)
      .map(fetchHaku)
      .flatMap(o => fetchVirallisetYhteystiedot(o.oppija).map(yhteystiedot => o.copy(
        yhteystiedot = o.yhteystiedot ++ yhteystiedot.map(yt =>
          ValpasYhteystiedot.virallinenYhteystieto(yt, localizationRepository.get("oppija__viralliset_yhteystiedot"))
        )
      )))
      .map(_.validate(koodistoviitepalvelu))
  }

  private def asValpasOppijaLaajatTiedot(dbRow: ValpasOppijaRow): Either[HttpStatus, ValpasOppijaLaajatTiedot] = {
    validatingAndResolvingExtractor
      .extract[List[ValpasOpiskeluoikeusLaajatTiedot]](dbRow.opiskeluoikeudet)
      .map(opiskeluoikeudet =>
        ValpasOppijaLaajatTiedot(
          henkilö = ValpasHenkilöLaajatTiedot(
            oid = dbRow.oppijaOid,
            hetu = dbRow.hetu,
            syntymäaika = dbRow.syntymäaika,
            etunimet = dbRow.etunimet,
            sukunimi = dbRow.sukunimi,
            turvakielto = dbRow.turvakielto,
            äidinkieli = dbRow.äidinkieli
          ),
          oikeutetutOppilaitokset = dbRow.oikeutetutOppilaitokset,
          opiskeluoikeudet = opiskeluoikeudet
        )
      )
  }

  private def fetchHaku(oppija: ValpasOppijaLaajatTiedot): OppijaHakutilanteillaLaajatTiedot = {
    val hakukoosteet = hakukoosteService.getYhteishakujenHakukoosteet(oppijaOids = Set(oppija.henkilö.oid), ainoastaanAktiivisetHaut = false, errorClue = s"oppija:${oppija.henkilö.oid}")
    val yhteystiedot = hakukoosteet.map(ilmoitetutYhteystiedot).getOrElse(Seq.empty)
    OppijaHakutilanteillaLaajatTiedot.apply(oppija, hakukoosteet).copy(yhteystiedot = yhteystiedot)
  }

  private def fetchHaut(errorClue: String)(oppijat: Seq[ValpasOppijaLaajatTiedot]): Seq[OppijaHakutilanteillaLaajatTiedot] = {
    hakukoosteService.getYhteishakujenHakukoosteet(oppijaOids = oppijat.map(_.henkilö.oid).toSet, ainoastaanAktiivisetHaut = true, errorClue = errorClue)
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
          .toRight(ValpasErrorCategory.internalError("Virallisten yhteystietojen haku epäonnistui"))
          .map(_.yhteystiedot.flatMap(yt => {
            val alkuperä = koodistoviitepalvelu.validate(yt.alkuperä)
              .filter(_.koodiarvo == "alkupera1") // Filtteröi pois muut kuin VTJ:ltä peräisin olevat yhteystiedot
            val tyyppi = koodistoviitepalvelu.validate(yt.tyyppi)
            (alkuperä, tyyppi) match {
              case (Some(alkuperä), Some(tyyppi)) => Some(yt.copy(alkuperä = alkuperä, tyyppi = tyyppi))
              case _ => None
            }
          }))
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
}
