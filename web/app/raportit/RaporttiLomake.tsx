import React, { useState } from 'react'
import { MassaluovutusRaportinLataus } from '../components-v2/raportit/MassaluovutusRaportinLataus'
import DateInput from '../date/DateInput'
import { lang, t } from '../i18n/i18n'
import { InfoIcon } from '../icons/InfoIcon'
import {
  aikajaksoraportinParametrit,
  aikarajattavanAikajaksoraportinParametrit,
  päiväraportinParametrit,
  tyyppivalinnallisenAikajaksoraportinParametrit,
  tänään,
  vuosiluokkaraportinParametrit
} from './raportinParametrit'
import {
  AikajaksoraportinLomake,
  AikarajattavanAikajaksoraportinLomake,
  Esimerkki,
  Osasuoritukset,
  PäiväraportinLomake,
  Raporttilomake,
  TyyppivalinnallisenAikajaksoraportinLomake,
  VuosiluokkaraportinLomake
} from './raporttikategoriat'
import { NäkyväOrganisaatio } from './raporttivalinta'
import { Kappaleet, Teksti } from './Teksti'
import { VuosiluokkaDropdown } from './VuosiluokkaDropdown'

type LomakkeenTiedot = {
  organisaatio: NäkyväOrganisaatio | undefined
  päivitysaika: string | undefined
  organisaatioidenNimet: Record<string, string>
}

export const RaporttiLomake: React.FC<
  LomakkeenTiedot & { lomake: Raporttilomake }
> = ({ lomake, ...tiedot }) => {
  switch (lomake.tyyppi) {
    case 'päivä':
      return <Päiväraportti lomake={lomake} {...tiedot} />
    case 'vuosiluokka':
      return <Vuosiluokkaraportti lomake={lomake} {...tiedot} />
    case 'aikajakso':
      return <Aikajaksoraportti lomake={lomake} {...tiedot} />
    case 'aikarajattava aikajakso':
      return <AikarajattavaAikajaksoraportti lomake={lomake} {...tiedot} />
    case 'tyyppivalinnallinen aikajakso':
      return (
        <TyyppivalinnallinenAikajaksoraportti lomake={lomake} {...tiedot} />
      )
  }
}

const vuosiluokat = [1, 2, 3, 4, 5, 6, 7, 8, 9]

const kaikkiOsasuoritukset: Record<Osasuoritukset, string> = {
  'tutkinnon osat':
    'Raportille valitaan kaikki tutkinnon osat riippumatta niiden suoritusajankohdasta',
  kurssisuoritukset:
    'Raportille valitaan kaikki kurssisuoritukset riippumatta niiden suoritusajankohdasta',
  opinnot:
    'Raportille valitaan kaikki opinnot riippumatta niiden suoritusajankohdasta'
}

const aikajaksonOsasuoritukset: Record<Osasuoritukset, string> = {
  'tutkinnon osat':
    'Raportille valitaan vain sellaiset tutkinnon osat, joiden arviointipäivä osuu yllä määritellylle aikajaksolle',
  kurssisuoritukset:
    'Raportille valitaan vain sellaiset kurssisuoritukset, joiden arviointipäivä osuu yllä määritellylle aikajaksolle',
  opinnot:
    'Raportille valitaan vain sellaiset opinnot, joiden arviointipäivä osuu yllä määritellylle aikajaksolle'
}

const Päiväraportti: React.FC<
  LomakkeenTiedot & { lomake: PäiväraportinLomake }
> = ({ lomake, organisaatio, päivitysaika, organisaatioidenNimet }) => {
  const [päivä, setPäivä] = useState<Date | undefined>(tänään)
  const [kotikuntaPvm, setKotikuntaPvm] = useState<Date | undefined>(tänään)

  return (
    <section>
      <LyhytKuvaus avain={lomake.kuvaus} />
      <PäivänValinta
        päivä={päivä}
        onChange={setPäivä}
        ohje={lomake.päivänOhje}
        inputId="dateinput-paiva"
      />
      {lomake.kotikunta && (
        <KotikunnanPäivänValinta
          päivä={kotikuntaPvm}
          onChange={setKotikuntaPvm}
          ohje={lomake.kotikunta.ohje}
        />
      )}
      <MassaluovutusRaportinLataus
        kyselymalli={lomake.kyselymalli}
        parametrit={päiväraportinParametrit({
          organisaatio,
          päivä,
          näytäKotikunta: !!lomake.kotikunta,
          kotikuntaPvm,
          kieli: lang
        })}
        dbUpdated={päivitysaika}
        oppilaitosNimet={organisaatioidenNimet}
      />
      <Vinkit esimerkki={lomake.esimerkki} />
    </section>
  )
}

const Vuosiluokkaraportti: React.FC<
  LomakkeenTiedot & { lomake: VuosiluokkaraportinLomake }
> = ({ lomake, organisaatio, päivitysaika, organisaatioidenNimet }) => {
  const [päivä, setPäivä] = useState<Date | undefined>(tänään)
  const [kotikuntaPvm, setKotikuntaPvm] = useState<Date | undefined>(tänään)
  const [vuosiluokka, setVuosiluokka] = useState(1)

  return (
    <section>
      <LyhytKuvaus avain={lomake.kuvaus} />
      <PäivänValinta
        päivä={päivä}
        onChange={setPäivä}
        ohje={lomake.päivänOhje}
        inputId="dateinput-paiva"
      />
      {lomake.kotikunta && (
        <KotikunnanPäivänValinta
          päivä={kotikuntaPvm}
          onChange={setKotikuntaPvm}
          ohje={lomake.kotikunta.ohje}
        />
      )}
      <div className="dropdown-selection parametri vuosiluokka">
        <label>
          <Teksti avain="select-class" />
        </label>
        <div>
          <VuosiluokkaDropdown
            vuosiluokat={vuosiluokat}
            valittu={vuosiluokka}
            onValinta={setVuosiluokka}
          />
        </div>
      </div>
      <MassaluovutusRaportinLataus
        kyselymalli={lomake.kyselymalli}
        parametrit={vuosiluokkaraportinParametrit({
          organisaatio,
          päivä,
          vuosiluokka,
          näytäKotikunta: !!lomake.kotikunta,
          kotikuntaPvm,
          kieli: lang
        })}
        dbUpdated={päivitysaika}
        oppilaitosNimet={organisaatioidenNimet}
      />
      <Vinkit esimerkki={lomake.esimerkki} />
    </section>
  )
}

const Aikajaksoraportti: React.FC<
  LomakkeenTiedot & { lomake: AikajaksoraportinLomake }
> = ({ lomake, organisaatio, päivitysaika, organisaatioidenNimet }) => {
  const [alku, setAlku] = useState<Date | undefined>()
  const [loppu, setLoppu] = useState<Date | undefined>()

  return (
    <section>
      <LyhytKuvaus avain={lomake.kuvaus} />
      <AikajaksonValinta
        alku={alku}
        loppu={loppu}
        onAlku={setAlku}
        onLoppu={setLoppu}
        ohje={lomake.ohje}
      />
      <MassaluovutusRaportinLataus
        kyselymalli={lomake.kyselymalli}
        parametrit={aikajaksoraportinParametrit({
          organisaatio,
          alku,
          loppu,
          kieli: lang
        })}
        dbUpdated={päivitysaika}
        oppilaitosNimet={organisaatioidenNimet}
      />
      <Vinkit esimerkki={lomake.esimerkki} />
    </section>
  )
}

const AikarajattavaAikajaksoraportti: React.FC<
  LomakkeenTiedot & { lomake: AikarajattavanAikajaksoraportinLomake }
> = ({ lomake, organisaatio, päivitysaika, organisaatioidenNimet }) => {
  const [alku, setAlku] = useState<Date | undefined>()
  const [loppu, setLoppu] = useState<Date | undefined>()
  const [kotikuntaPvm, setKotikuntaPvm] = useState<Date | undefined>(tänään)
  const [osasuoritustenAikarajaus, setOsasuoritustenAikarajaus] =
    useState(false)

  return (
    <section>
      <LyhytKuvaus avain={lomake.kuvaus} />
      <AikajaksonValinta
        alku={alku}
        loppu={loppu}
        onAlku={setAlku}
        onLoppu={setLoppu}
      />
      {!lomake.ilmanAikarajausta && (
        <Listavalinta
          saumaton
          vaihtoehdot={[
            {
              arvo: false,
              teksti: kaikkiOsasuoritukset[lomake.osasuoritukset]
            },
            {
              arvo: true,
              teksti: aikajaksonOsasuoritukset[lomake.osasuoritukset]
            }
          ]}
          valittu={osasuoritustenAikarajaus}
          onValinta={setOsasuoritustenAikarajaus}
        />
      )}
      {lomake.kotikunta && (
        <KotikunnanPäivänValinta
          päivä={kotikuntaPvm}
          onChange={setKotikuntaPvm}
          ohje={lomake.kotikunta.ohje}
        />
      )}
      <MassaluovutusRaportinLataus
        kyselymalli={lomake.kyselymalli}
        parametrit={aikarajattavanAikajaksoraportinParametrit({
          organisaatio,
          alku,
          loppu,
          osasuoritustenAikarajaus,
          näytäKotikunta: !!lomake.kotikunta,
          kotikuntaPvm,
          kieli: lang
        })}
        dbUpdated={päivitysaika}
        oppilaitosNimet={organisaatioidenNimet}
      />
      <Vinkit esimerkki={lomake.esimerkki} />
    </section>
  )
}

const TyyppivalinnallinenAikajaksoraportti: React.FC<
  LomakkeenTiedot & { lomake: TyyppivalinnallisenAikajaksoraportinLomake }
> = ({ lomake, organisaatio, päivitysaika, organisaatioidenNimet }) => {
  const [alku, setAlku] = useState<Date | undefined>()
  const [loppu, setLoppu] = useState<Date | undefined>()
  const [kotikuntaPvm, setKotikuntaPvm] = useState<Date | undefined>(tänään)
  const [osasuoritustenAikarajaus, setOsasuoritustenAikarajaus] =
    useState(false)
  const [raportinTyyppi, setRaportinTyyppi] = useState(lomake.oletustyyppi)

  return (
    <section>
      <LyhytKuvaus avain={lomake.kuvaus} />
      <Listavalinta
        otsikko={lomake.tyyppivalinnanOtsikko}
        vaihtoehdot={Object.values(lomake.raporttityypit).map((tyyppi) => ({
          arvo: tyyppi.key,
          teksti: tyyppi.name
        }))}
        valittu={raportinTyyppi}
        onValinta={setRaportinTyyppi}
      />
      <AikajaksonValinta
        alku={alku}
        loppu={loppu}
        onAlku={setAlku}
        onLoppu={setLoppu}
      />
      <Listavalinta
        otsikko={lomake.aikarajauksenOtsikko}
        vaihtoehdot={[
          { arvo: false, teksti: lomake.kaikkiOsasuoritukset },
          { arvo: true, teksti: lomake.aikajaksonOsasuoritukset }
        ]}
        valittu={osasuoritustenAikarajaus}
        onValinta={setOsasuoritustenAikarajaus}
      />
      {lomake.kotikunta && (
        <KotikunnanPäivänValinta
          päivä={kotikuntaPvm}
          onChange={setKotikuntaPvm}
          ohje={lomake.kotikunta.ohje}
        />
      )}
      <MassaluovutusRaportinLataus
        kyselymalli={lomake.kyselymalli}
        parametrit={tyyppivalinnallisenAikajaksoraportinParametrit({
          organisaatio,
          alku,
          loppu,
          osasuoritustenAikarajaus,
          raportinTyyppi,
          näytäKotikunta: !!lomake.kotikunta,
          kotikuntaPvm,
          kieli: lang
        })}
        dbUpdated={päivitysaika}
        oppilaitosNimet={organisaatioidenNimet}
      />
      <Vinkit esimerkki={lomake.esimerkki} />
    </section>
  )
}

const LyhytKuvaus: React.FC<{ avain: string }> = ({ avain }) => (
  <p className="lyhyt-kuvaus">
    <Teksti avain={avain} />
  </p>
)

type PäivänValintaProps = {
  päivä: Date | undefined
  onChange: (päivä: Date | undefined) => void
  inputId: string
  ohje?: string
  otsikko?: string
}

const PäivänValinta: React.FC<PäivänValintaProps> = ({
  päivä,
  onChange,
  inputId,
  ohje,
  otsikko
}) => (
  <div className="parametri">
    <label>{otsikko || <Teksti avain="select-date" />}</label>
    <Päivämääräkenttä inputId={inputId} päivä={päivä} onChange={onChange} />
    {ohje && (
      <div className="ohje">
        <Teksti avain={ohje} />
      </div>
    )}
  </div>
)

const KotikunnanPäivänValinta: React.FC<
  Omit<PäivänValintaProps, 'inputId' | 'otsikko'>
> = (props) => (
  <PäivänValinta
    {...props}
    otsikko={t('select-kotikunta-date')}
    inputId="dateinput-kotikuntapvm"
  />
)

type AikajaksonValintaProps = {
  alku: Date | undefined
  loppu: Date | undefined
  onAlku: (päivä: Date | undefined) => void
  onLoppu: (päivä: Date | undefined) => void
  ohje?: string
}

const AikajaksonValinta: React.FC<AikajaksonValintaProps> = ({
  alku,
  loppu,
  onAlku,
  onLoppu,
  ohje
}) => (
  <div className="parametri">
    <label>
      <Teksti avain="select-date-range" />
    </label>
    <div className="parametrit-kentat">
      <Päivämääräkenttä
        inputId="dateinput-alku"
        päivä={alku}
        onChange={onAlku}
      />
      <span className="parametri-aikajakso-viiva">{' — '}</span>
      <Päivämääräkenttä
        inputId="dateinput-loppu"
        päivä={loppu}
        onChange={onLoppu}
      />
    </div>
    {ohje && (
      <div className="ohje">
        <Teksti avain={ohje} />
      </div>
    )}
  </div>
)

const Päivämääräkenttä: React.FC<{
  inputId: string
  päivä: Date | undefined
  onChange: (päivä: Date | undefined) => void
}> = ({ inputId, päivä, onChange }) => (
  <DateInput
    inputId={inputId}
    value={päivä}
    valueCallback={(arvo: Date) => onChange(arvo)}
    validityCallback={(kelvollinen: boolean) =>
      !kelvollinen && onChange(undefined)
    }
  />
)

type ListavalintaProps<T> = {
  vaihtoehdot: Array<{ arvo: T; teksti: string }>
  valittu: T
  onValinta: (arvo: T) => void
  otsikko?: string
  saumaton?: boolean
}

const Listavalinta = <T,>({
  vaihtoehdot,
  valittu,
  onValinta,
  otsikko,
  saumaton
}: ListavalintaProps<T>) => (
  <div className={`parametri ${saumaton ? 'parametri-seamless' : ''}`}>
    {otsikko && (
      <label>
        <Teksti avain={otsikko} />
      </label>
    )}
    <div className="radio-select-container">
      {vaihtoehdot.map((vaihtoehto) => (
        <label
          key={String(vaihtoehto.arvo)}
          className="radio-option-container"
          data-label={t(vaihtoehto.teksti)}
          data-selected={vaihtoehto.arvo === valittu}
        >
          <Teksti avain={vaihtoehto.teksti} />
          <input
            type="radio"
            className="radio-option"
            checked={vaihtoehto.arvo === valittu}
            onChange={() => onValinta(vaihtoehto.arvo)}
          />
          <span className="radio-checkmark" />
        </label>
      ))}
    </div>
  </div>
)

const Vinkit: React.FC<{ esimerkki: Esimerkki | undefined }> = ({
  esimerkki
}) =>
  esimerkki ? (
    <div className="vinkit">
      <h3>
        <InfoIcon /> <Teksti avain="tips-and-help" />
      </h3>
      {'kappaleet' in esimerkki ? (
        <Kappaleet avain={esimerkki.kappaleet} />
      ) : (
        <Teksti avain={esimerkki.teksti} />
      )}
    </div>
  ) : null
