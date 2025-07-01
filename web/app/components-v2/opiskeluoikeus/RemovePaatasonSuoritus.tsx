import React from 'react'
import { CommonProps } from '../CommonProps'
import { Column, ColumnRow } from '../containers/Columns'
import { RemoveArrayItemField } from '../controls/RemoveArrayItemField'
import { FormModel } from '../forms/FormModel'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { ActivePäätasonSuoritus } from '../containers/EditorContainer'

export type RemovePaatasonSuoritusProps<
  S extends Opiskeluoikeus
> = CommonProps<{
  form: FormModel<S>,
  päätasonSuoritus: ActivePäätasonSuoritus<S>,
  removePäätasonSuoritus: () => Promise<void>
}>

export const RemovePaatasonSuoritus = <S extends Opiskeluoikeus>(props: RemovePaatasonSuoritusProps<S>) => {

  return (<ColumnRow>
    <Column span={24} align="right">
      <RemoveArrayItemField
        form={props.form}
        path={props.form.root.prop('suoritukset')}
        removeAt={props.päätasonSuoritus.index}
        label="Poista suoritus"
        onRemove={props.removePäätasonSuoritus}
        confirmation={{
          confirm: 'Vahvista poisto, operaatiota ei voi peruuttaa',
          cancel: 'Peruuta poisto'
        }}
      />
    </Column>
  </ColumnRow>)

}
