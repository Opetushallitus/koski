import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusOpintopisteissä } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import React, { useState } from 'react'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../../components-v2/containers/Modal'
import { t } from '../../i18n/i18n'
import { KoodistoSelect } from '../../components-v2/opiskeluoikeus/KoodistoSelect'
import { DateInput } from '../../components-v2/controls/DateInput'
import { LaajuusEdit } from '../../components-v2/opiskeluoikeus/LaajuusField'
import { Spacer } from '../../components-v2/layout/Spacer'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { RaisedButton } from '../../components-v2/controls/RaisedButton'
import { PreIBOmanÄidinkielenOpinnot2019Arvosana } from '../PreIB2019KieliJaViestintäRows'

export type UusiOmanÄidinkielenOpinnotModalProps = {
  onClose: () => void
  onSubmit: (
    arvosana: PreIBOmanÄidinkielenOpinnot2019Arvosana,
    kieli: Koodistokoodiviite<'kielivalikoima'>,
    laajuus: LaajuusOpintopisteissä,
    arviointipäivä?: string
  ) => void
}

export const UusiOmanÄidinkielenOpinnotModal = ({
  onClose,
  onSubmit
}: UusiOmanÄidinkielenOpinnotModalProps) => {
  const [arvosana, setArvosana] = useState<
    PreIBOmanÄidinkielenOpinnot2019Arvosana | undefined
  >(undefined)
  const [arviointipäivä, setArviointipäivä] = useState<string | undefined>(
    undefined
  )
  const [kieli, setKieli] = useState<
    Koodistokoodiviite<'kielivalikoima'> | undefined
  >(undefined)
  const [laajuus, setLaajuus] = useState<LaajuusOpintopisteissä | undefined>(
    undefined
  )

  return (
    <Modal onClose={onClose}>
      <ModalTitle>
        {t('Täydentävien oman äidinkielen opintojen lisäys')}
      </ModalTitle>
      <ModalBody>
        <label>
          {t('Arvosana')}
          <KoodistoSelect
            koodistoUri={'arviointiasteikkoyleissivistava'}
            format={(koodiviite) =>
              koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
            }
            onSelect={(koodiviite) => {
              koodiviite &&
                setArvosana(
                  koodiviite as PreIBOmanÄidinkielenOpinnot2019Arvosana
                )
            }}
            value={arvosana ? arvosana.koodiarvo : undefined}
            testId={'arvosana'}
          />
        </label>
        <label>
          {t('Arviointipäivä')}
          <DateInput
            value={arviointipäivä}
            onChange={(date) => setArviointipäivä(date)}
            testId={'päivä'}
          />
        </label>
        <label>
          {t('Kieli')}
          <KoodistoSelect
            koodistoUri={'kielivalikoima'}
            onSelect={(koodiviite) => {
              koodiviite && setKieli(koodiviite)
            }}
            value={kieli ? kieli.koodiarvo : undefined}
            testId={'kieli'}
          />
        </label>
        <label>
          {t('Laajuus')}
          <LaajuusEdit
            value={laajuus}
            onChange={(value) => setLaajuus(value)}
            createLaajuus={(value) => LaajuusOpintopisteissä({ arvo: value })}
          />
        </label>
        <Spacer />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={[arvosana, kieli, laajuus].includes(undefined)}
          onClick={() => {
            if (
              arvosana !== undefined &&
              kieli !== undefined &&
              laajuus !== undefined
            ) {
              onSubmit(arvosana, kieli, laajuus, arviointipäivä)
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
