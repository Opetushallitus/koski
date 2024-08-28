import React, { useEffect } from 'react'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { DialogKoodistoSelect } from '../components/DialogKoodistoSelect'
import { SuoritusFieldsProps } from '.'
import { KoodistokoodiviiteKoodistonNimellä } from '../../appstate/koodisto'

const päätasonSuoritus = Koodistokoodiviite({
  koodiarvo: 'muukuinsaanneltykoulutus',
  koodistoUri: 'suorituksentyyppi'
})

export const MuuKuinSäänneltyKoulutusFields = (props: SuoritusFieldsProps) => {
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => props.state.päätasonSuoritus.set(päätasonSuoritus), [])

  return props.state.opintokokonaisuus.visible ? (
    <label>
      {t('Opintokokonaisuus')}
      <DialogKoodistoSelect
        state={props.state.opintokokonaisuus}
        koodistoUri="opintokokonaisuudet"
        formatLabel={formatOpintokokonaisuusName}
        testId="opintokokonaisuus"
      />
    </label>
  ) : null
}

const formatOpintokokonaisuusName = (
  koodi: KoodistokoodiviiteKoodistonNimellä
): string => `${koodi.koodiviite.koodiarvo} ${t(koodi.koodiviite.nimi)}`
