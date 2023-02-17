import React, { useCallback } from 'react'
import { useGlobalErrors } from '../../appstate/globalErrors'
import { testId } from '../CommonProps'
import { ButtonGroup } from '../containers/ButtonGroup'
import { FooterBar } from '../containers/FooterBar'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import { FormModel } from '../forms/FormModel'
import { Trans } from '../texts/Trans'

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
        <FlatButton onClick={cancel} testId="opiskeluoikeus.cancelEdit">
          {'Peruuta'}
        </FlatButton>
        <RaisedButton
          disabled={!props.form.hasChanged || !props.form.isValid}
          onClick={save}
          testId="opiskeluoikeus.save"
        >
          {'Tallenna'}
        </RaisedButton>
        {!props.form.hasChanged && (
          <span {...testId({ testId: 'opiskeluoikeus.editStatus' })}>
            <Trans>{'Ei tallentamattomia muutoksia'}</Trans>
          </span>
        )}
        {!props.form.isValid && (
          <span {...testId({ testId: 'opiskeluoikeus.editStatus' })}>
            <Trans>{'Korjaa virheelliset tiedot.'}</Trans>
          </span>
        )}
      </ButtonGroup>
    </FooterBar>
  ) : null
}
