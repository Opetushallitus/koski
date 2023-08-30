package fi.oph.koski.api.misc

import fi.oph.koski.documentation.KorkeakouluTestdata
import fi.oph.koski.schema._

import java.time.LocalDate.{of => date}

trait OpiskeluoikeusTestMethodsKorkeakoulu extends PutOpiskeluoikeusTestMethods[KorkeakoulunOpiskeluoikeus]{
  def tag = implicitly[reflect.runtime.universe.TypeTag[KorkeakoulunOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = KorkeakoulunOpiskeluoikeus(
    lähdejärjestelmänId = None,
    arvioituPäättymispäivä = Some(date(2020, 5, 1)),
    oppilaitos = Some(KorkeakouluTestdata.helsinginYliopisto),
    suoritukset = Nil,
    tila = KorkeakoulunOpiskeluoikeudenTila(
      List(
        KorkeakoulunOpiskeluoikeusjakso(date(2012, 9, 1), nimi = Some(LocalizedString.missing), KorkeakouluTestdata.opiskeluoikeusAktiivinen)
      )
    ),
    tyyppi = koodisto.validateRequired(OpiskeluoikeudenTyyppi.korkeakoulutus),
    luokittelu = None
  )
}
