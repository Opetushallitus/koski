import React, { useState } from 'react'
import { useApiMethod, useOnApiError, useOnApiSuccess } from '../../api-fetch'
import { useGlobalErrors } from '../../appstate/globalErrors'
import { useVersionumero } from '../../appstate/sivunTila'
import { setInvalidationNotification } from '../../components/InvalidationNotification'
import { t } from '../../i18n/i18n'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { PäätasonSuoritus } from '../../types/fi/oph/koski/schema/PaatasonSuoritus'
import { deletePäätasonSuoritus } from '../../util/koskiApi'
import {
  getOpiskeluoikeusOid,
  getVersionumero
} from '../../util/opiskeluoikeus'
import { CommonProps } from '../CommonProps'
import { useWriteAccess } from '../access/RequiresWriteAccess'
import { Column, ColumnRow } from '../containers/Columns'
import { ActivePäätasonSuoritus } from '../containers/EditorContainer'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import { RemoveArrayItemField } from '../controls/RemoveArrayItemField'
import { FormModel } from '../forms/FormModel'

export type RemovePaatasonSuoritusProps<S extends Opiskeluoikeus> =
  CommonProps<{
    form: FormModel<S>
    päätasonSuoritus: ActivePäätasonSuoritus<S>
    removePäätasonSuoritus: () => Promise<void>
    invalidatable: boolean
  }>

const confirmation = {
  confirm: 'Vahvista poisto, operaatiota ei voi peruuttaa',
  cancel: 'Peruuta poisto'
}

export const RemovePaatasonSuoritus = <S extends Opiskeluoikeus>(
  props: RemovePaatasonSuoritusProps<S>
) => {
  const voiMuokata = useWriteAccess(props.form.state)
  const inVersiohistoria =
    useVersionumero(getOpiskeluoikeusOid(props.form.state)) !== null

  // Kuten vanhassa käyttöliittymässä: muokkausoikeudellinen poistaa
  // suorituksen muokkaustilassa, mutta mitätöintioikeudellinen, joka ei voi
  // muokata opiskeluoikeutta (esim. oppilaitoksen pääkäyttäjä tai
  // lähdejärjestelmästä siirretty opiskeluoikeus), katselutilassa.
  const katselutilassa =
    !props.form.editMode &&
    !inVersiohistoria &&
    props.invalidatable &&
    !voiMuokata

  if (!props.form.editMode && !katselutilassa) {
    return null
  }

  return (
    <ColumnRow>
      <Column span={24} align="right">
        {katselutilassa ? (
          <TallennetunSuorituksenPoisto
            opiskeluoikeus={props.form.state}
            suoritus={props.päätasonSuoritus.suoritus}
          />
        ) : (
          <RemoveArrayItemField
            form={props.form}
            path={props.form.root.prop('suoritukset')}
            removeAt={props.päätasonSuoritus.index}
            label="Poista suoritus"
            onRemove={props.removePäätasonSuoritus}
            confirmation={confirmation}
          />
        )}
      </Column>
    </ColumnRow>
  )
}

// Katselutilassa ei ole lomakkeen tallennusta, jonka kautta muokkaustilan
// poisto kulkee, joten suoritus poistetaan suoraan ja sivu ladataan uudelleen
// kuten vanhassa käyttöliittymässä.
const TallennetunSuorituksenPoisto: React.FC<{
  opiskeluoikeus: Opiskeluoikeus
  suoritus: PäätasonSuoritus
}> = (props) => {
  const poisto = useApiMethod(deletePäätasonSuoritus)
  const [confirmationVisible, setConfirmationVisible] = useState(false)
  const { push: näytäVirheet } = useGlobalErrors()
  useOnApiSuccess(poisto, () => {
    setInvalidationNotification('Suoritus poistettu')
    location.reload()
  })
  useOnApiError(poisto, (virhe) => {
    setConfirmationVisible(false)
    näytäVirheet(virhe.errors.map((e) => ({ message: t(e.messageKey) })))
  })

  const oid = getOpiskeluoikeusOid(props.opiskeluoikeus)
  const versionumero = getVersionumero(props.opiskeluoikeus)
  if (!oid || versionumero === undefined) {
    return null
  }

  return confirmationVisible ? (
    <>
      <RaisedButton
        type="dangerzone"
        onClick={() => poisto.call(oid, versionumero, props.suoritus)}
        testId="confirm"
      >
        {t(confirmation.confirm)}
      </RaisedButton>
      <FlatButton onClick={() => setConfirmationVisible(false)} testId="cancel">
        {t(confirmation.cancel)}
      </FlatButton>
    </>
  ) : (
    <FlatButton onClick={() => setConfirmationVisible(true)} testId="button">
      {t('Poista suoritus')}
    </FlatButton>
  )
}
