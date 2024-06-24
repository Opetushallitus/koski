import React, { useCallback } from 'react'
import { t } from '../../i18n/i18n'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { lukionDiaarinumerot2019 } from '../opintooikeus/createLukiokoulutuksenOpiskeluoikeus'
import { SuoritusFieldsProps } from './SuoritusFields'

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

  return (
    <>
      {t('Oppimäärä')}
      <DialogPäätasonSuoritusSelect state={props.state} testId="oppimäärä" />

      <DialogPerusteSelect
        state={props.state}
        default="OPH-2263-2019"
        filter={perusteFilter}
      />
    </>
  )
}
