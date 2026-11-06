import React, { useEffect } from 'react'
import {
  createPreferLocalCache,
  isSuccess,
  useApiWithParams
} from '../api-fetch'
import { OptionList, Select } from '../components-v2/controls/Select'
import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { Finnish } from '../types/fi/oph/koski/schema/Finnish'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { fetchPerusteenKoulutukset } from '../util/koskiApi'

const koulutuksetCache = createPreferLocalCache(fetchPerusteenKoulutukset)

export type KoulutuskoodiEditProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  path: FormOptic<AmmatillinenOpiskeluoikeus, Koodistokoodiviite<'koulutus'>>
  tunniste: Koodistokoodiviite<'koulutus'>
  diaarinumero?: string
}

// Editorin rajapinnan koodeilta puuttuu $class, jota lomakkeen validointi vaatii.
const koulutuskoodi = (
  koodi: Koodistokoodiviite<'koulutus'>
): Koodistokoodiviite<'koulutus'> =>
  Koodistokoodiviite({
    koodiarvo: koodi.koodiarvo,
    koodistoUri: 'koulutus',
    nimi: koodi.nimi && Finnish(koodi.nimi as Finnish)
  })

// Jos perusteella on vain yksi koulutus, ePerusteista poistunut koodi päivittyy
// siihen automaattisesti. Useasta koulutuksesta käyttäjä valitsee itse.
export const KoulutuskoodiEdit: React.FC<KoulutuskoodiEditProps> = ({
  form,
  path,
  tunniste,
  diaarinumero
}) => {
  const response = useApiWithParams(
    fetchPerusteenKoulutukset,
    diaarinumero ? [diaarinumero] : undefined,
    koulutuksetCache
  )
  const koulutukset = isSuccess(response)
    ? response.data.map((k) => koulutuskoodi(k.data))
    : []
  const ainoaKoulutus = koulutukset.length === 1 ? koulutukset[0] : undefined
  const päivitettävä =
    ainoaKoulutus && ainoaKoulutus.koodiarvo !== tunniste.koodiarvo
      ? ainoaKoulutus
      : undefined

  useEffect(() => {
    if (päivitettävä) {
      form.updateAt(path, () => päivitettävä)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [päivitettävä?.koodiarvo])

  if (koulutukset.length < 2) {
    return <span>{tunniste.koodiarvo}</span>
  }

  // Nykyinen koodi pidetään näkyvissä, vaikka se ei enää kuuluisi perusteeseen.
  const valittavat = koulutukset.some((k) => k.koodiarvo === tunniste.koodiarvo)
    ? koulutukset
    : [tunniste, ...koulutukset]
  const options: OptionList<Koodistokoodiviite<'koulutus'>> = valittavat.map(
    (k) => ({ key: k.koodiarvo, label: k.koodiarvo, value: k })
  )
  return (
    <Select
      options={options}
      value={tunniste.koodiarvo}
      onChange={(opt) => {
        const valittu = opt?.value
        if (valittu) {
          form.updateAt(path, () => valittu)
        }
      }}
      testId="koulutuskoodi"
    />
  )
}
