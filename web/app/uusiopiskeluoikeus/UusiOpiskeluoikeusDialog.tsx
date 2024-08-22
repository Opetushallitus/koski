import React, { useCallback, useState } from 'react'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../components-v2/containers/Modal'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { t } from '../i18n/i18n'
import { Opiskeluoikeus } from '../types/fi/oph/koski/schema/Opiskeluoikeus'
import { UusiOpiskeluoikeusForm } from './UusiOpiskeluoikeusForm'
import { TestIdRoot } from '../appstate/useTestId'

export type UusiOpiskeluoikeusDialogProps = {
  onSubmit: (opiskeluoikeus: Opiskeluoikeus) => void
  onClose: () => void
}

export const UusiOpiskeluoikeusDialog = (
  props: UusiOpiskeluoikeusDialogProps
) => {
  const [result, setResult] = useState<Opiskeluoikeus>()

  const onSubmit = useCallback(() => {
    result && props.onSubmit(result)
  }, [props, result])

  return (
    <TestIdRoot id="uusiOpiskeluoikeus">
      <Modal className="UusiOpiskeluoikeusDialog">
        <ModalTitle>{t('Opiskeluoikeuden lisäys')}</ModalTitle>
        <ModalBody>
          <UusiOpiskeluoikeusForm onResult={setResult} />
        </ModalBody>
        <ModalFooter>
          <FlatButton onClick={props.onClose} testId="cancel">
            {t('Peruuta')}
          </FlatButton>
          <RaisedButton onClick={onSubmit} disabled={!result} testId="submit">
            {t('Lisää opiskeluoikeus')}
          </RaisedButton>
        </ModalFooter>
      </Modal>
    </TestIdRoot>
  )
}
