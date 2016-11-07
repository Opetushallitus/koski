package fi.oph.koski.suoritusote

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema._

import scala.xml.Elem

class IBOpintosuoritusoteHtml(implicit override val user: KoskiSession) extends OpintosuoritusoteHtml {
  def ib(ht: TäydellisetHenkilötiedot, opiskeluoikeudet: List[IBOpiskeluoikeus]): Elem = {
    bodyHtml(ht, <div>{
      val ibTutkinnonSuoritukset: List[IBTutkinnonSuoritus] = opiskeluoikeudet.flatMap(_.suoritukset.collect { case s: IBTutkinnonSuoritus => s })
      val theoryOfKnowledgeSuoritukset: List[IBTheoryOfKnowledgeSuoritus] = ibTutkinnonSuoritukset.flatMap(_.theoryOfKnowledge)
      val ibAineidenSuoritukset: List[IBOppiaineenSuoritus] = ibTutkinnonSuoritukset.flatMap(_.osasuoritukset.toList.flatten)
      val preIBSuoritukset: List[PreIBOppiaineenSuoritus] = opiskeluoikeudet.flatMap(_.suoritukset.collect{ case s: PreIBSuoritus => s }).flatMap(_.osasuoritukset.toList.flatten)
      
      val ibSuorituksetRyhmittäin: Map[String, List[Suoritus]] = (ibAineidenSuoritukset ++ theoryOfKnowledgeSuoritukset).groupBy { s =>
        s.koulutusmoduuli match {
          case x: IBAineRyhmäOppiaine => i(x.ryhmä.lyhytNimi)
          case _ => "Others"
        }
      }

      val ibSuoritusElements = ibSuorituksetRyhmittäin.keys.toList.sorted.map { group =>
        <div>
          {suorituksetHtml(suorituksetSyvyydellä(ibSuorituksetRyhmittäin(group)), group)}
        </div>
      }

      suorituksetHtml(suorituksetSyvyydellä(preIBSuoritukset), "Preliminary year courses") ++
        <h3>International Baccalaureate Diploma Programme</h3> ++
        <div class="ib-studies">{ibSuoritusElements}</div>
    }</div>)
  }

  override protected def arvosana(suoritus: Suoritus): String = {
    val effort = suoritus match {
      case IBKurssinSuoritus(_, _, Some(viimeisinArviointi :: xs), _, _) => viimeisinArviointi.effort.map(_.koodiarvo).getOrElse("")
      case _ => ""
    }
    super.arvosana(suoritus) + effort
  }
}
