import { AhvenanmaanPerusopetuksenMuuOppiaine } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenMuuOppiaine'
import { AhvenanmaanPerusopetuksenOppiaineenSuoritus } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOppiaineenSuoritus'
import { AhvenanmaanPerusopetuksenVierasKieli } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenVierasKieli'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'

// Esitäytettävien oppiaineiden mallit luokka-asteittain. Vrt. manner-Suomen
// `perusopetus-v2/luokkaAsteenOppiaineet.ts`, joka taas on portattu backendin
// `NuortenPerusopetusPakollisetOppiaineet`-logiikasta. Ahvenanmaalla ei ole
// vastaavaa backend-lähdettä, joten mallit on määritelty tässä.
//
// Lähde: Ålands landskapsregeringin vahvistamat betygsformulär (ÅLR2021/9529,
// fastställt 26.11.2024 / avgångsbetyg 16.04.2025), tiedosto "Bilaga 1,
// Uppdaterade betygsformulär". Jokaisen luokka-asteen "Gemensamma ämnen"
// -listaus on poimittu suoraan lomakkeista. Tuntijako on Ahvenanmaalla
// koulukohtainen, joten nämä ovat muokattavia oletuksia, ei sitovia listoja.
//
// Oppiaineisto muuttuu lomakkeiden mukaan luokka-asteittain:
//  - åk 1–2: integroitu OM (omgivningskunskap), käsityö yhtenä aineena (KS),
//    ei vieraita kieliä; arviointi annetaan sanallisena omdöme.
//  - åk 3:   A1-kieli (engelska) alkaa; käsityö jakautuu tekstiili- (TX) ja
//    tekniseen työhön (TN). Ei vielä historiaa.
//  - åk 4:   historia (HI) alkaa.
//  - åk 5–6: OM jakautuu (BI, GE, FYKE = fysik & kemi, TE = hälsokunskap);
//    yhteiskuntaoppi (SA) alkaa.
//  - åk 7–9: fysiikka (FY) ja kemia (KE) eriytyvät; kotitalous (HEKO) alkaa.
//
// Kuten manner-Suomessa, 9. luokka palauttaa tyhjän listan: päättövuoden
// arvosanat kirjataan päättötodistukselle (avgångsbetyg, sama aineisto kuin
// åk 7–9), ei vuosiluokan suoritukselle. Jos oppilas jää luokalle
// (jääLuokalle), 9. luokan oma läsårsbetyg täytetään käsin.
//
// Valinnaiset aineet (valbara ämnen / tillvalsämnen) sekä valinnaiset kielet
// (A2 åk 5–, B1 åk 7–, B2 åk 8–) eivät kuulu esitäyttöön, vaan lisätään käsin.

// Oppiainekoodit koodistosta ahvenanmaankoskioppiaineetyleissivistava.
type MuuOppiaineKoodi =
  | 'SV'
  | 'SVA'
  | 'MA'
  | 'OM'
  | 'BI'
  | 'GE'
  | 'FYKE'
  | 'FY'
  | 'KE'
  | 'TE'
  | 'RELI'
  | 'HI'
  | 'MU'
  | 'SA'
  | 'KU'
  | 'KS'
  | 'TX'
  | 'TN'
  | 'ID'
  | 'HEKO'
  | 'EH'

type VierasKieliKoodi = 'A1' | 'A2' | 'B1' | 'B2'

const muu = (koodiarvo: MuuOppiaineKoodi, perusteenDiaarinumero?: string) =>
  AhvenanmaanPerusopetuksenOppiaineenSuoritus({
    koulutusmoduuli: AhvenanmaanPerusopetuksenMuuOppiaine({
      pakollinen: true,
      perusteenDiaarinumero,
      tunniste: Koodistokoodiviite({
        koodiarvo,
        koodistoUri: 'ahvenanmaankoskioppiaineetyleissivistava'
      })
    })
  })

const vierasKieli = (
  koodiarvo: VierasKieliKoodi,
  kieliKoodiarvo: string,
  perusteenDiaarinumero?: string
) =>
  AhvenanmaanPerusopetuksenOppiaineenSuoritus({
    koulutusmoduuli: AhvenanmaanPerusopetuksenVierasKieli({
      pakollinen: true,
      perusteenDiaarinumero,
      tunniste: Koodistokoodiviite({
        koodiarvo,
        koodistoUri: 'ahvenanmaankoskioppiaineetyleissivistava'
      }),
      kieli: Koodistokoodiviite({
        koodiarvo: kieliKoodiarvo,
        koodistoUri: 'kielivalikoima'
      })
    })
  })

/**
 * Palauttaa esitäytettävät oppiaineiden suoritukset annetulle luokka-asteelle.
 * Aineiston järjestys vastaa betygsformulärin "Gemensamma ämnen" -listausta.
 * 9. luokka ja tuntematon luokka-aste palauttavat tyhjän listan.
 */
export const ahvenanmaanLuokkaAsteenOppiaineet = (
  luokkaAste: string,
  perusteenDiaarinumero?: string
): AhvenanmaanPerusopetuksenOppiaineenSuoritus[] => {
  const n = parseInt(luokkaAste, 10)
  const m = (koodiarvo: MuuOppiaineKoodi) =>
    muu(koodiarvo, perusteenDiaarinumero)
  // A1 = engelska, pakollinen kieli åk 3 alkaen ("Engelska (A1)").
  const engelska = () => vierasKieli('A1', 'EN', perusteenDiaarinumero)

  // Åk 1–2 (Omdöme): käsityö yhtenä aineena (KS), ei vieraita kieliä.
  if (n >= 1 && n <= 2) {
    return [
      m('SV'),
      m('MA'),
      m('OM'),
      m('RELI'),
      m('MU'),
      m('KU'),
      m('KS'),
      m('ID')
    ]
  }

  // Åk 3: A1-kieli alkaa, käsityö jakautuu (TX, TN), ei vielä historiaa.
  if (n === 3) {
    return [
      m('SV'),
      m('MA'),
      m('OM'),
      m('RELI'),
      m('MU'),
      m('KU'),
      m('TX'),
      m('TN'),
      m('ID'),
      engelska()
    ]
  }

  // Åk 4: historia (HI) alkaa.
  if (n === 4) {
    return [
      m('SV'),
      m('MA'),
      m('OM'),
      m('RELI'),
      m('HI'),
      m('MU'),
      m('KU'),
      m('TX'),
      m('TN'),
      m('ID'),
      engelska()
    ]
  }

  // Åk 5–6: OM jakautuu (BI, GE, FYKE, TE), yhteiskuntaoppi (SA) alkaa.
  if (n >= 5 && n <= 6) {
    return [
      m('SV'),
      m('MA'),
      m('BI'),
      m('GE'),
      m('FYKE'),
      m('TE'),
      m('RELI'),
      m('HI'),
      m('SA'),
      m('MU'),
      m('KU'),
      m('TX'),
      m('TN'),
      m('ID'),
      engelska()
    ]
  }

  // Åk 7–8: FY ja KE eriytyvät, kotitalous (HEKO) alkaa. 9. luokka jää
  // tyhjäksi (arvosanat avgångsbetygiin, ks. moduulin kommentti).
  if (n >= 7 && n <= 8) {
    return [
      m('SV'),
      m('MA'),
      m('BI'),
      m('GE'),
      m('FY'),
      m('KE'),
      m('TE'),
      m('RELI'),
      m('HI'),
      m('SA'),
      m('MU'),
      m('KU'),
      m('TX'),
      m('TN'),
      m('ID'),
      m('HEKO'),
      engelska()
    ]
  }

  return []
}
