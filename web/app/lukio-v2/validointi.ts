import { pathPatternValidationRule } from '../components-v2/forms/ValidationRule'
import { pathToString } from '../components-v2/forms/validator'
import { isLukionOppiaineenSuoritus2019 } from '../types/fi/oph/koski/schema/LukionOppiaineenSuoritus2019'

type LukionPäätasonSuoritus = {
  vahvistus?: unknown
  osasuoritukset?: unknown[]
}

const puuttuuArviointi = (s: { arviointi?: unknown[] }): boolean =>
  (s.arviointi || []).length === 0

// Vanha käyttöliittymä (onKeskeneräisiäOsasuorituksia) vaatii arvioinnin sekä
// oppiaineelta että jokaiselta sen kurssilta/opintojaksolta rekursiivisesti -
// pelkkä oppiaineen oma arviointi ei riitä.
export const lukionOppiaineenArviointiVaaditaanVahvistetultaSuoritukselta =
  pathPatternValidationRule<LukionPäätasonSuoritus>(
    'suoritukset',
    '*'
  )((suoritus, path) =>
    suoritus.vahvistus === undefined
      ? []
      : (suoritus.osasuoritukset || []).flatMap((osasuoritus, index) => {
          if (!isLukionOppiaineenSuoritus2019(osasuoritus)) return []
          const oppiainePath = [...path, 'osasuoritukset', index.toString()]
          const oppiaineenVirhe = puuttuuArviointi(osasuoritus)
            ? [
                {
                  type: 'arviointiPuuttuuValmiiltaSuoritukselta' as const,
                  path: pathToString([...oppiainePath, 'arviointi'])
                }
              ]
            : []
          const kurssienVirheet = (osasuoritus.osasuoritukset || []).flatMap(
            (kurssi, kurssiIndex) =>
              puuttuuArviointi(kurssi)
                ? [
                    {
                      type: 'arviointiPuuttuuValmiiltaSuoritukselta' as const,
                      path: pathToString([
                        ...oppiainePath,
                        'osasuoritukset',
                        kurssiIndex.toString(),
                        'arviointi'
                      ])
                    }
                  ]
                : []
          )
          return [...oppiaineenVirhe, ...kurssienVirheet]
        })
  )
