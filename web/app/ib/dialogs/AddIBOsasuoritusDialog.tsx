import React, { useCallback } from 'react'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../../components-v2/containers/Modal'
import { Checkbox } from '../../components-v2/controls/Checkbox'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { NumberField } from '../../components-v2/controls/NumberField'
import { RaisedButton } from '../../components-v2/controls/RaisedButton'
import { Select } from '../../components-v2/controls/Select'
import { AddOppiaineenOsasuoritusDialog } from '../../components-v2/opiskeluoikeus/OppiaineTable'
import {
  PaikallinenKoulutus,
  PaikallinenKoulutusFields
} from '../../components-v2/opiskeluoikeus/PaikallinenKoulutusFields'
import { localize, t } from '../../i18n/i18n'
import { LaajuusKursseissa } from '../../types/fi/oph/koski/schema/LaajuusKursseissa'
import {
  isPaikallinenKoodi,
  PaikallinenKoodi
} from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PreIBKurssinSuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBKurssinSuoritus2015'
import { koodiviiteId } from '../../util/koodisto'
import { DialogSelect } from '../../uusiopiskeluoikeus/components/DialogSelect'
import { createPreIBKurssinSuoritus2015 } from '../oppiaineet/preIBKurssi2015'
import {
  useLukiokurssinTyypit,
  useOppiaineenKurssiOptions
} from '../state/options'
import { useIBOsasuoritusState } from '../state/osasuoritusState'

export const AddIBOsasuoritusDialog: AddOppiaineenOsasuoritusDialog<
  PreIBKurssinSuoritus2015
> = ({ onAdd, ...props }) => {
  const koulutus = props.oppiaine.koulutusmoduuli
  const state = useIBOsasuoritusState(koulutus, createPreIBKurssinSuoritus2015)

  const lukioTunnisteet = useOppiaineenKurssiOptions(
    !isPaikallinenKoodi(props.oppiaine.koulutusmoduuli.tunniste)
      ? props.oppiaine.koulutusmoduuli.tunniste
      : undefined
  )
  const kurssityypit = useLukiokurssinTyypit(state.lukiokurssinTyyppi.visible)

  const onPaikallinenKoulutus = useCallback(
    (paikallinen?: PaikallinenKoulutus) => {
      if (paikallinen) {
        state.paikallinenTunniste.set(
          PaikallinenKoodi({
            koodiarvo: paikallinen.koodiarvo,
            nimi: localize(paikallinen.nimi)
          })
        )
        state.kuvaus.set(localize(paikallinen.kuvaus))
      }
    },
    [state.kuvaus, state.paikallinenTunniste]
  )

  const addOsasuoritus = useCallback(() => {
    state.result && onAdd(state.result)
  }, [onAdd, state.result])

  return (
    <Modal>
      <ModalTitle>{t('Lisää osasuoritus')}</ModalTitle>
      <ModalBody>
        {state.tunniste.visible && lukioTunnisteet && (
          <label>
            {t('Osasuoritus')}
            <Select
              inlineOptions
              options={lukioTunnisteet}
              value={state.tunniste.value && koodiviiteId(state.tunniste.value)}
              onChange={(o) => state.tunniste.set(o?.value as any)}
              testId="tunniste"
            />
          </label>
        )}
        {state.paikallinenTunniste.visible && (
          <PaikallinenKoulutusFields onChange={onPaikallinenKoulutus} />
        )}
        {state.lukiokurssinTyyppi.visible && kurssityypit && (
          <label>
            {t('Kurssin tyyppi')}
            <DialogSelect
              options={kurssityypit}
              value={
                state.lukiokurssinTyyppi.value &&
                koodiviiteId(state.lukiokurssinTyyppi.value)
              }
              onChange={(o) => state.lukiokurssinTyyppi.set(o?.value)}
              testId="tunniste"
            />
          </label>
        )}
        {state.laajuus.visible && (
          <label>
            {t('Laajuus')}
            <NumberField
              value={state.laajuus.value?.arvo}
              onChange={(arvo) =>
                state.laajuus.set(LaajuusKursseissa({ arvo }))
              }
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
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={props.onClose}>{t('Peruuta')}</FlatButton>
        <RaisedButton onClick={addOsasuoritus} disabled={!state.result}>
          {t('Lisää')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
