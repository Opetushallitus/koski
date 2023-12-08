import React from 'react'
import { LocalizedTextView } from '../../components-v2/controls/LocalizedTestField'
import { FormField } from '../../components-v2/forms/FormField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../../components-v2/forms/FormModel'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../../components-v2/opiskeluoikeus/LaajuusField'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutuksenSuoritus'
import { isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonValinnaistenSuuntautumisopintojenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { deleteAt } from '../../util/array'
import { VSTSuoritusOsasuorituksilla } from '../common/types'
import { AddKOPSOsasuoritus } from './AddKOPSOsasuoritus'
import { KOPSOsaamiskokonaisuudenOsasuoritusProperties } from './osaamiskokonaisuus/KOPSOsaamiskokonaisuudenOsasuoritusProperties'
import { KOPSSuuntautumisopintojenOsasuoritusProperties } from './valinnainenSuuntautumisopinto/KOPSSuuntautumisopintojenOsasuoritusProperties'

type KOPSOsasuoritusPropertiesProps = {
  osasuoritusIndex: number
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTSuoritusOsasuorituksilla
  >
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus
  >
  testId: string
}

export const KOPSOsasuoritusProperties: React.FC<
  KOPSOsasuoritusPropertiesProps
> = (props) => {
  const osasuoritus = getValue(props.osasuoritusPath)(props.form.state)

  return (
    <OsasuoritusTable
      editMode={props.form.editMode}
      addNewOsasuoritusView={AddKOPSOsasuoritus}
      addNewOsasuoritusViewProps={{
        form: props.form,
        path: props.osasuoritusPath
      }}
      onRemove={(i) => {
        props.form.updateAt(
          props.osasuoritusPath.prop('osasuoritukset').optional(),
          deleteAt(i)
        )
      }}
      rows={(osasuoritus?.osasuoritukset || []).map((_os, osasuoritusIndex) => {
        return kopsOsasuoritusToTableRow({
          level: props.level + 1,
          form: props.form,
          osasuoritusPath: props.osasuoritusPath,
          // @ts-expect-error
          suoritusPath: props.osasuoritusPath,
          osasuoritusIndex: osasuoritusIndex,
          suoritusIndex: props.osasuoritusIndex
        })
      })}
    />
  )
}

export type KOPSOsasuoritusToTableRowParams = {
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTSuoritusOsasuorituksilla
  >
  suoritusIndex: number
  osasuoritusIndex: number
}

export const kopsOsasuoritusToTableRow = ({
  suoritusPath,
  suoritusIndex,
  osasuoritusIndex,
  form,
  level
}: KOPSOsasuoritusToTableRowParams): OsasuoritusRowData<
  'Osasuoritus' | 'Laajuus'
> => {
  const osasuoritusPath = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)
  const osasuoritus = getValue(osasuoritusPath)(form.state)

  return {
    suoritusIndex,
    osasuoritusIndex,
    osasuoritusPath: suoritusPath.prop('osasuoritukset').optional(),
    expandable: true,
    columns: {
      Osasuoritus: (
        <FormField
          form={form}
          path={osasuoritusPath.path('koulutusmoduuli.tunniste.nimi')}
          view={LocalizedTextView}
          testId="nimi"
        />
      ),
      Laajuus: (
        <FormField
          form={form}
          path={osasuoritusPath.path('koulutusmoduuli.laajuus')}
          view={LaajuusView}
          edit={LaajuusOpintopisteissäEdit}
        />
      )
    },
    content:
      isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
        osasuoritus
      ) ? (
        <KOPSSuuntautumisopintojenOsasuoritusProperties
          level={level}
          osasuoritusIndex={osasuoritusIndex}
          form={form}
          suoritusPath={suoritusPath}
          // @ts-expect-error Korjaa tyypitys
          osasuoritusPath={osasuoritusPath}
        />
      ) : (
        <KOPSOsaamiskokonaisuudenOsasuoritusProperties
          level={level}
          osasuoritusIndex={osasuoritusIndex}
          form={form}
          suoritusPath={suoritusPath}
          // @ts-expect-error Korjaa tyypitys
          osasuoritusPath={osasuoritusPath}
        />
      )
  }
}
