package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.schema
import fi.oph.koski.schema.KorkeakoulunOpintojaksonSuoritus
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

object HakemuspalveluKorkeakoulunOpiskeluoikeus {
  def fromKoskiSchema(kk: schema.KorkeakoulunOpiskeluoikeus) = HakemuspalveluKorkeakoulunOpiskeluoikeus(
    oppilaitos = kk.oppilaitos.map(ol =>
      HakemuspalveluOppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(HakemuspalveluKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(HakemuspalveluKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = kk.koulutustoimija.map(kt =>
      HakemuspalveluKoulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(HakemuspalveluKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    päättymispäivä = kk.päättymispäivä,
    tila = HakemuspalveluOpiskeluoikeudenTila(
      kk.tila.opiskeluoikeusjaksot.map(kkt =>
        HakemuspalveluOpiskeluoikeusjakso(
          kkt.alku,
          HakemuspalveluKoodistokoodiviite.fromKoskiSchema(kkt.tila),
          None
        )
      )
    ),
    lisätiedot = kk.lisätiedot.map(lisätiedot => HakemuspalveluKorkeakoulunOpiskeluoikeudenLisätiedot(
      virtaOpiskeluoikeudenTyyppi = lisätiedot.virtaOpiskeluoikeudenTyyppi.map(HakemuspalveluKoodistokoodiviite.fromKoskiSchema),
      lukukausiIlmoittautuminen = lisätiedot.lukukausiIlmoittautuminen.map(kkl =>
        HakemuspalveluLukukausi_Ilmoittautuminen(
          kkl.ilmoittautumisjaksot.map(kkilj =>
            HakemuspalveluLukukausi_Ilmoittautumisjakso(
              alku = kkilj.alku,
              loppu = kkilj.loppu,
              tila = HakemuspalveluKoodistokoodiviite.fromKoskiSchema(kkilj.tila),
              ylioppilaskunnanJäsen = kkilj.ylioppilaskunnanJäsen,
              maksetutLukuvuosimaksut = kkilj.maksetutLukuvuosimaksut.map(kklvm =>
                HakemuspalveluLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
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
          HakemuspalveluKorkeakoulututkinnonSuoritus(
            koulutusmoduuli = HakemuspalveluKorkeakoulututkinto(
              tunniste = HakemuspalveluKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste),
              koulutustyyppi = s.koulutusmoduuli.koulutustyyppi.map(HakemuspalveluKoodistokoodiviite.fromKoskiSchema),
              virtaNimi = s.koulutusmoduuli.virtaNimi
            ),
            vahvistus = s.vahvistus.map(v => HakemuspalveluVahvistus(v.päivä)),
            toimipiste = Some(HakemuspalveluToimipiste(
              s.toimipiste.oid,
              s.toimipiste.nimi,
              s.toimipiste.kotipaikka.map(HakemuspalveluKoodistokoodiviite.fromKoskiSchema)
            )),
            tyyppi = s.tyyppi,
            osasuoritukset = s.osasuoritukset
          )
      },
    tyyppi = kk.tyyppi,
    luokittelu = kk.luokittelu.map(_.map(HakemuspalveluKoodistokoodiviite.fromKoskiSchema))
  )
}

@Title("Korkeakoulun opiskeluoikeus")
case class HakemuspalveluKorkeakoulunOpiskeluoikeus(
  oppilaitos: Option[HakemuspalveluOppilaitos],
  koulutustoimija: Option[HakemuspalveluKoulutustoimija],
  override val päättymispäivä: Option[LocalDate],
  tila: HakemuspalveluOpiskeluoikeudenTila,
  lisätiedot: Option[HakemuspalveluKorkeakoulunOpiskeluoikeudenLisätiedot],
  suoritukset: List[HakemuspalveluKorkeakoulututkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  luokittelu: Option[List[HakemuspalveluKoodistokoodiviite]],
) extends HakemuspalveluOpiskeluoikeus {

  override def withSuoritukset(suoritukset: List[HakemuspalveluSuoritus]): HakemuspalveluOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: HakemuspalveluKorkeakoulututkinnonSuoritus => s }
    )
}

@Title("Korkeakoulututkinnon päätason suoritus")
case class HakemuspalveluKorkeakoulututkinnonSuoritus(
  koulutusmoduuli: HakemuspalveluKorkeakoulututkinto,
  @KoodistoKoodiarvo("korkeakoulututkinto")
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[HakemuspalveluVahvistus],
  toimipiste: Option[HakemuspalveluToimipiste],
  osasuoritukset: Option[List[KorkeakoulunOpintojaksonSuoritus]]
) extends HakemuspalveluSuoritus

case class HakemuspalveluKorkeakoulunOpiskeluoikeudenLisätiedot(
  virtaOpiskeluoikeudenTyyppi: Option[HakemuspalveluKoodistokoodiviite],
  lukukausiIlmoittautuminen: Option[HakemuspalveluLukukausi_Ilmoittautuminen],
) extends HakemuspalveluOpiskeluoikeudenLisätiedot

@Title("Lukukausi-ilmoittautuminen")
case class HakemuspalveluLukukausi_Ilmoittautuminen(
  ilmoittautumisjaksot: List[HakemuspalveluLukukausi_Ilmoittautumisjakso]
)

@Title("Lukukausi-ilmoittautumisjakso")
case class HakemuspalveluLukukausi_Ilmoittautumisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tila: HakemuspalveluKoodistokoodiviite,
  ylioppilaskunnanJäsen: Option[Boolean],
  maksetutLukuvuosimaksut: Option[HakemuspalveluLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu]
)

@Title("Lukukausi-ilmoittautumisjakson lukuvuosimaksutiedot")
case class HakemuspalveluLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
  maksettu: Option[Boolean],
  summa: Option[Int],
  apuraha: Option[Int]
)

@Title("Korkeakoulututkinto")
case class HakemuspalveluKorkeakoulututkinto(
  tunniste: HakemuspalveluKoodistokoodiviite,
  koulutustyyppi: Option[HakemuspalveluKoodistokoodiviite],
  virtaNimi: Option[schema.LocalizedString]
) extends HakemuspalveluSuorituksenKoulutusmoduuli

