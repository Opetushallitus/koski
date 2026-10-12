import React from 'react'
import * as R from 'ramda'
import { contextualizeModel } from './EditorModel.ts'
import { currentLocation } from '../util/location'
import Text from '../i18n/Text'
import { modelData } from './EditorModel'
import InvalidateOpiskeluoikeusButton from '../opiskeluoikeus/InvalidateOpiskeluoikeusButton'
import { RequiresLahdejarjestelmakytkennanPurkaminenAccess } from '../components-v2/access/RequiresLahdejarjestelmakytkennanPurkaminenAccess.tsx'
import { useVirkailijaUser } from '../appstate/user.tsx'
import {
  useMuokataanToistaOpiskeluoikeutta,
  useMuokattavaOpiskeluoikeus
} from '../appstate/sivunTila.ts'
import ButtonWithConfirmation from '../components/ButtonWithConfirmation'
import { puraLähdejärjestelmänKytkentä } from '../virkailija/VirkailijaOppijaView.jsx'

export const TogglableEditor = ({ model, renderChild, ...rest }) => {
  const muokattava = useMuokattavaOpiskeluoikeus()
  const hasAnyInvalidateAccess = useVirkailijaUser()?.hasAnyInvalidateAccess
  const context = model.context
  const opiskeluoikeusOid = modelData(model.context.opiskeluoikeus, 'oid')
  const muokataanToista = useMuokataanToistaOpiskeluoikeutta(opiskeluoikeusOid)
  const edit =
    opiskeluoikeusOid && currentLocation().params.edit === opiskeluoikeusOid
  const editingAny = !!currentLocation().params.edit || !!muokattava
  const modifiedContext = R.mergeRight(context, { edit })
  const showEditLink = model.editable && !editingAny
  // Mitätöintioikeudellinen (esim. pääkäyttäjä) mitätöi myös katselutilassa,
  // muut muokkaustilassa.
  const showDeleteLink =
    model.invalidatable &&
    !muokataanToista &&
    (!showEditLink || hasAnyInvalidateAccess)

  const editLink = (
    <div className="opiskeluoikeus-buttons">
      {showDeleteLink && (
        <InvalidateOpiskeluoikeusButton
          opiskeluoikeus={model.context.opiskeluoikeus}
        />
      )}
      {!showEditLink && !muokataanToista && (
        <PuraLähdejärjestelmänKytkentäButton
          opiskeluoikeus={modelData(model)}
        />
      )}
      {showEditLink && (
        <button
          className="koski-button toggle-edit"
          onClick={() => context.editBus.push(opiskeluoikeusOid)}
          {...rest}
        >
          <Text name="muokkaa" />
        </button>
      )}
    </div>
  )

  return renderChild(contextualizeModel(model, modifiedContext), editLink)
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
