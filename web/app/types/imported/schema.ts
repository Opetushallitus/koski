/*
 * fi.oph.koski.schema
 */

export type Oppija = {
  $class: 'Oppija'
  henkilö: Henkilö
  opiskeluoikeudet: Array<Opiskeluoikeus>
}

export type Aikajakso = {
  $class: 'Aikajakso'
  alku: string
  loppu?: string
}

export type AikuistenPerusopetuksenAlkuvaihe = {
  $class: 'AikuistenPerusopetuksenAlkuvaihe'
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenoppimaaranalkuvaihe'
  >
}

export type English = {
  $class: 'English'
  en: string
}

export type Finnish = {
  $class: 'Finnish'
  fi: string
  sv?: string
  en?: string
}

export type LocalizedString = English | Finnish | Swedish

export type Swedish = {
  $class: 'Swedish'
  sv: string
  en?: string
}

export type AikuistenPerusopetuksenAlkuvaiheenKurssi =
  | PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi
  | ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017

export type AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus = {
  $class: 'AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus'
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
  $class: 'AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus'
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
  $class: 'AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
}

export type AikuistenPerusopetuksenAlkuvaiheenSuoritus = {
  $class: 'AikuistenPerusopetuksenAlkuvaiheenSuoritus'
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
  $class: 'AikuistenPerusopetuksenAlkuvaiheenVierasKieli'
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenalkuvaiheenoppiaineet',
    'A1'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
}

export type AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus = {
  $class: 'AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus'
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
  $class: 'AikuistenPerusopetuksenKurssinSuoritus'
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
  $class: 'AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot'
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
  $class: 'AikuistenPerusopetuksenOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<AikuistenPerusopetuksenOpiskeluoikeusjakso>
}

export type AikuistenPerusopetuksenOpiskeluoikeus = {
  $class: 'AikuistenPerusopetuksenOpiskeluoikeus'
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
  $class: 'AikuistenPerusopetuksenOpiskeluoikeusjakso'
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
  $class: 'AikuistenPerusopetuksenOppiaineenOppimääränSuoritus'
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
  $class: 'AikuistenPerusopetuksenOppiaineenSuoritus'
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
  $class: 'AikuistenPerusopetuksenOppimääränSuoritus'
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
  $class: 'AikuistenPerusopetuksenPaikallinenOppiaine'
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
  $class: 'AikuistenPerusopetuksenUskonto'
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
}

export type AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli = {
  $class: 'AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli'
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
  $class: 'AikuistenPerusopetuksenÄidinkieliJaKirjallisuus'
  pakollinen: boolean
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
}

export type AikuistenPerusopetus = {
  $class: 'AikuistenPerusopetus'
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koulutus', '201101'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type AmmatillinenArviointi = {
  $class: 'AmmatillinenArviointi'
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
  $class: 'AmmatillinenOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<AmmatillinenOpiskeluoikeusjakso>
}

export type AmmatillinenOpiskeluoikeus = {
  $class: 'AmmatillinenOpiskeluoikeus'
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
  $class: 'AmmatillinenOpiskeluoikeusjakso'
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
  $class: 'AmmatillinenTutkintoKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', string>
  perusteenDiaarinumero?: string
  perusteenNimi?: LocalizedString
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type AmmatilliseenTehtäväänValmistavaKoulutus = {
  $class: 'AmmatilliseenTehtäväänValmistavaKoulutus'
  tunniste: Koodistokoodiviite<
    'ammatilliseentehtavaanvalmistavakoulutus',
    string
  >
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus?: LocalizedString
}

export type AmmatillisenOpiskeluoikeudenLisätiedot = {
  $class: 'AmmatillisenOpiskeluoikeudenLisätiedot'
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
  $class: 'AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus'
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
  $class: 'AmmatillisenTutkinnonOsaaPienempiKokonaisuus'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
}

export type AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =
  {
    $class: 'AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
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
  $class: 'AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillisentutkinnonosa'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: KorkeakouluopinnotTutkinnonOsa
  tutkinnonOsanRyhmä?: Koodistokoodiviite<'ammatillisentutkinnonosanryhma', '1'>
  osasuoritukset?: Array<KorkeakouluopintojenSuoritus>
}

export type AmmatillisenTutkinnonOsanLisätieto = {
  $class: 'AmmatillisenTutkinnonOsanLisätieto'
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
  $class: 'AmmatillisenTutkinnonOsittainenSuoritus'
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
  $class: 'AmmatillisenTutkinnonSuoritus'
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
  $class: 'AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli'
  tunniste: Koodistokoodiviite<'ammatillisenoppiaineet', 'VK' | 'TK1' | 'TK2'>
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export type AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla = {
  $class: 'AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla'
  tunniste: Koodistokoodiviite<
    'ammatillisenoppiaineet',
    'VVTK' | 'VVAI' | 'VVAI22' | 'VVVK'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export type AmmatillisenTutkinnonÄidinkieli = {
  $class: 'AmmatillisenTutkinnonÄidinkieli'
  tunniste: Koodistokoodiviite<'ammatillisenoppiaineet', 'AI'>
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export type Arvioitsija = {
  $class: 'Arvioitsija'
  nimi: string
}

export type DIANäyttötutkinto = {
  $class: 'DIANäyttötutkinto'
  tunniste: Koodistokoodiviite<'diapaattokoe', 'nayttotutkinto'>
}

export type DIAOpiskeluoikeudenLisätiedot = {
  $class: 'DIAOpiskeluoikeudenLisätiedot'
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  maksuttomuus?: Array<Maksuttomuus>
  ulkomainenVaihtoopiskelija?: boolean
  erityisenKoulutustehtävänJaksot?: Array<ErityisenKoulutustehtävänJakso>
  pidennettyPäättymispäivä?: boolean
}

export type DIAOpiskeluoikeudenTila = {
  $class: 'DIAOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<DIAOpiskeluoikeusjakso>
}

export type DIAOpiskeluoikeus = {
  $class: 'DIAOpiskeluoikeus'
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
  $class: 'DIAOpiskeluoikeusjakso'
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
  $class: 'DIAOppiaineenTutkintovaiheenLukukausi'
  tunniste: Koodistokoodiviite<'dialukukausi', '3' | '4' | '5' | '6'>
  laajuus?: LaajuusVuosiviikkotunneissa
}

export type DIAOppiaineenTutkintovaiheenNumeerinenArviointi = {
  $class: 'DIAOppiaineenTutkintovaiheenNumeerinenArviointi'
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
  $class: 'DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus'
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
  $class: 'DIAOppiaineenTutkintovaiheenSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'diaoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koetuloksenNelinkertainenPistemäärä?: number
  koulutusmoduuli: DIAOppiaine
  vastaavuustodistuksenTiedot?: DIAVastaavuustodistuksenTiedot
  osasuoritukset?: Array<DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus>
}

export type DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi = {
  $class: 'DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkodiatutkinto', 'S'>
  päivä?: string
  hyväksytty?: boolean
}

export type DIAOppiaineenValmistavanVaiheenLukukaudenArviointi = {
  $class: 'DIAOppiaineenValmistavanVaiheenLukukaudenArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkodiavalmistava', string>
  päivä?: string
  hyväksytty?: boolean
}

export type DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus = {
  $class: 'DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus'
  koulutusmoduuli: DIAOppiaineenValmistavanVaiheenLukukausi
  arviointi?: Array<DIAOppiaineenValmistavanVaiheenLukukaudenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'diaoppiaineenvalmistavanvaiheenlukukaudensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type DIAOppiaineenValmistavanVaiheenLukukausi = {
  $class: 'DIAOppiaineenValmistavanVaiheenLukukausi'
  tunniste: Koodistokoodiviite<'dialukukausi', '1' | '2'>
  laajuus?: LaajuusVuosiviikkotunneissa
}

export type DIAOppiaineenValmistavanVaiheenSuoritus = {
  $class: 'DIAOppiaineenValmistavanVaiheenSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'diaoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: DIAOppiaine
  osasuoritukset?: Array<DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus>
}

export type DIAOppiaineKieli = {
  $class: 'DIAOppiaineKieli'
  pakollinen: boolean
  osaAlue: Koodistokoodiviite<'diaosaalue', '1'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'EN' | 'FR' | 'SV' | 'RU'>
  laajuus?: LaajuusVuosiviikkotunneissa
  tunniste: Koodistokoodiviite<'oppiaineetdia', 'A' | 'B1' | 'B3'>
}

export type DIAOppiaineLisäaine = {
  $class: 'DIAOppiaineLisäaine'
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
  $class: 'DIAOppiaineLisäaineKieli'
  tunniste: Koodistokoodiviite<'oppiaineetdia', 'B2'>
  laajuus?: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'kielivalikoima', 'LA'>
}

export type DIAOppiaineMuu = {
  $class: 'DIAOppiaineMuu'
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
  $class: 'DIAOppiaineÄidinkieli'
  tunniste: Koodistokoodiviite<'oppiaineetdia', 'AI'>
  laajuus?: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'oppiainediaaidinkieli', 'FI' | 'S2' | 'DE'>
  osaAlue: Koodistokoodiviite<'diaosaalue', '1'>
}

export type DIAPäätasonSuoritus =
  | DIATutkinnonSuoritus
  | DIAValmistavanVaiheenSuoritus

export type DIAPäättökoe = {
  $class: 'DIAPäättökoe'
  tunniste: Koodistokoodiviite<
    'diapaattokoe',
    'kirjallinenkoe' | 'suullinenkoe'
  >
}

export type DIATutkinnonSuoritus = {
  $class: 'DIATutkinnonSuoritus'
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
  $class: 'DIATutkinto'
  tunniste: Koodistokoodiviite<'koulutus', '301103'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type DIATutkintovaiheenArviointi =
  | DIAOppiaineenTutkintovaiheenNumeerinenArviointi
  | DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi

export type DIAValmistavanVaiheenSuoritus = {
  $class: 'DIAValmistavanVaiheenSuoritus'
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
  $class: 'DIAValmistavaVaihe'
  tunniste: Koodistokoodiviite<'suorituksentyyppi', 'diavalmistavavaihe'>
}

export type DIAVastaavuustodistuksenTiedot = {
  $class: 'DIAVastaavuustodistuksenTiedot'
  keskiarvo: number
  lukioOpintojenLaajuus: LaajuusOpintopisteissäTaiKursseissa
}

export type DiplomaArviointi =
  | InternationalSchoolIBOppiaineenArviointi
  | NumeerinenInternationalSchoolOppiaineenArviointi
  | PassFailOppiaineenArviointi

export type DiplomaCoreRequirementsOppiaine = {
  $class: 'DiplomaCoreRequirementsOppiaine'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'TOK' | 'EE' | 'CAS'>
}

export type DiplomaCoreRequirementsOppiaineenSuoritus = {
  $class: 'DiplomaCoreRequirementsOppiaineenSuoritus'
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
  $class: 'DiplomaOppiaineenSuoritus'
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
  $class: 'DiplomaVuosiluokanSuoritus'
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
  $class: 'Duplikaatti'
  tyyppi: string
  arvo: string
}

export type EBArviointi =
  | EBTutkintoFinalMarkArviointi
  | EBTutkintoPreliminaryMarkArviointi

export type EBOppiaineenAlaosasuoritus = {
  $class: 'EBOppiaineenAlaosasuoritus'
  koulutusmoduuli: EBOppiaineKomponentti
  arviointi?: Array<EBArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinnonalaosasuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type EBOppiaineKomponentti = {
  $class: 'EBOppiaineKomponentti'
  tunniste: Koodistokoodiviite<'ebtutkinnonoppiaineenkomponentti', string>
}

export type EBTutkinnonOsasuoritus = {
  $class: 'EBTutkinnonOsasuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ebtutkinnonosasuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: SecondaryOppiaine
  osasuoritukset?: Array<EBOppiaineenAlaosasuoritus>
}

export type EBTutkinnonSuoritus = {
  $class: 'EBTutkinnonSuoritus'
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
  $class: 'EBTutkinto'
  tunniste: Koodistokoodiviite<'koulutus', '301104'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
}

export type EBTutkintoFinalMarkArviointi = {
  $class: 'EBTutkintoFinalMarkArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkifinalmark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}

export type EBTutkintoPreliminaryMarkArviointi = {
  $class: 'EBTutkintoPreliminaryMarkArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkis7preliminarymark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}

export type EiTiedossaOppiaine = {
  $class: 'EiTiedossaOppiaine'
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'XX'>
  perusteenDiaarinumero?: string
}

export type ErityisenKoulutustehtävänJakso = {
  $class: 'ErityisenKoulutustehtävänJakso'
  alku: string
  loppu?: string
  tehtävä: Koodistokoodiviite<'erityinenkoulutustehtava', string>
}

export type ErityisenTuenPäätös = {
  $class: 'ErityisenTuenPäätös'
  toteutuspaikka?: Koodistokoodiviite<'erityisopetuksentoteutuspaikka', string>
  opiskeleeToimintaAlueittain: boolean
  loppu?: string
  erityisryhmässä?: boolean
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
  alku?: string
}

export type EsiopetuksenOpiskeluoikeudenLisätiedot = {
  $class: 'EsiopetuksenOpiskeluoikeudenLisätiedot'
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
  $class: 'EsiopetuksenOpiskeluoikeus'
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
  $class: 'EsiopetuksenSuoritus'
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
  $class: 'Esiopetus'
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koulutus', '001101' | '001102'>
  kuvaus?: LocalizedString
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type EuropeanSchoolOfHelsinkiKielioppiaine = {
  $class: 'EuropeanSchoolOfHelsinkiKielioppiaine'
  tunniste: Koodistokoodiviite<'europeanschoolofhelsinkikielioppiaine', string>
  laajuus: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'kieli', string>
}

export type EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek = {
  $class: 'EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek'
  tunniste: Koodistokoodiviite<'europeanschoolofhelsinkikielioppiaine', 'GRC'>
  laajuus: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'kieli', 'EL'>
}

export type EuropeanSchoolOfHelsinkiKielioppiaineLatin = {
  $class: 'EuropeanSchoolOfHelsinkiKielioppiaineLatin'
  tunniste: Koodistokoodiviite<'europeanschoolofhelsinkikielioppiaine', 'LA'>
  laajuus: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'kieli', 'LA'>
}

export type EuropeanSchoolOfHelsinkiMuuOppiaine = {
  $class: 'EuropeanSchoolOfHelsinkiMuuOppiaine'
  tunniste: Koodistokoodiviite<'europeanschoolofhelsinkimuuoppiaine', string>
  laajuus: LaajuusVuosiviikkotunneissa
}

export type EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot = {
  $class: 'EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot'
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  maksuttomuus?: Array<Maksuttomuus>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
}

export type EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila = {
  $class: 'EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso>
}

export type EuropeanSchoolOfHelsinkiOpiskeluoikeus = {
  $class: 'EuropeanSchoolOfHelsinkiOpiskeluoikeus'
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
  $class: 'EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso'
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
  $class: 'EuropeanSchoolOfHelsinkiOsasuoritusArviointi'
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
  $class: 'FitnessAndWellBeing'
  tunniste: Koodistokoodiviite<'oppiaineetinternationalschool', 'HAWB'>
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}

export type Henkilö =
  | HenkilötiedotJaOid
  | OidHenkilö
  | TäydellisetHenkilötiedot
  | UusiHenkilö

export type HenkilötiedotJaOid = {
  $class: 'HenkilötiedotJaOid'
  sukunimi: string
  oid: string
  kutsumanimi: string
  hetu?: string
  etunimet: string
}

export type HenkilövahvistusPaikkakunnalla = {
  $class: 'HenkilövahvistusPaikkakunnalla'
  päivä: string
  paikkakunta: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio: Organisaatio
  myöntäjäHenkilöt: Array<Organisaatiohenkilö>
}

export type HenkilövahvistusValinnaisellaPaikkakunnalla = {
  $class: 'HenkilövahvistusValinnaisellaPaikkakunnalla'
  päivä: string
  paikkakunta?: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio: Organisaatio
  myöntäjäHenkilöt: Array<Organisaatiohenkilö>
}

export type HenkilövahvistusValinnaisellaTittelillä =
  HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla

export type HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla =
  {
    $class: 'HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla'
    päivä: string
    paikkakunta?: Koodistokoodiviite<'kunta', string>
    myöntäjäOrganisaatio: Organisaatio
    myöntäjäHenkilöt: Array<OrganisaatiohenkilöValinnaisellaTittelillä>
  }

export type Hojks = {
  $class: 'Hojks'
  opetusryhmä: Koodistokoodiviite<'opetusryhma', string>
  alku?: string
  loppu?: string
}

export type IBAineRyhmäOppiaine = IBOppiaineLanguage | IBOppiaineMuu

export type IBCASOppiaineenArviointi = {
  $class: 'IBCASOppiaineenArviointi'
  päivä?: string
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', 'S'>
  predicted: boolean
  hyväksytty?: boolean
}

export type IBCASSuoritus = {
  $class: 'IBCASSuoritus'
  arviointi?: Array<IBCASOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'iboppiainecas'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBOppiaineCAS
}

export type IBCoreRequirementsArviointi = {
  $class: 'IBCoreRequirementsArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkocorerequirementsib', string>
  predicted: boolean
  päivä?: string
  hyväksytty?: boolean
}

export type IBDiplomaLuokkaAste = {
  $class: 'IBDiplomaLuokkaAste'
  diplomaType: Koodistokoodiviite<'internationalschooldiplomatype', 'ib'>
  tunniste: Koodistokoodiviite<'internationalschoolluokkaaste', '11' | '12'>
}

export type IBExtendedEssaySuoritus = {
  $class: 'IBExtendedEssaySuoritus'
  arviointi?: Array<IBCoreRequirementsArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'iboppiaineee'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBOppiaineExtendedEssay
}

export type IBKurssi = {
  $class: 'IBKurssi'
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}

export type IBKurssinArviointi = {
  $class: 'IBKurssinArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', string>
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  päivä: string
  hyväksytty?: boolean
}

export type IBKurssinSuoritus = {
  $class: 'IBKurssinSuoritus'
  arviointi?: Array<IBKurssinArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ibkurssi'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBKurssi
}

export type IBOpiskeluoikeus = {
  $class: 'IBOpiskeluoikeus'
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
  $class: 'IBOppiaineCAS'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'CAS'>
  laajuus?: LaajuusTunneissa
  pakollinen: boolean
}

export type IBOppiaineenArviointi = {
  $class: 'IBOppiaineenArviointi'
  päivä?: string
  effort?: Koodistokoodiviite<'effortasteikkoib', string>
  arvosana: Koodistokoodiviite<'arviointiasteikkoib', string>
  predicted: boolean
  hyväksytty?: boolean
}

export type IBOppiaineenSuoritus = {
  $class: 'IBOppiaineenSuoritus'
  arviointi?: Array<IBOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'iboppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBAineRyhmäOppiaine
  osasuoritukset?: Array<IBKurssinSuoritus>
}

export type IBOppiaineExtendedEssay = {
  $class: 'IBOppiaineExtendedEssay'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'EE'>
  aine: IBAineRyhmäOppiaine
  aihe: LocalizedString
  pakollinen: boolean
}

export type IBOppiaineLanguage = {
  $class: 'IBOppiaineLanguage'
  pakollinen: boolean
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusTunneissa
  ryhmä: Koodistokoodiviite<'aineryhmaib', string>
  tunniste: Koodistokoodiviite<'oppiaineetib', 'A' | 'A2' | 'B' | 'AB'>
}

export type IBOppiaineMuu = {
  $class: 'IBOppiaineMuu'
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
  $class: 'IBOppiaineTheoryOfKnowledge'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'TOK'>
  pakollinen: boolean
}

export type IBPäätasonSuoritus =
  | IBTutkinnonSuoritus
  | PreIBSuoritus2015
  | PreIBSuoritus2019

export type IBTheoryOfKnowledgeSuoritus = {
  $class: 'IBTheoryOfKnowledgeSuoritus'
  arviointi?: Array<IBCoreRequirementsArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'iboppiainetok'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBOppiaineTheoryOfKnowledge
  osasuoritukset?: Array<IBKurssinSuoritus>
}

export type IBTutkinnonSuoritus = {
  $class: 'IBTutkinnonSuoritus'
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
  $class: 'IBTutkinto'
  tunniste: Koodistokoodiviite<'koulutus', '301102'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type InternationalSchoolCoreRequirementsArviointi = {
  $class: 'InternationalSchoolCoreRequirementsArviointi'
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
  $class: 'InternationalSchoolIBOppiaineenArviointi'
  predicted?: boolean
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoib',
    'S' | 'F' | '1' | '2' | '3' | '4' | '5' | '6' | '7'
  >
  päivä?: string
  hyväksytty?: boolean
}

export type InternationalSchoolMuuDiplomaOppiaine = {
  $class: 'InternationalSchoolMuuDiplomaOppiaine'
  tunniste: Koodistokoodiviite<
    'oppiaineetinternationalschool',
    'F' | 'HSCM' | 'ITGS' | 'MAA' | 'MAI' | 'INS'
  >
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}

export type InternationalSchoolOpiskeluoikeudenLisätiedot = {
  $class: 'InternationalSchoolOpiskeluoikeudenLisätiedot'
  erityisenKoulutustehtävänJaksot?: Array<ErityisenKoulutustehtävänJakso>
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  maksuttomuus?: Array<Maksuttomuus>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
}

export type InternationalSchoolOpiskeluoikeudenTila = {
  $class: 'InternationalSchoolOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<InternationalSchoolOpiskeluoikeusjakso>
}

export type InternationalSchoolOpiskeluoikeus = {
  $class: 'InternationalSchoolOpiskeluoikeus'
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
  $class: 'InternationalSchoolOpiskeluoikeusjakso'
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
  $class: 'ISHDiplomaLuokkaAste'
  diplomaType: Koodistokoodiviite<'internationalschooldiplomatype', 'ish'>
  tunniste: Koodistokoodiviite<'internationalschoolluokkaaste', '11' | '12'>
}

export type JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa = {
  $class: 'JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa'
  tunniste: Koodistokoodiviite<'tutkinnonosatvalinnanmahdollisuus', '1'>
  laajuus?: LaajuusOsaamispisteissä
}

export type Järjestämismuoto =
  | JärjestämismuotoIlmanLisätietoja
  | OppisopimuksellinenJärjestämismuoto

export type JärjestämismuotoIlmanLisätietoja = {
  $class: 'JärjestämismuotoIlmanLisätietoja'
  tunniste: Koodistokoodiviite<'jarjestamismuoto', string>
}

export type Järjestämismuotojakso = {
  $class: 'Järjestämismuotojakso'
  alku: string
  loppu?: string
  järjestämismuoto: Järjestämismuoto
}

export type KieliDiplomaOppiaine = {
  $class: 'KieliDiplomaOppiaine'
  tunniste: Koodistokoodiviite<'oppiaineetib', 'A' | 'A2' | 'B' | 'AB'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'EN' | 'ES' | 'FI' | 'FR'>
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}

export type Koodistokoodiviite<
  U extends string = string,
  A extends string = string
> = {
  $class: 'Koodistokoodiviite'
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
  $class: 'KorkeakoulunKoodistostaLöytyväArviointi'
  arvosana: Koodistokoodiviite<'virtaarvosana', string>
  päivä: string
  hyväksytty?: boolean
}

export type KorkeakoulunOpintojakso = {
  $class: 'KorkeakoulunOpintojakso'
  tunniste: PaikallinenKoodi
  nimi: LocalizedString
  laajuus?: Laajuus
}

export type KorkeakoulunOpintojaksonSuoritus = {
  $class: 'KorkeakoulunOpintojaksonSuoritus'
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
  $class: 'KorkeakoulunOpiskeluoikeudenLisätiedot'
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
  $class: 'KorkeakoulunOpiskeluoikeudenLukuvuosimaksu'
  alku: string
  loppu?: string
  summa?: number
}

export type KorkeakoulunOpiskeluoikeudenTila = {
  $class: 'KorkeakoulunOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<KorkeakoulunOpiskeluoikeusjakso>
}

export type KorkeakoulunOpiskeluoikeus = {
  $class: 'KorkeakoulunOpiskeluoikeus'
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
  $class: 'KorkeakoulunOpiskeluoikeusjakso'
  alku: string
  nimi?: LocalizedString
  tila: Koodistokoodiviite<'virtaopiskeluoikeudentila', string>
}

export type KorkeakoulunPaikallinenArviointi = {
  $class: 'KorkeakoulunPaikallinenArviointi'
  arvosana: PaikallinenKoodi
  päivä: string
  hyväksytty?: boolean
}

export type KorkeakouluopinnotTutkinnonOsa = {
  $class: 'KorkeakouluopinnotTutkinnonOsa'
  tunniste: Koodistokoodiviite<'tutkinnonosatvalinnanmahdollisuus', '2'>
  laajuus?: LaajuusOsaamispisteissä
}

export type KorkeakouluopintojenSuoritus = {
  $class: 'KorkeakouluopintojenSuoritus'
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
  $class: 'KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
}

export type KorkeakouluSuoritus =
  | KorkeakoulunOpintojaksonSuoritus
  | KorkeakoulututkinnonSuoritus
  | MuuKorkeakoulunSuoritus

export type KorkeakoulututkinnonSuoritus = {
  $class: 'KorkeakoulututkinnonSuoritus'
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
  $class: 'Korkeakoulututkinto'
  tunniste: Koodistokoodiviite<'koulutus', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  virtaNimi?: LocalizedString
}

export type Koulutussopimusjakso = {
  $class: 'Koulutussopimusjakso'
  työssäoppimispaikka?: LocalizedString
  työssäoppimispaikanYTunnus?: string
  paikkakunta: Koodistokoodiviite<'kunta', string>
  loppu?: string
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  alku: string
  työtehtävät?: LocalizedString
}

export type Koulutustoimija = {
  $class: 'Koulutustoimija'
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
  $class: 'LaajuusKaikkiYksiköt'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', string>
}

export type LaajuusKursseissa = {
  $class: 'LaajuusKursseissa'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '4'>
}

export type LaajuusOpintopisteissä = {
  $class: 'LaajuusOpintopisteissä'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '2'>
}

export type LaajuusOpintopisteissäTaiKursseissa =
  | LaajuusKursseissa
  | LaajuusOpintopisteissä

export type LaajuusOpintoviikoissa = {
  $class: 'LaajuusOpintoviikoissa'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '1'>
}

export type LaajuusOsaamispisteissä = {
  $class: 'LaajuusOsaamispisteissä'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '6'>
}

export type LaajuusTunneissa = {
  $class: 'LaajuusTunneissa'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '5'>
}

export type LaajuusViikoissa = {
  $class: 'LaajuusViikoissa'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '8'>
}

export type LaajuusVuosiviikkotunneissa = {
  $class: 'LaajuusVuosiviikkotunneissa'
  arvo: number
  yksikkö: Koodistokoodiviite<'opintojenlaajuusyksikko', '3'>
}

export type LaajuusVuosiviikkotunneissaTaiKursseissa =
  | LaajuusKursseissa
  | LaajuusVuosiviikkotunneissa

export type LanguageAcquisition = {
  $class: 'LanguageAcquisition'
  tunniste: Koodistokoodiviite<'oppiaineetinternationalschool', 'LAC'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'ES' | 'FI' | 'FR' | 'EN'>
}

export type LanguageAndLiterature = {
  $class: 'LanguageAndLiterature'
  tunniste: Koodistokoodiviite<'oppiaineetinternationalschool', 'LL'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'EN' | 'FI'>
}

export type Lukiodiplomit2019 = {
  $class: 'Lukiodiplomit2019'
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
  $class: 'LukionKurssinSuoritus2015'
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
  $class: 'LukionMatematiikka2015'
  pakollinen: boolean
  oppimäärä: Koodistokoodiviite<'oppiainematematiikka', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'MA'>
}

export type LukionMatematiikka2019 = {
  $class: 'LukionMatematiikka2019'
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'MA'>
  oppimäärä: Koodistokoodiviite<'oppiainematematiikka', string>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}

export type LukionModuuliMuissaOpinnoissa2019 =
  | LukionMuuModuuliMuissaOpinnoissa2019
  | LukionVieraanKielenModuuliMuissaOpinnoissa2019

export type LukionModuulinSuoritusMuissaOpinnoissa2019 = {
  $class: 'LukionModuulinSuoritusMuissaOpinnoissa2019'
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
  $class: 'LukionModuulinSuoritusOppiaineissa2019'
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
  $class: 'LukionMuuModuuliMuissaOpinnoissa2019'
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
}

export type LukionMuuModuuliOppiaineissa2019 = {
  $class: 'LukionMuuModuuliOppiaineissa2019'
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
}

export type LukionMuuValtakunnallinenOppiaine2015 = {
  $class: 'LukionMuuValtakunnallinenOppiaine2015'
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
  $class: 'LukionMuuValtakunnallinenOppiaine2019'
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
  $class: 'LukionOpiskeluoikeudenLisätiedot'
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
  $class: 'LukionOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<LukionOpiskeluoikeusjakso>
}

export type LukionOpiskeluoikeus = {
  $class: 'LukionOpiskeluoikeus'
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
  $class: 'LukionOpiskeluoikeusjakso'
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
  $class: 'LukionOppiaineenArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', string>
  päivä?: string
  hyväksytty?: boolean
}

export type LukionOppiaineenArviointi2019 =
  | NumeerinenLukionOppiaineenArviointi2019
  | SanallinenLukionOppiaineenArviointi2019

export type LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa =
  {
    $class: 'LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa'
    arviointi?: Array<LukionOppiaineenArviointi>
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'luvalukionoppiaine'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    koulutusmoduuli: LukionOppiaine2015
    osasuoritukset?: Array<LukionKurssinSuoritus2015>
  }

export type LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019 =
  {
    $class: 'LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019'
    arviointi?: Array<LukionOppiaineenArviointi2019>
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'luvalukionoppiaine2019'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    suoritettuErityisenäTutkintona?: boolean
    suorituskieli?: Koodistokoodiviite<'kieli', string>
    koulutusmoduuli: LukionOppiaine2019
    osasuoritukset?: Array<LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019>
  }

export type LukionOppiaineenOppimääränSuoritus2015 = {
  $class: 'LukionOppiaineenOppimääränSuoritus2015'
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
  $class: 'LukionOppiaineenPreIBSuoritus2019'
  arviointi?: Array<LukionOppiaineenArviointi2019>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suoritettuErityisenäTutkintona?: boolean
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PreIBLukionOppiaine2019
  osasuoritukset?: Array<PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019>
}

export type LukionOppiaineenSuoritus2015 = {
  $class: 'LukionOppiaineenSuoritus2015'
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionOppiaine2015
  osasuoritukset?: Array<LukionKurssinSuoritus2015>
}

export type LukionOppiaineenSuoritus2019 = {
  $class: 'LukionOppiaineenSuoritus2019'
  arviointi?: Array<LukionOppiaineenArviointi2019>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suoritettuErityisenäTutkintona?: boolean
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionOppiaine2019
  osasuoritukset?: Array<LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019>
}

export type LukionOppiaineidenOppimäärienSuoritus2019 = {
  $class: 'LukionOppiaineidenOppimäärienSuoritus2019'
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
  $class: 'LukionOppiaineidenOppimäärät2019'
  tunniste: LukionOppiaineidenOppimäärätKoodi2019
  perusteenDiaarinumero?: string
}

export type LukionOppiaineidenOppimäärätKoodi2019 = {
  $class: 'LukionOppiaineidenOppimäärätKoodi2019'
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
  $class: 'LukionOppimäärä'
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
  $class: 'LukionOppimääränSuoritus2015'
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
  $class: 'LukionOppimääränSuoritus2019'
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
  $class: 'LukionPaikallinenOpintojakso2019'
  tunniste: PaikallinenKoodi
  laajuus: LaajuusOpintopisteissä
  kuvaus: LocalizedString
  pakollinen: boolean
}

export type LukionPaikallisenOpintojaksonSuoritus2019 = {
  $class: 'LukionPaikallisenOpintojaksonSuoritus2019'
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
  $class: 'LukionUskonto2015'
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
}

export type LukionUskonto2019 = {
  $class: 'LukionUskonto2019'
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
}

export type LukionVieraanKielenModuuliMuissaOpinnoissa2019 = {
  $class: 'LukionVieraanKielenModuuliMuissaOpinnoissa2019'
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
}

export type LukionVieraanKielenModuuliOppiaineissa2019 = {
  $class: 'LukionVieraanKielenModuuliOppiaineissa2019'
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
  kieli?: Koodistokoodiviite<'kielivalikoima', string>
}

export type LukionÄidinkieliJaKirjallisuus2015 = {
  $class: 'LukionÄidinkieliJaKirjallisuus2015'
  pakollinen: boolean
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
}

export type LukionÄidinkieliJaKirjallisuus2019 = {
  $class: 'LukionÄidinkieliJaKirjallisuus2019'
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}

export type LukioonValmistavaKoulutus = {
  $class: 'LukioonValmistavaKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999906'>
  perusteenDiaarinumero?: string
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type LukioonValmistavanKoulutuksenKurssi =
  | PaikallinenLukioonValmistavanKoulutuksenKurssi
  | ValtakunnallinenLukioonValmistavanKoulutuksenKurssi

export type LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot = {
  $class: 'LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot'
  ulkomaanjaksot?: Array<Ulkomaanjakso>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
  oikeusMaksuttomaanAsuntolapaikkaan?: boolean
  maksuttomuus?: Array<Maksuttomuus>
  ulkomainenVaihtoopiskelija?: boolean
  pidennettyPäättymispäivä?: boolean
  sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
}

export type LukioonValmistavanKoulutuksenOpiskeluoikeus = {
  $class: 'LukioonValmistavanKoulutuksenOpiskeluoikeus'
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
  $class: 'LukioonValmistavanKoulutuksenOppiaineenSuoritus'
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
  $class: 'LukioonValmistavanKoulutuksenSuoritus'
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
  $class: 'LukioonValmistavanKurssinSuoritus'
  arviointi?: Array<LukionArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'luvakurssi'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukioonValmistavanKoulutuksenKurssi
}

export type LukioonValmistavaÄidinkieliJaKirjallisuus = {
  $class: 'LukioonValmistavaÄidinkieliJaKirjallisuus'
  tunniste: Koodistokoodiviite<'oppiaineetluva', 'LVAIK'>
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', 'AI7' | 'AI8'>
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}

export type LukioOpintojenSuoritus = {
  $class: 'LukioOpintojenSuoritus'
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
  $class: 'Lukukausi_Ilmoittautuminen'
  ilmoittautumisjaksot: Array<Lukukausi_Ilmoittautumisjakso>
}

export type Lukukausi_Ilmoittautumisjakso = {
  $class: 'Lukukausi_Ilmoittautumisjakso'
  tila: Koodistokoodiviite<'virtalukukausiilmtila', string>
  maksetutLukuvuosimaksut?: Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu
  ylioppilaskunnanJäsen?: boolean
  ythsMaksettu?: boolean
  loppu?: string
  alku: string
}

export type LukutaitokoulutuksenArviointi = {
  $class: 'LukutaitokoulutuksenArviointi'
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
  $class: 'Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu'
  maksettu?: boolean
  summa?: number
  apuraha?: number
}

export type LähdejärjestelmäId = {
  $class: 'LähdejärjestelmäId'
  id?: string
  lähdejärjestelmä: Koodistokoodiviite<'lahdejarjestelma', string>
}

export type Maksuttomuus = {
  $class: 'Maksuttomuus'
  alku: string
  loppu?: string
  maksuton: boolean
}

export type MuidenLukioOpintojenPreIBSuoritus2019 = {
  $class: 'MuidenLukioOpintojenPreIBSuoritus2019'
  koulutusmoduuli: PreIBMuutSuorituksetTaiVastaavat2019
  osasuoritukset?: Array<PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionmuuopinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type MuidenLukioOpintojenSuoritus2015 = {
  $class: 'MuidenLukioOpintojenSuoritus2015'
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionmuuopinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: MuuLukioOpinto2015
  osasuoritukset?: Array<LukionKurssinSuoritus2015>
}

export type MuidenLukioOpintojenSuoritus2019 = {
  $class: 'MuidenLukioOpintojenSuoritus2019'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionmuuopinto'>
  koulutusmoduuli: MuutSuorituksetTaiVastaavat2019
  osasuoritukset?: Array<LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type MuidenOpintovalmiuksiaTukevienOpintojenSuoritus = {
  $class: 'MuidenOpintovalmiuksiaTukevienOpintojenSuoritus'
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
  $class: 'MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine'
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenalkuvaiheenoppiaineet',
    'MA' | 'YH' | 'YL' | 'TE' | 'OP'
  >
}

export type MuuAikuistenPerusopetuksenOppiaine = {
  $class: 'MuuAikuistenPerusopetuksenOppiaine'
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
    $class: 'MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus'
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
  $class: 'MuuallaSuoritetutVapaanSivistystyönOpinnot'
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
  $class: 'MuuDiplomaOppiaine'
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
  $class: 'MuuKorkeakoulunOpinto'
  tunniste: Koodistokoodiviite<'virtaopiskeluoikeudentyyppi', string>
  nimi: LocalizedString
  laajuus?: Laajuus
}

export type MuuKorkeakoulunSuoritus = {
  $class: 'MuuKorkeakoulunSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'muukorkeakoulunsuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuuKorkeakoulunOpinto
  toimipiste: Oppilaitos
  osasuoritukset?: Array<KorkeakoulunOpintojaksonSuoritus>
  vahvistus?: Päivämäärävahvistus
}

export type MuuKuinSäänneltyKoulutus = {
  $class: 'MuuKuinSäänneltyKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999951'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusTunneissa
  opintokokonaisuus: Koodistokoodiviite<'opintokokonaisuudet', string>
}

export type MuuKuinYhteinenTutkinnonOsa =
  | MuuValtakunnallinenTutkinnonOsa
  | PaikallinenTutkinnonOsa

export type MuuLukioOpinto2015 = {
  $class: 'MuuLukioOpinto2015'
  tunniste: Koodistokoodiviite<'lukionmuutopinnot', string>
  laajuus?: LaajuusKursseissa
}

export type MuunAmmatillisenKoulutuksenArviointi = {
  $class: 'MuunAmmatillisenKoulutuksenArviointi'
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
  $class: 'MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto'
  tunniste: Koodistokoodiviite<'ammatillisentutkinnonosanlisatieto', string>
  kuvaus: LocalizedString
}

export type MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus = {
  $class: 'MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus'
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
  $class: 'MuunAmmatillisenKoulutuksenOsasuoritus'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}

export type MuunAmmatillisenKoulutuksenSuoritus = {
  $class: 'MuunAmmatillisenKoulutuksenSuoritus'
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
  $class: 'MuunAmmatillisenTutkinnonOsanSuoritus'
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
  $class: 'MuunKuinSäännellynKoulutuksenArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkomuks',
    'hyvaksytty' | 'hylatty'
  >
  arviointipäivä?: string
  hyväksytty?: boolean
}

export type MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso = {
  $class: 'MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso'
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'lasna' | 'hyvaksytystisuoritettu' | 'keskeytynyt' | 'mitatoity'
  >
  alku: string
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '14' | '15'>
}

export type MuunKuinSäännellynKoulutuksenOpiskeluoikeus = {
  $class: 'MuunKuinSäännellynKoulutuksenOpiskeluoikeus'
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
  $class: 'MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli'
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  laajuus: LaajuusTunneissa
}

export type MuunKuinSäännellynKoulutuksenOsasuoritus = {
  $class: 'MuunKuinSäännellynKoulutuksenOsasuoritus'
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
  $class: 'MuunKuinSäännellynKoulutuksenPäätasonSuoritus'
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
  $class: 'MuunKuinSäännellynKoulutuksenTila'
  opiskeluoikeusjaksot: Array<MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso>
}

export type MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus = {
  $class: 'MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'
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
  $class: 'MuuNuortenPerusopetuksenOppiaine'
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
  $class: 'MuuPerusopetuksenLisäopetuksenKoulutusmoduuli'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusVuosiviikkotunneissa
}

export type MuuPerusopetuksenLisäopetuksenSuoritus = {
  $class: 'MuuPerusopetuksenLisäopetuksenSuoritus'
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
  $class: 'MuutKielet'
  tunniste: Koodistokoodiviite<
    'oppiaineetluva',
    'LVMUUTK' | 'LVAK' | 'LVMAI' | 'LVPOAK'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}

export type MuutLukionSuoritukset2019 = {
  $class: 'MuutLukionSuoritukset2019'
  tunniste: Koodistokoodiviite<'lukionmuutopinnot', 'MS'>
  laajuus?: LaajuusOpintopisteissä
}

export type MuutSuorituksetTaiVastaavat2019 =
  | Lukiodiplomit2019
  | MuutLukionSuoritukset2019
  | TemaattisetOpinnot2019

export type MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine = {
  $class: 'MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine'
  tunniste: Koodistokoodiviite<
    'oppiaineetluva',
    'LVMALUO' | 'LVYHKU' | 'LVOPO' | 'LVMFKBM' | 'LVHIYH'
  >
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}

export type MuuValtakunnallinenTutkinnonOsa = {
  $class: 'MuuValtakunnallinenTutkinnonOsa'
  tunniste: Koodistokoodiviite<'tutkinnonosat', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
  kuvaus?: LocalizedString
}

export type MYPArviointi =
  | NumeerinenInternationalSchoolOppiaineenArviointi
  | PassFailOppiaineenArviointi

export type MYPLuokkaAste = {
  $class: 'MYPLuokkaAste'
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
  $class: 'MYPOppiaineenSuoritus'
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
  $class: 'MYPOppiaineMuu'
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
  $class: 'MYPVuosiluokanSuoritus'
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
  $class: 'NumeerinenInternationalSchoolOppiaineenArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoib',
    'S' | 'F' | '1' | '2' | '3' | '4' | '5' | '6' | '7'
  >
  päivä?: string
  hyväksytty?: boolean
}

export type NumeerinenLukionArviointi = {
  $class: 'NumeerinenLukionArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä: string
  hyväksytty?: boolean
}

export type NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 = {
  $class: 'NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä: string
  hyväksytty?: boolean
}

export type NumeerinenLukionOppiaineenArviointi2019 = {
  $class: 'NumeerinenLukionOppiaineenArviointi2019'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä?: string
  hyväksytty?: boolean
}

export type NumeerinenPerusopetuksenOppiaineenArviointi = {
  $class: 'NumeerinenPerusopetuksenOppiaineenArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  päivä?: string
  hyväksytty?: boolean
}

export type NuortenPerusopetuksenOpiskeluoikeudenTila = {
  $class: 'NuortenPerusopetuksenOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<NuortenPerusopetuksenOpiskeluoikeusjakso>
}

export type NuortenPerusopetuksenOpiskeluoikeusjakso = {
  $class: 'NuortenPerusopetuksenOpiskeluoikeusjakso'
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
  $class: 'NuortenPerusopetuksenOppiaineenOppimääränSuoritus'
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
  $class: 'NuortenPerusopetuksenOppiaineenSuoritus'
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
  $class: 'NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa'
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
  $class: 'NuortenPerusopetuksenOppimääränSuoritus'
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
  $class: 'NuortenPerusopetuksenPaikallinenOppiaine'
  pakollinen?: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: PaikallinenKoodi
}

export type NuortenPerusopetuksenUskonto = {
  $class: 'NuortenPerusopetuksenUskonto'
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
}

export type NuortenPerusopetuksenVierasTaiToinenKotimainenKieli = {
  $class: 'NuortenPerusopetuksenVierasTaiToinenKotimainenKieli'
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
  $class: 'NuortenPerusopetuksenÄidinkieliJaKirjallisuus'
  pakollinen: boolean
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
}

export type NuortenPerusopetus = {
  $class: 'NuortenPerusopetus'
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koulutus', '201101'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type NurseryLuokkaAste = {
  $class: 'NurseryLuokkaAste'
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'N1' | 'N2'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
}

export type NurseryVuosiluokanSuoritus = {
  $class: 'NurseryVuosiluokanSuoritus'
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
  $class: 'Näyttö'
  arviointi?: NäytönArviointi
  suorituspaikka?: NäytönSuorituspaikka
  haluaaTodistuksen?: boolean
  työssäoppimisenYhteydessä?: boolean
  kuvaus?: LocalizedString
  suoritusaika?: NäytönSuoritusaika
}

export type NäyttötutkintoonValmistavaKoulutus = {
  $class: 'NäyttötutkintoonValmistavaKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999904'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type NäyttötutkintoonValmistavanKoulutuksenOsa =
  | MuuValtakunnallinenTutkinnonOsa
  | PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa
  | YhteinenTutkinnonOsa

export type NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus = {
  $class: 'NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus'
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
  $class: 'NäyttötutkintoonValmistavanKoulutuksenSuoritus'
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
  $class: 'NäytönArviointi'
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
  $class: 'NäytönArviointikohde'
  tunniste: Koodistokoodiviite<'ammatillisennaytonarviointikohde', string>
  arvosana: Koodistokoodiviite<
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
}

export type NäytönArvioitsija = {
  $class: 'NäytönArvioitsija'
  nimi: string
  ntm?: boolean
}

export type NäytönSuoritusaika = {
  $class: 'NäytönSuoritusaika'
  alku: string
  loppu: string
}

export type NäytönSuorituspaikka = {
  $class: 'NäytönSuorituspaikka'
  tunniste: Koodistokoodiviite<'ammatillisennaytonsuorituspaikka', string>
  kuvaus: LocalizedString
}

export type OidHenkilö = {
  $class: 'OidHenkilö'
  oid: string
}

export type OidOrganisaatio = {
  $class: 'OidOrganisaatio'
  oid: string
  nimi?: LocalizedString
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}

export type OikeuttaMaksuttomuuteenPidennetty = {
  $class: 'OikeuttaMaksuttomuuteenPidennetty'
  alku: string
  loppu: string
}

export type OmanÄidinkielenOpinnotLaajuusKursseina = {
  $class: 'OmanÄidinkielenOpinnotLaajuusKursseina'
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
  $class: 'OmanÄidinkielenOpinnotLaajuusOpintopisteinä'
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
  $class: 'OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina'
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
  $class: 'OpiskeluoikeudenOrganisaatiohistoria'
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
  $class: 'OpiskeluoikeusAvaintaEiLöydy'
  tyyppi: string
  arvo: string
}

export type OpiskeluvalmiuksiaTukevienOpintojenJakso = {
  $class: 'OpiskeluvalmiuksiaTukevienOpintojenJakso'
  alku: string
  loppu: string
  kuvaus: LocalizedString
}

export type OppiaineenTaiToiminta_AlueenSuoritus =
  | NuortenPerusopetuksenOppiaineenSuoritus
  | PerusopetuksenToiminta_AlueenSuoritus

export type Oppilaitos = {
  $class: 'Oppilaitos'
  oid: string
  oppilaitosnumero?: Koodistokoodiviite<'oppilaitosnumero', string>
  nimi?: LocalizedString
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}

export type OppisopimuksellinenJärjestämismuoto = {
  $class: 'OppisopimuksellinenJärjestämismuoto'
  tunniste: Koodistokoodiviite<'jarjestamismuoto', '20'>
  oppisopimus: Oppisopimus
}

export type OppisopimuksellinenOsaamisenHankkimistapa = {
  $class: 'OppisopimuksellinenOsaamisenHankkimistapa'
  tunniste: Koodistokoodiviite<'osaamisenhankkimistapa', 'oppisopimus'>
  oppisopimus: Oppisopimus
}

export type OppisopimuksenPurkaminen = {
  $class: 'OppisopimuksenPurkaminen'
  päivä: string
  purettuKoeajalla: boolean
}

export type Oppisopimus = {
  $class: 'Oppisopimus'
  työnantaja: Yritys
  oppisopimuksenPurkaminen?: OppisopimuksenPurkaminen
}

export type OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =
  {
    $class: 'OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
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
    $class: 'OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
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
  $class: 'OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus'
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
  $class: 'OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999909'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus = {
  $class: 'OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus: LaajuusOpintopisteissä
}

export type OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso = {
  $class: 'OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso'
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
  $class: 'OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus'
  tunniste: Koodistokoodiviite<'vstosaamiskokonaisuus', string>
  laajuus?: LaajuusOpintopisteissä
}

export type OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi =
  {
    $class: 'OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi'
    arvosana: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
    päivä: string
    hyväksytty?: boolean
  }

export type OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus =
  {
    $class: 'OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus'
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'vstopintokokonaisuus'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus
    tunnustettu?: VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen
  }

export type OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =
  {
    $class: 'OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus'
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
    $class: 'OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot'
    tunniste: Koodistokoodiviite<
      'vstmuutopinnot',
      'valinnaisetsuuntautumisopinnot'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export type OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus =
  {
    $class: 'OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus'
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
  $class: 'Organisaatiohenkilö'
  nimi: string
  titteli: LocalizedString
  organisaatio: Organisaatio
}

export type OrganisaatiohenkilöValinnaisellaTittelillä = {
  $class: 'OrganisaatiohenkilöValinnaisellaTittelillä'
  nimi: string
  titteli?: LocalizedString
  organisaatio: Organisaatio
}

export type Organisaatiovahvistus = {
  $class: 'Organisaatiovahvistus'
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
  $class: 'OsaAikaisuusJakso'
  alku: string
  loppu?: string
  osaAikaisuus: number
}

export type Osaamisalajakso =
  | {
      $class: 'Osaamisalajakso'
      osaamisala: Koodistokoodiviite<'osaamisala', string>
      alku?: string
      loppu?: string
    }
  | Koodistokoodiviite<'osaamisala', string>

export type OsaamisenHankkimistapa =
  | OppisopimuksellinenOsaamisenHankkimistapa
  | OsaamisenHankkimistapaIlmanLisätietoja

export type OsaamisenHankkimistapaIlmanLisätietoja = {
  $class: 'OsaamisenHankkimistapaIlmanLisätietoja'
  tunniste: Koodistokoodiviite<'osaamisenhankkimistapa', string>
}

export type OsaamisenHankkimistapajakso = {
  $class: 'OsaamisenHankkimistapajakso'
  alku: string
  loppu?: string
  osaamisenHankkimistapa: OsaamisenHankkimistapa
}

export type OsaamisenTunnustaminen = {
  $class: 'OsaamisenTunnustaminen'
  osaaminen?: Suoritus
  selite: LocalizedString
  rahoituksenPiirissä?: boolean
}

export type OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =
  {
    $class: 'OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
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
  $class: 'OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'
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
  $class: 'PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export type PaikallinenAikuistenPerusopetuksenKurssi = {
  $class: 'PaikallinenAikuistenPerusopetuksenKurssi'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export type PaikallinenAmmatillisenTutkinnonOsanOsaAlue = {
  $class: 'PaikallinenAmmatillisenTutkinnonOsanOsaAlue'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export type PaikallinenKoodi = {
  $class: 'PaikallinenKoodi'
  koodiarvo: string
  nimi: LocalizedString
  koodistoUri?: string
}

export type PaikallinenLukionKurssi2015 = {
  $class: 'PaikallinenLukionKurssi2015'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKursseissa
  kuvaus: LocalizedString
  kurssinTyyppi: Koodistokoodiviite<'lukionkurssintyyppi', string>
}

export type PaikallinenLukionOpinto = {
  $class: 'PaikallinenLukionOpinto'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
  perusteenDiaarinumero: string
}

export type PaikallinenLukionOppiaine2015 = {
  $class: 'PaikallinenLukionOppiaine2015'
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
  kuvaus: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: PaikallinenKoodi
}

export type PaikallinenLukionOppiaine2019 = {
  $class: 'PaikallinenLukionOppiaine2019'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}

export type PaikallinenLukioonValmistavanKoulutuksenKurssi = {
  $class: 'PaikallinenLukioonValmistavanKoulutuksenKurssi'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
  kuvaus: LocalizedString
}

export type PaikallinenLukioonValmistavanKoulutuksenOppiaine = {
  $class: 'PaikallinenLukioonValmistavanKoulutuksenOppiaine'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}

export type PaikallinenMuuAmmatillinenKoulutus = {
  $class: 'PaikallinenMuuAmmatillinenKoulutus'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}

export type PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa = {
  $class: 'PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
}

export type PaikallinenOpintovalmiuksiaTukevaOpinto = {
  $class: 'PaikallinenOpintovalmiuksiaTukevaOpinto'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
}

export type PaikallinenTelmaKoulutuksenOsa = {
  $class: 'PaikallinenTelmaKoulutuksenOsa'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
  pakollinen: boolean
}

export type PaikallinenTutkinnonOsa = {
  $class: 'PaikallinenTutkinnonOsa'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export type PaikallinenValmaKoulutuksenOsa = {
  $class: 'PaikallinenValmaKoulutuksenOsa'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
  pakollinen: boolean
}

export type PassFailOppiaineenArviointi = {
  $class: 'PassFailOppiaineenArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkointernationalschool',
    'pass' | 'fail'
  >
  päivä?: string
  hyväksytty?: boolean
}

export type PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila = {
  $class: 'PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso>
}

export type PerusopetukseenValmistavanOpetuksenOpiskeluoikeus = {
  $class: 'PerusopetukseenValmistavanOpetuksenOpiskeluoikeus'
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
  $class: 'PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso'
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
  $class: 'PerusopetukseenValmistavanOpetuksenOppiaine'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  opetuksenSisältö?: LocalizedString
}

export type PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus = {
  $class: 'PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus'
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
  $class: 'PerusopetukseenValmistavanOpetuksenSuoritus'
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
  $class: 'PerusopetukseenValmistavaOpetus'
  tunniste: Koodistokoodiviite<'koulutus', '999905'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type PerusopetuksenKäyttäytymisenArviointi = {
  $class: 'PerusopetuksenKäyttäytymisenArviointi'
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
  $class: 'PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot'
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
  $class: 'PerusopetuksenLisäopetuksenOpiskeluoikeus'
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
  $class: 'PerusopetuksenLisäopetuksenOppiaineenSuoritus'
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
  $class: 'PerusopetuksenLisäopetuksenSuoritus'
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
  $class: 'PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus'
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
  $class: 'PerusopetuksenLisäopetus'
  tunniste: Koodistokoodiviite<'koulutus', '020075'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type PerusopetuksenLuokkaAste = {
  $class: 'PerusopetuksenLuokkaAste'
  tunniste: Koodistokoodiviite<'perusopetuksenluokkaaste' | 'koulutus', string>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type PerusopetuksenOpiskeluoikeudenLisätiedot = {
  $class: 'PerusopetuksenOpiskeluoikeudenLisätiedot'
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
  $class: 'PerusopetuksenOpiskeluoikeus'
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
  $class: 'PerusopetuksenToiminta_Alue'
  tunniste: Koodistokoodiviite<'perusopetuksentoimintaalue', string>
  laajuus?: LaajuusVuosiviikkotunneissa
}

export type PerusopetuksenToiminta_AlueenSuoritus = {
  $class: 'PerusopetuksenToiminta_AlueenSuoritus'
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksentoimintaalue'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PerusopetuksenToiminta_Alue
}

export type PerusopetuksenVuosiluokanSuorituksenLiite = {
  $class: 'PerusopetuksenVuosiluokanSuorituksenLiite'
  tunniste: Koodistokoodiviite<
    'perusopetuksentodistuksenliitetieto',
    'kayttaytyminen' | 'tyoskentely'
  >
  kuvaus: LocalizedString
}

export type PerusopetuksenVuosiluokanSuoritus = {
  $class: 'PerusopetuksenVuosiluokanSuoritus'
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
  $class: 'PreIBKoulutusmoduuli2015'
  tunniste: Koodistokoodiviite<'suorituksentyyppi', 'preiboppimaara'>
}

export type PreIBKoulutusmoduuli2019 = {
  $class: 'PreIBKoulutusmoduuli2019'
  tunniste: Koodistokoodiviite<'suorituksentyyppi', 'preiboppimaara2019'>
}

export type PreIBKurssi2015 =
  | IBKurssi
  | PaikallinenLukionKurssi2015
  | ValtakunnallinenLukionKurssi2015

export type PreIBKurssinSuoritus2015 = {
  $class: 'PreIBKurssinSuoritus2015'
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
  $class: 'PreIBLukionModuulinSuoritusMuissaOpinnoissa2019'
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
  $class: 'PreIBLukionModuulinSuoritusOppiaineissa2019'
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
  $class: 'PreIBLukionPaikallisenOpintojaksonSuoritus2019'
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
  $class: 'PreIBOppiaineenSuoritus2015'
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
  $class: 'PreIBSuoritus2015'
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
  $class: 'PreIBSuoritus2019'
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
  $class: 'PrimaryAlaoppimisalue'
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiprimaryalaoppimisalue',
    string
  >
}

export type PrimaryAlaoppimisalueArviointi = {
  $class: 'PrimaryAlaoppimisalueArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoeuropeanschoolofhelsinkiprimarymark',
    string
  >
  päivä?: string
  arvioitsijat?: Array<Arvioitsija>
  hyväksytty?: boolean
}

export type PrimaryLapsiAlaoppimisalue = {
  $class: 'PrimaryLapsiAlaoppimisalue'
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiprimarylapsialaoppimisalue',
    string
  >
}

export type PrimaryLapsiOppimisalue = {
  $class: 'PrimaryLapsiOppimisalue'
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkilapsioppimisalue',
    string
  >
}

export type PrimaryLapsiOppimisalueenAlaosasuoritus = {
  $class: 'PrimaryLapsiOppimisalueenAlaosasuoritus'
  koulutusmoduuli: PrimaryLapsiAlaoppimisalue
  arviointi?: Array<PrimaryAlaoppimisalueArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkialaosasuoritusprimarylapsi'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type PrimaryLapsiOppimisalueenSuoritus = {
  $class: 'PrimaryLapsiOppimisalueenSuoritus'
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
  $class: 'PrimaryLuokkaAste'
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'P1' | 'P2' | 'P3' | 'P4' | 'P5'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
}

export type PrimaryOppimisalueenAlaosasuoritus = {
  $class: 'PrimaryOppimisalueenAlaosasuoritus'
  koulutusmoduuli: PrimaryAlaoppimisalue
  arviointi?: Array<PrimaryAlaoppimisalueArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkialaosasuoritusprimary'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type PrimaryOppimisalueenSuoritus = {
  $class: 'PrimaryOppimisalueenSuoritus'
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
  $class: 'PrimaryVuosiluokanSuoritus'
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
  $class: 'PuhviKoe2019'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10' | 'S' | 'H'
  >
  kuvaus?: LocalizedString
  päivä: string
  hyväksytty?: boolean
}

export type PYPLuokkaAste = {
  $class: 'PYPLuokkaAste'
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
  $class: 'PYPOppiaineenSuoritus'
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
  $class: 'PYPOppiaineMuu'
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
  $class: 'PYPVuosiluokanSuoritus'
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
  $class: 'Päivämäärävahvistus'
  päivä: string
  myöntäjäOrganisaatio: Organisaatio
}

export type S7OppiaineenAlaosasuoritus = {
  $class: 'S7OppiaineenAlaosasuoritus'
  koulutusmoduuli: S7OppiaineKomponentti
  arviointi?: Array<SecondaryS7PreliminaryMarkArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'europeanschoolofhelsinkialaosasuorituss7'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type S7OppiaineKomponentti = {
  $class: 'S7OppiaineKomponentti'
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkis7oppiaineenkomponentti',
    string
  >
}

export type SanallinenInternationalSchoolOppiaineenArviointi = {
  $class: 'SanallinenInternationalSchoolOppiaineenArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkointernationalschool', string>
  päivä?: string
  hyväksytty?: boolean
}

export type SanallinenLukionArviointi = {
  $class: 'SanallinenLukionArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'S' | 'H' | 'O'
  >
  kuvaus?: LocalizedString
  päivä: string
  hyväksytty?: boolean
}

export type SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 = {
  $class: 'SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019'
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', 'H' | 'S'>
  kuvaus?: LocalizedString
  päivä: string
  hyväksytty?: boolean
}

export type SanallinenLukionOppiaineenArviointi2019 = {
  $class: 'SanallinenLukionOppiaineenArviointi2019'
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', 'H' | 'S'>
  päivä?: string
  hyväksytty?: boolean
}

export type SanallinenPerusopetuksenOppiaineenArviointi = {
  $class: 'SanallinenPerusopetuksenOppiaineenArviointi'
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
    $class: 'SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi'
    arvosana: Koodistokoodiviite<
      'arviointiasteikkotuva',
      'Hyväksytty' | 'Hylätty'
    >
    kuvaus?: LocalizedString
    päivä: string
    hyväksytty?: boolean
  }

export type SecondaryGradeArviointi = {
  $class: 'SecondaryGradeArviointi'
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
  $class: 'SecondaryLowerLuokkaAste'
  tunniste: Koodistokoodiviite<
    'europeanschoolofhelsinkiluokkaaste',
    'S1' | 'S2' | 'S3' | 'S4' | 'S5'
  >
  curriculum: Koodistokoodiviite<'europeanschoolofhelsinkicurriculum', string>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', '21'>
}

export type SecondaryLowerOppiaineenSuoritus = {
  $class: 'SecondaryLowerOppiaineenSuoritus'
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
  $class: 'SecondaryLowerVuosiluokanSuoritus'
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
  $class: 'SecondaryNumericalMarkArviointi'
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
  $class: 'SecondaryS7PreliminaryMarkArviointi'
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
  $class: 'SecondaryUpperLuokkaAste'
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
  $class: 'SecondaryUpperOppiaineenSuoritusS6'
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
  $class: 'SecondaryUpperOppiaineenSuoritusS7'
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
  $class: 'SecondaryUpperVuosiluokanSuoritus'
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
  $class: 'SisältäväOpiskeluoikeus'
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
  $class: 'SuullisenKielitaidonKoe2019'
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
  $class: 'TehostetunTuenPäätös'
  alku: string
  loppu?: string
  tukimuodot?: Array<Koodistokoodiviite<'perusopetuksentukimuoto', string>>
}

export type TelmaJaValmaArviointi = {
  $class: 'TelmaJaValmaArviointi'
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
  $class: 'TelmaKoulutuksenOsanSuoritus'
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
  $class: 'TelmaKoulutuksenSuoritus'
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
  $class: 'TelmaKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999903'>
  perusteenDiaarinumero?: string
  laajuus?: LaajuusOsaamispisteissä
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type TemaattisetOpinnot2019 = {
  $class: 'TemaattisetOpinnot2019'
  tunniste: Koodistokoodiviite<'lukionmuutopinnot', 'TO'>
  laajuus?: LaajuusOpintopisteissä
}

export type Toimipiste = {
  $class: 'Toimipiste'
  oid: string
  nimi?: LocalizedString
  kotipaikka?: Koodistokoodiviite<'kunta', string>
}

export type TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus = {
  $class: 'TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}

export type TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus =

    | TutkinnonOsaaPienemmänKokonaisuudenSuoritus
    | YhteisenTutkinnonOsanOsaAlueenSuoritus

export type TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus = {
  $class: 'TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus'
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
  $class: 'TutkinnonOsaaPienemmänKokonaisuudenSuoritus'
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
  $class: 'TutkinnonOsaaPienempiKokonaisuus'
  tunniste: PaikallinenKoodi
  laajuus?: LaajuusKaikkiYksiköt
  kuvaus: LocalizedString
}

export type TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus = {
  $class: 'TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus'
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
  $class: 'TutkintokoulutukseenValmentavanKoulutuksenSuoritus'
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
  $class: 'TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa'
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '104'>
  laajuus?: LaajuusViikoissa
}

export type TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus =
  {
    $class: 'TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus'
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
    $class: 'TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus'
    nimi: LocalizedString
    tunniste: PaikallinenKoodi
    laajuus?: LaajuusViikoissa
  }

export type TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus =
  {
    $class: 'TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus'
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
  $class: 'TutkintokoulutukseenValmentavanKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999908'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusViikoissa
}

export type TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot =
  {
    $class: 'TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot'
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
    $class: 'TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot'
    ulkomaanjaksot?: Array<Ulkomaanjakso>
    oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
    maksuttomuus?: Array<Maksuttomuus>
    pidennettyPäättymispäivä?: boolean
    sisäoppilaitosmainenMajoitus?: Array<Aikajakso>
  }

export type TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot =
  {
    $class: 'TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot'
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
  $class: 'TutkintokoulutukseenValmentavanOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<TutkintokoulutukseenValmentavanOpiskeluoikeusjakso>
}

export type TutkintokoulutukseenValmentavanOpiskeluoikeus = {
  $class: 'TutkintokoulutukseenValmentavanOpiskeluoikeus'
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
  $class: 'TutkintokoulutukseenValmentavanOpiskeluoikeusjakso'
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
  $class: 'TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen'
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '107'>
  laajuus?: LaajuusViikoissa
}

export type TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot = {
  $class: 'TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot'
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '105'>
  laajuus?: LaajuusViikoissa
}

export type TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot =
  {
    $class: 'TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot'
    tunniste: Koodistokoodiviite<'koulutuksenosattuva', '103'>
    laajuus?: LaajuusViikoissa
  }

export type TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot = {
  $class: 'TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot'
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '106'>
  laajuus?: LaajuusViikoissa
}

export type TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot = {
  $class: 'TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot'
  tunniste: Koodistokoodiviite<'koulutuksenosattuva', '101'>
  laajuus?: LaajuusViikoissa
}

export type TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen =
  {
    $class: 'TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen'
    tunniste: Koodistokoodiviite<'koulutuksenosattuva', '102'>
    laajuus?: LaajuusViikoissa
  }

export type Tutkintotoimikunta = {
  $class: 'Tutkintotoimikunta'
  nimi: LocalizedString
  tutkintotoimikunnanNumero: string
}

export type TuvaErityisenTuenPäätös = {
  $class: 'TuvaErityisenTuenPäätös'
  alku?: string
  loppu?: string
}

export type Työssäoppimisjakso = {
  $class: 'Työssäoppimisjakso'
  työssäoppimispaikka?: LocalizedString
  paikkakunta: Koodistokoodiviite<'kunta', string>
  loppu?: string
  laajuus: LaajuusOsaamispisteissä
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  alku: string
  työtehtävät?: LocalizedString
}

export type TäydellisetHenkilötiedot = {
  $class: 'TäydellisetHenkilötiedot'
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
  $class: 'Ulkomaanjakso'
  alku: string
  loppu?: string
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  kuvaus: LocalizedString
}

export type UusiHenkilö = {
  $class: 'UusiHenkilö'
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
  $class: 'ValmaKoulutuksenOsanSuoritus'
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
  $class: 'ValmaKoulutuksenSuoritus'
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
  $class: 'ValmaKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999901'>
  perusteenDiaarinumero?: string
  laajuus?: LaajuusOsaamispisteissä
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017 = {
  $class: 'ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017'
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenalkuvaiheenkurssit2017',
    string
  >
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export type ValtakunnallinenAikuistenPerusopetuksenKurssi2015 = {
  $class: 'ValtakunnallinenAikuistenPerusopetuksenKurssi2015'
  tunniste: Koodistokoodiviite<'aikuistenperusopetuksenkurssit2015', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export type ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017 = {
  $class: 'ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017'
  tunniste: Koodistokoodiviite<
    'aikuistenperusopetuksenpaattovaiheenkurssit2017',
    string
  >
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
}

export type ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue = {
  $class: 'ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue'
  tunniste: Koodistokoodiviite<'ammatillisenoppiaineet', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export type ValtakunnallinenLukionKurssi2015 = {
  $class: 'ValtakunnallinenLukionKurssi2015'
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
  $class: 'ValtakunnallinenLukioonValmistavanKoulutuksenKurssi'
  tunniste: Koodistokoodiviite<
    | 'lukioonvalmistavankoulutuksenkurssit2015'
    | 'lukioonvalmistavankoulutuksenmoduulit2019',
    string
  >
  laajuus?: LaajuusOpintopisteissäTaiKursseissa
}

export type VapaanSivistystyöJotpaKoulutuksenArviointi = {
  $class: 'VapaanSivistystyöJotpaKoulutuksenArviointi'
  arvosana: Koodistokoodiviite<'arviointiasteikkovst', string>
  päivä: string
  hyväksytty?: boolean
}

export type VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso = {
  $class: 'VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso'
  alku: string
  tila: Koodistokoodiviite<
    'koskiopiskeluoikeudentila',
    'hyvaksytystisuoritettu' | 'lasna' | 'keskeytynyt' | 'mitatoity'
  >
  opintojenRahoitus?: Koodistokoodiviite<'opintojenrahoitus', '14' | '15'>
}

export type VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus = {
  $class: 'VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus'
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
  $class: 'VapaanSivistystyönJotpaKoulutuksenOsasuoritus'
  tunniste: PaikallinenKoodi
  laajuus: LaajuusOpintopisteissä
}

export type VapaanSivistystyönJotpaKoulutuksenSuoritus = {
  $class: 'VapaanSivistystyönJotpaKoulutuksenSuoritus'
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
  $class: 'VapaanSivistystyönJotpaKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '099999'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
  opintokokonaisuus: Koodistokoodiviite<'opintokokonaisuudet', string>
}

export type VapaanSivistystyönLukutaidonKokonaisuus = {
  $class: 'VapaanSivistystyönLukutaidonKokonaisuus'
  tunniste: Koodistokoodiviite<'vstlukutaitokoulutuksenkokonaisuus', string>
  laajuus?: LaajuusOpintopisteissä
}

export type VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus = {
  $class: 'VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus'
  koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstlukutaitokoulutuksenkokonaisuudensuoritus'
  >
  arviointi?: Array<LukutaitokoulutuksenArviointi>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type VapaanSivistystyönLukutaitokoulutuksenSuoritus = {
  $class: 'VapaanSivistystyönLukutaitokoulutuksenSuoritus'
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
  $class: 'VapaanSivistystyönLukutaitokoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999911'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
}

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi =
  {
    $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi'
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
    $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
    tunniste: Koodistokoodiviite<
      'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
      'vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 =
  {
    $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022'
    tunniste: Koodistokoodiviite<'vstkoto2022kielijaviestintakoulutus', string>
    laajuus?: LaajuusOpintopisteissä
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus =
  {
    $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
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
    $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli'
    tunniste: Koodistokoodiviite<
      'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
      'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus =
  {
    $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'
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
    $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus'
    tunniste: PaikallinenKoodi
    kuvaus: LocalizedString
    laajuus?: LaajuusOpintopisteissä
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot =
  {
    $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot'
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
    $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli'
    tunniste: Koodistokoodiviite<
      'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
      'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus =
  {
    $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus'
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
    $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso'
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
    $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli'
    tunniste: Koodistokoodiviite<
      'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus',
      'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus =
  {
    $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus'
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
    $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
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
  $class: 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999910'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
}

export type VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus =

    | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot
    | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso

export type VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen = {
  $class: 'VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen'
  selite: LocalizedString
}

export type VapaanSivistystyönOpintokokonaisuudenSuoritus =
  | MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus
  | OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus

export type VapaanSivistystyönOpiskeluoikeudenLisätiedot = {
  $class: 'VapaanSivistystyönOpiskeluoikeudenLisätiedot'
  maksuttomuus?: Array<Maksuttomuus>
  oikeuttaMaksuttomuuteenPidennetty?: Array<OikeuttaMaksuttomuuteenPidennetty>
}

export type VapaanSivistystyönOpiskeluoikeudenTila = {
  $class: 'VapaanSivistystyönOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<VapaanSivistystyönOpiskeluoikeusjakso>
}

export type VapaanSivistystyönOpiskeluoikeus = {
  $class: 'VapaanSivistystyönOpiskeluoikeus'
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
  $class: 'VapaanSivistystyönVapaatavoitteinenKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '099999'>
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
  opintokokonaisuus?: Koodistokoodiviite<'opintokokonaisuudet', string>
}

export type VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso =
  {
    $class: 'VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'
    alku: string
    tila: Koodistokoodiviite<
      'koskiopiskeluoikeudentila',
      'hyvaksytystisuoritettu' | 'keskeytynyt' | 'mitatoity'
    >
  }

export type VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus =
  {
    $class: 'VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
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
  $class: 'VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus'
  kuvaus: LocalizedString
  tunniste: PaikallinenKoodi
  laajuus: LaajuusOpintopisteissä
}

export type VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus = {
  $class: 'VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus'
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
  $class: 'VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkovstvapaatavoitteinen' | 'arviointiasteikkovst',
    string
  >
  päivä: string
  hyväksytty?: boolean
}

export type VierasTaiToinenKotimainenKieli2015 = {
  $class: 'VierasTaiToinenKotimainenKieli2015'
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
  $class: 'VierasTaiToinenKotimainenKieli2019'
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
  $class: 'VSTKehittyvänKielenTaitotasonArviointi'
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
  $class: 'VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi'
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
  $class: 'VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstmaahanmuuttajienkotoutumiskoulutuksenkielitaitojensuoritus'
  >
  koulutusmoduuli: VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022
  arviointi?: Array<VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 = {
  $class: 'VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022'
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
  $class: 'VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'
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
  $class: 'VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022'
  tunniste: Koodistokoodiviite<'vstkoto2022kokonaisuus', 'ohjaus'>
  laajuus?: LaajuusOpintopisteissä
}

export type VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 = {
  $class: 'VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
  koulutusmoduuli: VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 = {
  $class: 'VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'
  arvosana: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
  päivä: string
  hyväksytty?: boolean
}

export type VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 =
  {
    $class: 'VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022'
    kuvaus: LocalizedString
    tunniste: PaikallinenKoodi
    laajuus?: LaajuusOpintopisteissä
  }

export type VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 = {
  $class: 'VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022'
  tunniste: Koodistokoodiviite<'vstkoto2022kokonaisuus', 'valinnaisetopinnot'>
  laajuus?: LaajuusOpintopisteissä
}

export type VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 = {
  $class: 'VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
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
    $class: 'VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus'
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojensuoritus'
    >
    koulutusmoduuli: VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export type VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 =
  {
    $class: 'VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022'
    tunniste: Koodistokoodiviite<
      'vstkoto2022kokonaisuus',
      'yhteiskuntajatyoelamaosaaminen'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export type VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 =
  {
    $class: 'VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022'
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
    $class: 'VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022'
    tunniste: Koodistokoodiviite<
      'vstkoto2022yhteiskuntajatyoosaamiskoulutus',
      string
    >
    laajuus?: LaajuusOpintopisteissä
  }

export type VSTKotoutumiskoulutus2022 = {
  $class: 'VSTKotoutumiskoulutus2022'
  tunniste: Koodistokoodiviite<'koulutus', '999910'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  laajuus?: LaajuusOpintopisteissä
}

export type VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus = {
  $class: 'VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus'
  koulutusmoduuli: VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export type YhteinenTutkinnonOsa = {
  $class: 'YhteinenTutkinnonOsa'
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
  $class: 'YhteisenAmmatillisenTutkinnonOsanSuoritus'
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
  $class: 'YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'
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
  $class: 'YhteisenTutkinnonOsanOsaAlueenSuoritus'
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
  $class: 'YlioppilaskokeenArviointi'
  arvosana: Koodistokoodiviite<'koskiyoarvosanat', string>
  pisteet?: number
  hyväksytty?: boolean
}

export type YlioppilasTutkinnonKoe = {
  $class: 'YlioppilasTutkinnonKoe'
  tunniste: Koodistokoodiviite<'koskiyokokeet', string>
}

export type YlioppilastutkinnonKokeenSuoritus = {
  $class: 'YlioppilastutkinnonKokeenSuoritus'
  arviointi?: Array<YlioppilaskokeenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ylioppilastutkinnonkoe'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  tutkintokerta: YlioppilastutkinnonTutkintokerta
  koulutusmoduuli: YlioppilasTutkinnonKoe
}

export type YlioppilastutkinnonOpiskeluoikeudenTila = {
  $class: 'YlioppilastutkinnonOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<LukionOpiskeluoikeusjakso>
}

export type YlioppilastutkinnonOpiskeluoikeus = {
  $class: 'YlioppilastutkinnonOpiskeluoikeus'
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
  $class: 'YlioppilastutkinnonSuoritus'
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
  $class: 'YlioppilastutkinnonTutkintokerta'
  koodiarvo: string
  vuosi: number
  vuodenaika: LocalizedString
}

export type Ylioppilastutkinto = {
  $class: 'Ylioppilastutkinto'
  tunniste: Koodistokoodiviite<'koulutus', '301000'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export type Yritys = {
  $class: 'Yritys'
  nimi: LocalizedString
  yTunnus: string
}

// Type guards

export const isOppija = (a: any): a is Oppija => a?.$class === 'Oppija'

export const isAikajakso = (a: any): a is Aikajakso => a?.$class === 'Aikajakso'

export const isAikuistenPerusopetuksenAlkuvaihe = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaihe =>
  a?.$class === 'AikuistenPerusopetuksenAlkuvaihe'

export const isEnglish = (a: any): a is English => a?.$class === 'English'

export const isFinnish = (a: any): a is Finnish => a?.$class === 'Finnish'

export const isLocalizedString = (a: any): a is LocalizedString =>
  isEnglish(a) || isFinnish(a) || isSwedish(a)

export const isSwedish = (a: any): a is Swedish => a?.$class === 'Swedish'

export const isAikuistenPerusopetuksenAlkuvaiheenKurssi = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenKurssi =>
  isPaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi(a) ||
  isValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017(a)

export const isAikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus =>
  a?.$class === 'AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus'

export const isAikuistenPerusopetuksenAlkuvaiheenOppiaine = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenOppiaine =>
  isAikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine(a) ||
  isAikuistenPerusopetuksenAlkuvaiheenVierasKieli(a) ||
  isAikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus(a) ||
  isMuuAikuistenPerusopetuksenAlkuvaiheenOppiaine(a)

export const isAikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus =>
  a?.$class === 'AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus'

export const isAikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine =>
  a?.$class === 'AikuistenPerusopetuksenAlkuvaiheenPaikallinenOppiaine'

export const isAikuistenPerusopetuksenAlkuvaiheenSuoritus = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenSuoritus =>
  a?.$class === 'AikuistenPerusopetuksenAlkuvaiheenSuoritus'

export const isAikuistenPerusopetuksenAlkuvaiheenVierasKieli = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenVierasKieli =>
  a?.$class === 'AikuistenPerusopetuksenAlkuvaiheenVierasKieli'

export const isAikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus =>
  a?.$class === 'AikuistenPerusopetuksenAlkuvaiheenÄidinkieliJaKirjallisuus'

export const isAikuistenPerusopetuksenKurssi = (
  a: any
): a is AikuistenPerusopetuksenKurssi =>
  isPaikallinenAikuistenPerusopetuksenKurssi(a) ||
  isValtakunnallinenAikuistenPerusopetuksenKurssi2015(a) ||
  isValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017(a)

export const isAikuistenPerusopetuksenKurssinSuoritus = (
  a: any
): a is AikuistenPerusopetuksenKurssinSuoritus =>
  a?.$class === 'AikuistenPerusopetuksenKurssinSuoritus'

export const isAikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus = (
  a: any
): a is AikuistenPerusopetuksenKurssinTaiAlkuvaiheenKurssinSuoritus =>
  isAikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus(a) ||
  isAikuistenPerusopetuksenKurssinSuoritus(a)

export const isAikuistenPerusopetuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot =>
  a?.$class === 'AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot'

export const isAikuistenPerusopetuksenOpiskeluoikeudenTila = (
  a: any
): a is AikuistenPerusopetuksenOpiskeluoikeudenTila =>
  a?.$class === 'AikuistenPerusopetuksenOpiskeluoikeudenTila'

export const isAikuistenPerusopetuksenOpiskeluoikeus = (
  a: any
): a is AikuistenPerusopetuksenOpiskeluoikeus =>
  a?.$class === 'AikuistenPerusopetuksenOpiskeluoikeus'

export const isAikuistenPerusopetuksenOpiskeluoikeusjakso = (
  a: any
): a is AikuistenPerusopetuksenOpiskeluoikeusjakso =>
  a?.$class === 'AikuistenPerusopetuksenOpiskeluoikeusjakso'

export const isAikuistenPerusopetuksenOppiaine = (
  a: any
): a is AikuistenPerusopetuksenOppiaine =>
  isAikuistenPerusopetuksenPaikallinenOppiaine(a) ||
  isAikuistenPerusopetuksenUskonto(a) ||
  isAikuistenPerusopetuksenVierasTaiToinenKotimainenKieli(a) ||
  isAikuistenPerusopetuksenÄidinkieliJaKirjallisuus(a) ||
  isMuuAikuistenPerusopetuksenOppiaine(a)

export const isAikuistenPerusopetuksenOppiaineenOppimääränSuoritus = (
  a: any
): a is AikuistenPerusopetuksenOppiaineenOppimääränSuoritus =>
  a?.$class === 'AikuistenPerusopetuksenOppiaineenOppimääränSuoritus'

export const isAikuistenPerusopetuksenOppiaineenSuoritus = (
  a: any
): a is AikuistenPerusopetuksenOppiaineenSuoritus =>
  a?.$class === 'AikuistenPerusopetuksenOppiaineenSuoritus'

export const isAikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine = (
  a: any
): a is AikuistenPerusopetuksenOppiainenTaiEiTiedossaOppiaine =>
  isAikuistenPerusopetuksenPaikallinenOppiaine(a) ||
  isAikuistenPerusopetuksenUskonto(a) ||
  isAikuistenPerusopetuksenVierasTaiToinenKotimainenKieli(a) ||
  isAikuistenPerusopetuksenÄidinkieliJaKirjallisuus(a) ||
  isEiTiedossaOppiaine(a) ||
  isMuuAikuistenPerusopetuksenOppiaine(a)

export const isAikuistenPerusopetuksenOppimääränSuoritus = (
  a: any
): a is AikuistenPerusopetuksenOppimääränSuoritus =>
  a?.$class === 'AikuistenPerusopetuksenOppimääränSuoritus'

export const isAikuistenPerusopetuksenPaikallinenOppiaine = (
  a: any
): a is AikuistenPerusopetuksenPaikallinenOppiaine =>
  a?.$class === 'AikuistenPerusopetuksenPaikallinenOppiaine'

export const isAikuistenPerusopetuksenPäätasonSuoritus = (
  a: any
): a is AikuistenPerusopetuksenPäätasonSuoritus =>
  isAikuistenPerusopetuksenAlkuvaiheenSuoritus(a) ||
  isAikuistenPerusopetuksenOppiaineenOppimääränSuoritus(a) ||
  isAikuistenPerusopetuksenOppimääränSuoritus(a)

export const isAikuistenPerusopetuksenUskonto = (
  a: any
): a is AikuistenPerusopetuksenUskonto =>
  a?.$class === 'AikuistenPerusopetuksenUskonto'

export const isAikuistenPerusopetuksenVierasTaiToinenKotimainenKieli = (
  a: any
): a is AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli =>
  a?.$class === 'AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli'

export const isAikuistenPerusopetuksenÄidinkieliJaKirjallisuus = (
  a: any
): a is AikuistenPerusopetuksenÄidinkieliJaKirjallisuus =>
  a?.$class === 'AikuistenPerusopetuksenÄidinkieliJaKirjallisuus'

export const isAikuistenPerusopetus = (a: any): a is AikuistenPerusopetus =>
  a?.$class === 'AikuistenPerusopetus'

export const isAmmatillinenArviointi = (a: any): a is AmmatillinenArviointi =>
  a?.$class === 'AmmatillinenArviointi'

export const isAmmatillinenOpiskeluoikeudenTila = (
  a: any
): a is AmmatillinenOpiskeluoikeudenTila =>
  a?.$class === 'AmmatillinenOpiskeluoikeudenTila'

export const isAmmatillinenOpiskeluoikeus = (
  a: any
): a is AmmatillinenOpiskeluoikeus => a?.$class === 'AmmatillinenOpiskeluoikeus'

export const isAmmatillinenOpiskeluoikeusjakso = (
  a: any
): a is AmmatillinenOpiskeluoikeusjakso =>
  a?.$class === 'AmmatillinenOpiskeluoikeusjakso'

export const isAmmatillinenPäätasonSuoritus = (
  a: any
): a is AmmatillinenPäätasonSuoritus =>
  isAmmatillisenTutkinnonOsittainenSuoritus(a) ||
  isAmmatillisenTutkinnonSuoritus(a) ||
  isMuunAmmatillisenKoulutuksenSuoritus(a) ||
  isNäyttötutkintoonValmistavanKoulutuksenSuoritus(a) ||
  isTelmaKoulutuksenSuoritus(a) ||
  isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(a) ||
  isValmaKoulutuksenSuoritus(a)

export const isAmmatillinenTutkintoKoulutus = (
  a: any
): a is AmmatillinenTutkintoKoulutus =>
  a?.$class === 'AmmatillinenTutkintoKoulutus'

export const isAmmatilliseenTehtäväänValmistavaKoulutus = (
  a: any
): a is AmmatilliseenTehtäväänValmistavaKoulutus =>
  a?.$class === 'AmmatilliseenTehtäväänValmistavaKoulutus'

export const isAmmatillisenOpiskeluoikeudenLisätiedot = (
  a: any
): a is AmmatillisenOpiskeluoikeudenLisätiedot =>
  a?.$class === 'AmmatillisenOpiskeluoikeudenLisätiedot'

export const isAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus = (
  a: any
): a is AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus =>
  a?.$class === 'AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus'

export const isAmmatillisenTutkinnonOsaaPienempiKokonaisuus = (
  a: any
): a is AmmatillisenTutkinnonOsaaPienempiKokonaisuus =>
  a?.$class === 'AmmatillisenTutkinnonOsaaPienempiKokonaisuus'

export const isAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =
  (
    a: any
  ): a is AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =>
    a?.$class ===
    'AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'

export const isAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus = (
  a: any
): a is AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus =>
  a?.$class === 'AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'

export const isAmmatillisenTutkinnonOsanLisätieto = (
  a: any
): a is AmmatillisenTutkinnonOsanLisätieto =>
  a?.$class === 'AmmatillisenTutkinnonOsanLisätieto'

export const isAmmatillisenTutkinnonOsanOsaAlue = (
  a: any
): a is AmmatillisenTutkinnonOsanOsaAlue =>
  isAmmatillisenTutkinnonVierasTaiToinenKotimainenKieli(a) ||
  isAmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(a) ||
  isAmmatillisenTutkinnonÄidinkieli(a) ||
  isPaikallinenAmmatillisenTutkinnonOsanOsaAlue(a) ||
  isValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(a)

export const isAmmatillisenTutkinnonOsanSuoritus = (
  a: any
): a is AmmatillisenTutkinnonOsanSuoritus =>
  isAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
    a
  ) ||
  isAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(a) ||
  isMuunAmmatillisenTutkinnonOsanSuoritus(a) ||
  isYhteisenAmmatillisenTutkinnonOsanSuoritus(a)

export const isAmmatillisenTutkinnonOsittainenSuoritus = (
  a: any
): a is AmmatillisenTutkinnonOsittainenSuoritus =>
  a?.$class === 'AmmatillisenTutkinnonOsittainenSuoritus'

export const isAmmatillisenTutkinnonSuoritus = (
  a: any
): a is AmmatillisenTutkinnonSuoritus =>
  a?.$class === 'AmmatillisenTutkinnonSuoritus'

export const isAmmatillisenTutkinnonVierasTaiToinenKotimainenKieli = (
  a: any
): a is AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli =>
  a?.$class === 'AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli'

export const isAmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla = (
  a: any
): a is AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla =>
  a?.$class === 'AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla'

export const isAmmatillisenTutkinnonÄidinkieli = (
  a: any
): a is AmmatillisenTutkinnonÄidinkieli =>
  a?.$class === 'AmmatillisenTutkinnonÄidinkieli'

export const isArvioitsija = (a: any): a is Arvioitsija =>
  a?.$class === 'Arvioitsija'

export const isDIANäyttötutkinto = (a: any): a is DIANäyttötutkinto =>
  a?.$class === 'DIANäyttötutkinto'

export const isDIAOpiskeluoikeudenLisätiedot = (
  a: any
): a is DIAOpiskeluoikeudenLisätiedot =>
  a?.$class === 'DIAOpiskeluoikeudenLisätiedot'

export const isDIAOpiskeluoikeudenTila = (
  a: any
): a is DIAOpiskeluoikeudenTila => a?.$class === 'DIAOpiskeluoikeudenTila'

export const isDIAOpiskeluoikeus = (a: any): a is DIAOpiskeluoikeus =>
  a?.$class === 'DIAOpiskeluoikeus'

export const isDIAOpiskeluoikeusjakso = (a: any): a is DIAOpiskeluoikeusjakso =>
  a?.$class === 'DIAOpiskeluoikeusjakso'

export const isDIAOppiaine = (a: any): a is DIAOppiaine =>
  isDIAOppiaineKieli(a) ||
  isDIAOppiaineLisäaine(a) ||
  isDIAOppiaineLisäaineKieli(a) ||
  isDIAOppiaineMuu(a) ||
  isDIAOppiaineÄidinkieli(a)

export const isDIAOppiaineenTutkintovaiheenLukukausi = (
  a: any
): a is DIAOppiaineenTutkintovaiheenLukukausi =>
  a?.$class === 'DIAOppiaineenTutkintovaiheenLukukausi'

export const isDIAOppiaineenTutkintovaiheenNumeerinenArviointi = (
  a: any
): a is DIAOppiaineenTutkintovaiheenNumeerinenArviointi =>
  a?.$class === 'DIAOppiaineenTutkintovaiheenNumeerinenArviointi'

export const isDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus = (
  a: any
): a is DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus =>
  a?.$class === 'DIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus'

export const isDIAOppiaineenTutkintovaiheenOsasuoritus = (
  a: any
): a is DIAOppiaineenTutkintovaiheenOsasuoritus =>
  isDIANäyttötutkinto(a) ||
  isDIAOppiaineenTutkintovaiheenLukukausi(a) ||
  isDIAPäättökoe(a)

export const isDIAOppiaineenTutkintovaiheenSuoritus = (
  a: any
): a is DIAOppiaineenTutkintovaiheenSuoritus =>
  a?.$class === 'DIAOppiaineenTutkintovaiheenSuoritus'

export const isDIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi = (
  a: any
): a is DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi =>
  a?.$class === 'DIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi'

export const isDIAOppiaineenValmistavanVaiheenLukukaudenArviointi = (
  a: any
): a is DIAOppiaineenValmistavanVaiheenLukukaudenArviointi =>
  a?.$class === 'DIAOppiaineenValmistavanVaiheenLukukaudenArviointi'

export const isDIAOppiaineenValmistavanVaiheenLukukaudenSuoritus = (
  a: any
): a is DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus =>
  a?.$class === 'DIAOppiaineenValmistavanVaiheenLukukaudenSuoritus'

export const isDIAOppiaineenValmistavanVaiheenLukukausi = (
  a: any
): a is DIAOppiaineenValmistavanVaiheenLukukausi =>
  a?.$class === 'DIAOppiaineenValmistavanVaiheenLukukausi'

export const isDIAOppiaineenValmistavanVaiheenSuoritus = (
  a: any
): a is DIAOppiaineenValmistavanVaiheenSuoritus =>
  a?.$class === 'DIAOppiaineenValmistavanVaiheenSuoritus'

export const isDIAOppiaineKieli = (a: any): a is DIAOppiaineKieli =>
  a?.$class === 'DIAOppiaineKieli'

export const isDIAOppiaineLisäaine = (a: any): a is DIAOppiaineLisäaine =>
  a?.$class === 'DIAOppiaineLisäaine'

export const isDIAOppiaineLisäaineKieli = (
  a: any
): a is DIAOppiaineLisäaineKieli => a?.$class === 'DIAOppiaineLisäaineKieli'

export const isDIAOppiaineMuu = (a: any): a is DIAOppiaineMuu =>
  a?.$class === 'DIAOppiaineMuu'

export const isDIAOppiaineÄidinkieli = (a: any): a is DIAOppiaineÄidinkieli =>
  a?.$class === 'DIAOppiaineÄidinkieli'

export const isDIAPäätasonSuoritus = (a: any): a is DIAPäätasonSuoritus =>
  isDIATutkinnonSuoritus(a) || isDIAValmistavanVaiheenSuoritus(a)

export const isDIAPäättökoe = (a: any): a is DIAPäättökoe =>
  a?.$class === 'DIAPäättökoe'

export const isDIATutkinnonSuoritus = (a: any): a is DIATutkinnonSuoritus =>
  a?.$class === 'DIATutkinnonSuoritus'

export const isDIATutkinto = (a: any): a is DIATutkinto =>
  a?.$class === 'DIATutkinto'

export const isDIATutkintovaiheenArviointi = (
  a: any
): a is DIATutkintovaiheenArviointi =>
  isDIAOppiaineenTutkintovaiheenNumeerinenArviointi(a) ||
  isDIAOppiaineenTutkintovaiheenSuoritusmerkintäArviointi(a)

export const isDIAValmistavanVaiheenSuoritus = (
  a: any
): a is DIAValmistavanVaiheenSuoritus =>
  a?.$class === 'DIAValmistavanVaiheenSuoritus'

export const isDIAValmistavaVaihe = (a: any): a is DIAValmistavaVaihe =>
  a?.$class === 'DIAValmistavaVaihe'

export const isDIAVastaavuustodistuksenTiedot = (
  a: any
): a is DIAVastaavuustodistuksenTiedot =>
  a?.$class === 'DIAVastaavuustodistuksenTiedot'

export const isDiplomaArviointi = (a: any): a is DiplomaArviointi =>
  isInternationalSchoolIBOppiaineenArviointi(a) ||
  isNumeerinenInternationalSchoolOppiaineenArviointi(a) ||
  isPassFailOppiaineenArviointi(a)

export const isDiplomaCoreRequirementsOppiaine = (
  a: any
): a is DiplomaCoreRequirementsOppiaine =>
  a?.$class === 'DiplomaCoreRequirementsOppiaine'

export const isDiplomaCoreRequirementsOppiaineenSuoritus = (
  a: any
): a is DiplomaCoreRequirementsOppiaineenSuoritus =>
  a?.$class === 'DiplomaCoreRequirementsOppiaineenSuoritus'

export const isDiplomaIBOppiaineenSuoritus = (
  a: any
): a is DiplomaIBOppiaineenSuoritus =>
  isDiplomaCoreRequirementsOppiaineenSuoritus(a) ||
  isDiplomaOppiaineenSuoritus(a)

export const isDiplomaLuokkaAste = (a: any): a is DiplomaLuokkaAste =>
  isIBDiplomaLuokkaAste(a) || isISHDiplomaLuokkaAste(a)

export const isDiplomaOppiaineenSuoritus = (
  a: any
): a is DiplomaOppiaineenSuoritus => a?.$class === 'DiplomaOppiaineenSuoritus'

export const isDiplomaVuosiluokanSuoritus = (
  a: any
): a is DiplomaVuosiluokanSuoritus => a?.$class === 'DiplomaVuosiluokanSuoritus'

export const isDuplikaatti = (a: any): a is Duplikaatti =>
  a?.$class === 'Duplikaatti'

export const isEBArviointi = (a: any): a is EBArviointi =>
  isEBTutkintoFinalMarkArviointi(a) || isEBTutkintoPreliminaryMarkArviointi(a)

export const isEBOppiaineenAlaosasuoritus = (
  a: any
): a is EBOppiaineenAlaosasuoritus => a?.$class === 'EBOppiaineenAlaosasuoritus'

export const isEBOppiaineKomponentti = (a: any): a is EBOppiaineKomponentti =>
  a?.$class === 'EBOppiaineKomponentti'

export const isEBTutkinnonOsasuoritus = (a: any): a is EBTutkinnonOsasuoritus =>
  a?.$class === 'EBTutkinnonOsasuoritus'

export const isEBTutkinnonSuoritus = (a: any): a is EBTutkinnonSuoritus =>
  a?.$class === 'EBTutkinnonSuoritus'

export const isEBTutkinto = (a: any): a is EBTutkinto =>
  a?.$class === 'EBTutkinto'

export const isEBTutkintoFinalMarkArviointi = (
  a: any
): a is EBTutkintoFinalMarkArviointi =>
  a?.$class === 'EBTutkintoFinalMarkArviointi'

export const isEBTutkintoPreliminaryMarkArviointi = (
  a: any
): a is EBTutkintoPreliminaryMarkArviointi =>
  a?.$class === 'EBTutkintoPreliminaryMarkArviointi'

export const isEiTiedossaOppiaine = (a: any): a is EiTiedossaOppiaine =>
  a?.$class === 'EiTiedossaOppiaine'

export const isErityisenKoulutustehtävänJakso = (
  a: any
): a is ErityisenKoulutustehtävänJakso =>
  a?.$class === 'ErityisenKoulutustehtävänJakso'

export const isErityisenTuenPäätös = (a: any): a is ErityisenTuenPäätös =>
  a?.$class === 'ErityisenTuenPäätös'

export const isEsiopetuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is EsiopetuksenOpiskeluoikeudenLisätiedot =>
  a?.$class === 'EsiopetuksenOpiskeluoikeudenLisätiedot'

export const isEsiopetuksenOpiskeluoikeus = (
  a: any
): a is EsiopetuksenOpiskeluoikeus => a?.$class === 'EsiopetuksenOpiskeluoikeus'

export const isEsiopetuksenSuoritus = (a: any): a is EsiopetuksenSuoritus =>
  a?.$class === 'EsiopetuksenSuoritus'

export const isEsiopetus = (a: any): a is Esiopetus => a?.$class === 'Esiopetus'

export const isEuropeanSchoolOfHelsinkiKielioppiaine = (
  a: any
): a is EuropeanSchoolOfHelsinkiKielioppiaine =>
  a?.$class === 'EuropeanSchoolOfHelsinkiKielioppiaine'

export const isEuropeanSchoolOfHelsinkiKielioppiaineAncientGreek = (
  a: any
): a is EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek =>
  a?.$class === 'EuropeanSchoolOfHelsinkiKielioppiaineAncientGreek'

export const isEuropeanSchoolOfHelsinkiKielioppiaineLatin = (
  a: any
): a is EuropeanSchoolOfHelsinkiKielioppiaineLatin =>
  a?.$class === 'EuropeanSchoolOfHelsinkiKielioppiaineLatin'

export const isEuropeanSchoolOfHelsinkiMuuOppiaine = (
  a: any
): a is EuropeanSchoolOfHelsinkiMuuOppiaine =>
  a?.$class === 'EuropeanSchoolOfHelsinkiMuuOppiaine'

export const isEuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot = (
  a: any
): a is EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot =>
  a?.$class === 'EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot'

export const isEuropeanSchoolOfHelsinkiOpiskeluoikeudenTila = (
  a: any
): a is EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila =>
  a?.$class === 'EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila'

export const isEuropeanSchoolOfHelsinkiOpiskeluoikeus = (
  a: any
): a is EuropeanSchoolOfHelsinkiOpiskeluoikeus =>
  a?.$class === 'EuropeanSchoolOfHelsinkiOpiskeluoikeus'

export const isEuropeanSchoolOfHelsinkiOpiskeluoikeusjakso = (
  a: any
): a is EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso =>
  a?.$class === 'EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso'

export const isEuropeanSchoolOfHelsinkiOsasuoritusArviointi = (
  a: any
): a is EuropeanSchoolOfHelsinkiOsasuoritusArviointi =>
  a?.$class === 'EuropeanSchoolOfHelsinkiOsasuoritusArviointi'

export const isEuropeanSchoolOfHelsinkiPäätasonSuoritus = (
  a: any
): a is EuropeanSchoolOfHelsinkiPäätasonSuoritus =>
  isEBTutkinnonSuoritus(a) ||
  isNurseryVuosiluokanSuoritus(a) ||
  isPrimaryVuosiluokanSuoritus(a) ||
  isSecondaryLowerVuosiluokanSuoritus(a) ||
  isSecondaryUpperVuosiluokanSuoritus(a)

export const isFitnessAndWellBeing = (a: any): a is FitnessAndWellBeing =>
  a?.$class === 'FitnessAndWellBeing'

export const isHenkilö = (a: any): a is Henkilö =>
  isHenkilötiedotJaOid(a) ||
  isOidHenkilö(a) ||
  isTäydellisetHenkilötiedot(a) ||
  isUusiHenkilö(a)

export const isHenkilötiedotJaOid = (a: any): a is HenkilötiedotJaOid =>
  a?.$class === 'HenkilötiedotJaOid'

export const isHenkilövahvistusPaikkakunnalla = (
  a: any
): a is HenkilövahvistusPaikkakunnalla =>
  a?.$class === 'HenkilövahvistusPaikkakunnalla'

export const isHenkilövahvistusValinnaisellaPaikkakunnalla = (
  a: any
): a is HenkilövahvistusValinnaisellaPaikkakunnalla =>
  a?.$class === 'HenkilövahvistusValinnaisellaPaikkakunnalla'

export const isHenkilövahvistusValinnaisellaTittelillä = (
  a: any
): a is HenkilövahvistusValinnaisellaTittelillä =>
  isHenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla(a)

export const isHenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla =
  (
    a: any
  ): a is HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla =>
    a?.$class ===
    'HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla'

export const isHojks = (a: any): a is Hojks => a?.$class === 'Hojks'

export const isIBAineRyhmäOppiaine = (a: any): a is IBAineRyhmäOppiaine =>
  isIBOppiaineLanguage(a) || isIBOppiaineMuu(a)

export const isIBCASOppiaineenArviointi = (
  a: any
): a is IBCASOppiaineenArviointi => a?.$class === 'IBCASOppiaineenArviointi'

export const isIBCASSuoritus = (a: any): a is IBCASSuoritus =>
  a?.$class === 'IBCASSuoritus'

export const isIBCoreRequirementsArviointi = (
  a: any
): a is IBCoreRequirementsArviointi =>
  a?.$class === 'IBCoreRequirementsArviointi'

export const isIBDiplomaLuokkaAste = (a: any): a is IBDiplomaLuokkaAste =>
  a?.$class === 'IBDiplomaLuokkaAste'

export const isIBExtendedEssaySuoritus = (
  a: any
): a is IBExtendedEssaySuoritus => a?.$class === 'IBExtendedEssaySuoritus'

export const isIBKurssi = (a: any): a is IBKurssi => a?.$class === 'IBKurssi'

export const isIBKurssinArviointi = (a: any): a is IBKurssinArviointi =>
  a?.$class === 'IBKurssinArviointi'

export const isIBKurssinSuoritus = (a: any): a is IBKurssinSuoritus =>
  a?.$class === 'IBKurssinSuoritus'

export const isIBOpiskeluoikeus = (a: any): a is IBOpiskeluoikeus =>
  a?.$class === 'IBOpiskeluoikeus'

export const isIBOppiaineCAS = (a: any): a is IBOppiaineCAS =>
  a?.$class === 'IBOppiaineCAS'

export const isIBOppiaineenArviointi = (a: any): a is IBOppiaineenArviointi =>
  a?.$class === 'IBOppiaineenArviointi'

export const isIBOppiaineenSuoritus = (a: any): a is IBOppiaineenSuoritus =>
  a?.$class === 'IBOppiaineenSuoritus'

export const isIBOppiaineExtendedEssay = (
  a: any
): a is IBOppiaineExtendedEssay => a?.$class === 'IBOppiaineExtendedEssay'

export const isIBOppiaineLanguage = (a: any): a is IBOppiaineLanguage =>
  a?.$class === 'IBOppiaineLanguage'

export const isIBOppiaineMuu = (a: any): a is IBOppiaineMuu =>
  a?.$class === 'IBOppiaineMuu'

export const isIBOppiaineTheoryOfKnowledge = (
  a: any
): a is IBOppiaineTheoryOfKnowledge =>
  a?.$class === 'IBOppiaineTheoryOfKnowledge'

export const isIBPäätasonSuoritus = (a: any): a is IBPäätasonSuoritus =>
  isIBTutkinnonSuoritus(a) || isPreIBSuoritus2015(a) || isPreIBSuoritus2019(a)

export const isIBTheoryOfKnowledgeSuoritus = (
  a: any
): a is IBTheoryOfKnowledgeSuoritus =>
  a?.$class === 'IBTheoryOfKnowledgeSuoritus'

export const isIBTutkinnonSuoritus = (a: any): a is IBTutkinnonSuoritus =>
  a?.$class === 'IBTutkinnonSuoritus'

export const isIBTutkinto = (a: any): a is IBTutkinto =>
  a?.$class === 'IBTutkinto'

export const isInternationalSchoolCoreRequirementsArviointi = (
  a: any
): a is InternationalSchoolCoreRequirementsArviointi =>
  a?.$class === 'InternationalSchoolCoreRequirementsArviointi'

export const isInternationalSchoolIBOppiaine = (
  a: any
): a is InternationalSchoolIBOppiaine =>
  isFitnessAndWellBeing(a) ||
  isInternationalSchoolMuuDiplomaOppiaine(a) ||
  isKieliDiplomaOppiaine(a) ||
  isMuuDiplomaOppiaine(a)

export const isInternationalSchoolIBOppiaineenArviointi = (
  a: any
): a is InternationalSchoolIBOppiaineenArviointi =>
  a?.$class === 'InternationalSchoolIBOppiaineenArviointi'

export const isInternationalSchoolMuuDiplomaOppiaine = (
  a: any
): a is InternationalSchoolMuuDiplomaOppiaine =>
  a?.$class === 'InternationalSchoolMuuDiplomaOppiaine'

export const isInternationalSchoolOpiskeluoikeudenLisätiedot = (
  a: any
): a is InternationalSchoolOpiskeluoikeudenLisätiedot =>
  a?.$class === 'InternationalSchoolOpiskeluoikeudenLisätiedot'

export const isInternationalSchoolOpiskeluoikeudenTila = (
  a: any
): a is InternationalSchoolOpiskeluoikeudenTila =>
  a?.$class === 'InternationalSchoolOpiskeluoikeudenTila'

export const isInternationalSchoolOpiskeluoikeus = (
  a: any
): a is InternationalSchoolOpiskeluoikeus =>
  a?.$class === 'InternationalSchoolOpiskeluoikeus'

export const isInternationalSchoolOpiskeluoikeusjakso = (
  a: any
): a is InternationalSchoolOpiskeluoikeusjakso =>
  a?.$class === 'InternationalSchoolOpiskeluoikeusjakso'

export const isInternationalSchoolVuosiluokanSuoritus = (
  a: any
): a is InternationalSchoolVuosiluokanSuoritus =>
  isDiplomaVuosiluokanSuoritus(a) ||
  isMYPVuosiluokanSuoritus(a) ||
  isPYPVuosiluokanSuoritus(a)

export const isISHDiplomaLuokkaAste = (a: any): a is ISHDiplomaLuokkaAste =>
  a?.$class === 'ISHDiplomaLuokkaAste'

export const isJatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa = (
  a: any
): a is JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa =>
  a?.$class === 'JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa'

export const isJärjestämismuoto = (a: any): a is Järjestämismuoto =>
  isJärjestämismuotoIlmanLisätietoja(a) ||
  isOppisopimuksellinenJärjestämismuoto(a)

export const isJärjestämismuotoIlmanLisätietoja = (
  a: any
): a is JärjestämismuotoIlmanLisätietoja =>
  a?.$class === 'JärjestämismuotoIlmanLisätietoja'

export const isJärjestämismuotojakso = (a: any): a is Järjestämismuotojakso =>
  a?.$class === 'Järjestämismuotojakso'

export const isKieliDiplomaOppiaine = (a: any): a is KieliDiplomaOppiaine =>
  a?.$class === 'KieliDiplomaOppiaine'

export const isKoodistokoodiviite = (a: any): a is Koodistokoodiviite =>
  a?.$class === 'Koodistokoodiviite'

export const isKorkeakoulunArviointi = (a: any): a is KorkeakoulunArviointi =>
  isKorkeakoulunKoodistostaLöytyväArviointi(a) ||
  isKorkeakoulunPaikallinenArviointi(a)

export const isKorkeakoulunKoodistostaLöytyväArviointi = (
  a: any
): a is KorkeakoulunKoodistostaLöytyväArviointi =>
  a?.$class === 'KorkeakoulunKoodistostaLöytyväArviointi'

export const isKorkeakoulunOpintojakso = (
  a: any
): a is KorkeakoulunOpintojakso => a?.$class === 'KorkeakoulunOpintojakso'

export const isKorkeakoulunOpintojaksonSuoritus = (
  a: any
): a is KorkeakoulunOpintojaksonSuoritus =>
  a?.$class === 'KorkeakoulunOpintojaksonSuoritus'

export const isKorkeakoulunOpiskeluoikeudenLisätiedot = (
  a: any
): a is KorkeakoulunOpiskeluoikeudenLisätiedot =>
  a?.$class === 'KorkeakoulunOpiskeluoikeudenLisätiedot'

export const isKorkeakoulunOpiskeluoikeudenLukuvuosimaksu = (
  a: any
): a is KorkeakoulunOpiskeluoikeudenLukuvuosimaksu =>
  a?.$class === 'KorkeakoulunOpiskeluoikeudenLukuvuosimaksu'

export const isKorkeakoulunOpiskeluoikeudenTila = (
  a: any
): a is KorkeakoulunOpiskeluoikeudenTila =>
  a?.$class === 'KorkeakoulunOpiskeluoikeudenTila'

export const isKorkeakoulunOpiskeluoikeus = (
  a: any
): a is KorkeakoulunOpiskeluoikeus => a?.$class === 'KorkeakoulunOpiskeluoikeus'

export const isKorkeakoulunOpiskeluoikeusjakso = (
  a: any
): a is KorkeakoulunOpiskeluoikeusjakso =>
  a?.$class === 'KorkeakoulunOpiskeluoikeusjakso'

export const isKorkeakoulunPaikallinenArviointi = (
  a: any
): a is KorkeakoulunPaikallinenArviointi =>
  a?.$class === 'KorkeakoulunPaikallinenArviointi'

export const isKorkeakouluopinnotTutkinnonOsa = (
  a: any
): a is KorkeakouluopinnotTutkinnonOsa =>
  a?.$class === 'KorkeakouluopinnotTutkinnonOsa'

export const isKorkeakouluopintojenSuoritus = (
  a: any
): a is KorkeakouluopintojenSuoritus =>
  a?.$class === 'KorkeakouluopintojenSuoritus'

export const isKorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus = (
  a: any
): a is KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus =>
  a?.$class === 'KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus'

export const isKorkeakouluSuoritus = (a: any): a is KorkeakouluSuoritus =>
  isKorkeakoulunOpintojaksonSuoritus(a) ||
  isKorkeakoulututkinnonSuoritus(a) ||
  isMuuKorkeakoulunSuoritus(a)

export const isKorkeakoulututkinnonSuoritus = (
  a: any
): a is KorkeakoulututkinnonSuoritus =>
  a?.$class === 'KorkeakoulututkinnonSuoritus'

export const isKorkeakoulututkinto = (a: any): a is Korkeakoulututkinto =>
  a?.$class === 'Korkeakoulututkinto'

export const isKoulutussopimusjakso = (a: any): a is Koulutussopimusjakso =>
  a?.$class === 'Koulutussopimusjakso'

export const isKoulutustoimija = (a: any): a is Koulutustoimija =>
  a?.$class === 'Koulutustoimija'

export const isLaajuus = (a: any): a is Laajuus =>
  isLaajuusKaikkiYksiköt(a) ||
  isLaajuusKursseissa(a) ||
  isLaajuusOpintopisteissä(a) ||
  isLaajuusOpintoviikoissa(a) ||
  isLaajuusOsaamispisteissä(a) ||
  isLaajuusTunneissa(a) ||
  isLaajuusViikoissa(a) ||
  isLaajuusVuosiviikkotunneissa(a)

export const isLaajuusKaikkiYksiköt = (a: any): a is LaajuusKaikkiYksiköt =>
  a?.$class === 'LaajuusKaikkiYksiköt'

export const isLaajuusKursseissa = (a: any): a is LaajuusKursseissa =>
  a?.$class === 'LaajuusKursseissa'

export const isLaajuusOpintopisteissä = (a: any): a is LaajuusOpintopisteissä =>
  a?.$class === 'LaajuusOpintopisteissä'

export const isLaajuusOpintopisteissäTaiKursseissa = (
  a: any
): a is LaajuusOpintopisteissäTaiKursseissa =>
  isLaajuusKursseissa(a) || isLaajuusOpintopisteissä(a)

export const isLaajuusOpintoviikoissa = (a: any): a is LaajuusOpintoviikoissa =>
  a?.$class === 'LaajuusOpintoviikoissa'

export const isLaajuusOsaamispisteissä = (
  a: any
): a is LaajuusOsaamispisteissä => a?.$class === 'LaajuusOsaamispisteissä'

export const isLaajuusTunneissa = (a: any): a is LaajuusTunneissa =>
  a?.$class === 'LaajuusTunneissa'

export const isLaajuusViikoissa = (a: any): a is LaajuusViikoissa =>
  a?.$class === 'LaajuusViikoissa'

export const isLaajuusVuosiviikkotunneissa = (
  a: any
): a is LaajuusVuosiviikkotunneissa =>
  a?.$class === 'LaajuusVuosiviikkotunneissa'

export const isLaajuusVuosiviikkotunneissaTaiKursseissa = (
  a: any
): a is LaajuusVuosiviikkotunneissaTaiKursseissa =>
  isLaajuusKursseissa(a) || isLaajuusVuosiviikkotunneissa(a)

export const isLanguageAcquisition = (a: any): a is LanguageAcquisition =>
  a?.$class === 'LanguageAcquisition'

export const isLanguageAndLiterature = (a: any): a is LanguageAndLiterature =>
  a?.$class === 'LanguageAndLiterature'

export const isLukiodiplomit2019 = (a: any): a is Lukiodiplomit2019 =>
  a?.$class === 'Lukiodiplomit2019'

export const isLukionArviointi = (a: any): a is LukionArviointi =>
  isNumeerinenLukionArviointi(a) || isSanallinenLukionArviointi(a)

export const isLukionKurssi2015 = (a: any): a is LukionKurssi2015 =>
  isPaikallinenLukionKurssi2015(a) || isValtakunnallinenLukionKurssi2015(a)

export const isLukionKurssinSuoritus2015 = (
  a: any
): a is LukionKurssinSuoritus2015 => a?.$class === 'LukionKurssinSuoritus2015'

export const isLukionMatematiikka2015 = (a: any): a is LukionMatematiikka2015 =>
  a?.$class === 'LukionMatematiikka2015'

export const isLukionMatematiikka2019 = (a: any): a is LukionMatematiikka2019 =>
  a?.$class === 'LukionMatematiikka2019'

export const isLukionModuuliMuissaOpinnoissa2019 = (
  a: any
): a is LukionModuuliMuissaOpinnoissa2019 =>
  isLukionMuuModuuliMuissaOpinnoissa2019(a) ||
  isLukionVieraanKielenModuuliMuissaOpinnoissa2019(a)

export const isLukionModuulinSuoritusMuissaOpinnoissa2019 = (
  a: any
): a is LukionModuulinSuoritusMuissaOpinnoissa2019 =>
  a?.$class === 'LukionModuulinSuoritusMuissaOpinnoissa2019'

export const isLukionModuulinSuoritusOppiaineissa2019 = (
  a: any
): a is LukionModuulinSuoritusOppiaineissa2019 =>
  a?.$class === 'LukionModuulinSuoritusOppiaineissa2019'

export const isLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 = (
  a: any
): a is LukionModuulinTaiPaikallisenOpintojaksonArviointi2019 =>
  isNumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019(a) ||
  isSanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019(a)

export const isLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 =
  (
    a: any
  ): a is LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 =>
    isLukionModuulinSuoritusMuissaOpinnoissa2019(a) ||
    isLukionPaikallisenOpintojaksonSuoritus2019(a)

export const isLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 =
  (
    a: any
  ): a is LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 =>
    isLukionModuulinSuoritusOppiaineissa2019(a) ||
    isLukionPaikallisenOpintojaksonSuoritus2019(a)

export const isLukionModuuliOppiaineissa2019 = (
  a: any
): a is LukionModuuliOppiaineissa2019 =>
  isLukionMuuModuuliOppiaineissa2019(a) ||
  isLukionVieraanKielenModuuliOppiaineissa2019(a)

export const isLukionMuuModuuliMuissaOpinnoissa2019 = (
  a: any
): a is LukionMuuModuuliMuissaOpinnoissa2019 =>
  a?.$class === 'LukionMuuModuuliMuissaOpinnoissa2019'

export const isLukionMuuModuuliOppiaineissa2019 = (
  a: any
): a is LukionMuuModuuliOppiaineissa2019 =>
  a?.$class === 'LukionMuuModuuliOppiaineissa2019'

export const isLukionMuuValtakunnallinenOppiaine2015 = (
  a: any
): a is LukionMuuValtakunnallinenOppiaine2015 =>
  a?.$class === 'LukionMuuValtakunnallinenOppiaine2015'

export const isLukionMuuValtakunnallinenOppiaine2019 = (
  a: any
): a is LukionMuuValtakunnallinenOppiaine2019 =>
  a?.$class === 'LukionMuuValtakunnallinenOppiaine2019'

export const isLukionOpiskeluoikeudenLisätiedot = (
  a: any
): a is LukionOpiskeluoikeudenLisätiedot =>
  a?.$class === 'LukionOpiskeluoikeudenLisätiedot'

export const isLukionOpiskeluoikeudenTila = (
  a: any
): a is LukionOpiskeluoikeudenTila => a?.$class === 'LukionOpiskeluoikeudenTila'

export const isLukionOpiskeluoikeus = (a: any): a is LukionOpiskeluoikeus =>
  a?.$class === 'LukionOpiskeluoikeus'

export const isLukionOpiskeluoikeusjakso = (
  a: any
): a is LukionOpiskeluoikeusjakso => a?.$class === 'LukionOpiskeluoikeusjakso'

export const isLukionOppiaine2015 = (a: any): a is LukionOppiaine2015 =>
  isLukionMatematiikka2015(a) ||
  isLukionMuuValtakunnallinenOppiaine2015(a) ||
  isLukionUskonto2015(a) ||
  isLukionÄidinkieliJaKirjallisuus2015(a) ||
  isPaikallinenLukionOppiaine2015(a) ||
  isVierasTaiToinenKotimainenKieli2015(a)

export const isLukionOppiaine2019 = (a: any): a is LukionOppiaine2019 =>
  isLukionMatematiikka2019(a) ||
  isLukionMuuValtakunnallinenOppiaine2019(a) ||
  isLukionUskonto2019(a) ||
  isLukionÄidinkieliJaKirjallisuus2019(a) ||
  isPaikallinenLukionOppiaine2019(a) ||
  isVierasTaiToinenKotimainenKieli2019(a)

export const isLukionOppiaineenArviointi = (
  a: any
): a is LukionOppiaineenArviointi => a?.$class === 'LukionOppiaineenArviointi'

export const isLukionOppiaineenArviointi2019 = (
  a: any
): a is LukionOppiaineenArviointi2019 =>
  isNumeerinenLukionOppiaineenArviointi2019(a) ||
  isSanallinenLukionOppiaineenArviointi2019(a)

export const isLukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa =
  (
    a: any
  ): a is LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa =>
    a?.$class ===
    'LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa'

export const isLukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019 =
  (
    a: any
  ): a is LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019 =>
    a?.$class ===
    'LukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019'

export const isLukionOppiaineenOppimääränSuoritus2015 = (
  a: any
): a is LukionOppiaineenOppimääränSuoritus2015 =>
  a?.$class === 'LukionOppiaineenOppimääränSuoritus2015'

export const isLukionOppiaineenPreIBSuoritus2019 = (
  a: any
): a is LukionOppiaineenPreIBSuoritus2019 =>
  a?.$class === 'LukionOppiaineenPreIBSuoritus2019'

export const isLukionOppiaineenSuoritus2015 = (
  a: any
): a is LukionOppiaineenSuoritus2015 =>
  a?.$class === 'LukionOppiaineenSuoritus2015'

export const isLukionOppiaineenSuoritus2019 = (
  a: any
): a is LukionOppiaineenSuoritus2019 =>
  a?.$class === 'LukionOppiaineenSuoritus2019'

export const isLukionOppiaineidenOppimäärienSuoritus2019 = (
  a: any
): a is LukionOppiaineidenOppimäärienSuoritus2019 =>
  a?.$class === 'LukionOppiaineidenOppimäärienSuoritus2019'

export const isLukionOppiaineidenOppimäärät2019 = (
  a: any
): a is LukionOppiaineidenOppimäärät2019 =>
  a?.$class === 'LukionOppiaineidenOppimäärät2019'

export const isLukionOppiaineidenOppimäärätKoodi2019 = (
  a: any
): a is LukionOppiaineidenOppimäärätKoodi2019 =>
  a?.$class === 'LukionOppiaineidenOppimäärätKoodi2019'

export const isLukionOppiaineTaiEiTiedossaOppiaine2015 = (
  a: any
): a is LukionOppiaineTaiEiTiedossaOppiaine2015 =>
  isEiTiedossaOppiaine(a) ||
  isLukionMatematiikka2015(a) ||
  isLukionMuuValtakunnallinenOppiaine2015(a) ||
  isLukionUskonto2015(a) ||
  isLukionÄidinkieliJaKirjallisuus2015(a) ||
  isPaikallinenLukionOppiaine2015(a) ||
  isVierasTaiToinenKotimainenKieli2015(a)

export const isLukionOppimäärä = (a: any): a is LukionOppimäärä =>
  a?.$class === 'LukionOppimäärä'

export const isLukionOppimääränOsasuoritus2015 = (
  a: any
): a is LukionOppimääränOsasuoritus2015 =>
  isLukionOppiaineenSuoritus2015(a) || isMuidenLukioOpintojenSuoritus2015(a)

export const isLukionOppimääränOsasuoritus2019 = (
  a: any
): a is LukionOppimääränOsasuoritus2019 =>
  isLukionOppiaineenSuoritus2019(a) || isMuidenLukioOpintojenSuoritus2019(a)

export const isLukionOppimääränSuoritus2015 = (
  a: any
): a is LukionOppimääränSuoritus2015 =>
  a?.$class === 'LukionOppimääränSuoritus2015'

export const isLukionOppimääränSuoritus2019 = (
  a: any
): a is LukionOppimääränSuoritus2019 =>
  a?.$class === 'LukionOppimääränSuoritus2019'

export const isLukionPaikallinenOpintojakso2019 = (
  a: any
): a is LukionPaikallinenOpintojakso2019 =>
  a?.$class === 'LukionPaikallinenOpintojakso2019'

export const isLukionPaikallisenOpintojaksonSuoritus2019 = (
  a: any
): a is LukionPaikallisenOpintojaksonSuoritus2019 =>
  a?.$class === 'LukionPaikallisenOpintojaksonSuoritus2019'

export const isLukionPäätasonSuoritus = (a: any): a is LukionPäätasonSuoritus =>
  isLukionOppiaineenOppimääränSuoritus2015(a) ||
  isLukionOppiaineidenOppimäärienSuoritus2019(a) ||
  isLukionOppimääränSuoritus2015(a) ||
  isLukionOppimääränSuoritus2019(a)

export const isLukionUskonto2015 = (a: any): a is LukionUskonto2015 =>
  a?.$class === 'LukionUskonto2015'

export const isLukionUskonto2019 = (a: any): a is LukionUskonto2019 =>
  a?.$class === 'LukionUskonto2019'

export const isLukionVieraanKielenModuuliMuissaOpinnoissa2019 = (
  a: any
): a is LukionVieraanKielenModuuliMuissaOpinnoissa2019 =>
  a?.$class === 'LukionVieraanKielenModuuliMuissaOpinnoissa2019'

export const isLukionVieraanKielenModuuliOppiaineissa2019 = (
  a: any
): a is LukionVieraanKielenModuuliOppiaineissa2019 =>
  a?.$class === 'LukionVieraanKielenModuuliOppiaineissa2019'

export const isLukionÄidinkieliJaKirjallisuus2015 = (
  a: any
): a is LukionÄidinkieliJaKirjallisuus2015 =>
  a?.$class === 'LukionÄidinkieliJaKirjallisuus2015'

export const isLukionÄidinkieliJaKirjallisuus2019 = (
  a: any
): a is LukionÄidinkieliJaKirjallisuus2019 =>
  a?.$class === 'LukionÄidinkieliJaKirjallisuus2019'

export const isLukioonValmistavaKoulutus = (
  a: any
): a is LukioonValmistavaKoulutus => a?.$class === 'LukioonValmistavaKoulutus'

export const isLukioonValmistavanKoulutuksenKurssi = (
  a: any
): a is LukioonValmistavanKoulutuksenKurssi =>
  isPaikallinenLukioonValmistavanKoulutuksenKurssi(a) ||
  isValtakunnallinenLukioonValmistavanKoulutuksenKurssi(a)

export const isLukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot =>
  a?.$class === 'LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot'

export const isLukioonValmistavanKoulutuksenOpiskeluoikeus = (
  a: any
): a is LukioonValmistavanKoulutuksenOpiskeluoikeus =>
  a?.$class === 'LukioonValmistavanKoulutuksenOpiskeluoikeus'

export const isLukioonValmistavanKoulutuksenOppiaine = (
  a: any
): a is LukioonValmistavanKoulutuksenOppiaine =>
  isLukioonValmistavaÄidinkieliJaKirjallisuus(a) ||
  isMuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine(a) ||
  isMuutKielet(a) ||
  isPaikallinenLukioonValmistavanKoulutuksenOppiaine(a)

export const isLukioonValmistavanKoulutuksenOppiaineenSuoritus = (
  a: any
): a is LukioonValmistavanKoulutuksenOppiaineenSuoritus =>
  a?.$class === 'LukioonValmistavanKoulutuksenOppiaineenSuoritus'

export const isLukioonValmistavanKoulutuksenOsasuoritus = (
  a: any
): a is LukioonValmistavanKoulutuksenOsasuoritus =>
  isLukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa(a) ||
  isLukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019(a) ||
  isLukioonValmistavanKoulutuksenOppiaineenSuoritus(a)

export const isLukioonValmistavanKoulutuksenSuoritus = (
  a: any
): a is LukioonValmistavanKoulutuksenSuoritus =>
  a?.$class === 'LukioonValmistavanKoulutuksenSuoritus'

export const isLukioonValmistavanKurssinSuoritus = (
  a: any
): a is LukioonValmistavanKurssinSuoritus =>
  a?.$class === 'LukioonValmistavanKurssinSuoritus'

export const isLukioonValmistavaÄidinkieliJaKirjallisuus = (
  a: any
): a is LukioonValmistavaÄidinkieliJaKirjallisuus =>
  a?.$class === 'LukioonValmistavaÄidinkieliJaKirjallisuus'

export const isLukioOpintojenSuoritus = (a: any): a is LukioOpintojenSuoritus =>
  a?.$class === 'LukioOpintojenSuoritus'

export const isLukukausi_Ilmoittautuminen = (
  a: any
): a is Lukukausi_Ilmoittautuminen => a?.$class === 'Lukukausi_Ilmoittautuminen'

export const isLukukausi_Ilmoittautumisjakso = (
  a: any
): a is Lukukausi_Ilmoittautumisjakso =>
  a?.$class === 'Lukukausi_Ilmoittautumisjakso'

export const isLukutaitokoulutuksenArviointi = (
  a: any
): a is LukutaitokoulutuksenArviointi =>
  a?.$class === 'LukutaitokoulutuksenArviointi'

export const isLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu = (
  a: any
): a is Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu =>
  a?.$class === 'Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu'

export const isLähdejärjestelmäId = (a: any): a is LähdejärjestelmäId =>
  a?.$class === 'LähdejärjestelmäId'

export const isMaksuttomuus = (a: any): a is Maksuttomuus =>
  a?.$class === 'Maksuttomuus'

export const isMuidenLukioOpintojenPreIBSuoritus2019 = (
  a: any
): a is MuidenLukioOpintojenPreIBSuoritus2019 =>
  a?.$class === 'MuidenLukioOpintojenPreIBSuoritus2019'

export const isMuidenLukioOpintojenSuoritus2015 = (
  a: any
): a is MuidenLukioOpintojenSuoritus2015 =>
  a?.$class === 'MuidenLukioOpintojenSuoritus2015'

export const isMuidenLukioOpintojenSuoritus2019 = (
  a: any
): a is MuidenLukioOpintojenSuoritus2019 =>
  a?.$class === 'MuidenLukioOpintojenSuoritus2019'

export const isMuidenOpintovalmiuksiaTukevienOpintojenSuoritus = (
  a: any
): a is MuidenOpintovalmiuksiaTukevienOpintojenSuoritus =>
  a?.$class === 'MuidenOpintovalmiuksiaTukevienOpintojenSuoritus'

export const isMuuAikuistenPerusopetuksenAlkuvaiheenOppiaine = (
  a: any
): a is MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine =>
  a?.$class === 'MuuAikuistenPerusopetuksenAlkuvaiheenOppiaine'

export const isMuuAikuistenPerusopetuksenOppiaine = (
  a: any
): a is MuuAikuistenPerusopetuksenOppiaine =>
  a?.$class === 'MuuAikuistenPerusopetuksenOppiaine'

export const isMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus =
  (
    a: any
  ): a is MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus =>
    a?.$class ===
    'MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus'

export const isMuuallaSuoritetutVapaanSivistystyönOpinnot = (
  a: any
): a is MuuallaSuoritetutVapaanSivistystyönOpinnot =>
  a?.$class === 'MuuallaSuoritetutVapaanSivistystyönOpinnot'

export const isMuuAmmatillinenKoulutus = (
  a: any
): a is MuuAmmatillinenKoulutus =>
  isAmmatilliseenTehtäväänValmistavaKoulutus(a) ||
  isPaikallinenMuuAmmatillinenKoulutus(a)

export const isMuuAmmatillinenOsasuoritus = (
  a: any
): a is MuuAmmatillinenOsasuoritus =>
  isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus(a) ||
  isTutkinnonOsaaPienemmänKokonaisuudenSuoritus(a) ||
  isYhteisenTutkinnonOsanOsaAlueenSuoritus(a)

export const isMuuDiplomaOppiaine = (a: any): a is MuuDiplomaOppiaine =>
  a?.$class === 'MuuDiplomaOppiaine'

export const isMuuKorkeakoulunOpinto = (a: any): a is MuuKorkeakoulunOpinto =>
  a?.$class === 'MuuKorkeakoulunOpinto'

export const isMuuKorkeakoulunSuoritus = (
  a: any
): a is MuuKorkeakoulunSuoritus => a?.$class === 'MuuKorkeakoulunSuoritus'

export const isMuuKuinSäänneltyKoulutus = (
  a: any
): a is MuuKuinSäänneltyKoulutus => a?.$class === 'MuuKuinSäänneltyKoulutus'

export const isMuuKuinYhteinenTutkinnonOsa = (
  a: any
): a is MuuKuinYhteinenTutkinnonOsa =>
  isMuuValtakunnallinenTutkinnonOsa(a) || isPaikallinenTutkinnonOsa(a)

export const isMuuLukioOpinto2015 = (a: any): a is MuuLukioOpinto2015 =>
  a?.$class === 'MuuLukioOpinto2015'

export const isMuunAmmatillisenKoulutuksenArviointi = (
  a: any
): a is MuunAmmatillisenKoulutuksenArviointi =>
  a?.$class === 'MuunAmmatillisenKoulutuksenArviointi'

export const isMuunAmmatillisenKoulutuksenOsasuorituksenLisätieto = (
  a: any
): a is MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto =>
  a?.$class === 'MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto'

export const isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus = (
  a: any
): a is MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus =>
  a?.$class === 'MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus'

export const isMuunAmmatillisenKoulutuksenOsasuoritus = (
  a: any
): a is MuunAmmatillisenKoulutuksenOsasuoritus =>
  a?.$class === 'MuunAmmatillisenKoulutuksenOsasuoritus'

export const isMuunAmmatillisenKoulutuksenSuoritus = (
  a: any
): a is MuunAmmatillisenKoulutuksenSuoritus =>
  a?.$class === 'MuunAmmatillisenKoulutuksenSuoritus'

export const isMuunAmmatillisenTutkinnonOsanSuoritus = (
  a: any
): a is MuunAmmatillisenTutkinnonOsanSuoritus =>
  a?.$class === 'MuunAmmatillisenTutkinnonOsanSuoritus'

export const isMuunKuinSäännellynKoulutuksenArviointi = (
  a: any
): a is MuunKuinSäännellynKoulutuksenArviointi =>
  a?.$class === 'MuunKuinSäännellynKoulutuksenArviointi'

export const isMuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso = (
  a: any
): a is MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso =>
  a?.$class === 'MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso'

export const isMuunKuinSäännellynKoulutuksenOpiskeluoikeus = (
  a: any
): a is MuunKuinSäännellynKoulutuksenOpiskeluoikeus =>
  a?.$class === 'MuunKuinSäännellynKoulutuksenOpiskeluoikeus'

export const isMuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli = (
  a: any
): a is MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli =>
  a?.$class === 'MuunKuinSäännellynKoulutuksenOsasuorituksenKoulutusmoduuli'

export const isMuunKuinSäännellynKoulutuksenOsasuoritus = (
  a: any
): a is MuunKuinSäännellynKoulutuksenOsasuoritus =>
  a?.$class === 'MuunKuinSäännellynKoulutuksenOsasuoritus'

export const isMuunKuinSäännellynKoulutuksenPäätasonSuoritus = (
  a: any
): a is MuunKuinSäännellynKoulutuksenPäätasonSuoritus =>
  a?.$class === 'MuunKuinSäännellynKoulutuksenPäätasonSuoritus'

export const isMuunKuinSäännellynKoulutuksenTila = (
  a: any
): a is MuunKuinSäännellynKoulutuksenTila =>
  a?.$class === 'MuunKuinSäännellynKoulutuksenTila'

export const isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus = (
  a: any
): a is MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus =>
  a?.$class === 'MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'

export const isMuuNuortenPerusopetuksenOppiaine = (
  a: any
): a is MuuNuortenPerusopetuksenOppiaine =>
  a?.$class === 'MuuNuortenPerusopetuksenOppiaine'

export const isMuuPerusopetuksenLisäopetuksenKoulutusmoduuli = (
  a: any
): a is MuuPerusopetuksenLisäopetuksenKoulutusmoduuli =>
  a?.$class === 'MuuPerusopetuksenLisäopetuksenKoulutusmoduuli'

export const isMuuPerusopetuksenLisäopetuksenSuoritus = (
  a: any
): a is MuuPerusopetuksenLisäopetuksenSuoritus =>
  a?.$class === 'MuuPerusopetuksenLisäopetuksenSuoritus'

export const isMuutKielet = (a: any): a is MuutKielet =>
  a?.$class === 'MuutKielet'

export const isMuutLukionSuoritukset2019 = (
  a: any
): a is MuutLukionSuoritukset2019 => a?.$class === 'MuutLukionSuoritukset2019'

export const isMuutSuorituksetTaiVastaavat2019 = (
  a: any
): a is MuutSuorituksetTaiVastaavat2019 =>
  isLukiodiplomit2019(a) ||
  isMuutLukionSuoritukset2019(a) ||
  isTemaattisetOpinnot2019(a)

export const isMuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine = (
  a: any
): a is MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine =>
  a?.$class === 'MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine'

export const isMuuValtakunnallinenTutkinnonOsa = (
  a: any
): a is MuuValtakunnallinenTutkinnonOsa =>
  a?.$class === 'MuuValtakunnallinenTutkinnonOsa'

export const isMYPArviointi = (a: any): a is MYPArviointi =>
  isNumeerinenInternationalSchoolOppiaineenArviointi(a) ||
  isPassFailOppiaineenArviointi(a)

export const isMYPLuokkaAste = (a: any): a is MYPLuokkaAste =>
  a?.$class === 'MYPLuokkaAste'

export const isMYPOppiaine = (a: any): a is MYPOppiaine =>
  isLanguageAcquisition(a) || isLanguageAndLiterature(a) || isMYPOppiaineMuu(a)

export const isMYPOppiaineenSuoritus = (a: any): a is MYPOppiaineenSuoritus =>
  a?.$class === 'MYPOppiaineenSuoritus'

export const isMYPOppiaineMuu = (a: any): a is MYPOppiaineMuu =>
  a?.$class === 'MYPOppiaineMuu'

export const isMYPVuosiluokanSuoritus = (a: any): a is MYPVuosiluokanSuoritus =>
  a?.$class === 'MYPVuosiluokanSuoritus'

export const isNumeerinenInternationalSchoolOppiaineenArviointi = (
  a: any
): a is NumeerinenInternationalSchoolOppiaineenArviointi =>
  a?.$class === 'NumeerinenInternationalSchoolOppiaineenArviointi'

export const isNumeerinenLukionArviointi = (
  a: any
): a is NumeerinenLukionArviointi => a?.$class === 'NumeerinenLukionArviointi'

export const isNumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 =
  (
    a: any
  ): a is NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 =>
    a?.$class ===
    'NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019'

export const isNumeerinenLukionOppiaineenArviointi2019 = (
  a: any
): a is NumeerinenLukionOppiaineenArviointi2019 =>
  a?.$class === 'NumeerinenLukionOppiaineenArviointi2019'

export const isNumeerinenPerusopetuksenOppiaineenArviointi = (
  a: any
): a is NumeerinenPerusopetuksenOppiaineenArviointi =>
  a?.$class === 'NumeerinenPerusopetuksenOppiaineenArviointi'

export const isNuortenPerusopetuksenOpiskeluoikeudenTila = (
  a: any
): a is NuortenPerusopetuksenOpiskeluoikeudenTila =>
  a?.$class === 'NuortenPerusopetuksenOpiskeluoikeudenTila'

export const isNuortenPerusopetuksenOpiskeluoikeusjakso = (
  a: any
): a is NuortenPerusopetuksenOpiskeluoikeusjakso =>
  a?.$class === 'NuortenPerusopetuksenOpiskeluoikeusjakso'

export const isNuortenPerusopetuksenOppiaine = (
  a: any
): a is NuortenPerusopetuksenOppiaine =>
  isMuuNuortenPerusopetuksenOppiaine(a) ||
  isNuortenPerusopetuksenPaikallinenOppiaine(a) ||
  isNuortenPerusopetuksenUskonto(a) ||
  isNuortenPerusopetuksenVierasTaiToinenKotimainenKieli(a) ||
  isNuortenPerusopetuksenÄidinkieliJaKirjallisuus(a)

export const isNuortenPerusopetuksenOppiaineenOppimääränSuoritus = (
  a: any
): a is NuortenPerusopetuksenOppiaineenOppimääränSuoritus =>
  a?.$class === 'NuortenPerusopetuksenOppiaineenOppimääränSuoritus'

export const isNuortenPerusopetuksenOppiaineenSuoritus = (
  a: any
): a is NuortenPerusopetuksenOppiaineenSuoritus =>
  a?.$class === 'NuortenPerusopetuksenOppiaineenSuoritus'

export const isNuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa =
  (
    a: any
  ): a is NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa =>
    a?.$class ===
    'NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa'

export const isNuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine = (
  a: any
): a is NuortenPerusopetuksenOppiainenTaiEiTiedossaOppiaine =>
  isEiTiedossaOppiaine(a) ||
  isMuuNuortenPerusopetuksenOppiaine(a) ||
  isNuortenPerusopetuksenPaikallinenOppiaine(a) ||
  isNuortenPerusopetuksenUskonto(a) ||
  isNuortenPerusopetuksenVierasTaiToinenKotimainenKieli(a) ||
  isNuortenPerusopetuksenÄidinkieliJaKirjallisuus(a)

export const isNuortenPerusopetuksenOppimääränSuoritus = (
  a: any
): a is NuortenPerusopetuksenOppimääränSuoritus =>
  a?.$class === 'NuortenPerusopetuksenOppimääränSuoritus'

export const isNuortenPerusopetuksenPaikallinenOppiaine = (
  a: any
): a is NuortenPerusopetuksenPaikallinenOppiaine =>
  a?.$class === 'NuortenPerusopetuksenPaikallinenOppiaine'

export const isNuortenPerusopetuksenUskonto = (
  a: any
): a is NuortenPerusopetuksenUskonto =>
  a?.$class === 'NuortenPerusopetuksenUskonto'

export const isNuortenPerusopetuksenVierasTaiToinenKotimainenKieli = (
  a: any
): a is NuortenPerusopetuksenVierasTaiToinenKotimainenKieli =>
  a?.$class === 'NuortenPerusopetuksenVierasTaiToinenKotimainenKieli'

export const isNuortenPerusopetuksenÄidinkieliJaKirjallisuus = (
  a: any
): a is NuortenPerusopetuksenÄidinkieliJaKirjallisuus =>
  a?.$class === 'NuortenPerusopetuksenÄidinkieliJaKirjallisuus'

export const isNuortenPerusopetus = (a: any): a is NuortenPerusopetus =>
  a?.$class === 'NuortenPerusopetus'

export const isNurseryLuokkaAste = (a: any): a is NurseryLuokkaAste =>
  a?.$class === 'NurseryLuokkaAste'

export const isNurseryVuosiluokanSuoritus = (
  a: any
): a is NurseryVuosiluokanSuoritus => a?.$class === 'NurseryVuosiluokanSuoritus'

export const isNäyttö = (a: any): a is Näyttö => a?.$class === 'Näyttö'

export const isNäyttötutkintoonValmistavaKoulutus = (
  a: any
): a is NäyttötutkintoonValmistavaKoulutus =>
  a?.$class === 'NäyttötutkintoonValmistavaKoulutus'

export const isNäyttötutkintoonValmistavanKoulutuksenOsa = (
  a: any
): a is NäyttötutkintoonValmistavanKoulutuksenOsa =>
  isMuuValtakunnallinenTutkinnonOsa(a) ||
  isPaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa(a) ||
  isYhteinenTutkinnonOsa(a)

export const isNäyttötutkintoonValmistavanKoulutuksenOsanSuoritus = (
  a: any
): a is NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus =>
  a?.$class === 'NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus'

export const isNäyttötutkintoonValmistavanKoulutuksenSuoritus = (
  a: any
): a is NäyttötutkintoonValmistavanKoulutuksenSuoritus =>
  a?.$class === 'NäyttötutkintoonValmistavanKoulutuksenSuoritus'

export const isNäytönArviointi = (a: any): a is NäytönArviointi =>
  a?.$class === 'NäytönArviointi'

export const isNäytönArviointikohde = (a: any): a is NäytönArviointikohde =>
  a?.$class === 'NäytönArviointikohde'

export const isNäytönArvioitsija = (a: any): a is NäytönArvioitsija =>
  a?.$class === 'NäytönArvioitsija'

export const isNäytönSuoritusaika = (a: any): a is NäytönSuoritusaika =>
  a?.$class === 'NäytönSuoritusaika'

export const isNäytönSuorituspaikka = (a: any): a is NäytönSuorituspaikka =>
  a?.$class === 'NäytönSuorituspaikka'

export const isOidHenkilö = (a: any): a is OidHenkilö =>
  a?.$class === 'OidHenkilö'

export const isOidOrganisaatio = (a: any): a is OidOrganisaatio =>
  a?.$class === 'OidOrganisaatio'

export const isOikeuttaMaksuttomuuteenPidennetty = (
  a: any
): a is OikeuttaMaksuttomuuteenPidennetty =>
  a?.$class === 'OikeuttaMaksuttomuuteenPidennetty'

export const isOmanÄidinkielenOpinnotLaajuusKursseina = (
  a: any
): a is OmanÄidinkielenOpinnotLaajuusKursseina =>
  a?.$class === 'OmanÄidinkielenOpinnotLaajuusKursseina'

export const isOmanÄidinkielenOpinnotLaajuusOpintopisteinä = (
  a: any
): a is OmanÄidinkielenOpinnotLaajuusOpintopisteinä =>
  a?.$class === 'OmanÄidinkielenOpinnotLaajuusOpintopisteinä'

export const isOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina = (
  a: any
): a is OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina =>
  a?.$class === 'OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina'

export const isOpiskeluoikeudenOrganisaatiohistoria = (
  a: any
): a is OpiskeluoikeudenOrganisaatiohistoria =>
  a?.$class === 'OpiskeluoikeudenOrganisaatiohistoria'

export const isOpiskeluoikeus = (a: any): a is Opiskeluoikeus =>
  isAikuistenPerusopetuksenOpiskeluoikeus(a) ||
  isAmmatillinenOpiskeluoikeus(a) ||
  isDIAOpiskeluoikeus(a) ||
  isEsiopetuksenOpiskeluoikeus(a) ||
  isEuropeanSchoolOfHelsinkiOpiskeluoikeus(a) ||
  isIBOpiskeluoikeus(a) ||
  isInternationalSchoolOpiskeluoikeus(a) ||
  isKorkeakoulunOpiskeluoikeus(a) ||
  isLukionOpiskeluoikeus(a) ||
  isLukioonValmistavanKoulutuksenOpiskeluoikeus(a) ||
  isMuunKuinSäännellynKoulutuksenOpiskeluoikeus(a) ||
  isPerusopetukseenValmistavanOpetuksenOpiskeluoikeus(a) ||
  isPerusopetuksenLisäopetuksenOpiskeluoikeus(a) ||
  isPerusopetuksenOpiskeluoikeus(a) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeus(a) ||
  isVapaanSivistystyönOpiskeluoikeus(a) ||
  isYlioppilastutkinnonOpiskeluoikeus(a)

export const isOpiskeluoikeusAvaintaEiLöydy = (
  a: any
): a is OpiskeluoikeusAvaintaEiLöydy =>
  a?.$class === 'OpiskeluoikeusAvaintaEiLöydy'

export const isOpiskeluvalmiuksiaTukevienOpintojenJakso = (
  a: any
): a is OpiskeluvalmiuksiaTukevienOpintojenJakso =>
  a?.$class === 'OpiskeluvalmiuksiaTukevienOpintojenJakso'

export const isOppiaineenTaiToiminta_AlueenSuoritus = (
  a: any
): a is OppiaineenTaiToiminta_AlueenSuoritus =>
  isNuortenPerusopetuksenOppiaineenSuoritus(a) ||
  isPerusopetuksenToiminta_AlueenSuoritus(a)

export const isOppilaitos = (a: any): a is Oppilaitos =>
  a?.$class === 'Oppilaitos'

export const isOppisopimuksellinenJärjestämismuoto = (
  a: any
): a is OppisopimuksellinenJärjestämismuoto =>
  a?.$class === 'OppisopimuksellinenJärjestämismuoto'

export const isOppisopimuksellinenOsaamisenHankkimistapa = (
  a: any
): a is OppisopimuksellinenOsaamisenHankkimistapa =>
  a?.$class === 'OppisopimuksellinenOsaamisenHankkimistapa'

export const isOppisopimuksenPurkaminen = (
  a: any
): a is OppisopimuksenPurkaminen => a?.$class === 'OppisopimuksenPurkaminen'

export const isOppisopimus = (a: any): a is Oppisopimus =>
  a?.$class === 'Oppisopimus'

export const isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =
  (
    a: any
  ): a is OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus =>
    a?.$class ===
    'OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'

export const isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 =
  (
    a: any
  ): a is OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 =>
    a?.$class ===
    'OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'

export const isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =
  (
    a: any
  ): a is OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus =>
    a?.$class ===
    'OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus'

export const isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus = (
  a: any
): a is OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus =>
  a?.$class === 'OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus'

export const isOppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus = (
  a: any
): a is OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus =>
  a?.$class === 'OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus'

export const isOppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso =
  (
    a: any
  ): a is OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso =>
    a?.$class ===
    'OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso'

export const isOppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus =
  (
    a: any
  ): a is OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus =>
    a?.$class ===
    'OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus'

export const isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi =
  (
    a: any
  ): a is OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi =>
    a?.$class ===
    'OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi'

export const isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus =
  (
    a: any
  ): a is OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus =>
    a?.$class ===
    'OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus'

export const isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =
  (
    a: any
  ): a is OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =>
    a?.$class ===
    'OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus'

export const isOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus = (
  a: any
): a is OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus =>
  isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
    a
  ) ||
  isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
    a
  )

export const isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot =
  (
    a: any
  ): a is OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot =>
    a?.$class ===
    'OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot'

export const isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus =
  (
    a: any
  ): a is OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus =>
    a?.$class ===
    'OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus'

export const isOrganisaatio = (a: any): a is Organisaatio =>
  isKoulutustoimija(a) ||
  isOidOrganisaatio(a) ||
  isOppilaitos(a) ||
  isToimipiste(a) ||
  isTutkintotoimikunta(a) ||
  isYritys(a)

export const isOrganisaatiohenkilö = (a: any): a is Organisaatiohenkilö =>
  a?.$class === 'Organisaatiohenkilö'

export const isOrganisaatiohenkilöValinnaisellaTittelillä = (
  a: any
): a is OrganisaatiohenkilöValinnaisellaTittelillä =>
  a?.$class === 'OrganisaatiohenkilöValinnaisellaTittelillä'

export const isOrganisaatiovahvistus = (a: any): a is Organisaatiovahvistus =>
  a?.$class === 'Organisaatiovahvistus'

export const isOrganisaatioWithOid = (a: any): a is OrganisaatioWithOid =>
  isKoulutustoimija(a) ||
  isOidOrganisaatio(a) ||
  isOppilaitos(a) ||
  isToimipiste(a)

export const isOsaAikaisuusJakso = (a: any): a is OsaAikaisuusJakso =>
  a?.$class === 'OsaAikaisuusJakso'

export const isOsaamisalajakso = (a: any): a is Osaamisalajakso =>
  isOsaamisalajakso(a) || isKoodistokoodiviite(a)

export const isOsaamisenHankkimistapa = (a: any): a is OsaamisenHankkimistapa =>
  isOppisopimuksellinenOsaamisenHankkimistapa(a) ||
  isOsaamisenHankkimistapaIlmanLisätietoja(a)

export const isOsaamisenHankkimistapaIlmanLisätietoja = (
  a: any
): a is OsaamisenHankkimistapaIlmanLisätietoja =>
  a?.$class === 'OsaamisenHankkimistapaIlmanLisätietoja'

export const isOsaamisenHankkimistapajakso = (
  a: any
): a is OsaamisenHankkimistapajakso =>
  a?.$class === 'OsaamisenHankkimistapajakso'

export const isOsaamisenTunnustaminen = (a: any): a is OsaamisenTunnustaminen =>
  a?.$class === 'OsaamisenTunnustaminen'

export const isOsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =
  (
    a: any
  ): a is OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus =>
    a?.$class ===
    'OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'

export const isOsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus = (
  a: any
): a is OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus =>
  a?.$class === 'OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'

export const isOsittaisenAmmatillisenTutkinnonOsanSuoritus = (
  a: any
): a is OsittaisenAmmatillisenTutkinnonOsanSuoritus =>
  isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(a) ||
  isOsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
    a
  ) ||
  isOsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(a) ||
  isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(a)

export const isPaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi = (
  a: any
): a is PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi =>
  a?.$class === 'PaikallinenAikuistenPerusopetuksenAlkuvaiheenKurssi'

export const isPaikallinenAikuistenPerusopetuksenKurssi = (
  a: any
): a is PaikallinenAikuistenPerusopetuksenKurssi =>
  a?.$class === 'PaikallinenAikuistenPerusopetuksenKurssi'

export const isPaikallinenAmmatillisenTutkinnonOsanOsaAlue = (
  a: any
): a is PaikallinenAmmatillisenTutkinnonOsanOsaAlue =>
  a?.$class === 'PaikallinenAmmatillisenTutkinnonOsanOsaAlue'

export const isPaikallinenKoodi = (a: any): a is PaikallinenKoodi =>
  a?.$class === 'PaikallinenKoodi'

export const isPaikallinenLukionKurssi2015 = (
  a: any
): a is PaikallinenLukionKurssi2015 =>
  a?.$class === 'PaikallinenLukionKurssi2015'

export const isPaikallinenLukionOpinto = (
  a: any
): a is PaikallinenLukionOpinto => a?.$class === 'PaikallinenLukionOpinto'

export const isPaikallinenLukionOppiaine2015 = (
  a: any
): a is PaikallinenLukionOppiaine2015 =>
  a?.$class === 'PaikallinenLukionOppiaine2015'

export const isPaikallinenLukionOppiaine2019 = (
  a: any
): a is PaikallinenLukionOppiaine2019 =>
  a?.$class === 'PaikallinenLukionOppiaine2019'

export const isPaikallinenLukioonValmistavanKoulutuksenKurssi = (
  a: any
): a is PaikallinenLukioonValmistavanKoulutuksenKurssi =>
  a?.$class === 'PaikallinenLukioonValmistavanKoulutuksenKurssi'

export const isPaikallinenLukioonValmistavanKoulutuksenOppiaine = (
  a: any
): a is PaikallinenLukioonValmistavanKoulutuksenOppiaine =>
  a?.$class === 'PaikallinenLukioonValmistavanKoulutuksenOppiaine'

export const isPaikallinenMuuAmmatillinenKoulutus = (
  a: any
): a is PaikallinenMuuAmmatillinenKoulutus =>
  a?.$class === 'PaikallinenMuuAmmatillinenKoulutus'

export const isPaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa = (
  a: any
): a is PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa =>
  a?.$class === 'PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa'

export const isPaikallinenOpintovalmiuksiaTukevaOpinto = (
  a: any
): a is PaikallinenOpintovalmiuksiaTukevaOpinto =>
  a?.$class === 'PaikallinenOpintovalmiuksiaTukevaOpinto'

export const isPaikallinenTelmaKoulutuksenOsa = (
  a: any
): a is PaikallinenTelmaKoulutuksenOsa =>
  a?.$class === 'PaikallinenTelmaKoulutuksenOsa'

export const isPaikallinenTutkinnonOsa = (
  a: any
): a is PaikallinenTutkinnonOsa => a?.$class === 'PaikallinenTutkinnonOsa'

export const isPaikallinenValmaKoulutuksenOsa = (
  a: any
): a is PaikallinenValmaKoulutuksenOsa =>
  a?.$class === 'PaikallinenValmaKoulutuksenOsa'

export const isPassFailOppiaineenArviointi = (
  a: any
): a is PassFailOppiaineenArviointi =>
  a?.$class === 'PassFailOppiaineenArviointi'

export const isPerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila =>
  a?.$class === 'PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila'

export const isPerusopetukseenValmistavanOpetuksenOpiskeluoikeus = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOpiskeluoikeus =>
  a?.$class === 'PerusopetukseenValmistavanOpetuksenOpiskeluoikeus'

export const isPerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso =>
  a?.$class === 'PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso'

export const isPerusopetukseenValmistavanOpetuksenOppiaine = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOppiaine =>
  a?.$class === 'PerusopetukseenValmistavanOpetuksenOppiaine'

export const isPerusopetukseenValmistavanOpetuksenOppiaineenSuoritus = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus =>
  a?.$class === 'PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus'

export const isPerusopetukseenValmistavanOpetuksenOsasuoritus = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOsasuoritus =>
  isNuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa(a) ||
  isPerusopetukseenValmistavanOpetuksenOppiaineenSuoritus(a)

export const isPerusopetukseenValmistavanOpetuksenSuoritus = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenSuoritus =>
  a?.$class === 'PerusopetukseenValmistavanOpetuksenSuoritus'

export const isPerusopetukseenValmistavaOpetus = (
  a: any
): a is PerusopetukseenValmistavaOpetus =>
  a?.$class === 'PerusopetukseenValmistavaOpetus'

export const isPerusopetuksenKäyttäytymisenArviointi = (
  a: any
): a is PerusopetuksenKäyttäytymisenArviointi =>
  a?.$class === 'PerusopetuksenKäyttäytymisenArviointi'

export const isPerusopetuksenLisäopetuksenAlisuoritus = (
  a: any
): a is PerusopetuksenLisäopetuksenAlisuoritus =>
  isMuuPerusopetuksenLisäopetuksenSuoritus(a) ||
  isPerusopetuksenLisäopetuksenOppiaineenSuoritus(a) ||
  isPerusopetuksenLisäopetuksenToiminta_AlueenSuoritus(a)

export const isPerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot =>
  a?.$class === 'PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot'

export const isPerusopetuksenLisäopetuksenOpiskeluoikeus = (
  a: any
): a is PerusopetuksenLisäopetuksenOpiskeluoikeus =>
  a?.$class === 'PerusopetuksenLisäopetuksenOpiskeluoikeus'

export const isPerusopetuksenLisäopetuksenOppiaineenSuoritus = (
  a: any
): a is PerusopetuksenLisäopetuksenOppiaineenSuoritus =>
  a?.$class === 'PerusopetuksenLisäopetuksenOppiaineenSuoritus'

export const isPerusopetuksenLisäopetuksenSuoritus = (
  a: any
): a is PerusopetuksenLisäopetuksenSuoritus =>
  a?.$class === 'PerusopetuksenLisäopetuksenSuoritus'

export const isPerusopetuksenLisäopetuksenToiminta_AlueenSuoritus = (
  a: any
): a is PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus =>
  a?.$class === 'PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus'

export const isPerusopetuksenLisäopetus = (
  a: any
): a is PerusopetuksenLisäopetus => a?.$class === 'PerusopetuksenLisäopetus'

export const isPerusopetuksenLuokkaAste = (
  a: any
): a is PerusopetuksenLuokkaAste => a?.$class === 'PerusopetuksenLuokkaAste'

export const isPerusopetuksenOpiskeluoikeudenLisätiedot = (
  a: any
): a is PerusopetuksenOpiskeluoikeudenLisätiedot =>
  a?.$class === 'PerusopetuksenOpiskeluoikeudenLisätiedot'

export const isPerusopetuksenOpiskeluoikeus = (
  a: any
): a is PerusopetuksenOpiskeluoikeus =>
  a?.$class === 'PerusopetuksenOpiskeluoikeus'

export const isPerusopetuksenOppiaineenArviointi = (
  a: any
): a is PerusopetuksenOppiaineenArviointi =>
  isNumeerinenPerusopetuksenOppiaineenArviointi(a) ||
  isSanallinenPerusopetuksenOppiaineenArviointi(a)

export const isPerusopetuksenPäätasonSuoritus = (
  a: any
): a is PerusopetuksenPäätasonSuoritus =>
  isNuortenPerusopetuksenOppiaineenOppimääränSuoritus(a) ||
  isNuortenPerusopetuksenOppimääränSuoritus(a) ||
  isPerusopetuksenVuosiluokanSuoritus(a)

export const isPerusopetuksenToiminta_Alue = (
  a: any
): a is PerusopetuksenToiminta_Alue =>
  a?.$class === 'PerusopetuksenToiminta_Alue'

export const isPerusopetuksenToiminta_AlueenSuoritus = (
  a: any
): a is PerusopetuksenToiminta_AlueenSuoritus =>
  a?.$class === 'PerusopetuksenToiminta_AlueenSuoritus'

export const isPerusopetuksenVuosiluokanSuorituksenLiite = (
  a: any
): a is PerusopetuksenVuosiluokanSuorituksenLiite =>
  a?.$class === 'PerusopetuksenVuosiluokanSuorituksenLiite'

export const isPerusopetuksenVuosiluokanSuoritus = (
  a: any
): a is PerusopetuksenVuosiluokanSuoritus =>
  a?.$class === 'PerusopetuksenVuosiluokanSuoritus'

export const isPreIBKoulutusmoduuli2015 = (
  a: any
): a is PreIBKoulutusmoduuli2015 => a?.$class === 'PreIBKoulutusmoduuli2015'

export const isPreIBKoulutusmoduuli2019 = (
  a: any
): a is PreIBKoulutusmoduuli2019 => a?.$class === 'PreIBKoulutusmoduuli2019'

export const isPreIBKurssi2015 = (a: any): a is PreIBKurssi2015 =>
  isIBKurssi(a) ||
  isPaikallinenLukionKurssi2015(a) ||
  isValtakunnallinenLukionKurssi2015(a)

export const isPreIBKurssinSuoritus2015 = (
  a: any
): a is PreIBKurssinSuoritus2015 => a?.$class === 'PreIBKurssinSuoritus2015'

export const isPreIBLukionModuuliMuissaOpinnoissa2019 = (
  a: any
): a is PreIBLukionModuuliMuissaOpinnoissa2019 =>
  isLukionMuuModuuliMuissaOpinnoissa2019(a) ||
  isLukionVieraanKielenModuuliMuissaOpinnoissa2019(a)

export const isPreIBLukionModuulinSuoritusMuissaOpinnoissa2019 = (
  a: any
): a is PreIBLukionModuulinSuoritusMuissaOpinnoissa2019 =>
  a?.$class === 'PreIBLukionModuulinSuoritusMuissaOpinnoissa2019'

export const isPreIBLukionModuulinSuoritusOppiaineissa2019 = (
  a: any
): a is PreIBLukionModuulinSuoritusOppiaineissa2019 =>
  a?.$class === 'PreIBLukionModuulinSuoritusOppiaineissa2019'

export const isPreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 =
  (
    a: any
  ): a is PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 =>
    isPreIBLukionModuulinSuoritusMuissaOpinnoissa2019(a) ||
    isPreIBLukionPaikallisenOpintojaksonSuoritus2019(a)

export const isPreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 =
  (
    a: any
  ): a is PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 =>
    isPreIBLukionModuulinSuoritusOppiaineissa2019(a) ||
    isPreIBLukionPaikallisenOpintojaksonSuoritus2019(a)

export const isPreIBLukionModuuliOppiaineissa2019 = (
  a: any
): a is PreIBLukionModuuliOppiaineissa2019 =>
  isLukionMuuModuuliOppiaineissa2019(a) ||
  isLukionVieraanKielenModuuliOppiaineissa2019(a)

export const isPreIBLukionOppiaine2019 = (
  a: any
): a is PreIBLukionOppiaine2019 =>
  isLukionMatematiikka2019(a) ||
  isLukionMuuValtakunnallinenOppiaine2019(a) ||
  isLukionUskonto2019(a) ||
  isLukionÄidinkieliJaKirjallisuus2019(a) ||
  isPaikallinenLukionOppiaine2019(a) ||
  isVierasTaiToinenKotimainenKieli2019(a)

export const isPreIBLukionPaikallisenOpintojaksonSuoritus2019 = (
  a: any
): a is PreIBLukionPaikallisenOpintojaksonSuoritus2019 =>
  a?.$class === 'PreIBLukionPaikallisenOpintojaksonSuoritus2019'

export const isPreIBMuutSuorituksetTaiVastaavat2019 = (
  a: any
): a is PreIBMuutSuorituksetTaiVastaavat2019 =>
  isLukiodiplomit2019(a) ||
  isMuutLukionSuoritukset2019(a) ||
  isTemaattisetOpinnot2019(a)

export const isPreIBOppiaine2015 = (a: any): a is PreIBOppiaine2015 =>
  isIBOppiaineLanguage(a) ||
  isIBOppiaineMuu(a) ||
  isLukionMatematiikka2015(a) ||
  isLukionMuuValtakunnallinenOppiaine2015(a) ||
  isLukionUskonto2015(a) ||
  isLukionÄidinkieliJaKirjallisuus2015(a) ||
  isPaikallinenLukionOppiaine2015(a) ||
  isVierasTaiToinenKotimainenKieli2015(a)

export const isPreIBOppiaineenSuoritus2015 = (
  a: any
): a is PreIBOppiaineenSuoritus2015 =>
  a?.$class === 'PreIBOppiaineenSuoritus2015'

export const isPreIBPaikallinenOpintojakso2019 = (
  a: any
): a is PreIBPaikallinenOpintojakso2019 => isLukionPaikallinenOpintojakso2019(a)

export const isPreIBSuorituksenOsasuoritus2015 = (
  a: any
): a is PreIBSuorituksenOsasuoritus2015 =>
  isMuidenLukioOpintojenSuoritus2015(a) || isPreIBOppiaineenSuoritus2015(a)

export const isPreIBSuorituksenOsasuoritus2019 = (
  a: any
): a is PreIBSuorituksenOsasuoritus2019 =>
  isLukionOppiaineenPreIBSuoritus2019(a) ||
  isMuidenLukioOpintojenPreIBSuoritus2019(a)

export const isPreIBSuoritus2015 = (a: any): a is PreIBSuoritus2015 =>
  a?.$class === 'PreIBSuoritus2015'

export const isPreIBSuoritus2019 = (a: any): a is PreIBSuoritus2019 =>
  a?.$class === 'PreIBSuoritus2019'

export const isPrimaryAlaoppimisalue = (a: any): a is PrimaryAlaoppimisalue =>
  a?.$class === 'PrimaryAlaoppimisalue'

export const isPrimaryAlaoppimisalueArviointi = (
  a: any
): a is PrimaryAlaoppimisalueArviointi =>
  a?.$class === 'PrimaryAlaoppimisalueArviointi'

export const isPrimaryLapsiAlaoppimisalue = (
  a: any
): a is PrimaryLapsiAlaoppimisalue => a?.$class === 'PrimaryLapsiAlaoppimisalue'

export const isPrimaryLapsiOppimisalue = (
  a: any
): a is PrimaryLapsiOppimisalue => a?.$class === 'PrimaryLapsiOppimisalue'

export const isPrimaryLapsiOppimisalueenAlaosasuoritus = (
  a: any
): a is PrimaryLapsiOppimisalueenAlaosasuoritus =>
  a?.$class === 'PrimaryLapsiOppimisalueenAlaosasuoritus'

export const isPrimaryLapsiOppimisalueenSuoritus = (
  a: any
): a is PrimaryLapsiOppimisalueenSuoritus =>
  a?.$class === 'PrimaryLapsiOppimisalueenSuoritus'

export const isPrimaryLuokkaAste = (a: any): a is PrimaryLuokkaAste =>
  a?.$class === 'PrimaryLuokkaAste'

export const isPrimaryOppimisalueenAlaosasuoritus = (
  a: any
): a is PrimaryOppimisalueenAlaosasuoritus =>
  a?.$class === 'PrimaryOppimisalueenAlaosasuoritus'

export const isPrimaryOppimisalueenSuoritus = (
  a: any
): a is PrimaryOppimisalueenSuoritus =>
  a?.$class === 'PrimaryOppimisalueenSuoritus'

export const isPrimaryOsasuoritus = (a: any): a is PrimaryOsasuoritus =>
  isPrimaryLapsiOppimisalueenSuoritus(a) || isPrimaryOppimisalueenSuoritus(a)

export const isPrimarySuorituskielenVaativaOppimisalue = (
  a: any
): a is PrimarySuorituskielenVaativaOppimisalue =>
  isEuropeanSchoolOfHelsinkiKielioppiaine(a) ||
  isEuropeanSchoolOfHelsinkiKielioppiaineAncientGreek(a) ||
  isEuropeanSchoolOfHelsinkiKielioppiaineLatin(a) ||
  isEuropeanSchoolOfHelsinkiMuuOppiaine(a)

export const isPrimaryVuosiluokanSuoritus = (
  a: any
): a is PrimaryVuosiluokanSuoritus => a?.$class === 'PrimaryVuosiluokanSuoritus'

export const isPuhviKoe2019 = (a: any): a is PuhviKoe2019 =>
  a?.$class === 'PuhviKoe2019'

export const isPYPLuokkaAste = (a: any): a is PYPLuokkaAste =>
  a?.$class === 'PYPLuokkaAste'

export const isPYPOppiaine = (a: any): a is PYPOppiaine =>
  isLanguageAcquisition(a) || isLanguageAndLiterature(a) || isPYPOppiaineMuu(a)

export const isPYPOppiaineenSuoritus = (a: any): a is PYPOppiaineenSuoritus =>
  a?.$class === 'PYPOppiaineenSuoritus'

export const isPYPOppiaineMuu = (a: any): a is PYPOppiaineMuu =>
  a?.$class === 'PYPOppiaineMuu'

export const isPYPVuosiluokanSuoritus = (a: any): a is PYPVuosiluokanSuoritus =>
  a?.$class === 'PYPVuosiluokanSuoritus'

export const isPäivämäärävahvistus = (a: any): a is Päivämäärävahvistus =>
  a?.$class === 'Päivämäärävahvistus'

export const isS7OppiaineenAlaosasuoritus = (
  a: any
): a is S7OppiaineenAlaosasuoritus => a?.$class === 'S7OppiaineenAlaosasuoritus'

export const isS7OppiaineKomponentti = (a: any): a is S7OppiaineKomponentti =>
  a?.$class === 'S7OppiaineKomponentti'

export const isSanallinenInternationalSchoolOppiaineenArviointi = (
  a: any
): a is SanallinenInternationalSchoolOppiaineenArviointi =>
  a?.$class === 'SanallinenInternationalSchoolOppiaineenArviointi'

export const isSanallinenLukionArviointi = (
  a: any
): a is SanallinenLukionArviointi => a?.$class === 'SanallinenLukionArviointi'

export const isSanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 =
  (
    a: any
  ): a is SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019 =>
    a?.$class ===
    'SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019'

export const isSanallinenLukionOppiaineenArviointi2019 = (
  a: any
): a is SanallinenLukionOppiaineenArviointi2019 =>
  a?.$class === 'SanallinenLukionOppiaineenArviointi2019'

export const isSanallinenPerusopetuksenOppiaineenArviointi = (
  a: any
): a is SanallinenPerusopetuksenOppiaineenArviointi =>
  a?.$class === 'SanallinenPerusopetuksenOppiaineenArviointi'

export const isSanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi =
  (
    a: any
  ): a is SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi =>
    a?.$class ===
    'SanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi'

export const isSecondaryGradeArviointi = (
  a: any
): a is SecondaryGradeArviointi => a?.$class === 'SecondaryGradeArviointi'

export const isSecondaryLowerArviointi = (
  a: any
): a is SecondaryLowerArviointi =>
  isSecondaryGradeArviointi(a) || isSecondaryNumericalMarkArviointi(a)

export const isSecondaryLowerLuokkaAste = (
  a: any
): a is SecondaryLowerLuokkaAste => a?.$class === 'SecondaryLowerLuokkaAste'

export const isSecondaryLowerOppiaineenSuoritus = (
  a: any
): a is SecondaryLowerOppiaineenSuoritus =>
  a?.$class === 'SecondaryLowerOppiaineenSuoritus'

export const isSecondaryLowerVuosiluokanSuoritus = (
  a: any
): a is SecondaryLowerVuosiluokanSuoritus =>
  a?.$class === 'SecondaryLowerVuosiluokanSuoritus'

export const isSecondaryNumericalMarkArviointi = (
  a: any
): a is SecondaryNumericalMarkArviointi =>
  a?.$class === 'SecondaryNumericalMarkArviointi'

export const isSecondaryOppiaine = (a: any): a is SecondaryOppiaine =>
  isEuropeanSchoolOfHelsinkiKielioppiaine(a) ||
  isEuropeanSchoolOfHelsinkiKielioppiaineAncientGreek(a) ||
  isEuropeanSchoolOfHelsinkiKielioppiaineLatin(a) ||
  isEuropeanSchoolOfHelsinkiMuuOppiaine(a)

export const isSecondaryS7PreliminaryMarkArviointi = (
  a: any
): a is SecondaryS7PreliminaryMarkArviointi =>
  a?.$class === 'SecondaryS7PreliminaryMarkArviointi'

export const isSecondaryUpperLuokkaAste = (
  a: any
): a is SecondaryUpperLuokkaAste => a?.$class === 'SecondaryUpperLuokkaAste'

export const isSecondaryUpperOppiaineenSuoritus = (
  a: any
): a is SecondaryUpperOppiaineenSuoritus =>
  isSecondaryUpperOppiaineenSuoritusS6(a) ||
  isSecondaryUpperOppiaineenSuoritusS7(a)

export const isSecondaryUpperOppiaineenSuoritusS6 = (
  a: any
): a is SecondaryUpperOppiaineenSuoritusS6 =>
  a?.$class === 'SecondaryUpperOppiaineenSuoritusS6'

export const isSecondaryUpperOppiaineenSuoritusS7 = (
  a: any
): a is SecondaryUpperOppiaineenSuoritusS7 =>
  a?.$class === 'SecondaryUpperOppiaineenSuoritusS7'

export const isSecondaryUpperVuosiluokanSuoritus = (
  a: any
): a is SecondaryUpperVuosiluokanSuoritus =>
  a?.$class === 'SecondaryUpperVuosiluokanSuoritus'

export const isSisältäväOpiskeluoikeus = (
  a: any
): a is SisältäväOpiskeluoikeus => a?.$class === 'SisältäväOpiskeluoikeus'

export const isSuoritus = (a: any): a is Suoritus =>
  isAikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus(a) ||
  isAikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus(a) ||
  isAikuistenPerusopetuksenAlkuvaiheenSuoritus(a) ||
  isAikuistenPerusopetuksenKurssinSuoritus(a) ||
  isAikuistenPerusopetuksenOppiaineenOppimääränSuoritus(a) ||
  isAikuistenPerusopetuksenOppiaineenSuoritus(a) ||
  isAikuistenPerusopetuksenOppimääränSuoritus(a) ||
  isAmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(a) ||
  isAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
    a
  ) ||
  isAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(a) ||
  isAmmatillisenTutkinnonOsittainenSuoritus(a) ||
  isAmmatillisenTutkinnonSuoritus(a) ||
  isDIAOppiaineenTutkintovaiheenOsasuorituksenSuoritus(a) ||
  isDIAOppiaineenTutkintovaiheenSuoritus(a) ||
  isDIAOppiaineenValmistavanVaiheenLukukaudenSuoritus(a) ||
  isDIAOppiaineenValmistavanVaiheenSuoritus(a) ||
  isDIATutkinnonSuoritus(a) ||
  isDIAValmistavanVaiheenSuoritus(a) ||
  isDiplomaCoreRequirementsOppiaineenSuoritus(a) ||
  isDiplomaOppiaineenSuoritus(a) ||
  isDiplomaVuosiluokanSuoritus(a) ||
  isEBOppiaineenAlaosasuoritus(a) ||
  isEBTutkinnonOsasuoritus(a) ||
  isEBTutkinnonSuoritus(a) ||
  isEsiopetuksenSuoritus(a) ||
  isIBCASSuoritus(a) ||
  isIBExtendedEssaySuoritus(a) ||
  isIBKurssinSuoritus(a) ||
  isIBOppiaineenSuoritus(a) ||
  isIBTheoryOfKnowledgeSuoritus(a) ||
  isIBTutkinnonSuoritus(a) ||
  isKorkeakoulunOpintojaksonSuoritus(a) ||
  isKorkeakouluopintojenSuoritus(a) ||
  isKorkeakoulututkinnonSuoritus(a) ||
  isLukioOpintojenSuoritus(a) ||
  isLukionKurssinSuoritus2015(a) ||
  isLukionModuulinSuoritusMuissaOpinnoissa2019(a) ||
  isLukionModuulinSuoritusOppiaineissa2019(a) ||
  isLukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa(a) ||
  isLukionOppiaineenOpintojenSuoritusLukioonValmistavassaKoulutuksessa2019(a) ||
  isLukionOppiaineenOppimääränSuoritus2015(a) ||
  isLukionOppiaineenPreIBSuoritus2019(a) ||
  isLukionOppiaineenSuoritus2015(a) ||
  isLukionOppiaineenSuoritus2019(a) ||
  isLukionOppiaineidenOppimäärienSuoritus2019(a) ||
  isLukionOppimääränSuoritus2015(a) ||
  isLukionOppimääränSuoritus2019(a) ||
  isLukionPaikallisenOpintojaksonSuoritus2019(a) ||
  isLukioonValmistavanKoulutuksenOppiaineenSuoritus(a) ||
  isLukioonValmistavanKoulutuksenSuoritus(a) ||
  isLukioonValmistavanKurssinSuoritus(a) ||
  isMYPOppiaineenSuoritus(a) ||
  isMYPVuosiluokanSuoritus(a) ||
  isMuidenLukioOpintojenPreIBSuoritus2019(a) ||
  isMuidenLukioOpintojenSuoritus2015(a) ||
  isMuidenLukioOpintojenSuoritus2019(a) ||
  isMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(a) ||
  isMuuKorkeakoulunSuoritus(a) ||
  isMuuPerusopetuksenLisäopetuksenSuoritus(a) ||
  isMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
    a
  ) ||
  isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus(a) ||
  isMuunAmmatillisenKoulutuksenSuoritus(a) ||
  isMuunAmmatillisenTutkinnonOsanSuoritus(a) ||
  isMuunKuinSäännellynKoulutuksenOsasuoritus(a) ||
  isMuunKuinSäännellynKoulutuksenPäätasonSuoritus(a) ||
  isMuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(a) ||
  isNuortenPerusopetuksenOppiaineenOppimääränSuoritus(a) ||
  isNuortenPerusopetuksenOppiaineenSuoritus(a) ||
  isNuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa(a) ||
  isNuortenPerusopetuksenOppimääränSuoritus(a) ||
  isNurseryVuosiluokanSuoritus(a) ||
  isNäyttötutkintoonValmistavanKoulutuksenOsanSuoritus(a) ||
  isNäyttötutkintoonValmistavanKoulutuksenSuoritus(a) ||
  isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus(
    a
  ) ||
  isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022(
    a
  ) ||
  isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(a) ||
  isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
    a
  ) ||
  isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
    a
  ) ||
  isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
    a
  ) ||
  isOsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(
    a
  ) ||
  isOsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(a) ||
  isPYPOppiaineenSuoritus(a) ||
  isPYPVuosiluokanSuoritus(a) ||
  isPerusopetukseenValmistavanOpetuksenOppiaineenSuoritus(a) ||
  isPerusopetukseenValmistavanOpetuksenSuoritus(a) ||
  isPerusopetuksenLisäopetuksenOppiaineenSuoritus(a) ||
  isPerusopetuksenLisäopetuksenSuoritus(a) ||
  isPerusopetuksenLisäopetuksenToiminta_AlueenSuoritus(a) ||
  isPerusopetuksenToiminta_AlueenSuoritus(a) ||
  isPerusopetuksenVuosiluokanSuoritus(a) ||
  isPreIBKurssinSuoritus2015(a) ||
  isPreIBLukionModuulinSuoritusMuissaOpinnoissa2019(a) ||
  isPreIBLukionModuulinSuoritusOppiaineissa2019(a) ||
  isPreIBLukionPaikallisenOpintojaksonSuoritus2019(a) ||
  isPreIBOppiaineenSuoritus2015(a) ||
  isPreIBSuoritus2015(a) ||
  isPreIBSuoritus2019(a) ||
  isPrimaryLapsiOppimisalueenAlaosasuoritus(a) ||
  isPrimaryLapsiOppimisalueenSuoritus(a) ||
  isPrimaryOppimisalueenAlaosasuoritus(a) ||
  isPrimaryOppimisalueenSuoritus(a) ||
  isPrimaryVuosiluokanSuoritus(a) ||
  isS7OppiaineenAlaosasuoritus(a) ||
  isSecondaryLowerOppiaineenSuoritus(a) ||
  isSecondaryLowerVuosiluokanSuoritus(a) ||
  isSecondaryUpperOppiaineenSuoritusS6(a) ||
  isSecondaryUpperOppiaineenSuoritusS7(a) ||
  isSecondaryUpperVuosiluokanSuoritus(a) ||
  isTelmaKoulutuksenOsanSuoritus(a) ||
  isTelmaKoulutuksenSuoritus(a) ||
  isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(a) ||
  isTutkinnonOsaaPienemmänKokonaisuudenSuoritus(a) ||
  isTutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus(a) ||
  isTutkintokoulutukseenValmentavanKoulutuksenSuoritus(a) ||
  isTutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus(
    a
  ) ||
  isTutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus(a) ||
  isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus(a) ||
  isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022(a) ||
  isVSTKotoutumiskoulutuksenOhjauksenSuoritus2022(a) ||
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(a) ||
  isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus(a) ||
  isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022(a) ||
  isVSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus(a) ||
  isValmaKoulutuksenOsanSuoritus(a) ||
  isValmaKoulutuksenSuoritus(a) ||
  isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(a) ||
  isVapaanSivistystyönJotpaKoulutuksenSuoritus(a) ||
  isVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus(a) ||
  isVapaanSivistystyönLukutaitokoulutuksenSuoritus(a) ||
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus(
    a
  ) ||
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus(
    a
  ) ||
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot(
    a
  ) ||
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
    a
  ) ||
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso(
    a
  ) ||
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus(
    a
  ) ||
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
    a
  ) ||
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(a) ||
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(a) ||
  isYhteisenAmmatillisenTutkinnonOsanSuoritus(a) ||
  isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(a) ||
  isYhteisenTutkinnonOsanOsaAlueenSuoritus(a) ||
  isYlioppilastutkinnonKokeenSuoritus(a) ||
  isYlioppilastutkinnonSuoritus(a)

export const isSuullisenKielitaidonKoe2019 = (
  a: any
): a is SuullisenKielitaidonKoe2019 =>
  a?.$class === 'SuullisenKielitaidonKoe2019'

export const isTehostetunTuenPäätös = (a: any): a is TehostetunTuenPäätös =>
  a?.$class === 'TehostetunTuenPäätös'

export const isTelmaJaValmaArviointi = (a: any): a is TelmaJaValmaArviointi =>
  a?.$class === 'TelmaJaValmaArviointi'

export const isTelmaKoulutuksenOsa = (a: any): a is TelmaKoulutuksenOsa =>
  isMuuValtakunnallinenTutkinnonOsa(a) ||
  isPaikallinenTelmaKoulutuksenOsa(a) ||
  isYhteinenTutkinnonOsa(a)

export const isTelmaKoulutuksenOsanSuoritus = (
  a: any
): a is TelmaKoulutuksenOsanSuoritus =>
  a?.$class === 'TelmaKoulutuksenOsanSuoritus'

export const isTelmaKoulutuksenSuoritus = (
  a: any
): a is TelmaKoulutuksenSuoritus => a?.$class === 'TelmaKoulutuksenSuoritus'

export const isTelmaKoulutus = (a: any): a is TelmaKoulutus =>
  a?.$class === 'TelmaKoulutus'

export const isTemaattisetOpinnot2019 = (a: any): a is TemaattisetOpinnot2019 =>
  a?.$class === 'TemaattisetOpinnot2019'

export const isToimipiste = (a: any): a is Toimipiste =>
  a?.$class === 'Toimipiste'

export const isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus = (
  a: any
): a is TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus =>
  a?.$class === 'TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus'

export const isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus =
  (
    a: any
  ): a is TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus =>
    isTutkinnonOsaaPienemmänKokonaisuudenSuoritus(a) ||
    isYhteisenTutkinnonOsanOsaAlueenSuoritus(a)

export const isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus = (
  a: any
): a is TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus =>
  a?.$class === 'TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus'

export const isTutkinnonOsaaPienemmänKokonaisuudenSuoritus = (
  a: any
): a is TutkinnonOsaaPienemmänKokonaisuudenSuoritus =>
  a?.$class === 'TutkinnonOsaaPienemmänKokonaisuudenSuoritus'

export const isTutkinnonOsaaPienempiKokonaisuus = (
  a: any
): a is TutkinnonOsaaPienempiKokonaisuus =>
  a?.$class === 'TutkinnonOsaaPienempiKokonaisuus'

export const isTutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus = (
  a: any
): a is TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus =>
  a?.$class === 'TutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus'

export const isTutkintokoulutukseenValmentavanKoulutuksenMuuOsa = (
  a: any
): a is TutkintokoulutukseenValmentavanKoulutuksenMuuOsa =>
  isTutkintokoulutukseenValmentavaPerustaitojenVahvistaminen(a) ||
  isTutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot(a) ||
  isTutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot(
    a
  ) ||
  isTutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot(a) ||
  isTutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot(a) ||
  isTutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen(
    a
  )

export const isTutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus = (
  a: any
): a is TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus =>
  isTutkintokoulutukseenValmentavaKoulutuksenMuunOsanSuoritus(a) ||
  isTutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus(a)

export const isTutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus = (
  a: any
): a is TutkintokoulutukseenValmentavanKoulutuksenPäätasonSuoritus =>
  isTutkintokoulutukseenValmentavanKoulutuksenSuoritus(a)

export const isTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi =>
    isSanallinenTutkintokoulutukseenValmentavanKoulutuksenSuorituksenArviointi(
      a
    )

export const isTutkintokoulutukseenValmentavanKoulutuksenSuoritus = (
  a: any
): a is TutkintokoulutukseenValmentavanKoulutuksenSuoritus =>
  a?.$class === 'TutkintokoulutukseenValmentavanKoulutuksenSuoritus'

export const isTutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa =>
    a?.$class ===
    'TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa'

export const isTutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus =>
    a?.$class ===
    'TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuorituksenSuoritus'

export const isTutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus =>
    a?.$class ===
    'TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosanOsasuoritus'

export const isTutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus =>
    a?.$class ===
    'TutkintokoulutukseenValmentavanKoulutuksenValinnaisenOsanSuoritus'

export const isTutkintokoulutukseenValmentavanKoulutus = (
  a: any
): a is TutkintokoulutukseenValmentavanKoulutus =>
  a?.$class === 'TutkintokoulutukseenValmentavanKoulutus'

export const isTutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot =>
    a?.$class ===
    'TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot'

export const isTutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot = (
  a: any
): a is TutkintokoulutukseenValmentavanOpiskeluoikeudenLisätiedot =>
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot(
    a
  ) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot(
    a
  ) ||
  isTutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot(
    a
  )

export const isTutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot =>
    a?.$class ===
    'TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot'

export const isTutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot =>
    a?.$class ===
    'TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot'

export const isTutkintokoulutukseenValmentavanOpiskeluoikeudenTila = (
  a: any
): a is TutkintokoulutukseenValmentavanOpiskeluoikeudenTila =>
  a?.$class === 'TutkintokoulutukseenValmentavanOpiskeluoikeudenTila'

export const isTutkintokoulutukseenValmentavanOpiskeluoikeus = (
  a: any
): a is TutkintokoulutukseenValmentavanOpiskeluoikeus =>
  a?.$class === 'TutkintokoulutukseenValmentavanOpiskeluoikeus'

export const isTutkintokoulutukseenValmentavanOpiskeluoikeusjakso = (
  a: any
): a is TutkintokoulutukseenValmentavanOpiskeluoikeusjakso =>
  a?.$class === 'TutkintokoulutukseenValmentavanOpiskeluoikeusjakso'

export const isTutkintokoulutukseenValmentavaPerustaitojenVahvistaminen = (
  a: any
): a is TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen =>
  a?.$class === 'TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen'

export const isTutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot = (
  a: any
): a is TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot =>
  a?.$class === 'TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot'

export const isTutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot =>
    a?.$class ===
    'TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot'

export const isTutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot = (
  a: any
): a is TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot =>
  a?.$class === 'TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot'

export const isTutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot = (
  a: any
): a is TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot =>
  a?.$class === 'TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot'

export const isTutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen =
  (
    a: any
  ): a is TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen =>
    a?.$class ===
    'TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen'

export const isTutkintotoimikunta = (a: any): a is Tutkintotoimikunta =>
  a?.$class === 'Tutkintotoimikunta'

export const isTuvaErityisenTuenPäätös = (
  a: any
): a is TuvaErityisenTuenPäätös => a?.$class === 'TuvaErityisenTuenPäätös'

export const isTyössäoppimisjakso = (a: any): a is Työssäoppimisjakso =>
  a?.$class === 'Työssäoppimisjakso'

export const isTäydellisetHenkilötiedot = (
  a: any
): a is TäydellisetHenkilötiedot => a?.$class === 'TäydellisetHenkilötiedot'

export const isUlkomaanjakso = (a: any): a is Ulkomaanjakso =>
  a?.$class === 'Ulkomaanjakso'

export const isUusiHenkilö = (a: any): a is UusiHenkilö =>
  a?.$class === 'UusiHenkilö'

export const isVahvistus = (a: any): a is Vahvistus =>
  isHenkilövahvistusPaikkakunnalla(a) ||
  isHenkilövahvistusValinnaisellaPaikkakunnalla(a) ||
  isHenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla(a) ||
  isOrganisaatiovahvistus(a) ||
  isPäivämäärävahvistus(a)

export const isValmaKoulutuksenOsa = (a: any): a is ValmaKoulutuksenOsa =>
  isMuuValtakunnallinenTutkinnonOsa(a) ||
  isPaikallinenValmaKoulutuksenOsa(a) ||
  isYhteinenTutkinnonOsa(a)

export const isValmaKoulutuksenOsanSuoritus = (
  a: any
): a is ValmaKoulutuksenOsanSuoritus =>
  a?.$class === 'ValmaKoulutuksenOsanSuoritus'

export const isValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus = (
  a: any
): a is ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus =>
  isValmaKoulutuksenOsanSuoritus(a) ||
  isYhteisenTutkinnonOsanOsaAlueenSuoritus(a)

export const isValmaKoulutuksenSuoritus = (
  a: any
): a is ValmaKoulutuksenSuoritus => a?.$class === 'ValmaKoulutuksenSuoritus'

export const isValmaKoulutus = (a: any): a is ValmaKoulutus =>
  a?.$class === 'ValmaKoulutus'

export const isValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017 = (
  a: any
): a is ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017 =>
  a?.$class === 'ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017'

export const isValtakunnallinenAikuistenPerusopetuksenKurssi2015 = (
  a: any
): a is ValtakunnallinenAikuistenPerusopetuksenKurssi2015 =>
  a?.$class === 'ValtakunnallinenAikuistenPerusopetuksenKurssi2015'

export const isValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017 =
  (
    a: any
  ): a is ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017 =>
    a?.$class ===
    'ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017'

export const isValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue = (
  a: any
): a is ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue =>
  a?.$class === 'ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue'

export const isValtakunnallinenLukionKurssi2015 = (
  a: any
): a is ValtakunnallinenLukionKurssi2015 =>
  a?.$class === 'ValtakunnallinenLukionKurssi2015'

export const isValtakunnallinenLukioonValmistavanKoulutuksenKurssi = (
  a: any
): a is ValtakunnallinenLukioonValmistavanKoulutuksenKurssi =>
  a?.$class === 'ValtakunnallinenLukioonValmistavanKoulutuksenKurssi'

export const isVapaanSivistystyöJotpaKoulutuksenArviointi = (
  a: any
): a is VapaanSivistystyöJotpaKoulutuksenArviointi =>
  a?.$class === 'VapaanSivistystyöJotpaKoulutuksenArviointi'

export const isVapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso = (
  a: any
): a is VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso =>
  a?.$class === 'VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso'

export const isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus = (
  a: any
): a is VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus =>
  a?.$class === 'VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus'

export const isVapaanSivistystyönJotpaKoulutuksenOsasuoritus = (
  a: any
): a is VapaanSivistystyönJotpaKoulutuksenOsasuoritus =>
  a?.$class === 'VapaanSivistystyönJotpaKoulutuksenOsasuoritus'

export const isVapaanSivistystyönJotpaKoulutuksenSuoritus = (
  a: any
): a is VapaanSivistystyönJotpaKoulutuksenSuoritus =>
  a?.$class === 'VapaanSivistystyönJotpaKoulutuksenSuoritus'

export const isVapaanSivistystyönJotpaKoulutus = (
  a: any
): a is VapaanSivistystyönJotpaKoulutus =>
  a?.$class === 'VapaanSivistystyönJotpaKoulutus'

export const isVapaanSivistystyönLukutaidonKokonaisuus = (
  a: any
): a is VapaanSivistystyönLukutaidonKokonaisuus =>
  a?.$class === 'VapaanSivistystyönLukutaidonKokonaisuus'

export const isVapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus = (
  a: any
): a is VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus =>
  a?.$class === 'VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus'

export const isVapaanSivistystyönLukutaitokoulutuksenSuoritus = (
  a: any
): a is VapaanSivistystyönLukutaitokoulutuksenSuoritus =>
  a?.$class === 'VapaanSivistystyönLukutaitokoulutuksenSuoritus'

export const isVapaanSivistystyönLukutaitokoulutus = (
  a: any
): a is VapaanSivistystyönLukutaitokoulutus =>
  a?.$class === 'VapaanSivistystyönLukutaitokoulutus'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKokonaisuudenSuoritus =>
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus(
      a
    ) ||
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus(
      a
    ) ||
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
      a
    ) ||
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
      a
    )

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus =>
    a?.$class ===
    'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'

export const isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus = (
  a: any
): a is VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus =>
  a?.$class === 'VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus'

export const isVapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus =
  (
    a: any
  ): a is VapaanSivistystyönMaahanmuuttajienKuntoutuskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenOsasuoritus =>
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot(
      a
    ) ||
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso(
      a
    )

export const isVapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen = (
  a: any
): a is VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen =>
  a?.$class === 'VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen'

export const isVapaanSivistystyönOpintokokonaisuudenSuoritus = (
  a: any
): a is VapaanSivistystyönOpintokokonaisuudenSuoritus =>
  isMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
    a
  ) ||
  isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(a)

export const isVapaanSivistystyönOpiskeluoikeudenLisätiedot = (
  a: any
): a is VapaanSivistystyönOpiskeluoikeudenLisätiedot =>
  a?.$class === 'VapaanSivistystyönOpiskeluoikeudenLisätiedot'

export const isVapaanSivistystyönOpiskeluoikeudenTila = (
  a: any
): a is VapaanSivistystyönOpiskeluoikeudenTila =>
  a?.$class === 'VapaanSivistystyönOpiskeluoikeudenTila'

export const isVapaanSivistystyönOpiskeluoikeus = (
  a: any
): a is VapaanSivistystyönOpiskeluoikeus =>
  a?.$class === 'VapaanSivistystyönOpiskeluoikeus'

export const isVapaanSivistystyönOpiskeluoikeusjakso = (
  a: any
): a is VapaanSivistystyönOpiskeluoikeusjakso =>
  isOppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(a) ||
  isVapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(a) ||
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso(a)

export const isVapaanSivistystyönPäätasonSuoritus = (
  a: any
): a is VapaanSivistystyönPäätasonSuoritus =>
  isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus(
    a
  ) ||
  isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022(
    a
  ) ||
  isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(a) ||
  isVapaanSivistystyönJotpaKoulutuksenSuoritus(a) ||
  isVapaanSivistystyönLukutaitokoulutuksenSuoritus(a) ||
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(a)

export const isVapaanSivistystyönVapaatavoitteinenKoulutus = (
  a: any
): a is VapaanSivistystyönVapaatavoitteinenKoulutus =>
  a?.$class === 'VapaanSivistystyönVapaatavoitteinenKoulutus'

export const isVapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso =
  (
    a: any
  ): a is VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso =>
    a?.$class ===
    'VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'

export const isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus =
  (
    a: any
  ): a is VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus =>
    a?.$class ===
    'VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'

export const isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus = (
  a: any
): a is VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus =>
  a?.$class === 'VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus'

export const isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus = (
  a: any
): a is VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus =>
  a?.$class === 'VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus'

export const isVapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi = (
  a: any
): a is VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi =>
  a?.$class === 'VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi'

export const isVierasTaiToinenKotimainenKieli2015 = (
  a: any
): a is VierasTaiToinenKotimainenKieli2015 =>
  a?.$class === 'VierasTaiToinenKotimainenKieli2015'

export const isVierasTaiToinenKotimainenKieli2019 = (
  a: any
): a is VierasTaiToinenKotimainenKieli2019 =>
  a?.$class === 'VierasTaiToinenKotimainenKieli2019'

export const isVirtaVirhe = (a: any): a is VirtaVirhe =>
  isDuplikaatti(a) || isOpiskeluoikeusAvaintaEiLöydy(a)

export const isVSTKehittyvänKielenTaitotasonArviointi = (
  a: any
): a is VSTKehittyvänKielenTaitotasonArviointi =>
  a?.$class === 'VSTKehittyvänKielenTaitotasonArviointi'

export const isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi = (
  a: any
): a is VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi =>
  a?.$class === 'VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi'

export const isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus = (
  a: any
): a is VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus =>
  a?.$class === 'VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus'

export const isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 = (
  a: any
): a is VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 =>
  a?.$class === 'VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022'

export const isVSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli = (
  a: any
): a is VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli =>
  a?.$class === 'VSTKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli'

export const isVSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022 = (
  a: any
): a is VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022 =>
  isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022(a) ||
  isVSTKotoutumiskoulutuksenOhjauksenSuoritus2022(a) ||
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(a) ||
  isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022(a)

export const isVSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 = (
  a: any
): a is VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022 =>
  a?.$class === 'VSTKotoutumiskoulutuksenOhjauksenKoulutusmoduuli2022'

export const isVSTKotoutumiskoulutuksenOhjauksenSuoritus2022 = (
  a: any
): a is VSTKotoutumiskoulutuksenOhjauksenSuoritus2022 =>
  a?.$class === 'VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'

export const isVSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 = (
  a: any
): a is VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022 =>
  a?.$class === 'VSTKotoutumiskoulutuksenOsasuorituksenArviointi2022'

export const isVSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 =
  (
    a: any
  ): a is VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 =>
    a?.$class ===
    'VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022'

export const isVSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 =
  (
    a: any
  ): a is VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022 =>
    a?.$class ===
    'VSTKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli2022'

export const isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 = (
  a: any
): a is VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 =>
  a?.$class === 'VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'

export const isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =
  (
    a: any
  ): a is VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus =>
    a?.$class ===
    'VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenAlaosasuoritus'

export const isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 =
  (
    a: any
  ): a is VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022 =>
    a?.$class ===
    'VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenKoulutusmoduuli2022'

export const isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 =
  (
    a: any
  ): a is VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022 =>
    a?.$class ===
    'VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022'

export const isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022 =
  (
    a: any
  ): a is VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022 =>
    a?.$class ===
    'VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaamisenAlasuorituksenKoulutusmoduuli2022'

export const isVSTKotoutumiskoulutus2022 = (
  a: any
): a is VSTKotoutumiskoulutus2022 => a?.$class === 'VSTKotoutumiskoulutus2022'

export const isVSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus = (
  a: any
): a is VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus =>
  a?.$class === 'VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus'

export const isYhteinenTutkinnonOsa = (a: any): a is YhteinenTutkinnonOsa =>
  a?.$class === 'YhteinenTutkinnonOsa'

export const isYhteisenAmmatillisenTutkinnonOsanSuoritus = (
  a: any
): a is YhteisenAmmatillisenTutkinnonOsanSuoritus =>
  a?.$class === 'YhteisenAmmatillisenTutkinnonOsanSuoritus'

export const isYhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus = (
  a: any
): a is YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus =>
  a?.$class === 'YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus'

export const isYhteisenTutkinnonOsanOsaAlueenSuoritus = (
  a: any
): a is YhteisenTutkinnonOsanOsaAlueenSuoritus =>
  a?.$class === 'YhteisenTutkinnonOsanOsaAlueenSuoritus'

export const isYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus =
  (
    a: any
  ): a is YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus =>
    isLukioOpintojenSuoritus(a) ||
    isMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(a) ||
    isYhteisenTutkinnonOsanOsaAlueenSuoritus(a)

export const isYlioppilaskokeenArviointi = (
  a: any
): a is YlioppilaskokeenArviointi => a?.$class === 'YlioppilaskokeenArviointi'

export const isYlioppilasTutkinnonKoe = (a: any): a is YlioppilasTutkinnonKoe =>
  a?.$class === 'YlioppilasTutkinnonKoe'

export const isYlioppilastutkinnonKokeenSuoritus = (
  a: any
): a is YlioppilastutkinnonKokeenSuoritus =>
  a?.$class === 'YlioppilastutkinnonKokeenSuoritus'

export const isYlioppilastutkinnonOpiskeluoikeudenTila = (
  a: any
): a is YlioppilastutkinnonOpiskeluoikeudenTila =>
  a?.$class === 'YlioppilastutkinnonOpiskeluoikeudenTila'

export const isYlioppilastutkinnonOpiskeluoikeus = (
  a: any
): a is YlioppilastutkinnonOpiskeluoikeus =>
  a?.$class === 'YlioppilastutkinnonOpiskeluoikeus'

export const isYlioppilastutkinnonSuoritus = (
  a: any
): a is YlioppilastutkinnonSuoritus =>
  a?.$class === 'YlioppilastutkinnonSuoritus'

export const isYlioppilastutkinnonTutkintokerta = (
  a: any
): a is YlioppilastutkinnonTutkintokerta =>
  a?.$class === 'YlioppilastutkinnonTutkintokerta'

export const isYlioppilastutkinto = (a: any): a is Ylioppilastutkinto =>
  a?.$class === 'Ylioppilastutkinto'

export const isYritys = (a: any): a is Yritys => a?.$class === 'Yritys'
