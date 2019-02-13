package fi.oph.koski.documentation

import fi.oph.koski.tiedonsiirto.ExamplesTiedonsiirto

object Examples {
  val examples: List[Example] = ExamplesAmmatillinen.examples ++ ExamplesMuuAmmatillinen.examples ++ ExamplesValma.examples ++ ExamplesTelma.examples ++
    ExamplesPerusopetukseenValmistavaOpetus.examples ++ ExamplesEsiopetus.examples ++ ExamplesPerusopetus.examples ++ ExamplesAikuistenPerusopetus.examples ++
    ExamplesPerusopetuksenLisaopetus.examples ++ ExamplesLukioonValmistavaKoulutus.examples ++
    ExamplesLukio.examples ++ ExamplesYlioppilastutkinto.examples ++
    ExamplesKorkeakoulu.examples ++ ExamplesTiedonsiirto.examples ++ ExamplesIB.examples

  val allExamples = examples
}
