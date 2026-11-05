import { pathPatternValidationRule } from '../components-v2/forms/ValidationRule'
import { pathToString } from '../components-v2/forms/validator'
import { isLukionOppiaineenSuoritus2019 } from '../types/fi/oph/koski/schema/LukionOppiaineenSuoritus2019'

type LukionPäätasonSuoritus = {
  vahvistus?: unknown
  osasuoritukset?: unknown[]
}

/**
 * Sama sääntö kuin components-v2/opiskeluoikeus/validointi.ts:n
 * arviointiVaaditaanVahvistetultaSuoritukselta, mutta rajattu vain
 * oppiaineisiin (LukionOppiaineenSuoritus2019).
 *
 * Jaettua sääntöä ei voi käyttää sellaisenaan: lukion suoritusten
 * osasuoritukset-taulukko sisältää oppiaineiden lisäksi myös lukiodiplomeja
 * ja muita opintoja (MuidenLukioOpintojenSuoritus2019), joilla ei ole
 * arviointi-kenttää lainkaan - jaettu sääntö tulkitsisi ne aina "arviointi
 * puuttuu" -virheeksi, vaikka ne eivät ole arvioitavia osasuorituksia
 * (vrt. jaetun säännön dokumentaation vaatimus "osasuoritukset ovat
 * arvioitavia", components-v2/opiskeluoikeus/validointi.ts:16-23).
 * Varmistettu livenä: jaettu sääntö esti tallennuksen pysyvästi testioppijalla
 * 1.2.246.562.24.00000000015, jolla on kolme tällaista osasuoritusta.
 */
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
