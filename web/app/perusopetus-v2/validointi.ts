import { pathPatternValidationRule } from '../components-v2/forms/ValidationRule'
import { pathToString } from '../components-v2/forms/validator'

/**
 * Vahvistetun päätason suorituksen osasuorituksilta vaaditaan arviointi.
 *
 * Vastaa palvelimen validaatiota (KoskiValidator.validateValmiinSuorituksenStatus:
 * "Valmiiksi merkityllä suorituksella ... on keskeneräinen osasuoritus ..."),
 * mutta kohdistuu suoraan puuttuvaan arvosanakenttään, jotta käyttäjä näkee
 * punaisen huomautuksen jo ennen tallennusyritystä. Vanhassa käyttöliittymässä
 * sama sääntö on SuoritusEditor.validateModelissa.
 *
 * Sääntö kohdistuu päätason suoritukseen eikä suoraan osasuoritukseen, koska ehto
 * (onko päätason suoritus vahvistettu) luetaan osasuorituksen yläpuolelta.
 */
export const arviointiVaaditaanVahvistetultaSuoritukselta =
  pathPatternValidationRule<PäätasonSuoritus>(
    'suoritukset',
    '*'
  )((suoritus, path) =>
    suoritus.vahvistus === undefined
      ? []
      : osasuoritukset(suoritus).flatMap((osasuoritus, index) =>
          arviointiPuuttuu(osasuoritus)
            ? [
                {
                  type: 'arviointiPuuttuuValmiiltaSuoritukselta' as const,
                  path: pathToString([
                    ...path,
                    'osasuoritukset',
                    index.toString(),
                    'arviointi'
                  ])
                }
              ]
            : []
        )
  )

type PäätasonSuoritus = {
  vahvistus?: unknown
  osasuoritukset?: Osasuoritus[]
}

type Osasuoritus = { arviointi?: Array<unknown> }

// Kaikilla perusopetuksen päätason suorituksilla ei ole osasuorituksia
// (esim. oppiaineen oppimäärän suoritus).
const osasuoritukset = (suoritus: PäätasonSuoritus): Osasuoritus[] =>
  suoritus.osasuoritukset || []

const arviointiPuuttuu = (osasuoritus: Osasuoritus): boolean =>
  (osasuoritus.arviointi || []).length === 0
