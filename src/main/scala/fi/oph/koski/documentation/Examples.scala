package fi.oph.koski.documentation

import fi.oph.koski.henkilo.HenkilötiedotHetuRequest
import fi.oph.koski.tiedonsiirto.ExamplesTiedonsiirto

object Examples {
  val oppijaExamples: List[Example] = ExamplesAmmatillinen.examples ++ ExamplesMuuAmmatillinen.examples ++ ExamplesValma.examples ++ ExamplesTelma.examples ++
    ExamplesPerusopetukseenValmistavaOpetus.examples ++ ExamplesEsiopetus.examples ++ ExamplesPerusopetus.examples ++ ExamplesAikuistenPerusopetus.examples ++
    ExamplesPerusopetuksenLisaopetus.examples ++ ExamplesLukioonValmistavaKoulutus.examples ++
    ExamplesLukio.examples ++ ExamplesLukio2019.examples ++ ExamplesYlioppilastutkinto.examples ++
    ExamplesKorkeakoulu.examples ++ ExamplesTiedonsiirto.examples ++ ExamplesIB.examples ++ ExamplesDIA.examples ++ ExamplesInternationalSchool.examples ++
    ExamplesVapaaSivistystyö.examples ++ ExamplesTutkintokoulutukseenValmentavaKoulutus.examples ++
    ExamplesVapaaSivistystyöKotoutuskoulutus2022.Examples.examples ++ ExamplesEuropeanSchoolOfHelsinki.examples

  val searchByHetuExamples: List[ExampleHetuSearch] = List(
    ExampleHetuSearch("010101-123N", "Henkilötunnus", HenkilötiedotHetuRequest("010101-123N")),
  )
}
