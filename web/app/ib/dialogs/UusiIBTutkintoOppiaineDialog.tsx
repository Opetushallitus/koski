import React, { useCallback } from 'react'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../../components-v2/containers/Modal'
import { Checkbox } from '../../components-v2/controls/Checkbox'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { LocalizedTextEdit } from '../../components-v2/controls/LocalizedTestField'
import { RaisedButton } from '../../components-v2/controls/RaisedButton'
import {
  Select,
  SelectOption,
  useKoodistoOptions
} from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { IBOpiskeluoikeus } from '../../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { IBTutkinnonOppiaineenSuoritus } from '../../types/fi/oph/koski/schema/IBTutkinnonOppiaineenSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { koodiviiteId } from '../../util/koodisto'
import { PäätasonSuoritusOf } from '../../util/opiskeluoikeus'
import { DialogSelect } from '../../uusiopiskeluoikeus/components/DialogSelect'
import {
  useIBTutkintoOppiaineState,
  UusiIBTutkintoOppiaineState
} from '../state/ibTutkintoOppiaine'
import {
  useAineryhmäOptions,
  useKielivalikoimaOptions,
  useOppiaineTasoOptions
} from '../state/options'
import { LaajuusEdit } from '../../components-v2/opiskeluoikeus/LaajuusField'
import { createLaajuusTunneissa } from '../../util/laajuus'

export type UusiIBTutkintoOppiaineDialogProps = {
  organisaatioOid: string
  päätasonSuoritus: PäätasonSuoritusOf<IBOpiskeluoikeus>
  onClose: () => void
  onSubmit: (oppiaine: IBTutkinnonOppiaineenSuoritus) => void
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
        <IBTutkintoOppiaineForm
          state={state}
          tunnisteet={tunnisteet}
          kielet={kielet}
          ryhmät={ryhmät}
          tasot={tasot}
          onTunniste={onTunniste}
        />
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

type IBTutkintoOppiaineFormProps = {
  state:
    | UusiIBTutkintoOppiaineState
    | UusiIBTutkintoOppiaineState['extendedEssay']
  tunnisteet: SelectOption<Koodistokoodiviite<'oppiaineetib'>>[]
  kielet: SelectOption<Koodistokoodiviite<'kielivalikoima'>>[] | null
  ryhmät: SelectOption<Koodistokoodiviite<'aineryhmaib'>>[] | null
  tasot: SelectOption<Koodistokoodiviite<'oppiaineentasoib'>>[] | null
  onTunniste: (o?: SelectOption<Koodistokoodiviite<'oppiaineetib'>>) => void
}

const IBTutkintoOppiaineForm: React.FC<IBTutkintoOppiaineFormProps> = ({
  state,
  tunnisteet,
  kielet,
  ryhmät,
  tasot,
  onTunniste
}) => {
  const onExtendedEssayTunniste = useCallback(
    (option?: SelectOption<Koodistokoodiviite<'oppiaineetib'>>) => {
      ;(state as UusiIBTutkintoOppiaineState).extendedEssay.tunniste.set(
        option?.value
      )
    },
    [state]
  )

  return (
    <>
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
      {(state as UusiIBTutkintoOppiaineState).extendedEssay?.tunniste
        .visible && (
        <>
          <div className="UusiOppiaine__aine">
            <IBTutkintoOppiaineForm
              state={(state as UusiIBTutkintoOppiaineState).extendedEssay}
              tunnisteet={tunnisteet}
              kielet={kielet}
              ryhmät={ryhmät}
              tasot={tasot}
              onTunniste={onExtendedEssayTunniste}
            />
          </div>
          <label>
            {t('Aihe')}
            <LocalizedTextEdit
              value={
                (state as UusiIBTutkintoOppiaineState).extendedEssay.aihe.value
              }
              onChange={
                (state as UusiIBTutkintoOppiaineState).extendedEssay.aihe.set
              }
              testId="essayAihe"
            />
          </label>
        </>
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
      {(state as UusiIBTutkintoOppiaineState).cas?.laajuus.visible && (
        <label>
          {t('Laajuus')}
          <LaajuusEdit
            value={(state as UusiIBTutkintoOppiaineState).cas.laajuus.value}
            onChange={(state as UusiIBTutkintoOppiaineState).cas.laajuus.set}
            createLaajuus={createLaajuusTunneissa}
            testId="laajuus"
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
    </>
  )
}
