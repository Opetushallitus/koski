import { LocalizedString } from './LocalizedString'
import { NäytönSuorituspaikka } from './NaytonSuorituspaikka'
import { NäytönSuoritusaika } from './NaytonSuoritusaika'
import { NäytönArviointi } from './NaytonArviointi'

/**
 * Tutkinnon tai koulutuksen osan suoritukseen kuuluvan ammattiosaamisen näytön tiedot.
 *
 * @see `fi.oph.koski.schema.NäyttöAmmatillinenOsottainenUseastaTutkinnosta`
 */
export type NäyttöAmmatillinenOsottainenUseastaTutkinnosta = {
  $class: 'fi.oph.koski.schema.NäyttöAmmatillinenOsottainenUseastaTutkinnosta'
  kuvaus?: LocalizedString
  suorituspaikka?: NäytönSuorituspaikka
  suoritusaika?: NäytönSuoritusaika
  arviointi?: NäytönArviointi
}

export const NäyttöAmmatillinenOsottainenUseastaTutkinnosta = (
  o: {
    kuvaus?: LocalizedString
    suorituspaikka?: NäytönSuorituspaikka
    suoritusaika?: NäytönSuoritusaika
    arviointi?: NäytönArviointi
  } = {}
): NäyttöAmmatillinenOsottainenUseastaTutkinnosta => ({
  $class: 'fi.oph.koski.schema.NäyttöAmmatillinenOsottainenUseastaTutkinnosta',
  ...o
})

NäyttöAmmatillinenOsottainenUseastaTutkinnosta.className =
  'fi.oph.koski.schema.NäyttöAmmatillinenOsottainenUseastaTutkinnosta' as const

export const isNäyttöAmmatillinenOsottainenUseastaTutkinnosta = (
  a: any
): a is NäyttöAmmatillinenOsottainenUseastaTutkinnosta =>
  a?.$class ===
  'fi.oph.koski.schema.NäyttöAmmatillinenOsottainenUseastaTutkinnosta'
