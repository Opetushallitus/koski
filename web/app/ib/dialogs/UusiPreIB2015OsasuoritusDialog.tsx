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
import { NumberField } from '../../components-v2/controls/NumberField'
import { RaisedButton } from '../../components-v2/controls/RaisedButton'
import {
  optionGroup,
  paikallinenKoodiToOption,
  Select,
  SelectOption
} from '../../components-v2/controls/Select'
import { AddOppiaineenOsasuoritusDialog } from '../../components-v2/opiskeluoikeus/OppiaineTable'
import {
  paikallinenKoulutus,
  PaikallinenKoulutus,
  PaikallinenKoulutusFields
} from '../../components-v2/opiskeluoikeus/PaikallinenKoulutusFields'
import { localize, t } from '../../i18n/i18n'
import { IBKurssi } from '../../types/fi/oph/koski/schema/IBKurssi'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusKursseissa } from '../../types/fi/oph/koski/schema/LaajuusKursseissa'
import {
  isPaikallinenKoodi,
  PaikallinenKoodi
} from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PaikallinenLukionKurssi2015 } from '../../types/fi/oph/koski/schema/PaikallinenLukionKurssi2015'
import { PreIBKurssinSuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBKurssinSuoritus2015'
import { koodiviiteEquals, koodiviiteId } from '../../util/koodisto'
import { DialogSelect } from '../../uusiopiskeluoikeus/components/DialogSelect'
import { PreIB2015OsasuoritusTunniste } from '../oppiaineet/preIBKurssi2015'
import {
  useLukiokurssinTyypit,
  useOppiaineenKurssiOptions
} from '../state/options'
import {
  usePreIB2015OsasuoritusState,
  UusiIBKurssiKey,
  UusiPaikallinenLukionKurssiKey
} from '../state/preIB2015Kurssi'

export const UusiPreIB2015OsasuoritusDialog: AddOppiaineenOsasuoritusDialog<
  PreIBKurssinSuoritus2015
> = ({ onAdd, ...props }) => {
  const koulutus = props.oppiaine.koulutusmoduuli
  const {
    preferences: paikallisetLukionKurssit,
    store: storePaikallinenLukionKurssi,
    remove: removePaikallinenLukionKurssi
  } = usePreferences<PaikallinenLukionKurssi2015>(
    props.organisaatioOid,
    'paikallinenlukionkurssi'
  )
  const {
    preferences: ibKurssit,
    store: storeIBKurssi,
    remove: removeIBKurssi
  } = usePreferences<IBKurssi>(props.organisaatioOid, 'ibkurssi')

  const state = usePreIB2015OsasuoritusState()

  const valtakunnallisetTunnisteetOptions =
    useOppiaineenKurssiOptions(
      !isPaikallinenKoodi(props.oppiaine.koulutusmoduuli.tunniste)
        ? props.oppiaine.koulutusmoduuli.tunniste
        : undefined
    ) || []

  const paikallisetTunnisteetOptions = useMemo(
    () => [
      optionGroup(t('IB-kurssit'), [
        ...ibKurssit.map((kurssi) =>
          paikallinenKoodiToOption(kurssi.tunniste, { removable: true })
        ),
        {
          key: UusiIBKurssiKey,
          label: t('Lisää uusi')
        }
      ]),
      optionGroup(t('Paikalliset lukion kurssit'), [
        ...paikallisetLukionKurssit.map((kurssi) =>
          paikallinenKoodiToOption(kurssi.tunniste, { removable: true })
        ),
        {
          key: UusiPaikallinenLukionKurssiKey,
          label: t('Lisää uusi')
        }
      ])
    ],
    [ibKurssit, paikallisetLukionKurssit]
  )

  const tunnisteet: SelectOption<Koodistokoodiviite | PaikallinenKoodi>[] = [
    ...paikallisetTunnisteetOptions,
    ...valtakunnallisetTunnisteetOptions
  ]

  const kurssityypit = useLukiokurssinTyypit(state.lukiokurssinTyyppi.visible)

  const onTunniste = useCallback(
    (option?: SelectOption<Koodistokoodiviite | PaikallinenKoodi>) => {
      const tunniste = option?.value
      if (
        option?.key === UusiIBKurssiKey ||
        option?.key === UusiPaikallinenLukionKurssiKey
      ) {
        state.uusiTyyppi.set(option.key === UusiIBKurssiKey ? 'ib' : 'lukio')
        state.tunniste.set(
          PaikallinenKoodi({ koodiarvo: '', nimi: localize('') })
        )
        state.kuvaus.set(localize(''))
      } else if (isPaikallinenKoodi(tunniste)) {
        const kurssi = [...paikallisetLukionKurssit, ...ibKurssit].find((k) =>
          koodiviiteEquals(tunniste)(k.tunniste)
        )
        state.tunniste.set(tunniste)
        state.kuvaus.set(kurssi?.kuvaus)
      } else if (option?.value) {
        state.uusiTyyppi.set(undefined)
        state.tunniste.set(option.value as PreIB2015OsasuoritusTunniste)
      }
    },
    [
      ibKurssit,
      paikallisetLukionKurssit,
      state.kuvaus,
      state.tunniste,
      state.uusiTyyppi
    ]
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
      if (state.uusiTyyppi.value === 'lukio') {
        storePaikallinenLukionKurssi(
          koodiviiteId(kurssi.koulutusmoduuli.tunniste),
          kurssi.koulutusmoduuli as PaikallinenLukionKurssi2015
        )
      }
      if (state.uusiTyyppi.value === 'ib') {
        storeIBKurssi(
          koodiviiteId(kurssi.koulutusmoduuli.tunniste),
          kurssi.koulutusmoduuli as IBKurssi
        )
      }
    }
  }, [onAdd, state, storeIBKurssi, storePaikallinenLukionKurssi])

  const onRemoveTunniste = useCallback(
    (option: SelectOption<Koodistokoodiviite | PaikallinenKoodi>) => {
      const key = option.value && koodiviiteId(option.value)
      if (key) {
        removePaikallinenLukionKurssi(key)
        removeIBKurssi(key)
      }
    },
    [removeIBKurssi, removePaikallinenLukionKurssi]
  )

  return (
    <Modal>
      <ModalTitle>{t('Lisää osasuoritus')}</ModalTitle>
      <ModalBody>
        {state.tunniste.visible && valtakunnallisetTunnisteetOptions && (
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
