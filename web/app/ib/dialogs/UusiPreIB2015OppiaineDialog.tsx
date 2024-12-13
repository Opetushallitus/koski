import React, { useCallback } from 'react'
import { usePreferences } from '../../appstate/preferences'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../../components-v2/containers/Modal'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { RaisedButton } from '../../components-v2/controls/RaisedButton'
import { Select, SelectOption } from '../../components-v2/controls/Select'
import {
  paikallinenKoulutus,
  PaikallinenKoulutus,
  PaikallinenKoulutusFields
} from '../../components-v2/opiskeluoikeus/PaikallinenKoulutusFields'
import { localize, t } from '../../i18n/i18n'
import { IBOpiskeluoikeus } from '../../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import {
  isPaikallinenKoodi,
  PaikallinenKoodi
} from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import {
  isPaikallinenLukionOppiaine2015,
  PaikallinenLukionOppiaine2015
} from '../../types/fi/oph/koski/schema/PaikallinenLukionOppiaine2015'
import { PreIBSuorituksenOsasuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBSuorituksenOsasuoritus2015'
import { koodiviiteId } from '../../util/koodisto'
import { PäätasonSuoritusOf } from '../../util/opiskeluoikeus'
import { DialogSelect } from '../../uusiopiskeluoikeus/components/DialogSelect'
import {
  preIB2015Oppiainekategoriat,
  useAineryhmäOptions,
  useKielivalikoimaOptions,
  useMatematiikanOppimääräOptions,
  usePreIBTunnisteOptions,
  useÄidinkielenKieliOptions,
  UusiPaikallinenKey
} from '../state/options'
import {
  PreIBOppiaineTunniste,
  useUusiPreIB2015OppiaineState
} from '../state/preIBOppiaine'

export type UusiPreIB2015OppiaineDialogProps = {
  organisaatioOid: string
  päätasonSuoritus: PäätasonSuoritusOf<IBOpiskeluoikeus>
  onClose: () => void
  onSubmit: (oppiaine: PreIBSuorituksenOsasuoritus2015) => void
}

export const UusiPreIB2015OppiaineDialog: React.FC<
  UusiPreIB2015OppiaineDialogProps
> = (props) => {
  const state = useUusiPreIB2015OppiaineState()
  const {
    preferences: paikallisetOppiaineet,
    store: storePaikallinenOppiaine,
    remove: removePaikallinenOppiaine
  } = usePreferences<PaikallinenLukionOppiaine2015>(
    props.organisaatioOid,
    'paikallinenlukionoppiaine'
  )
  const tunnisteet = usePreIBTunnisteOptions(
    preIB2015Oppiainekategoriat,
    props.päätasonSuoritus,
    paikallisetOppiaineet
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
      state.tunniste.set(option?.value)
      if (isPaikallinenKoodi(option?.value)) {
        state.paikallinenTunniste.setVisible(true)
        state.paikallinenTunniste.set(option.value)
        state.paikallinenKuvaus.set(localize('todo: kaiva kuvaus'))
      } else {
        state.paikallinenTunniste.setVisible(option?.key === UusiPaikallinenKey)
      }
    },
    [state.paikallinenKuvaus, state.paikallinenTunniste, state.tunniste]
  )

  const onDeleteTunniste = useCallback(
    (option?: SelectOption<PreIBOppiaineTunniste>) => {
      if (option?.value) {
        removePaikallinenOppiaine(koodiviiteId(option.value))
      }
    },
    [removePaikallinenOppiaine]
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
      const koulutusmoduuli = state.result.koulutusmoduuli
      if (isPaikallinenLukionOppiaine2015(koulutusmoduuli)) {
        storePaikallinenOppiaine(
          koodiviiteId(koulutusmoduuli.tunniste),
          koulutusmoduuli
        )
      }
    }
  }, [props, state.result, storePaikallinenOppiaine])

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
                  ? UusiPaikallinenKey
                  : state.tunniste.value && koodiviiteId(state.tunniste.value)
              }
              onChange={onTunniste}
              onRemove={onDeleteTunniste}
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
          <PaikallinenKoulutusFields
            onChange={onPaikallinenKoulutus}
            initial={
              state.paikallinenTunniste.value &&
              state.paikallinenKuvaus.value &&
              paikallinenKoulutus(
                state.paikallinenTunniste.value,
                state.paikallinenKuvaus.value
              )
            }
          />
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
