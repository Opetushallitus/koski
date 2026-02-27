package fi.oph.koski.kios

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

object KiosKorkeakoulunOpiskeluoikeus {
  def fromKoskiSchema(kk: schema.KorkeakoulunOpiskeluoikeus) = KiosKorkeakoulunOpiskeluoikeus(
    oppilaitos = kk.oppilaitos.map(ol =>
      Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(KiosKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(KiosKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = kk.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(KiosKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    päättymispäivä = kk.päättymispäivä,
    tila = KiosOpiskeluoikeudenTila(
      kk.tila.opiskeluoikeusjaksot.map(kkt =>
        KiosOpiskeluoikeusjakso(
          kkt.alku,
          KiosKoodistokoodiviite.fromKoskiSchema(kkt.tila),
          None
        )
      )
    ),
    lisätiedot = kk.lisätiedot.map(lisätiedot => KiosKorkeakoulunOpiskeluoikeudenLisätiedot(
      virtaOpiskeluoikeudenTyyppi = lisätiedot.virtaOpiskeluoikeudenTyyppi.map(KiosKoodistokoodiviite.fromKoskiSchema),
      lukukausiIlmoittautuminen = lisätiedot.lukukausiIlmoittautuminen.map(kkl =>
        KiosLukukausi_Ilmoittautuminen(
          kkl.ilmoittautumisjaksot.map(kkilj =>
            KiosLukukausi_Ilmoittautumisjakso(
              alku = kkilj.alku,
              loppu = kkilj.loppu,
              tila = KiosKoodistokoodiviite.fromKoskiSchema(kkilj.tila),
              ylioppilaskunnanJäsen = kkilj.ylioppilaskunnanJäsen,
              maksetutLukuvuosimaksut = kkilj.maksetutLukuvuosimaksut.map(kklvm =>
                KiosLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
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
          KiosKorkeakoulututkinnonSuoritus(
            koulutusmoduuli = KiosKorkeakoulututkinto(
              tunniste = KiosKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste),
              koulutustyyppi = s.koulutusmoduuli.koulutustyyppi.map(KiosKoodistokoodiviite.fromKoskiSchema),
              virtaNimi = s.koulutusmoduuli.virtaNimi
            ),
            vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
            toimipiste = Some(Toimipiste(
              s.toimipiste.oid,
              s.toimipiste.nimi,
              s.toimipiste.kotipaikka.map(KiosKoodistokoodiviite.fromKoskiSchema)
            )),
            tyyppi = s.tyyppi
          )
      },
    tyyppi = kk.tyyppi,
    luokittelu = kk.luokittelu.map(_.map(KiosKoodistokoodiviite.fromKoskiSchema))
  )
}

@Title("Korkeakoulun opiskeluoikeus")
case class KiosKorkeakoulunOpiskeluoikeus(
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  override val päättymispäivä: Option[LocalDate],
  tila: KiosOpiskeluoikeudenTila,
  lisätiedot: Option[KiosKorkeakoulunOpiskeluoikeudenLisätiedot],
  suoritukset: List[KiosKorkeakoulututkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  luokittelu: Option[List[KiosKoodistokoodiviite]],
) extends KiosOpiskeluoikeus {

  override def withSuoritukset(suoritukset: List[Suoritus]): KiosOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: KiosKorkeakoulututkinnonSuoritus => s }
    )
}

@Title("Korkeakoulututkinnon päätason suoritus")
case class KiosKorkeakoulututkinnonSuoritus(
  koulutusmoduuli: KiosKorkeakoulututkinto,
  @KoodistoKoodiarvo("korkeakoulututkinto")
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  toimipiste: Option[Toimipiste],
) extends Suoritus

case class KiosKorkeakoulunOpiskeluoikeudenLisätiedot(
  virtaOpiskeluoikeudenTyyppi: Option[KiosKoodistokoodiviite],
  lukukausiIlmoittautuminen: Option[KiosLukukausi_Ilmoittautuminen],
) extends KiosOpiskeluoikeudenLisätiedot

@Title("Lukukausi-ilmoittautuminen")
case class KiosLukukausi_Ilmoittautuminen(
  ilmoittautumisjaksot: List[KiosLukukausi_Ilmoittautumisjakso]
)

@Title("Lukukausi-ilmoittautumisjakso")
case class KiosLukukausi_Ilmoittautumisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tila: KiosKoodistokoodiviite,
  ylioppilaskunnanJäsen: Option[Boolean],
  maksetutLukuvuosimaksut: Option[KiosLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu]
)

@Title("Lukukausi-ilmoittautumisjakson lukuvuosimaksutiedot")
case class KiosLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
  maksettu: Option[Boolean],
  summa: Option[Int],
  apuraha: Option[Int]
)

@Title("Muu korkeakoulun opinto")
case class KiosMuuKorkeakoulunOpinto(
  tunniste: KiosKoodistokoodiviite,
  nimi: schema.LocalizedString,
) extends SuorituksenKoulutusmoduuli

@Title("Korkeakoulututkinto")
case class KiosKorkeakoulututkinto(
  tunniste: KiosKoodistokoodiviite,
  koulutustyyppi: Option[KiosKoodistokoodiviite],
  virtaNimi: Option[schema.LocalizedString]
) extends SuorituksenKoulutusmoduuli

@Title("Korkeakoulun opintojakso")
case class KiosKorkeakoulunOpintojakso(
  tunniste: KiosPaikallinenKoodi,
  nimi: schema.LocalizedString,
) extends SuorituksenKoulutusmoduuli
