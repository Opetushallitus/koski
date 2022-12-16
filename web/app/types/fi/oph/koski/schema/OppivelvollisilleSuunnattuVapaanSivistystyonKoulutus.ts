import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Vapaan sivistystyön oppivelvollisuuskoulutuksen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus`
 */
export type OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus = {
  $class: 'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus'
  tunniste: Koodistokoodiviite<'koulutus', '999909'>
  perusteenDiaarinumero?: string
  koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
}

export const OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus = (
  o: {
    tunniste?: Koodistokoodiviite<'koulutus', '999909'>
    perusteenDiaarinumero?: string
    koulutustyyppi?: Koodistokoodiviite<'koulutustyyppi', string>
  } = {}
): OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus => ({
  $class:
    'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus',
  tunniste: Koodistokoodiviite({
    koodiarvo: '999909',
    koodistoUri: 'koulutus'
  }),
  ...o
})

export const isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus = (
  a: any
): a is OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus =>
  a?.$class === 'OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus'
