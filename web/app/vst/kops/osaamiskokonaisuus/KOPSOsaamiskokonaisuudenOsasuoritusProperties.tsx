import React from 'react'
import { LocalizedTextView } from '../../../components-v2/controls/LocalizedTestField'
import { FormField } from '../../../components-v2/forms/FormField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../../../components-v2/forms/FormModel'
import {
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../../../components-v2/opiskeluoikeus/ArvosanaField'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../../../components-v2/opiskeluoikeus/LaajuusField'
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi } from '../../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenArviointi'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus } from '../../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOsaamiskokonaisuudenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { deleteAt } from '../../../util/array'
import { createArviointi } from '../../common/arviointi'
import {
  ArviointiProperty,
  KuvausProperty,
  TunnustettuProperty
} from '../../common/propertyFields'
import { VSTSuoritusOsasuorituksilla } from '../../common/types'
import { AddOpintokokonaisuudenSuoritus } from './AddOpintokokonaisuudenSuoritus'

export type KOPSOsaamiskokonaisuudenOsasuoritusPropertiesProps = {
  osasuoritusIndex: number
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTSuoritusOsasuorituksilla
  >
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus
  >
  organisaatioOid: string
  testId: string
}

export const KOPSOsaamiskokonaisuudenOsasuoritusProperties: React.FC<
  KOPSOsaamiskokonaisuudenOsasuoritusPropertiesProps
> = (props) => {
  const osasuoritus = getValue(props.osasuoritusPath)(props.form.state)

  return (
    <OsasuoritusTable
      editMode={props.form.editMode}
      addNewOsasuoritusView={AddOpintokokonaisuudenSuoritus}
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
        return osaamiskokonaisuudenOsasuoritusToTableRow({
          form: props.form,
          suoritusPath: props.osasuoritusPath,
          suoritusIndex: props.osasuoritusIndex,
          osasuoritusIndex: osasuoritusIndex
        })
      })}
    />
  )
}

export type OsaamiskokonaisuudenOsasuoritusToTableRowParams = {
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus
  >
  suoritusIndex: number
  osasuoritusIndex: number
}

export const osaamiskokonaisuudenOsasuoritusToTableRow = ({
  suoritusPath,
  suoritusIndex,
  osasuoritusIndex,
  form
}: OsaamiskokonaisuudenOsasuoritusToTableRowParams): OsasuoritusRowData<
  'Osasuoritus' | 'Laajuus' | 'Arvosana'
> => {
  const osasuoritusPath = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)

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
      Arvosana: (
        <FormField
          form={form}
          path={osasuoritusPath.path('arviointi')}
          view={ParasArvosanaView}
          edit={(arvosanaProps) => (
            <ParasArvosanaEdit
              {...arvosanaProps}
              createArviointi={createArviointi(
                OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi
              )}
            />
          )}
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
    content: (
      <>
        <KuvausProperty form={form} path={osasuoritusPath} />
        <ArviointiProperty form={form} path={osasuoritusPath} />
        <TunnustettuProperty form={form} path={osasuoritusPath} />
      </>
    )
  }
}
