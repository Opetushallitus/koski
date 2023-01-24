import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { NäytönArvioitsija } from './NaytonArvioitsija'
import { NäytönArviointikohde } from './NaytonArviointikohde'

/**
 * Näytön arvioinnin lisätiedot
 *
 * @see `fi.oph.koski.schema.NäytönArviointi`
 */
export type NäytönArviointi = {
  $class: 'fi.oph.koski.schema.NäytönArviointi'
  päivä: string
  arvosana: Koodistokoodiviite<
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
  arvioinnistaPäättäneet?: Array<
    Koodistokoodiviite<'ammatillisennaytonarvioinnistapaattaneet', string>
  >
  hylkäyksenPeruste?: LocalizedString
  hyväksytty?: boolean
  arviointikeskusteluunOsallistuneet?: Array<
    Koodistokoodiviite<
      'ammatillisennaytonarviointikeskusteluunosallistuneet',
      string
    >
  >
  arvioitsijat?: Array<NäytönArvioitsija>
  arviointikohteet?: Array<NäytönArviointikohde>
}

export const NäytönArviointi = (o: {
  päivä: string
  arvosana: Koodistokoodiviite<
    | 'arviointiasteikkoammatillinenhyvaksyttyhylatty'
    | 'arviointiasteikkoammatillinent1k3'
    | 'arviointiasteikkoammatillinen15',
    string
  >
  arvioinnistaPäättäneet?: Array<
    Koodistokoodiviite<'ammatillisennaytonarvioinnistapaattaneet', string>
  >
  hylkäyksenPeruste?: LocalizedString
  hyväksytty?: boolean
  arviointikeskusteluunOsallistuneet?: Array<
    Koodistokoodiviite<
      'ammatillisennaytonarviointikeskusteluunosallistuneet',
      string
    >
  >
  arvioitsijat?: Array<NäytönArvioitsija>
  arviointikohteet?: Array<NäytönArviointikohde>
}): NäytönArviointi => ({ $class: 'fi.oph.koski.schema.NäytönArviointi', ...o })

NäytönArviointi.className = 'fi.oph.koski.schema.NäytönArviointi' as const

export const isNäytönArviointi = (a: any): a is NäytönArviointi =>
  a?.$class === 'fi.oph.koski.schema.NäytönArviointi'
