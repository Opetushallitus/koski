package fi.oph.koski.aktiivisetjapaattyneetopinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation.Title

import java.time.LocalDate

object AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus {
  def fromKoskiSchema(kk: schema.KorkeakoulunOpiskeluoikeus) = AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus(
    oppilaitos = kk.oppilaitos.map(ol =>
      Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = kk.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    päättymispäivä = kk.päättymispäivä,
    tila = AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila(
      kk.tila.opiskeluoikeusjaksot.map(kkt =>
        AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso(
          kkt.alku,
          AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema(kkt.tila),
          None
        )
      )
    ),
    lisätiedot = kk.lisätiedot.map(lisätiedot => AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot(
      virtaOpiskeluoikeudenTyyppi = lisätiedot.virtaOpiskeluoikeudenTyyppi.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema),
      lukukausiIlmoittautuminen = lisätiedot.lukukausiIlmoittautuminen.map(kkl =>
        AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen(
          kkl.ilmoittautumisjaksot.map(kkilj =>
            AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso(
              alku = kkilj.alku,
              loppu = kkilj.loppu,
              tila = AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema(kkilj.tila),
              ylioppilaskunnanJäsen = kkilj.ylioppilaskunnanJäsen,
              maksetutLukuvuosimaksut = kkilj.maksetutLukuvuosimaksut.map(kklvm =>
                AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
                  kklvm.maksettu,
                  kklvm.summa,
                  kklvm.apuraha
                )
              )
            )
          )
        )
      ),
      koulutuskuntaJaksot = lisätiedot.koulutuskuntaJaksot.map(j => AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso(j.alku, j.loppu, j.koulutuskunta)),
      opettajanPedagogisetOpinnot = lisätiedot.opettajanPedagogisetOpinnot.map(_.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema)),
      opetettavanAineenOpinnot = lisätiedot.opetettavanAineenOpinnot.map(_.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema)),
    )),
    suoritukset = kk.suoritukset
      .map {
        case s: schema.KorkeakoulunOpintojaksonSuoritus =>
          AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus(
            koulutusmoduuli = AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso(
              tunniste = AktiivisetJaPäättyneetOpinnotPaikallinenKoodi.fromKoskiSchema(s.koulutusmoduuli.tunniste),
              nimi = s.koulutusmoduuli.nimi
            ),
            vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
            suorituskieli = s.suorituskieli.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema),
            toimipiste = Some(Toimipiste(
              s.toimipiste.oid,
              s.toimipiste.nimi,
              s.toimipiste.kotipaikka.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema)
            )),
            tyyppi = s.tyyppi
          )
        case s: schema.KorkeakoulututkinnonSuoritus =>
          AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus(
            koulutusmoduuli = AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto(
              tunniste = AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste),
              koulutustyyppi = s.koulutusmoduuli.koulutustyyppi.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema),
              virtaNimi = s.koulutusmoduuli.virtaNimi,
              eurooppalainenTutkintojenViitekehysEQF = None, // Täydennetään myöhemmässä vaiheessa
              kansallinenTutkintojenViitekehysNQF = None, // Täydennetään myöhemmässä vaiheessa
            ),
            vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
            suorituskieli = s.suorituskieli.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema),
            toimipiste = Some(Toimipiste(
              s.toimipiste.oid,
              s.toimipiste.nimi,
              s.toimipiste.kotipaikka.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema)
            )),
            tyyppi = s.tyyppi
          )
        case s: schema.MuuKorkeakoulunSuoritus =>
          AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus(
            koulutusmoduuli = AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto(
              tunniste = AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste),
              nimi = s.koulutusmoduuli.nimi,
              eurooppalainenTutkintojenViitekehysEQF = None, // Täydennetään myöhemmässä vaiheessa
              kansallinenTutkintojenViitekehysNQF = None, // Täydennetään myöhemmässä vaiheessa
            ),
            vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
            suorituskieli = s.suorituskieli.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema),
            toimipiste = Some(Toimipiste(
              s.toimipiste.oid,
              s.toimipiste.nimi,
              s.toimipiste.kotipaikka.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema)
            )),
            tyyppi = s.tyyppi,
          )
      },
    tyyppi = kk.tyyppi,
    luokittelu = kk.luokittelu.map(_.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema))
  )
}

@Title("Korkeakoulun opiskeluoikeus")
case class AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus(
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  override val päättymispäivä: Option[LocalDate],
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila,
  lisätiedot: Option[AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot],
  suoritukset: List[AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  luokittelu: Option[List[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite]],
) extends AktiivisetJaPäättyneetOpinnotOpiskeluoikeus {

  override def withSuoritukset(suoritukset: List[Suoritus]): AktiivisetJaPäättyneetOpinnotOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus => s }
    )
  override def withoutSisältyyOpiskeluoikeuteen: AktiivisetJaPäättyneetOpinnotOpiskeluoikeus = this
}

trait AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus extends Suoritus

case class AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso,
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("korkeakoulunopintojakso")
  tyyppi: schema.Koodistokoodiviite
) extends AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus

case class AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto ,
  @KoodistoKoodiarvo("korkeakoulututkinto")
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
) extends AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus

case class AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto,
  vahvistus: Option[Vahvistus],
  suorituskieli: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  @KoodistoKoodiarvo("muukorkeakoulunsuoritus")
  tyyppi: schema.Koodistokoodiviite
) extends AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus

case class AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot(
  virtaOpiskeluoikeudenTyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  lukukausiIlmoittautuminen: Option[AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen],
  koulutuskuntaJaksot: List[AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso],
  opettajanPedagogisetOpinnot: Option[List[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite]],
  opetettavanAineenOpinnot: Option[List[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite]],
) extends AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenLisätiedot

@Title("Lukukausi-ilmoittautuminen")
case class AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen(
  ilmoittautumisjaksot: List[AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso]
)

@Title("Lukukausi-ilmoittautumisjakso")
case class AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  tila: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  ylioppilaskunnanJäsen: Option[Boolean],
  maksetutLukuvuosimaksut: Option[AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu]
)

@Title("Lukukausi-ilmoittautumisjakson lukuvuosimaksutiedot")
case class AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
  maksettu: Option[Boolean],
  summa: Option[Int],
  apuraha: Option[Int]
)

@Title("Muu korkeakoulun opinto")
case class AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  nimi: schema.LocalizedString,
  eurooppalainenTutkintojenViitekehysEQF: Option[schema.Koodistokoodiviite],
  kansallinenTutkintojenViitekehysNQF: Option[schema.Koodistokoodiviite]
) extends ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli

@Title("Korkeakoulututkinto")
case class AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
  koulutustyyppi: Option[AktiivisetJaPäättyneetOpinnotKoodistokoodiviite],
  virtaNimi: Option[schema.LocalizedString],
  eurooppalainenTutkintojenViitekehysEQF: Option[schema.Koodistokoodiviite],
  kansallinenTutkintojenViitekehysNQF: Option[schema.Koodistokoodiviite]
) extends ViitekehyksellisenTutkintoSuorituksenKoulutusmoduuli

@Title("Korkeakoulun opintojakso")
case class AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso(
  tunniste: AktiivisetJaPäättyneetOpinnotPaikallinenKoodi,
  nimi: schema.LocalizedString
) extends SuorituksenKoulutusmoduuli

@Title("Koulutuskuntajakso")
case class AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("kunta")
  koulutuskunta: schema.Koodistokoodiviite
)
