import React from 'react'
import { DialogKoodistoSelect } from './DialogKoodistoSelect'
import { DialogField } from '../state/state'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { KoodistokoodiviiteKoodistonNimellä } from '../../appstate/koodisto'
import { t } from '../../i18n/i18n'

export type OpintokokonaisuusSelectProps = {
  opintokokonaisuudet: DialogField<Koodistokoodiviite<'opintokokonaisuudet'>>
  default?: string
}

export const OpintokokonaisuusSelect = (
  props: OpintokokonaisuusSelectProps
) => (
  <DialogKoodistoSelect
    state={props.opintokokonaisuudet}
    koodistoUri="opintokokonaisuudet"
    testId="opintokokonaisuus"
    formatLabel={formatOpintokokonaisuusName}
  />
)

const formatOpintokokonaisuusName = (
  koodi: KoodistokoodiviiteKoodistonNimellä
): string => `${koodi.koodiviite.koodiarvo} ${t(koodi.koodiviite.nimi)}`
