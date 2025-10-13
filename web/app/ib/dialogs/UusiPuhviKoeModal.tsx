import { useKoodistoOfConstraint } from '../../appstate/koodisto'
import { useChildSchema } from '../../appstate/constraints'
import { PuhviKoe2019 } from '../../types/fi/oph/koski/schema/PuhviKoe2019'
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
import { Spacer } from '../../components-v2/layout/Spacer'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { RaisedButton } from '../../components-v2/controls/RaisedButton'
import { PreIBPuhviKoe2019Arvosana } from '../PreIB2019KieliJaViestintäRows'

export type UusiPuhviKoeModal = {
  onClose: () => void
  onSubmit: (arvosana: PreIBPuhviKoe2019Arvosana, päivä: string) => void
}

export const UusiPuhviKoeModal = ({ onClose, onSubmit }: UusiPuhviKoeModal) => {
  const arvosanat =
    useKoodistoOfConstraint(
      useChildSchema(PuhviKoe2019.className, 'arvosana')
    ) || []

  const [arvosana, setArvosana] = useState<
    PreIBPuhviKoe2019Arvosana | undefined
  >(undefined)
  const [päivä, setPäivä] = useState<string | undefined>(undefined)

  return (
    <Modal onClose={onClose}>
      <ModalTitle>
        {t('Toisen asteen puheviestintätaitojen päättökokeen lisäys')}
      </ModalTitle>
      <ModalBody>
        <label>
          {t('Arvosana')}
          <KoodistoSelect
            koodistoUri={'arviointiasteikkoyleissivistava'}
            format={(koodiviite) =>
              koodiviite.koodiarvo + ' ' + t(koodiviite.nimi)
            }
            koodiarvot={arvosanat.map((a) => a.koodiviite.koodiarvo)}
            onSelect={(koodiviite) => {
              koodiviite && setArvosana(koodiviite as PreIBPuhviKoe2019Arvosana)
            }}
            value={arvosana ? arvosana.koodiarvo : undefined}
            testId={'arvosana'}
          />
        </label>
        <label>
          {t('Päivä')}
          <DateInput
            value={päivä}
            onChange={(date) => setPäivä(date)}
            testId={'päivä'}
            align="right"
          />
        </label>
        <Spacer />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={[arvosana, päivä].includes(undefined)}
          onClick={() => {
            if (arvosana !== undefined && päivä !== undefined) {
              onSubmit(arvosana, päivä)
              onClose()
            }
          }}
          testId="confirm"
        >
          {t('Lisää toisen asteen puheviestintätaitojen päättökoe')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
