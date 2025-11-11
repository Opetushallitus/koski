import { LocalizedString } from './LocalizedString'
import { NäytönSuorituspaikka } from './NaytonSuorituspaikka'
import { NäytönSuoritusaika } from './NaytonSuoritusaika'
import { NäytönArviointi } from './NaytonArviointi'

/**
 * Tutkinnon tai koulutuksen osan suoritukseen kuuluvan ammattiosaamisen näytön tiedot.
 *
 * @see `fi.oph.koski.schema.NäyttöAmmatillinenOsittainen`
 */
export type NäyttöAmmatillinenOsittainen = {
  $class: 'fi.oph.koski.schema.NäyttöAmmatillinenOsittainen'
  kuvaus?: LocalizedString
  suorituspaikka?: NäytönSuorituspaikka
  suoritusaika?: NäytönSuoritusaika
  arviointi?: NäytönArviointi
}

export const NäyttöAmmatillinenOsittainen = (
  o: {
    kuvaus?: LocalizedString
    suorituspaikka?: NäytönSuorituspaikka
    suoritusaika?: NäytönSuoritusaika
    arviointi?: NäytönArviointi
  } = {}
): NäyttöAmmatillinenOsittainen => ({
  $class: 'fi.oph.koski.schema.NäyttöAmmatillinenOsittainen',
  ...o
})

NäyttöAmmatillinenOsittainen.className =
  'fi.oph.koski.schema.NäyttöAmmatillinenOsittainen' as const

export const isNäyttöAmmatillinenOsittainen = (
  a: any
): a is NäyttöAmmatillinenOsittainen =>
  a?.$class === 'fi.oph.koski.schema.NäyttöAmmatillinenOsittainen'
