import React from 'react'
import { LocalizedTextView } from '../../components-v2/controls/LocalizedTestField'
import { FormField } from '../../components-v2/forms/FormField'
import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import {
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
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenArviointi'
import { VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { createArviointi } from '../common/arviointi'
import { ArviointiProperty } from '../common/propertyFields'
import { VSTSuoritus, VSTSuoritusOsasuorituksilla } from '../common/types'

type VSTLukutaitoPropertiesProps = {
  osasuoritusIndex: number
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTSuoritusOsasuorituksilla
  >
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus
  >
  createOsasuoritus: (
    path: FormOptic<VapaanSivistystyönPäätasonSuoritus, any>,
    osasuoritus: VSTSuoritus
  ) => void
  testId: string
}

export const VSTLukutaitoProperties: React.FC<VSTLukutaitoPropertiesProps> = (
  props
) => {
  return (
    <div>
      <ArviointiProperty form={props.form} path={props.osasuoritusPath} />
    </div>
  )
}

export type OsasuoritusToTableRowParams = {
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTSuoritusOsasuorituksilla
  >
  suoritusIndex: number
  osasuoritusIndex: number
  createOsasuoritus: (
    path: FormOptic<VapaanSivistystyönPäätasonSuoritus, any>,
    osasuoritus: VSTSuoritus
  ) => void
}

export const osasuoritusToTableRow = ({
  suoritusPath,
  suoritusIndex,
  osasuoritusIndex,
  form,
  level,
  createOsasuoritus
}: OsasuoritusToTableRowParams): OsasuoritusRowData<
  'Osasuoritus' | 'Laajuus' | 'Arvosana' | 'Taitotaso'
> => {
  const osasuoritus = suoritusPath
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
          path={osasuoritus.path('koulutusmoduuli.tunniste.nimi')}
          view={LocalizedTextView}
          testId="nimi"
        />
      ),
      Laajuus: (
        <FormField
          form={form}
          path={osasuoritus.path('koulutusmoduuli.laajuus')}
          view={LaajuusView}
          edit={LaajuusOpintopisteissäEdit}
        />
      ),
      Arvosana: (
        <FormField
          form={form}
          path={osasuoritus.path('arviointi')}
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
      Taitotaso: (
        <FormField
          form={form}
          path={osasuoritus.path('arviointi')}
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
        suoritusPath={suoritusPath}
        // @ts-expect-error Korjaa tyypitys
        osasuoritusPath={osasuoritus}
        createOsasuoritus={createOsasuoritus}
      />
    )
  }
}
