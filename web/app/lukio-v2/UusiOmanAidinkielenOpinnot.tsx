import React, { useState } from 'react'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../components-v2/containers/Modal'
import { DateInput } from '../components-v2/controls/DateInput'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { LaajuusEdit } from '../components-v2/opiskeluoikeus/LaajuusField'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { koodiarvoOnly } from '../components-v2/opiskeluoikeus/ArvosanaField'
import { Spacer } from '../components-v2/layout/Spacer'
import { t } from '../i18n/i18n'
import { LaajuusOpintopisteissä } from '../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { LukionOmanÄidinkielenOpinnot } from '../types/fi/oph/koski/schema/LukionOmanAidinkielenOpinnot'

type Arvosana = LukionOmanÄidinkielenOpinnot['arvosana']
type Kieli = LukionOmanÄidinkielenOpinnot['kieli']

// Sama malli kuin perusopetus-v2/UusiTäydentäväOmanÄidinkielenOpinnotModal.tsx,
// mutta lukion omilla tyypeillä - laajuus on lukiolla pakollinen (toisin kuin
// perusopetuksella), siksi mukana submitDisabled-tarkistuksessa.
export type UusiOmanÄidinkielenOpinnotModalProps = {
  onClose: () => void
  onSubmit: (opinnot: LukionOmanÄidinkielenOpinnot) => void
}

export const UusiOmanÄidinkielenOpinnotModal: React.FC<
  UusiOmanÄidinkielenOpinnotModalProps
> = ({ onClose, onSubmit }) => {
  const [arvosana, setArvosana] = useState<Arvosana | undefined>(undefined)
  const [arviointipäivä, setArviointipäivä] = useState<string | undefined>(
    undefined
  )
  const [kieli, setKieli] = useState<Kieli | undefined>(undefined)
  const [laajuus, setLaajuus] = useState<LaajuusOpintopisteissä | undefined>(
    undefined
  )

  const submitDisabled =
    arvosana === undefined || kieli === undefined || laajuus === undefined

  return (
    <Modal onClose={onClose}>
      <ModalTitle>
        {t('Täydentävien oman äidinkielen opintojen lisäys')}
      </ModalTitle>
      <ModalBody>
        <label>
          {t('Arvosana')}
          <KoodistoSelect
            koodistoUri="arviointiasteikkoyleissivistava"
            format={koodiarvoOnly}
            sort
            skipAutoFocus
            onSelect={(k) => k && setArvosana(k as Arvosana)}
            value={arvosana?.koodiarvo}
            testId="arvosana"
          />
        </label>
        <label>
          {t('Arviointipäivä')}
          <DateInput
            value={arviointipäivä}
            onChange={(d) => setArviointipäivä(d)}
            testId="arviointipäivä"
            align="right"
          />
        </label>
        <label>
          {t('Kieli')}
          <KoodistoSelect
            koodistoUri="kielivalikoima"
            onSelect={(k) => k && setKieli(k as Kieli)}
            value={kieli?.koodiarvo}
            testId="kieli"
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
        <Spacer />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={submitDisabled}
          onClick={() => {
            if (
              arvosana !== undefined &&
              kieli !== undefined &&
              laajuus !== undefined
            ) {
              onSubmit(
                LukionOmanÄidinkielenOpinnot({
                  arvosana,
                  kieli,
                  laajuus,
                  arviointipäivä
                })
              )
              onClose()
            }
          }}
          testId="confirm"
        >
          {t('Lisää täydentävät oman äidinkielen opinnot')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
