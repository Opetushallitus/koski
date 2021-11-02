package fi.oph.koski.valpas.rouhinta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.DatabaseConverters
import fi.oph.koski.henkilo.{Hetu, OppijaHenkilö}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Timing
import fi.oph.koski.valpas.valpasuser.ValpasSession
import fi.oph.koski.valpas.ValpasErrorCategory

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
        .map(oppivelvollisetKoskessa => {
          val (suorittavat, eiSuorittavat) =
            (oppivelvollisetKoskessa.map(ValpasRouhintaOppivelvollinen.apply) ++ oppivelvollisetOnrissa.map(ValpasRouhintaOppivelvollinen.apply))
              .partition(_.suorittaaOppivelvollisuutta)

          HeturouhinnanTulos(
            eiOppivelvollisuuttaSuorittavat = eiSuorittavat,
            oppivelvollisuuttaSuorittavat = suorittavat.flatMap(_.hetu).map(RouhintaPelkkäHetu),
            oppijanumerorekisterinUlkopuoliset = oppijanumerorekisterinUlkopuolisetHetut.map(RouhintaPelkkäHetu),
            oppivelvollisuudenUlkopuoliset = (oppivelvollisuudenUlkopuolisetKoskessa.map(_.hetu) ++ oppivelvollisuudenUlkopuolisetOnrissa.flatMap(_.hetu)).map(RouhintaPelkkäHetu),
            virheellisetHetut = virheellisetHetut.map(RouhintaPelkkäHetu),
          )
        })
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
  eiOppivelvollisuuttaSuorittavat: Seq[ValpasRouhintaOppivelvollinen],
  oppivelvollisuuttaSuorittavat: Seq[RouhintaPelkkäHetu],
  oppijanumerorekisterinUlkopuoliset: Seq[RouhintaPelkkäHetu],
  oppivelvollisuudenUlkopuoliset: Seq[RouhintaPelkkäHetu],
  virheellisetHetut: Seq[RouhintaPelkkäHetu],
)

case class RouhintaPelkkäHetu(
  hetu: String,
)
