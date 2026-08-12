import { pathPatternValidationRule } from '../forms/ValidationRule'
import { pathToString } from '../forms/validator'

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
 *
 * Sääntö annetaan useForm:lle editorikohtaisesti eikä oletuksena kaikille, koska
 * palvelin ei sovella samaa sääntöä kaikkiin opiskeluoikeuksiin. Ennen kuin lisäät
 * säännön uuteen editoriin, tarkista että:
 *
 *  - palvelin ei vapauta suoritustyyppiä (validatePäätasonSuorituksenStatus ohittaa
 *    ammatillisen tutkinnon osittaisen suorituksen, VST:n ja ylioppilastutkinnon),
 *  - osasuoritukset ovat arvioitavia (Arvioinniton- ja MahdollisestiArvioinniton-
 *    suoritukset eivät ole "kesken" ilman arviointia, joten sääntö varoittaisi
 *    niistä turhaan),
 *  - osasuoritukset eivät sisällä alaosasuorituksia. Palvelin tarkistaa
 *    rekursiivisetOsasuoritukset, tämä sääntö vain päätason suorituksen suorat
 *    osasuoritukset.
 *
 * Käytössä: perusopetus (PerusopetusEditor), Ahvenanmaan perusopetus
 * (AhvenanmaanPerusopetusEditor).
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

// Kaikilla päätason suorituksilla ei ole osasuorituksia
// (esim. perusopetuksen oppiaineen oppimäärän suoritus).
const osasuoritukset = (suoritus: PäätasonSuoritus): Osasuoritus[] =>
  suoritus.osasuoritukset || []

const arviointiPuuttuu = (osasuoritus: Osasuoritus): boolean =>
  (osasuoritus.arviointi || []).length === 0
