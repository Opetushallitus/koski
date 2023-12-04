import React from 'react'
import { TestIdLayer } from '../../appstate/useTestId'
import { CommonProps } from '../../components-v2/CommonProps'
import { DateEdit, DateView } from '../../components-v2/controls/DateField'
import { LocalizedTextView } from '../../components-v2/controls/LocalizedTestField'
import {
  FieldEditorProps,
  FieldViewerProps
} from '../../components-v2/forms/FormField'
import { FormListField } from '../../components-v2/forms/FormListField'
import { getValue } from '../../components-v2/forms/FormModel'
import {
  ArvosanaEdit,
  ArvosanaView
} from '../../components-v2/opiskeluoikeus/ArvosanaField'
import {
  OsasuoritusProperty,
  OsasuoritusSubproperty
} from '../../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { todayISODate } from '../../date/date'
import { LukutaitokoulutuksenArviointi } from '../../types/fi/oph/koski/schema/LukutaitokoulutuksenArviointi'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenArviointi } from '../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenArviointi'
import { VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { VSTPropertyFieldProps } from '../common/propertyFields'
import { VSTOsasuoritus } from '../typeguards'
import { KoodistoEdit } from '../../components-v2/opiskeluoikeus/KoodistoField'

export const VSTLukutaitoKielitaitotasoProperty = (
  props: VSTPropertyFieldProps<VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus>
) => {
  const osasuoritus = getValue(props.path)(props.form.state)
  const arvioitu = (osasuoritus?.arviointi?.length || 0) > 0

  return arvioitu ? (
    <TestIdLayer id="arviointi">
      <OsasuoritusProperty label="Arviointi">
        <FormListField
          form={props.form}
          path={props.path.prop('arviointi')}
          view={VSTLukutaitoKielitaitotasoView}
          edit={VSTLukutaitoKielitaitotasoEdit}
          editProps={{ osasuoritus }}
        />
      </OsasuoritusProperty>
    </TestIdLayer>
  ) : null
}

export type VSTLukutaitoKielitaitotasoViewProps = CommonProps<
  FieldViewerProps<LukutaitokoulutuksenArviointi, {}>
>

export const VSTLukutaitoKielitaitotasoView = (
  props: VSTLukutaitoKielitaitotasoViewProps
) => {
  const startRow = (props.index || 0) * 2
  return props.value ? (
    <>
      <OsasuoritusSubproperty rowNumber={startRow} label="Taitotaso">
        <LocalizedTextView value={props.value.taitotaso.nimi} />
      </OsasuoritusSubproperty>
      <OsasuoritusSubproperty rowNumber={startRow + 1} label="Arvosana">
        <ArvosanaView value={props.value} />
      </OsasuoritusSubproperty>
      <OsasuoritusSubproperty rowNumber={startRow + 2} label="Päivämäärä">
        <DateView value={props.value.päivä} />
      </OsasuoritusSubproperty>
    </>
  ) : null
}

export type VSTLukutaitoKielitaitotasoEditProps = CommonProps<
  FieldEditorProps<
    LukutaitokoulutuksenArviointi,
    {
      osasuoritus: VSTOsasuoritus
    }
  >
>

export const VSTLukutaitoKielitaitotasoEdit = (
  props: VSTLukutaitoKielitaitotasoEditProps
) => {
  const startRow = (props.index || 0) * 2
  console.log('VSTLukutaitoKielitaitotasoEdit', props)
  return props.value ? (
    <>
      <OsasuoritusSubproperty rowNumber={startRow} label="Taitotaso">
        <KoodistoEdit
          koodistoUri="arviointiasteikkokehittyvankielitaidontasot"
          testId="taitotaso"
          value={props.value.taitotaso}
          onChange={(taitotaso) =>
            taitotaso &&
            props.value &&
            props.onChange({
              ...props.value,
              taitotaso: taitotaso as LukutaitokoulutuksenArviointi['taitotaso']
            })
          }
        />
      </OsasuoritusSubproperty>
      <OsasuoritusSubproperty rowNumber={startRow + 1} label="Arvosana">
        <KoodistoEdit
          koodistoUri="arviointiasteikkovst"
          testId="arvosana"
          value={props.value.arvosana}
          onChange={(arvosana) =>
            arvosana &&
            props.value &&
            props.onChange({
              ...props.value,
              arvosana: arvosana as LukutaitokoulutuksenArviointi['arvosana']
            })
          }
        />
      </OsasuoritusSubproperty>
      <OsasuoritusSubproperty rowNumber={startRow + 2} label="Päivämäärä">
        <DateEdit
          value={props.value.päivä}
          onChange={(päivä) =>
            päivä &&
            props.value &&
            props.onChange({
              ...props.value,
              päivä
            })
          }
        />
      </OsasuoritusSubproperty>
    </>
  ) : null
}
