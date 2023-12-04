import { append } from 'fp-ts/lib/Array'
import React, { useState } from 'react'
import { useKoodisto, useKoodistoFiller } from '../../../appstate/koodisto'
import { usePreferences } from '../../../appstate/preferences'
import { CommonProps } from '../../../components-v2/CommonProps'
import { Column, ColumnRow } from '../../../components-v2/containers/Columns'
import {
  Select,
  SelectOption,
  groupKoodistoToOptions
} from '../../../components-v2/controls/Select'
import { FormModel, FormOptic } from '../../../components-v2/forms/FormModel'
import { emptyLocalizedString, finnish, t } from '../../../i18n/i18n'
import {
  Koodistokoodiviite,
  isKoodistokoodiviite
} from '../../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus } from '../../../types/fi/oph/koski/schema/MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyonOpintojenSuoritus'
import { MuuallaSuoritetutVapaanSivistystyönOpinnot } from '../../../types/fi/oph/koski/schema/MuuallaSuoritetutVapaanSivistystyonOpinnot'
import { OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus } from '../../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonValinnaistenSuuntautumisopintojenSuoritus'
import { VapaanSivistystyönOpintokokonaisuudenSuoritus } from '../../../types/fi/oph/koski/schema/VapaanSivistystyonOpintokokonaisuudenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import {
  VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus,
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus
} from '../../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuoritus'
import { isKoodistoviiteOf } from '../../../util/schema'
import { laajuusOpintopisteissa } from '../../common/constructors'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus } from '../../../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenSuoritus'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus } from '../../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpintokokonaisuus'
import { UusiOsasuoritusModal } from '../../../components-v2/opiskeluoikeus/PaikallinenOsasuoritusSelect'
import { PaikallinenKoodi } from '../../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { TestIdLayer } from '../../../appstate/useTestId'
import { appendOptional } from '../../../util/array'

type AddSuuntautumisopinnonOsasuoritusProps = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  path: FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus
  >
  organisaatioOid: string
}>

type SuuntautumisopintoOption = SelectOption<
  | Koodistokoodiviite<'vstmuuallasuoritetutopinnot'>
  | VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus
>

export const AddSuuntautumisopinnonOsasuoritus: React.FC<
  AddSuuntautumisopinnonOsasuoritusProps
> = ({ form, path, organisaatioOid, ...props }) => {
  const fillKoodistot = useKoodistoFiller()
  const osasuoritukset = path.prop('osasuoritukset')
  const [addingNew, setAddingNew] = useState(false)

  const storedPaikalliset =
    usePreferences<VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus>(
      organisaatioOid,
      'vapaansivistystyonvapaatavoitteisenkoulutuksenosasuoritus'
    )

  const options = useOptions(storedPaikalliset.preferences)

  const onAdd = async (tunniste: PaikallinenKoodi) => {
    if (tunniste) {
      setAddingNew(false)
      form.updateAt(
        osasuoritukset,
        appendOptional(
          await fillKoodistot(createNewPaikallinenOpinto(tunniste))
        )
      )
    }
  }

  const onSelect = async (option?: SuuntautumisopintoOption) => {
    const value = option?.value
    if (value) {
      if (isKoodistokoodiviite(value) && isMuuallaSuoritettuOpinto(value)) {
        form.updateAt(
          osasuoritukset,
          appendOptional(
            await fillKoodistot(createMuuallaSuoritettuOpinto(value))
          )
        )
      } else if (
        isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus(value)
      ) {
        form.updateAt(
          osasuoritukset,
          appendOptional(await fillKoodistot(createPaikallinenOpinto(value)))
        )
      }
    } else if (option?.key === NEW_PAIKALLINEN) {
      setAddingNew(true)
    }
  }

  return (
    <TestIdLayer id="addOsasuoritus">
      <ColumnRow indent={1}>
        <Column span={10}>
          <Select
            placeholder={t('Lisää paikallinen opintokokonaisuus')}
            options={options}
            onChange={onSelect}
            testId="select"
          />
          {addingNew && (
            <UusiOsasuoritusModal
              onClose={() => setAddingNew(false)}
              title={t('Lisää paikallinen opintokokonaisuus')}
              onSubmit={onAdd}
            />
          )}
        </Column>
      </ColumnRow>
    </TestIdLayer>
  )
}

const NEW_PAIKALLINEN = '__NEW__'

const useOptions = (
  paikalliset: VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus[]
): SuuntautumisopintoOption[] => {
  const muuallaSuoritetut = useKoodisto('vstmuuallasuoritetutopinnot')
  const groupedMuuallaSuoritetut = muuallaSuoritetut
    ? groupKoodistoToOptions(muuallaSuoritetut)
    : []
  const groupedPaikalliset: SelectOption<VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus>[] =
    [
      {
        key: 'paikallinen',
        label: t('Paikalliset opintokokonaisuudet'),
        isGroup: true,
        children: [
          {
            key: NEW_PAIKALLINEN,
            label: t('Lisää uusi')
          },
          ...paikalliset.map((value) => ({
            key: value.tunniste.koodiarvo,
            label: t(value.tunniste.nimi),
            value
          }))
        ]
      }
    ]
  return [...groupedMuuallaSuoritetut, ...groupedPaikalliset]
}

const createMuuallaSuoritettuOpinto = (
  tunniste: Koodistokoodiviite<'vstmuuallasuoritetutopinnot'>
): VapaanSivistystyönOpintokokonaisuudenSuoritus =>
  MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
    {
      koulutusmoduuli: MuuallaSuoritetutVapaanSivistystyönOpinnot({
        tunniste,
        kuvaus: tunniste.nimi || finnish(''),
        laajuus: laajuusOpintopisteissa(1)
      })
    }
  )

const createPaikallinenOpinto = ({
  tunniste,
  kuvaus,
  laajuus
}: VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus): VapaanSivistystyönOpintokokonaisuudenSuoritus =>
  OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus({
    koulutusmoduuli:
      OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus({
        tunniste,
        kuvaus,
        laajuus
      })
  })

const createNewPaikallinenOpinto = (tunniste: PaikallinenKoodi) =>
  createPaikallinenOpinto(
    VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus({
      tunniste,
      kuvaus: tunniste.nimi,
      laajuus: laajuusOpintopisteissa(1)
    })
  )

const isMuuallaSuoritettuOpinto = isKoodistoviiteOf(
  'vstmuuallasuoritetutopinnot'
)
