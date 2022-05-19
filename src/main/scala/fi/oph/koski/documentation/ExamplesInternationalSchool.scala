package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.InternationalSchoolExampleData._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.schema._

object ExamplesInternationalSchool {

  val gradeExplorer: PYPVuosiluokanSuoritus = pypSuoritus("explorer", date(2005, 8, 15), Some(date(2006, 6, 30))).copy(
    osasuoritukset = Some(List(
      pypOppiaineenSuoritus(pypOppiaine("LA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("MA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("PE"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("VA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("MU"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("ICT"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("LIB"), arviointi("achievedoutcomes"))
    ))
  )

  val grade1: PYPVuosiluokanSuoritus = pypSuoritus("1", date(2006, 8, 15), Some(date(2007, 6, 30))).copy(
    osasuoritukset = Some(List(
      pypOppiaineenSuoritus(pypOppiaine("LA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("MA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("FMT"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("PE"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("VA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("MU"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("ICT"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("LIB"), arviointi("achievedoutcomes"))
    ))
  )

  val grade2: PYPVuosiluokanSuoritus = pypSuoritus("2", date(2007, 8, 15), Some(date(2008, 6, 30))).copy(
    osasuoritukset = Some(List(
      pypOppiaineenSuoritus(pypOppiaine("LA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("MA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("FMT"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("PE"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("VA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("MU"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("ICT"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("LIB"), arviointi("achievedoutcomes"))
    ))
  )

  val grade3: PYPVuosiluokanSuoritus = pypSuoritus("3", date(2008, 8, 15), Some(date(2009, 6, 30))).copy(
    osasuoritukset = Some(List(
      pypOppiaineenSuoritus(pypOppiaine("LA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("MA"), arviointi("learningtoward")),
      pypOppiaineenSuoritus(pypOppiaine("FMT"), arviointi("learningtoward")),
      pypOppiaineenSuoritus(pypOppiaine("PE"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("VA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("MU"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("ICT"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("LIB"), arviointi("achievedoutcomes"))
    ))
  )

  val grade4: PYPVuosiluokanSuoritus = pypSuoritus("4", date(2009, 8, 15), Some(date(2010, 6, 30))).copy(
    osasuoritukset = Some(List(
      pypOppiaineenSuoritus(pypOppiaine("LA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("MA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("FMT"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("PE"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("VA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("MU"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("ICT"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("LIB"), arviointi("achievedoutcomes"))
    ))
  )

  val grade5: PYPVuosiluokanSuoritus = pypSuoritus("5", date(2010, 8, 15), Some(date(2011, 6, 30))).copy(
    osasuoritukset = Some(List(
      pypOppiaineenSuoritus(pypOppiaine("LA"), arviointi("learningtoward")),
      pypOppiaineenSuoritus(pypOppiaine("MA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("PE"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("VA"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("MU"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("ICT"), arviointi("achievedoutcomes")),
      pypOppiaineenSuoritus(pypOppiaine("LIB"), arviointi("achievedoutcomes"))
    ))
  )

  val grade6: MYPVuosiluokanSuoritus = mypSuoritus(6, date(2011, 8, 15), Some(date(2012, 6, 30))).copy(
    osasuoritukset = Some(List(
      mypOppiaineenSuoritus(languageAndLiterature("EN"), arviointi(5)),
      mypOppiaineenSuoritus(languageAqcuisition("FI"), arviointi(6)),
      mypOppiaineenSuoritus(languageAqcuisition("FR"), arviointi(6)),
      mypOppiaineenSuoritus(mypOppiaine("IS"), arviointi(5)),
      mypOppiaineenSuoritus(mypOppiaine("SCI"), arviointi(7)),
      mypOppiaineenSuoritus(mypOppiaine("MA"), arviointi(5)),
      mypOppiaineenSuoritus(mypOppiaine("DR"), arviointi(7)),
      mypOppiaineenSuoritus(mypOppiaine("PHE"), arviointi(6))
    ))
  )

  val grade7: MYPVuosiluokanSuoritus = mypSuoritus(7, date(2012, 8, 15), Some(date(2013, 6, 30))).copy(
    osasuoritukset = Some(List(
      mypOppiaineenSuoritus(languageAndLiterature("EN"), arviointi(6)),
      mypOppiaineenSuoritus(languageAqcuisition("ES"), arviointi(5)),
      mypOppiaineenSuoritus(languageAqcuisition("FI"), arviointi(6)),
      mypOppiaineenSuoritus(mypOppiaine("IS"), arviointi(5)),
      mypOppiaineenSuoritus(mypOppiaine("SCI"), arviointi(6)),
      mypOppiaineenSuoritus(mypOppiaine("MA"), arviointi(6)),
      mypOppiaineenSuoritus(mypOppiaine("MU"), arviointi(6)),
      mypOppiaineenSuoritus(mypOppiaine("PHE"), arviointi(6)),
      mypOppiaineenSuoritus(mypOppiaine("DE"), arviointi(4))
    ))
  )

  val grade8: MYPVuosiluokanSuoritus = mypSuoritus(8, date(2013, 8, 15), Some(date(2014, 6, 30))).copy(
    osasuoritukset = Some(List(
      mypOppiaineenSuoritus(languageAndLiterature("FI"), arviointi(7)),
      mypOppiaineenSuoritus(languageAndLiterature("EN"), arviointi(6)),
      mypOppiaineenSuoritus(languageAqcuisition("ES"), arviointi(6)),
      mypOppiaineenSuoritus(mypOppiaine("IS"), arviointi(7)),
      mypOppiaineenSuoritus(mypOppiaine("SCI"), arviointi(6)),
      mypOppiaineenSuoritus(mypOppiaine("MA"), arviointi(7)),
      mypOppiaineenSuoritus(mypOppiaine("MU"), arviointi(7)),
      mypOppiaineenSuoritus(mypOppiaine("PHE"), arviointi(7)),
      mypOppiaineenSuoritus(mypOppiaine("DE"), arviointi(5))
    ))
  )

  val grade9: MYPVuosiluokanSuoritus = mypSuoritus(9, date(2014, 8, 15), Some(date(2015, 6, 30))).copy(
    osasuoritukset = Some(List(
      mypOppiaineenSuoritus(languageAndLiterature("EN"), arviointi(6)),
      mypOppiaineenSuoritus(languageAqcuisition("FR"), arviointi(7)),
      mypOppiaineenSuoritus(mypOppiaine("IS"), arviointi(6)),
      mypOppiaineenSuoritus(mypOppiaine("SCI"), arviointi(5)),
      mypOppiaineenSuoritus(mypOppiaine("EMA"), arviointi(5)),
      mypOppiaineenSuoritus(mypOppiaine("PHE"), arviointi(6))
    ))
  )

  val grade10: MYPVuosiluokanSuoritus = mypSuoritus(10, date(2015, 8, 15), Some(date(2016, 6, 30))).copy(
    osasuoritukset = Some(List(
      mypOppiaineenSuoritus(languageAndLiterature("EN"), arviointi(7)),
      mypOppiaineenSuoritus(languageAqcuisition("FR"), arviointi(7)),
      mypOppiaineenSuoritus(mypOppiaine("IS"), arviointi(7)),
      mypOppiaineenSuoritus(mypOppiaine("SCI"), arviointi(7)),
      mypOppiaineenSuoritus(mypOppiaine("EMA"), arviointi(5)),
      mypOppiaineenSuoritus(mypOppiaine("MU"), arviointi(7)),
      mypOppiaineenSuoritus(mypOppiaine("PHE"), arviointi(6))
    ))
  )

  val grade11: DiplomaVuosiluokanSuoritus = diplomaSuoritus(11, date(2016, 8, 15), Some(date(2017, 6, 30))).copy(
    osasuoritukset = Some(List(
      diplomaOppiaineenSuoritus(diplomaKieliOppiaine("A", "FI", Some("SL")), diplomaArviointi(5)),
      diplomaOppiaineenSuoritus(diplomaKieliOppiaine("A2", "EN"), diplomaArviointi(5)),
      diplomaOppiaineenSuoritus(diplomaIBOppiaine("HIS", Some("HL")), diplomaArviointi(4)),
      diplomaOppiaineenSuoritus(diplomaIBOppiaine("PSY"), diplomaArviointi(3)),
      diplomaOppiaineenSuoritus(diplomaIBOppiaine("ESS", Some("SL")), diplomaArviointi(3)),
      diplomaOppiaineenSuoritus(diplomaIBOppiaine("MATST"), diplomaArviointi("S")),
      diplomaOppiaineenSuoritus(FitnessAndWellBeing(Koodistokoodiviite("HAWB", "oppiaineetinternationalschool")), Some(PassFailOppiaineenArviointi(Koodistokoodiviite("pass", "arviointiasteikkointernationalschool")))),
      diplomaTOKSuoritus(tokArvionti("D"))
    ))
  )

  val grade12: DiplomaVuosiluokanSuoritus = diplomaSuoritus(12, date(2017, 8, 15), Some(date(2018, 6, 30))).copy(
    osasuoritukset = Some(List(
      diplomaOppiaineenSuoritus(diplomaKieliOppiaine("A", "FI", Some("SL")), diplomaArviointi(6)),
      diplomaOppiaineenSuoritus(diplomaKieliOppiaine("B", "FI"), diplomaArviointi(6)),
      diplomaOppiaineenSuoritus(diplomaIBOppiaine("PSY", Some("HL")), diplomaArviointi(6)),
      diplomaOppiaineenSuoritus(diplomaIBOppiaine("CHE", Some("HL")), diplomaArviointi(4)),
      diplomaOppiaineenSuoritus(diplomaIBOppiaine("BIO", Some("HL")), diplomaArviointi(5)),
      diplomaOppiaineenSuoritus(diplomaInternationalSchoolOppiaine("MAI", Some("SL")), diplomaArviointi(4)),
      diplomaOppiaineenSuoritus(FitnessAndWellBeing(Koodistokoodiviite("HAWB", "oppiaineetinternationalschool")), Some(PassFailOppiaineenArviointi(Koodistokoodiviite("fail", "arviointiasteikkointernationalschool")))),
      diplomaTOKSuoritus(tokArvionti("C"))
    ))
  )

  val lisätiedot = InternationalSchoolOpiskeluoikeudenLisätiedot(
    erityisenKoulutustehtävänJaksot = Some(List(ExamplesLukio.erityisenKoulutustehtävänJakso)),
    ulkomaanjaksot = Some(List(ExamplesLukio.ulkomaanjakso))
  )

  val opiskeluoikeus = InternationalSchoolOpiskeluoikeus(
    oppilaitos = Some(internationalSchoolOfHelsinki),
    lisätiedot = Some(lisätiedot),
    tila = InternationalSchoolOpiskeluoikeudenTila(
      List(
        InternationalSchoolOpiskeluoikeusjakso(date(2004, 8, 15), LukioExampleData.opiskeluoikeusAktiivinen),
        InternationalSchoolOpiskeluoikeusjakso(date(2018, 6, 30), LukioExampleData.opiskeluoikeusPäättynyt)
      )
    ),
    suoritukset = List(gradeExplorer, grade1, grade2, grade3, grade4, grade5, grade6, grade7, grade8, grade9, grade10, grade11, grade12)
  )

  val examples = List(
    Example("internationalschool", "International School of Helsinki", Oppija(asUusiOppija(KoskiSpecificMockOppijat.internationalschool), List(opiskeluoikeus)))
  )
}
