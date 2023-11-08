import React from 'react'
import { CommonProps } from '../../components-v2/CommonProps'
import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import { VapaanSivistystyönLukutaidonKokonaisuus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonLukutaidonKokonaisuus'
import { VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenKokonaisuudenSuoritus'
import { VapaanSivistystyönLukutaitokoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { AddKoodistonOsasuoritus } from '../common/AddKoodistonOsasuoritus'
import { laajuusOpintopisteissa } from '../common/constructors'

type AddLukutaitoOsasuoritusProps = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VapaanSivistystyönLukutaitokoulutuksenSuoritus
  >
}>

export const AddLukutaitoOsasuoritus: React.FC<AddLukutaitoOsasuoritusProps> = (
  props
) => (
  <AddKoodistonOsasuoritus
    form={props.form}
    suoritusPath={props.osasuoritusPath}
    koodistoUri="vstlukutaitokoulutuksenkokonaisuus"
    createOsasuoritus={(tunniste) =>
      VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus({
        koulutusmoduuli: VapaanSivistystyönLukutaidonKokonaisuus({
          tunniste,
          laajuus: laajuusOpintopisteissa(1)
        })
      })
    }
    level={0}
    testId={props.testId}
  />
)
