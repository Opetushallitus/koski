import React, { useCallback } from 'react'
import {
  Modal,
  ModalTitle,
  ModalBody,
  ModalFooter
} from '../../components-v2/containers/Modal'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { RaisedButton } from '../../components-v2/controls/RaisedButton'
import { SelectOption } from '../../components-v2/controls/Select'
import {
  PaikallinenKoulutus,
  PaikallinenKoulutusFields
} from '../../components-v2/opiskeluoikeus/PaikallinenKoulutusFields'
import { localize, t } from '../../i18n/i18n'
import { IBOpiskeluoikeus } from '../../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PreIBSuorituksenOsasuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBSuorituksenOsasuoritus2015'
import { koodiviiteId } from '../../util/koodisto'
import { PäätasonSuoritusOf } from '../../util/opiskeluoikeus'
import { DialogSelect } from '../../uusiopiskeluoikeus/components/DialogSelect'
import {
  usePreIBTunnisteOptions,
  preIB2015Oppiainekategoriat,
  useKielivalikoimaOptions,
  useMatematiikanOppimääräOptions,
  useAineryhmäOptions,
  useÄidinkielenKieliOptions,
  PaikallinenKey
} from '../state/options'
import {
  useUusiPreIB2015OppiaineState,
  PreIBOppiaineTunniste
} from '../state/preIBOppiaine'
import { Select } from '../../components-v2/controls/Select'

export type UusiPreIB2015OppiaineDialogProps = {
  päätasonSuoritus: PäätasonSuoritusOf<IBOpiskeluoikeus>
  onClose: () => void
  onSubmit: (oppiaine: PreIBSuorituksenOsasuoritus2015) => void
}

export const UusiPreIB2015OppiaineDialog: React.FC<
  UusiPreIB2015OppiaineDialogProps
> = (props) => {
  const state = useUusiPreIB2015OppiaineState()
  const tunnisteet = usePreIBTunnisteOptions(
    preIB2015Oppiainekategoriat,
    props.päätasonSuoritus
  )
  const kielet = useKielivalikoimaOptions(state.kieli.visible)
  const matematiikanOppimäärät = useMatematiikanOppimääräOptions(
    state.matematiikanOppimäärä.visible
  )
  const ryhmät = useAineryhmäOptions(state.ryhmä.visible)
  const äidinkielenKielet = useÄidinkielenKieliOptions(
    state.äidinkielenKieli.visible
  )

  const onTunniste = useCallback(
    (option?: SelectOption<PreIBOppiaineTunniste>) => {
      console.log('tunniste', option)
      state.tunniste.set(option?.value)
      state.paikallinenTunniste.setVisible(option?.key === PaikallinenKey)
    },
    [state.paikallinenTunniste, state.tunniste]
  )

  const onPaikallinenKoulutus = useCallback(
    (paikallinen?: PaikallinenKoulutus) => {
      if (paikallinen) {
        state.paikallinenTunniste.set(
          PaikallinenKoodi({
            koodiarvo: paikallinen.koodiarvo,
            nimi: localize(paikallinen.nimi)
          })
        )
        state.paikallinenKuvaus.set(localize(paikallinen.kuvaus))
      }
    },
    [state.paikallinenKuvaus, state.paikallinenTunniste]
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
              value={
                state.paikallinenTunniste.visible
                  ? PaikallinenKey
                  : state.tunniste.value && koodiviiteId(state.tunniste.value)
              }
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
        {state.ryhmä.visible && ryhmät && (
          <label>
            {t('Aineryhmä')}
            <DialogSelect
              options={ryhmät}
              value={state.ryhmä.value && koodiviiteId(state.ryhmä.value)}
              onChange={(o) => state.ryhmä.set(o?.value)}
              testId="ryhmä"
            />
          </label>
        )}
        {state.matematiikanOppimäärä.visible && matematiikanOppimäärät && (
          <label>
            {t('Oppimäärä')}
            <DialogSelect
              options={matematiikanOppimäärät}
              value={
                state.matematiikanOppimäärä.value &&
                koodiviiteId(state.matematiikanOppimäärä.value)
              }
              onChange={(o) => state.matematiikanOppimäärä.set(o?.value)}
              testId="matematiikanOppimäärä"
            />
          </label>
        )}
        {state.äidinkielenKieli.visible && äidinkielenKielet && (
          <label>
            {t('Kieli')}
            <DialogSelect
              options={äidinkielenKielet}
              value={
                state.äidinkielenKieli.value &&
                koodiviiteId(state.äidinkielenKieli.value)
              }
              onChange={(o) => state.äidinkielenKieli.set(o?.value)}
              testId="äidinkielenKieli"
            />
          </label>
        )}
        {state.paikallinenTunniste.visible && (
          <PaikallinenKoulutusFields onChange={onPaikallinenKoulutus} />
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
          {t('Lisää opiskeluoikeus')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
