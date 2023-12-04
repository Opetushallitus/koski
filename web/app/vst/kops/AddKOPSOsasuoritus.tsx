import React from 'react'
import { useKoodistoFiller, useKoodistot } from '../../appstate/koodisto'
import { CommonProps } from '../../components-v2/CommonProps'
import {
  Select,
  SelectOption,
  groupKoodistoToOptions
} from '../../components-v2/controls/Select'
import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import { OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutuksenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { t } from '../../i18n/i18n'
import { append } from 'fp-ts/lib/Array'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOsasuoritus'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOsaamiskokonaisuudenSuoritus'
import { OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonValinnaistenSuuntautumisopintojenSuoritus'
import { isKoodistoviiteOf } from '../../util/schema'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOsaamiskokonaisuus'
import { laajuusOpintopisteissa } from '../common/constructors'
import { OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonValinnaisetSuuntautumisopinnot'
import { Column, ColumnRow } from '../../components-v2/containers/Columns'
import { TestIdLayer } from '../../appstate/useTestId'

export type AddKOPSOsasuoritusProps = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  path: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus
  >
}>

export const AddKOPSOsasuoritus: React.FC<AddKOPSOsasuoritusProps> = ({
  form,
  path
}) => {
  const options = useOptions()
  const fillKoodistot = useKoodistoFiller()
  const osasuoritukset = path.prop('osasuoritukset').optional()

  const onAdd = async (option?: SelectOption<Koodistokoodiviite>) => {
    const tunniste = option?.value
    if (tunniste) {
      form.updateAt(
        osasuoritukset,
        append(await fillKoodistot(createOsasuoritus(tunniste)))
      )
    }
  }

  return (
    <TestIdLayer id="addOsasuoritus">
      <ColumnRow>
        <Column span={10}>
          <Select
            placeholder={t('Lisää osasuoritus')}
            options={options}
            onChange={onAdd}
            testId="select"
          />
        </Column>
      </ColumnRow>
    </TestIdLayer>
  )
}

const useOptions = () => {
  const koodistot = useKoodistot('vstosaamiskokonaisuus', 'vstmuutopinnot')
  return koodistot ? groupKoodistoToOptions(koodistot) : []
}

const createOsasuoritus = (
  tunniste: Koodistokoodiviite
): OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus => {
  return isOsaamiskokonaisuus(tunniste)
    ? OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus({
        koulutusmoduuli:
          OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus({
            tunniste,
            laajuus: laajuusOpintopisteissa(1)
          })
      })
    : OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
        {
          koulutusmoduuli:
            OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot(
              {
                laajuus: laajuusOpintopisteissa(1)
              }
            )
        }
      )
}

const isOsaamiskokonaisuus = isKoodistoviiteOf('vstosaamiskokonaisuus')
