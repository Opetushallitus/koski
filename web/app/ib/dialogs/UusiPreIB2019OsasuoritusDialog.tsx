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
  isLukionPaikallinenOpintojakso2019,
  LukionPaikallinenOpintojakso2019
} from '../../types/fi/oph/koski/schema/LukionPaikallinenOpintojakso2019'
import {
  isPaikallinenKoodi,
  PaikallinenKoodi
} from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 } from '../../types/fi/oph/koski/schema/PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019'
import { koodiviiteEquals, koodiviiteId } from '../../util/koodisto'
import { PreIB2019OsasuoritusTunniste } from '../oppiaineet/preIBModuuli2019'
import { labelWithKoodiarvo } from '../state/options'
import {
  usePreIB2019OsasuoritusState,
  UusiPaikallinenLukionKurssiKey
} from '../state/preIB2019Moduuli'
import { isPreIBSuorituksenOsasuoritus2019 } from '../../types/fi/oph/koski/schema/PreIBSuorituksenOsasuoritus2019'
import { LaajuusEdit } from '../../components-v2/opiskeluoikeus/LaajuusField'
import { LaajuusOpintopisteissä } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'

export const UusiPreIB2019OsasuoritusDialog: AddOppiaineenOsasuoritusDialog<
  PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019
> = ({ onAdd, ...props }) => {
  const {
    preferences: paikallisetOpintojaksot,
    store: storePaikallinenOpintojakso,
    remove: removePaikallinenOpintojakso
  } = usePreferences<LukionPaikallinenOpintojakso2019>(
    props.organisaatioOid,
    'lukionpaikallinenopintojakso2019'
  )

  const state = usePreIB2019OsasuoritusState(
    isPreIBSuorituksenOsasuoritus2019(props.oppiaine)
      ? props.oppiaine.koulutusmoduuli.tunniste
      : undefined
  )

  const lukioTunnisteOptions = useKoodistoOptions('moduulikoodistolops2021')

  const paikallisetTunnisteetOptions = useMemo(
    () => [
      optionGroup(t('Paikalliset opintojaksot'), [
        ...paikallisetOpintojaksot.map((kurssi) =>
          paikallinenKoodiToOption(kurssi.tunniste, { removable: true })
        ),
        {
          key: UusiPaikallinenLukionKurssiKey,
          label: t('Lisää uusi')
        }
      ])
    ],
    [paikallisetOpintojaksot]
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
      const tunniste = option?.value
      if (option?.key === UusiPaikallinenLukionKurssiKey) {
        state.tunniste.set(
          PaikallinenKoodi({ koodiarvo: '', nimi: localize('') })
        )
        state.kuvaus.set(localize(''))
      } else if (isPaikallinenKoodi(tunniste)) {
        const opintojakso = paikallisetOpintojaksot.find((jakso) =>
          koodiviiteEquals(tunniste)(jakso.tunniste)
        )
        state.tunniste.set(tunniste)
        state.kuvaus.set(opintojakso?.kuvaus)
        state.laajuus.set(opintojakso?.laajuus)
      } else if (option?.value) {
        state.tunniste.set(option.value as PreIB2019OsasuoritusTunniste)
      }
    },
    [paikallisetOpintojaksot, state.kuvaus, state.laajuus, state.tunniste]
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

      if (isLukionPaikallinenOpintojakso2019(kurssi.koulutusmoduuli)) {
        storePaikallinenOpintojakso(
          koodiviiteId(kurssi.koulutusmoduuli.tunniste),
          kurssi.koulutusmoduuli
        )
      }
    }
  }, [onAdd, state.result, storePaikallinenOpintojakso])

  const onRemoveTunniste = useCallback(
    (option: SelectOption<Koodistokoodiviite | PaikallinenKoodi>) => {
      const key = option.value && koodiviiteId(option.value)
      if (key) {
        removePaikallinenOpintojakso(key)
      }
    },
    [removePaikallinenOpintojakso]
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
        {state.laajuus.visible && (
          <label>
            {t('Laajuus')}
            <LaajuusEdit
              value={state.laajuus.value}
              onChange={state.laajuus.set}
              createLaajuus={(arvo) => LaajuusOpintopisteissä({ arvo })} // TODO: Tehdään tälle sama temppu kuin arvosanalle eli päätellään oikea laajuus komponentin sisällä luokan perusteella
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
          <Checkbox
            label={t('Pakollinen')}
            checked={!!state.pakollinen.value}
            onChange={state.pakollinen.set}
            testId="pakollinen"
          />
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
