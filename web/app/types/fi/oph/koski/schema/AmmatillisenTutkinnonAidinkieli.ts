import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * AmmatillisenTutkinnonÄidinkieli
 *
 * @see `fi.oph.koski.schema.AmmatillisenTutkinnonÄidinkieli`
 */
export type AmmatillisenTutkinnonÄidinkieli = {
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonÄidinkieli'
  tunniste: Koodistokoodiviite<'ammatillisenoppiaineet', 'AI'>
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export const AmmatillisenTutkinnonÄidinkieli = (o: {
  tunniste?: Koodistokoodiviite<'ammatillisenoppiaineet', 'AI'>
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}): AmmatillisenTutkinnonÄidinkieli => ({
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonÄidinkieli',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'AI',
    koodistoUri: 'ammatillisenoppiaineet'
  }),
  ...o
})

export const isAmmatillisenTutkinnonÄidinkieli = (
  a: any
): a is AmmatillisenTutkinnonÄidinkieli =>
  a?.$class === 'fi.oph.koski.schema.AmmatillisenTutkinnonÄidinkieli'
