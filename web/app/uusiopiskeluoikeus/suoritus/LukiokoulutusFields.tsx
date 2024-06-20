import React, { useCallback } from 'react'
import { Select } from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { usePäätasonSuoritustyypit } from '../state/hooks'
import { SuoritusFieldsProps } from './SuoritusFields'
import { lukionDiaarinumerot2019 } from '../opintooikeus/createLukiokoulutuksenOpiskeluoikeus'

export const LukioKoulutusFields = (props: SuoritusFieldsProps) => {
  const suorituksenTyyppi = props.state.päätasonSuoritus.value?.koodiarvo
  const perusteFilter = useCallback(
    (diaarinumero: string): boolean =>
      suorituksenTyyppi === 'lukionoppimaara'
        ? true
        : suorituksenTyyppi === 'lukionoppiaineenoppimaara'
          ? !lukionDiaarinumerot2019.includes(diaarinumero)
          : lukionDiaarinumerot2019.includes(diaarinumero),
    [suorituksenTyyppi]
  )
  const options = usePäätasonSuoritustyypit(props.state)

  return (
    <>
      {t('Oppimäärä')}
      <Select
        options={options}
        value={
          props.state.päätasonSuoritus.value &&
          koodistokoodiviiteId(props.state.päätasonSuoritus.value)
        }
        onChange={(opt) => props.state.päätasonSuoritus.set(opt?.value)}
        testId="oppimäärä"
      />

      <DialogPerusteSelect
        state={props.state}
        default="OPH-2263-2019"
        filter={perusteFilter}
      />
    </>
  )
}
