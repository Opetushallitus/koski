import {
  PaikallinenKoodi,
  LocalizedString,
  LaajuusOpintopisteissä
} from './common'

export type OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus = {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus: LaajuusOpintopisteissä
}

export type VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus =
  {
    tunniste: PaikallinenKoodi
    kuvaus: LocalizedString
    laajuus?: LaajuusOpintopisteissä
  }
