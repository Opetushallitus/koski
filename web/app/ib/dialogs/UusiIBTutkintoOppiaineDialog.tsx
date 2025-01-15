import React, { useCallback } from 'react'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../../components-v2/containers/Modal'
import { Checkbox } from '../../components-v2/controls/Checkbox'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { RaisedButton } from '../../components-v2/controls/RaisedButton'
import {
  Select,
  SelectOption,
  useKoodistoOptions
} from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { IBOpiskeluoikeus } from '../../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { IBOppiaineenSuoritus } from '../../types/fi/oph/koski/schema/IBOppiaineenSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { koodiviiteId } from '../../util/koodisto'
import { PäätasonSuoritusOf } from '../../util/opiskeluoikeus'
import { DialogSelect } from '../../uusiopiskeluoikeus/components/DialogSelect'
import { useIBTutkintoOppiaineState } from '../state/ibTutkintoOppiaine'
import {
  useAineryhmäOptions,
  useKielivalikoimaOptions,
  useOppiaineTasoOptions
} from '../state/options'

export type UusiIBTutkintoOppiaineDialogProps = {
  organisaatioOid: string
  päätasonSuoritus: PäätasonSuoritusOf<IBOpiskeluoikeus>
  onClose: () => void
  onSubmit: (oppiaine: IBOppiaineenSuoritus) => void
}

export const UusiIBTutkintoOppiaineDialog: React.FC<
  UusiIBTutkintoOppiaineDialogProps
> = (props) => {
  const state = useIBTutkintoOppiaineState()
  const tunnisteet = useKoodistoOptions('oppiaineetib')
  const kielet = useKielivalikoimaOptions(true)
  const ryhmät = useAineryhmäOptions(true)
  const tasot = useOppiaineTasoOptions(true)

  const onTunniste = useCallback(
    (option?: SelectOption<Koodistokoodiviite<'oppiaineetib'>>) => {
      state.tunniste.set(option?.value)
    },
    [state.tunniste]
  )

  const onSubmit = useCallback(() => {
    if (state.result) {
      props.onSubmit(state.result)
    }
  }, [props, state.result])

  return (
    <Modal>
      <ModalTitle>{t('Oppiaineen lisäys')}</ModalTitle>
      <ModalBody>
        {tunnisteet && (
          <label>
            {t('Oppiaine')}
            <Select
              inlineOptions
              options={tunnisteet}
              value={state.tunniste.value && koodiviiteId(state.tunniste.value)}
              onChange={onTunniste}
              testId="tunniste"
            />
          </label>
        )}
        {state.kieli.visible && kielet && (
          <label>
            {t('Kieli')}
            <DialogSelect
              options={kielet}
              value={state.kieli.value && koodiviiteId(state.kieli.value)}
              onChange={(o) => state.kieli.set(o?.value)}
              testId="kieli"
            />
          </label>
        )}
        {state.taso.visible && tasot && (
          <label>
            {t('Taso')}
            <DialogSelect
              options={tasot}
              value={state.taso.value && koodiviiteId(state.taso.value)}
              onChange={(o) => state.taso.set(o?.value)}
              testId="taso"
            />
          </label>
        )}
        {state.ryhmä.visible && ryhmät && (
          <label>
            {t('Aineryhmä')}
            <DialogSelect
              options={ryhmät}
              value={state.ryhmä.value && koodiviiteId(state.ryhmä.value)}
              onChange={(o) => state.ryhmä.set(o?.value)}
              testId="aineryhmä"
            />
          </label>
        )}
        {state.pakollinen.visible && (
          <label>
            <Checkbox
              label={t('Pakollinen')}
              checked={!!state.pakollinen.value}
              onChange={state.pakollinen.set}
              testId="pakollinen"
            />
          </label>
        )}
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={props.onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          onClick={onSubmit}
          disabled={!state.result}
          testId="submit"
        >
          {t('Lisää')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
