import React from 'react'
import { CommonProps } from '../../../components-v2/CommonProps'
import { FormModel, FormOptic } from '../../../components-v2/forms/FormModel'
import { finnish, t } from '../../../i18n/i18n'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus } from '../../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpintokokonaisuus'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus } from '../../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenSuoritus'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus } from '../../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOsaamiskokonaisuudenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { AddPaikallinenOsasuoritus } from '../../common/AddPaikallinenOsasuoritus'
import { laajuusOpintopisteissa } from '../../common/constructors'

export type AddOpintokokonaisuudenSuoritusProps = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  path: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus
  >
}>

export const AddOpintokokonaisuudenSuoritus: React.FC<
  AddOpintokokonaisuudenSuoritusProps
> = (props) => (
  <AddPaikallinenOsasuoritus
    form={props.form}
    path={props.path}
    createOsasuoritus={(tunniste) =>
      OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus({
        koulutusmoduuli:
          OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus({
            tunniste,
            kuvaus: tunniste.nimi,
            laajuus: laajuusOpintopisteissa(1)
          })
      })
    }
    preferenceStoreName="oppivelvollisillesuunnattuvapaansivistystyonopintokokonaisuus"
    level={1}
    placeholder={t('Lisää paikallinen opintokokonaisuus')}
  />
)
