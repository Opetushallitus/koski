import React from 'react'
import { LocalizedTextView } from '../../components-v2/controls/LocalizedTestField'
import { FormField } from '../../components-v2/forms/FormField'
import {
  FormModel,
  FormOptic,
  getValue
} from '../../components-v2/forms/FormModel'
import {
  koodinNimiOnly,
  ParasArvosanaEdit,
  ParasArvosanaView
} from '../../components-v2/opiskeluoikeus/ArvosanaField'
import {
  LaajuusOpintopisteissäEdit,
  LaajuusView
} from '../../components-v2/opiskeluoikeus/LaajuusField'
import { OsasuoritusRowData } from '../../components-v2/opiskeluoikeus/OsasuoritusTable'
import {
  TaitotasoEdit,
  TaitotasoView
} from '../../components-v2/opiskeluoikeus/TaitotasoField'
import { VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VSTSuoritusOsasuorituksilla } from '../common/types'
import { VSTLukutaitoKielitaitotasoProperty } from './VSTLukutaitoKielitaitotasoField'

type VSTLukutaitoPropertiesProps = {
  osasuoritusIndex: number
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus
  >
  testId: string
}

export const VSTLukutaitoProperties: React.FC<VSTLukutaitoPropertiesProps> = (
  props
) => (
  <VSTLukutaitoKielitaitotasoProperty
    form={props.form}
    path={props.suoritusPath}
  />
)

export type OsasuoritusToTableRowParams = {
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTSuoritusOsasuorituksilla
  >
  suoritusIndex: number
  osasuoritusIndex: number
}

export const osasuoritusToTableRow = ({
  suoritusPath,
  suoritusIndex,
  osasuoritusIndex,
  form,
  level
}: OsasuoritusToTableRowParams): OsasuoritusRowData<
  'Osasuoritus' | 'Laajuus' | 'Arvosana' | 'Taitotaso'
> => {
  const osasuoritusPath = suoritusPath
    .prop('osasuoritukset')
    .optional()
    .at(osasuoritusIndex)
  const osasuoritus = getValue(osasuoritusPath)(form.state)

  return {
    suoritusIndex,
    osasuoritusIndex,
    osasuoritusPath: suoritusPath.prop('osasuoritukset'),
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
      ),
      Arvosana: (
        <FormField
          form={form}
          path={osasuoritusPath.path('arviointi')}
          view={ParasArvosanaView}
          edit={ParasArvosanaEdit}
          editProps={{
            suoritusClassName: osasuoritus?.$class,
            format: koodinNimiOnly
          }}
        />
      ),
      Taitotaso: (
        <FormField
          form={form}
          path={osasuoritusPath.path('arviointi')}
          view={TaitotasoView}
          edit={TaitotasoEdit}
        />
      )
    },
    content: (
      <VSTLukutaitoProperties
        level={level}
        osasuoritusIndex={osasuoritusIndex}
        form={form}
        // @ts-expect-error Korjaa tyypitys
        suoritusPath={osasuoritusPath}
      />
    )
  }
}
