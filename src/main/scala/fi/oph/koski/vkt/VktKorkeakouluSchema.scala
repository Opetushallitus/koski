package fi.oph.koski.vkt

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

object VktKorkeakoulunOpiskeluoikeus {
  def fromKoskiSchema(kk: schema.KorkeakoulunOpiskeluoikeus) = VktKorkeakoulunOpiskeluoikeus(
    oppilaitos = kk.oppilaitos.map(ol =>
      Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(VktKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(VktKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = kk.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(VktKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    päättymispäivä = kk.päättymispäivä,
    tila = VktOpiskeluoikeudenTila(
      kk.tila.opiskeluoikeusjaksot.map(kkt =>
        VktOpiskeluoikeusjakso(
          kkt.alku,
          VktKoodistokoodiviite.fromKoskiSchema(kkt.tila),
          None
        )
      )
    ),
    lisätiedot = kk.lisätiedot.map(lisätiedot => VktKorkeakoulunOpiskeluoikeudenLisätiedot(
      virtaOpiskeluoikeudenTyyppi = lisätiedot.virtaOpiskeluoikeudenTyyppi.map(VktKoodistokoodiviite.fromKoskiSchema),
      lukukausiIlmoittautuminen = lisätiedot.lukukausiIlmoittautuminen.map(kkl =>
        VktLukukausi_Ilmoittautuminen(
          kkl.ilmoittautumisjaksot.map(kkilj =>
            VktLukukausi_Ilmoittautumisjakso(
              alku = kkilj.alku,
              loppu = kkilj.loppu,
              tila = VktKoodistokoodiviite.fromKoskiSchema(kkilj.tila),
              ylioppilaskunnanJäsen = kkilj.ylioppilaskunnanJäsen,
              maksetutLukuvuosimaksut = kkilj.maksetutLukuvuosimaksut.map(kklvm =>
                VktLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
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
      .map {
        case s: schema.KorkeakoulunOpintojaksonSuoritus =>
          VktKorkeakoulunOpintojaksonSuoritus(
            koulutusmoduuli = VktKorkeakoulunOpintojakso(
              tunniste = VktPaikallinenKoodi.fromKoskiSchema(s.koulutusmoduuli.tunniste),
              nimi = s.koulutusmoduuli.nimi,
            ),
            vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
            suorituskieli = s.suorituskieli.map(VktKoodistokoodiviite.fromKoskiSchema),
            toimipiste = Some(Toimipiste(
              s.toimipiste.oid,
              s.toimipiste.nimi,
              s.toimipiste.kotipaikka.map(VktKoodistokoodiviite.fromKoskiSchema)
            )),
            tyyppi = s.tyyppi
          )
        case s: schema.KorkeakoulututkinnonSuoritus =>
          VktKorkeakoulututkinnonSuoritus(
            koulutusmoduuli = VktKorkeakoulututkinto(
              tunniste = VktKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste),
              koulutustyyppi = s.koulutusmoduuli.koulutustyyppi.map(VktKoodistokoodiviite.fromKoskiSchema),
              virtaNimi = s.koulutusmoduuli.virtaNimi
            ),
            vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
            suorituskieli = s.suorituskieli.map(VktKoodistokoodiviite.fromKoskiSchema),
            toimipiste = Some(Toimipiste(
              s.toimipiste.oid,
              s.toimipiste.nimi,
              s.toimipiste.kotipaikka.map(VktKoodistokoodiviite.fromKoskiSchema)
            )),
            tyyppi = s.tyyppi
          )
        case s: schema.MuuKorkeakoulunSuoritus =>
          VktMuuKorkeakoulunSuoritus(
            koulutusmoduuli = VktMuuKorkeakoulunOpinto(
              tunniste = VktKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste),
              nimi = s.koulutusmoduuli.nimi,
            ),
            vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
            suorituskieli = s.suorituskieli.map(VktKoodistokoodiviite.fromKoskiSchema),
            toimipiste = Some(Toimipiste(
              s.toimipiste.oid,
              s.toimipiste.nimi,
              s.toimipiste.kotipaikka.map(VktKoodistokoodiviite.fromKoskiSchema)
            )),
            tyyppi = s.tyyppi,
          )
      },
    tyyppi = kk.tyyppi,
    luokittelu = kk.luokittelu.map(_.map(VktKoodistokoodiviite.fromKoskiSchema))
  )
}

@Title("Korkeakoulun opiskeluoikeus")
case class VktKorkeakoulunOpiskeluoikeus(
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  override val päättymispäivä: Option[LocalDate],
  tila: VktOpiskeluoikeudenTila,
  lisätiedot: Option[VktKorkeakoulunOpiskeluoikeudenLisätiedot],
  suoritukset: List[VktKorkeakouluSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  luokittelu: Option[List[VktKoodistokoodiviite]],
) extends VktOpiskeluoikeus {

  override def withSuoritukset(suoritukset: List[Suoritus]): VktOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: VktKorkeakouluSuoritus => s }
    )

  override def withoutSisältyyOpiskeluoikeuteen: VktOpiskeluoikeus = this
}

trait VktKorkeakouluSuoritus extends Suoritus

case class VktKorkeakoulunOpintojaksonSuoritus(
  koulutusmoduuli: VktKorkeakoulunOpintojakso,
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[VktKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("korkeakoulunopintojakso")
  tyyppi: schema.Koodistokoodiviite
) extends VktKorkeakouluSuoritus

case class VktKorkeakoulututkinnonSuoritus(
  koulutusmoduuli: VktKorkeakoulututkinto,
  @KoodistoKoodiarvo("korkeakoulututkinto")
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[VktKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
) extends VktKorkeakouluSuoritus

case class VktMuuKorkeakoulunSuoritus(
  koulutusmoduuli: VktMuuKorkeakoulunOpinto,
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[VktKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("muukorkeakoulunsuoritus")
  tyyppi: schema.Koodistokoodiviite
) extends VktKorkeakouluSuoritus

case class VktKorkeakoulunOpiskeluoikeudenLisätiedot(
  virtaOpiskeluoikeudenTyyppi: Option[VktKoodistokoodiviite],
  lukukausiIlmoittautuminen: Option[VktLukukausi_Ilmoittautuminen],
) extends VktOpiskeluoikeudenLisätiedot

@Title("Lukukausi-ilmoittautuminen")
case class VktLukukausi_Ilmoittautuminen(
  ilmoittautumisjaksot: List[VktLukukausi_Ilmoittautumisjakso]
)

@Title("Lukukausi-ilmoittautumisjakso")
case class VktLukukausi_Ilmoittautumisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tila: VktKoodistokoodiviite,
  ylioppilaskunnanJäsen: Option[Boolean],
  maksetutLukuvuosimaksut: Option[VktLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu]
)

@Title("Lukukausi-ilmoittautumisjakson lukuvuosimaksutiedot")
case class VktLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
  maksettu: Option[Boolean],
  summa: Option[Int],
  apuraha: Option[Int]
)

@Title("Muu korkeakoulun opinto")
case class VktMuuKorkeakoulunOpinto(
  tunniste: VktKoodistokoodiviite,
  nimi: schema.LocalizedString,
) extends SuorituksenKoulutusmoduuli

@Title("Korkeakoulututkinto")
case class VktKorkeakoulututkinto(
  tunniste: VktKoodistokoodiviite,
  koulutustyyppi: Option[VktKoodistokoodiviite],
  virtaNimi: Option[schema.LocalizedString]
) extends SuorituksenKoulutusmoduuli

@Title("Korkeakoulun opintojakso")
case class VktKorkeakoulunOpintojakso(
  tunniste: VktPaikallinenKoodi,
  nimi: schema.LocalizedString,
) extends SuorituksenKoulutusmoduuli
