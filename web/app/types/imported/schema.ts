/*
 * fi.oph.koski.schema
 */

export type Oppija = {
  henkilö: Henkilö
  opiskeluoikeudet: Array<Opiskeluoikeus>
}

export type Aikajakso = {
  alku: string
  loppu?: string
}

export type AikuistenPerusopetuksenAlkuvaihe = {
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenoppimaaranalkuvaihe'
  >
}

export type English = {
  en: string
}

export type Finnish = {
  fi: string
  sv?: string
  en?: string
}

export type LocalizedString = English | Finnish | Swedish

export type Swedish = {
  sv: string
  en?: string
}

export type AikuistenPerusopetuksenAlkuvaiheenKurssi =
  | PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi
  | ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017

export type AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus = {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenalkuvaiheenkurssi'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaiheenKurssi
  tunnustettu?: OsaamisenTunnustaminen
}

export type AikuistenPerusopetuksenAlkuvaiheenOppiaine =
  | AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine
  | AikuistenPerusopetuksenAlkuvaiheenVierasKieli
  | AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus
  | MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine

export type AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus = {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenalkuvaiheenoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  suoritustapa?: Koodistokoodiviite<
    'perusopetuksensuoritustapa',
    'erityinentutkinto'
  >
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaiheenOppiaine
  osasuoritukset?: Array<AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus>
}

export type AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
}

export type AikuistenPerusopetuksenAlkuvaiheenSuoritus = {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenoppimaaranalkuvaihe'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusKursseina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaihe
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type AikuistenPerusopetuksenAlkuvaiheenVierasKieli = {
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenalkuvaiheenoppiaineet',
    'A1'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
}

export type AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus = {
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenalkuvaiheenoppiaineet',
    'AI'
  >
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
}

export type AikuistenPerusopetuksenKurssi =
  | PaikallinenAikuistenPerusopetuksenKurssi
  | ValtakunnallinenAikuistenPerusopetuksenKurssi2015
  | ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017

export type AikuistenPerusopetuksenKurssinSuoritus = {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenkurssi'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: AikuistenPerusopetuksenKurssi
  tunnustettu?: OsaamisenTunnustaminen
}

export type AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus =
  | AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus
  | AikuistenPerusopetuksenKurssinSuoritus

export type AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot = {
  tehostetunTuenPäätökset?: Array<Aikajakso>
  ulkomaanjaksot?: Array<Aikajakso>
  majoitusetu?: Aikajakso
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  oikeusMaksuttomaanAsuntolapaikkaan?: Aikajakso
  vaikeastiVammainen?: Array<Aikajakso>
  maksuttomuus?: Array<Maksuttomuus>
  ulkomailla?: Aikajakso
  vammainen?: Array<Aikajakso>
  tehostetunTuenPäätös?: Aikajakso
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  vuosiluokkiinSitoutumatonOpetus?: boolean
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export type AikuistenPerusopetuksenOpiskeluoikeudenTila = {
  opiskeluoikeusjaksot: Array<AikuistenPerusopetuksenOpiskeluoikeusjakso>
}

export type AikuistenPerusopetuksenOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'aikuistenperusopetus'>
  tila: AikuistenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<AikuistenPerusopetuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  oppilaitos?: Oppilaitos
}

export type AikuistenPerusopetuksenOpiskeluoikeusjakso = {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'peruutettu'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '1' | '6'>
}

export type AikuistenPerusopetuksenOppiaine =
  | AikuistenPerusopetuksenPaikallinenOppiaine
  | AikuistenPerusopetuksenUskonto
  | AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli
  | AikuistenPerusopetuksenÄidinkieliJaKirjallisuus
  | MuuAikuistenPerusopetuksenOppiaine

export type AikuistenPerusopetuksenOppiaineenOppimääränSuoritus = {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetuksenoppiaineenoppimaara'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type AikuistenPerusopetuksenOppiaineenSuoritus = {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  suoritustapa?: Koodistokoodiviite<
    'perusopetuksensuoritustapa',
    'erityinentutkinto'
  >
  koulutusmoduuli: AikuistenPerusopetuksenOppiaine
  osasuoritukset?: Array<AikuistenPerusopetuksenKurssinSuoritus>
}

export type AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine =
  | AikuistenPerusopetuksenPaikallinenOppiaine
  | AikuistenPerusopetuksenUskonto
  | AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli
  | AikuistenPerusopetuksenÄidinkieliJaKirjallisuus
  | EiTiedossaOppiaine
  | MuuAikuistenPerusopetuksenOppiaine

export type AikuistenPerusopetuksenOppimääränSuoritus = {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenoppimaara'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusKursseina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: AikuistenPerusopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AikuistenPerusopetuksenOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type AikuistenPerusopetuksenPaikallinenOppiaine = {
  pakollinen?: boolean
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: PaikallinenKoodi
}

export type AikuistenPerusopetuksenPäätasonSuoritus =
  | AikuistenPerusopetuksenAlkuvaiheenSuoritus
  | AikuistenPerusopetuksenOppiaineenOppimääränSuoritus
  | AikuistenPerusopetuksenOppimääränSuoritus

export type AikuistenPerusopetuksenUskonto = {
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
}

export type AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli = {
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    'A1' | 'A2' | 'B1' | 'B2' | 'B3'
  >
}

export type AikuistenPerusopetuksenÄidinkieliJaKirjallisuus = {
  pakollinen: boolean
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
}

export type AikuistenPerusopetus = {
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koulutus', '201101'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type AmmatillinenArviointi = {
  päivä: string
  arvosana: Koodistokoodiviite<
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}

export type AmmatillinenOpiskeluoikeudenTila = {
  opiskeluoikeusjaksot: Array<AmmatillinenOpiskeluoikeusjakso>
}

export type AmmatillinenOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ammatillinenkoulutus'>
  tila: AmmatillinenOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: AmmatillisenOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<AmmatillinenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  ostettu?: boolean
  oppilaitos?: Oppilaitos
}

export type AmmatillinenOpiskeluoikeusjakso = {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'loma'
    | 'eronnut'
    | 'peruutettu'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', string>
}

export type AmmatillinenPäätasonSuoritus =
  | AmmatillisenTutkinnonOsittainenSuoritus
  | AmmatillisenTutkinnonSuoritus
  | MuunAmmatillisenKoulutuksenSuoritus
  | NäyttötutkintoonValmistavanKoulutuksenSuoritus
  | TelmaKoulutuksenSuoritus
  | TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus
  | ValmaKoulutuksenSuoritus

export type AmmatillinenTutkintoKoulutus = {
  tunniste: Koodistokoodiviite<'koulutus', string>
  perusteenDiaarinumero?: string
  perusteenNimi?: LocalizedString
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type AmmatilliseenTehtäväänValmistavaKoulutus = {
  tunniste: Koodistokoodiviite<
    'ammatilliseentehtavaanvalmistavakoulutus',
    string
  >
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus?: LocalizedString
}

export type AmmatillisenOpiskeluoikeudenLisätiedot = {
  osaAikaisuusjaksot?: Array<OsaAikaisuusJakso>
  vaativanErityisenTuenErityinenTehtävä?: Array<Aikajakso>
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitus?: Array<Aikajakso>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  oikeusMaksuttomaanAsuntolapaikkaan?: boolean
  vaikeastiVammainen?: Array<Aikajakso>
  maksuttomuus?: Array<Maksuttomuus>
  vammainenJaAvustaja?: Array<Aikajakso>
  majoitus?: Array<Aikajakso>
  vankilaopetuksessa?: Array<Aikajakso>
  henkilöstökoulutus?: boolean
  erityinenTuki?: Array<Aikajakso>
  koulutusvienti?: boolean
  opiskeluvalmiuksiaTukevatOpinnot?: Array<OpiskeluvalmiuksiaTukevienOpintojenJakso>
  hojks?: Hojks
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export type AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus = {
  arviointi?: Array<AmmatillinenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillisentutkinnonosaapienempikokonaisuus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: AmmatillisenTutkinnonOsaaPienempiKokonaisuus
  tunnustettu?: OsaamisenTunnustaminen
}

export type AmmatillisenTutkinnonOsaaPienempiKokonaisuus = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
}

export type AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =
  {
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa
    tutkinnonOsanRyhmä?: Koodistokoodiviite<
      'ammatillisentutkinnonosanryhma',
      '1'
    >
    osasuoritukset?: Array<YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus>
  }

export type AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: KorkeakouluopinnotTutkinnonOsa
  tutkinnonOsanRyhmä?: Koodistokoodiviite<'ammatillisentutkinnonosanryhma', '1'>
  osasuoritukset?: Array<KorkeakouluopintojenSuoritus>
}

export type AmmatillisenTutkinnonOsanLisätieto = {
  tunniste: Koodistokoodiviite<'ammatillisentutkinnonosanlisatieto', string>
  kuvaus: LocalizedString
}

export type AmmatillisenTutkinnonOsanOsaAlue =
  | AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli
  | AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla
  | AmmatillisenTutkinnonÄidinkieli
  | PaikallinenAmmatillisenTutkinnonOsanOsaAlue
  | ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue

export type AmmatillisenTutkinnonOsanSuoritus =
  | AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
  | AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
  | MuunAmmatillisenTutkinnonOsanSuoritus
  | YhteisenAmmatillisenTutkinnonOsanSuoritus

export type AmmatillisenTutkinnonOsittainenSuoritus = {
  toinenTutkintonimike?: boolean
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillinentutkintoosittainen'
  >
  keskiarvo?: number
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  toinenOsaamisala?: boolean
  keskiarvoSisältääMukautettujaArvosanoja?: boolean
  suoritustapa: Koodistokoodiviite<'ammatillisentutkinnonsuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: AmmatillinenTutkintoKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OsittaisenAmmatillisenTutkinnonOsanSuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export type AmmatillisenTutkinnonSuoritus = {
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillinentutkinto'>
  keskiarvo?: number
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  keskiarvoSisältääMukautettujaArvosanoja?: boolean
  suoritustapa: Koodistokoodiviite<'ammatillisentutkinnonsuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: AmmatillinenTutkintoKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AmmatillisenTutkinnonOsanSuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export type AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli = {
  tunniste: Koodistokoodiviite<'ammatillisenoppiaineet', 'VK' | 'TK1' | 'TK2'>
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export type AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla = {
  tunniste: Koodistokoodiviite<
    'ammatillisenoppiaineet',
    'VVTK' | 'VVAI' | 'VVAI22' | 'VVVK'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export type AmmatillisenTutkinnonÄidinkieli = {
  tunniste: Koodistokoodiviite<'ammatillisenoppiaineet', 'AI'>
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export type Arvioitsija = {
  nimi: string
}

export type DIANäyttötutkinto = {
  tunniste: Koodistokoodiviite<'diapaattokoe', 'nayttotutkinto'>
}

export type DIAOpiskeluoikeudenLisätiedot = {
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  maksuttomuus?: Array<Maksuttomuus>
  ulkomainenVaihtoopiskelija?: boolean
  erityisenKoulutustehtävänJaksot?: Array<ErityisenKoulutustehtävänJakso>
  pidennettyPäättymispäivä?: boolean
}

export type DIAOpiskeluoikeudenTila = {
  opiskeluoikeusjaksot: Array<DIAOpiskeluoikeusjakso>
}

export type DIAOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'diatutkinto'>
  tila: DIAOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: DIAOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<DIAPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export type DIAOpiskeluoikeusjakso = {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'peruutettu'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '1' | '6'>
}

export type DIAOppiaine =
  | DIAOppiaineKieli
  | DIAOppiaineLisäaine
  | DIAOppiaineLisäaineKieli
  | DIAOppiaineMuu
  | DIAOppiaineÄidinkieli

export type DIAOppiaineenTutkintovaiheenLukukausi = {
  tunniste: Koodistokoodiviite<'dialukukausi', '3' | '4' | '5' | '6'>
  laajuus?: LaajuusVuosiviikkotunneissa
}

export type DIAOppiaineenTutkintovaiheenNumeerinenArviointi = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkodiatutkinto',
    | '0'
    | '1'
    | '2'
    | '2-'
    | '3'
    | '4'
    | '5'
    | '6'
    | '7'
    | '8'
    | '9'
    | '10'
    | '11'
    | '12'
    | '13'
    | '14'
    | '15'
  >
  päivä?: string
  lasketaanKokonaispistemäärään?: boolean
  hyväksytty?: boolean
}

export type DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus = {
  koulutusmoduuli: DIAOppiaineenTutkintovaiheenOsasuoritus
  arviointi?: Array<DIATutkintovaiheenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'diaoppiaineentutkintovaiheenosasuorituksensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type DIAOppiaineenTutkintovaiheenOsasuoritus =
  | DIANäyttötutkinto
  | DIAOppiaineenTutkintovaiheenLukukausi
  | DIAPäättökoe

export type DIAOppiaineenTutkintovaiheenSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'diaoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koetuloksenNelinkertainenPistemäärä?: number
  koulutusmoduuli: DIAOppiaine
  vastaavuustodistuksenTiedot?: DIAVastaavuustodistuksenTiedot
  osasuoritukset?: Array<DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus>
}

export type DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi = {
  arvosana: Koodistokoodiviite<'arviointiasteikkodiatutkinto', 'S'>
  päivä?: string
  hyväksytty?: boolean
}

export type DIAOppiaineenValmistavanVaiheenLukukaudenArviointi = {
  arvosana: Koodistokoodiviite<'arviointiasteikkodiavalmistava', string>
  päivä?: string
  hyväksytty?: boolean
}

export type DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus = {
  koulutusmoduuli: DIAOppiaineenValmistavanVaiheenLukukausi
  arviointi?: Array<DIAOppiaineenValmistavanVaiheenLukukaudenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'diaoppiaineenvalmistavanvaiheenlukukaudensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type DIAOppiaineenValmistavanVaiheenLukukausi = {
  tunniste: Koodistokoodiviite<'dialukukausi', '1' | '2'>
  laajuus?: LaajuusVuosiviikkotunneissa
}

export type DIAOppiaineenValmistavanVaiheenSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'diaoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: DIAOppiaine
  osasuoritukset?: Array<DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus>
}

export type DIAOppiaineKieli = {
  pakollinen: boolean
  osaAlue: Koodistokoodiviite<'diaosaalue', '1'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'EN' | 'FR' | 'SV' | 'RU'>
  laajuus?: LaajuusVuosiviikkotunneissa
  tunniste: Koodistokoodiviite<'oppiaineetdia', 'A' | 'B1' | 'B3'>
}

export type DIAOppiaineLisäaine = {
  tunniste: Koodistokoodiviite<
    'oppiaineetdia',
    | 'CLOE'
    | 'CCEA'
    | 'LT'
    | 'MASY'
    | 'MALI'
    | 'LI'
    | 'VELI'
    | 'ELI'
    | 'RALI'
    | 'VT'
  >
  laajuus?: LaajuusVuosiviikkotunneissa
}

export type DIAOppiaineLisäaineKieli = {
  tunniste: Koodistokoodiviite<'oppiaineetdia', 'B2'>
  laajuus?: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'kielivalikoima', 'LA'>
}

export type DIAOppiaineMuu = {
  tunniste: Koodistokoodiviite<
    'oppiaineetdia',
    | 'KU'
    | 'MU'
    | 'MA'
    | 'FY'
    | 'BI'
    | 'KE'
    | 'TI'
    | 'TK'
    | 'HI'
    | 'MAA'
    | 'TA'
    | 'US'
    | 'FI'
    | 'ET'
  >
  laajuus?: LaajuusVuosiviikkotunneissa
  osaAlue: Koodistokoodiviite<'diaosaalue', string>
  pakollinen: boolean
}

export type DIAOppiaineÄidinkieli = {
  tunniste: Koodistokoodiviite<'oppiaineetdia', 'AI'>
  laajuus?: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'oppiainediaaidinkieli', 'FI' | 'S2' | 'DE'>
  osaAlue: Koodistokoodiviite<'diaosaalue', '1'>
}

export type DIAPäätasonSuoritus =
  | DIATutkinnonSuoritus
  | DIAValmistavanVaiheenSuoritus

export type DIAPäättökoe = {
  tunniste: Koodistokoodiviite<
    'diapaattokoe',
    'kirjallinenkoe' | 'suullinenkoe'
  >
}

export type DIATutkinnonSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'diatutkintovaihe'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  tutkintoaineidenKokonaispistemäärä?: number
  kokonaispistemäärästäJohdettuKeskiarvo?: number
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  kokonaispistemäärä?: number
  koulutusmoduuli: DIATutkinto
  toimipiste: OrganisaatioWithOid
  lukukausisuoritustenKokonaispistemäärä?: number
  osasuoritukset?: Array<DIAOppiaineenTutkintovaiheenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type DIATutkinto = {
  tunniste: Koodistokoodiviite<'koulutus', '301103'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type DIATutkintovaiheenArviointi =
  | DIAOppiaineenTutkintovaiheenNumeerinenArviointi
  | DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi

export type DIAValmistavanVaiheenSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'diavalmistavavaihe'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: DIAValmistavaVaihe
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<DIAOppiaineenValmistavanVaiheenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type DIAValmistavaVaihe = {
  tunniste: Koodistokoodiviite<'suorituksentyyppi', 'diavalmistavavaihe'>
}

export type DIAVastaavuustodistuksenTiedot = {
  keskiarvo: number
  lukioOpintojenLaajuus: LaajuusOpintopisteissäTaiKursseissa
}

export type DiplomaArviointi =
  | InternationalSchoolIBOppiaineenArviointi
  | NumeerinenInternationalSchoolOppiaineenArviointi
  | PassFailOppiaineenArviointi

export type DiplomaCoreRequirementsOppiaine = {
  tunniste: Koodistokoodiviite<'oppiaineetib', 'TOK' | 'EE' | 'CAS'>
}

export type DiplomaCoreRequirementsOppiaineenSuoritus = {
  arviointi?: Array<InternationalSchoolCoreRequirementsArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolcorerequirements'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: DiplomaCoreRequirementsOppiaine
}

export type DiplomaIBOppiaineenSuoritus =
  | DiplomaCoreRequirementsOppiaineenSuoritus
  | DiplomaOppiaineenSuoritus

export type DiplomaLuokkaAste = IBDiplomaLuokkaAste | ISHDiplomaLuokkaAste

export type DiplomaOppiaineenSuoritus = {
  arviointi?: Array<DiplomaArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschooldiplomaoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: InternationalSchoolIBOppiaine
}

export type DiplomaVuosiluokanSuoritus = {
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschooldiplomavuosiluokka'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  koulutusmoduuli: DiplomaLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<DiplomaIBOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type Duplikaatti = {
  tyyppi: string
  arvo: string
}

export type EBArviointi =
  | EBTutkintoFinalMarkArviointi
  | EBTutkintoPreliminaryMarkArviointi

export type EBOppiaineenAlaosasuoritus = {
  koulutusmoduuli: EBOppiaineKomponentti
  arviointi?: Array<EBArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinnonalaosasuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type EBOppiaineKomponentti = {
  tunniste: Koodistokoodiviite<'ebtutkinnonoppiaineenkomponentti', string>
}

export type EBTutkinnonOsasuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinnonosasuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  osasuoritukset?: Array<EBOppiaineenAlaosasuoritus>
}

export type EBTutkinnonSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  yleisarvosana?: number
  koulutusmoduuli: EBTutkinto
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<EBTutkinnonOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type EBTutkinto = {
  tunniste: Koodistokoodiviite<'koulutus', '301104'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
}

export type EBTutkintoFinalMarkArviointi = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkifinalmark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}

export type EBTutkintoPreliminaryMarkArviointi = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}

export type EiTiedossaOppiaine = {
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'XX'>
  perusteenDiaarinumero?: string
}

export type ErityisenKoulutustehtävänJakso = {
  alku: string
  loppu?: string
  tehtävä: Koodistokoodiviite<'erityinenkoulutustehtava', string>
}

export type ErityisenTuenPäätös = {
  toteutuspaikka?: Koodistokoodiviite<'erityisopetuksentoteutuspaikka', string>
  opiskeleeToimintaAlueittain: boolean
  loppu?: string
  erityisryhmässä?: boolean
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  alku?: string
}

export type EsiopetuksenOpiskeluoikeudenLisätiedot = {
  pidennettyOppivelvollisuus?: Aikajakso
  majoitusetu?: Aikajakso
  kuljetusetu?: Aikajakso
  vaikeastiVammainen?: Array<Aikajakso>
  koulukoti?: Array<Aikajakso>
  erityisenTuenPäätökset?: Array<ErityisenTuenPäätös>
  erityisenTuenPäätös?: ErityisenTuenPäätös
  vammainen?: Array<Aikajakso>
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export type EsiopetuksenOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'esiopetus'>
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: EsiopetuksenOpiskeluoikeudenLisätiedot
  versionumero?: number
  järjestämismuoto?: Koodistokoodiviite<
    'vardajarjestamismuoto',
    'JM02' | 'JM03'
  >
  suoritukset: Array<EsiopetuksenSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export type EsiopetuksenSuoritus = {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'esiopetuksensuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  kielikylpykieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: Esiopetus
  toimipiste: OrganisaatioWithOid
  osaAikainenErityisopetus?: Array<
    Koodistokoodiviite<'osaaikainenerityisopetuslukuvuodenaikana', string>
  >
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type Esiopetus = {
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koulutus', '001101' | '001102'>
  kuvaus?: LocalizedString
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type EuropeanSchoolOfHelsinkiKielioppiaine = {
  tunniste: Koodistokoodiviite<'europeanschoolofhelsinkikielioppiaine', string>
  laajuus: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'kieli', string>
}

export type EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek = {
  tunniste: Koodistokoodiviite<'europeanschoolofhelsinkikielioppiaine', 'GRC'>
  laajuus: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'kieli', 'EL'>
}

export type EuropeanSchoolOfHelsinkiKielioppiaineLatin = {
  tunniste: Koodistokoodiviite<'europeanschoolofhelsinkikielioppiaine', 'LA'>
  laajuus: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'kieli', 'LA'>
}

export type EuropeanSchoolOfHelsinkiMuuOppiaine = {
  tunniste: Koodistokoodiviite<'europeanschoolofhelsinkimuuoppiaine', string>
  laajuus: LaajuusVuosiviikkotunneissa
}

export type EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot = {
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  maksuttomuus?: Array<Maksuttomuus>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
}

export type EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila = {
  opiskeluoikeusjaksot: Array<EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso>
}

export type EuropeanSchoolOfHelsinkiOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<
    'opiskeluoikeudentyyppi',
    'europeanschoolofhelsinki'
  >
  tila: EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<EuropeanSchoolOfHelsinkiPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export type EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso = {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '6'>
}

export type EuropeanSchoolOfHelsinkiOsasuoritusArviointi = {
  päivä?: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkiosasuoritus',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}

export type EuropeanSchoolOfHelsinkiPäätasonSuoritus =
  | EBTutkinnonSuoritus
  | NurseryVuosiluokanSuoritus
  | PrimaryVuosiluokanSuoritus
  | SecondaryLowerVuosiluokanSuoritus
  | SecondaryUpperVuosiluokanSuoritus

export type FitnessAndWellBeing = {
  tunniste: Koodistokoodiviite<'oppiaineetinternationalschool', 'HAWB'>
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}

export type Henkilö =
  | HenkilötiedotJaOid
  | OidHenkilö
  | TäydellisetHenkilötiedot
  | UusiHenkilö

export type HenkilötiedotJaOid = {
  sukunimi: string
  oid: string
  kutsumanimi: string
  hetu?: string
  etunimet: string
}

export type HenkilövahvistusPaikkakunnalla = {
  päivä: string
  paikkakunta: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio: Organisaatio
  myöntäjäHenkilöt: Array<Organisaatiohenkilö>
}

export type HenkilövahvistusValinnaisellaPaikkakunnalla = {
  päivä: string
  paikkakunta?: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio: Organisaatio
  myöntäjäHenkilöt: Array<Organisaatiohenkilö>
}

export type HenkilövahvistusValinnaisellaTittelillä =
  HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla

export type HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla =
  {
    päivä: string
    paikkakunta?: Koodistokoodiviite<'kunta', string>
    myöntäjäOrganisaatio: Organisaatio
    myöntäjäHenkilöt: Array<OrganisaatiohenkilöValinnaisellaTittelillä>
  }

export type Hojks = {
  opetusryhmä: Koodistokoodiviite<'opetusryhma', string>
  alku?: string
  loppu?: string
}

export type IBAineRyhmäOppiaine = IBOppiaineLanguage | IBOppiaineMuu

export type IBCASOppiaineenArviointi = {
  päivä?: string
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', 'S'>
  predicted: boolean
  hyväksytty?: boolean
}

export type IBCASSuoritus = {
  arviointi?: Array<IBCASOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'iboppiainecas'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBOppiaineCAS
}

export type IBCoreRequirementsArviointi = {
  arvosana: Koodistokoodiviite<'arviointiasteikkocorerequirementsib', string>
  predicted: boolean
  päivä?: string
  hyväksytty?: boolean
}

export type IBDiplomaLuokkaAste = {
  diplomaType: Koodistokoodiviite<'internationalschooldiplomatype', 'ib'>
  tunniste: Koodistokoodiviite<'internationalschoolluokkaaste', '11' | '12'>
}

export type IBExtendedEssaySuoritus = {
  arviointi?: Array<IBCoreRequirementsArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'iboppiaineee'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBOppiaineExtendedEssay
}

export type IBKurssi = {
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}

export type IBKurssinArviointi = {
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', string>
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  päivä: string
  hyväksytty?: boolean
}

export type IBKurssinSuoritus = {
  arviointi?: Array<IBKurssinArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ibkurssi'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBKurssi
}

export type IBOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ibtutkinto'>
  tila: LukionOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: LukionOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<IBPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export type IBOppiaineCAS = {
  tunniste: Koodistokoodiviite<'oppiaineetib', 'CAS'>
  laajuus?: LaajuusTunneissa
  pakollinen: boolean
}

export type IBOppiaineenArviointi = {
  päivä?: string
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', string>
  predicted: boolean
  hyväksytty?: boolean
}

export type IBOppiaineenSuoritus = {
  arviointi?: Array<IBOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'iboppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBAineRyhmäOppiaine
  osasuoritukset?: Array<IBKurssinSuoritus>
}

export type IBOppiaineExtendedEssay = {
  tunniste: Koodistokoodiviite<'oppiaineetib', 'EE'>
  aine: IBAineRyhmäOppiaine
  aihe: LocalizedString
  pakollinen: boolean
}

export type IBOppiaineLanguage = {
  pakollinen: boolean
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusTunneissa
  ryhmä: Koodistokoodiviite<'aineryhmaib', string>
  tunniste: Koodistokoodiviite<'oppiaineetib', 'A' | 'A2' | 'B' | 'AB'>
}

export type IBOppiaineMuu = {
  pakollinen: boolean
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
  laajuus?: LaajuusTunneissa
  ryhmä: Koodistokoodiviite<'aineryhmaib', string>
  tunniste: Koodistokoodiviite<
    'oppiaineetib',
    | 'BIO'
    | 'BU'
    | 'CHE'
    | 'DAN'
    | 'ECO'
    | 'FIL'
    | 'GEO'
    | 'HIS'
    | 'MAT'
    | 'MATFT'
    | 'MATST'
    | 'MUS'
    | 'PHI'
    | 'PHY'
    | 'POL'
    | 'PSY'
    | 'REL'
    | 'SOC'
    | 'ESS'
    | 'THE'
    | 'VA'
    | 'CS'
  >
}

export type IBOppiaineTheoryOfKnowledge = {
  tunniste: Koodistokoodiviite<'oppiaineetib', 'TOK'>
  pakollinen: boolean
}

export type IBPäätasonSuoritus =
  | IBTutkinnonSuoritus
  | PreIBSuoritus2015
  | PreIBSuoritus2019

export type IBTheoryOfKnowledgeSuoritus = {
  arviointi?: Array<IBCoreRequirementsArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'iboppiainetok'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBOppiaineTheoryOfKnowledge
  osasuoritukset?: Array<IBKurssinSuoritus>
}

export type IBTutkinnonSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ibtutkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  extendedEssay?: IBExtendedEssaySuoritus
  creativityActionService?: IBCASSuoritus
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  lisäpisteet?: Koodistokoodiviite<'arviointiasteikkolisapisteetib', string>
  theoryOfKnowledge?: IBTheoryOfKnowledgeSuoritus
  koulutusmoduuli: IBTutkinto
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<IBOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type IBTutkinto = {
  tunniste: Koodistokoodiviite<'koulutus', '301102'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type InternationalSchoolCoreRequirementsArviointi = {
  predicted?: boolean
  arvosana: Koodistokoodiviite<'arviointiasteikkocorerequirementsib', string>
  päivä?: string
  hyväksytty?: boolean
}

export type InternationalSchoolIBOppiaine =
  | FitnessAndWellBeing
  | InternationalSchoolMuuDiplomaOppiaine
  | KieliDiplomaOppiaine
  | MuuDiplomaOppiaine

export type InternationalSchoolIBOppiaineenArviointi = {
  predicted?: boolean
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoib',
    'S' | 'F' | '1' | '2' | '3' | '4' | '5' | '6' | '7'
  >
  päivä?: string
  hyväksytty?: boolean
}

export type InternationalSchoolMuuDiplomaOppiaine = {
  tunniste: Koodistokoodiviite<
    'oppiaineetinternationalschool',
    'F' | 'HSCM' | 'ITGS' | 'MAA' | 'MAI' | 'INS'
  >
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}

export type InternationalSchoolOpiskeluoikeudenLisätiedot = {
  erityisenKoulutustehtävänJaksot?: Array<ErityisenKoulutustehtävänJakso>
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  maksuttomuus?: Array<Maksuttomuus>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
}

export type InternationalSchoolOpiskeluoikeudenTila = {
  opiskeluoikeusjaksot: Array<InternationalSchoolOpiskeluoikeusjakso>
}

export type InternationalSchoolOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'internationalschool'>
  tila: InternationalSchoolOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: InternationalSchoolOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<InternationalSchoolVuosiluokanSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export type InternationalSchoolOpiskeluoikeusjakso = {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'peruutettu'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '1' | '6'>
}

export type InternationalSchoolVuosiluokanSuoritus =
  | DiplomaVuosiluokanSuoritus
  | MYPVuosiluokanSuoritus
  | PYPVuosiluokanSuoritus

export type ISHDiplomaLuokkaAste = {
  diplomaType: Koodistokoodiviite<'internationalschooldiplomatype', 'ish'>
  tunniste: Koodistokoodiviite<'internationalschoolluokkaaste', '11' | '12'>
}

export type JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa = {
  tunniste: Koodistokoodiviite<'tutkinnonosatvalinnanmahdollisuus', '1'>
  laajuus?: LaajuusOsaamispisteissä
}

export type Järjestämismuoto =
  | JärjestämismuotoIlmanLisätietoja
  | OppisopimuksellinenJärjestämismuoto

export type JärjestämismuotoIlmanLisätietoja = {
  tunniste: Koodistokoodiviite<'jarjestamismuoto', string>
}

export type Järjestämismuotojakso = {
  alku: string
  loppu?: string
  järjestämismuoto: Järjestämismuoto
}

export type KieliDiplomaOppiaine = {
  tunniste: Koodistokoodiviite<'oppiaineetib', 'A' | 'A2' | 'B' | 'AB'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'EN' | 'ES' | 'FI' | 'FR'>
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}

export type Koodistokoodiviite<U extends string, A extends string> = {
  koodistoVersio?: number
  koodiarvo: A
  nimi?: LocalizedString
  lyhytNimi?: LocalizedString
  koodistoUri: U
}

export type KorkeakoulunArviointi =
  | KorkeakoulunKoodistostaLöytyväArviointi
  | KorkeakoulunPaikallinenArviointi

export type KorkeakoulunKoodistostaLöytyväArviointi = {
  arvosana: Koodistokoodiviite<'virtaarvosana', string>
  päivä: string
  hyväksytty?: boolean
}

export type KorkeakoulunOpintojakso = {
  tunniste: PaikallinenKoodi
  nimi: LocalizedString
  laajuus?: Laajuus
}

export type KorkeakoulunOpintojaksonSuoritus = {
  arviointi?: Array<KorkeakoulunArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'korkeakoulunopintojakso'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: KorkeakoulunOpintojakso
  toimipiste: Oppilaitos
  osasuoritukset?: Array<KorkeakoulunOpintojaksonSuoritus>
  vahvistus?: Päivämäärävahvistus
}

export type KorkeakoulunOpiskeluoikeudenLisätiedot = {
  ensisijaisuus?: Array<Aikajakso>
  maksettavatLukuvuosimaksut?: Array<KorkeakoulunOpiskeluoikeudenLukuvuosimaksu>
  järjestäväOrganisaatio?: Oppilaitos
  virtaOpiskeluoikeudenTyyppi?: Koodistokoodiviite<
    'virtaopiskeluoikeudentyyppi',
    string
  >
  lukukausiIlmoittautuminen?: Lukukausi_Ilmoittautuminen
}

export type KorkeakoulunOpiskeluoikeudenLukuvuosimaksu = {
  alku: string
  loppu?: string
  summa?: number
}

export type KorkeakoulunOpiskeluoikeudenTila = {
  opiskeluoikeusjaksot: Array<KorkeakoulunOpiskeluoikeusjakso>
}

export type KorkeakoulunOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'korkeakoulutus'>
  tila: KorkeakoulunOpiskeluoikeudenTila
  alkamispäivä?: string
  oid?: string
  synteettinen: boolean
  koulutustoimija?: Koulutustoimija
  lisätiedot?: KorkeakoulunOpiskeluoikeudenLisätiedot
  virtaVirheet: Array<VirtaVirhe>
  suoritukset: Array<KorkeakouluSuoritus>
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export type KorkeakoulunOpiskeluoikeusjakso = {
  alku: string
  nimi?: LocalizedString
  tila: Koodistokoodiviite<'virtaopiskeluoikeudentila', string>
}

export type KorkeakoulunPaikallinenArviointi = {
  arvosana: PaikallinenKoodi
  päivä: string
  hyväksytty?: boolean
}

export type KorkeakouluopinnotTutkinnonOsa = {
  tunniste: Koodistokoodiviite<'tutkinnonosatvalinnanmahdollisuus', '2'>
  laajuus?: LaajuusOsaamispisteissä
}

export type KorkeakouluopintojenSuoritus = {
  arviointi?: Array<AmmatillinenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillinenkorkeakouluopintoja'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus
  tunnustettu?: OsaamisenTunnustaminen
}

export type KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
}

export type KorkeakouluSuoritus =
  | KorkeakoulunOpintojaksonSuoritus
  | KorkeakoulututkinnonSuoritus
  | MuuKorkeakoulunSuoritus

export type KorkeakoulututkinnonSuoritus = {
  arviointi?: Array<KorkeakoulunArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'korkeakoulututkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: Korkeakoulututkinto
  toimipiste: Oppilaitos
  osasuoritukset?: Array<KorkeakoulunOpintojaksonSuoritus>
  vahvistus?: Päivämäärävahvistus
}

export type Korkeakoulututkinto = {
  tunniste: Koodistokoodiviite<'koulutus', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  virtaNimi?: LocalizedString
}

export type Koulutussopimusjakso = {
  työssäoppimispaikka?: LocalizedString
  työssäoppimispaikanYTunnus?: string
  paikkakunta: Koodistokoodiviite<'kunta', string>
  loppu?: string
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  alku: string
  työtehtävät?: LocalizedString
}

export type Koulutustoimija = {
  oid: string
  nimi?: LocalizedString
  yTunnus?: string
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}

export type Laajuus =
  | LaajuusKaikkiYksiköt
  | LaajuusKursseissa
  | LaajuusOpintopisteissä
  | LaajuusOpintoviikoissa
  | LaajuusOsaamispisteissä
  | LaajuusTunneissa
  | LaajuusViikoissa
  | LaajuusVuosiviikkotunneissa

export type LaajuusKaikkiYksiköt = {
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', string>
}

export type LaajuusKursseissa = {
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '4'>
}

export type LaajuusOpintopisteissä = {
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '2'>
}

export type LaajuusOpintopisteissäTaiKursseissa =
  | LaajuusKursseissa
  | LaajuusOpintopisteissä

export type LaajuusOpintoviikoissa = {
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '1'>
}

export type LaajuusOsaamispisteissä = {
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '6'>
}

export type LaajuusTunneissa = {
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '5'>
}

export type LaajuusViikoissa = {
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '8'>
}

export type LaajuusVuosiviikkotunneissa = {
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '3'>
}

export type LaajuusVuosiviikkotunneissaTaiKursseissa =
  | LaajuusKursseissa
  | LaajuusVuosiviikkotunneissa

export type LanguageAcquisition = {
  tunniste: Koodistokoodiviite<'oppiaineetinternationalschool', 'LAC'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'ES' | 'FI' | 'FR' | 'EN'>
}

export type LanguageAndLiterature = {
  tunniste: Koodistokoodiviite<'oppiaineetinternationalschool', 'LL'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'EN' | 'FI'>
}

export type Lukiodiplomit2019 = {
  tunniste: Koodistokoodiviite<'lukionmuutopinnot', 'LD'>
  laajuus?: LaajuusOpintopisteissä
}

export type LukionArviointi =
  | NumeerinenLukionArviointi
  | SanallinenLukionArviointi

export type LukionKurssi2015 =
  | PaikallinenLukionKurssi2015
  | ValtakunnallinenLukionKurssi2015

export type LukionKurssinSuoritus2015 = {
  arviointi?: Array<LukionArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionkurssi'>
  suoritettuLukiodiplomina?: boolean
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suoritettuSuullisenaKielikokeena?: boolean
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionKurssi2015
  tunnustettu?: OsaamisenTunnustaminen
}

export type LukionMatematiikka2015 = {
  pakollinen: boolean
  oppimäärä: Koodistokoodiviite<'oppiainematematiikka', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'MA'>
}

export type LukionMatematiikka2019 = {
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'MA'>
  oppimäärä: Koodistokoodiviite<'oppiainematematiikka', string>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}

export type LukionModuuliMuissaOpinnoissa2019 =
  | LukionMuuModuuliMuissaOpinnoissa2019
  | LukionVieraanKielenModuuliMuissaOpinnoissa2019

export type LukionModuulinSuoritusMuissaOpinnoissa2019 = {
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionvaltakunnallinenmoduuli'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionModuuliMuissaOpinnoissa2019
  tunnustettu?: OsaamisenTunnustaminen
}

export type LukionModuulinSuoritusOppiaineissa2019 = {
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionvaltakunnallinenmoduuli'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionModuuliOppiaineissa2019
  tunnustettu?: OsaamisenTunnustaminen
}

export type LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 =
  | NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019
  | SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019

export type LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 =

    | LukionModuulinSuoritusMuissaOpinnoissa2019
    | LukionPaikallisenOpintojaksonSuoritus2019

export type LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 =
  | LukionModuulinSuoritusOppiaineissa2019
  | LukionPaikallisenOpintojaksonSuoritus2019

export type LukionModuuliOppiaineissa2019 =
  | LukionMuuModuuliOppiaineissa2019
  | LukionVieraanKielenModuuliOppiaineissa2019

export type LukionMuuModuuliMuissaOpinnoissa2019 = {
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
}

export type LukionMuuModuuliOppiaineissa2019 = {
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
}

export type LukionMuuValtakunnallinenOppiaine2015 = {
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    | 'HI'
    | 'MU'
    | 'BI'
    | 'PS'
    | 'ET'
    | 'KO'
    | 'FI'
    | 'KE'
    | 'YH'
    | 'TE'
    | 'KS'
    | 'FY'
    | 'GE'
    | 'LI'
    | 'KU'
    | 'OP'
  >
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
}

export type LukionMuuValtakunnallinenOppiaine2019 = {
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    | 'BI'
    | 'ET'
    | 'FI'
    | 'FY'
    | 'GE'
    | 'HI'
    | 'KE'
    | 'KU'
    | 'LI'
    | 'MU'
    | 'OP'
    | 'PS'
    | 'TE'
    | 'YH'
  >
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}

export type LukionOpiskeluoikeudenLisätiedot = {
  alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy?: LocalizedString
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  oikeusMaksuttomaanAsuntolapaikkaan?: boolean
  maksuttomuus?: Array<Maksuttomuus>
  ulkomainenVaihtoopiskelija?: boolean
  erityisenKoulutustehtävänJaksot?: Array<ErityisenKoulutustehtävänJakso>
  yksityisopiskelija?: boolean
  pidennettyPäättymispäivä?: boolean
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export type LukionOpiskeluoikeudenTila = {
  opiskeluoikeusjaksot: Array<LukionOpiskeluoikeusjakso>
}

export type LukionOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'lukiokoulutus'>
  tila: LukionOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: LukionOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<LukionPäätasonSuoritus>
  oppimääräSuoritettu?: boolean
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export type LukionOpiskeluoikeusjakso = {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'peruutettu'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '1' | '6'>
}

export type LukionOppiaine2015 =
  | LukionMatematiikka2015
  | LukionMuuValtakunnallinenOppiaine2015
  | LukionUskonto2015
  | LukionÄidinkieliJaKirjallisuus2015
  | PaikallinenLukionOppiaine2015
  | VierasTaiToinenKotimainenKieli2015

export type LukionOppiaine2019 =
  | LukionMatematiikka2019
  | LukionMuuValtakunnallinenOppiaine2019
  | LukionUskonto2019
  | LukionÄidinkieliJaKirjallisuus2019
  | PaikallinenLukionOppiaine2019
  | VierasTaiToinenKotimainenKieli2019

export type LukionOppiaineenArviointi = {
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', string>
  päivä?: string
  hyväksytty?: boolean
}

export type LukionOppiaineenArviointi2019 =
  | NumeerinenLukionOppiaineenArviointi2019
  | SanallinenLukionOppiaineenArviointi2019

export type LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa =
  {
    arviointi?: Array<LukionOppiaineenArviointi>
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'luvalukionoppiaine'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    koulutusmoduuli: LukionOppiaine2015
    osasuoritukset?: Array<LukionKurssinSuoritus2015>
  }

export type LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019 =
  {
    arviointi?: Array<LukionOppiaineenArviointi2019>
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'luvalukionoppiaine2019'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suoritettuErityisenäTutkintona?: boolean
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    koulutusmoduuli: LukionOppiaine2019
    osasuoritukset?: Array<LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019>
  }

export type LukionOppiaineenOppimääränSuoritus2015 = {
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaineenoppimaara'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  lukionOppimääräSuoritettu?: boolean
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli: LukionOppiaineTaiEiTiedossaOppiaine2015
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukionKurssinSuoritus2015>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type LukionOppiaineenPreIBSuoritus2019 = {
  arviointi?: Array<LukionOppiaineenArviointi2019>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suoritettuErityisenäTutkintona?: boolean
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBLukionOppiaine2019
  osasuoritukset?: Array<PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019>
}

export type LukionOppiaineenSuoritus2015 = {
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionOppiaine2015
  osasuoritukset?: Array<LukionKurssinSuoritus2015>
}

export type LukionOppiaineenSuoritus2019 = {
  arviointi?: Array<LukionOppiaineenArviointi2019>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suoritettuErityisenäTutkintona?: boolean
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionOppiaine2019
  osasuoritukset?: Array<LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019>
}

export type LukionOppiaineidenOppimäärienSuoritus2019 = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionaineopinnot'>
  suullisenKielitaidonKokeet?: Array<SuullisenKielitaidonKoe2019>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  puhviKoe?: PuhviKoe2019
  oppimäärä: Koodistokoodiviite<'lukionoppimaara', string>
  lukionOppimääräSuoritettu?: boolean
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli: LukionOppiaineidenOppimäärät2019
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukionOppiaineenSuoritus2019>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type LukionOppiaineidenOppimäärät2019 = {
  tunniste: LukionOppiaineidenOppimäärätKoodi2019
  perusteenDiaarinumero?: string
}

export type LukionOppiaineidenOppimäärätKoodi2019 = {
  koodiarvo?: string
}

export type LukionOppiaineTaiEiTiedossaOppiaine2015 =
  | EiTiedossaOppiaine
  | LukionMatematiikka2015
  | LukionMuuValtakunnallinenOppiaine2015
  | LukionUskonto2015
  | LukionÄidinkieliJaKirjallisuus2015
  | PaikallinenLukionOppiaine2015
  | VierasTaiToinenKotimainenKieli2015

export type LukionOppimäärä = {
  tunniste: Koodistokoodiviite<'koulutus', '309902'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type LukionOppimääränOsasuoritus2015 =
  | LukionOppiaineenSuoritus2015
  | MuidenLukioOpintojenSuoritus2015

export type LukionOppimääränOsasuoritus2019 =
  | LukionOppiaineenSuoritus2019
  | MuidenLukioOpintojenSuoritus2019

export type LukionOppimääränSuoritus2015 = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppimaara'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusKursseina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  oppimäärä: Koodistokoodiviite<'lukionoppimaara', string>
  koulusivistyskieli?: Array<Koodistokoodiviite<'kieli', 'FI' | 'SV'>>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli: LukionOppimäärä
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukionOppimääränOsasuoritus2015>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type LukionOppimääränSuoritus2019 = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppimaara'>
  suullisenKielitaidonKokeet?: Array<SuullisenKielitaidonKoe2019>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusOpintopisteinä
  suoritettuErityisenäTutkintona?: boolean
  suorituskieli: Koodistokoodiviite<'kieli', string>
  puhviKoe?: PuhviKoe2019
  oppimäärä: Koodistokoodiviite<'lukionoppimaara', string>
  koulusivistyskieli?: Array<Koodistokoodiviite<'kieli', 'FI' | 'SV'>>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli: LukionOppimäärä
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukionOppimääränOsasuoritus2019>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type LukionPaikallinenOpintojakso2019 = {
  tunniste: PaikallinenKoodi
  laajuus: LaajuusOpintopisteissä
  kuvaus: LocalizedString
  pakollinen: boolean
}

export type LukionPaikallisenOpintojaksonSuoritus2019 = {
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionpaikallinenopintojakso'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionPaikallinenOpintojakso2019
  tunnustettu?: OsaamisenTunnustaminen
}

export type LukionPäätasonSuoritus =
  | LukionOppiaineenOppimääränSuoritus2015
  | LukionOppiaineidenOppimäärienSuoritus2019
  | LukionOppimääränSuoritus2015
  | LukionOppimääränSuoritus2019

export type LukionUskonto2015 = {
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
}

export type LukionUskonto2019 = {
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
}

export type LukionVieraanKielenModuuliMuissaOpinnoissa2019 = {
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
}

export type LukionVieraanKielenModuuliOppiaineissa2019 = {
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
  kieli?: Koodistokoodiviite<'kielivalikoima', string>
}

export type LukionÄidinkieliJaKirjallisuus2015 = {
  pakollinen: boolean
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
}

export type LukionÄidinkieliJaKirjallisuus2019 = {
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}

export type LukioonValmistavaKoulutus = {
  tunniste: Koodistokoodiviite<'koulutus', '999906'>
  perusteenDiaarinumero?: string
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type LukioonValmistavanKoulutuksenKurssi =
  | PaikallinenLukioonValmistavanKoulutuksenKurssi
  | ValtakunnallinenLukioonValmistavanKoulutuksenKurssi

export type LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot = {
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  oikeusMaksuttomaanAsuntolapaikkaan?: boolean
  maksuttomuus?: Array<Maksuttomuus>
  ulkomainenVaihtoopiskelija?: boolean
  pidennettyPäättymispäivä?: boolean
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export type LukioonValmistavanKoulutuksenOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'luva'>
  tila: LukionOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<LukioonValmistavanKoulutuksenSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export type LukioonValmistavanKoulutuksenOppiaine =
  | LukioonValmistavaÄidinkieliJaKirjallisuus
  | MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine
  | MuutKielet
  | PaikallinenLukioonValmistavanKoulutuksenOppiaine

export type LukioonValmistavanKoulutuksenOppiaineenSuoritus = {
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'luvaoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukioonValmistavanKoulutuksenOppiaine
  osasuoritukset?: Array<LukioonValmistavanKurssinSuoritus>
}

export type LukioonValmistavanKoulutuksenOsasuoritus =
  | LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa
  | LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019
  | LukioonValmistavanKoulutuksenOppiaineenSuoritus

export type LukioonValmistavanKoulutuksenSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'luva'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  oppimäärä: Koodistokoodiviite<'lukionoppimaara', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: LukioonValmistavaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<LukioonValmistavanKoulutuksenOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type LukioonValmistavanKurssinSuoritus = {
  arviointi?: Array<LukionArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'luvakurssi'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukioonValmistavanKoulutuksenKurssi
}

export type LukioonValmistavaÄidinkieliJaKirjallisuus = {
  tunniste: Koodistokoodiviite<'oppiaineetluva', 'LVAIK'>
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', 'AI7' | 'AI8'>
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}

export type LukioOpintojenSuoritus = {
  arviointi?: Array<AmmatillinenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillinenlukionopintoja'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: PaikallinenLukionOpinto
  tunnustettu?: OsaamisenTunnustaminen
}

export type Lukukausi_Ilmoittautuminen = {
  ilmoittautumisjaksot: Array<Lukukausi_Ilmoittautumisjakso>
}

export type Lukukausi_Ilmoittautumisjakso = {
  tila: Koodistokoodiviite<'virtalukukausiilmtila', string>
  maksetutLukuvuosimaksut?: Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu
  ylioppilaskunnanJäsen?: boolean
  ythsMaksettu?: boolean
  loppu?: string
  alku: string
}

export type LukutaitokoulutuksenArviointi = {
  arvosana: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
  päivä: string
  taitotaso: Koodistokoodiviite<
    'arviointiasteikkokehittyvankielitaidontasot',
    | 'A1.1'
    | 'A1.2'
    | 'A1.3'
    | 'A2.1'
    | 'A2.2'
    | 'B1.1'
    | 'B1.2'
    | 'B2.1'
    | 'B2.2'
    | 'C1.1'
    | 'C1.2'
    | 'C2.1'
    | 'C2.2'
  >
  hyväksytty?: boolean
}

export type Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu = {
  maksettu?: boolean
  summa?: number
  apuraha?: number
}

export type LähdejärjestelmäId = {
  id?: string
  lähdejärjestelmä: Koodistokoodiviite<'lahdejarjestelma', string>
}

export type Maksuttomuus = {
  alku: string
  loppu?: string
  maksuton: boolean
}

export type MuidenLukioOpintojenPreIBSuoritus2019 = {
  koulutusmoduuli: PreIBMuutSuorituksetTaiVastaavat2019
  osasuoritukset?: Array<PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionmuuopinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type MuidenLukioOpintojenSuoritus2015 = {
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionmuuopinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: MuuLukioOpinto2015
  osasuoritukset?: Array<LukionKurssinSuoritus2015>
}

export type MuidenLukioOpintojenSuoritus2019 = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionmuuopinto'>
  koulutusmoduuli: MuutSuorituksetTaiVastaavat2019
  osasuoritukset?: Array<LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type MuidenOpintovalmiuksiaTukevienOpintojenSuoritus = {
  arviointi?: Array<AmmatillinenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillinenmuitaopintovalmiuksiatukeviaopintoja'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: PaikallinenOpintovalmiuksiaTukevaOpinto
  tunnustettu?: OsaamisenTunnustaminen
}

export type MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine = {
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenalkuvaiheenoppiaineet',
    'MA' | 'YH' | 'YL' | 'TE' | 'OP'
  >
}

export type MuuAikuistenPerusopetuksenOppiaine = {
  pakollinen: boolean
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    | 'OPA'
    | 'HI'
    | 'MU'
    | 'BI'
    | 'PS'
    | 'ET'
    | 'KO'
    | 'FI'
    | 'KE'
    | 'YH'
    | 'TE'
    | 'KS'
    | 'FY'
    | 'GE'
    | 'LI'
    | 'KU'
    | 'MA'
    | 'YL'
    | 'OP'
  >
}

export type MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus =
  {
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmuuallasuoritetutopinnot'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: MuuallaSuoritetutVapaanSivistystyönOpinnot
    tunnustettu?: VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen
  }

export type MuuallaSuoritetutVapaanSivistystyönOpinnot = {
  tunniste: Koodistokoodiviite<'vstmuuallasuoritetutopinnot', string>
  kuvaus: LocalizedString
  laajuus: LaajuusOpintopisteissä
}

export type MuuAmmatillinenKoulutus =
  | AmmatilliseenTehtäväänValmistavaKoulutus
  | PaikallinenMuuAmmatillinenKoulutus

export type MuuAmmatillinenOsasuoritus =
  | MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus
  | TutkinnonOsaaPienemmänKokonaisuudenSuoritus
  | YhteisenTutkinnonOsanOsaAlueenSuoritus

export type MuuDiplomaOppiaine = {
  tunniste: Koodistokoodiviite<
    'oppiaineetib',
    | 'BIO'
    | 'CHE'
    | 'ECO'
    | 'ESS'
    | 'HIS'
    | 'MAT'
    | 'MATST'
    | 'PHY'
    | 'PSY'
    | 'VA'
  >
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}

export type MuuKorkeakoulunOpinto = {
  tunniste: Koodistokoodiviite<'virtaopiskeluoikeudentyyppi', string>
  nimi: LocalizedString
  laajuus?: Laajuus
}

export type MuuKorkeakoulunSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'muukorkeakoulunsuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuuKorkeakoulunOpinto
  toimipiste: Oppilaitos
  osasuoritukset?: Array<KorkeakoulunOpintojaksonSuoritus>
  vahvistus?: Päivämäärävahvistus
}

export type MuuKuinSäänneltyKoulutus = {
  tunniste: Koodistokoodiviite<'koulutus', '999951'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusTunneissa
  opintokokonaisuus: Koodistokoodiviite<'opintokokonaisuudet', string>
}

export type MuuKuinYhteinenTutkinnonOsa =
  | MuuValtakunnallinenTutkinnonOsa
  | PaikallinenTutkinnonOsa

export type MuuLukioOpinto2015 = {
  tunniste: Koodistokoodiviite<'lukionmuutopinnot', string>
  laajuus?: LaajuusKursseissa
}

export type MuunAmmatillisenKoulutuksenArviointi = {
  päivä: string
  arvosana: Koodistokoodiviite<
    | 'arviointiasteikkomuuammatillinenkoulutus'
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}

export type MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto = {
  tunniste: Koodistokoodiviite<'ammatillisentutkinnonosanlisatieto', string>
  kuvaus: LocalizedString
}

export type MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus = {
  arviointi?: Array<MuunAmmatillisenKoulutuksenArviointi>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'muunammatillisenkoulutuksenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuunAmmatillisenKoulutuksenOsasuoritus
  osasuoritukset?: Array<MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus>
}

export type MuunAmmatillisenKoulutuksenOsasuoritus = {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}

export type MuunAmmatillisenKoulutuksenSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'muuammatillinenkoulutus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  täydentääTutkintoa?: AmmatillinenTutkintoKoulutus
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  koulutusmoduuli: MuuAmmatillinenKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<MuuAmmatillinenOsasuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export type MuunAmmatillisenTutkinnonOsanSuoritus = {
  arviointi?: Array<AmmatillinenArviointi>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: MuuKuinYhteinenTutkinnonOsa
  tunnustettu?: OsaamisenTunnustaminen
  toimipiste?: OrganisaatioWithOid
  tutkinnonOsanRyhmä?: Koodistokoodiviite<
    'ammatillisentutkinnonosanryhma',
    '1' | '3' | '4'
  >
  osasuoritukset?: Array<AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus>
  tutkinto?: AmmatillinenTutkintoKoulutus
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
}

export type MuunKuinSäännellynKoulutuksenArviointi = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkomuks',
    'hyvaksytty' | 'hylatty'
  >
  arviointipäivä?: string
  hyväksytty?: boolean
}

export type MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso = {
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'lasna' | 'hyvaksytystisuoritettu' | 'keskeytynyt' | 'mitatoity'
  >
  alku: string
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '14' | '15'>
}

export type MuunKuinSäännellynKoulutuksenOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<
    'opiskeluoikeudentyyppi',
    'muukuinsaanneltykoulutus'
  >
  tila: MuunKuinSäännellynKoulutuksenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<MuunKuinSäännellynKoulutuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export type MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli = {
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  laajuus: LaajuusTunneissa
}

export type MuunKuinSäännellynKoulutuksenOsasuoritus = {
  arviointi?: Array<MuunKuinSäännellynKoulutuksenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'muunkuinsaannellynkoulutuksenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli
  osasuoritukset?: Array<MuunKuinSäännellynKoulutuksenOsasuoritus>
  vahvistus?: Vahvistus
}

export type MuunKuinSäännellynKoulutuksenPäätasonSuoritus = {
  arviointi?: Array<MuunKuinSäännellynKoulutuksenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'muukuinsaanneltykoulutus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuuKuinSäänneltyKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<MuunKuinSäännellynKoulutuksenOsasuoritus>
  vahvistus?: Päivämäärävahvistus
}

export type MuunKuinSäännellynKoulutuksenTila = {
  opiskeluoikeusjaksot: Array<MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso>
}

export type MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus = {
  arviointi?: Array<AmmatillinenArviointi>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: MuuKuinYhteinenTutkinnonOsa
  tunnustettu?: OsaamisenTunnustaminen
  toimipiste?: OrganisaatioWithOid
  tutkinnonOsanRyhmä?: Koodistokoodiviite<
    'ammatillisentutkinnonosanryhma',
    '1' | '3' | '4'
  >
  osasuoritukset?: Array<AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus>
  tutkinto?: AmmatillinenTutkintoKoulutus
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
}

export type MuuNuortenPerusopetuksenOppiaine = {
  pakollinen: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    | 'HI'
    | 'MU'
    | 'BI'
    | 'PS'
    | 'ET'
    | 'KO'
    | 'FI'
    | 'KE'
    | 'YH'
    | 'TE'
    | 'KS'
    | 'FY'
    | 'GE'
    | 'LI'
    | 'KU'
    | 'MA'
    | 'YL'
    | 'OP'
  >
}

export type MuuPerusopetuksenLisäopetuksenKoulutusmoduuli = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusVuosiviikkotunneissa
}

export type MuuPerusopetuksenLisäopetuksenSuoritus = {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'muuperusopetuksenlisaopetuksensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuuPerusopetuksenLisäopetuksenKoulutusmoduuli
}

export type MuutKielet = {
  tunniste: Koodistokoodiviite<
    'oppiaineetluva',
    'LVMUUTK' | 'LVAK' | 'LVMAI' | 'LVPOAK'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}

export type MuutLukionSuoritukset2019 = {
  tunniste: Koodistokoodiviite<'lukionmuutopinnot', 'MS'>
  laajuus?: LaajuusOpintopisteissä
}

export type MuutSuorituksetTaiVastaavat2019 =
  | Lukiodiplomit2019
  | MuutLukionSuoritukset2019
  | TemaattisetOpinnot2019

export type MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine = {
  tunniste: Koodistokoodiviite<
    'oppiaineetluva',
    'LVMALUO' | 'LVYHKU' | 'LVOPO' | 'LVMFKBM' | 'LVHIYH'
  >
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}

export type MuuValtakunnallinenTutkinnonOsa = {
  tunniste: Koodistokoodiviite<'tutkinnonosat', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
  kuvaus?: LocalizedString
}

export type MYPArviointi =
  | NumeerinenInternationalSchoolOppiaineenArviointi
  | PassFailOppiaineenArviointi

export type MYPLuokkaAste = {
  tunniste: Koodistokoodiviite<
    'internationalschoolluokkaaste',
    '6' | '7' | '8' | '9' | '10'
  >
}

export type MYPOppiaine =
  | LanguageAcquisition
  | LanguageAndLiterature
  | MYPOppiaineMuu

export type MYPOppiaineenSuoritus = {
  arviointi?: Array<MYPArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolmypoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MYPOppiaine
}

export type MYPOppiaineMuu = {
  tunniste: Koodistokoodiviite<
    'oppiaineetinternationalschool',
    | 'AD'
    | 'DE'
    | 'DR'
    | 'EAL'
    | 'EMA'
    | 'ILS'
    | 'IS'
    | 'MA'
    | 'ME'
    | 'MU'
    | 'PHE'
    | 'PP'
    | 'SCI'
    | 'SMA'
    | 'VA'
    | 'INS'
    | 'MF'
  >
}

export type MYPVuosiluokanSuoritus = {
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolmypvuosiluokka'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  koulutusmoduuli: MYPLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<MYPOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type NumeerinenInternationalSchoolOppiaineenArviointi = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoib',
    'S' | 'F' | '1' | '2' | '3' | '4' | '5' | '6' | '7'
  >
  päivä?: string
  hyväksytty?: boolean
}

export type NumeerinenLukionArviointi = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä: string
  hyväksytty?: boolean
}

export type NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä: string
  hyväksytty?: boolean
}

export type NumeerinenLukionOppiaineenArviointi2019 = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä?: string
  hyväksytty?: boolean
}

export type NumeerinenPerusopetuksenOppiaineenArviointi = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä?: string
  hyväksytty?: boolean
}

export type NuortenPerusopetuksenOpiskeluoikeudenTila = {
  opiskeluoikeusjaksot: Array<NuortenPerusopetuksenOpiskeluoikeusjakso>
}

export type NuortenPerusopetuksenOpiskeluoikeusjakso = {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'peruutettu'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
}

export type NuortenPerusopetuksenOppiaine =
  | MuuNuortenPerusopetuksenOppiaine
  | NuortenPerusopetuksenPaikallinenOppiaine
  | NuortenPerusopetuksenUskonto
  | NuortenPerusopetuksenVierasTaiToinenKotimainenKieli
  | NuortenPerusopetuksenÄidinkieliJaKirjallisuus

export type NuortenPerusopetuksenOppiaineenOppimääränSuoritus = {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'nuortenperusopetuksenoppiaineenoppimaara'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  luokkaAste?: Koodistokoodiviite<'perusopetuksenluokkaaste', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine
  toimipiste: OrganisaatioWithOid
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type NuortenPerusopetuksenOppiaineenSuoritus = {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksenoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  suoritustapa?: Koodistokoodiviite<
    'perusopetuksensuoritustapa',
    'erityinentutkinto'
  >
  painotettuOpetus: boolean
  koulutusmoduuli: NuortenPerusopetuksenOppiaine
  yksilöllistettyOppimäärä?: boolean
}

export type NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa = {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetuksenoppiaineperusopetukseenvalmistavassaopetuksessa'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  luokkaAste?: Koodistokoodiviite<'perusopetuksenluokkaaste', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  suoritustapa?: Koodistokoodiviite<
    'perusopetuksensuoritustapa',
    'erityinentutkinto'
  >
  koulutusmoduuli: NuortenPerusopetuksenOppiaine
}

export type NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine =
  | EiTiedossaOppiaine
  | MuuNuortenPerusopetuksenOppiaine
  | NuortenPerusopetuksenPaikallinenOppiaine
  | NuortenPerusopetuksenUskonto
  | NuortenPerusopetuksenVierasTaiToinenKotimainenKieli
  | NuortenPerusopetuksenÄidinkieliJaKirjallisuus

export type NuortenPerusopetuksenOppimääränSuoritus = {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksenoppimaara'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  koulusivistyskieli?: Array<Koodistokoodiviite<'kieli', 'FI' | 'SV'>>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: NuortenPerusopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OppiaineenTaiToiminta_AlueenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type NuortenPerusopetuksenPaikallinenOppiaine = {
  pakollinen?: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: PaikallinenKoodi
}

export type NuortenPerusopetuksenUskonto = {
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
}

export type NuortenPerusopetuksenVierasTaiToinenKotimainenKieli = {
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    'A1' | 'A2' | 'B1' | 'B2' | 'B3' | 'AOM'
  >
}

export type NuortenPerusopetuksenÄidinkieliJaKirjallisuus = {
  pakollinen: boolean
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
}

export type NuortenPerusopetus = {
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koulutus', '201101'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type NurseryLuokkaAste = {
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'N1' | 'N2'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
}

export type NurseryVuosiluokanSuoritus = {
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkivuosiluokkanursery'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle?: boolean
  koulutusmoduuli: NurseryLuokkaAste
  toimipiste: OrganisaatioWithOid
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type Näyttö = {
  arviointi?: NäytönArviointi
  suorituspaikka?: NäytönSuorituspaikka
  haluaaTodistuksen?: boolean
  työssäoppimisenYhteydessä?: boolean
  kuvaus?: LocalizedString
  suoritusaika?: NäytönSuoritusaika
}

export type NäyttötutkintoonValmistavaKoulutus = {
  tunniste: Koodistokoodiviite<'koulutus', '999904'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type NäyttötutkintoonValmistavanKoulutuksenOsa =
  | MuuValtakunnallinenTutkinnonOsa
  | PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa
  | YhteinenTutkinnonOsa

export type NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus = {
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'nayttotutkintoonvalmistavankoulutuksenosa'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: NäyttötutkintoonValmistavanKoulutuksenOsa
}

export type NäyttötutkintoonValmistavanKoulutuksenSuoritus = {
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'nayttotutkintoonvalmistavakoulutus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  päättymispäivä?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: NäyttötutkintoonValmistavaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus>
  tutkinto: AmmatillinenTutkintoKoulutus
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export type NäytönArviointi = {
  päivä: string
  arvosana: Koodistokoodiviite<
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
  arvioinnistaPäättäneet?: Array<
    Koodistokoodiviite<'ammatillisennaytonarvioinnistapaattaneet', string>
  >
  hylkäyksenPeruste?: LocalizedString
  hyväksytty?: boolean
  arviointikeskusteluunOsallistuneet?: Array<
    Koodistokoodiviite<
      'ammatillisennaytonarviointikeskusteluunosallistuneet',
      string
    >
  >
  arvioitsijat?: Array<NäytönArvioitsija>
  arviointikohteet?: Array<NäytönArviointikohde>
}

export type NäytönArviointikohde = {
  tunniste: Koodistokoodiviite<'ammatillisennaytonarviointikohde', string>
  arvosana: Koodistokoodiviite<
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
}

export type NäytönArvioitsija = {
  nimi: string
  ntm?: boolean
}

export type NäytönSuoritusaika = {
  alku: string
  loppu: string
}

export type NäytönSuorituspaikka = {
  tunniste: Koodistokoodiviite<'ammatillisennaytonsuorituspaikka', string>
  kuvaus: LocalizedString
}

export type OidHenkilö = {
  oid: string
}

export type OidOrganisaatio = {
  oid: string
  nimi?: LocalizedString
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}

export type OikeuttaMaksuttomuuteenPidennetty = {
  alku: string
  loppu: string
}

export type OmanÄidinkielenOpinnotLaajuusKursseina = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'O' | '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  hyväksytty?: boolean
  laajuus?: LaajuusKursseissa
  arviointipäivä?: string
}

export type OmanÄidinkielenOpinnotLaajuusOpintopisteinä = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'O' | '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  hyväksytty?: boolean
  laajuus: LaajuusOpintopisteissä
  arviointipäivä?: string
}

export type OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'O' | '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  hyväksytty?: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  arviointipäivä?: string
}

export type OpiskeluoikeudenOrganisaatiohistoria = {
  muutospäivä: string
  oppilaitos?: Oppilaitos
  koulutustoimija?: Koulutustoimija
}

export type Opiskeluoikeus =
  | AikuistenPerusopetuksenOpiskeluoikeus
  | AmmatillinenOpiskeluoikeus
  | DIAOpiskeluoikeus
  | EsiopetuksenOpiskeluoikeus
  | EuropeanSchoolOfHelsinkiOpiskeluoikeus
  | IBOpiskeluoikeus
  | InternationalSchoolOpiskeluoikeus
  | KorkeakoulunOpiskeluoikeus
  | LukionOpiskeluoikeus
  | LukioonValmistavanKoulutuksenOpiskeluoikeus
  | MuunKuinSäännellynKoulutuksenOpiskeluoikeus
  | PerusopetukseenValmistavanOpetuksenOpiskeluoikeus
  | PerusopetuksenLisäopetuksenOpiskeluoikeus
  | PerusopetuksenOpiskeluoikeus
  | TutkintokoulutukseenValmentavanOpiskeluoikeus
  | VapaanSivistystyönOpiskeluoikeus
  | YlioppilastutkinnonOpiskeluoikeus

export type OpiskeluoikeusAvaintaEiLöydy = {
  tyyppi: string
  arvo: string
}

export type OpiskeluvalmiuksiaTukevienOpintojenJakso = {
  alku: string
  loppu: string
  kuvaus: LocalizedString
}

export type OppiaineenTaiToiminta_AlueenSuoritus =
  | NuortenPerusopetuksenOppiaineenSuoritus
  | PerusopetuksenToiminta_AlueenSuoritus

export type Oppilaitos = {
  oid: string
  oppilaitosnumero?: Koodistokoodiviite<'oppilaitosnumero', string>
  nimi?: LocalizedString
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}

export type OppisopimuksellinenJärjestämismuoto = {
  tunniste: Koodistokoodiviite<'jarjestamismuoto', '20'>
  oppisopimus: Oppisopimus
}

export type OppisopimuksellinenOsaamisenHankkimistapa = {
  tunniste: Koodistokoodiviite<'osaamisenhankkimistapa', 'oppisopimus'>
  oppisopimus: Oppisopimus
}

export type OppisopimuksenPurkaminen = {
  päivä: string
  purettuKoeajalla: boolean
}

export type Oppisopimus = {
  työnantaja: Yritys
  oppisopimuksenPurkaminen?: OppisopimuksenPurkaminen
}

export type OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =
  {
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli: Koodistokoodiviite<'kieli', string>
    todistuksellaNäkyvätLisätiedot?: LocalizedString
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
    toimipiste: OrganisaatioWithOid
    osasuoritukset?: Array<VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus>
    vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
  }

export type OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 =
  {
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli: Koodistokoodiviite<'kieli', string>
    todistuksellaNäkyvätLisätiedot?: LocalizedString
    koulutusmoduuli: VSTKotoutumiskoulutus2022
    toimipiste: OrganisaatioWithOid
    osasuoritukset?: Array<VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022>
    vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
  }

export type OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus = {
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstoppivelvollisillesuunnattukoulutus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export type OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus = {
  tunniste: Koodistokoodiviite<'koulutus', '999909'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus: LaajuusOpintopisteissä
}

export type OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso = {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'lasna'
    | 'valiaikaisestikeskeytynyt'
    | 'katsotaaneronneeksi'
    | 'valmistunut'
    | 'mitatoity'
  >
}

export type OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus = {
  tunniste: Koodistokoodiviite<'vstosaamiskokonaisuus', string>
  laajuus?: LaajuusOpintopisteissä
}

export type OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi =
  {
    arvosana: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
    päivä: string
    hyväksytty?: boolean
  }

export type OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus =
  {
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'vstopintokokonaisuus'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus
    tunnustettu?: VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen
  }

export type OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =
  {
    koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus
    osasuoritukset?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus>
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'vstosaamiskokonaisuus'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export type OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus =
  | OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus
  | OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus

export type OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot =
  {
    tunniste: Koodistokoodiviite<
      'vstmuutopinnot',
      'valinnaisetsuuntautumisopinnot'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export type OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus =
  {
    koulutusmoduuli: OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot
    osasuoritukset?: Array<VapaanSivistystyönOpintokokonaisuudenSuoritus>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstvalinnainensuuntautuminen'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export type Organisaatio =
  | Koulutustoimija
  | OidOrganisaatio
  | Oppilaitos
  | Toimipiste
  | Tutkintotoimikunta
  | Yritys

export type Organisaatiohenkilö = {
  nimi: string
  titteli: LocalizedString
  organisaatio: Organisaatio
}

export type OrganisaatiohenkilöValinnaisellaTittelillä = {
  nimi: string
  titteli?: LocalizedString
  organisaatio: Organisaatio
}

export type Organisaatiovahvistus = {
  päivä: string
  paikkakunta: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio: Organisaatio
}

export type OrganisaatioWithOid =
  | Koulutustoimija
  | OidOrganisaatio
  | Oppilaitos
  | Toimipiste

export type OsaAikaisuusJakso = {
  alku: string
  loppu?: string
  osaAikaisuus: number
}

export type Osaamisalajakso =
  | {
      osaamisala: Koodistokoodiviite<'osaamisala', string>
      alku?: string
      loppu?: string
    }
  | Koodistokoodiviite<'osaamisala', string>

export type OsaamisenHankkimistapa =
  | OppisopimuksellinenOsaamisenHankkimistapa
  | OsaamisenHankkimistapaIlmanLisätietoja

export type OsaamisenHankkimistapaIlmanLisätietoja = {
  tunniste: Koodistokoodiviite<'osaamisenhankkimistapa', string>
}

export type OsaamisenHankkimistapajakso = {
  alku: string
  loppu?: string
  osaamisenHankkimistapa: OsaamisenHankkimistapa
}

export type OsaamisenTunnustaminen = {
  osaaminen?: Suoritus
  selite: LocalizedString
  rahoituksenPiirissä?: boolean
}

export type OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =
  {
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa
    tutkinnonOsanRyhmä?: Koodistokoodiviite<
      'ammatillisentutkinnonosanryhma',
      '1'
    >
    osasuoritukset?: Array<YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus>
  }

export type OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: KorkeakouluopinnotTutkinnonOsa
  tutkinnonOsanRyhmä?: Koodistokoodiviite<'ammatillisentutkinnonosanryhma', '1'>
  osasuoritukset?: Array<KorkeakouluopintojenSuoritus>
}

export type OsittaisenAmmatillisenTutkinnonOsanSuoritus =
  | MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
  | OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
  | OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
  | YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus

export type PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi = {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export type PaikallinenAikuistenPerusopetuksenKurssi = {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export type PaikallinenAmmatillisenTutkinnonOsanOsaAlue = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export type PaikallinenKoodi = {
  koodiarvo: string
  nimi: LocalizedString
  koodistoUri?: string
}

export type PaikallinenLukionKurssi2015 = {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKursseissa
  kuvaus: LocalizedString
  kurssinTyyppi: Koodistokoodiviite<'lukionkurssintyyppi', string>
}

export type PaikallinenLukionOpinto = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
  perusteenDiaarinumero: string
}

export type PaikallinenLukionOppiaine2015 = {
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
  kuvaus: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: PaikallinenKoodi
}

export type PaikallinenLukionOppiaine2019 = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}

export type PaikallinenLukioonValmistavanKoulutuksenKurssi = {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
  kuvaus: LocalizedString
}

export type PaikallinenLukioonValmistavanKoulutuksenOppiaine = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}

export type PaikallinenMuuAmmatillinenKoulutus = {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}

export type PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
}

export type PaikallinenOpintovalmiuksiaTukevaOpinto = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
}

export type PaikallinenTelmaKoulutuksenOsa = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
  pakollinen: boolean
}

export type PaikallinenTutkinnonOsa = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export type PaikallinenValmaKoulutuksenOsa = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
  pakollinen: boolean
}

export type PassFailOppiaineenArviointi = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkointernationalschool',
    'pass' | 'fail'
  >
  päivä?: string
  hyväksytty?: boolean
}

export type PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila = {
  opiskeluoikeusjaksot: Array<PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso>
}

export type PerusopetukseenValmistavanOpetuksenOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<
    'opiskeluoikeudentyyppi',
    'perusopetukseenvalmistavaopetus'
  >
  tila: PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<PerusopetukseenValmistavanOpetuksenSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  oppilaitos?: Oppilaitos
}

export type PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso = {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'loma'
    | 'eronnut'
    | 'peruutettu'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
  >
}

export type PerusopetukseenValmistavanOpetuksenOppiaine = {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  opetuksenSisältö?: LocalizedString
}

export type PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus = {
  arviointi?: Array<SanallinenPerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetukseenvalmistavanopetuksenoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PerusopetukseenValmistavanOpetuksenOppiaine
}

export type PerusopetukseenValmistavanOpetuksenOsasuoritus =
  | NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa
  | PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus

export type PerusopetukseenValmistavanOpetuksenSuoritus = {
  kokonaislaajuus?: LaajuusVuosiviikkotunneissa
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetukseenvalmistavaopetus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: PerusopetukseenValmistavaOpetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PerusopetukseenValmistavanOpetuksenOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type PerusopetukseenValmistavaOpetus = {
  tunniste: Koodistokoodiviite<'koulutus', '999905'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type PerusopetuksenKäyttäytymisenArviointi = {
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', string>
  kuvaus?: LocalizedString
  päivä?: string
  hyväksytty?: boolean
}

export type PerusopetuksenLisäopetuksenAlisuoritus =
  | MuuPerusopetuksenLisäopetuksenSuoritus
  | PerusopetuksenLisäopetuksenOppiaineenSuoritus
  | PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus

export type PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot = {
  tehostetunTuenPäätökset?: Array<TehostetunTuenPäätös>
  joustavaPerusopetus?: Aikajakso
  pidennettyOppivelvollisuus?: Aikajakso
  ulkomaanjaksot?: Array<Aikajakso>
  majoitusetu?: Aikajakso
  kotiopetusjaksot?: Array<Aikajakso>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  kotiopetus?: Aikajakso
  oikeusMaksuttomaanAsuntolapaikkaan?: Aikajakso
  kuljetusetu?: Aikajakso
  vaikeastiVammainen?: Array<Aikajakso>
  perusopetuksenAloittamistaLykätty?: boolean
  maksuttomuus?: Array<Maksuttomuus>
  koulukoti?: Array<Aikajakso>
  erityisenTuenPäätökset?: Array<ErityisenTuenPäätös>
  aloittanutEnnenOppivelvollisuutta?: boolean
  erityisenTuenPäätös?: ErityisenTuenPäätös
  ulkomailla?: Aikajakso
  vammainen?: Array<Aikajakso>
  tehostetunTuenPäätös?: TehostetunTuenPäätös
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  vuosiluokkiinSitoutumatonOpetus?: boolean
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export type PerusopetuksenLisäopetuksenOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<
    'opiskeluoikeudentyyppi',
    'perusopetuksenlisaopetus'
  >
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<PerusopetuksenLisäopetuksenSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  oppilaitos?: Oppilaitos
}

export type PerusopetuksenLisäopetuksenOppiaineenSuoritus = {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetuksenlisaopetuksenoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: NuortenPerusopetuksenOppiaine
  korotus: boolean
  yksilöllistettyOppimäärä?: boolean
}

export type PerusopetuksenLisäopetuksenSuoritus = {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksenlisaopetus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: PerusopetuksenLisäopetus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PerusopetuksenLisäopetuksenAlisuoritus>
  osaAikainenErityisopetus?: boolean
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus = {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'perusopetuksenlisaopetuksentoimintaalue'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PerusopetuksenToiminta_Alue
  korotus?: boolean
}

export type PerusopetuksenLisäopetus = {
  tunniste: Koodistokoodiviite<'koulutus', '020075'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type PerusopetuksenLuokkaAste = {
  tunniste: Koodistokoodiviite<'perusopetuksenluokkaaste' | 'koulutus', string>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type PerusopetuksenOpiskeluoikeudenLisätiedot = {
  tehostetunTuenPäätökset?: Array<TehostetunTuenPäätös>
  joustavaPerusopetus?: Aikajakso
  pidennettyOppivelvollisuus?: Aikajakso
  ulkomaanjaksot?: Array<Aikajakso>
  majoitusetu?: Aikajakso
  kotiopetusjaksot?: Array<Aikajakso>
  kotiopetus?: Aikajakso
  oikeusMaksuttomaanAsuntolapaikkaan?: Aikajakso
  kuljetusetu?: Aikajakso
  vaikeastiVammainen?: Array<Aikajakso>
  perusopetuksenAloittamistaLykätty?: boolean
  koulukoti?: Array<Aikajakso>
  erityisenTuenPäätökset?: Array<ErityisenTuenPäätös>
  aloittanutEnnenOppivelvollisuutta?: boolean
  erityisenTuenPäätös?: ErityisenTuenPäätös
  ulkomailla?: Aikajakso
  vammainen?: Array<Aikajakso>
  tehostetunTuenPäätös?: TehostetunTuenPäätös
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  vuosiluokkiinSitoutumatonOpetus?: boolean
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export type PerusopetuksenOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'perusopetus'>
  tila: NuortenPerusopetuksenOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  sisältyyOpiskeluoikeuteen?: SisältäväOpiskeluoikeus
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: PerusopetuksenOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<PerusopetuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  oppilaitos?: Oppilaitos
}

export type PerusopetuksenOppiaineenArviointi =
  | NumeerinenPerusopetuksenOppiaineenArviointi
  | SanallinenPerusopetuksenOppiaineenArviointi

export type PerusopetuksenPäätasonSuoritus =
  | NuortenPerusopetuksenOppiaineenOppimääränSuoritus
  | NuortenPerusopetuksenOppimääränSuoritus
  | PerusopetuksenVuosiluokanSuoritus

export type PerusopetuksenToiminta_Alue = {
  tunniste: Koodistokoodiviite<'perusopetuksentoimintaalue', string>
  laajuus?: LaajuusVuosiviikkotunneissa
}

export type PerusopetuksenToiminta_AlueenSuoritus = {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksentoimintaalue'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PerusopetuksenToiminta_Alue
}

export type PerusopetuksenVuosiluokanSuorituksenLiite = {
  tunniste: Koodistokoodiviite<
    'perusopetuksentodistuksenliitetieto',
    'kayttaytyminen' | 'tyoskentely'
  >
  kuvaus: LocalizedString
}

export type PerusopetuksenVuosiluokanSuoritus = {
  muutSuorituskielet?: Array<Koodistokoodiviite<'kieli', string>>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksenvuosiluokka'>
  liitetiedot?: Array<PerusopetuksenVuosiluokanSuorituksenLiite>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
  suorituskieli: Koodistokoodiviite<'kieli', string>
  kielikylpykieli?: Koodistokoodiviite<'kieli', string>
  luokka: string
  suoritustapa?: Koodistokoodiviite<'perusopetuksensuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle?: boolean
  käyttäytymisenArvio?: PerusopetuksenKäyttäytymisenArviointi
  koulutusmoduuli: PerusopetuksenLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OppiaineenTaiToiminta_AlueenSuoritus>
  osaAikainenErityisopetus?: boolean
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type PreIBKoulutusmoduuli2015 = {
  tunniste: Koodistokoodiviite<'suorituksentyyppi', 'preiboppimaara'>
}

export type PreIBKoulutusmoduuli2019 = {
  tunniste: Koodistokoodiviite<'suorituksentyyppi', 'preiboppimaara2019'>
}

export type PreIBKurssi2015 =
  | IBKurssi
  | PaikallinenLukionKurssi2015
  | ValtakunnallinenLukionKurssi2015

export type PreIBKurssinSuoritus2015 = {
  arviointi?: Array<LukionArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'preibkurssi'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBKurssi2015
}

export type PreIBLukionModuuliMuissaOpinnoissa2019 =
  | LukionMuuModuuliMuissaOpinnoissa2019
  | LukionVieraanKielenModuuliMuissaOpinnoissa2019

export type PreIBLukionModuulinSuoritusMuissaOpinnoissa2019 = {
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionvaltakunnallinenmoduuli'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBLukionModuuliMuissaOpinnoissa2019
  tunnustettu?: OsaamisenTunnustaminen
}

export type PreIBLukionModuulinSuoritusOppiaineissa2019 = {
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionvaltakunnallinenmoduuli'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBLukionModuuliOppiaineissa2019
  tunnustettu?: OsaamisenTunnustaminen
}

export type PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 =

    | PreIBLukionModuulinSuoritusMuissaOpinnoissa2019
    | PreIBLukionPaikallisenOpintojaksonSuoritus2019

export type PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 =

    | PreIBLukionModuulinSuoritusOppiaineissa2019
    | PreIBLukionPaikallisenOpintojaksonSuoritus2019

export type PreIBLukionModuuliOppiaineissa2019 =
  | LukionMuuModuuliOppiaineissa2019
  | LukionVieraanKielenModuuliOppiaineissa2019

export type PreIBLukionOppiaine2019 =
  | LukionMatematiikka2019
  | LukionMuuValtakunnallinenOppiaine2019
  | LukionUskonto2019
  | LukionÄidinkieliJaKirjallisuus2019
  | PaikallinenLukionOppiaine2019
  | VierasTaiToinenKotimainenKieli2019

export type PreIBLukionPaikallisenOpintojaksonSuoritus2019 = {
  arviointi?: Array<LukionModuulinTaiPaikallisenOpintojaksonArviointi2019>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'lukionpaikallinenopintojakso'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBPaikallinenOpintojakso2019
  tunnustettu?: OsaamisenTunnustaminen
}

export type PreIBMuutSuorituksetTaiVastaavat2019 =
  | Lukiodiplomit2019
  | MuutLukionSuoritukset2019
  | TemaattisetOpinnot2019

export type PreIBOppiaine2015 =
  | IBOppiaineLanguage
  | IBOppiaineMuu
  | LukionMatematiikka2015
  | LukionMuuValtakunnallinenOppiaine2015
  | LukionUskonto2015
  | LukionÄidinkieliJaKirjallisuus2015
  | PaikallinenLukionOppiaine2015
  | VierasTaiToinenKotimainenKieli2015

export type PreIBOppiaineenSuoritus2015 = {
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'preiboppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBOppiaine2015
  osasuoritukset?: Array<PreIBKurssinSuoritus2015>
}

export type PreIBPaikallinenOpintojakso2019 = LukionPaikallinenOpintojakso2019

export type PreIBSuorituksenOsasuoritus2015 =
  | MuidenLukioOpintojenSuoritus2015
  | PreIBOppiaineenSuoritus2015

export type PreIBSuorituksenOsasuoritus2019 =
  | LukionOppiaineenPreIBSuoritus2019
  | MuidenLukioOpintojenPreIBSuoritus2019

export type PreIBSuoritus2015 = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'preiboppimaara'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: PreIBKoulutusmoduuli2015
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PreIBSuorituksenOsasuoritus2015>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type PreIBSuoritus2019 = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'preiboppimaara'>
  suullisenKielitaidonKokeet?: Array<SuullisenKielitaidonKoe2019>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  omanÄidinkielenOpinnot?: OmanÄidinkielenOpinnotLaajuusOpintopisteinä
  suorituskieli: Koodistokoodiviite<'kieli', string>
  puhviKoe?: PuhviKoe2019
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  ryhmä?: string
  koulutusmoduuli: PreIBKoulutusmoduuli2019
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PreIBSuorituksenOsasuoritus2019>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type PrimaryAlaoppimisalue = {
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiprimaryalaoppimisalue',
    string
  >
}

export type PrimaryAlaoppimisalueArviointi = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkiprimarymark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}

export type PrimaryLapsiAlaoppimisalue = {
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiprimarylapsialaoppimisalue',
    string
  >
}

export type PrimaryLapsiOppimisalue = {
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkilapsioppimisalue',
    string
  >
}

export type PrimaryLapsiOppimisalueenAlaosasuoritus = {
  koulutusmoduuli: PrimaryLapsiAlaoppimisalue
  arviointi?: Array<PrimaryAlaoppimisalueArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkialaosasuoritusprimarylapsi'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type PrimaryLapsiOppimisalueenSuoritus = {
  arviointi?: Array<EuropeanSchoolOfHelsinkiOsasuoritusArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuoritusprimarylapsi'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: PrimaryLapsiOppimisalue
  osasuoritukset?: Array<PrimaryLapsiOppimisalueenAlaosasuoritus>
  yksilöllistettyOppimäärä?: boolean
}

export type PrimaryLuokkaAste = {
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'P1' | 'P2' | 'P3' | 'P4' | 'P5'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
}

export type PrimaryOppimisalueenAlaosasuoritus = {
  koulutusmoduuli: PrimaryAlaoppimisalue
  arviointi?: Array<PrimaryAlaoppimisalueArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkialaosasuoritusprimary'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type PrimaryOppimisalueenSuoritus = {
  arviointi?: Array<EuropeanSchoolOfHelsinkiOsasuoritusArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuoritusprimary'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PrimarySuorituskielenVaativaOppimisalue
  osasuoritukset?: Array<PrimaryOppimisalueenAlaosasuoritus>
  yksilöllistettyOppimäärä?: boolean
}

export type PrimaryOsasuoritus =
  | PrimaryLapsiOppimisalueenSuoritus
  | PrimaryOppimisalueenSuoritus

export type PrimarySuorituskielenVaativaOppimisalue =
  | EuropeanSchoolOfHelsinkiKielioppiaine
  | EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek
  | EuropeanSchoolOfHelsinkiKielioppiaineLatin
  | EuropeanSchoolOfHelsinkiMuuOppiaine

export type PrimaryVuosiluokanSuoritus = {
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkivuosiluokkaprimary'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle?: boolean
  koulutusmoduuli: PrimaryLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PrimaryOsasuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type PuhviKoe2019 = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10' | 'S' | 'H'
  >
  kuvaus?: LocalizedString
  päivä: string
  hyväksytty?: boolean
}

export type PYPLuokkaAste = {
  tunniste: Koodistokoodiviite<
    'internationalschoolluokkaaste',
    'explorer' | '1' | '2' | '3' | '4' | '5'
  >
}

export type PYPOppiaine =
  | LanguageAcquisition
  | LanguageAndLiterature
  | PYPOppiaineMuu

export type PYPOppiaineenSuoritus = {
  arviointi?: Array<SanallinenInternationalSchoolOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolpypoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PYPOppiaine
}

export type PYPOppiaineMuu = {
  tunniste: Koodistokoodiviite<
    'oppiaineetinternationalschool',
    | 'DD'
    | 'DE'
    | 'DR'
    | 'EAL'
    | 'EMA'
    | 'FR'
    | 'FMT'
    | 'ICT'
    | 'ILS'
    | 'IS'
    | 'LA'
    | 'LIB'
    | 'MA'
    | 'ME'
    | 'MU'
    | 'PE'
    | 'PHE'
    | 'SCI'
    | 'SS'
    | 'VA'
    | 'ART'
    | 'FFL'
  >
}

export type PYPVuosiluokanSuoritus = {
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolpypvuosiluokka'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  luokka?: string
  koulutusmoduuli: PYPLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<PYPOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type Päivämäärävahvistus = {
  päivä: string
  myöntäjäOrganisaatio: Organisaatio
}

export type S7OppiaineenAlaosasuoritus = {
  koulutusmoduuli: S7OppiaineKomponentti
  arviointi?: Array<SecondaryS7PreliminaryMarkArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkialaosasuorituss7'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type S7OppiaineKomponentti = {
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkis7oppiaineenkomponentti',
    string
  >
}

export type SanallinenInternationalSchoolOppiaineenArviointi = {
  arvosana: Koodistokoodiviite<'arviointiasteikkointernationalschool', string>
  päivä?: string
  hyväksytty?: boolean
}

export type SanallinenLukionArviointi = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'S' | 'H' | 'O'
  >
  kuvaus?: LocalizedString
  päivä: string
  hyväksytty?: boolean
}

export type SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 = {
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', 'H' | 'S'>
  kuvaus?: LocalizedString
  päivä: string
  hyväksytty?: boolean
}

export type SanallinenLukionOppiaineenArviointi2019 = {
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', 'H' | 'S'>
  päivä?: string
  hyväksytty?: boolean
}

export type SanallinenPerusopetuksenOppiaineenArviointi = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'S' | 'H' | 'O'
  >
  kuvaus?: LocalizedString
  päivä?: string
  hyväksytty?: boolean
}

export type SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi =
  {
    arvosana: Koodistokoodiviite<
      'arviointiasteikkotuva',
      'Hyväksytty' | 'Hylätty'
    >
    kuvaus?: LocalizedString
    päivä: string
    hyväksytty?: boolean
  }

export type SecondaryGradeArviointi = {
  päivä?: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkisecondarygrade',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}

export type SecondaryLowerArviointi =
  | SecondaryGradeArviointi
  | SecondaryNumericalMarkArviointi

export type SecondaryLowerLuokkaAste = {
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'S1' | 'S2' | 'S3' | 'S4' | 'S5'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
}

export type SecondaryLowerOppiaineenSuoritus = {
  arviointi?: Array<SecondaryLowerArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuoritussecondarylower'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  yksilöllistettyOppimäärä?: boolean
}

export type SecondaryLowerVuosiluokanSuoritus = {
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkivuosiluokkasecondarylower'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle?: boolean
  koulutusmoduuli: SecondaryLowerLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<SecondaryLowerOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type SecondaryNumericalMarkArviointi = {
  päivä?: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkinumericalmark',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}

export type SecondaryOppiaine =
  | EuropeanSchoolOfHelsinkiKielioppiaine
  | EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek
  | EuropeanSchoolOfHelsinkiKielioppiaineLatin
  | EuropeanSchoolOfHelsinkiMuuOppiaine

export type SecondaryS7PreliminaryMarkArviointi = {
  päivä?: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}

export type SecondaryUpperLuokkaAste = {
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'S6' | 'S7'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
}

export type SecondaryUpperOppiaineenSuoritus =
  | SecondaryUpperOppiaineenSuoritusS6
  | SecondaryUpperOppiaineenSuoritusS7

export type SecondaryUpperOppiaineenSuoritusS6 = {
  arviointi?: Array<SecondaryNumericalMarkArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuorituss6'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  yksilöllistettyOppimäärä?: boolean
}

export type SecondaryUpperOppiaineenSuoritusS7 = {
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkiosasuorituss7'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  osasuoritukset?: Array<S7OppiaineenAlaosasuoritus>
  yksilöllistettyOppimäärä?: boolean
}

export type SecondaryUpperVuosiluokanSuoritus = {
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkivuosiluokkasecondaryupper'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  luokka?: string
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  jääLuokalle?: boolean
  koulutusmoduuli: SecondaryUpperLuokkaAste
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<SecondaryUpperOppiaineenSuoritus>
  vahvistus?: HenkilövahvistusPaikkakunnalla
}

export type SisältäväOpiskeluoikeus = {
  oppilaitos: Oppilaitos
  oid: string
}

export type Suoritus =
  | AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus
  | AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus
  | AikuistenPerusopetuksenAlkuvaiheenSuoritus
  | AikuistenPerusopetuksenKurssinSuoritus
  | AikuistenPerusopetuksenOppiaineenOppimääränSuoritus
  | AikuistenPerusopetuksenOppiaineenSuoritus
  | AikuistenPerusopetuksenOppimääränSuoritus
  | AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus
  | AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
  | AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
  | AmmatillisenTutkinnonOsittainenSuoritus
  | AmmatillisenTutkinnonSuoritus
  | DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus
  | DIAOppiaineenTutkintovaiheenSuoritus
  | DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus
  | DIAOppiaineenValmistavanVaiheenSuoritus
  | DIATutkinnonSuoritus
  | DIAValmistavanVaiheenSuoritus
  | DiplomaCoreRequirementsOppiaineenSuoritus
  | DiplomaOppiaineenSuoritus
  | DiplomaVuosiluokanSuoritus
  | EBOppiaineenAlaosasuoritus
  | EBTutkinnonOsasuoritus
  | EBTutkinnonSuoritus
  | EsiopetuksenSuoritus
  | IBCASSuoritus
  | IBExtendedEssaySuoritus
  | IBKurssinSuoritus
  | IBOppiaineenSuoritus
  | IBTheoryOfKnowledgeSuoritus
  | IBTutkinnonSuoritus
  | KorkeakoulunOpintojaksonSuoritus
  | KorkeakouluopintojenSuoritus
  | KorkeakoulututkinnonSuoritus
  | LukioOpintojenSuoritus
  | LukionKurssinSuoritus2015
  | LukionModuulinSuoritusMuissaOpinnoissa2019
  | LukionModuulinSuoritusOppiaineissa2019
  | LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa
  | LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019
  | LukionOppiaineenOppimääränSuoritus2015
  | LukionOppiaineenPreIBSuoritus2019
  | LukionOppiaineenSuoritus2015
  | LukionOppiaineenSuoritus2019
  | LukionOppiaineidenOppimäärienSuoritus2019
  | LukionOppimääränSuoritus2015
  | LukionOppimääränSuoritus2019
  | LukionPaikallisenOpintojaksonSuoritus2019
  | LukioonValmistavanKoulutuksenOppiaineenSuoritus
  | LukioonValmistavanKoulutuksenSuoritus
  | LukioonValmistavanKurssinSuoritus
  | MYPOppiaineenSuoritus
  | MYPVuosiluokanSuoritus
  | MuidenLukioOpintojenPreIBSuoritus2019
  | MuidenLukioOpintojenSuoritus2015
  | MuidenLukioOpintojenSuoritus2019
  | MuidenOpintovalmiuksiaTukevienOpintojenSuoritus
  | MuuKorkeakoulunSuoritus
  | MuuPerusopetuksenLisäopetuksenSuoritus
  | MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus
  | MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus
  | MuunAmmatillisenKoulutuksenSuoritus
  | MuunAmmatillisenTutkinnonOsanSuoritus
  | MuunKuinSäännellynKoulutuksenOsasuoritus
  | MuunKuinSäännellynKoulutuksenPäätasonSuoritus
  | MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
  | NuortenPerusopetuksenOppiaineenOppimääränSuoritus
  | NuortenPerusopetuksenOppiaineenSuoritus
  | NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa
  | NuortenPerusopetuksenOppimääränSuoritus
  | NurseryVuosiluokanSuoritus
  | NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus
  | NäyttötutkintoonValmistavanKoulutuksenSuoritus
  | OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus
  | OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022
  | OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus
  | OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus
  | OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus
  | OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus
  | OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
  | OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
  | PYPOppiaineenSuoritus
  | PYPVuosiluokanSuoritus
  | PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus
  | PerusopetukseenValmistavanOpetuksenSuoritus
  | PerusopetuksenLisäopetuksenOppiaineenSuoritus
  | PerusopetuksenLisäopetuksenSuoritus
  | PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus
  | PerusopetuksenToiminta_AlueenSuoritus
  | PerusopetuksenVuosiluokanSuoritus
  | PreIBKurssinSuoritus2015
  | PreIBLukionModuulinSuoritusMuissaOpinnoissa2019
  | PreIBLukionModuulinSuoritusOppiaineissa2019
  | PreIBLukionPaikallisenOpintojaksonSuoritus2019
  | PreIBOppiaineenSuoritus2015
  | PreIBSuoritus2015
  | PreIBSuoritus2019
  | PrimaryLapsiOppimisalueenAlaosasuoritus
  | PrimaryLapsiOppimisalueenSuoritus
  | PrimaryOppimisalueenAlaosasuoritus
  | PrimaryOppimisalueenSuoritus
  | PrimaryVuosiluokanSuoritus
  | S7OppiaineenAlaosasuoritus
  | SecondaryLowerOppiaineenSuoritus
  | SecondaryLowerVuosiluokanSuoritus
  | SecondaryUpperOppiaineenSuoritusS6
  | SecondaryUpperOppiaineenSuoritusS7
  | SecondaryUpperVuosiluokanSuoritus
  | TelmaKoulutuksenOsanSuoritus
  | TelmaKoulutuksenSuoritus
  | TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus
  | TutkinnonOsaaPienemmänKokonaisuudenSuoritus
  | TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus
  | TutkintokoulutukseenValmentavanKoulutuksenSuoritus
  | TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus
  | TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus
  | VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus
  | VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022
  | VSTKotoutumiskoulutuksenOhjauksenSuoritus2022
  | VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022
  | VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus
  | VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022
  | VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus
  | ValmaKoulutuksenOsanSuoritus
  | ValmaKoulutuksenSuoritus
  | VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus
  | VapaanSivistystyönJotpaKoulutuksenSuoritus
  | VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus
  | VapaanSivistystyönLukutaitokoulutuksenSuoritus
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus
  | VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
  | VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus
  | YhteisenAmmatillisenTutkinnonOsanSuoritus
  | YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus
  | YhteisenTutkinnonOsanOsaAlueenSuoritus
  | YlioppilastutkinnonKokeenSuoritus
  | YlioppilastutkinnonSuoritus

export type SuullisenKielitaidonKoe2019 = {
  päivä: string
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10' | 'S' | 'H'
  >
  taitotaso: Koodistokoodiviite<
    'arviointiasteikkokehittyvankielitaidontasot',
    | 'alle_A1.1'
    | 'A1.1'
    | 'A1.2'
    | 'A1.3'
    | 'A2.1'
    | 'A2.2'
    | 'B1.1'
    | 'B1.2'
    | 'B2.1'
    | 'B2.2'
    | 'C1.1'
    | 'yli_C1.1'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  hyväksytty?: boolean
  kuvaus?: LocalizedString
}

export type TehostetunTuenPäätös = {
  alku: string
  loppu?: string
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
}

export type TelmaJaValmaArviointi = {
  päivä: string
  arvosana: Koodistokoodiviite<
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}

export type TelmaKoulutuksenOsa =
  | MuuValtakunnallinenTutkinnonOsa
  | PaikallinenTelmaKoulutuksenOsa
  | YhteinenTutkinnonOsa

export type TelmaKoulutuksenOsanSuoritus = {
  arviointi?: Array<TelmaJaValmaArviointi>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'telmakoulutuksenosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: TelmaKoulutuksenOsa
  tunnustettu?: OsaamisenTunnustaminen
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
}

export type TelmaKoulutuksenSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'telma'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: TelmaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<TelmaKoulutuksenOsanSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export type TelmaKoulutus = {
  tunniste: Koodistokoodiviite<'koulutus', '999903'>
  perusteenDiaarinumero?: string
  laajuus?: LaajuusOsaamispisteissä
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type TemaattisetOpinnot2019 = {
  tunniste: Koodistokoodiviite<'lukionmuutopinnot', 'TO'>
  laajuus?: LaajuusOpintopisteissä
}

export type Toimipiste = {
  oid: string
  nimi?: LocalizedString
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}

export type TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus = {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}

export type TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus =

    | TutkinnonOsaaPienemmänKokonaisuudenSuoritus
    | YhteisenTutkinnonOsanOsaAlueenSuoritus

export type TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus = {
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  koulutusmoduuli: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export type TutkinnonOsaaPienemmänKokonaisuudenSuoritus = {
  arviointi?: Array<MuunAmmatillisenKoulutuksenArviointi>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'tutkinnonosaapienempikokonaisuus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto>
  liittyyTutkinnonOsaan: Koodistokoodiviite<'tutkinnonosat', string>
  koulutusmoduuli: TutkinnonOsaaPienempiKokonaisuus
}

export type TutkinnonOsaaPienempiKokonaisuus = {
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}

export type TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus = {
  arviointi?: Array<SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    | 'tutkintokoulutukseenvalmentava'
    | 'tuvaperusopetus'
    | 'tuvalukiokoulutus'
    | 'tuvaammatillinenkoulutus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenMuuOsa
  tunnustettu?: OsaamisenTunnustaminen
}

export type TutkintokoulutukseenValmentavanKoulutuksenMuuOsa =
  | TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen
  | TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot
  | TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot
  | TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot
  | TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot
  | TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen

export type TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus =
  | TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus
  | TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus

export type TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus =
  TutkintokoulutukseenValmentavanKoulutuksenSuoritus

export type TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi =
  SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi

export type TutkintokoulutukseenValmentavanKoulutuksenSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'tuvakoulutuksensuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export type TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa = {
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '104'>
  laajuus?: LaajuusViikoissa
}

export type TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus =
  {
    arviointi?: Array<TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'tutkintokoulutukseenvalmentava'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus
    tunnustettu?: OsaamisenTunnustaminen
  }

export type TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus =
  {
    nimi: LocalizedString
    tunniste: PaikallinenKoodi
    laajuus?: LaajuusViikoissa
  }

export type TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus =
  {
    arviointi?: Array<SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'tutkintokoulutukseenvalmentava'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa
    tunnustettu?: OsaamisenTunnustaminen
    osasuoritukset?: Array<TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus>
  }

export type TutkintokoulutukseenValmentavanKoulutus = {
  tunniste: Koodistokoodiviite<'koulutus', '999908'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusViikoissa
}

export type TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot =
  {
    osaAikaisuusjaksot?: Array<OsaAikaisuusJakso>
    vaativanErityisenTuenErityinenTehtävä?: Array<Aikajakso>
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    vaativanErityisenTuenYhteydessäJärjestettäväMajoitus?: Array<Aikajakso>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    vaikeastiVammainen?: Array<Aikajakso>
    maksuttomuus?: Array<Maksuttomuus>
    vammainenJaAvustaja?: Array<Aikajakso>
    majoitus?: Array<Aikajakso>
    vankilaopetuksessa?: Array<Aikajakso>
    erityinenTuki?: Array<Aikajakso>
    koulutusvienti?: boolean
    pidennettyPäättymispäivä?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  }

export type TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot =
  | TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot
  | TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot
  | TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot

export type TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot =
  {
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    maksuttomuus?: Array<Maksuttomuus>
    pidennettyPäättymispäivä?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  }

export type TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot =
  {
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    majoitusetu?: Aikajakso
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    kuljetusetu?: Aikajakso
    vaikeastiVammainen?: Array<Aikajakso>
    maksuttomuus?: Array<Maksuttomuus>
    koulukoti?: Array<Aikajakso>
    erityisenTuenPäätökset?: Array<TuvaErityisenTuenPäätös>
    vammainen?: Array<Aikajakso>
    pidennettyPäättymispäivä?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  }

export type TutkintokoulutukseenValmentavanOpiskeluoikeudenTila = {
  opiskeluoikeusjaksot: Array<TutkintokoulutukseenValmentavanOpiskeluoikeusjakso>
}

export type TutkintokoulutukseenValmentavanOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'tuva'>
  tila: TutkintokoulutukseenValmentavanOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  järjestämislupa: Koodistokoodiviite<'tuvajarjestamislupa', string>
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export type TutkintokoulutukseenValmentavanOpiskeluoikeusjakso = {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    | 'eronnut'
    | 'katsotaaneronneeksi'
    | 'lasna'
    | 'mitatoity'
    | 'valiaikaisestikeskeytynyt'
    | 'valmistunut'
    | 'loma'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '1' | '6' | '10'>
}

export type TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen = {
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '107'>
  laajuus?: LaajuusViikoissa
}

export type TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot = {
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '105'>
  laajuus?: LaajuusViikoissa
}

export type TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot =
  {
    tunniste: Koodistokoodiviite<'koulutuksenosattuva', '103'>
    laajuus?: LaajuusViikoissa
  }

export type TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot = {
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '106'>
  laajuus?: LaajuusViikoissa
}

export type TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot = {
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '101'>
  laajuus?: LaajuusViikoissa
}

export type TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen =
  {
    tunniste: Koodistokoodiviite<'koulutuksenosattuva', '102'>
    laajuus?: LaajuusViikoissa
  }

export type Tutkintotoimikunta = {
  nimi: LocalizedString
  tutkintotoimikunnanNumero: string
}

export type TuvaErityisenTuenPäätös = {
  alku?: string
  loppu?: string
}

export type Työssäoppimisjakso = {
  työssäoppimispaikka?: LocalizedString
  paikkakunta: Koodistokoodiviite<'kunta', string>
  loppu?: string
  laajuus: LaajuusOsaamispisteissä
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  alku: string
  työtehtävät?: LocalizedString
}

export type TäydellisetHenkilötiedot = {
  äidinkieli?: Koodistokoodiviite<'kieli', string>
  sukunimi: string
  oid: string
  syntymäaika?: string
  kutsumanimi: string
  kansalaisuus?: Array<Koodistokoodiviite<'maatjavaltiot2', string>>
  turvakielto?: boolean
  hetu?: string
  etunimet: string
}

export type Ulkomaanjakso = {
  alku: string
  loppu?: string
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  kuvaus: LocalizedString
}

export type UusiHenkilö = {
  hetu: string
  etunimet: string
  kutsumanimi?: string
  sukunimi: string
}

export type Vahvistus =
  | HenkilövahvistusPaikkakunnalla
  | HenkilövahvistusValinnaisellaPaikkakunnalla
  | HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla
  | Organisaatiovahvistus
  | Päivämäärävahvistus

export type ValmaKoulutuksenOsa =
  | MuuValtakunnallinenTutkinnonOsa
  | PaikallinenValmaKoulutuksenOsa
  | YhteinenTutkinnonOsa

export type ValmaKoulutuksenOsanSuoritus = {
  arviointi?: Array<TelmaJaValmaArviointi>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'valmakoulutuksenosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: ValmaKoulutuksenOsa
  tunnustettu?: OsaamisenTunnustaminen
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
}

export type ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus =
  | ValmaKoulutuksenOsanSuoritus
  | YhteisenTutkinnonOsanOsaAlueenSuoritus

export type ValmaKoulutuksenSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'valma'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: ValmaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export type ValmaKoulutus = {
  tunniste: Koodistokoodiviite<'koulutus', '999901'>
  perusteenDiaarinumero?: string
  laajuus?: LaajuusOsaamispisteissä
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017 = {
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenalkuvaiheenkurssit2017',
    string
  >
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export type ValtakunnallinenAikuistenPerusopetuksenKurssi2015 = {
  tunniste: Koodistokoodiviite<'aikuistenperusopetuksenkurssit2015', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export type ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017 = {
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenpaattovaiheenkurssit2017',
    string
  >
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export type ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue = {
  tunniste: Koodistokoodiviite<'ammatillisenoppiaineet', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export type ValtakunnallinenLukionKurssi2015 = {
  tunniste: Koodistokoodiviite<
    | 'lukionkurssit'
    | 'lukionkurssitops2004aikuiset'
    | 'lukionkurssitops2003nuoret',
    string
  >
  laajuus?: LaajuusKursseissa
  kurssinTyyppi: Koodistokoodiviite<'lukionkurssintyyppi', string>
}

export type ValtakunnallinenLukioonValmistavanKoulutuksenKurssi = {
  tunniste: Koodistokoodiviite<
    | 'lukioonvalmistavankoulutuksenkurssit2015'
    | 'lukioonvalmistavankoulutuksenmoduulit2019',
    string
  >
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
}

export type VapaanSivistystyöJotpaKoulutuksenArviointi = {
  arvosana: Koodistokoodiviite<'arviointiasteikkovst', string>
  päivä: string
  hyväksytty?: boolean
}

export type VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso = {
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'hyvaksytystisuoritettu' | 'lasna' | 'keskeytynyt' | 'mitatoity'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '14' | '15'>
}

export type VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus = {
  arviointi?: Array<VapaanSivistystyöJotpaKoulutuksenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstjotpakoulutuksenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: VapaanSivistystyönJotpaKoulutuksenOsasuoritus
  osasuoritukset?: Array<VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus>
}

export type VapaanSivistystyönJotpaKoulutuksenOsasuoritus = {
  tunniste: PaikallinenKoodi
  laajuus: LaajuusOpintopisteissä
}

export type VapaanSivistystyönJotpaKoulutuksenSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'vstjotpakoulutus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: VapaanSivistystyönJotpaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export type VapaanSivistystyönJotpaKoulutus = {
  tunniste: Koodistokoodiviite<'koulutus', '099999'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
  opintokokonaisuus: Koodistokoodiviite<'opintokokonaisuudet', string>
}

export type VapaanSivistystyönLukutaidonKokonaisuus = {
  tunniste: Koodistokoodiviite<'vstlukutaitokoulutuksenkokonaisuus', string>
  laajuus?: LaajuusOpintopisteissä
}

export type VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus = {
  koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstlukutaitokoulutuksenkokonaisuudensuoritus'
  >
  arviointi?: Array<LukutaitokoulutuksenArviointi>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type VapaanSivistystyönLukutaitokoulutuksenSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'vstlukutaitokoulutus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: VapaanSivistystyönLukutaitokoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export type VapaanSivistystyönLukutaitokoulutus = {
  tunniste: Koodistokoodiviite<'koulutus', '999911'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
}

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi =
  {
    päivä: string
    luetunYmmärtämisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    arvosana: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
    puhumisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    hyväksytty?: boolean
    kirjoittamisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
    kuullunYmmärtämisenTaitotaso?: VSTKehittyvänKielenTaitotasonArviointi
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli =
  {
    tunniste: Koodistokoodiviite<
      'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
      'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 =
  {
    tunniste: Koodistokoodiviite<'vstkoto2022kielijaviestintakoulutus', string>
    laajuus?: LaajuusOpintopisteissä
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus =
  {
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus'
    >
    arviointi?: Array<VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi>
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus =

    | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus
    | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus
    | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus
    | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli =
  {
    tunniste: Koodistokoodiviite<
      'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
      'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus =
  {
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus'
    >
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus =
  {
    tunniste: PaikallinenKoodi
    kuvaus: LocalizedString
    laajuus?: LaajuusOpintopisteissä
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot =
  {
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojensuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli =
  {
    tunniste: Koodistokoodiviite<
      'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
      'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus =
  {
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli
    osasuoritukset?: Array<VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus>
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso =
  {
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojentyoelamajaksonsuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli =
  {
    tunniste: Koodistokoodiviite<
      'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
      'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus =
  {
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus =
  {
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli
    osasuoritukset?: Array<VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus>
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus = {
  tunniste: Koodistokoodiviite<'koulutus', '999910'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
}

export type VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus =

    | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot
    | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso

export type VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen = {
  selite: LocalizedString
}

export type VapaanSivistystyönOpintokokonaisuudenSuoritus =
  | MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus
  | OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus

export type VapaanSivistystyönOpiskeluoikeudenLisätiedot = {
  maksuttomuus?: Array<Maksuttomuus>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
}

export type VapaanSivistystyönOpiskeluoikeudenTila = {
  opiskeluoikeusjaksot: Array<VapaanSivistystyönOpiskeluoikeusjakso>
}

export type VapaanSivistystyönOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<
    'opiskeluoikeudentyyppi',
    'vapaansivistystyonkoulutus'
  >
  tila: VapaanSivistystyönOpiskeluoikeudenTila
  alkamispäivä?: string
  organisaatiohistoria?: Array<OpiskeluoikeudenOrganisaatiohistoria>
  oid?: string
  koulutustoimija?: Koulutustoimija
  lisätiedot?: VapaanSivistystyönOpiskeluoikeudenLisätiedot
  versionumero?: number
  suoritukset: Array<VapaanSivistystyönPäätasonSuoritus>
  aikaleima?: string
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  arvioituPäättymispäivä?: string
  oppilaitos?: Oppilaitos
}

export type VapaanSivistystyönOpiskeluoikeusjakso =
  | OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso
  | VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso
  | VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso

export type VapaanSivistystyönPäätasonSuoritus =
  | OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus
  | OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022
  | OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus
  | VapaanSivistystyönJotpaKoulutuksenSuoritus
  | VapaanSivistystyönLukutaitokoulutuksenSuoritus
  | VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus

export type VapaanSivistystyönVapaatavoitteinenKoulutus = {
  tunniste: Koodistokoodiviite<'koulutus', '099999'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
  opintokokonaisuus?: Koodistokoodiviite<'opintokokonaisuudet', string>
}

export type VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso =
  {
    alku: string
    tila: Koodistokoodiviite<
      'koskiopiskeluoikeudentila',
      'hyvaksytystisuoritettu' | 'keskeytynyt' | 'mitatoity'
    >
  }

export type VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus =
  {
    arviointi?: Array<VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstvapaatavoitteisenkoulutuksenosasuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus
    osasuoritukset?: Array<VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus>
  }

export type VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus = {
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  laajuus: LaajuusOpintopisteissä
}

export type VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus = {
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstvapaatavoitteinenkoulutus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: VapaanSivistystyönVapaatavoitteinenKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export type VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkovstvapaatavoitteinen' | 'arviointiasteikkovst',
    string
  >
  päivä: string
  hyväksytty?: boolean
}

export type VierasTaiToinenKotimainenKieli2015 = {
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    'A1' | 'A2' | 'B1' | 'B2' | 'B3' | 'AOM'
  >
}

export type VierasTaiToinenKotimainenKieli2019 = {
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    'A' | 'B1' | 'B2' | 'B3' | 'AOM'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}

export type VirtaVirhe = Duplikaatti | OpiskeluoikeusAvaintaEiLöydy

export type VSTKehittyvänKielenTaitotasonArviointi = {
  taso: Koodistokoodiviite<
    'arviointiasteikkokehittyvankielitaidontasot',
    | 'A1.1'
    | 'A1.2'
    | 'A1.3'
    | 'A2.1'
    | 'A2.2'
    | 'B1.1'
    | 'B1.2'
    | 'B2.1'
    | 'B2.2'
    | 'C1.1'
    | 'C1.2'
    | 'C2.1'
    | 'C2.2'
  >
}

export type VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi = {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkokehittyvankielitaidontasot',
    | 'A1.1'
    | 'A1.2'
    | 'A1.3'
    | 'A2.1'
    | 'A2.2'
    | 'B1.1'
    | 'B1.2'
    | 'B2.1'
    | 'B2.2'
    | 'C1.1'
    | 'C1.2'
    | 'C2.1'
    | 'C2.2'
    | 'alle_A1.1'
    | 'yli_C1.1'
  >
  arviointipäivä?: string
  hyväksytty?: boolean
}

export type VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus = {
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstmaahanmuuttajienkotoutumiskoulutuksenkielitaitojensuoritus'
  >
  koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022
  arviointi?: Array<VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 = {
  arviointi?: Array<VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli
  osasuoritukset?: Array<VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus>
}

export type VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli = {
  tunniste: Koodistokoodiviite<
    'vstkoto2022kokonaisuus',
    'kielijaviestintaosaaminen'
  >
  laajuus?: LaajuusOpintopisteissä
}

export type VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022 =
  | VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022
  | VSTKotoutumiskoulutuksenOhjauksenSuoritus2022
  | VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022
  | VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022

export type VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 = {
  tunniste: Koodistokoodiviite<'vstkoto2022kokonaisuus', 'ohjaus'>
  laajuus?: LaajuusOpintopisteissä
}

export type VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 = {
  koulutusmoduuli: VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 = {
  arvosana: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
  päivä: string
  hyväksytty?: boolean
}

export type VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 =
  {
    kuvaus: LocalizedString
    tunniste: PaikallinenKoodi
    laajuus?: LaajuusOpintopisteissä
  }

export type VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 = {
  tunniste: Koodistokoodiviite<'vstkoto2022kokonaisuus', 'valinnaisetopinnot'>
  laajuus?: LaajuusOpintopisteissä
}

export type VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 = {
  arviointi?: Array<VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022
  osasuoritukset?: Array<VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus>
}

export type VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =
  {
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojensuoritus'
    >
    koulutusmoduuli: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export type VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 =
  {
    tunniste: Koodistokoodiviite<
      'vstkoto2022kokonaisuus',
      'yhteiskuntajatyoelamaosaaminen'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export type VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 =
  {
    arviointi?: Array<VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022
    osasuoritukset?: Array<VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus>
  }

export type VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022 =
  {
    tunniste: Koodistokoodiviite<
      'vstkoto2022yhteiskuntajatyoosaamiskoulutus',
      string
    >
    laajuus?: LaajuusOpintopisteissä
  }

export type VSTKotoutumiskoulutus2022 = {
  tunniste: Koodistokoodiviite<'koulutus', '999910'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
}

export type VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus = {
  koulutusmoduuli: VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type YhteinenTutkinnonOsa = {
  tunniste: Koodistokoodiviite<
    'tutkinnonosat',
    | '101053'
    | '101054'
    | '101055'
    | '101056'
    | '106727'
    | '106728'
    | '106729'
    | '400012'
    | '400013'
    | '400014'
    | '600001'
    | '600002'
  >
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export type YhteisenAmmatillisenTutkinnonOsanSuoritus = {
  arviointi?: Array<AmmatillinenArviointi>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: YhteinenTutkinnonOsa
  tunnustettu?: OsaamisenTunnustaminen
  toimipiste?: OrganisaatioWithOid
  tutkinnonOsanRyhmä?: Koodistokoodiviite<'ammatillisentutkinnonosanryhma', '2'>
  osasuoritukset?: Array<YhteisenTutkinnonOsanOsaAlueenSuoritus>
  tutkinto?: AmmatillinenTutkintoKoulutus
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
}

export type YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus = {
  arviointi?: Array<AmmatillinenArviointi>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: YhteinenTutkinnonOsa
  tunnustettu?: OsaamisenTunnustaminen
  toimipiste?: OrganisaatioWithOid
  tutkinnonOsanRyhmä?: Koodistokoodiviite<'ammatillisentutkinnonosanryhma', '2'>
  osasuoritukset?: Array<YhteisenTutkinnonOsanOsaAlueenSuoritus>
  tutkinto?: AmmatillinenTutkintoKoulutus
  vahvistus?: HenkilövahvistusValinnaisellaTittelillä
}

export type YhteisenTutkinnonOsanOsaAlueenSuoritus = {
  arviointi?: Array<AmmatillinenArviointi>
  näyttö?: Näyttö
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillisentutkinnonosanosaalue'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: AmmatillisenTutkinnonOsanOsaAlue
  tunnustettu?: OsaamisenTunnustaminen
}

export type YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus =

    | LukioOpintojenSuoritus
    | MuidenOpintovalmiuksiaTukevienOpintojenSuoritus
    | YhteisenTutkinnonOsanOsaAlueenSuoritus

export type YlioppilaskokeenArviointi = {
  arvosana: Koodistokoodiviite<'koskiyoarvosanat', string>
  pisteet?: number
  hyväksytty?: boolean
}

export type YlioppilasTutkinnonKoe = {
  tunniste: Koodistokoodiviite<'koskiyokokeet', string>
}

export type YlioppilastutkinnonKokeenSuoritus = {
  arviointi?: Array<YlioppilaskokeenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ylioppilastutkinnonkoe'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  tutkintokerta: YlioppilastutkinnonTutkintokerta
  koulutusmoduuli: YlioppilasTutkinnonKoe
}

export type YlioppilastutkinnonOpiskeluoikeudenTila = {
  opiskeluoikeusjaksot: Array<LukionOpiskeluoikeusjakso>
}

export type YlioppilastutkinnonOpiskeluoikeus = {
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ylioppilastutkinto'>
  tila: YlioppilastutkinnonOpiskeluoikeudenTila
  alkamispäivä?: string
  koulutustoimija?: Koulutustoimija
  suoritukset: Array<YlioppilastutkinnonSuoritus>
  päättymispäivä?: string
  lähdejärjestelmänId?: LähdejärjestelmäId
  oppilaitos?: Oppilaitos
}

export type YlioppilastutkinnonSuoritus = {
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ylioppilastutkinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulusivistyskieli?: Array<Koodistokoodiviite<'kieli', 'FI' | 'SV'>>
  pakollisetKokeetSuoritettu: boolean
  koulutusmoduuli: Ylioppilastutkinto
  toimipiste?: OrganisaatioWithOid
  osasuoritukset?: Array<YlioppilastutkinnonKokeenSuoritus>
  vahvistus?: Organisaatiovahvistus
}

export type YlioppilastutkinnonTutkintokerta = {
  koodiarvo: string
  vuosi: number
  vuodenaika: LocalizedString
}

export type Ylioppilastutkinto = {
  tunniste: Koodistokoodiviite<'koulutus', '301000'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type Yritys = {
  nimi: LocalizedString
  yTunnus: string
}
