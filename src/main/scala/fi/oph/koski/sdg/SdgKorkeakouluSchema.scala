package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

object SdgKorkeakoulunOpiskeluoikeus {
  def fromKoskiSchema(kk: schema.KorkeakoulunOpiskeluoikeus) = SdgKorkeakoulunOpiskeluoikeus(
    oppilaitos = kk.oppilaitos,
    koulutustoimija = kk.koulutustoimija,
    päättymispäivä = kk.päättymispäivä,
    tila = SdgKorkeakoulunOpiskeluoikeudenTila(
      opiskeluoikeusjaksot = kk.tila.opiskeluoikeusjaksot.map(j => SdgKorkeakoulunOpiskeluoikeusjakso(
        tila = j.tila,
        alku = j.alku
      ))
    ),
    lisätiedot = kk.lisätiedot.map(lisätiedot => SdgKorkeakoulunOpiskeluoikeudenLisätiedot(
      virtaOpiskeluoikeudenTyyppi = lisätiedot.virtaOpiskeluoikeudenTyyppi,
      lukukausiIlmoittautuminen = lisätiedot.lukukausiIlmoittautuminen.map(kkl =>
        SdgLukukausi_Ilmoittautuminen(
          kkl.ilmoittautumisjaksot.map(kkilj =>
            SdgLukukausi_Ilmoittautumisjakso(
              alku = kkilj.alku,
              loppu = kkilj.loppu,
              tila = kkilj.tila,
              ylioppilaskunnanJäsen = kkilj.ylioppilaskunnanJäsen,
              maksetutLukuvuosimaksut = kkilj.maksetutLukuvuosimaksut.map(kklvm =>
                SdgLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
                  kklvm.maksettu,
                  kklvm.summa,
                  kklvm.apuraha
                )
              )
            )
          )
        )
      )
    )),
    suoritukset = kk.suoritukset
      .collect {
        case s: schema.KorkeakoulututkinnonSuoritus =>
          SdgKorkeakoulututkinnonSuoritus(
            koulutusmoduuli = s.koulutusmoduuli,
            vahvistus = s.vahvistus.map(v => SdgVahvistus(v.päivä)),
            toimipiste = Some(SdgToimipiste(
              s.toimipiste.oid,
              s.toimipiste.nimi,
              s.toimipiste.kotipaikka
            )),
            tyyppi = s.tyyppi,
            osasuoritukset = s.osasuoritukset.map(_.map(SdgKorkeakoulunOpintojaksonSuoritus.fromKoskiSchema))
          )
      },
    tyyppi = kk.tyyppi,
  )
}

@Title("Korkeakoulun opiskeluoikeus")
case class SdgKorkeakoulunOpiskeluoikeus(
  oppilaitos: Option[schema.Oppilaitos],
  koulutustoimija: Option[schema.Koulutustoimija],
  override val päättymispäivä: Option[LocalDate],
  tila: SdgKorkeakoulunOpiskeluoikeudenTila,
  lisätiedot: Option[SdgKorkeakoulunOpiskeluoikeudenLisätiedot],
  suoritukset: List[SdgKorkeakoulututkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends Opiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): Opiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: SdgKorkeakoulututkinnonSuoritus => s }
    )
}

case class SdgKorkeakoulunOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[SdgKorkeakoulunOpiskeluoikeusjakso]
) extends GenericOpiskeluoikeudenTila

case class SdgKorkeakoulunOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoUri("virtaopiskeluoikeudentila")
  tila: schema.Koodistokoodiviite
) extends GenericOpiskeluoikeusjakso {
  def opiskeluoikeusPäättynyt: Boolean =
    schema.Opiskeluoikeus.OpiskeluoikeudenPäättymistila.korkeakoulu(tila.koodiarvo)
}

@Title("Korkeakoulututkinnon suoritus")
case class SdgKorkeakoulututkinnonSuoritus(
  koulutusmoduuli: schema.Korkeakoulututkinto,
  @KoodistoKoodiarvo("korkeakoulututkinto")
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[SdgVahvistus],
  toimipiste: Option[SdgToimipiste],
  osasuoritukset: Option[List[SdgKorkeakoulunOpintojaksonSuoritus]]
) extends Suoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgKorkeakoulututkinnonSuoritus =
    this.copy(osasuoritukset = os.map(_.collect {
      case s: SdgKorkeakoulunOpintojaksonSuoritus => s
    }))
}

case class SdgKorkeakoulunOpiskeluoikeudenLisätiedot(
  virtaOpiskeluoikeudenTyyppi: Option[schema.Koodistokoodiviite],
  lukukausiIlmoittautuminen: Option[SdgLukukausi_Ilmoittautuminen],
) extends SdgOpiskeluoikeudenLisätiedot

@Title("Lukukausi-ilmoittautuminen")
case class SdgLukukausi_Ilmoittautuminen(
  ilmoittautumisjaksot: List[SdgLukukausi_Ilmoittautumisjakso]
)

@Title("Lukukausi-ilmoittautumisjakso")
case class SdgLukukausi_Ilmoittautumisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tila: schema.Koodistokoodiviite,
  ylioppilaskunnanJäsen: Option[Boolean],
  maksetutLukuvuosimaksut: Option[SdgLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu]
)

@Title("Lukukausi-ilmoittautumisjakson lukuvuosimaksutiedot")
case class SdgLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
  maksettu: Option[Boolean],
  summa: Option[Int],
  apuraha: Option[Int]
)

@Title("Korkeakoulututkinto")
case class SdgKorkeakoulututkinto(
  tunniste: schema.Koodistokoodiviite,
  koulutustyyppi: Option[schema.Koodistokoodiviite],
  virtaNimi: Option[schema.LocalizedString]
) extends SuorituksenKoulutusmoduuli

@Title("Korkeakoulun opintojakson suoritus")
case class SdgKorkeakoulunOpintojaksonSuoritus(
  @Title("Opintojakso")
  koulutusmoduuli: schema.KorkeakoulunOpintojakso,
  toimipiste: schema.Oppilaitos,
  arviointi: Option[List[schema.KorkeakoulunArviointi]],
  suorituskieli: Option[schema.Koodistokoodiviite],
  @Title("Sisältyvät opintojaksot")
  osasuoritukset: Option[List[SdgKorkeakoulunOpintojaksonSuoritus]] = None,
  @KoodistoKoodiarvo("korkeakoulunopintojakso")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus

object SdgKorkeakoulunOpintojaksonSuoritus {
  def fromKoskiSchema(k: schema.KorkeakoulunOpintojaksonSuoritus): SdgKorkeakoulunOpintojaksonSuoritus =
    SdgKorkeakoulunOpintojaksonSuoritus(
      koulutusmoduuli = k.koulutusmoduuli,
      toimipiste = k.toimipiste,
      arviointi = k.arviointi,
      suorituskieli = k.suorituskieli,
      osasuoritukset = k.osasuoritukset.map(_.map(fromKoskiSchema)),
      tyyppi = k.tyyppi
    )
}
