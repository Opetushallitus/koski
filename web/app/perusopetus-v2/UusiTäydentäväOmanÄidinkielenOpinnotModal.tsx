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
import { Spacer } from '../components-v2/layout/Spacer'
import { t } from '../i18n/i18n'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusVuosiviikkotunneissa } from '../types/fi/oph/koski/schema/LaajuusVuosiviikkotunneissa'
import { OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina } from '../types/fi/oph/koski/schema/OmanAidinkielenOpinnotLaajuusVuosiviikkotunteina'

type Arvosana = OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina['arvosana']
type Kieli = OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina['kieli']

export type UusiTäydentäväOmanÄidinkielenOpinnotModalProps = {
  onClose: () => void
  onSubmit: (opinnot: OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina) => void
}

export const UusiTäydentäväOmanÄidinkielenOpinnotModal: React.FC<
  UusiTäydentäväOmanÄidinkielenOpinnotModalProps
> = ({ onClose, onSubmit }) => {
  const [arvosana, setArvosana] = useState<Arvosana | undefined>(undefined)
  const [arviointipäivä, setArviointipäivä] = useState<string | undefined>(
    undefined
  )
  const [kieli, setKieli] = useState<Kieli | undefined>(undefined)
  const [laajuus, setLaajuus] = useState<
    LaajuusVuosiviikkotunneissa | undefined
  >(undefined)

  const submitDisabled = arvosana === undefined || kieli === undefined

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
            format={(k) => k.koodiarvo + ' ' + t(k.nimi)}
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
            onChange={(value) => setLaajuus(value)}
            createLaajuus={(arvo) => LaajuusVuosiviikkotunneissa({ arvo })}
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
            if (arvosana !== undefined && kieli !== undefined) {
              onSubmit(
                OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina({
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
