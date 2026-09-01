package fi.oph.koski.kela

import fi.oph.koski.schema
import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri, SensitiveData}
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate

object KelaKorkeakoulunOpiskeluoikeus {
  def fromKoskiSchema(kk: schema.KorkeakoulunOpiskeluoikeus) = KelaKorkeakoulunOpiskeluoikeus(
    lähdejärjestelmänId = kk.lähdejärjestelmänId.flatMap(_.id),
    oppilaitos = kk.oppilaitos.map(oppilaitos),
    koulutustoimija = kk.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(KelaKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    päättymispäivä = kk.päättymispäivä,
    tila = KelaKorkeakoulunOpiskeluoikeudenTila(
      kk.tila.opiskeluoikeusjaksot.map(oj =>
        KelaKorkeakoulunOpiskeluoikeusjakso(oj.alku, KelaKoodistokoodiviite.fromKoskiSchema(oj.tila))
      )
    ),
    lisätiedot = kk.lisätiedot.map(lisätiedot),
    suoritukset = kk.suoritukset.map(suoritus),
    luokittelu = kk.luokittelu.map(_.map(KelaKoodistokoodiviite.fromKoskiSchema)),
    tyyppi = kk.tyyppi
  )

  private def lisätiedot(l: schema.KorkeakoulunOpiskeluoikeudenLisätiedot) = KelaKorkeakoulunOpiskeluoikeudenLisätiedot(
    virtaOpiskeluoikeudenTyyppi = l.virtaOpiskeluoikeudenTyyppi.map(KelaKoodistokoodiviite.fromKoskiSchema),
    lukukausiIlmoittautuminen = l.lukukausiIlmoittautuminen.map(li =>
      KelaKorkeakoulunLukukausiIlmoittautuminen(
        li.ilmoittautumisjaksot.map(j =>
          KelaKorkeakoulunLukukausiIlmoittautumisjakso(
            alku = j.alku,
            loppu = j.loppu,
            tila = KelaKoodistokoodiviite.fromKoskiSchema(j.tila),
            ilmoittautumispäivä = j.ilmoittautumispäivä
          )
        )
      )
    ),
    koulutuskuntaJaksot = l.koulutuskuntaJaksot.map(j =>
      KelaKorkeakoulunKoulutuskuntaJakso(j.alku, j.loppu, KelaKoodistokoodiviite.fromKoskiSchema(j.koulutuskunta))
    ),
    rahoituslähdeJaksot = l.rahoituslähdeJaksot.map(_.map(j =>
      KelaKorkeakoulunRahoituslähdeJakso(j.alku, j.loppu, KelaKoodistokoodiviite.fromKoskiSchema(j.rahoituslähde))
    )),
    liikkuvuusjaksot = l.liikkuvuusjaksot.map(_.map(j =>
      KelaKorkeakoulunLiikkuvuusjakso(
        alku = j.alku,
        loppu = j.loppu,
        suunta = KelaKoodistokoodiviite.fromKoskiSchema(j.suunta),
        maa = KelaKoodistokoodiviite.fromKoskiSchema(j.maa),
        tyyppi = KelaKoodistokoodiviite.fromKoskiSchema(j.tyyppi),
        liikkuvuusohjelma = KelaKoodistokoodiviite.fromKoskiSchema(j.liikkuvuusohjelma),
        luokittelu = j.luokittelu.map(_.map(KelaKoodistokoodiviite.fromKoskiSchema))
      )
    )),
    siirtoOpiskelija = l.siirtoOpiskelija.map(s =>
      KelaKorkeakoulunSiirtoOpiskelija(s.siirtoPäivä, s.lähdeOrganisaatio.map(oppilaitos))
    ),
    koulutusala = l.koulutusala.map(koulutusala)
  )

  private def suoritus(s: schema.KorkeakouluSuoritus): KelaKorkeakoulunPäätasonSuoritus = s match {
    case t: schema.KorkeakoulututkinnonSuoritus => KelaKorkeakoulututkinnonSuoritus(
      koulutusmoduuli = KelaKorkeakoulututkinto(
        tunniste = KelaKoodistokoodiviite.fromKoskiSchema(t.koulutusmoduuli.tunniste),
        koulutustyyppi = t.koulutusmoduuli.koulutustyyppi.map(KelaKoodistokoodiviite.fromKoskiSchema),
        virtaNimi = t.koulutusmoduuli.virtaNimi,
        koulutusala = t.koulutusmoduuli.koulutusala.map(koulutusala)
      ),
      toimipiste = toimipiste(t.toimipiste),
      vahvistus = t.vahvistus.map(v => Vahvistus(v.päivä)),
      osasuoritukset = t.osasuoritukset.map(_.map(osasuoritus)),
      hyväksilukupäivä = t.hyväksilukupäivä,
      lisätieto = t.lisätieto,
      vaadittuLaajuus = t.vaadittuLaajuus.map(laajuus),
      liittyvätOpiskeluoikeudet = t.liittyvätOpiskeluoikeudet.map(_.map(liittyväOpiskeluoikeus)),
      tyyppi = t.tyyppi
    )
    case o: schema.KorkeakoulunOpintojaksonSuoritus => KelaKorkeakoulunOpintojaksonSuoritus(
      koulutusmoduuli = opintojakso(o.koulutusmoduuli),
      toimipiste = toimipiste(o.toimipiste),
      vahvistus = o.vahvistus.map(v => Vahvistus(v.päivä)),
      osasuoritukset = o.osasuoritukset.map(_.map(osasuoritus)),
      luokittelu = o.luokittelu.map(_.map(KelaKoodistokoodiviite.fromKoskiSchema)),
      hyväksilukupäivä = o.hyväksilukupäivä,
      opinnäytetyö = o.opinnäytetyö,
      lisätieto = o.lisätieto,
      tyyppi = o.tyyppi
    )
    case m: schema.MuuKorkeakoulunSuoritus => KelaMuuKorkeakoulunSuoritus(
      koulutusmoduuli = KelaMuuKorkeakoulunOpinto(
        tunniste = KelaKoodistokoodiviite.fromKoskiSchema(m.koulutusmoduuli.tunniste),
        nimi = m.koulutusmoduuli.nimi,
        laajuus = m.koulutusmoduuli.laajuus.map(laajuus)
      ),
      toimipiste = toimipiste(m.toimipiste),
      vahvistus = m.vahvistus.map(v => Vahvistus(v.päivä)),
      osasuoritukset = m.osasuoritukset.map(_.map(osasuoritus)),
      vaadittuLaajuus = m.vaadittuLaajuus.map(laajuus),
      tyyppi = m.tyyppi
    )
  }

  private def osasuoritus(o: schema.KorkeakoulunOpintojaksonSuoritus): KelaKorkeakoulunOpintojaksonOsasuoritus =
    KelaKorkeakoulunOpintojaksonOsasuoritus(
      koulutusmoduuli = opintojakso(o.koulutusmoduuli),
      toimipiste = toimipiste(o.toimipiste),
      vahvistus = o.vahvistus.map(v => Vahvistus(v.päivä)),
      osasuoritukset = o.osasuoritukset.map(_.map(osasuoritus)),
      luokittelu = o.luokittelu.map(_.map(KelaKoodistokoodiviite.fromKoskiSchema)),
      hyväksilukupäivä = o.hyväksilukupäivä,
      opinnäytetyö = o.opinnäytetyö,
      lisätieto = o.lisätieto,
      tyyppi = o.tyyppi
    )

  private def opintojakso(k: schema.KorkeakoulunOpintojakso) = KelaKorkeakoulunOpintojakso(
    tunniste = KelaPaikallinenKoodiviite(k.tunniste.koodiarvo, Some(k.tunniste.nimi), k.tunniste.koodistoUri),
    nimi = k.nimi,
    laajuus = k.laajuus.map(laajuus),
    koulutusala = k.koulutusala.map(koulutusala)
  )

  private def liittyväOpiskeluoikeus(lo: schema.LiittyväOpiskeluoikeus) = KelaLiittyväOpiskeluoikeus(
    lähdejärjestelmänId = lo.lähdejärjestelmänId,
    oppilaitos = lo.oppilaitos.map(oppilaitos),
    tyyppi = lo.tyyppi.map(KelaKoodistokoodiviite.fromKoskiSchema)
  )

  private def koulutusala(k: schema.KorkeakoulunKoulutusala) = KelaKorkeakoulunKoulutusala(
    opintoala1995 = k.opintoala1995.map(KelaKoodistokoodiviite.fromKoskiSchema),
    okmOhjausala = k.okmOhjausala.map(KelaKoodistokoodiviite.fromKoskiSchema),
    koulutusala2002 = k.koulutusala2002.map(KelaKoodistokoodiviite.fromKoskiSchema),
    osuus = k.osuus
  )

  private def laajuus(l: schema.Laajuus) =
    KelaLaajuus(l.arvo, KelaKoodistokoodiviite.fromKoskiSchema(l.yksikkö))

  private def oppilaitos(o: schema.Oppilaitos) = Oppilaitos(
    o.oid,
    o.oppilaitosnumero.map(KelaKoodistokoodiviite.fromKoskiSchema),
    o.nimi,
    o.kotipaikka.map(KelaKoodistokoodiviite.fromKoskiSchema)
  )

  private def toimipiste(o: schema.Oppilaitos) =
    Toimipiste(o.oid, o.nimi, o.kotipaikka.map(KelaKoodistokoodiviite.fromKoskiSchema))
}

@Title("Korkeakoulun opiskeluoikeus")
@Description("Korkeakoulun opiskeluoikeus. Tiedot haetaan Virrasta, eikä niitä tallenneta Koskeen.")
case class KelaKorkeakoulunOpiskeluoikeus(
  @Description("Opiskeluoikeuden tunniste Virrassa. Korkeakoulun opiskeluoikeudella ei ole Koski-oidia, joten tämä on ainoa yksilöivä tunniste.")
  lähdejärjestelmänId: Option[String],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  override val päättymispäivä: Option[LocalDate],
  tila: KelaKorkeakoulunOpiskeluoikeudenTila,
  lisätiedot: Option[KelaKorkeakoulunOpiskeluoikeudenLisätiedot],
  suoritukset: List[KelaKorkeakoulunPäätasonSuoritus],
  @Title("Opiskeluoikeuden luokittelu")
  @KoodistoUri("virtaopiskeluoikeudenluokittelu")
  luokittelu: Option[List[KelaKoodistokoodiviite]],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends KelaOpiskeluoikeus {
  override def oid = None
  override def versionumero = None
  override def aikaleima = None
  override def arvioituPäättymispäivä = None
  override def sisältyyOpiskeluoikeuteen = None
  override def organisaatioHistoria = None
  override def organisaatiohistoria = None

  override def withHyväksyntämerkinnälläKorvattuArvosana: KelaOpiskeluoikeus = this
  override def withOrganisaatiohistoria: KelaOpiskeluoikeus = this
}

case class KelaKorkeakoulunOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KelaKorkeakoulunOpiskeluoikeusjakso]
) extends KelaOpiskeluoikeudenTilaTrait

case class KelaKorkeakoulunOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: KelaKoodistokoodiviite
) extends KelaOpiskeluoikeusjaksoTrait {
  override def opiskeluoikeusPäättynyt: Boolean =
    schema.Opiskeluoikeus.OpiskeluoikeudenPäättymistila.korkeakoulu(tila.koodiarvo)
}

@Title("Korkeakoulun opiskeluoikeuden lisätiedot")
case class KelaKorkeakoulunOpiskeluoikeudenLisätiedot(
  @Title("Korkeakoulun opiskeluoikeuden tyyppi")
  @KoodistoUri("virtaopiskeluoikeudentyyppi")
  virtaOpiskeluoikeudenTyyppi: Option[KelaKoodistokoodiviite],
  lukukausiIlmoittautuminen: Option[KelaKorkeakoulunLukukausiIlmoittautuminen],
  @Title("Koulutuskunnat")
  koulutuskuntaJaksot: List[KelaKorkeakoulunKoulutuskuntaJakso] = Nil,
  @Title("Rahoituslähteet")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  rahoituslähdeJaksot: Option[List[KelaKorkeakoulunRahoituslähdeJakso]],
  @Title("Liikkuvuusjaksot")
  liikkuvuusjaksot: Option[List[KelaKorkeakoulunLiikkuvuusjakso]],
  siirtoOpiskelija: Option[KelaKorkeakoulunSiirtoOpiskelija],
  koulutusala: Option[KelaKorkeakoulunKoulutusala]
) extends KelaOpiskeluoikeudenLisätiedot

case class KelaKorkeakoulunLukukausiIlmoittautuminen(
  ilmoittautumisjaksot: List[KelaKorkeakoulunLukukausiIlmoittautumisjakso]
)

case class KelaKorkeakoulunLukukausiIlmoittautumisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("virtalukukausiilmtila")
  tila: KelaKoodistokoodiviite,
  @Description("Päivämäärä, jolloin ilmoittautuminen on tehty")
  ilmoittautumispäivä: Option[LocalDate]
) extends KelaJakso

case class KelaKorkeakoulunKoulutuskuntaJakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("kunta")
  koulutuskunta: KelaKoodistokoodiviite
) extends KelaJakso

case class KelaKorkeakoulunRahoituslähdeJakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("virtarahoituslahde")
  rahoituslähde: KelaKoodistokoodiviite
) extends KelaJakso

case class KelaKorkeakoulunLiikkuvuusjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("virtaliikkuvuudensuunta")
  suunta: KelaKoodistokoodiviite,
  @KoodistoUri("maatjavaltiot2")
  maa: KelaKoodistokoodiviite,
  @KoodistoUri("virtaliikkuvuudentyyppi")
  tyyppi: KelaKoodistokoodiviite,
  @KoodistoUri("virtaliikkuvuusohjelma")
  liikkuvuusohjelma: KelaKoodistokoodiviite,
  @KoodistoUri("liikkuvuudenluokittelu")
  luokittelu: Option[List[KelaKoodistokoodiviite]]
) extends KelaJakso

case class KelaKorkeakoulunSiirtoOpiskelija(
  siirtoPäivä: LocalDate,
  lähdeOrganisaatio: Option[Oppilaitos]
)

@Description("Koulutusala Virran luokituksen mukaan")
case class KelaKorkeakoulunKoulutusala(
  @KoodistoUri("opintoalaoph1995")
  opintoala1995: Option[KelaKoodistokoodiviite],
  @KoodistoUri("okmohjauksenala")
  okmOhjausala: Option[KelaKoodistokoodiviite],
  @KoodistoUri("koulutusalaoph2002")
  koulutusala2002: Option[KelaKoodistokoodiviite],
  osuus: Option[Double]
)

@Description("Opiskeluoikeus, johon tämä opiskeluoikeus antaa mahdollisuuden jatkaa")
case class KelaLiittyväOpiskeluoikeus(
  @Description("Liittyvän opiskeluoikeuden lähdejärjestelmän id, sama tunniste jolla se esiintyy tässä vastauksessa")
  lähdejärjestelmänId: String,
  oppilaitos: Option[Oppilaitos],
  @KoodistoUri("virtaopiskeluoikeudentyyppi")
  tyyppi: Option[KelaKoodistokoodiviite]
)

trait KelaKorkeakoulunPäätasonSuoritus extends KelaSuoritus {
  def toimipiste: Toimipiste
  def vahvistus: Option[Vahvistus]
  override def withHyväksyntämerkinnälläKorvattuArvosana: KelaSuoritus = this
}

@Title("Korkeakoulututkinnon suoritus")
case class KelaKorkeakoulututkinnonSuoritus(
  koulutusmoduuli: KelaKorkeakoulututkinto,
  toimipiste: Toimipiste,
  vahvistus: Option[Vahvistus],
  @Title("Opintojaksot")
  osasuoritukset: Option[List[KelaKorkeakoulunOpintojaksonOsasuoritus]],
  @Description("Päivämäärä, jolloin suoritus on hyväksiluettu")
  hyväksilukupäivä: Option[LocalDate],
  @Description("Opintosuorituksen julkinen lisätieto")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  lisätieto: Option[schema.LocalizedString],
  @Description("Tutkinnon tai opintojen vaadittu laajuus")
  vaadittuLaajuus: Option[KelaLaajuus],
  liittyvätOpiskeluoikeudet: Option[List[KelaLiittyväOpiskeluoikeus]],
  @KoodistoKoodiarvo("korkeakoulututkinto")
  tyyppi: schema.Koodistokoodiviite
) extends KelaKorkeakoulunPäätasonSuoritus

@Title("Korkeakoulun opintojakson suoritus")
case class KelaKorkeakoulunOpintojaksonSuoritus(
  koulutusmoduuli: KelaKorkeakoulunOpintojakso,
  toimipiste: Toimipiste,
  vahvistus: Option[Vahvistus],
  @Title("Sisältyvät opintojaksot")
  osasuoritukset: Option[List[KelaKorkeakoulunOpintojaksonOsasuoritus]],
  @KoodistoUri("virtaopsuorluokittelu")
  luokittelu: Option[List[KelaKoodistokoodiviite]],
  @Description("Päivämäärä, jolloin suoritus on hyväksiluettu")
  hyväksilukupäivä: Option[LocalDate],
  @Description("Tieto siitä, onko opintosuoritus opinnäytetyö")
  opinnäytetyö: Option[Boolean],
  @Description("Opintosuorituksen julkinen lisätieto")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  lisätieto: Option[schema.LocalizedString],
  @KoodistoKoodiarvo("korkeakoulunopintojakso")
  tyyppi: schema.Koodistokoodiviite
) extends KelaKorkeakoulunPäätasonSuoritus

@Title("Muu korkeakoulun suoritus")
case class KelaMuuKorkeakoulunSuoritus(
  koulutusmoduuli: KelaMuuKorkeakoulunOpinto,
  toimipiste: Toimipiste,
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaKorkeakoulunOpintojaksonOsasuoritus]],
  @Description("Tutkinnon tai opintojen vaadittu laajuus")
  vaadittuLaajuus: Option[KelaLaajuus],
  @KoodistoKoodiarvo("muukorkeakoulunsuoritus")
  tyyppi: schema.Koodistokoodiviite
) extends KelaKorkeakoulunPäätasonSuoritus

@Title("Korkeakoulun opintojakson osasuoritus")
case class KelaKorkeakoulunOpintojaksonOsasuoritus(
  koulutusmoduuli: KelaKorkeakoulunOpintojakso,
  toimipiste: Toimipiste,
  vahvistus: Option[Vahvistus],
  @Title("Sisältyvät opintojaksot")
  osasuoritukset: Option[List[KelaKorkeakoulunOpintojaksonOsasuoritus]],
  @KoodistoUri("virtaopsuorluokittelu")
  luokittelu: Option[List[KelaKoodistokoodiviite]],
  @Description("Päivämäärä, jolloin suoritus on hyväksiluettu")
  hyväksilukupäivä: Option[LocalDate],
  @Description("Tieto siitä, onko opintosuoritus opinnäytetyö")
  opinnäytetyö: Option[Boolean],
  @Description("Opintosuorituksen julkinen lisätieto")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  lisätieto: Option[schema.LocalizedString],
  @KoodistoKoodiarvo("korkeakoulunopintojakso")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus {
  override def withHyväksyntämerkinnälläKorvattuArvosana: Osasuoritus = this
}

@Description("Korkeakoulututkinnon tunnistetiedot")
case class KelaKorkeakoulututkinto(
  @KoodistoUri("koulutus")
  tunniste: KelaKoodistokoodiviite,
  koulutustyyppi: Option[KelaKoodistokoodiviite],
  @Description("Tutkinnon nimi sellaisena kuin se Virrassa on")
  virtaNimi: Option[schema.LocalizedString],
  koulutusala: Option[KelaKorkeakoulunKoulutusala]
) extends SuorituksenKoulutusmoduuli

@Description("Korkeakoulun opintojakson tunnistetiedot")
case class KelaKorkeakoulunOpintojakso(
  tunniste: KelaPaikallinenKoodiviite,
  nimi: schema.LocalizedString,
  laajuus: Option[KelaLaajuus],
  koulutusala: Option[KelaKorkeakoulunKoulutusala]
) extends SuorituksenKoulutusmoduuli with OsasuorituksenKoulutusmoduuli

@Description("Muun korkeakoulun opinnon tunnistetiedot")
case class KelaMuuKorkeakoulunOpinto(
  @Title("Opiskeluoikeuden tyyppi")
  @KoodistoUri("virtaopiskeluoikeudentyyppi")
  tunniste: KelaKoodistokoodiviite,
  nimi: schema.LocalizedString,
  laajuus: Option[KelaLaajuus]
) extends SuorituksenKoulutusmoduuli
