package fi.oph.koski.documentation

import fi.oph.koski.tiedonsiirto.ExamplesTiedonsiirto

object Examples {
  val examples: List[Example] = ExamplesAmmatillinen.examples ++ ExamplesMuuAmmatillinen.examples ++ ExamplesValma.examples ++ ExamplesTelma.examples ++
    ExamplesPerusopetukseenValmistavaOpetus.examples ++ ExamplesEsiopetus.examples ++ ExamplesPerusopetus.examples ++ ExamplesAikuistenPerusopetus.examples ++
    ExamplesPerusopetuksenLisaopetus.examples ++ ExamplesLukioonValmistavaKoulutus.examples ++
    ExamplesLukio.examples ++ ExamplesLukio2019.examples ++ ExamplesYlioppilastutkinto.examples ++
    ExamplesKorkeakoulu.examples ++ ExamplesTiedonsiirto.examples ++ ExamplesIB.examples ++ ExamplesDIA.examples ++ ExamplesInternationalSchool.examples ++
    ExamplesVapaaSivistystyö.examples ++ ExamplesTutkintokoulutukseenValmentavaKoulutus.examples ++
    ExamplesVapaaSivistystyöKotoutuskoulutus2022.Examples.examples

  val allExamples = examples
}
