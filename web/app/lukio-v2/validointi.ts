import { pathPatternValidationRule } from '../components-v2/forms/ValidationRule'
import { pathToString } from '../components-v2/forms/validator'
import { isLukionOppiaineenSuoritus2019 } from '../types/fi/oph/koski/schema/LukionOppiaineenSuoritus2019'

type LukionPäätasonSuoritus = {
  vahvistus?: unknown
  osasuoritukset?: unknown[]
}

export const lukionOppiaineenArviointiVaaditaanVahvistetultaSuoritukselta =
  pathPatternValidationRule<LukionPäätasonSuoritus>(
    'suoritukset',
    '*'
  )((suoritus, path) =>
    suoritus.vahvistus === undefined
      ? []
      : (suoritus.osasuoritukset || []).flatMap((osasuoritus, index) =>
          isLukionOppiaineenSuoritus2019(osasuoritus) &&
          (osasuoritus.arviointi || []).length === 0
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
