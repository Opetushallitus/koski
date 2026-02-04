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
import { LocalizedTextEdit } from '../../components-v2/controls/LocalizedTestField'
import { RaisedButton } from '../../components-v2/controls/RaisedButton'
import {
  paikallinenKoodiToOption,
  Select,
  SelectOption
} from '../../components-v2/controls/Select'
import { KoodistoSelect } from '../../components-v2/opiskeluoikeus/KoodistoSelect'
import { AddOppiaineenOsasuoritusDialog } from '../../components-v2/opiskeluoikeus/OppiaineTable'
import {
  paikallinenKoulutus,
  PaikallinenKoulutus,
  PaikallinenKoulutusFields
} from '../../components-v2/opiskeluoikeus/PaikallinenKoulutusFields'
import { emptyLocalizedString, localize, t } from '../../i18n/i18n'
import { IBKurssi } from '../../types/fi/oph/koski/schema/IBKurssi'
import { IBKurssinSuoritus } from '../../types/fi/oph/koski/schema/IBKurssinSuoritus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { OsaamisenTunnustaminen } from '../../types/fi/oph/koski/schema/OsaamisenTunnustaminen'
import {
  isPaikallinenKoodi,
  PaikallinenKoodi
} from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { koodiviiteEquals, koodiviiteId } from '../../util/koodisto'
import { IBLaajuusEdit } from '../components/IBLaajuusEdit'
import {
  useIBTutkintoKurssiState,
  UusiIBKurssiKey
} from '../state/ibTutkintoKurssi'

export const UusiIBTutkintoOsasuoritusDialog: AddOppiaineenOsasuoritusDialog<
  IBKurssinSuoritus
> = ({ onAdd, alkamispäivä, ...props }) => {
  const {
    preferences: ibKurssit,
    store: storeIBKurssi,
    remove: removeIBKurssi
  } = usePreferences<IBKurssi>(props.organisaatioOid, 'ibkurssi')

  const state = useIBTutkintoKurssiState(alkamispäivä)

  const tunnisteOptions = useMemo(
    () => [
      ...ibKurssit.map((kurssi) =>
        paikallinenKoodiToOption(kurssi.tunniste, { removable: true })
      ),
      {
        key: UusiIBKurssiKey,
        label: t('Lisää uusi')
      }
    ],
    [ibKurssit]
  )

  const onTunniste = useCallback(
    (option?: SelectOption<PaikallinenKoodi>) => {
      const tunniste = option?.value
      if (option?.key === UusiIBKurssiKey) {
        state.tunniste.set(
          PaikallinenKoodi({ koodiarvo: '', nimi: localize('') })
        )
        state.kuvaus.set(localize(''))
      } else if (isPaikallinenKoodi(tunniste)) {
        const kurssi = ibKurssit.find((jakso) =>
          koodiviiteEquals(tunniste)(jakso.tunniste)
        )
        state.tunniste.set(tunniste)
        state.kuvaus.set(kurssi?.kuvaus)
      } else if (option?.value) {
        state.tunniste.set(option.value)
      }
    },
    [ibKurssit, state.kuvaus, state.tunniste]
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
      storeIBKurssi(
        koodiviiteId(kurssi.koulutusmoduuli.tunniste),
        kurssi.koulutusmoduuli
      )
    }
  }, [onAdd, state.result, storeIBKurssi])

  const onRemoveTunniste = useCallback(
    (option: SelectOption<Koodistokoodiviite | PaikallinenKoodi>) => {
      const key = option.value && koodiviiteId(option.value)
      if (key) {
        removeIBKurssi(key)
      }
    },
    [removeIBKurssi]
  )

  return (
    <Modal>
      <ModalTitle>{t('Lisää osasuoritus')}</ModalTitle>
      <ModalBody>
        {state.tunniste.visible && tunnisteOptions && (
          <label>
            {t('Osasuoritus')}
            <Select
              inlineOptions
              options={tunnisteOptions}
              value={state.tunniste.value && koodiviiteId(state.tunniste.value)}
              onChange={onTunniste}
              onRemove={onRemoveTunniste}
              testId="tunniste"
            />
          </label>
        )}
        {state.kuvaus.visible && (
          <PaikallinenKoulutusFields
            onChange={onPaikallinenKoulutus}
            initial={
              state.tunniste.value && state.kuvaus.value
                ? paikallinenKoulutus(state.tunniste.value, state.kuvaus.value)
                : undefined
            }
          />
        )}
        {state.laajuus.visible && (
          <label>
            {t('Laajuus')}
            <IBLaajuusEdit
              alkamispäivä={alkamispäivä}
              value={state.laajuus.value}
              onChange={state.laajuus.set}
            />
          </label>
        )}
        {state.suorituskieli.visible && (
          <label>
            {t('Suorituskieli')}
            <KoodistoSelect
              koodistoUri="kieli"
              value={state.suorituskieli.value?.koodiarvo}
              onSelect={state.suorituskieli.set}
              testId="suorituskieli"
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
        {state.tunnustettu.visible && (
          <label>
            {t('Osaamisen tunnustaminen')}
            {state.tunnustettu.value ? (
              <fieldset>
                <LocalizedTextEdit
                  testId="tunnustettu.selite"
                  large
                  value={state.tunnustettu.value.selite}
                  onChange={(selite) =>
                    state.tunnustettu.set({
                      ...state.tunnustettu.value!,
                      selite: selite ?? emptyLocalizedString
                    })
                  }
                />
                <Checkbox
                  checked={state.tunnustettu.value.rahoituksenPiirissä}
                  onChange={(rahoituksenPiirissä) =>
                    state.tunnustettu.set({
                      ...state.tunnustettu.value!,
                      rahoituksenPiirissä
                    })
                  }
                  label={t('Rahoituksen piirissä')}
                  testId="tunnustettu.rahoituksenPiirissä"
                />
              </fieldset>
            ) : (
              <FlatButton
                testId="tunnustettu.lisää"
                onClick={() =>
                  state.tunnustettu.set(
                    OsaamisenTunnustaminen({
                      selite: emptyLocalizedString
                    })
                  )
                }
              >
                {t('Lisää osaamisen tunnustaminen')}
              </FlatButton>
            )}
          </label>
        )}
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={props.onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          onClick={addOsasuoritus}
          disabled={!state.result}
          testId="submit"
        >
          {t('Lisää')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
