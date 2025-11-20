package fi.oph.koski.todistus.yleinenkielitutkinto

import fi.oph.koski.todistus.pdfgenerator.TodistusData
import scala.jdk.CollectionConverters._

case class YleinenKielitutkintoTodistusData(
  templateName: String,
  oppijaNimi: String,
  oppijaSyntymäaika: String,
  tutkinnonNimi: String,
  suorituksetJaArvosanat: List[YleinenKielitutkintoSuoritusJaArvosana],
  tasonArvosanarajat: String,
  järjestäjäNimi: String,
  allekirjoitusPäivämäärä: String,
  vahvistusViimeinenPäivämäärä: String
 ) extends TodistusData {

  def toTemplateVariables: Map[String, Object] = Map(
    "oppijaNimi" -> oppijaNimi,
    "oppijaSyntymäaika" -> oppijaSyntymäaika,
    "tutkinnonNimi" -> tutkinnonNimi,
    "suorituksetJaArvosanat" -> suorituksetJaArvosanat.asJava,
    "tasonArvosanarajat" -> tasonArvosanarajat,
    "järjestäjäNimi" -> järjestäjäNimi,
    "allekirjoitusPäivämäärä" -> allekirjoitusPäivämäärä,
    "vahvistusViimeinenPäivämäärä" -> vahvistusViimeinenPäivämäärä
  )
}

case class YleinenKielitutkintoSuoritusJaArvosana(
  suoritus: String,
  arvosana: String
)
