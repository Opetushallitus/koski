import { FormOptic } from '../components-v2/forms/FormModel'
import { ValidationRule } from '../components-v2/forms/ValidationRule'
import { ValidationError } from '../components-v2/forms/validator'
import { parsePath } from '../util/optics'
import {
  PerusopetuksenOpiskeluoikeus,
  isPerusopetuksenOpiskeluoikeus
} from '../types/fi/oph/koski/schema/PerusopetuksenOpiskeluoikeus'

/**
 * Vahvistetun päätason suorituksen osasuorituksilta vaaditaan arviointi.
 *
 * Vastaa palvelimen validaatiota (KoskiValidator.validateValmiinSuorituksenStatus:
 * "Valmiiksi merkityllä suorituksella ... on keskeneräinen osasuoritus ..."),
 * mutta kohdistuu suoraan puuttuvaan arvosanakenttään, jotta käyttäjä näkee
 * punaisen huomautuksen jo ennen tallennusyritystä. Vanhassa käyttöliittymässä
 * sama sääntö on SuoritusEditor.validateModelissa.
 *
 * Huom: ValidationRule ajetaan vain kerran datan juuressa (ks. validator.ts:
 * validate ei laskeudu ValidationRulen kanssa puuhun), joten sääntö kävelee
 * suoritukset itse ja muodostaa virhepolut samassa muodossa kuin parsePath.
 */
export const arviointiVaaditaanVahvistetultaSuoritukselta: ValidationRule<PerusopetuksenOpiskeluoikeus> =
  {
    type: 'ValidationRule',
    isMatch: (
      data: any,
      path: string[]
    ): data is PerusopetuksenOpiskeluoikeus =>
      path.length === 0 && isPerusopetuksenOpiskeluoikeus(data),
    validate: (
      opiskeluoikeus: PerusopetuksenOpiskeluoikeus
    ): ValidationError[] =>
      opiskeluoikeus.suoritukset.flatMap((suoritus, suoritusIndex) =>
        suoritus.vahvistus === undefined
          ? []
          : osasuoritukset(suoritus).flatMap((osasuoritus, osasuoritusIndex) =>
              arviointiPuuttuu(osasuoritus)
                ? [
                    {
                      type: 'arviointiPuuttuuValmiiltaSuoritukselta' as const,
                      path: arviointiPath(suoritusIndex, osasuoritusIndex)
                    }
                  ]
                : []
            )
      )
  }

type Osasuoritus = { arviointi?: Array<unknown> }

// Kaikilla perusopetuksen päätason suorituksilla ei ole osasuorituksia
// (esim. oppiaineen oppimäärän suoritus).
const osasuoritukset = (suoritus: object): Osasuoritus[] =>
  (suoritus as { osasuoritukset?: Osasuoritus[] }).osasuoritukset || []

const arviointiPuuttuu = (osasuoritus: Osasuoritus): boolean =>
  (osasuoritus.arviointi || []).length === 0

/**
 * Virhepolku samassa muodossa kuin util/optics.ts:n parsePath tuottaa, jotta
 * useFormErrors löytää virheen kentälle. Ks. arviointiErrorPath.
 */
const arviointiPath = (suoritusIndex: number, osasuoritusIndex: number) =>
  `suoritukset.${suoritusIndex}.osasuoritukset.${osasuoritusIndex}.arviointi`

/**
 * Arvosanakentän virhepolku. parsePath muodostaa polun etsimällä arvon
 * identiteetin datasta, joten se palauttaa undefined kun arviointi puuttuu —
 * eli juuri silloin kun virhe pitäisi näyttää. Polku muodostetaan siksi
 * osasuorituksen (joka on olemassa) polusta.
 */
export const arviointiErrorPath = <T extends object>(
  osasuoritusPath: FormOptic<PerusopetuksenOpiskeluoikeus, T>,
  state: PerusopetuksenOpiskeluoikeus
): string | undefined => {
  const path = parsePath(osasuoritusPath, state)
  return path && `${path}.arviointi`
}
