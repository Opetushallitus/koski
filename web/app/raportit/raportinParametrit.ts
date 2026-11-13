import {
  AikajaksoraportinKyselynParametrit,
  PäiväraportinKyselynParametrit,
  TyyppivalintaraportinKyselynParametrit,
  VuosiluokkaraportinKyselynParametrit
} from '../components-v2/raportit/massaluovutusRaporttiKyselyt'
import { formatISODate } from '../date/date'
import { Language } from '../i18n/i18n'

type Valinnat = {
  organisaatio: { oid: string } | undefined
  kieli: Language
}

type Kotikunta = {
  näytäKotikunta: boolean
  kotikuntaPvm: Date | undefined
}

type Aikajakso = {
  alku: Date | undefined
  loppu: Date | undefined
}

export const tänään = () => new Date()

const isoPäivä = (päivä: Date): string => formatISODate(päivä) || ''

const samaPäivä = (a: Date, b: Date) =>
  a.getFullYear() === b.getFullYear() &&
  a.getMonth() === b.getMonth() &&
  a.getDate() === b.getDate()

// Kuluvan päivän kotikuntaa ei tarvitse asettaa
const kotikuntaPvmParametri = ({ näytäKotikunta, kotikuntaPvm }: Kotikunta) =>
  näytäKotikunta && kotikuntaPvm && !samaPäivä(tänään(), kotikuntaPvm)
    ? formatISODate(kotikuntaPvm)
    : undefined

const kotikuntaValittu = ({ näytäKotikunta, kotikuntaPvm }: Kotikunta) =>
  !näytäKotikunta || !!kotikuntaPvm

const aikajaksoValittu = ({ alku, loppu }: Aikajakso) =>
  !!alku && !!loppu && loppu.valueOf() >= alku.valueOf()

export const päiväraportinParametrit = (
  valinnat: Valinnat & Kotikunta & { päivä: Date | undefined }
): PäiväraportinKyselynParametrit | null =>
  valinnat.organisaatio && valinnat.päivä && kotikuntaValittu(valinnat)
    ? {
        organisaatioOid: valinnat.organisaatio.oid,
        paiva: isoPäivä(valinnat.päivä),
        kotikuntaPvm: kotikuntaPvmParametri(valinnat),
        language: valinnat.kieli
      }
    : null

export const vuosiluokkaraportinParametrit = (
  valinnat: Valinnat &
    Kotikunta & { päivä: Date | undefined; vuosiluokka: number }
): VuosiluokkaraportinKyselynParametrit | null => {
  const päiväraportti = päiväraportinParametrit(valinnat)
  return päiväraportti
    ? { ...päiväraportti, vuosiluokka: String(valinnat.vuosiluokka) }
    : null
}

export const aikajaksoraportinParametrit = (
  valinnat: Valinnat & Aikajakso
): AikajaksoraportinKyselynParametrit | null =>
  valinnat.organisaatio &&
  valinnat.alku &&
  valinnat.loppu &&
  aikajaksoValittu(valinnat)
    ? {
        organisaatioOid: valinnat.organisaatio.oid,
        alku: isoPäivä(valinnat.alku),
        loppu: isoPäivä(valinnat.loppu),
        language: valinnat.kieli
      }
    : null

export const aikarajattavanAikajaksoraportinParametrit = (
  valinnat: Valinnat &
    Aikajakso &
    Kotikunta & { osasuoritustenAikarajaus: boolean }
): AikajaksoraportinKyselynParametrit | null => {
  const aikajaksoraportti = aikajaksoraportinParametrit(valinnat)
  return aikajaksoraportti && kotikuntaValittu(valinnat)
    ? {
        ...aikajaksoraportti,
        osasuoritustenAikarajaus: valinnat.osasuoritustenAikarajaus,
        kotikuntaPvm: kotikuntaPvmParametri(valinnat)
      }
    : null
}

export const tyyppivalinnallisenAikajaksoraportinParametrit = (
  valinnat: Valinnat &
    Aikajakso &
    Kotikunta & { osasuoritustenAikarajaus: boolean; raportinTyyppi: string }
): TyyppivalintaraportinKyselynParametrit<string> | null => {
  const aikarajattava = aikarajattavanAikajaksoraportinParametrit(valinnat)
  return aikarajattava && valinnat.raportinTyyppi
    ? { ...aikarajattava, raportinTyyppi: valinnat.raportinTyyppi }
    : null
}
