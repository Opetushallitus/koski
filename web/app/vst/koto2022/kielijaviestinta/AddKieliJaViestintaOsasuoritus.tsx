import React from 'react'
import { CommonProps } from '../../../components-v2/CommonProps'
import { FormModel, FormOptic } from '../../../components-v2/forms/FormModel'
import { Koodistokoodiviite } from '../../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenOsasuoritus'
import { VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022 } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import { VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022 } from '../../../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022'
import { VapaanSivistystyönOpiskeluoikeus } from '../../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { AddKoodistonOsasuoritus } from '../../common/AddKoodistonOsasuoritus'
import { LaajuusOpintopisteissä } from "../../../types/fi/oph/koski/schema/LaajuusOpintopisteissa"

type AddKieliJaViestintaOsasuoritus = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022
  >
}>

export const AddKieliJaViestintaOsasuoritus: React.FC<
  AddKieliJaViestintaOsasuoritus
> = (props) => (
  <AddKoodistonOsasuoritus
    form={props.form}
    path={props.osasuoritusPath}
    koodistoUri="vstkoto2022kielijaviestintakoulutus"
    createOsasuoritus={createOsasuoritus}
    level={1}
  />
)

const createOsasuoritus = (
  tunniste: Koodistokoodiviite<'vstkoto2022kielijaviestintakoulutus'>
): VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus =>
  VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus({
    koulutusmoduuli:
      VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli2022(
        { tunniste, laajuus: LaajuusOpintopisteissä({arvo: 1}) }
      )
  })
