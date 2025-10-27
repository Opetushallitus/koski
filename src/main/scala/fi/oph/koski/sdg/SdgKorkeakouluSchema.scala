package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

object KorkeakoulunOpiskeluoikeus {
  def fromKoskiSchema(kk: schema.KorkeakoulunOpiskeluoikeus) = KorkeakoulunOpiskeluoikeus(
    oppilaitos = kk.oppilaitos,
    koulutustoimija = kk.koulutustoimija,
    päättymispäivä = kk.päättymispäivä,
    tila = KorkeakoulunOpiskeluoikeudenTila(
      opiskeluoikeusjaksot = kk.tila.opiskeluoikeusjaksot.map(j => KorkeakoulunOpiskeluoikeusjakso(
        tila = j.tila,
        alku = j.alku
      ))
    ),
    lisätiedot = kk.lisätiedot.map(lisätiedot => KorkeakoulunOpiskeluoikeudenLisätiedot(
      virtaOpiskeluoikeudenTyyppi = lisätiedot.virtaOpiskeluoikeudenTyyppi,
      lukukausiIlmoittautuminen = lisätiedot.lukukausiIlmoittautuminen.map(kkl =>
        Lukukausi_Ilmoittautuminen(
          kkl.ilmoittautumisjaksot.map(kkilj =>
            Lukukausi_Ilmoittautumisjakso(
              alku = kkilj.alku,
              loppu = kkilj.loppu,
              tila = kkilj.tila,
              ylioppilaskunnanJäsen = kkilj.ylioppilaskunnanJäsen,
              maksetutLukuvuosimaksut = kkilj.maksetutLukuvuosimaksut.map(kklvm =>
                Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
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
          KorkeakoulututkinnonSuoritus(
            koulutusmoduuli = s.koulutusmoduuli,
            vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
            toimipiste = Some(Toimipiste(
              s.toimipiste.oid,
              s.toimipiste.nimi,
              s.toimipiste.kotipaikka
            )),
            tyyppi = s.tyyppi,
            osasuoritukset = s.osasuoritukset.map(_.map(KorkeakoulunOpintojaksonSuoritus.fromKoskiSchema))
          )
      },
    tyyppi = kk.tyyppi,
  )
}

@Title("Korkeakoulun opiskeluoikeus")
case class KorkeakoulunOpiskeluoikeus(
  oppilaitos: Option[schema.Oppilaitos],
  koulutustoimija: Option[schema.Koulutustoimija],
  override val päättymispäivä: Option[LocalDate],
  tila: KorkeakoulunOpiskeluoikeudenTila,
  lisätiedot: Option[KorkeakoulunOpiskeluoikeudenLisätiedot],
  suoritukset: List[KorkeakoulututkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends Opiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): Opiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: KorkeakoulututkinnonSuoritus => s }
    )
}

case class KorkeakoulunOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KorkeakoulunOpiskeluoikeusjakso]
) extends GenericOpiskeluoikeudenTila

case class KorkeakoulunOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoUri("virtaopiskeluoikeudentila")
  tila: schema.Koodistokoodiviite
) extends GenericOpiskeluoikeusjakso {
  def opiskeluoikeusPäättynyt: Boolean =
    schema.Opiskeluoikeus.OpiskeluoikeudenPäättymistila.korkeakoulu(tila.koodiarvo)
}

@Title("Korkeakoulututkinnon suoritus")
case class KorkeakoulututkinnonSuoritus(
  koulutusmoduuli: schema.Korkeakoulututkinto,
  @KoodistoKoodiarvo("korkeakoulututkinto")
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
  osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]]
) extends Suoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): KorkeakoulututkinnonSuoritus =
    this.copy(osasuoritukset = os.map(_.collect {
      case s: KorkeakoulunOpintojaksonSuoritus => s
    }))
}

case class KorkeakoulunOpiskeluoikeudenLisätiedot(
  virtaOpiskeluoikeudenTyyppi: Option[schema.Koodistokoodiviite],
  lukukausiIlmoittautuminen: Option[Lukukausi_Ilmoittautuminen],
) extends SdgOpiskeluoikeudenLisätiedot

@Title("Lukukausi-ilmoittautuminen")
case class Lukukausi_Ilmoittautuminen(
  ilmoittautumisjaksot: List[Lukukausi_Ilmoittautumisjakso]
)

@Title("Lukukausi-ilmoittautumisjakso")
case class Lukukausi_Ilmoittautumisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tila: schema.Koodistokoodiviite,
  ylioppilaskunnanJäsen: Option[Boolean],
  maksetutLukuvuosimaksut: Option[Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu]
)

@Title("Lukukausi-ilmoittautumisjakson lukuvuosimaksutiedot")
case class Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
  maksettu: Option[Boolean],
  summa: Option[Int],
  apuraha: Option[Int]
)

@Title("Korkeakoulututkinto")
case class Korkeakoulututkinto(
  tunniste: schema.Koodistokoodiviite,
  koulutustyyppi: Option[schema.Koodistokoodiviite],
  virtaNimi: Option[schema.LocalizedString]
) extends SuorituksenKoulutusmoduuli

@Title("Korkeakoulun opintojakson suoritus")
case class KorkeakoulunOpintojaksonSuoritus(
  @Title("Opintojakso")
  koulutusmoduuli: schema.KorkeakoulunOpintojakso,
  toimipiste: schema.Oppilaitos,
  arviointi: Option[List[schema.KorkeakoulunArviointi]],
  suorituskieli: Option[schema.Koodistokoodiviite],
  @Title("Sisältyvät opintojaksot")
  osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]] = None,
  @KoodistoKoodiarvo("korkeakoulunopintojakso")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus

object KorkeakoulunOpintojaksonSuoritus {
  def fromKoskiSchema(k: schema.KorkeakoulunOpintojaksonSuoritus): KorkeakoulunOpintojaksonSuoritus =
    KorkeakoulunOpintojaksonSuoritus(
      koulutusmoduuli = k.koulutusmoduuli,
      toimipiste = k.toimipiste,
      arviointi = k.arviointi,
      suorituskieli = k.suorituskieli,
      osasuoritukset = k.osasuoritukset.map(_.map(fromKoskiSchema)),
      tyyppi = k.tyyppi
    )
}
