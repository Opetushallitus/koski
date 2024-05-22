package fi.oph.koski.vkt

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

object VKTKorkeakoulunOpiskeluoikeus {
  def fromKoskiSchema(kk: schema.KorkeakoulunOpiskeluoikeus) = VKTKorkeakoulunOpiskeluoikeus(
    oppilaitos = kk.oppilaitos.map(ol =>
      Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(VKTKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(VKTKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = kk.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(VKTKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    päättymispäivä = kk.päättymispäivä,
    tila = VKTOpiskeluoikeudenTila(
      kk.tila.opiskeluoikeusjaksot.map(kkt =>
        VKTOpiskeluoikeusjakso(
          kkt.alku,
          VKTKoodistokoodiviite.fromKoskiSchema(kkt.tila),
          None
        )
      )
    ),
    lisätiedot = kk.lisätiedot.map(lisätiedot => VKTKorkeakoulunOpiskeluoikeudenLisätiedot(
      virtaOpiskeluoikeudenTyyppi = lisätiedot.virtaOpiskeluoikeudenTyyppi.map(VKTKoodistokoodiviite.fromKoskiSchema),
      lukukausiIlmoittautuminen = lisätiedot.lukukausiIlmoittautuminen.map(kkl =>
        VKTLukukausi_Ilmoittautuminen(
          kkl.ilmoittautumisjaksot.map(kkilj =>
            VKTLukukausi_Ilmoittautumisjakso(
              alku = kkilj.alku,
              loppu = kkilj.loppu,
              tila = VKTKoodistokoodiviite.fromKoskiSchema(kkilj.tila),
              ylioppilaskunnanJäsen = kkilj.ylioppilaskunnanJäsen,
              maksetutLukuvuosimaksut = kkilj.maksetutLukuvuosimaksut.map(kklvm =>
                VKTLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
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
          VKTKorkeakoulunOpintojaksonSuoritus(
            koulutusmoduuli = VKTKorkeakoulunOpintojakso(
              tunniste = VKTPaikallinenKoodi.fromKoskiSchema(s.koulutusmoduuli.tunniste),
              nimi = s.koulutusmoduuli.nimi,
            ),
            vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
            suorituskieli = s.suorituskieli.map(VKTKoodistokoodiviite.fromKoskiSchema),
            toimipiste = Some(Toimipiste(
              s.toimipiste.oid,
              s.toimipiste.nimi,
              s.toimipiste.kotipaikka.map(VKTKoodistokoodiviite.fromKoskiSchema)
            )),
            tyyppi = s.tyyppi
          )
        case s: schema.KorkeakoulututkinnonSuoritus =>
          VKTKorkeakoulututkinnonSuoritus(
            koulutusmoduuli = VKTKorkeakoulututkinto(
              tunniste = VKTKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste),
              koulutustyyppi = s.koulutusmoduuli.koulutustyyppi.map(VKTKoodistokoodiviite.fromKoskiSchema),
              virtaNimi = s.koulutusmoduuli.virtaNimi
            ),
            vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
            suorituskieli = s.suorituskieli.map(VKTKoodistokoodiviite.fromKoskiSchema),
            toimipiste = Some(Toimipiste(
              s.toimipiste.oid,
              s.toimipiste.nimi,
              s.toimipiste.kotipaikka.map(VKTKoodistokoodiviite.fromKoskiSchema)
            )),
            tyyppi = s.tyyppi
          )
        case s: schema.MuuKorkeakoulunSuoritus =>
          VKTMuuKorkeakoulunSuoritus(
            koulutusmoduuli = VKTMuuKorkeakoulunOpinto(
              tunniste = VKTKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste),
              nimi = s.koulutusmoduuli.nimi,
            ),
            vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
            suorituskieli = s.suorituskieli.map(VKTKoodistokoodiviite.fromKoskiSchema),
            toimipiste = Some(Toimipiste(
              s.toimipiste.oid,
              s.toimipiste.nimi,
              s.toimipiste.kotipaikka.map(VKTKoodistokoodiviite.fromKoskiSchema)
            )),
            tyyppi = s.tyyppi,
          )
      },
    tyyppi = kk.tyyppi,
    luokittelu = kk.luokittelu.map(_.map(VKTKoodistokoodiviite.fromKoskiSchema))
  )
}

@Title("Korkeakoulun opiskeluoikeus")
case class VKTKorkeakoulunOpiskeluoikeus(
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  override val päättymispäivä: Option[LocalDate],
  tila: VKTOpiskeluoikeudenTila,
  lisätiedot: Option[VKTKorkeakoulunOpiskeluoikeudenLisätiedot],
  suoritukset: List[VKTKorkeakouluSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  luokittelu: Option[List[VKTKoodistokoodiviite]],
) extends VKTOpiskeluoikeus {

  override def withSuoritukset(suoritukset: List[Suoritus]): VKTOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: VKTKorkeakouluSuoritus => s }
    )

  override def withoutSisältyyOpiskeluoikeuteen: VKTOpiskeluoikeus = this
}

trait VKTKorkeakouluSuoritus extends Suoritus

case class VKTKorkeakoulunOpintojaksonSuoritus(
  koulutusmoduuli: VKTKorkeakoulunOpintojakso,
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[VKTKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("korkeakoulunopintojakso")
  tyyppi: schema.Koodistokoodiviite
) extends VKTKorkeakouluSuoritus

case class VKTKorkeakoulututkinnonSuoritus(
  koulutusmoduuli: VKTKorkeakoulututkinto,
  @KoodistoKoodiarvo("korkeakoulututkinto")
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[VKTKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
) extends VKTKorkeakouluSuoritus

case class VKTMuuKorkeakoulunSuoritus(
  koulutusmoduuli: VKTMuuKorkeakoulunOpinto,
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[VKTKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("muukorkeakoulunsuoritus")
  tyyppi: schema.Koodistokoodiviite
) extends VKTKorkeakouluSuoritus

case class VKTKorkeakoulunOpiskeluoikeudenLisätiedot(
  virtaOpiskeluoikeudenTyyppi: Option[VKTKoodistokoodiviite],
  lukukausiIlmoittautuminen: Option[VKTLukukausi_Ilmoittautuminen],
) extends VKTOpiskeluoikeudenLisätiedot

@Title("Lukukausi-ilmoittautuminen")
case class VKTLukukausi_Ilmoittautuminen(
  ilmoittautumisjaksot: List[VKTLukukausi_Ilmoittautumisjakso]
)

@Title("Lukukausi-ilmoittautumisjakso")
case class VKTLukukausi_Ilmoittautumisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tila: VKTKoodistokoodiviite,
  ylioppilaskunnanJäsen: Option[Boolean],
  maksetutLukuvuosimaksut: Option[VKTLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu]
)

@Title("Lukukausi-ilmoittautumisjakson lukuvuosimaksutiedot")
case class VKTLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
  maksettu: Option[Boolean],
  summa: Option[Int],
  apuraha: Option[Int]
)

@Title("Muu korkeakoulun opinto")
case class VKTMuuKorkeakoulunOpinto(
  tunniste: VKTKoodistokoodiviite,
  nimi: schema.LocalizedString,
) extends SuorituksenKoulutusmoduuli

@Title("Korkeakoulututkinto")
case class VKTKorkeakoulututkinto(
  tunniste: VKTKoodistokoodiviite,
  koulutustyyppi: Option[VKTKoodistokoodiviite],
  virtaNimi: Option[schema.LocalizedString]
) extends SuorituksenKoulutusmoduuli

@Title("Korkeakoulun opintojakso")
case class VKTKorkeakoulunOpintojakso(
  tunniste: VKTPaikallinenKoodi,
  nimi: schema.LocalizedString,
) extends SuorituksenKoulutusmoduuli
