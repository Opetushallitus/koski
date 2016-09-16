package fi.oph.koski.suoritusote

import java.text.NumberFormat
import java.time.LocalDate

import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.localization.Locale._
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.schema._
import fi.oph.koski.todistus.LocalizedHtml

import scala.xml.{Elem, Node}

class IBOpintosuoritusoteHtml(implicit override val user: KoskiUser) extends OpintosuoritusoteHtml {
  def ib(ht: TäydellisetHenkilötiedot, opiskeluoikeudet: List[IBOpiskeluoikeus]): Elem = {
    bodyHtml(ht, <div>
      {
      val (preIBSuoritukset, ibSuoritukset) = opiskeluoikeudet
        .flatMap(_.suoritukset.flatMap(_.osasuoritukset.toList.flatten))
        .partition(_.isInstanceOf[PreIBOppiaineenSuoritus])
      suorituksetHtml(suorituksetSyvyydellä(preIBSuoritukset), "Preliminary year courses") ++
      suorituksetHtml(suorituksetSyvyydellä(ibSuoritukset), "INTERNATIONAL BACCALAUREATE DIPLOMA PROGRAMME")
      }
    </div>)
  }

  override protected def arvosana(suoritus: Suoritus): String = {
    val effort = suoritus match {
      case IBKurssinSuoritus(_, _, Some(viimeisinArviointi :: xs), _, _) => viimeisinArviointi.effort.map(_.koodiarvo).getOrElse("")
      case _ => ""
    }
    super.arvosana(suoritus) + effort
  }
}
