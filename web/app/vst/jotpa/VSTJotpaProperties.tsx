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
import {
  OsasuoritusRowData,
  OsasuoritusTable
} from '../../components-v2/opiskeluoikeus/OsasuoritusTable'
import { VapaanSivistystyöJotpaKoulutuksenArviointi } from '../../types/fi/oph/koski/schema/VapaanSivistystyoJotpaKoulutuksenArviointi'
import { VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { deleteAt } from '../../util/array'
import { createArviointi } from '../common/arviointi'
import { ArviointiProperty } from '../common/propertyFields'
import {
  VSTSuoritus,
  VSTSuoritusPaikallisillaOsasuorituksilla
} from '../common/types'
import { AddJotpaAlaosasuoritus } from './AddJotpaAlaosasuoritus'
import {
  PaikallisenKoulutusmoduulinLaajuusView,
  PaikallisenKoulutusmoduulinLaajuusEdit
} from '../../components-v2/opiskeluoikeus/PaikallisenKoulutusmoduulinLaajuusField'
import { LaajuusOpintopisteissä } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuoritus'
import { VapaanSivistystyönJotpaKoulutuksenOsasuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuoritus'

type VSTJotpaPropertiesProps = {
  osasuoritusIndex: number
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTSuoritusPaikallisillaOsasuorituksilla
  >
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus
  >
  testId: string
}

export const VSTJotpaProperties: React.FC<VSTJotpaPropertiesProps> = (
  props
) => {
  const osasuoritus = getValue(props.osasuoritusPath)(props.form.state)

  return (
    <div>
      <ArviointiProperty
        form={props.form}
        path={props.osasuoritusPath}
        arviointi={VapaanSivistystyöJotpaKoulutuksenArviointi}
      />
      <OsasuoritusTable
        editMode={props.form.editMode}
        addNewOsasuoritusView={AddJotpaAlaosasuoritus}
        addNewOsasuoritusViewProps={{
          form: props.form,
          osasuoritusPath: props.osasuoritusPath,
          level: props.level + 1
        }}
        onRemove={(i) => {
          props.form.updateAt(
            props.osasuoritusPath.prop('osasuoritukset').optional(),
            deleteAt(i)
          )
        }}
        rows={(osasuoritus?.osasuoritukset || []).map(
          (_os, osasuoritusIndex) => {
            return osasuoritusToTableRow({
              level: props.level + 1,
              form: props.form,
              osasuoritusPath: props.osasuoritusPath,
              // @ts-expect-error
              suoritusPath: props.osasuoritusPath,
              osasuoritusIndex: osasuoritusIndex,
              suoritusIndex: props.osasuoritusIndex,
              testId: `osasuoritukset.${osasuoritusIndex}`
            })
          }
        )}
      />
    </div>
  )
}

interface OsasuoritusToTableRowParams {
  level: number
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTSuoritusPaikallisillaOsasuorituksilla
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
  'Osasuoritus' | 'Laajuus' | 'Arvosana'
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
          path={osasuoritusPath.prop('koulutusmoduuli')}
          view={PaikallisenKoulutusmoduulinLaajuusView}
          edit={PaikallisenKoulutusmoduulinLaajuusEdit}
          editProps={{
            koulutusmoduuli: VapaanSivistystyönJotpaKoulutuksenOsasuoritus,
            template: LaajuusOpintopisteissä
          }}
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
      )
    },
    content: (
      <VSTJotpaProperties
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
