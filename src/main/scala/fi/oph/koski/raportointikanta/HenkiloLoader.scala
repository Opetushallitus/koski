package fi.oph.koski.raportointikanta

import fi.oph.koski.henkilo.{Hetu, OpintopolkuHenkilöFacade}
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.koodisto.{KoodistoPalvelu, Kunta}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository
import fi.oph.koski.schema.{MaksuttomuusTieto, OikeuttaMaksuttomuuteenPidennetty}

import java.sql.Date

object HenkilöLoader extends Logging {
  private val BatchSize = 1000
  private val name = "henkilot"

  def loadHenkilöt(opintopolkuHenkilöFacade: OpintopolkuHenkilöFacade,
                   db: RaportointiDatabase,
                   koodistoPalvelu: KoodistoPalvelu,
                   opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository): Int = {
    logger.info("Ladataan henkilö-OIDeja opiskeluoikeuksista...")
    // note: this list has 1-2M oids in production.
    val oids = db.oppijaOidsFromOpiskeluoikeudet
    logger.info(s"Löytyi ${oids.size} henkilö-OIDia")
    db.setStatusLoadStarted(name)
    var masterOids = scala.collection.mutable.Set[String]()
    val count = oids.toList.grouped(BatchSize).map(batchOids => {
      val batchOppijat = opintopolkuHenkilöFacade.findMasterOppijat(batchOids)
      val batchRows = batchOppijat.map { case (oid, oppija) => {
        buildRHenkilöRow(oid, oppija, koodistoPalvelu, opiskeluoikeusRepository)
      } }.toList
      db.loadHenkilöt(batchRows)
      db.setLastUpdate(name)
      batchRows.foreach(masterOids += _.masterOid)

      val kotikuntahistoria = opintopolkuHenkilöFacade.findKuntahistoriat(batchOids).map(_.toDbRow)
      db.loadKotikuntahistoria(kotikuntahistoria)

      batchRows.size
    }).sum

    val masterOidsEiKoskessa = masterOids.diff(oids.toSet)

    val masterFetchCount =  masterOidsEiKoskessa.toList.grouped(BatchSize).map(batchOids => {
      val batchOppijat = opintopolkuHenkilöFacade.findMasterOppijat(batchOids)
      val batchRows = batchOppijat.map { case (oid, oppija) => buildRHenkilöRow(oid, oppija, koodistoPalvelu, opiskeluoikeusRepository) }.toList
      db.loadHenkilöt(batchRows)
      batchRows.size
    }).sum
    val total = count + masterFetchCount

    db.setStatusLoadCompletedAndCount(name, total)
    logger.info(s"Ladattiin $total henkilöä")
    logger.info(s"Haettiin masterMaster tiedot $masterFetchCount henkilölle")
    logger.info(s"Puuttuvia masterMaster pareja ${masterOidsEiKoskessa.size - masterFetchCount}")
    total
  }

  private def buildRHenkilöRow(oid: String,
                               oppija: LaajatOppijaHenkilöTiedot,
                               koodistoPalvelu: KoodistoPalvelu,
                               opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository) =
    RHenkilöRow(
      oppijaOid = oid,
      masterOid = oppija.oid,
      linkitetytOidit = oppija.linkitetytOidit,
      hetu = oppija.hetu,
      sukupuoli = oppija.sukupuoli,
      syntymäaika = oppija.syntymäaika.orElse(oppija.hetu.flatMap(Hetu.toBirthday)).map(Date.valueOf),
      sukunimi = oppija.sukunimi,
      etunimet = oppija.etunimet,
      aidinkieli = oppija.äidinkieli,
      kansalaisuus = oppija.kansalaisuus.filter(_.nonEmpty).map(_.sorted.mkString(",")),
      turvakielto = oppija.turvakielto,
      kotikunta = oppija.kotikunta,
      kotikuntaNimiFi = Kunta.getKunnanNimi(oppija.kotikunta, koodistoPalvelu, "fi"),
      kotikuntaNimiSv = Kunta.getKunnanNimi(oppija.kotikunta, koodistoPalvelu, "sv"),
      yksiloity =  oppija.yksilöity
      // TODO: Disabloitu ainakin väliaikaiseksi, kun kokeillaan SQL-pohjaista tapaa näiden laskemiseen.
      // Nyt jos tämä maksuttomuuden pidennys lasketaan tässä kohtaa, joudutaan käymään kokonaisuutena
      // kaksi kertaa läpi kaikki opiskeluoikeudet, mitä Koskessa on, kun raportointikantaa luodaan.
      //oikeuttaMaksuttomuuteenPidennettyYhteensä = oikeuttaMaksuttomuuteenPidennettyYhteensä(oppija, opiskeluoikeusRepository)
    )

  private def oikeuttaMaksuttomuuteenPidennettyYhteensä(oppija: LaajatOppijaHenkilöTiedot, opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository): Int = {
    val maksuttomuusJaksot = opiskeluoikeusRepository.findByOppija(oppija, false, false)(KoskiSpecificSession.systemUser).map(
      _.map(_.lisätiedot match {
        case Some(tiedot) => {
          tiedot match {
            case maksuttomuusTieto: MaksuttomuusTieto => {
              maksuttomuusTieto.oikeuttaMaksuttomuuteenPidennetty.toList.flatten
            }
            case _ => List()
          }
        }
        case None => List()
      })
    )

    maksuttomuusJaksot.warningsToLeft match {
      case Right(jaksot) => OikeuttaMaksuttomuuteenPidennetty.maksuttomuusJaksojenYhteenlaskettuPituus(jaksot.flatten)
      case _ => {
        logger.error(s"Pidennystä maksuttoman opiskelun oikeuteen ei voitu laskea oppijalle ${oppija.oid}")
        0
      }
    }
  }
}
