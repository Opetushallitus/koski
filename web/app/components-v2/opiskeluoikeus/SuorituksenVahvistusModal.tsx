import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import React, { useCallback, useMemo } from 'react'
import { useConstraint } from '../../appstate/constraints'
import { usePreferences } from '../../appstate/preferences'
import { todayISODate } from '../../date/date'
import { HenkilövahvistusPaikkakunnalla } from '../../types/fi/oph/koski/schema/HenkilovahvistusPaikkakunnalla'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from '../../types/fi/oph/koski/schema/HenkilovahvistusValinnaisellaPaikkakunnalla'
import { HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla } from '../../types/fi/oph/koski/schema/HenkilovahvistusValinnaisellaTittelillaJaValinnaisellaPaikkakunnalla'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Organisaatio } from '../../types/fi/oph/koski/schema/Organisaatio'
import { Organisaatiohenkilö } from '../../types/fi/oph/koski/schema/Organisaatiohenkilo'
import { OrganisaatiohenkilöValinnaisellaTittelillä } from '../../types/fi/oph/koski/schema/OrganisaatiohenkiloValinnaisellaTittelilla'
import { Organisaatiovahvistus } from '../../types/fi/oph/koski/schema/Organisaatiovahvistus'
import { Päivämäärävahvistus } from '../../types/fi/oph/koski/schema/Paivamaaravahvistus'
import { Vahvistus } from '../../types/fi/oph/koski/schema/Vahvistus'
import {
  constraintArrayItem,
  constraintObjectClass,
  constraintObjectProp
} from '../../util/constraints'
import {
  AnyOrganisaatiohenkilö,
  castOrganisaatiohenkilö
} from '../../util/henkilo'
import { ClassOf } from '../../util/types'
import { common, CommonProps } from '../CommonProps'
import { Modal, ModalBody, ModalFooter, ModalTitle } from '../containers/Modal'
import { DateEdit, DateView } from '../controls/DateField'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import { FormField } from '../forms/FormField'
import { useForm } from '../forms/FormModel'
import { Trans } from '../texts/Trans'
import { KuntaEdit, KuntaView } from './KuntaField'
import { OrganisaatioEdit, OrganisaatioView } from './OrganisaatioField'
import {
  OrganisaatioHenkilötEdit,
  OrganisaatioHenkilötView
} from './OrganisaatioHenkilotField'

export type SuorituksenVahvistusModalProps<T extends Vahvistus> = CommonProps<{
  vahvistusClass: ClassOf<T>
  organisaatioOid: string
  onSubmit: (form: T) => void
  onCancel: () => void
}>

export type VahvistusForm<T extends AnyOrganisaatiohenkilö> = {
  päivä: string
  paikkakunta?: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio?: Organisaatio
  myöntäjäHenkilöt: Array<T>
}

const initialState = <
  T extends AnyOrganisaatiohenkilö
>(): VahvistusForm<T> => ({
  päivä: todayISODate(),
  myöntäjäHenkilöt: []
})

export const SuorituksenVahvistusModal = <
  T extends Vahvistus,
  S extends AnyOrganisaatiohenkilö
>(
  props: SuorituksenVahvistusModalProps<T>
): React.ReactElement => {
  const vahvistusC = useConstraint(props.vahvistusClass)

  const [storedMyöntäjät, storeMyöntäjä] = usePreferences<Organisaatiohenkilö>(
    props.organisaatioOid,
    'myöntäjät'
  )

  const organisaatiohenkilöClass = useMemo(
    () =>
      pipe(
        vahvistusC,
        constraintObjectProp('myöntäjäHenkilöt'),
        constraintArrayItem,
        constraintObjectClass
      ) as ClassOf<S> | null,
    [vahvistusC]
  )

  const castStoredMyöntäjät = organisaatiohenkilöClass
    ? storedMyöntäjät.map(castOrganisaatiohenkilö(organisaatiohenkilöClass))
    : []

  const form = useForm(initialState<S>(), true, vahvistusC)
  const vahvistus = useMemo(
    () => formDataToVahvistus(form.state, props.vahvistusClass) as T | null,
    [form.state, props.vahvistusClass]
  )

  const [pvmPath, paikkakuntaPath, organisaatioPath, myöntäjäHenkilötPath] =
    useMemo(
      () => [
        form.root.prop('päivä'),
        form.root.prop('paikkakunta'),
        form.root.prop('myöntäjäOrganisaatio'),
        form.root.prop('myöntäjäHenkilöt')
      ],
      []
    )

  const onSubmit = useCallback(() => {
    if (vahvistus) {
      props.onSubmit(vahvistus)
    }
  }, [vahvistus])

  return (
    <Modal
      {...common(props, ['SuorituksenVahvistusModal'])}
      onClose={props.onCancel}
    >
      <ModalTitle>
        <Trans>Suoritus valmis</Trans>
      </ModalTitle>
      <ModalBody>
        <label>
          Päivämäärä
          <FormField
            form={form}
            path={pvmPath}
            view={DateView}
            edit={DateEdit}
          />
        </label>

        <label>
          Paikkakunta
          <FormField
            form={form}
            path={paikkakuntaPath}
            optional
            view={KuntaView}
            edit={KuntaEdit}
          />
        </label>

        <label>
          Myöntäjäorganisaatio
          <FormField
            form={form}
            path={organisaatioPath}
            optional
            view={OrganisaatioView}
            edit={OrganisaatioEdit}
          />
        </label>

        {organisaatiohenkilöClass && (
          <label>
            Myöntäjät
            <FormField
              form={form}
              path={myöntäjäHenkilötPath}
              view={OrganisaatioHenkilötView}
              edit={OrganisaatioHenkilötEdit}
              editProps={
                {
                  henkilöClass: organisaatiohenkilöClass,
                  organisaatio: form.state.myöntäjäOrganisaatio,
                  storedHenkilöt: castStoredMyöntäjät
                } as const
              }
            />
          </label>
        )}
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={props.onCancel}>Peruuta</FlatButton>
        <RaisedButton onClick={onSubmit} disabled={!form.errors || !vahvistus}>
          Vahvista suoritus
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

const formDataToVahvistus = <T extends AnyOrganisaatiohenkilö>(
  {
    päivä,
    paikkakunta,
    myöntäjäOrganisaatio,
    myöntäjäHenkilöt
  }: VahvistusForm<T>,
  c: ClassOf<Vahvistus>
): Vahvistus | null => {
  if (myöntäjäOrganisaatio) {
    if (
      c === 'fi.oph.koski.schema.HenkilövahvistusPaikkakunnalla' &&
      paikkakunta &&
      A.isNonEmpty(myöntäjäHenkilöt)
    ) {
      return HenkilövahvistusPaikkakunnalla({
        päivä,
        paikkakunta,
        myöntäjäOrganisaatio,
        myöntäjäHenkilöt: myöntäjäHenkilöt as Organisaatiohenkilö[]
      })
    }

    if (
      c === 'fi.oph.koski.schema.HenkilövahvistusValinnaisellaPaikkakunnalla' &&
      myöntäjäOrganisaatio &&
      A.isNonEmpty(myöntäjäHenkilöt)
    ) {
      return HenkilövahvistusValinnaisellaPaikkakunnalla({
        päivä,
        paikkakunta,
        myöntäjäOrganisaatio
      })
    }

    if (
      c ===
        'fi.oph.koski.schema.HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla' &&
      myöntäjäOrganisaatio &&
      A.isNonEmpty(myöntäjäHenkilöt)
    ) {
      return HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla(
        {
          päivä,
          paikkakunta,
          myöntäjäOrganisaatio,
          myöntäjäHenkilöt:
            myöntäjäHenkilöt as OrganisaatiohenkilöValinnaisellaTittelillä[]
        }
      )
    }

    if (
      c === 'fi.oph.koski.schema.Organisaatiovahvistus' &&
      myöntäjäOrganisaatio &&
      paikkakunta
    ) {
      return Organisaatiovahvistus({ päivä, paikkakunta, myöntäjäOrganisaatio })
    }

    if (c === 'fi.oph.koski.schema.Päivämäärävahvistus') {
      return Päivämäärävahvistus({ päivä, myöntäjäOrganisaatio })
    }
  }
  return null
}
