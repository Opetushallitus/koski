import { useMemo } from 'react'
import {
  DialogField,
  useDialogField
} from '../../components-v2/createdialog/DialogField'
import { localize } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PreIBSuorituksenOsasuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBSuorituksenOsasuoritus2015'
import { PreIBSuorituksenOsasuoritus2019 } from '../../types/fi/oph/koski/schema/PreIBSuorituksenOsasuoritus2019'
import { createPreIBSuorituksenOsasuoritus2015 } from '../oppiaineet/preIBOppiaine2015'
import { createPreIBSuorituksenOsasuoritus2019 } from '../oppiaineet/preIBOppiaine2019'
import {
  isIBOppiaineLanguageTunniste,
  isIBOppiaineMuuTunniste,
  isLukionMatematiikka2015Tunniste,
  isLukionÄidinkieliJaKirjallisuus2015Tunniste,
  isVierasTaiToinenKotimainenKieli2015Tunniste,
  isVierasTaiToinenKotimainenKieli2019Tunniste
} from '../oppiaineet/tunnisteet'

export type PreIBOppiaineProps = {
  tunniste?: PreIBOppiaineTunniste
  paikallinenTunniste?: PaikallinenKoodi
  kieli?: Koodistokoodiviite<'kielivalikoima'>
  ryhmä?: Koodistokoodiviite<'aineryhmaib'>
  matematiikanOppimäärä?: Koodistokoodiviite<'oppiainematematiikka'>
  äidinkielenKieli?: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
  paikallinenKuvaus?: LocalizedString
}

export type PreIBOppiaineTunnisteKoodistoUri =
  | 'oppiaineetib'
  | 'koskioppiaineetyleissivistava'

export type ValtakunnallinenPreIBOppiaineTunniste =
  Koodistokoodiviite<PreIBOppiaineTunnisteKoodistoUri>
export type PreIBOppiaineTunniste =
  | ValtakunnallinenPreIBOppiaineTunniste
  | PaikallinenKoodi

export type UusiPreIBOppiaineState<T> = {
  tunniste: DialogField<PreIBOppiaineTunniste>
  paikallinenTunniste: DialogField<PaikallinenKoodi>
  kieli: DialogField<Koodistokoodiviite<'kielivalikoima'>>
  ryhmä: DialogField<Koodistokoodiviite<'aineryhmaib'>>
  matematiikanOppimäärä: DialogField<Koodistokoodiviite<'oppiainematematiikka'>>
  äidinkielenKieli: DialogField<
    Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
  >
  paikallinenKuvaus: DialogField<LocalizedString>
  result: T | null
}

export const useUusiPreIB2015OppiaineState =
  (): UusiPreIBOppiaineState<PreIBSuorituksenOsasuoritus2015> => {
    const tunniste = useDialogField<PreIBOppiaineTunniste>(true)

    const kieli = useDialogField<Koodistokoodiviite<'kielivalikoima'>>(
      isIBOppiaineLanguageTunniste(tunniste.value) ||
        isVierasTaiToinenKotimainenKieli2015Tunniste(tunniste.value)
    )

    const ryhmä = useDialogField<Koodistokoodiviite<'aineryhmaib'>>(
      isIBOppiaineLanguageTunniste(tunniste.value) ||
        isIBOppiaineMuuTunniste(tunniste.value)
    )

    const matematiikanOppimäärä = useDialogField<
      Koodistokoodiviite<'oppiainematematiikka'>
    >(isLukionMatematiikka2015Tunniste(tunniste.value))

    const äidinkielenKieli = useDialogField<
      Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
    >(isLukionÄidinkieliJaKirjallisuus2015Tunniste(tunniste.value))

    const paikallinenTunniste = useDialogField<PaikallinenKoodi>(false, () =>
      PaikallinenKoodi({ koodiarvo: '', nimi: localize('') })
    )
    const paikallinenKuvaus = useDialogField<LocalizedString>(false, () =>
      localize('')
    )

    const result = useMemo(
      () =>
        createPreIBSuorituksenOsasuoritus2015({
          tunniste: tunniste.value,
          paikallinenTunniste: paikallinenTunniste.value,
          kieli: kieli.value,
          ryhmä: ryhmä.value,
          matematiikanOppimäärä: matematiikanOppimäärä.value,
          äidinkielenKieli: äidinkielenKieli.value,
          paikallinenKuvaus: paikallinenKuvaus.value
        }),
      [
        kieli.value,
        matematiikanOppimäärä.value,
        paikallinenKuvaus.value,
        paikallinenTunniste.value,
        ryhmä.value,
        tunniste.value,
        äidinkielenKieli.value
      ]
    )

    return {
      tunniste,
      paikallinenTunniste,
      kieli,
      ryhmä,
      matematiikanOppimäärä,
      äidinkielenKieli,
      paikallinenKuvaus,
      result
    }
  }

export const useUusiPreIB2019OppiaineState =
  (): UusiPreIBOppiaineState<PreIBSuorituksenOsasuoritus2019> => {
    const tunniste = useDialogField<PreIBOppiaineTunniste>(true)

    const kieli = useDialogField<Koodistokoodiviite<'kielivalikoima'>>(
      isIBOppiaineLanguageTunniste(tunniste.value) ||
        isVierasTaiToinenKotimainenKieli2019Tunniste(tunniste.value)
    )

    const ryhmä = useDialogField<Koodistokoodiviite<'aineryhmaib'>>(
      isIBOppiaineLanguageTunniste(tunniste.value) ||
        isIBOppiaineMuuTunniste(tunniste.value)
    )

    const matematiikanOppimäärä = useDialogField<
      Koodistokoodiviite<'oppiainematematiikka'>
    >(isLukionMatematiikka2015Tunniste(tunniste.value))

    const äidinkielenKieli = useDialogField<
      Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus'>
    >(isLukionÄidinkieliJaKirjallisuus2015Tunniste(tunniste.value))

    const paikallinenTunniste = useDialogField<PaikallinenKoodi>(false, () =>
      PaikallinenKoodi({ koodiarvo: '', nimi: localize('') })
    )
    const paikallinenKuvaus = useDialogField<LocalizedString>(false, () =>
      localize('')
    )

    const result = useMemo(
      () =>
        createPreIBSuorituksenOsasuoritus2019({
          tunniste: tunniste.value,
          paikallinenTunniste: paikallinenTunniste.value,
          kieli: kieli.value,
          ryhmä: ryhmä.value,
          matematiikanOppimäärä: matematiikanOppimäärä.value,
          äidinkielenKieli: äidinkielenKieli.value,
          paikallinenKuvaus: paikallinenKuvaus.value
        }),
      [
        kieli.value,
        matematiikanOppimäärä.value,
        paikallinenKuvaus.value,
        paikallinenTunniste.value,
        ryhmä.value,
        tunniste.value,
        äidinkielenKieli.value
      ]
    )

    return {
      tunniste,
      paikallinenTunniste,
      kieli,
      ryhmä,
      matematiikanOppimäärä,
      äidinkielenKieli,
      paikallinenKuvaus,
      result
    }
  }
