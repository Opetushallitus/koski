package fi.oph.koski.valpas.oppivelvollisuudestavapautus

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.raportit.RaportitService
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.{Henkilö, OrganisaatioWithOid}
import fi.oph.koski.util.ChainingSyntax.localDateOps
import fi.oph.koski.util.ChunkReader
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat
import fi.oph.koski.valpas.kuntailmoitus.ValpasKunnat
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService
import fi.oph.koski.valpas.oppija.{ValpasAccessResolver, ValpasErrorCategory}
import fi.oph.koski.valpas.valpasuser.ValpasSession

import java.time.{LocalDate, LocalDateTime}

class ValpasOppivelvollisuudestaVapautusService(application: KoskiApplication) extends Logging {
  val db = new ValpasOppivelvollisuudestaVapautusRepository(application.valpasDatabase.db)
  private val organisaatioService = application.organisaatioService
  private val rajapäivätService = application.valpasRajapäivätService
  private val accessResolver = new ValpasAccessResolver
  private val raportitService = new RaportitService(application)

  private val VAPAUTUS_SALLITTU_AIKAISINTAAN = if (Environment.isMockEnvironment(application.config)) {
    LocalDate.of(1999, 8, 1)
  } else {
    LocalDate.of(2022, 8, 1)
  }

  def mapVapautetutOppijat[T](oppijat: Seq[T], toOids: T => Seq[String])(map: (T, OppivelvollisuudestaVapautus) => T): Seq[T] = {
    val oppijaOids = oppijat.flatMap(toOids)
    val vapautukset = db.getOppivelvollisuudestaVapautetutOppijat(oppijaOids)
    val aktiivisetKunnat = organisaatioService.aktiivisetKunnat()
    val dueTime = raportitService.viimeisinOpiskeluoikeuspäivitystenVastaanottoaika

    oppijat.map { oppija =>
      vapautukset
        .flatMap(OppivelvollisuudestaVapautus.apply(aktiivisetKunnat, rajapäivätService, dueTime))
        .find(v => toOids(oppija).contains(v.oppijaOid)) match {
          case Some(vapautus) => map(oppija, vapautus)
          case None => oppija
        }
    }
  }

  def lisääOppivelvollisuudestaVapautus(vapautus: UusiOppivelvollisuudestaVapautus)(implicit session: ValpasSession): Either[HttpStatus, Unit] =
    withValidation(Some(vapautus.vapautettu), vapautus.kuntakoodi) { () =>
      db.lisääOppivelvollisuudestaVapautus(vapautus.oppijaOid, session.oid, vapautus.vapautettu, vapautus.kuntakoodi)
    }

  def mitätöiOppivelvollisuudestaVapautus(vapautus: OppivelvollisuudestaVapautuksenMitätöinti)(implicit session: ValpasSession): Either[HttpStatus, Unit] =
    withValidation(None, vapautus.kuntakoodi) { () =>
      if (db.mitätöiOppivelvollisuudestaVapautus(vapautus.oppijaOid, vapautus.kuntakoodi)) {
        Right(Unit)
      } else {
        Left(ValpasErrorCategory.notFound.vapautustaEiLöydy())
      }
    }

  def pohjatiedot(implicit session: ValpasSession): OppivelvollisuudestaVapautuksenPohjatiedot = OppivelvollisuudestaVapautuksenPohjatiedot(
    aikaisinPvm = VAPAUTUS_SALLITTU_AIKAISINTAAN,
    kunnat = ValpasKunnat.getUserKunnat(organisaatioService).sortBy(_.nimi.map(_.get(session.lang)))
  )

  def kaikkiVapautuksetIteratorIlmanKäyttöoikeustarkastusta(pageSize: Int): ChunkReader[RawOppivelvollisuudestaVapautus] = {
    val pvmRaja = rajapäivätService.tarkastelupäivä
    new ChunkReader(pageSize, chunk => {
      val list = db.readPage(chunk.offset, chunk.pageSize)
      if (list.isEmpty) {
        None
      } else {
        Some(list.filter(e => e.mitätöity.isEmpty && e.vapautettu.isEqualOrBefore(pvmRaja)))
      }
    })
  }

  private def withValidation[T](aloitusPvm: Option[LocalDate], kotipaikkaKoodiarvo: String)(fn: () => T)(implicit session: ValpasSession): Either[HttpStatus, T] =
    if (accessResolver.accessToKuntaOrg(kotipaikkaKoodiarvo)) {
      if (aloitusPvm.exists(_.isBefore(VAPAUTUS_SALLITTU_AIKAISINTAAN))) {
        Left(ValpasErrorCategory.badRequest.validation.virheellinenPäivämäärä(s"Aikaisin sallittu oppivelvollisuudesta vapautuksen alkamispäivä on ${VAPAUTUS_SALLITTU_AIKAISINTAAN.format(finnishDateFormat)}"))
      } else {
        Right(fn())
      }
    } else {
      Left(ValpasErrorCategory.forbidden.organisaatio())
    }
}

case class UusiOppivelvollisuudestaVapautus(
  oppijaOid: Oid,
  vapautettu: LocalDate,
  kuntakoodi: String,
)

case class OppivelvollisuudestaVapautuksenMitätöinti(
  oppijaOid: Oid,
  kuntakoodi: String,
)

case class OppivelvollisuudestaVapautus(
  oppijaOid: Henkilö.Oid,
  vapautettu: LocalDate,
  tulevaisuudessa: Boolean,
  mitätöitymässä: Boolean,
  kunta: Option[OrganisaatioWithOid],
) {
  def poistaTurvakiellonAlaisetTiedot: OppivelvollisuudestaVapautus = copy(kunta = None)
  def oppivelvollisuusVoimassaAsti: LocalDate = vapautettu.minusDays(1)
  def oikeusKoulutuksenMaksuttomuuteenVoimassaAsti: LocalDate = oppivelvollisuusVoimassaAsti
}

object OppivelvollisuudestaVapautus {
  def apply(
    aktiivisetKunnat: Seq[OrganisaatioWithOid],
    rajapäivätService: ValpasRajapäivätService,
    raportointikantaDueTime: LocalDateTime
  )(vapautus: RawOppivelvollisuudestaVapautus): Option[OppivelvollisuudestaVapautus] = {
    val mitätöity = vapautus.mitätöity.isDefined
    val mitätöintiPäivittynytRaportointikantaan = vapautus.mitätöity.exists(_.isBefore(raportointikantaDueTime))
    val raportointikantaanEiVieläOlePäivitettyVapautusta = vapautus.aikaleima.isAfter(raportointikantaDueTime)

    if (mitätöity && (mitätöintiPäivittynytRaportointikantaan || raportointikantaanEiVieläOlePäivitettyVapautusta)) {
      None
    } else {
      Some(OppivelvollisuudestaVapautus(
        oppijaOid = vapautus.oppijaOid,
        vapautettu = vapautus.vapautettu,
        tulevaisuudessa = rajapäivätService.tarkastelupäivä.isBefore(vapautus.vapautettu),
        mitätöitymässä = vapautus.mitätöity.isDefined,
        kunta = aktiivisetKunnat.find(_.kotipaikka.exists(_.koodiarvo == vapautus.kunta)),
      ))
    }
  }
}

case class OppivelvollisuudestaVapautuksenPohjatiedot(
  aikaisinPvm: LocalDate,
  kunnat: Seq[OrganisaatioWithOid],
)
