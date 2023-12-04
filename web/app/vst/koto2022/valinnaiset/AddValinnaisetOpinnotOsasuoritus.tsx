import React from 'react'
import { CommonProps } from '../../../components-v2/CommonProps'
import { FormModel, FormOptic } from '../../../components-v2/forms/FormModel'
import { PaikallinenKoodi } from '../../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022 } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022'
import { VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022 } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import { VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus } from '../../../types/fi/oph/koski/schema/VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { AddPaikallinenOsasuoritus } from '../../common/AddPaikallinenOsasuoritus'
import { laajuusOpintopisteissa } from '../../common/constructors'

type AddValinnaisetOpinnotOsasuoritusProps = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022
  >
}>

export const AddValinnaisetOpinnotOsasuoritus: React.FC<
  AddValinnaisetOpinnotOsasuoritusProps
> = (props) => (
  <AddPaikallinenOsasuoritus
    form={props.form}
    path={props.osasuoritusPath}
    createOsasuoritus={createOsasuoritus}
    preferenceStoreName="vstkotoutumiskoulutuksenvalinnaistenopintojenalasuorituksenkoulutusmoduuli2022"
    level={1}
  />
)

const createOsasuoritus = (
  tunniste: PaikallinenKoodi
): VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus =>
  VSTKotoutumiskoulutusValinnaistenOpintojenAlaosasuoritus({
    koulutusmoduuli:
      VSTKotoutumiskoulutuksenValinnaistenOpintojenAlasuorituksenKoulutusmoduuli2022(
        {
          tunniste,
          kuvaus: tunniste.nimi,
          laajuus: laajuusOpintopisteissa(1)
        }
      )
  })
