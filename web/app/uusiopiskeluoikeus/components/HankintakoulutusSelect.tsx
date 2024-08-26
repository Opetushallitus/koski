import React from 'react'
import { useVirkailijaUser } from '../../appstate/user'
import { Checkbox } from '../../components-v2/controls/Checkbox'
import { t } from '../../i18n/i18n'
import { UusiOpiskeluoikeusDialogState } from '../state/state'
import { DialogKoodistoSelect } from './DialogKoodistoSelect'
import { TestIdLayer } from '../../appstate/useTestId'

export type HankintakoulutusSelectProps = {
  state: UusiOpiskeluoikeusDialogState
}

export const HankintakoulutusSelect = (props: HankintakoulutusSelectProps) => {
  const user = useVirkailijaUser()
  const isVarhaiskasvatusKoulutustoimija =
    (user?.varhaiskasvatuksenJärjestäjäKoulutustoimijat.length || 0) > 0
  const isTPOKoulutustoimija = user?.hasOneKoulutustoimijaWriteAccess || false

  return isVarhaiskasvatusKoulutustoimija || isTPOKoulutustoimija ? (
    <TestIdLayer id="hankintakoulutus">
      <section className="labelgroup">
        <span className="labelgroup--head">{t('Hankintakoulutus')}</span>

        {isVarhaiskasvatusKoulutustoimija && (
          <HankintakoulutusCheckbox
            state={props.state}
            label={t(
              'Esiopetus ostetaan oman organisaation ulkopuolelta ostopalveluna tai palvelusetelinä'
            )}
            value="esiopetus"
            testId="esiopetus"
          />
        )}

        {isTPOKoulutustoimija && (
          <HankintakoulutusCheckbox
            state={props.state}
            label={t('Taiteen perusopetus hankintakoulutuksena')}
            value="tpo"
            testId="tpo"
          />
        )}
      </section>

      {props.state.varhaiskasvatuksenJärjestämistapa.visible &&
        isVarhaiskasvatusKoulutustoimija && (
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
    </TestIdLayer>
  ) : null
}

type HankintakoulutusCheckboxProps = {
  state: UusiOpiskeluoikeusDialogState
  value: UusiOpiskeluoikeusDialogState['hankintakoulutus']['value']
  label: string
  testId: string
}

const HankintakoulutusCheckbox = (props: HankintakoulutusCheckboxProps) =>
  [undefined, props.value].includes(props.state.hankintakoulutus.value) ? (
    <Checkbox
      label={props.label}
      checked={props.state.hankintakoulutus.value === props.value}
      onChange={(opt) =>
        props.state.hankintakoulutus.set(opt ? props.value : undefined)
      }
      testId={props.testId}
    />
  ) : null
