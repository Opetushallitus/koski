import { NonEmptyArray } from 'fp-ts/lib/NonEmptyArray'
import React, { useCallback, useMemo } from 'react'
import { useAllowedStrings } from '../../appstate/constraints'
import { useKoodisto } from '../../appstate/koodisto'
import { todayISODate } from '../../date/date'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Opiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/Opiskeluoikeusjakso'
import { KoodiarvotOf } from '../../util/koodisto'
// import { OpiskeluoikeusjaksoOf } from '../../util/schema'
import { ClassOf } from '../../util/types'
import { common, CommonProps } from '../CommonProps'
import { Label } from '../containers/Label'
import { Modal, ModalBody, ModalFooter, ModalTitle } from '../containers/Modal'
import { DateEdit, DateView } from '../controls/DateField'
import { FlatButton } from '../controls/FlatButton'
import { RadioButtonsEdit } from '../controls/RadioButtons'
import { RaisedButton } from '../controls/RaisedButton'
import { FormField, Nothing } from '../forms/FormField'
import { useForm } from '../forms/FormModel'
import { ValidationError } from '../forms/validator'

export type UusiOpiskeluoikeudenTilaModalProps<T extends Opiskeluoikeusjakso> =
  CommonProps<{
    onSubmit: (
      form: UusiOpiskeluoikeusjakso<T>
    ) => NonEmptyArray<ValidationError> | undefined
    onClose: () => void
    opiskeluoikeusjaksoClass: ClassOf<Opiskeluoikeusjakso>
  }>

export type UusiOpiskeluoikeusjakso<T extends Opiskeluoikeusjakso> = {
  alku: string
  tila: OpiskeluoikeudenTilakoodi<KoodiarvotOf<T['tila']>>
}

const KOODISTOURI_OPISKELUOIKEUDEN_TILA = 'koskiopiskeluoikeudentila'
type TilaKoodistoUri = typeof KOODISTOURI_OPISKELUOIKEUDEN_TILA

type OpiskeluoikeudenTilakoodi<S extends string = string> = Koodistokoodiviite<
  TilaKoodistoUri,
  S
>

const useInitialOpiskelujaksoForm = <T extends Opiskeluoikeusjakso>(
  opiskeluoikeusjaksoClass: ClassOf<T>
) =>
  useMemo<UusiOpiskeluoikeusjakso<T>>(
    () => ({
      alku: todayISODate(),
      tila: Koodistokoodiviite({
        koodistoUri: KOODISTOURI_OPISKELUOIKEUDEN_TILA,
        koodiarvo: defaultTila(opiskeluoikeusjaksoClass)
      })
    }),
    [opiskeluoikeusjaksoClass]
  )

export const UusiOpiskeluoikeudenTilaModal = <T extends Opiskeluoikeusjakso>(
  props: UusiOpiskeluoikeudenTilaModalProps<T>
) => {
  const koodiarvot = useAllowedStrings(
    props.opiskeluoikeusjaksoClass,
    'tila.koodiarvo'
  )?.filter((k) => k !== 'mitatoity')

  const tilat = useKoodisto(KOODISTOURI_OPISKELUOIKEUDEN_TILA, koodiarvot)?.map(
    (k) => k.koodiviite
  )

  const tilaOptions = useMemo(
    () =>
      tilat?.map((tila) => ({
        key: tila.koodiarvo,
        label: t(tila.nimi),
        value: tila
      })),
    [tilat]
  )

  const initialState = useInitialOpiskelujaksoForm<T>(
    props.opiskeluoikeusjaksoClass
  )
  const form = useForm(initialState, true)
  const [päivämääräPath, tilaPath] = useMemo(
    () => [form.root.prop('alku'), form.root.prop('tila')],
    [form.root]
  )

  const onSubmit = useCallback(() => {
    props.onSubmit(form.state) // TODO: Käsittele onSubmitin mahdollisesti palauttamat virheet
  }, [form.state, props])

  return (
    <Modal
      {...common(props, ['UusiOpiskeluoikeudenTilaModal'])}
      onClose={props.onClose}
    >
      <ModalTitle>{'Uusi opiskeluoikeuden tila'}</ModalTitle>

      <ModalBody>
        <Label label="Päivämäärä">
          <FormField
            form={form}
            path={päivämääräPath}
            view={DateView}
            edit={DateEdit}
          />
        </Label>

        <Label label="Tila">
          <FormField
            form={form}
            path={tilaPath}
            view={Nothing}
            edit={RadioButtonsEdit}
            editProps={{
              getKey: (tila: Koodistokoodiviite) => tila.koodiarvo,
              options: tilaOptions
            }}
          />
        </Label>
      </ModalBody>

      <ModalFooter>
        <FlatButton onClick={props.onClose}>{'Peruuta'}</FlatButton>
        <RaisedButton onClick={onSubmit}>{'Lisää'}</RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

// Utils

const defaultTila = (cn: ClassOf<Opiskeluoikeusjakso>): string => 'lasna'
