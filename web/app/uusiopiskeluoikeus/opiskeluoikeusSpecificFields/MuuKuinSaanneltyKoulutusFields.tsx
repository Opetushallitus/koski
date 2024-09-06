import React, { useEffect } from 'react'
import { SuoritusFieldsProps } from '.'
import { KoodistokoodiviiteKoodistonNimellä } from '../../appstate/koodisto'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { OpintokokonaisuusSelect } from '../components/OpintokokonaisuusSelect'

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
      <OpintokokonaisuusSelect
        opintokokonaisuudet={props.state.opintokokonaisuus}
      />
    </label>
  ) : null
}

const formatOpintokokonaisuusName = (
  koodi: KoodistokoodiviiteKoodistonNimellä
): string => `${koodi.koodiviite.koodiarvo} ${t(koodi.koodiviite.nimi)}`
