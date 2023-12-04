import React from 'react'
import { TestIdLayer } from '../../appstate/useTestId'
import { CommonProps } from '../../components-v2/CommonProps'
import { FormField } from '../../components-v2/forms/FormField'
import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import {
  KehittyvänKielenTaitotasoEdit,
  KehittyvänKielenTaitotasoView
} from '../../components-v2/opiskeluoikeus/KehittyvänKielenTaitotasoField'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyValue
} from '../../components-v2/opiskeluoikeus/OsasuoritusProperty'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { lastElement } from '../../util/optics'
import { t } from '../../i18n/i18n'
import { DateEdit, DateView } from '../../components-v2/controls/DateField'

export type VSTKoto2012KieliPropertiesProps = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  path: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus
  >
}>

export const VSTKoto2012KieliProperties: React.FC<
  VSTKoto2012KieliPropertiesProps
> = (props) => {
  // Huom! On väärin ottaa arviointilistan viimeinen arviointi ja luottaa että se on oikein, mutta se on riittävä tässä tapauksessa.
  const arviointiPath = props.path
    .prop('arviointi')
    .optional()
    .compose(lastElement())

  return (
    <>
      <VSTKoto2012TaitotasoProperty
        form={props.form}
        path={arviointiPath}
        osa="kuullunYmmärtämisenTaitotaso"
        label="Kuullun ymmärtämisen taitotaso"
      />
      <VSTKoto2012TaitotasoProperty
        form={props.form}
        path={arviointiPath}
        osa="puhumisenTaitotaso"
        label="Puhumisen taitotaso"
      />
      <VSTKoto2012TaitotasoProperty
        form={props.form}
        path={arviointiPath}
        osa="luetunYmmärtämisenTaitotaso"
        label="Luetun ymmärtämisen taitotaso"
      />
      <VSTKoto2012TaitotasoProperty
        form={props.form}
        path={arviointiPath}
        osa="kirjoittamisenTaitotaso"
        label="Kirjoittamisen taitotaso"
      />
      <OsasuoritusProperty label={t('Päivämäärä')}>
        <OsasuoritusPropertyValue>
          <FormField
            form={props.form}
            path={arviointiPath.prop('päivä')}
            view={DateView}
            edit={DateEdit}
          />
        </OsasuoritusPropertyValue>
      </OsasuoritusProperty>
    </>
  )
}

type VSTKoto2012TaitotasoPropertyProps = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  path: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi
  >
  osa:
    | 'luetunYmmärtämisenTaitotaso'
    | 'puhumisenTaitotaso'
    | 'kirjoittamisenTaitotaso'
    | 'kuullunYmmärtämisenTaitotaso'
  label: string
}>
const VSTKoto2012TaitotasoProperty: React.FC<
  VSTKoto2012TaitotasoPropertyProps
> = (props) => (
  <TestIdLayer id={props.osa}>
    <OsasuoritusProperty label={t(props.label)}>
      <OsasuoritusPropertyValue>
        <FormField
          form={props.form}
          path={props.path.prop(props.osa)}
          view={KehittyvänKielenTaitotasoView}
          edit={KehittyvänKielenTaitotasoEdit}
        />
      </OsasuoritusPropertyValue>
    </OsasuoritusProperty>
  </TestIdLayer>
)
