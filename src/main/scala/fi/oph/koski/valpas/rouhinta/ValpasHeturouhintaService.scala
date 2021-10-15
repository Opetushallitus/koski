package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.henkilo.{Hetu, OppijaHenkilö}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Finnish, Koodistokoodiviite, LocalizedString}
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.OppijaHakutilanteillaLaajatTiedot
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasOpiskeluoikeusLaajatTiedot}
import fi.oph.koski.valpas.valpasrepository.ValpasOppivelvollisuudenKeskeytys
import fi.oph.koski.valpas.valpasuser.ValpasSession
import slick.jdbc.GetResult

import java.time.LocalDate

class ValpasHeturouhintaService(application: KoskiApplication) extends DatabaseConverters with Logging with Timing {
  private val db = application.raportointiDatabase
  private val rajapäivätService = application.valpasRajapäivätService
  private val oppijanumerorekisteri = application.opintopolkuHenkilöFacade
  private val oppijaService = application.valpasOppijaService

  def haeHetulistanPerusteella
    (hetut: Seq[String])
    (implicit session: ValpasSession)
  : Either[HttpStatus, HeturouhinnanTulos] = {
    val (validitHetut, virheellisetHetut) = hetut.partition(hetu => Hetu.validate(hetu, acceptSynthetic = false).isRight)
    val oppijatKoskessa = haeOppijat(validitHetut)
    val koskestaLöytymättömätHetut = validitHetut.diff(oppijatKoskessa.map(_.hetu))

    val (oppijatJotkaOnrissaMuttaEiKoskessa, oppijanumerorekisterinUlkopuolisetHetut) = haeOppijanumerorekisteristä(koskestaLöytymättömätHetut)

    val (oppivelvollisetOnrissa, oppivelvollisuudenUlkopuolisetOnrissa) = oppijatJotkaOnrissaMuttaEiKoskessa.partition(onOppivelvollinen)
    val (oppivelvollisetKoskessa, oppivelvollisuudenUlkopuolisetKoskessa) = oppijatKoskessa.partition(_.oppivelvollisuusVoimassa)

    oppijaService
      .getOppijalista(oppivelvollisetKoskessa.map(_.masterOid))
      .map(oppivelvollisetKoskessa => HeturouhinnanTulos(
        oppivelvolliset = oppivelvollisetKoskessa.map(RouhintaOppivelvollinen.apply) ++ oppivelvollisetOnrissa.map(RouhintaOppivelvollinen.apply),
        oppijanumerorekisterinUlkopuoliset = oppijanumerorekisterinUlkopuolisetHetut.map(RouhintaPelkkäHetu),
        oppivelvollisuudenUlkopuoliset = (oppivelvollisuudenUlkopuolisetKoskessa.map(_.hetu) ++ oppivelvollisuudenUlkopuolisetOnrissa.flatMap(_.hetu)).map(RouhintaPelkkäHetu),
        virheellisetHetut = virheellisetHetut.map(RouhintaPelkkäHetu),
      ))
  }

  private def haeOppijat(hetut: Seq[String]): Seq[HetuMasterOid] = {
    db.runDbSync(sql"""
      SELECT
        r_henkilo.master_oid as master_oid,
        r_henkilo.hetu as hetu,
        r_henkilo.syntymaaika as syntymaaika,
        COALESCE(${rajapäivätService.tarkastelupäivä} BETWEEN oppivelvollisuustiedot.oppivelvollisuusvoimassaalkaen AND oppivelvollisuustiedot.oppivelvollisuusvoimassaasti, false) as oppivelvollisuus_voimassa
      FROM r_henkilo
      LEFT JOIN oppivelvollisuustiedot ON (r_henkilo.master_oid = oppivelvollisuustiedot.oppija_oid)
      WHERE
        r_henkilo.hetu = any($hetut)
      """.as[HetuMasterOid])
  }

  private def haeOppijanumerorekisteristä(hetut: Seq[String]): (Seq[OppijaHenkilö], Seq[String]) = {
    val oppijat = oppijanumerorekisteri.findOppijatByHetusNoSlaveOids(hetut)
    val hetutRekisterissä = oppijat.flatMap(_.hetu)
    (
      oppijat,
      hetut.filterNot(hetutRekisterissä.contains)
    )
  }

  private def onOppivelvollinen(oppija: OppijaHenkilö): Boolean = onOppivelvollinen(oppija.syntymäaika)

  private def onOppivelvollinen(syntymäaika: Option[LocalDate]): Boolean = {
    syntymäaika match {
      case Some(syntymäaika) => {
        val oppivelvollisuusAlkaa = rajapäivätService.oppivelvollisuusAlkaa(syntymäaika)
        val oppivelvollisuusLoppuu = syntymäaika.plusYears(rajapäivätService.oppivelvollisuusLoppuuIka.toLong)
        !oppivelvollisuusAlkaa.isAfter(rajapäivätService.tarkastelupäivä) && oppivelvollisuusLoppuu.isAfter(rajapäivätService.tarkastelupäivä)
      }
      case None => false
    }
  }
}

case class HetuMasterOid(
  hetu: String,
  syntymäaika: Option[LocalDate],
  masterOid: ValpasHenkilö.Oid,
  oppivelvollisuusVoimassa: Boolean,
)

object HetuMasterOid {
  implicit def getResult: GetResult[HetuMasterOid] = GetResult(r => {
    HetuMasterOid(
      hetu = r.rs.getString("hetu"),
      syntymäaika = r.getLocalDateOption("syntymaaika"),
      masterOid = r.rs.getString("master_oid"),
      oppivelvollisuusVoimassa = r.rs.getBoolean("oppivelvollisuus_voimassa"),
    )
  })
}

case class HeturouhinnanTulos(
  oppivelvolliset: Seq[RouhintaOppivelvollinen],
  oppijanumerorekisterinUlkopuoliset: Seq[RouhintaPelkkäHetu],
  oppivelvollisuudenUlkopuoliset: Seq[RouhintaPelkkäHetu],
  virheellisetHetut: Seq[RouhintaPelkkäHetu],
)

case class RouhintaOppivelvollinen(
  oppijanumero: ValpasHenkilö.Oid,
  etunimet: String,
  sukunimi: String,
  syntymäaika: Option[LocalDate],
  hetu: Option[String],
  viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus: Option[RouhintaOppivelvollisuus],
  oppivelvollisuudenKeskeytys: Seq[ValpasOppivelvollisuudenKeskeytys],
)

case class RouhintaOppivelvollisuus(
  suorituksenTyyppi: Koodistokoodiviite,
  päättymispäivä: Option[String],
  viimeisinTila: Koodistokoodiviite,
  toimipiste: LocalizedString,
)

object RouhintaOppivelvollinen {
  def apply(tiedot: OppijaHakutilanteillaLaajatTiedot): RouhintaOppivelvollinen = {
    val oo = tiedot.oppija.opiskeluoikeudet
      .filter(_.oppivelvollisuudenSuorittamiseenKelpaava)
      .sortBy(oo => Seq(
        oo.perusopetusTiedot.flatMap(_.päättymispäivä),
        oo.perusopetuksenJälkeinenTiedot.flatMap(_.päättymispäivä),
        oo.perusopetusTiedot.flatMap(_.alkamispäivä),
        oo.perusopetuksenJälkeinenTiedot.flatMap(_.alkamispäivä),
      ).flatten.headOption.getOrElse("9999-99-99")
      )
      .lastOption

    RouhintaOppivelvollinen(
      oppijanumero = tiedot.oppija.henkilö.oid,
      etunimet = tiedot.oppija.henkilö.etunimet,
      sukunimi = tiedot.oppija.henkilö.sukunimi,
      syntymäaika = tiedot.oppija.henkilö.syntymäaika,
      hetu = tiedot.oppija.henkilö.hetu,
      viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus = oo.flatMap(RouhintaOppivelvollisuus.apply),
      oppivelvollisuudenKeskeytys = tiedot.oppivelvollisuudenKeskeytykset.filter(_.voimassa)
    )
  }

  def apply(henkilö: OppijaHenkilö): RouhintaOppivelvollinen = RouhintaOppivelvollinen(
    oppijanumero = henkilö.oid,
    etunimet = henkilö.etunimet,
    sukunimi = henkilö.sukunimi,
    syntymäaika = henkilö.syntymäaika,
    hetu = henkilö.hetu,
    viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus = None,
    oppivelvollisuudenKeskeytys = Nil,
  )
}

object RouhintaOppivelvollisuus {
  def apply(oo: ValpasOpiskeluoikeusLaajatTiedot): Option[RouhintaOppivelvollisuus] = {
    oo.tarkasteltavaPäätasonSuoritus.flatMap(päätasonSuoritus => {
      ((oo.perusopetusTiedot, oo.perusopetuksenJälkeinenTiedot) match {
        case (Some(o), None) => Some((o.päättymispäivä, o.tarkastelupäivänKoskiTila))
        case (None, Some(o)) => Some((o.päättymispäivä, o.tarkastelupäivänKoskiTila))
        case _ => None
      }).map(tila => {
        RouhintaOppivelvollisuus(
          suorituksenTyyppi = päätasonSuoritus.suorituksenTyyppi,
          päättymispäivä = tila._1,
          viimeisinTila = tila._2,
          toimipiste = oo.tarkasteltavaPäätasonSuoritus.map(_.toimipiste.nimi).getOrElse(Finnish("???")),
        )
      })
    })
  }
}

case class RouhintaPelkkäHetu(
  hetu: String,
)
