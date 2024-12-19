import React, { useCallback, useMemo } from 'react'
import { usePreferences } from '../../appstate/preferences'
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
  mapOptionLabels,
  optionGroup,
  paikallinenKoodiToOption,
  Select,
  SelectOption,
  sortOptions,
  useKoodistoOptions
} from '../../components-v2/controls/Select'
import { KoodistoSelect } from '../../components-v2/opiskeluoikeus/KoodistoSelect'
import { AddOppiaineenOsasuoritusDialog } from '../../components-v2/opiskeluoikeus/OppiaineTable'
import {
  paikallinenKoulutus,
  PaikallinenKoulutus,
  PaikallinenKoulutusFields
} from '../../components-v2/opiskeluoikeus/PaikallinenKoulutusFields'
import { localize, t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import {
  isPaikallinenKoodi,
  PaikallinenKoodi
} from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PaikallinenLukionKurssi2015 } from '../../types/fi/oph/koski/schema/PaikallinenLukionKurssi2015'
import { koodiviiteId } from '../../util/koodisto'
import { PreIB2019OsasuoritusTunniste } from '../oppiaineet/preIBModuuli2019'
import {
  usePreIB2019OsasuoritusState,
  UusiPaikallinenLukionKurssiKey
} from '../state/preIB2019Kurssi'
import { labelWithKoodiarvo } from '../state/options'

export const UusiPreIB2019OsasuoritusDialog: AddOppiaineenOsasuoritusDialog<
  PreIBKurssinSuoritus2019 // TODO: nyt ei pysy pää enää kasassa, jatketaan tästä...
> = ({ onAdd, ...props }) => {
  const koulutus = props.oppiaine.koulutusmoduuli
  const {
    preferences: paikallisetLukionKurssit,
    store: storePaikallinenLukionKurssi,
    remove: removePaikallinenLukionKurssi
  } = usePreferences<PaikallinenLukionKurssi2015>( // TODO: Tänne oikeat paikalliset
    props.organisaatioOid,
    'paikallinenlukionkurssi'
  )

  const state = usePreIB2019OsasuoritusState()

  const lukioTunnisteOptions = useKoodistoOptions('moduulikoodistolops2021')

  const paikallisetTunnisteetOptions = useMemo(
    () => [
      optionGroup(t('Paikalliset moduulit'), [
        ...paikallisetLukionKurssit.map((kurssi) =>
          paikallinenKoodiToOption(kurssi.tunniste, { removable: true })
        ),
        {
          key: UusiPaikallinenLukionKurssiKey,
          label: t('Lisää uusi')
        }
      ])
    ],
    [paikallisetLukionKurssit]
  )

  const tunnisteet: SelectOption<Koodistokoodiviite | PaikallinenKoodi>[] =
    useMemo(
      () => [
        ...paikallisetTunnisteetOptions,
        ...sortOptions(
          mapOptionLabels(labelWithKoodiarvo)(lukioTunnisteOptions)
        )
      ],
      [lukioTunnisteOptions, paikallisetTunnisteetOptions]
    )

  const onTunniste = useCallback(
    (option?: SelectOption<Koodistokoodiviite | PaikallinenKoodi>) => {
      if (option?.key === UusiPaikallinenLukionKurssiKey) {
        state.tunniste.set(
          PaikallinenKoodi({ koodiarvo: '', nimi: localize('') })
        )
        state.kuvaus.set(localize(''))
      } else if (isPaikallinenKoodi(option?.value)) {
        state.tunniste.set(option.value)
        state.kuvaus.set(localize('todo: kaiva kuvaus tähän'))
      } else if (option?.value) {
        state.tunniste.set(option.value as PreIB2019OsasuoritusTunniste)
      }
    },
    [state.kuvaus, state.tunniste]
  )

  const onPaikallinenKoulutus = useCallback(
    (paikallinen?: PaikallinenKoulutus) => {
      if (paikallinen) {
        state.tunniste.set(
          PaikallinenKoodi({
            koodiarvo: paikallinen.koodiarvo,
            nimi: localize(paikallinen.nimi)
          })
        )
        state.kuvaus.set(localize(paikallinen.kuvaus))
      }
    },
    [state.kuvaus, state.tunniste]
  )

  const addOsasuoritus = useCallback(() => {
    const kurssi = state.result
    if (kurssi) {
      onAdd(kurssi)
    }
  }, [onAdd, state])

  const onRemoveTunniste = useCallback(
    (option: SelectOption<Koodistokoodiviite | PaikallinenKoodi>) => {
      const key = option.value && koodiviiteId(option.value)
      if (key) {
        removePaikallinenLukionKurssi(key)
      }
    },
    [removePaikallinenLukionKurssi]
  )

  return (
    <Modal>
      <ModalTitle>{t('Lisää osasuoritus')}</ModalTitle>
      <ModalBody>
        {state.tunniste.visible && lukioTunnisteOptions && (
          <label>
            {t('Osasuoritus')}
            <Select
              inlineOptions
              options={tunnisteet}
              value={state.tunniste.value && koodiviiteId(state.tunniste.value)}
              onChange={onTunniste}
              onRemove={onRemoveTunniste}
              testId="tunniste"
            />
          </label>
        )}
        {state.matematiikanOppimäärä.visible && (
          <label>
            {t('Oppimäärä')}
            <KoodistoSelect
              koodistoUri="oppiainematematiikka"
              value={state.matematiikanOppimäärä.value?.koodiarvo}
              onSelect={state.matematiikanOppimäärä.set}
              testId="matematiikanOppimäärä"
            />
          </label>
        )}
        {state.äidinkielenKieli.visible && (
          <label>
            {t('Kieli')}
            <KoodistoSelect
              koodistoUri="oppiaineaidinkielijakirjallisuus"
              value={state.äidinkielenKieli.value?.koodiarvo}
              onSelect={state.äidinkielenKieli.set}
              testId="äidinkielenKieli"
            />
          </label>
        )}
        {state.vierasKieli.visible && (
          <label>
            {t('Kieli')}
            <KoodistoSelect
              koodistoUri="kielivalikoima"
              value={state.vierasKieli.value?.koodiarvo}
              onSelect={state.vierasKieli.set}
              testId="vierasKieli"
            />
          </label>
        )}
        {state.isPaikallinen && (
          <PaikallinenKoulutusFields
            onChange={onPaikallinenKoulutus}
            initial={
              isPaikallinenKoodi(state.tunniste.value) && state.kuvaus.value
                ? paikallinenKoulutus(state.tunniste.value, state.kuvaus.value)
                : undefined
            }
          />
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
