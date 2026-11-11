import {
  AikajaksoraportinKyselynParametrit,
  aikuistenPerusopetuksenKurssikertymaKyselymalli,
  aikuistenPerusopetuksenOppijamaaratRaporttiKyselymalli,
  aikuistenPerusopetusRaporttityypit,
  aikuistenPerusopetusSuoritustiedotKyselymalli,
  ammatillinenOpiskelijavuositiedotKyselymalli,
  ammatillinenOsittainenSuoritustiedotKyselymalli,
  ammatillinenTutkintoSuoritustiedotKyselymalli,
  esiopetuksenOppijamaaratRaporttiKyselymalli,
  esiopetusKyselymalli,
  ibRaporttityypit,
  ibSuoritustiedotKyselymalli,
  kotikuntalaskelmaKyselymalli,
  kunnanOppijatKyselymalli,
  lukio2019OpintopistekertymatKyselymalli,
  lukio2019SuoritustiedotKyselymalli,
  lukioDiaIbInternationalESHOpiskelijamaaratKyselymalli,
  lukioKurssikertymatKyselymalli,
  lukionSuoritustiedotKyselymalli,
  luvaOpiskelijamaaratKyselymalli,
  MassaluovutusKyselymalli,
  muuAmmatillinenKyselymalli,
  muuKuinSaanneltyKoulutusKyselymalli,
  paallekkaisetOpiskeluoikeudetKyselymalli,
  perusopetukseenValmistavaKyselymalli,
  perusopetuksenLisaopetuksenOppijamaaratRaporttiKyselymalli,
  perusopetuksenOppijamaaratRaporttiKyselymalli,
  perusopetuksenVuosiluokkaKyselymalli,
  PäiväraportinKyselynParametrit,
  Raporttityypit,
  topksAmmatillinenKyselymalli,
  tuvaPerusopetuksenOppijamaaratRaporttiKyselymalli,
  tuvaSuoritustiedotKyselymalli,
  TyyppivalintaraportinKyselynParametrit,
  VuosiluokkaraportinKyselynParametrit,
  vstJotpaKyselymalli,
  vstKopsKyselymalli,
  vstOsaamismerkitKyselymalli,
  vstVapaatavoitteinenKyselymalli
} from '../components-v2/raportit/massaluovutusRaporttiKyselyt'
import { Raporttikategoria } from './raporttivalinta'

export type Esimerkki = { kappaleet: string } | { teksti: string }

export type Kotikunta = { ohje: string }

type LomakkeenTekstit = {
  kuvaus: string
  esimerkki?: Esimerkki
}

export type PäiväraportinLomake = LomakkeenTekstit & {
  tyyppi: 'päivä'
  kyselymalli: MassaluovutusKyselymalli<PäiväraportinKyselynParametrit>
  päivänOhje?: string
  kotikunta?: Kotikunta
}

export type VuosiluokkaraportinLomake = LomakkeenTekstit & {
  tyyppi: 'vuosiluokka'
  kyselymalli: MassaluovutusKyselymalli<VuosiluokkaraportinKyselynParametrit>
  päivänOhje?: string
  kotikunta?: Kotikunta
}

export type AikajaksoraportinLomake = LomakkeenTekstit & {
  tyyppi: 'aikajakso'
  kyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit>
  ohje?: string
}

export type Osasuoritukset = 'tutkinnon osat' | 'kurssisuoritukset' | 'opinnot'

export type AikarajattavanAikajaksoraportinLomake = LomakkeenTekstit & {
  tyyppi: 'aikarajattava aikajakso'
  kyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit>
  osasuoritukset: Osasuoritukset
  ilmanAikarajausta?: boolean
  kotikunta?: Kotikunta
}

export type TyyppivalinnallisenAikajaksoraportinLomake = LomakkeenTekstit & {
  tyyppi: 'tyyppivalinnallinen aikajakso'
  kyselymalli: MassaluovutusKyselymalli<
    TyyppivalintaraportinKyselynParametrit<any>
  >
  raporttityypit: Raporttityypit
  oletustyyppi: string
  tyyppivalinnanOtsikko: string
  aikarajauksenOtsikko: string
  kaikkiOsasuoritukset: string
  aikajaksonOsasuoritukset: string
  kotikunta?: Kotikunta
}

export type Raporttilomake =
  | PäiväraportinLomake
  | VuosiluokkaraportinLomake
  | AikajaksoraportinLomake
  | AikarajattavanAikajaksoraportinLomake
  | TyyppivalinnallisenAikajaksoraportinLomake

export type Raportti = {
  id: string
  name: string
  visibleForAllOrgs?: boolean
  lomake: Raporttilomake
}

const lukionSuoritustiedot = (
  kyselymalli: MassaluovutusKyselymalli<AikajaksoraportinKyselynParametrit>
): Raportti => ({
  id: 'lukionsuoritustietojentarkistus',
  name: 'raportti-tab-lukionsuoritustietojentarkistus',
  lomake: {
    tyyppi: 'aikarajattava aikajakso',
    kuvaus: 'Lukioraportti-short-description-massaluovutus',
    esimerkki: { kappaleet: 'Lukioraportti-example' },
    osasuoritukset: 'kurssisuoritukset',
    kotikunta: { ohje: 'Lukioraportti-kotikunta-date-input-help' },
    kyselymalli
  }
})

const lukiokoulutuksenOpiskelijamäärät: Raportti = {
  id: 'lukiodiaibinternationaleshopiskelijamaarat',
  name: 'raportti-tab-lukiodiaibinternationalopiskelijamaarat',
  lomake: {
    tyyppi: 'päivä',
    kuvaus: 'lukiokoulutuksen-opiskelijamaarat-short-description-massaluovutus',
    päivänOhje: 'lukiokoulutuksen-opiskelijamaarat-date-input-help',
    esimerkki: { kappaleet: 'lukiokoulutuksen-opiskelijamaarat-example' },
    kyselymalli: lukioDiaIbInternationalESHOpiskelijamaaratKyselymalli
  }
}

const luvaOpiskelijamäärät: Raportti = {
  id: 'luvaopiskelijamaarat',
  name: 'raportti-tab-luvaopiskelijamaarat',
  lomake: {
    tyyppi: 'päivä',
    kuvaus: 'luva-opiskelijamaarat-short-description-massaluovutus',
    päivänOhje: 'luva-opiskelijamaarat-date-input-help',
    esimerkki: { kappaleet: 'luva-opiskelijamaarat-example' },
    kyselymalli: luvaOpiskelijamaaratKyselymalli
  }
}

export const kaikkiRaportitKategorioittain: Raporttikategoria<Raportti>[] = [
  {
    id: 'esiopetus',
    tab: 'raporttikategoria-tab-esiopetus',
    heading: 'raporttikategoria-heading-esiopetus',
    raportit: [
      {
        id: 'esiopetuksenraportti',
        name: 'raportti-tab-esiopetuksenraportti',
        lomake: {
          tyyppi: 'päivä',
          kuvaus: 'esiopetusraportti-short-description-massaluovutus',
          päivänOhje: 'esiopetusraportti-date-input-help',
          kotikunta: { ohje: 'esiopetusraportti-kotikunta-date-input-help' },
          esimerkki: { kappaleet: 'esiopetusraportti-example' },
          kyselymalli: esiopetusKyselymalli
        }
      },
      {
        id: 'esiopetuksenoppijamäärienraportti',
        name: 'raportti-tab-esiopetuksenoppijamäärienraportti',
        lomake: {
          tyyppi: 'päivä',
          kuvaus:
            'Esiopetus-oppilasmäärät-raportti-short-description-massaluovutus',
          päivänOhje: 'Esiopetus-oppilasmäärät-raportti-date-input-help',
          esimerkki: { kappaleet: 'Esiopetus-oppilasmäärät-raportti-example' },
          kyselymalli: esiopetuksenOppijamaaratRaporttiKyselymalli
        }
      }
    ]
  },
  {
    id: 'perusopetus',
    tab: 'raporttikategoria-tab-perusopetus',
    heading: 'raporttikategoria-heading-perusopetus',
    raportit: [
      {
        id: 'perusopetuksenvuosiluokka',
        name: 'raportti-tab-perusopetuksenvuosiluokka',
        lomake: {
          tyyppi: 'vuosiluokka',
          kuvaus: 'PerusopetuksenVuosiluokka-short-description-massaluovutus',
          päivänOhje: 'PerusopetuksenVuosiluokka-date-input-help',
          kotikunta: {
            ohje: 'PerusopetuksenVuosiluokka-kotikunta-date-input-help'
          },
          esimerkki: { kappaleet: 'PerusopetuksenVuosiluokka-example' },
          kyselymalli: perusopetuksenVuosiluokkaKyselymalli
        }
      },
      {
        id: 'perusopetuksenoppijamääräraportti',
        name: 'raportti-tab-perusopetuksenoppijamääräraportti',
        lomake: {
          tyyppi: 'päivä',
          kuvaus:
            'Perusopetus-oppijamäärät-raportti-short-description-massaluovutus',
          päivänOhje: 'Perusopetus-oppijamäärät-raportti-date-input-help',
          esimerkki: { kappaleet: 'Perusopetus-oppijamäärät-raportti-example' },
          kyselymalli: perusopetuksenOppijamaaratRaporttiKyselymalli
        }
      },
      {
        id: 'perusopetuksenlisäopetuksenoppijamääräraportti',
        name: 'raportti-tab-perusopetuksenlisäopetuksenoppijamääräraportti',
        lomake: {
          tyyppi: 'päivä',
          kuvaus:
            'Perusopetus-lisäopetus-oppijamäärät-raportti-short-description-massaluovutus',
          päivänOhje:
            'Perusopetus-lisäopetus-oppijamäärät-raportti-date-input-help',
          esimerkki: {
            kappaleet: 'Perusopetus-lisäopetus-oppijamäärät-raportti-example'
          },
          kyselymalli:
            perusopetuksenLisaopetuksenOppijamaaratRaporttiKyselymalli
        }
      }
    ]
  },
  {
    id: 'perusopetukseenvalmistava',
    tab: 'raporttikategoria-tab-perusopetukseenvalmistava',
    heading: 'raporttikategoria-heading-perusopetukseenvalmistava',
    raportit: [
      {
        id: 'perusopetukseenvalmistavanopetuksentarkistus',
        name: 'raportti-tab-perusopetukseenvalmistavantarkistusraportti',
        lomake: {
          tyyppi: 'aikarajattava aikajakso',
          kuvaus:
            'Perusopetukseen-valmistava-raportti-short-description-massaluovutus',
          esimerkki: {
            kappaleet: 'Perusopetukseen-valmistava-raportti-example'
          },
          osasuoritukset: 'opinnot',
          ilmanAikarajausta: true,
          kyselymalli: perusopetukseenValmistavaKyselymalli
        }
      }
    ]
  },
  {
    id: 'tuva',
    tab: 'raporttikategoria-tab-tuva',
    heading: 'raporttikategoria-heading-tuva',
    raportit: [
      {
        id: 'tuvasuoritustietojentarkistus',
        name: 'raportti-tab-tuvasuoritustiedotraportti',
        lomake: {
          tyyppi: 'aikajakso',
          kuvaus:
            'tuva-suoritustiedot-raportti-short-description-massaluovutus',
          esimerkki: { kappaleet: 'tuva-suoritustiedot-raportti-example' },
          kyselymalli: tuvaSuoritustiedotKyselymalli
        }
      },
      {
        id: 'tuvaperusopetuksenoppijamääräraportti',
        name: 'raportti-tab-tuvaperusopetuksenoppijamäärätraportti',
        lomake: {
          tyyppi: 'päivä',
          kuvaus:
            'Tuva-perusopetus-oppijamäärät-raportti-short-description-massaluovutus',
          päivänOhje: 'Tuva-perusopetus-oppijamäärät-raportti-date-input-help',
          esimerkki: {
            kappaleet: 'Tuva-perusopetus-oppijamäärät-raportti-example'
          },
          kyselymalli: tuvaPerusopetuksenOppijamaaratRaporttiKyselymalli
        }
      }
    ]
  },
  {
    id: 'aikuisten-perusopetus',
    tab: 'raporttikategoria-tab-aikuisten-perusopetus',
    heading: 'raporttikategoria-heading-aikuisten-perusopetus',
    raportit: [
      {
        id: 'aikuistenperusopetussuoritustietojentarkistus',
        name: 'raportti-tab-aikuistenperusopetussuoritustietojentarkistus',
        lomake: {
          tyyppi: 'tyyppivalinnallinen aikajakso',
          kuvaus:
            'aikuisten-perusopetus-raportti-short-description-massaluovutus',
          esimerkki: { kappaleet: 'aikuisten-perusopetus-raportti-example' },
          raporttityypit: aikuistenPerusopetusRaporttityypit,
          oletustyyppi: aikuistenPerusopetusRaporttityypit.alkuvaihe.key,
          tyyppivalinnanOtsikko: 'suorituksentyyppivalinta-help',
          aikarajauksenOtsikko:
            'aikuistenperusopetuksen-raportti-osasuoritusten-aikavaraus-help',
          kaikkiOsasuoritukset:
            'Raportille valitaan kaikki kurssisuoritukset riippumatta niiden suoritusajankohdasta',
          aikajaksonOsasuoritukset:
            'Raportille valitaan vain sellaiset kurssit, joiden arviointipäivä osuu yllä määritellylle aikajaksolle',
          kyselymalli: aikuistenPerusopetusSuoritustiedotKyselymalli
        }
      },
      {
        id: 'aikuistenperusopetusoppijamäärienraportti',
        name: 'raportti-tab-aikuistenperusopetusoppijamäärienraportti',
        lomake: {
          tyyppi: 'päivä',
          kuvaus:
            'Aikuisten-perusopetus-oppilasmäärät-raportti-short-description-massaluovutus',
          päivänOhje:
            'Aikuisten-perusopetus-oppilasmäärät-raportti-date-input-help',
          esimerkki: {
            kappaleet: 'Aikuisten-perusopetus-oppilasmäärät-raportti-example'
          },
          kyselymalli: aikuistenPerusopetuksenOppijamaaratRaporttiKyselymalli
        }
      },
      {
        id: 'aikuistenperusopetuskurssikertymänraportti',
        name: 'raportti-tab-aikuistenperusopetuskurssikertymänraportti',
        lomake: {
          tyyppi: 'aikajakso',
          kuvaus:
            'Aikuisten-perusopetus-kurssikertymä-raportti-short-description-massaluovutus',
          ohje: 'Aikuisten-perusopetus-kurssikertymä-raportti-date-input-help',
          esimerkki: {
            kappaleet: 'Aikuisten-perusopetus-kurssikertymä-raportti-example'
          },
          kyselymalli: aikuistenPerusopetuksenKurssikertymaKyselymalli
        }
      }
    ]
  },
  {
    id: 'ammatillinen-koulutus',
    tab: 'raporttikategoria-tab-ammatillinen-koulutus',
    heading: 'raporttikategoria-heading-ammatillinen-koulutus',
    raportit: [
      {
        id: 'ammatillinenopiskelijavuositiedot',
        name: 'raportti-tab-ammatillinenopiskelijavuositiedot',
        lomake: {
          tyyppi: 'aikajakso',
          kuvaus: 'AmmatillinenOpiskelijavuositiedot-description-massaluovutus',
          kyselymalli: ammatillinenOpiskelijavuositiedotKyselymalli
        }
      },
      {
        id: 'ammatillinentutkintosuoritustietojentarkistus',
        name: 'raportti-tab-ammatillinentutkintosuoritustietojentarkistus',
        lomake: {
          tyyppi: 'aikarajattava aikajakso',
          kuvaus: 'SuoritustietojenTarkistus-short-description-massaluovutus',
          esimerkki: { kappaleet: 'SuoritustietojenTarkistus-example' },
          osasuoritukset: 'tutkinnon osat',
          kyselymalli: ammatillinenTutkintoSuoritustiedotKyselymalli
        }
      },
      {
        id: 'ammatillinenosittainensuoritustietojentarkistus',
        name: 'raportti-tab-ammatillinenosittainensuoritustietojentarkistus',
        lomake: {
          tyyppi: 'aikarajattava aikajakso',
          kuvaus:
            'AmmatillinenOsittainenSuoritustietojenTarkistus-short-description-massaluovutus',
          esimerkki: {
            kappaleet: 'AmmatillinenOsittainenSuoritustietojenTarkistus-example'
          },
          osasuoritukset: 'tutkinnon osat',
          kyselymalli: ammatillinenOsittainenSuoritustiedotKyselymalli
        }
      },
      {
        id: 'muuammatillinenkoulutus',
        name: 'raportti-tab-muuammatillinenkoulutus',
        lomake: {
          tyyppi: 'aikajakso',
          kuvaus: 'muuammatillinenraportti-description-massaluovutus',
          kyselymalli: muuAmmatillinenKyselymalli
        }
      },
      {
        id: 'topksammatillinen',
        name: 'raportti-tab-topksammatillinen',
        lomake: {
          tyyppi: 'aikajakso',
          kuvaus: 'topksammatillinen-description-massaluovutus',
          kyselymalli: topksAmmatillinenKyselymalli
        }
      }
    ]
  },
  {
    id: 'lukio',
    tab: 'raporttikategoria-tab-lukio',
    heading: 'raporttikategoria-heading-lukio',
    raportit: [
      lukionSuoritustiedot(lukionSuoritustiedotKyselymalli),
      {
        id: 'lukiokurssikertyma',
        name: 'raportti-tab-lukiokurssikertyma',
        lomake: {
          tyyppi: 'aikajakso',
          kuvaus: 'lukion-kurssikertyma-short-description-massaluovutus',
          ohje: 'lukion-kurssikertyma-date-input-help',
          esimerkki: { kappaleet: 'lukion-kurssikertyma-example' },
          kyselymalli: lukioKurssikertymatKyselymalli
        }
      },
      lukiokoulutuksenOpiskelijamäärät,
      luvaOpiskelijamäärät
    ]
  },
  {
    id: 'lukio2019',
    tab: 'raporttikategoria-tab-lukio2019',
    heading: 'raporttikategoria-heading-lukio2019',
    raportit: [
      lukionSuoritustiedot(lukio2019SuoritustiedotKyselymalli),
      {
        id: 'lukioopintopistekertyma',
        name: 'raportti-tab-lukio2019opintopistekertyma',
        lomake: {
          tyyppi: 'aikajakso',
          kuvaus: 'lukion-opintopistekertyma-short-description-massaluovutus',
          ohje: 'lukion-opintopistekertyma-date-input-help',
          esimerkki: { kappaleet: 'lukion-opintopistekertyma-example' },
          kyselymalli: lukio2019OpintopistekertymatKyselymalli
        }
      },
      lukiokoulutuksenOpiskelijamäärät,
      luvaOpiskelijamäärät
    ]
  },
  {
    id: 'ib',
    tab: 'raporttikategoria-tab-ib',
    heading: 'raporttikategoria-heading-ib',
    raportit: [
      {
        id: 'ibsuoritustietojentarkistus',
        name: 'raportti-tab-ibsuoritustietojentarkistus',
        lomake: {
          tyyppi: 'tyyppivalinnallinen aikajakso',
          kuvaus: 'IBSuoritustiedotRaportti-short-description-massaluovutus',
          esimerkki: { kappaleet: 'IBSuoritustiedotRaportti-example' },
          raporttityypit: ibRaporttityypit,
          oletustyyppi: ibRaporttityypit.ib.key,
          tyyppivalinnanOtsikko: 'suorituksentyyppivalinta-ib-help',
          aikarajauksenOtsikko: 'ib-raportti-osasuoritusten-aikavaraus-help',
          kaikkiOsasuoritukset: 'ib-raportti-osasuoritusten-aikarajaus-ei',
          aikajaksonOsasuoritukset:
            'ib-raportti-osasuoritusten-aikarajaus-kyllä',
          kotikunta: { ohje: 'Lukioraportti-kotikunta-date-input-help' },
          kyselymalli: ibSuoritustiedotKyselymalli
        }
      }
    ]
  },
  {
    id: 'vst',
    tab: 'raporttikategoria-tab-vst',
    heading: 'raporttikategoria-heading-vst',
    raportit: [
      {
        id: 'jatkuvanoppimisenvapaansivistystyönraportti',
        name: 'raportti-tab-vstjotpa',
        lomake: {
          tyyppi: 'aikajakso',
          kuvaus: 'VstJotpaRaportti-short-description-massaluovutus',
          esimerkki: { kappaleet: 'VstJotpaRaportti-example' },
          kyselymalli: vstJotpaKyselymalli
        }
      },
      {
        id: 'vapaansivistystyönvapaatavoitteisenkoulutuksenraportti',
        name: 'raportti-tab-vstvapaatavoitteinen',
        lomake: {
          tyyppi: 'aikajakso',
          kuvaus: 'VstVapaatavoitteinenRaportti-short-description',
          esimerkki: { kappaleet: 'VstVapaatavoitteinenRaportti-example' },
          kyselymalli: vstVapaatavoitteinenKyselymalli
        }
      },
      {
        id: 'vapaansivistystyönoppivelvollisillesuunnatunkoulutuksenraportti',
        name: 'raportti-tab-vstkops',
        lomake: {
          tyyppi: 'aikajakso',
          kuvaus: 'VstKopsRaportti-short-description',
          esimerkki: { kappaleet: 'VstKopsRaportti-example' },
          kyselymalli: vstKopsKyselymalli
        }
      },
      {
        id: 'vapaansivistystyönosaamismerkkienraportti',
        name: 'raportti-tab-vstosaamismerkit',
        lomake: {
          tyyppi: 'aikajakso',
          kuvaus: 'VstOsaamismerkitRaportti-short-description',
          esimerkki: { kappaleet: 'VstOsaamismerkitRaportti-example' },
          kyselymalli: vstOsaamismerkitKyselymalli
        }
      }
    ]
  },
  {
    id: 'muks',
    tab: 'raporttikategoria-tab-muks',
    heading: 'raporttikategoria-heading-muks',
    raportit: [
      {
        id: 'muunkuinsäännellynkoulutuksenraportti',
        name: 'raporttikategoria-heading-muks',
        lomake: {
          tyyppi: 'aikajakso',
          kuvaus: 'MuksRaportti-short-description-massaluovutus',
          esimerkki: { kappaleet: 'MuksRaportti-example' },
          kyselymalli: muuKuinSaanneltyKoulutusKyselymalli
        }
      }
    ]
  },
  {
    id: 'muut',
    tab: 'raporttikategoria-tab-muut',
    heading: 'raporttikategoria-heading-muut',
    raportit: [
      {
        id: 'paallekkaisetopiskeluoikeudet',
        name: 'raportti-tab-paallekkaisetopiskeluoikeudet',
        visibleForAllOrgs: true,
        lomake: {
          tyyppi: 'aikajakso',
          kuvaus:
            'paallekkaiset-opiskeluoikeudet-short-description-massaluovutus',
          esimerkki: { teksti: 'paallekkaiset-opiskeluoikeudet-example' },
          kyselymalli: paallekkaisetOpiskeluoikeudetKyselymalli
        }
      },
      {
        id: 'kotikuntalaskelmaraportti',
        name: 'raportti-tab-kotikuntalaskelma',
        lomake: {
          tyyppi: 'päivä',
          kuvaus: 'kotikuntalaskelma-short-description-massaluovutus',
          päivänOhje: 'kotikuntalaskelma-date-input-help',
          esimerkki: { kappaleet: 'kotikuntalaskelma-example' },
          kyselymalli: kotikuntalaskelmaKyselymalli
        }
      },
      {
        id: 'kunnanoppijatraportti',
        name: 'raportti-tab-kunnanoppijat',
        lomake: {
          tyyppi: 'päivä',
          kuvaus: 'kunnanoppijat-short-description-massaluovutus',
          päivänOhje: 'kunnanoppijat-date-input-help',
          esimerkki: { kappaleet: 'kunnanoppijat-example' },
          kyselymalli: kunnanOppijatKyselymalli
        }
      }
    ]
  }
]
