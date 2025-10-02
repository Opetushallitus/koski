package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

object SdgKorkeakoulunOpiskeluoikeus {
  def fromKoskiSchema(kk: schema.KorkeakoulunOpiskeluoikeus) = SdgKorkeakoulunOpiskeluoikeus(
    oppilaitos = kk.oppilaitos.map(ol =>
      Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(SdgKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(SdgKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = kk.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(SdgKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    päättymispäivä = kk.päättymispäivä,
    tila = SdgOpiskeluoikeudenTila(
      kk.tila.opiskeluoikeusjaksot.map(kkt =>
        SdgOpiskeluoikeusjakso(
          kkt.alku,
          SdgKoodistokoodiviite.fromKoskiSchema(kkt.tila),
          None
        )
      )
    ),
    lisätiedot = kk.lisätiedot.map(lisätiedot => SdgKorkeakoulunOpiskeluoikeudenLisätiedot(
      virtaOpiskeluoikeudenTyyppi = lisätiedot.virtaOpiskeluoikeudenTyyppi.map(SdgKoodistokoodiviite.fromKoskiSchema),
      lukukausiIlmoittautuminen = lisätiedot.lukukausiIlmoittautuminen.map(kkl =>
        SdgLukukausi_Ilmoittautuminen(
          kkl.ilmoittautumisjaksot.map(kkilj =>
            SdgLukukausi_Ilmoittautumisjakso(
              alku = kkilj.alku,
              loppu = kkilj.loppu,
              tila = SdgKoodistokoodiviite.fromKoskiSchema(kkilj.tila),
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
            koulutusmoduuli = SdgKorkeakoulututkinto(
              tunniste = SdgKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste),
              koulutustyyppi = s.koulutusmoduuli.koulutustyyppi.map(SdgKoodistokoodiviite.fromKoskiSchema),
              virtaNimi = s.koulutusmoduuli.virtaNimi
            ),
            vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
            toimipiste = Some(Toimipiste(
              s.toimipiste.oid,
              s.toimipiste.nimi,
              s.toimipiste.kotipaikka.map(SdgKoodistokoodiviite.fromKoskiSchema)
            )),
            tyyppi = s.tyyppi
          )
      },
    tyyppi = kk.tyyppi,
    luokittelu = kk.luokittelu.map(_.map(SdgKoodistokoodiviite.fromKoskiSchema))
  )
}

@Title("Korkeakoulun opiskeluoikeus")
case class SdgKorkeakoulunOpiskeluoikeus(
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  override val päättymispäivä: Option[LocalDate],
  tila: SdgOpiskeluoikeudenTila,
  lisätiedot: Option[SdgKorkeakoulunOpiskeluoikeudenLisätiedot],
  suoritukset: List[SdgKorkeakoulututkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  luokittelu: Option[List[SdgKoodistokoodiviite]],
) extends SdgOpiskeluoikeus {

  override def withSuoritukset(suoritukset: List[Suoritus]): SdgOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: SdgKorkeakoulututkinnonSuoritus => s }
    )
}

@Title("Korkeakoulututkinnon päätason suoritus")
case class SdgKorkeakoulututkinnonSuoritus(
  koulutusmoduuli: SdgKorkeakoulututkinto,
  @KoodistoKoodiarvo("korkeakoulututkinto")
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
) extends Suoritus

case class SdgKorkeakoulunOpiskeluoikeudenLisätiedot(
  virtaOpiskeluoikeudenTyyppi: Option[SdgKoodistokoodiviite],
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
  tila: SdgKoodistokoodiviite,
  ylioppilaskunnanJäsen: Option[Boolean],
  maksetutLukuvuosimaksut: Option[SdgLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu]
)

@Title("Lukukausi-ilmoittautumisjakson lukuvuosimaksutiedot")
case class SdgLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
  maksettu: Option[Boolean],
  summa: Option[Int],
  apuraha: Option[Int]
)

@Title("Muu korkeakoulun opinto")
case class SdgMuuKorkeakoulunOpinto(
  tunniste: SdgKoodistokoodiviite,
  nimi: schema.LocalizedString,
) extends SuorituksenKoulutusmoduuli

@Title("Korkeakoulututkinto")
case class SdgKorkeakoulututkinto(
  tunniste: SdgKoodistokoodiviite,
  koulutustyyppi: Option[SdgKoodistokoodiviite],
  virtaNimi: Option[schema.LocalizedString]
) extends SuorituksenKoulutusmoduuli

@Title("Korkeakoulun opintojakso")
case class SdgKorkeakoulunOpintojakso(
  tunniste: SdgPaikallinenKoodi,
  nimi: schema.LocalizedString,
) extends SuorituksenKoulutusmoduuli
