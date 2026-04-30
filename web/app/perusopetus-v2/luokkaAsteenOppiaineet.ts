import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { MuuNuortenPerusopetuksenOppiaine } from '../types/fi/oph/koski/schema/MuuNuortenPerusopetuksenOppiaine'
import { NuortenPerusopetuksenOppiaineenSuoritus } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenOppiaineenSuoritus'
import { NuortenPerusopetuksenÄidinkieliJaKirjallisuus } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenAidinkieliJaKirjallisuus'
import { NuortenPerusopetuksenUskonto } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenUskonto'
import { NuortenPerusopetuksenVierasTaiToinenKotimainenKieli } from '../types/fi/oph/koski/schema/NuortenPerusopetuksenVierasTaiToinenKotimainenKieli'

type MuuOppiaineKoodiarvo =
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

const muu = (koodiarvo: MuuOppiaineKoodiarvo, perusteenDiaarinumero?: string) =>
  NuortenPerusopetuksenOppiaineenSuoritus({
    painotettuOpetus: false,
    koulutusmoduuli: MuuNuortenPerusopetuksenOppiaine({
      pakollinen: true,
      perusteenDiaarinumero,
      tunniste: Koodistokoodiviite({
        koodiarvo,
        koodistoUri: 'koskioppiaineetyleissivistava'
      })
    })
  })

const äidinkieli = (perusteenDiaarinumero?: string) =>
  NuortenPerusopetuksenOppiaineenSuoritus({
    painotettuOpetus: false,
    koulutusmoduuli: NuortenPerusopetuksenÄidinkieliJaKirjallisuus({
      pakollinen: true,
      perusteenDiaarinumero,
      kieli: Koodistokoodiviite({
        koodiarvo: 'AI1',
        koodistoUri: 'oppiaineaidinkielijakirjallisuus'
      })
    })
  })

const uskonto = (perusteenDiaarinumero?: string) =>
  NuortenPerusopetuksenOppiaineenSuoritus({
    painotettuOpetus: false,
    koulutusmoduuli: NuortenPerusopetuksenUskonto({
      pakollinen: true,
      perusteenDiaarinumero
    })
  })

const vierasKieli = (
  koodiarvo: 'A1' | 'A2' | 'B1' | 'B2' | 'B3' | 'AOM',
  kieliKoodiarvo: string,
  perusteenDiaarinumero?: string
) =>
  NuortenPerusopetuksenOppiaineenSuoritus({
    painotettuOpetus: false,
    koulutusmoduuli: NuortenPerusopetuksenVierasTaiToinenKotimainenKieli({
      pakollinen: true,
      perusteenDiaarinumero,
      tunniste: Koodistokoodiviite({
        koodiarvo,
        koodistoUri: 'koskioppiaineetyleissivistava'
      }),
      kieli: Koodistokoodiviite({
        koodiarvo: kieliKoodiarvo,
        koodistoUri: 'kielivalikoima'
      })
    })
  })

/**
 * Palauttaa esitäytettävät oppiaineiden suoritukset annetulle luokka-asteelle.
 * Portattu backendin `NuortenPerusopetusPakollisetOppiaineet`-logiikasta.
 *
 * Luokka 9 palauttaa tyhjän listan (oppiaineet kirjataan päättötodistukselle).
 */
export const luokkaAsteenOppiaineet = (
  luokkaAste: string,
  perusteenDiaarinumero?: string
): NuortenPerusopetuksenOppiaineenSuoritus[] => {
  const n = parseInt(luokkaAste, 10)
  if (n >= 1 && n <= 2) {
    return [
      äidinkieli(perusteenDiaarinumero),
      muu('MA', perusteenDiaarinumero),
      muu('YL', perusteenDiaarinumero),
      uskonto(perusteenDiaarinumero),
      muu('MU', perusteenDiaarinumero),
      muu('KU', perusteenDiaarinumero),
      muu('KS', perusteenDiaarinumero),
      muu('LI', perusteenDiaarinumero),
      muu('OP', perusteenDiaarinumero)
    ]
  }
  if (n >= 3 && n <= 6) {
    return [
      äidinkieli(perusteenDiaarinumero),
      vierasKieli('A1', 'EN', perusteenDiaarinumero),
      muu('MA', perusteenDiaarinumero),
      muu('YL', perusteenDiaarinumero),
      uskonto(perusteenDiaarinumero),
      muu('HI', perusteenDiaarinumero),
      muu('YH', perusteenDiaarinumero),
      muu('MU', perusteenDiaarinumero),
      muu('KU', perusteenDiaarinumero),
      muu('KS', perusteenDiaarinumero),
      muu('LI', perusteenDiaarinumero),
      muu('OP', perusteenDiaarinumero)
    ]
  }
  if (n >= 7 && n <= 8) {
    return [
      äidinkieli(perusteenDiaarinumero),
      vierasKieli('A1', 'EN', perusteenDiaarinumero),
      vierasKieli('B1', 'SV', perusteenDiaarinumero),
      muu('MA', perusteenDiaarinumero),
      muu('BI', perusteenDiaarinumero),
      muu('GE', perusteenDiaarinumero),
      muu('FY', perusteenDiaarinumero),
      muu('KE', perusteenDiaarinumero),
      muu('TE', perusteenDiaarinumero),
      uskonto(perusteenDiaarinumero),
      muu('HI', perusteenDiaarinumero),
      muu('YH', perusteenDiaarinumero),
      muu('MU', perusteenDiaarinumero),
      muu('KU', perusteenDiaarinumero),
      muu('KS', perusteenDiaarinumero),
      muu('LI', perusteenDiaarinumero),
      muu('KO', perusteenDiaarinumero),
      muu('OP', perusteenDiaarinumero)
    ]
  }
  return []
}

/**
 * Palauttaa sen luokka-asteen koodiarvot 1-9, joille ei vielä löydy
 * seuraavalle luokalle siirrettävää vuosiluokan suoritusta.
 */
export const puuttuvatLuokkaAsteet = (
  olemassaOlevatLuokkaAsteet: string[]
): string[] => {
  const existing = new Set(olemassaOlevatLuokkaAsteet)
  return ['1', '2', '3', '4', '5', '6', '7', '8', '9'].filter(
    (k) => !existing.has(k)
  )
}
