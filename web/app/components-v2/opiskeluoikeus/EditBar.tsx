import React, { useCallback } from 'react'
import { useGlobalErrors } from '../../appstate/globalErrors'
import { ButtonGroup } from '../containers/ButtonGroup'
import { FooterBar } from '../containers/FooterBar'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import { FormModel } from '../forms/FormModel'
import { Trans } from '../texts/Trans'
import { TestIdRoot, TestIdText } from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'

export type EditBarProps<T extends object> = {
  form: FormModel<T>
  onSave: () => void
}

export const EditBar = <T extends object>(props: EditBarProps<T>) => {
  const errors = useGlobalErrors()

  const save = useCallback(() => {
    errors.clearAll()
    props.onSave()
  }, [errors, props])

  const cancel = useCallback(() => {
    errors.clearAll()
    props.form.cancel()
  }, [errors, props.form])

  return props.form.editMode ? (
    <FooterBar>
      <ButtonGroup>
        <FlatButton
          disabled={props.form.isSaving}
          onClick={cancel}
          testId="cancelEdit"
        >
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={
            props.form.isSaving || !props.form.hasChanged || !props.form.isValid
          }
          onClick={save}
          testId="save"
        >
          {t('Tallenna')}
        </RaisedButton>
        {!props.form.hasChanged && (
          <TestIdText id="editStatus">
            <Trans>{'Ei tallentamattomia muutoksia'}</Trans>
          </TestIdText>
        )}
        {!props.form.isValid && (
          <TestIdText id="editStatus">
            <Trans>{'Korjaa virheelliset tiedot.'}</Trans>
          </TestIdText>
        )}
      </ButtonGroup>
    </FooterBar>
  ) : null
}
