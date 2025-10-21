import { LocalizedString } from './LocalizedString'
import { NäytönSuorituspaikka } from './NaytonSuorituspaikka'
import { NäytönSuoritusaika } from './NaytonSuoritusaika'
import { NäytönArviointi } from './NaytonArviointi'

/**
 * Tutkinnon tai koulutuksen osan suoritukseen kuuluvan ammattiosaamisen näytön tiedot.
 *
 * @see `fi.oph.koski.schema.Näyttö`
 */
export type Näyttö = {
  $class: 'fi.oph.koski.schema.Näyttö'
  kuvaus?: LocalizedString
  suorituspaikka?: NäytönSuorituspaikka
  suoritusaika?: NäytönSuoritusaika
  arviointi?: NäytönArviointi
}

export const Näyttö = (
  o: {
    kuvaus?: LocalizedString
    suorituspaikka?: NäytönSuorituspaikka
    suoritusaika?: NäytönSuoritusaika
    arviointi?: NäytönArviointi
  } = {}
): Näyttö => ({ $class: 'fi.oph.koski.schema.Näyttö', ...o })

Näyttö.className = 'fi.oph.koski.schema.Näyttö' as const

export const isNäyttö = (a: any): a is Näyttö =>
  a?.$class === 'fi.oph.koski.schema.Näyttö'
