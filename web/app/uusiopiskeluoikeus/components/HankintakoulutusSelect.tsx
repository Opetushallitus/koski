import React from 'react'
import { Checkbox } from '../../components-v2/controls/Checkbox'
import { t } from '../../i18n/i18n'
import { UusiOpiskeluoikeusDialogState } from '../state/state'
import { DialogKoodistoSelect } from './DialogKoodistoSelect'

export type HankintakoulutusSelectProps = {
  state: UusiOpiskeluoikeusDialogState
}

export const HankintakoulutusSelect = (props: HankintakoulutusSelectProps) => (
  <>
    <section className="labelgroup">
      <span className="labelgroup--head">{t('Hankintakoulutus')}</span>

      <HankintakoulutusCheckbox
        state={props.state}
        label={t(
          'Esiopetus ostetaan oman organisaation ulkopuolelta ostopalveluna tai palvelusetelinä'
        )}
        value="esiopetus"
      />

      <HankintakoulutusCheckbox
        state={props.state}
        label={t(
          'Taiteen perusopetus hankintakoulutuksena järjestetään oman organisaation ulkopuolelta'
        )}
        value="tpo"
      />
    </section>

    {props.state.varhaiskasvatuksenJärjestämistapa.visible && (
      <label>
        {t('Varhaiskasvatuksen järjestämismuoto')}
        <DialogKoodistoSelect
          state={props.state.varhaiskasvatuksenJärjestämistapa}
          koodistoUri="vardajarjestamismuoto"
          koodiarvot={['JM02', 'JM03']}
          testId="varhaiskasvatuksenJärjestämismuoto"
        />
      </label>
    )}
  </>
)

type HankintakoulutusCheckboxProps = {
  state: UusiOpiskeluoikeusDialogState
  value: UusiOpiskeluoikeusDialogState['hankintakoulutus']['value']
  label: string
}

const HankintakoulutusCheckbox = (props: HankintakoulutusCheckboxProps) =>
  [undefined, props.value].includes(props.state.hankintakoulutus.value) ? (
    <Checkbox
      label={props.label}
      checked={props.state.hankintakoulutus.value === props.value}
      onChange={(opt) =>
        props.state.hankintakoulutus.set(opt ? props.value : undefined)
      }
    />
  ) : null
