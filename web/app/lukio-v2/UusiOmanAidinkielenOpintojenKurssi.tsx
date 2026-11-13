import React, { useMemo, useState } from 'react'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../components-v2/containers/Modal'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import {
  filterOptions,
  mapOptionLabels,
  Select,
  SelectOption,
  useKoodistoOptions
} from '../components-v2/controls/Select'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { koodiarvoOnly } from '../components-v2/opiskeluoikeus/ArvosanaField'
import { LaajuusEdit } from '../components-v2/opiskeluoikeus/LaajuusField'
import { t } from '../i18n/i18n'
import { todayISODate } from '../date/date'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi } from '../types/fi/oph/koski/schema/LukionOmanAidinkielenOpinnonOsasuorituksenArviointi'
import { LukionOmanÄidinkielenOpinto } from '../types/fi/oph/koski/schema/LukionOmanAidinkielenOpinto'
import { LukionOmanÄidinkielenOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/LukionOmanAidinkielenOpintojenOsasuoritus'
import { labelWithKoodiarvo } from './UusiLukionModuuli'

type OmanÄidinkielenOpinnonTunniste = LukionOmanÄidinkielenOpinto['tunniste']

export const UusiOmanÄidinkielenOpintojenKurssi: React.FC<{
  olemassaOlevatKoodiarvot: string[]
  onAdd: (kurssi: LukionOmanÄidinkielenOpintojenOsasuoritus) => void
  onClose: () => void
}> = ({ olemassaOlevatKoodiarvot, onAdd, onClose }) => {
  const [tunniste, setTunniste] = useState<
    OmanÄidinkielenOpinnonTunniste | undefined
  >(undefined)
  const [laajuus, setLaajuus] = useState<LaajuusOpintopisteissä | undefined>(
    undefined
  )
  const [suorituskieli, setSuorituskieli] = useState<
    Koodistokoodiviite<'kieli'> | undefined
  >(undefined)
  const [arvosana, setArvosana] = useState<
    LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi['arvosana'] | undefined
  >(undefined)

  const kaikkiTunnisteet = useKoodistoOptions('moduulikoodistolops2021')
  const tunnisteOptions: SelectOption<OmanÄidinkielenOpinnonTunniste>[] =
    useMemo(
      () =>
        mapOptionLabels(labelWithKoodiarvo)(
          filterOptions<Koodistokoodiviite<'moduulikoodistolops2021'>>(
            (o) =>
              !!o.value?.koodiarvo.startsWith('OÄI') &&
              !olemassaOlevatKoodiarvot.includes(o.value.koodiarvo)
          )(kaikkiTunnisteet)
        ) as SelectOption<OmanÄidinkielenOpinnonTunniste>[],
      [kaikkiTunnisteet, olemassaOlevatKoodiarvot]
    )

  const voiLisätä = Boolean(tunniste && laajuus)

  const lisää = () => {
    if (!tunniste || !laajuus) return
    onAdd(
      LukionOmanÄidinkielenOpintojenOsasuoritus({
        koulutusmoduuli: LukionOmanÄidinkielenOpinto({ tunniste, laajuus }),
        suorituskieli,
        arviointi: arvosana
          ? [
              LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi({
                arvosana,
                päivä: todayISODate()
              })
            ]
          : undefined
      })
    )
  }

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Lisää osasuoritus')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Kurssi')}
          <Select
            options={tunnisteOptions}
            value={tunniste?.koodiarvo}
            onChange={(option) => setTunniste(option?.value)}
            testId="tunniste"
          />
        </label>
        <label>
          {t('Laajuus')}
          <LaajuusEdit
            value={laajuus}
            onChange={setLaajuus}
            createLaajuus={(arvo) => LaajuusOpintopisteissä({ arvo })}
          />
        </label>
        <label>
          {t('Suorituskieli')}
          <KoodistoSelect
            koodistoUri="kieli"
            zeroValueOption
            onSelect={setSuorituskieli}
            value={suorituskieli?.koodiarvo}
            testId="suorituskieli"
          />
        </label>
        <label>
          {t('Arvosana')}
          <KoodistoSelect
            koodistoUri="arviointiasteikkoyleissivistava"
            format={koodiarvoOnly}
            sort
            zeroValueOption
            onSelect={(k) =>
              setArvosana(
                k as
                  | LukionOmanÄidinkielenOpinnonOsasuorituksenArviointi['arvosana']
                  | undefined
              )
            }
            value={arvosana?.koodiarvo}
            testId="arvosana"
          />
        </label>
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton onClick={lisää} disabled={!voiLisätä} testId="submit">
          {t('Lisää')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
