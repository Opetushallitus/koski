import { AmmatillinenArviointi } from './AmmatillinenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AmmatillisenTutkinnonOsanLisätieto } from './AmmatillisenTutkinnonOsanLisatieto'
import { PaikallinenOpintovalmiuksiaTukevaOpinto } from './PaikallinenOpintovalmiuksiaTukevaOpinto'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * MuidenOpintovalmiuksiaTukevienOpintojenSuoritus
 *
 * @see `fi.oph.koski.schema.MuidenOpintovalmiuksiaTukevienOpintojenSuoritus`
 */
export type MuidenOpintovalmiuksiaTukevienOpintojenSuoritus = {
  $class: 'fi.oph.koski.schema.MuidenOpintovalmiuksiaTukevienOpintojenSuoritus'
  arviointi?: Array<AmmatillinenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillinenmuitaopintovalmiuksiatukeviaopintoja'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: PaikallinenOpintovalmiuksiaTukevaOpinto
  tunnustettu?: OsaamisenTunnustaminen
}

export const MuidenOpintovalmiuksiaTukevienOpintojenSuoritus = (o: {
  arviointi?: Array<AmmatillinenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillinenmuitaopintovalmiuksiatukeviaopintoja'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  lisätiedot?: Array<AmmatillisenTutkinnonOsanLisätieto>
  koulutusmoduuli: PaikallinenOpintovalmiuksiaTukevaOpinto
  tunnustettu?: OsaamisenTunnustaminen
}): MuidenOpintovalmiuksiaTukevienOpintojenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillinenmuitaopintovalmiuksiatukeviaopintoja',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.MuidenOpintovalmiuksiaTukevienOpintojenSuoritus',
  ...o
})

MuidenOpintovalmiuksiaTukevienOpintojenSuoritus.className =
  'fi.oph.koski.schema.MuidenOpintovalmiuksiaTukevienOpintojenSuoritus' as const

export const isMuidenOpintovalmiuksiaTukevienOpintojenSuoritus = (
  a: any
): a is MuidenOpintovalmiuksiaTukevienOpintojenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.MuidenOpintovalmiuksiaTukevienOpintojenSuoritus'
