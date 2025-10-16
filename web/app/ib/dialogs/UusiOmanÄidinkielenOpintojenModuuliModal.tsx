import { LukionOmanÄidinkielenOpinto } from '../../types/fi/oph/koski/schema/LukionOmanAidinkielenOpinto'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { useKoodistoOfConstraint } from '../../appstate/koodisto'
import { useChildSchema } from '../../appstate/constraints'
import { LukionOmanÄidinkielenOpintojenOsasuoritus } from '../../types/fi/oph/koski/schema/LukionOmanAidinkielenOpintojenOsasuoritus'
import React, { useState } from 'react'
import { LaajuusOpintopisteissä } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../../components-v2/containers/Modal'
import { t } from '../../i18n/i18n'
import { KoodistoSelect } from '../../components-v2/opiskeluoikeus/KoodistoSelect'
import { LaajuusEdit } from '../../components-v2/opiskeluoikeus/LaajuusField'
import { DateInput } from '../../components-v2/controls/DateInput'
import { Spacer } from '../../components-v2/layout/Spacer'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { RaisedButton } from '../../components-v2/controls/RaisedButton'
import {
  PreIBOmanÄidinkielenOpinto,
  PreIBOmanÄidinkielenOpintoOsasuorituksenArvosana
} from '../PreIB2019KieliJaViestintäRows'

export type UusiOmanÄidinkielenOpintojenModuuliModalProps = {
  olemassaOlevatModuulit: string[]
  onClose: () => void
  onSubmit: (
    koulutusmoduuli: LukionOmanÄidinkielenOpinto,
    arvosana?: PreIBOmanÄidinkielenOpintoOsasuorituksenArvosana,
    arviointipäivä?: string,
    kieli?: Koodistokoodiviite<'kieli'>
  ) => void
}

export const UusiOmanÄidinkielenOpintojenModuuliModal = ({
  olemassaOlevatModuulit,
  onClose,
  onSubmit
}: UusiOmanÄidinkielenOpintojenModuuliModalProps) => {
  const moduuliOptions =
    useKoodistoOfConstraint(
      useChildSchema(LukionOmanÄidinkielenOpinto.className, 'tunniste')
    ) || []
  const arvosanaOptions =
    useKoodistoOfConstraint(
      useChildSchema(
        LukionOmanÄidinkielenOpintojenOsasuoritus.className,
        'arviointi.[].arvosana'
      )
    ) || []

  const [moduuli, setModuuli] = useState<
    PreIBOmanÄidinkielenOpinto | undefined
  >(undefined)
  const [laajuus, setLaajuus] = useState<LaajuusOpintopisteissä | undefined>(
    undefined
  )
  const [kieli, setKieli] = useState<Koodistokoodiviite<'kieli'> | undefined>(
    undefined
  )
  const [arvosana, setArvosana] = useState<
    PreIBOmanÄidinkielenOpintoOsasuorituksenArvosana | undefined
  >(undefined)
  const [arviointipäivä, setArviointipäivä] = useState<string | undefined>(
    undefined
  )

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Lisää moduuli')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Moduuli')}
          <KoodistoSelect
            koodistoUri={'moduulikoodistolops2021'}
            koodiarvot={moduuliOptions.map((m) => m.koodiviite.koodiarvo)}
            filter={(koodiviite) =>
              !olemassaOlevatModuulit.includes(koodiviite.koodiarvo)
            }
            format={(koodiviite) =>
              koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
            }
            onSelect={(koodiviite) => {
              koodiviite && setModuuli(koodiviite as PreIBOmanÄidinkielenOpinto)
            }}
            value={moduuli ? moduuli.koodiarvo : undefined}
            testId={'koulutusmoduuli'}
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
        <label>
          {t('Arvosana')}
          <KoodistoSelect
            koodistoUri={'arviointiasteikkoyleissivistava'}
            koodiarvot={arvosanaOptions.map((m) => m.koodiviite.koodiarvo)}
            format={(koodiviite) =>
              koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
            }
            onSelect={(koodiviite) => {
              koodiviite &&
                setArvosana(
                  koodiviite as PreIBOmanÄidinkielenOpintoOsasuorituksenArvosana
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
            align="right"
          />
        </label>
        <label>
          {t('Kieli')}
          <KoodistoSelect
            koodistoUri={'kieli'}
            onSelect={(koodiviite) => {
              koodiviite && setKieli(koodiviite)
            }}
            value={kieli ? kieli.koodiarvo : undefined}
            testId={'kieli'}
          />
        </label>
        <Spacer />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={
            [moduuli, laajuus].includes(undefined) ||
            (!!arvosana && !arviointipäivä) ||
            (!arvosana && !!arviointipäivä)
          }
          onClick={() => {
            if (moduuli !== undefined && laajuus !== undefined) {
              onSubmit(
                LukionOmanÄidinkielenOpinto({ tunniste: moduuli, laajuus }),
                arvosana,
                arviointipäivä,
                kieli
              )
              onClose()
            }
          }}
          testId="confirm"
        >
          {t('Lisää')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
