import {
  isKoodistokoodiviite,
  Koodistokoodiviite
} from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LaajuusOpintopisteissä } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { LukionMuuModuuliMuissaOpinnoissa2019 } from '../../types/fi/oph/koski/schema/LukionMuuModuuliMuissaOpinnoissa2019'
import { LukionMuuModuuliOppiaineissa2019 } from '../../types/fi/oph/koski/schema/LukionMuuModuuliOppiaineissa2019'
import { LukionPaikallinenOpintojakso2019 } from '../../types/fi/oph/koski/schema/LukionPaikallinenOpintojakso2019'
import { LukionVieraanKielenModuuliMuissaOpinnoissa2019 } from '../../types/fi/oph/koski/schema/LukionVieraanKielenModuuliMuissaOpinnoissa2019'
import { LukionVieraanKielenModuuliOppiaineissa2019 } from '../../types/fi/oph/koski/schema/LukionVieraanKielenModuuliOppiaineissa2019'
import {
  isPaikallinenKoodi,
  PaikallinenKoodi
} from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PreIBLukionModuulinSuoritus2019 } from '../../types/fi/oph/koski/schema/PreIBLukionModuulinSuoritus2019'
import { PreIBLukionModuulinSuoritusMuissaOpinnoissa2019 } from '../../types/fi/oph/koski/schema/PreIBLukionModuulinSuoritusMuissaOpinnoissa2019'
import { PreIBLukionModuulinSuoritusOppiaineissa2019 } from '../../types/fi/oph/koski/schema/PreIBLukionModuulinSuoritusOppiaineissa2019'
import { PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 } from '../../types/fi/oph/koski/schema/PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019'
import { PreIBLukionPaikallisenOpintojaksonSuoritus2019 } from '../../types/fi/oph/koski/schema/PreIBLukionPaikallisenOpintojaksonSuoritus2019'
import { PreIBSuorituksenOsasuoritus2019 } from '../../types/fi/oph/koski/schema/PreIBSuorituksenOsasuoritus2019'
import { PermissiveKoodiviite } from '../../util/koodisto'
import { KoulutusmoduuliOf, TunnisteOf } from '../../util/schema'
import {
  isLukionMuuModuuliMuissaOpinnoissa2019OppiaineenTunniste,
  isLukionMuuModuuliOppiaineissa2019OppiaineenTunniste,
  isLukionVieraanKielenModuuliMuissaOpinnoissa2019OppiaineenTunniste,
  isLukionVieraanKielenModuuliOppiaineissa2019OppiaineenTunniste,
  isMuidenLukioOpintojenPreIBSuoritus2019OppiaineenTunniste
} from './tunnisteet'

export type PreIB2019ModuuliProps = {
  oppiaineenTunniste: PreIB2019ModuuliOppiaineenTunniste
  tunniste?: PreIB2019OsasuoritusTunniste
  kuvaus?: LocalizedString
  laajuus?: LaajuusOpintopisteissä
  pakollinen?: boolean
  vierasKieli?: Koodistokoodiviite<'kielivalikoima'>
}

export type PreIB2019ModuuliOppiaineenTunniste = PermissiveKoodiviite<
  TunnisteOf<KoulutusmoduuliOf<PreIBSuorituksenOsasuoritus2019>>
>

export type PreIB2019OsasuoritusTunniste =
  | Koodistokoodiviite<'moduulikoodistolops2021'>
  | PaikallinenKoodi

export const createPreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 = (
  props: PreIB2019ModuuliProps
): PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 | null =>
  createPreIBModuulinSuoritus2019(props) ||
  createPreIBLukionPaikallisenOpintojaksonSuoritus2019(props)

const createPreIBModuulinSuoritus2019 = (
  props: PreIB2019ModuuliProps
): PreIBLukionModuulinSuoritus2019 | null =>
  isMuidenLukioOpintojenPreIBSuoritus2019OppiaineenTunniste(
    props.oppiaineenTunniste
  )
    ? createPreIBLukionModuulinSuoritusMuissaOpinnoissa2019(props)
    : createPreIBLukionModuulinSuoritusOppiaineissa2019(props)

const createPreIBLukionModuulinSuoritusOppiaineissa2019 = (
  props: PreIB2019ModuuliProps
): PreIBLukionModuulinSuoritusOppiaineissa2019 | null => {
  const koulutusmoduuli =
    createLukionVieraanKielenModuuliOppiaineissa2019(props) ||
    createLukionMuuModuuliOppiaineissa2019(props)
  return (
    koulutusmoduuli &&
    PreIBLukionModuulinSuoritusOppiaineissa2019({ koulutusmoduuli })
  )
}

const createPreIBLukionModuulinSuoritusMuissaOpinnoissa2019 = (
  props: PreIB2019ModuuliProps
): PreIBLukionModuulinSuoritusMuissaOpinnoissa2019 | null => {
  const koulutusmoduuli =
    createLukionVieraanKielenModuuliMuissaOpinnoissa2019(props) ||
    createLukionMuuModuuliMuissaOpinnoissa2019(props)
  return (
    koulutusmoduuli &&
    PreIBLukionModuulinSuoritusMuissaOpinnoissa2019({ koulutusmoduuli })
  )
}

const createLukionMuuModuuliOppiaineissa2019 = ({
  oppiaineenTunniste,
  tunniste,
  pakollinen,
  laajuus
}: PreIB2019ModuuliProps): LukionMuuModuuliOppiaineissa2019 | null =>
  isKoodistokoodiviite(tunniste) &&
  laajuus &&
  isLukionMuuModuuliOppiaineissa2019OppiaineenTunniste(oppiaineenTunniste)
    ? LukionMuuModuuliOppiaineissa2019({
        tunniste,
        pakollinen: !!pakollinen,
        laajuus
      })
    : null

const createLukionVieraanKielenModuuliOppiaineissa2019 = ({
  oppiaineenTunniste,
  tunniste,
  pakollinen,
  laajuus
}: PreIB2019ModuuliProps): LukionVieraanKielenModuuliOppiaineissa2019 | null =>
  isKoodistokoodiviite(tunniste) &&
  laajuus &&
  isLukionVieraanKielenModuuliOppiaineissa2019OppiaineenTunniste(
    oppiaineenTunniste
  )
    ? LukionVieraanKielenModuuliOppiaineissa2019({
        tunniste,
        pakollinen: !!pakollinen,
        laajuus
      })
    : null

const createLukionMuuModuuliMuissaOpinnoissa2019 = ({
  oppiaineenTunniste,
  tunniste,
  laajuus,
  pakollinen
}: PreIB2019ModuuliProps): LukionMuuModuuliMuissaOpinnoissa2019 | null =>
  laajuus &&
  isKoodistokoodiviite(tunniste) &&
  isLukionMuuModuuliMuissaOpinnoissa2019OppiaineenTunniste(oppiaineenTunniste)
    ? LukionMuuModuuliMuissaOpinnoissa2019({
        tunniste,
        laajuus,
        pakollinen: !!pakollinen
      })
    : null

const createLukionVieraanKielenModuuliMuissaOpinnoissa2019 = ({
  oppiaineenTunniste,
  tunniste,
  laajuus,
  pakollinen,
  vierasKieli
}: PreIB2019ModuuliProps): LukionVieraanKielenModuuliMuissaOpinnoissa2019 | null =>
  laajuus &&
  vierasKieli &&
  isKoodistokoodiviite(tunniste) &&
  isLukionVieraanKielenModuuliMuissaOpinnoissa2019OppiaineenTunniste(
    oppiaineenTunniste
  )
    ? LukionVieraanKielenModuuliMuissaOpinnoissa2019({
        tunniste,
        laajuus,
        pakollinen: !!pakollinen,
        kieli: vierasKieli
      })
    : null

const createPreIBLukionPaikallisenOpintojaksonSuoritus2019 = (
  props: PreIB2019ModuuliProps
): PreIBLukionPaikallisenOpintojaksonSuoritus2019 | null => {
  const koulutusmoduuli = createLukionPaikallinenOpintojakso2019(props)
  return (
    koulutusmoduuli &&
    PreIBLukionPaikallisenOpintojaksonSuoritus2019({ koulutusmoduuli })
  )
}

const createLukionPaikallinenOpintojakso2019 = ({
  tunniste,
  laajuus,
  kuvaus,
  pakollinen
}: PreIB2019ModuuliProps): LukionPaikallinenOpintojakso2019 | null =>
  isPaikallinenKoodi(tunniste) && laajuus && kuvaus
    ? LukionPaikallinenOpintojakso2019({
        tunniste,
        laajuus,
        kuvaus,
        pakollinen: !!pakollinen
      })
    : null
