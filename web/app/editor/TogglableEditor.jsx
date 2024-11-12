import React from 'react'
import * as R from 'ramda'
import { contextualizeModel } from './EditorModel.ts'
import { currentLocation } from '../util/location'
import Text from '../i18n/Text'
import { modelData } from './EditorModel'
import InvalidateOpiskeluoikeusButton from '../opiskeluoikeus/InvalidateOpiskeluoikeusButton'
import { RequiresLahdejarjestelmakytkennanPurkaminenAccess } from '../components-v2/access/RequiresLahdejarjestelmakytkennanPurkaminenAccess.tsx'
import { useVirkailijaUser } from '../appstate/user.tsx'
import ButtonWithConfirmation from '../components/ButtonWithConfirmation'
import { puraLähdejärjestelmänKytkentä } from '../virkailija/VirkailijaOppijaView.jsx'

export class TogglableEditor extends React.Component {
  render() {
    const { model, renderChild, ...rest } = this.props
    const context = model.context
    const opiskeluoikeusOid = modelData(model.context.opiskeluoikeus, 'oid')
    const edit =
      opiskeluoikeusOid && currentLocation().params.edit === opiskeluoikeusOid
    const editingAny = !!currentLocation().params.edit
    const modifiedContext = R.mergeRight(context, { edit })
    const showEditLink = model.editable && !editingAny
    const showDeleteLink = model.invalidatable && !showEditLink

    const editLink = showEditLink ? (
      <button
        className="koski-button toggle-edit"
        onClick={() => context.editBus.push(opiskeluoikeusOid)}
        {...rest}
      >
        <Text name="muokkaa" />
      </button>
    ) : (
      <div className="invalidate-etc-buttons">
        {showDeleteLink && (
          <div>
            <InvalidateOpiskeluoikeusButton
              opiskeluoikeus={model.context.opiskeluoikeus}
            />
          </div>
        )}
        <PuraLähdejärjestelmänKytkentäButton
          opiskeluoikeus={modelData(model)}
        />
      </div>
    )

    return renderChild(contextualizeModel(model, modifiedContext), editLink)
  }
}

const PuraLähdejärjestelmänKytkentäButton = (props) => (
  <RequiresLahdejarjestelmakytkennanPurkaminenAccess
    opiskeluoikeus={props.opiskeluoikeus}
  >
    <div>
      <ButtonWithConfirmation
        text="Pura lähdejärjestelmäkytkentä"
        confirmationText="Vahvista lähdejärjestelmäkytkennän purkaminen, operaatiota ei voi peruuttaa"
        cancelText="Peruuta"
        action={() => puraLähdejärjestelmänKytkentä(props.opiskeluoikeus.oid)}
        className="pura-kytkenta"
        confirmationClassName="confirm-pura-kytkenta"
      />
    </div>
  </RequiresLahdejarjestelmakytkennanPurkaminenAccess>
)
