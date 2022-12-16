import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * VSTKehittyvänKielenTaitotasonArviointi
 *
 * @see `fi.oph.koski.schema.VSTKehittyvänKielenTaitotasonArviointi`
 */
export type VSTKehittyvänKielenTaitotasonArviointi = {
  $class: 'fi.oph.koski.schema.VSTKehittyvänKielenTaitotasonArviointi'
  taso: Koodistokoodiviite<
    'arviointiasteikkokehittyvankielitaidontasot',
    | 'A1.1'
    | 'A1.2'
    | 'A1.3'
    | 'A2.1'
    | 'A2.2'
    | 'B1.1'
    | 'B1.2'
    | 'B2.1'
    | 'B2.2'
    | 'C1.1'
    | 'C1.2'
    | 'C2.1'
    | 'C2.2'
  >
}

export const VSTKehittyvänKielenTaitotasonArviointi = (o: {
  taso: Koodistokoodiviite<
    'arviointiasteikkokehittyvankielitaidontasot',
    | 'A1.1'
    | 'A1.2'
    | 'A1.3'
    | 'A2.1'
    | 'A2.2'
    | 'B1.1'
    | 'B1.2'
    | 'B2.1'
    | 'B2.2'
    | 'C1.1'
    | 'C1.2'
    | 'C2.1'
    | 'C2.2'
  >
}): VSTKehittyvänKielenTaitotasonArviointi => ({
  $class: 'fi.oph.koski.schema.VSTKehittyvänKielenTaitotasonArviointi',
  ...o
})

export const isVSTKehittyvänKielenTaitotasonArviointi = (
  a: any
): a is VSTKehittyvänKielenTaitotasonArviointi =>
  a?.$class === 'VSTKehittyvänKielenTaitotasonArviointi'
