import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Pre-IB-koulutuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.PreIBKoulutusmoduuli2015`
 */
export type PreIBKoulutusmoduuli2015 = {
  $class: 'fi.oph.koski.schema.PreIBKoulutusmoduuli2015'
  tunniste: Koodistokoodiviite<'suorituksentyyppi', 'preiboppimaara'>
}

export const PreIBKoulutusmoduuli2015 = (
  o: {
    tunniste?: Koodistokoodiviite<'suorituksentyyppi', 'preiboppimaara'>
  } = {}
): PreIBKoulutusmoduuli2015 => ({
  $class: 'fi.oph.koski.schema.PreIBKoulutusmoduuli2015',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'preiboppimaara',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

export const isPreIBKoulutusmoduuli2015 = (
  a: any
): a is PreIBKoulutusmoduuli2015 =>
  a?.$class === 'fi.oph.koski.schema.PreIBKoulutusmoduuli2015'
