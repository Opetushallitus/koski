import React from 'react'
import { SuoritusFieldsProps } from '.'
import { tutkintoKey, useTutkinnot } from '../../ammatillinen-v2/useTutkinnot'
import { t } from '../../i18n/i18n'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { DialogSelect } from '../components/DialogSelect'

// Tutkinnot ovat manner-Suomen perusteita kuten manner-Suomen ammatillisessa
// koulutuksessa. Suoritustapaa ei valita, koska se on aina reformi.
export const AhvenanmaanAmmatillinenFields = (props: SuoritusFieldsProps) => {
  const tutkinnot = useTutkinnot(props.state.oppilaitos.value?.oid)

  return (
    <>
      <label>
        {t('Suoritustyyppi')}
        <DialogPäätasonSuoritusSelect
          state={props.state}
          default="ahvenanmaanammatillinentutkinto"
          testId="suoritustyyppi"
        />
      </label>

      {props.state.tutkinto.visible && (
        <label>
          {t('Tutkinto')}
          <DialogSelect
            options={tutkinnot.options}
            value={
              props.state.tutkinto.value &&
              tutkintoKey(props.state.tutkinto.value)
            }
            onChange={(opt) => props.state.tutkinto.set(opt?.value)}
            onSearch={tutkinnot.setDebounceQuery}
            testId="tutkinto"
          />
        </label>
      )}
    </>
  )
}
