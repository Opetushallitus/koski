package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.henkilo.{Hetu, OppijaHenkilö}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasHenkilö, ValpasOpiskeluoikeusLaajatTiedot, ValpasOpiskeluoikeusTiedot}
import fi.oph.koski.valpas.valpasrepository.ValpasOppivelvollisuudenKeskeytys
import fi.oph.koski.valpas.valpasuser.ValpasSession
import fi.oph.koski.valpas.{OppijaHakutilanteillaLaajatTiedot, ValpasErrorCategory}

import java.time.LocalDate

class ValpasHeturouhintaService(application: KoskiApplication) extends DatabaseConverters with Logging with Timing {
  private val rajapäivätService = application.valpasRajapäivätService
  private val oppijanumerorekisteri = application.opintopolkuHenkilöFacade
  private val oppijaService = application.valpasOppijaService

  private val maxHetuCount = application.config.getInt("valpas.rouhintaMaxHetuCount")

  def haeHetulistanPerusteella
    (hetut: Seq[String])
    (implicit session: ValpasSession)
  : Either[HttpStatus, HeturouhinnanTulos] = {
    cleanedHetuList(hetut).flatMap(hetut => {
      val (validitHetut, virheellisetHetut) = hetut.partition(hetu => Hetu.validate(hetu, acceptSynthetic = false).isRight)
      val oppijatKoskessa = oppijaService.getOppijaOiditHetuillaIlmanOikeustarkastusta(validitHetut)
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
    })
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

  private def cleanedHetuList(hetut: Seq[String]): Either[HttpStatus, Seq[String]] = {
    val list = hetut.map(_.trim).filter(_.nonEmpty)
    if (list.length > maxHetuCount) {
      Left(ValpasErrorCategory.badRequest.requestTooLarge(s"Kyselyssä oli liian monta hetua (${list.length} / $maxHetuCount)"))
    } else {
      Right(list)
    }
  }
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
  viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus: Option[RouhintaOpiskeluoikeus],
  oppivelvollisuudenKeskeytys: Seq[ValpasOppivelvollisuudenKeskeytys],
)

object RouhintaOppivelvollinen {
  def apply(tiedot: OppijaHakutilanteillaLaajatTiedot): RouhintaOppivelvollinen = {
    val oos = tiedot.oppija.opiskeluoikeudet
      .filter(_.oppivelvollisuudenSuorittamiseenKelpaava)
      .flatMap(oo => RouhintaOpiskeluoikeus.apply(oo))

    RouhintaOppivelvollinen(
      oppijanumero = tiedot.oppija.henkilö.oid,
      etunimet = tiedot.oppija.henkilö.etunimet,
      sukunimi = tiedot.oppija.henkilö.sukunimi,
      syntymäaika = tiedot.oppija.henkilö.syntymäaika,
      hetu = tiedot.oppija.henkilö.hetu,
      viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus = oos.sorted.lastOption,
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

case class RouhintaOpiskeluoikeus(
  suorituksenTyyppi: Koodistokoodiviite,
  päättymispäivä: Option[String],
  viimeisinTila: Koodistokoodiviite,
  toimipiste: LocalizedString,
) extends Ordered[RouhintaOpiskeluoikeus] {
  override def compare(that: RouhintaOpiskeluoikeus): Int =
    OrderedOpiskeluoikeusTiedot.compareDates(päättymispäivä, that.päättymispäivä)
}

object RouhintaOpiskeluoikeus {
  def apply(oo: ValpasOpiskeluoikeusLaajatTiedot): Option[RouhintaOpiskeluoikeus] = {
    oo.tarkasteltavaPäätasonSuoritus.flatMap(päätasonSuoritus => {
      Seq(
        oo.perusopetusTiedot.map(t => OrderedOpiskeluoikeusTiedot(t)),
        oo.perusopetuksenJälkeinenTiedot.map(t => OrderedOpiskeluoikeusTiedot(t)),
      )
        .flatten
        .sorted
        .lastOption
        .map(viimeisinTila => RouhintaOpiskeluoikeus(
          suorituksenTyyppi = päätasonSuoritus.suorituksenTyyppi,
          päättymispäivä = viimeisinTila.tiedot.päättymispäivä,
          viimeisinTila = viimeisinTila.tiedot.tarkastelupäivänKoskiTila,
          toimipiste = päätasonSuoritus.toimipiste.nimi,
        ))
    })
  }
}

case class OrderedOpiskeluoikeusTiedot(
  tiedot: ValpasOpiskeluoikeusTiedot
) extends Ordered[OrderedOpiskeluoikeusTiedot] {
  override def compare(that: OrderedOpiskeluoikeusTiedot): Int =
    if (tiedot.päättymispäivä != that.tiedot.päättymispäivä) {
      OrderedOpiskeluoikeusTiedot.compareDates(tiedot.päättymispäivä, that.tiedot.päättymispäivä)
    } else {
      OrderedOpiskeluoikeusTiedot.compareDates(tiedot.alkamispäivä, that.tiedot.alkamispäivä)
    }
}

object OrderedOpiskeluoikeusTiedot {
  def compareDates(a: Option[String], b: Option[String]): Int =
    a.getOrElse("9999-99-99").compare(b.getOrElse("9999-99-99"))
}

case class RouhintaPelkkäHetu(
  hetu: String,
)
