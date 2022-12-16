import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Arvioitsija } from './Arvioitsija'

/**
 * MuunAmmatillisenKoulutuksenArviointi
 *
 * @see `fi.oph.koski.schema.MuunAmmatillisenKoulutuksenArviointi`
 */
export type MuunAmmatillisenKoulutuksenArviointi = {
  $class: 'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenArviointi'
  päivä: string
  arvosana: Koodistokoodiviite<
    | 'arviointiasteikkomuuammatillinenkoulutus'
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}

export const MuunAmmatillisenKoulutuksenArviointi = (o: {
  päivä: string
  arvosana: Koodistokoodiviite<
    | 'arviointiasteikkomuuammatillinenkoulutus'
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
  hyväksytty?: boolean
  kuvaus?: LocalizedString
  arvioitsijat?: Array<Arvioitsija>
}): MuunAmmatillisenKoulutuksenArviointi => ({
  $class: 'fi.oph.koski.schema.MuunAmmatillisenKoulutuksenArviointi',
  ...o
})

export const isMuunAmmatillisenKoulutuksenArviointi = (
  a: any
): a is MuunAmmatillisenKoulutuksenArviointi =>
  a?.$class === 'MuunAmmatillisenKoulutuksenArviointi'
