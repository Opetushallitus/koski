package fi.oph.koski.api

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.KorkeakouluTestdata
import fi.oph.koski.schema._

trait OpiskeluoikeusTestMethodsKorkeakoulu extends PutOpiskeluOikeusTestMethods[KorkeakoulunOpiskeluoikeus]{
  override def defaultOpiskeluoikeus = KorkeakoulunOpiskeluoikeus(
    alkamispäivä = Some(date(2016, 9, 1)),
    arvioituPäättymispäivä = Some(date(2020, 5, 1)),
    oppilaitos = KorkeakouluTestdata.helsinginYliopisto,
    suoritukset = Nil,
    tila = KorkeakoulunOpiskeluoikeudenTila(
      List(
        KorkeakoulunOpiskeluoikeusjakso(date(2012, 9, 1), KorkeakouluTestdata.opiskeluoikeusAktiivinen)
      )
    )
  )
}
