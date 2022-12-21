import React from 'react'
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

export const EditBar = <T extends object>(props: EditBarProps<T>) =>
  props.form.editMode ? (
    <FooterBar>
      <ButtonGroup>
        <FlatButton onClick={props.form.cancel}>Peruuta</FlatButton>
        <RaisedButton
          disabled={!props.form.hasChanged || !props.form.isValid}
          onClick={props.onSave}
        >
          Tallenna
        </RaisedButton>
        {!props.form.hasChanged && (
          <span>
            <Trans>Ei tallentamattomia muutoksia</Trans>
          </span>
        )}
        {!props.form.isValid && (
          <span>
            <Trans>Opiskelusuorituksessa on virheitä</Trans>
          </span>
        )}
      </ButtonGroup>
    </FooterBar>
  ) : null
