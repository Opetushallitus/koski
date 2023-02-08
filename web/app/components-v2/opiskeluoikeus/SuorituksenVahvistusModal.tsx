import * as A from 'fp-ts/Array'
import React, { useCallback, useMemo } from 'react'
import { useChildClassName, useSchema } from '../../appstate/constraints'
import { usePreferences } from '../../appstate/preferences'
import { todayISODate } from '../../date/date'
import { localize } from '../../i18n/i18n'
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
  AnyOrganisaatiohenkilö,
  castOrganisaatiohenkilö,
  OrganisaatiohenkilöEq
} from '../../util/henkilo'
import {
  getOrganisaationKotipaikka,
  getOrganisaatioOid
} from '../../util/organisaatiot'
import { isHenkilövahvistus } from '../../util/schema'
import { ClassOf } from '../../util/types'
import { common, CommonProps } from '../CommonProps'
import { Label } from '../containers/Label'
import { Modal, ModalBody, ModalFooter, ModalTitle } from '../containers/Modal'
import { DateEdit, DateView } from '../controls/DateField'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import { FormField, sideUpdate } from '../forms/FormField'
import { useForm } from '../forms/FormModel'
import { Trans } from '../texts/Trans'
import { KuntaEdit, Kuntakoodiviite, KuntaView } from './KuntaField'
import { OrganisaatioEdit, OrganisaatioView } from './OrganisaatioField'
import {
  OrganisaatioHenkilötEdit,
  OrganisaatioHenkilötView
} from './OrganisaatioHenkilotField'

export type SuorituksenVahvistusModalProps<T extends Vahvistus> = CommonProps<{
  vahvistusClass: ClassOf<T>
  organisaatio: Organisaatio
  onSubmit: (form: T) => void
  onCancel: () => void
}>

export type VahvistusForm<T extends AnyOrganisaatiohenkilö> = {
  päivä: string
  paikkakunta?: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio?: Organisaatio
  myöntäjäHenkilöt: Array<T>
}

const initialState = <T extends AnyOrganisaatiohenkilö>(
  organisaatio: Organisaatio
): VahvistusForm<T> => ({
  päivä: todayISODate(),
  myöntäjäHenkilöt: [],
  myöntäjäOrganisaatio: organisaatio,
  paikkakunta: getOrganisaationKotipaikka(organisaatio)
})

export const SuorituksenVahvistusModal = <
  V extends Vahvistus,
  H extends AnyOrganisaatiohenkilö
>(
  props: SuorituksenVahvistusModalProps<V>
): React.ReactElement => {
  const vahvistusSchema = useSchema(props.vahvistusClass)

  const organisaatiohenkilöClass = useChildClassName<H>(
    props.vahvistusClass,
    'myöntäjäHenkilöt.[]'
  )

  const {
    preferences: storedMyöntäjät,
    store: storeMyöntäjä,
    remove: removeMyöntäjä
  } = usePreferences<Organisaatiohenkilö>(
    getOrganisaatioOid(props.organisaatio),
    'myöntäjät'
  )

  const castStoredMyöntäjät = organisaatiohenkilöClass
    ? storedMyöntäjät.map(castOrganisaatiohenkilö(organisaatiohenkilöClass))
    : []

  const form = useForm(
    initialState<H>(props.organisaatio),
    true,
    vahvistusSchema
  )
  const vahvistus = useMemo(
    () => formDataToVahvistus(form.state, props.vahvistusClass) as V | null,
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
      [form.root]
    )

  const onSubmit = useCallback(() => {
    if (vahvistus) {
      if (isHenkilövahvistus(vahvistus)) {
        A.difference(OrganisaatiohenkilöEq)(storedMyöntäjät)(
          vahvistus.myöntäjäHenkilöt
        ).forEach((henkilö) =>
          storeMyöntäjä(
            henkilö.nimi,
            Organisaatiohenkilö({
              ...henkilö,
              titteli: henkilö.titteli || localize('-')
            })
          )
        )
      }
      props.onSubmit(vahvistus)
    }
  }, [vahvistus, props, storedMyöntäjät, storeMyöntäjä])

  const updatePaikkakuntaByOrganisaatio = useMemo(
    () =>
      sideUpdate<VahvistusForm<H>, Organisaatio, Kuntakoodiviite | undefined>(
        paikkakuntaPath,
        (org) => org && getOrganisaationKotipaikka(org)
      ),
    [paikkakuntaPath]
  )

  const onRemoveStoredHenkilö = useCallback(
    (henkilö: AnyOrganisaatiohenkilö) => {
      removeMyöntäjä(henkilö.nimi)
    },
    [removeMyöntäjä]
  )

  return (
    <Modal
      {...common(props, ['SuorituksenVahvistusModal'])}
      onClose={props.onCancel}
    >
      <ModalTitle>
        <Trans>{'Suoritus valmis'}</Trans>
      </ModalTitle>
      <ModalBody>
        <Label label="Päivämäärä">
          <FormField
            form={form}
            path={pvmPath}
            view={DateView}
            edit={DateEdit}
          />
        </Label>

        <Label label="Paikkakunta">
          <FormField
            form={form}
            path={paikkakuntaPath}
            optional
            view={KuntaView}
            edit={KuntaEdit}
          />
        </Label>

        <Label label="Organisaatio">
          <FormField
            form={form}
            path={organisaatioPath}
            updateAlso={[updatePaikkakuntaByOrganisaatio]}
            optional
            view={OrganisaatioView}
            edit={OrganisaatioEdit}
          />
        </Label>

        {organisaatiohenkilöClass && (
          <Label label="Myöntäjät">
            <FormField
              form={form}
              path={myöntäjäHenkilötPath}
              view={OrganisaatioHenkilötView}
              edit={OrganisaatioHenkilötEdit}
              editProps={{
                henkilöClass: organisaatiohenkilöClass,
                organisaatio: form.state.myöntäjäOrganisaatio,
                storedHenkilöt: castStoredMyöntäjät,
                onRemoveStoredHenkilö
              }}
            />
          </Label>
        )}
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={props.onCancel}>{'Peruuta'}</FlatButton>
        <RaisedButton
          onClick={onSubmit}
          disabled={A.isNonEmpty(form.errors) || !vahvistus}
        >
          {'Merkitse valmiiksi'}
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
