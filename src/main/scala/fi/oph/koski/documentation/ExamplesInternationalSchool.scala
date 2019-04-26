package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.InternationalSchoolExampleData._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.schema._

object ExamplesInternationalSchool {

  val gradeExplorer: PYPVuosiluokanSuoritus = pypSuoritus("explorer", Some(date(2006, 6, 30))).copy(
    osasuoritukset = Some(List(
      oppiaineenSuoritus(oppiaine("LA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("MA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("PE"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("VA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("MU"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("ICT"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("LIB"), arviointi("achievedoutcomes"))
    ))
  )

  val grade1: PYPVuosiluokanSuoritus = pypSuoritus("1", Some(date(2007, 6, 30))).copy(
    osasuoritukset = Some(List(
      oppiaineenSuoritus(oppiaine("LA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("MA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("FMT"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("PE"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("VA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("MU"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("ICT"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("LIB"), arviointi("achievedoutcomes"))
    ))
  )

  val grade2: PYPVuosiluokanSuoritus = pypSuoritus("2", Some(date(2008, 6, 30))).copy(
    osasuoritukset = Some(List(
      oppiaineenSuoritus(oppiaine("LA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("MA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("FMT"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("PE"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("VA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("MU"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("ICT"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("LIB"), arviointi("achievedoutcomes"))
    ))
  )

  val grade3: PYPVuosiluokanSuoritus = pypSuoritus("3", Some(date(2009, 6, 30))).copy(
    osasuoritukset = Some(List(
      oppiaineenSuoritus(oppiaine("LA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("MA"), arviointi("learningtoward")),
      oppiaineenSuoritus(oppiaine("FMT"), arviointi("learningtoward")),
      oppiaineenSuoritus(oppiaine("PE"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("VA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("MU"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("ICT"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("LIB"), arviointi("achievedoutcomes"))
    ))
  )

  val grade4: PYPVuosiluokanSuoritus = pypSuoritus("4", Some(date(2010, 6, 30))).copy(
    osasuoritukset = Some(List(
      oppiaineenSuoritus(oppiaine("LA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("MA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("FMT"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("PE"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("VA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("MU"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("ICT"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("LIB"), arviointi("achievedoutcomes"))
    ))
  )

  val grade5: PYPVuosiluokanSuoritus = pypSuoritus("5", Some(date(2011, 6, 30))).copy(
    osasuoritukset = Some(List(
      oppiaineenSuoritus(oppiaine("LA"), arviointi("learningtoward")),
      oppiaineenSuoritus(oppiaine("MA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("PE"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("VA"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("MU"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("ICT"), arviointi("achievedoutcomes")),
      oppiaineenSuoritus(oppiaine("LIB"), arviointi("achievedoutcomes"))
    ))
  )

  val grade6: MYPVuosiluokanSuoritus = mypSuoritus(6, Some(date(2012, 6, 30))).copy(
    osasuoritukset = Some(List(
      oppiaineenSuoritus(languageAndLiterature("EN"), arviointi(5)),
      oppiaineenSuoritus(languageAqcuisition("FI"), arviointi(6)),
      oppiaineenSuoritus(languageAqcuisition("FR"), arviointi(6)),
      oppiaineenSuoritus(oppiaine("IS"), arviointi(5)),
      oppiaineenSuoritus(oppiaine("SCI"), arviointi(7)),
      oppiaineenSuoritus(oppiaine("MA"), arviointi(5)),
      oppiaineenSuoritus(oppiaine("DR"), arviointi(7)),
      oppiaineenSuoritus(oppiaine("PHE"), arviointi(6))
    ))
  )

  val grade7: MYPVuosiluokanSuoritus = mypSuoritus(7, Some(date(2013, 6, 30))).copy(
    osasuoritukset = Some(List(
      oppiaineenSuoritus(languageAndLiterature("EN"), arviointi(6)),
      oppiaineenSuoritus(languageAqcuisition("ES"), arviointi(5)),
      oppiaineenSuoritus(languageAqcuisition("FI"), arviointi(6)),
      oppiaineenSuoritus(oppiaine("IS"), arviointi(5)),
      oppiaineenSuoritus(oppiaine("SCI"), arviointi(6)),
      oppiaineenSuoritus(oppiaine("MA"), arviointi(6)),
      oppiaineenSuoritus(oppiaine("MU"), arviointi(6)),
      oppiaineenSuoritus(oppiaine("PHE"), arviointi(6)),
      oppiaineenSuoritus(oppiaine("DD"), arviointi(4))
    ))
  )

  val grade8: MYPVuosiluokanSuoritus = mypSuoritus(8, Some(date(2014, 6, 30))).copy(
    osasuoritukset = Some(List(
      oppiaineenSuoritus(languageAndLiterature("FI"), arviointi(7)),
      oppiaineenSuoritus(languageAndLiterature("EN"), arviointi(6)),
      oppiaineenSuoritus(languageAqcuisition("ES"), arviointi(6)),
      oppiaineenSuoritus(oppiaine("IS"), arviointi(7)),
      oppiaineenSuoritus(oppiaine("SCI"), arviointi(6)),
      oppiaineenSuoritus(oppiaine("MA"), arviointi(7)),
      oppiaineenSuoritus(oppiaine("MU"), arviointi(7)),
      oppiaineenSuoritus(oppiaine("PHE"), arviointi(7)),
      oppiaineenSuoritus(oppiaine("DE"), arviointi(5))
    ))
  )

  val grade9: MYPVuosiluokanSuoritus = mypSuoritus(9, Some(date(2015, 6, 30))).copy(
    osasuoritukset = Some(List(
      oppiaineenSuoritus(languageAndLiterature("EN"), arviointi(6)),
      oppiaineenSuoritus(languageAqcuisition("FR"), arviointi(7)),
      oppiaineenSuoritus(oppiaine("IS"), arviointi(6)),
      oppiaineenSuoritus(oppiaine("SCI"), arviointi(5)),
      oppiaineenSuoritus(oppiaine("EMA"), arviointi(5)),
      oppiaineenSuoritus(oppiaine("PHE"), arviointi(6))
    ))
  )

  val grade10: MYPVuosiluokanSuoritus = mypSuoritus(10, Some(date(2016, 6, 30))).copy(
    osasuoritukset = Some(List(
      oppiaineenSuoritus(languageAndLiterature("EN"), arviointi(7)),
      oppiaineenSuoritus(languageAqcuisition("FR"), arviointi(7)),
      oppiaineenSuoritus(oppiaine("IS"), arviointi(7)),
      oppiaineenSuoritus(oppiaine("SCI"), arviointi(7)),
      oppiaineenSuoritus(oppiaine("EMA"), arviointi(5)),
      oppiaineenSuoritus(oppiaine("MU"), arviointi(7)),
      oppiaineenSuoritus(oppiaine("PHE"), arviointi(6))
    ))
  )

  val grade11: DiplomaVuosiluokanSuoritus = diplomaSuoritus(11, Some(date(2017, 6, 30))).copy(
    osasuoritukset = Some(List(
      diplomaOppiaineenSuoritus(diplomaKieliOppiaine("A", "FI", Some("SL")), diplomaArviointi(5)),
      diplomaOppiaineenSuoritus(diplomaKieliOppiaine("A2", "EN"), diplomaArviointi(5)),
      diplomaOppiaineenSuoritus(diplomaOppiaine("HIS", Some("HL")), diplomaArviointi(4)),
      diplomaOppiaineenSuoritus(diplomaOppiaine("PSY"), diplomaArviointi(3)),
      diplomaOppiaineenSuoritus(diplomaOppiaine("ESS", Some("SL")), diplomaArviointi(3)),
      diplomaOppiaineenSuoritus(diplomaOppiaine("MATST"), diplomaArviointi(4)),
      diplomaTOKSuoritus(tokArvionti("D"))
    ))
  )

  val grade12: DiplomaVuosiluokanSuoritus = diplomaSuoritus(12, Some(date(2018, 6, 30))).copy(
    osasuoritukset = Some(List(
      diplomaOppiaineenSuoritus(diplomaKieliOppiaine("A", "FI", Some("SL")), diplomaArviointi(6)),
      diplomaOppiaineenSuoritus(diplomaKieliOppiaine("B", "FI"), diplomaArviointi(6)),
      diplomaOppiaineenSuoritus(diplomaOppiaine("PSY", Some("HL")), diplomaArviointi(6)),
      diplomaOppiaineenSuoritus(diplomaOppiaine("CHE", Some("HL")), diplomaArviointi(4)),
      diplomaOppiaineenSuoritus(diplomaOppiaine("BIO", Some("HL")), diplomaArviointi(5)),
      diplomaOppiaineenSuoritus(diplomaOppiaine("MAT", Some("SL")), diplomaArviointi(4)),
      diplomaTOKSuoritus(tokArvionti("C"))
    ))
  )

  val lisätiedot = InternationalSchoolOpiskeluoikeudenLisätiedot(
    erityisenKoulutustehtävänJaksot = Some(List(ExamplesLukio.erityisenKoulutustehtävänJakso)),
    ulkomaanjaksot = Some(List(ExamplesLukio.ulkomaanjakso))
  )

  val opiskeluoikeus = InternationalSchoolOpiskeluoikeus(
    lisätiedot = Some(lisätiedot),
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(date(2006, 8, 15), LukioExampleData.opiskeluoikeusAktiivinen),
        LukionOpiskeluoikeusjakso(date(2018, 6, 30), LukioExampleData.opiskeluoikeusPäättynyt)
      )
    ),
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8, grade9, grade10, grade11, grade12)
  )

  val examples = List(
    Example("internationalschool", "International School of Helsinki", Oppija(asUusiOppija(MockOppijat.internationalschool), List(opiskeluoikeus)))
  )
}
