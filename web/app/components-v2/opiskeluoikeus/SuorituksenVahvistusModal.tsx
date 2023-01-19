import React, { useMemo } from 'react'
import { useConstraint } from '../../appstate/constraints'
import { todayISODate } from '../../date/date'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { Organisaatio } from '../../types/fi/oph/koski/schema/Organisaatio'
import { OrganisaatiohenkilöValinnaisellaTittelillä } from '../../types/fi/oph/koski/schema/OrganisaatiohenkiloValinnaisellaTittelilla'
import { Vahvistus } from '../../types/fi/oph/koski/schema/Vahvistus'
import { ClassOf } from '../../util/types'
import { common, CommonProps } from '../CommonProps'
import { Modal, ModalBody, ModalTitle } from '../containers/Modal'
import { DateEdit, DateView } from '../controls/DateField'
import { FormField } from '../forms/FormField'
import { useForm } from '../forms/FormModel'
import { Trans } from '../texts/Trans'
import { KuntaEdit, KuntaView } from './KuntaField'
import { OrganisaatioEdit, OrganisaatioView } from './OrganisaatioField'

export type SuorituksenVahvistusModalProps<T extends Vahvistus> = CommonProps<{
  vahvistusClass: ClassOf<T>
}>

export type VahvistusForm = {
  päivä: string
  paikkakunta?: Koodistokoodiviite<'kunta', string>
  myöntäjäOrganisaatio?: Organisaatio
  myöntäjäHenkilöt?: Array<OrganisaatiohenkilöValinnaisellaTittelillä>
}

const initialState: VahvistusForm = {
  päivä: todayISODate()
}

export const SuorituksenVahvistusModal = <T extends Vahvistus>(
  props: SuorituksenVahvistusModalProps<T>
): React.ReactElement => {
  const constraint = useConstraint(props.vahvistusClass)
  const form = useForm(initialState, true, constraint)

  const pvmPath = useMemo(() => form.root.prop('päivä'), [])
  const paikkakuntaPath = useMemo(() => form.root.prop('paikkakunta'), [])
  const organisaatioPath = useMemo(
    () => form.root.prop('myöntäjäOrganisaatio'),
    []
  )

  return (
    <Modal {...common(props, ['SuorituksenVahvistusModal'])}>
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
      </ModalBody>
    </Modal>
  )
}
