import React, { useEffect, useMemo, useState } from 'react'
import { isSuccess, useApiOnce, useOnApiError } from '../api-fetch'
import {
  fetchRaportointikannanPäivitysaika,
  fetchRaporttienOrganisaatiot
} from '../util/koskiApi'
import { replaceLocation, showInternalError } from '../util/location'
import { OrganisaatioDropdown } from './OrganisaatioDropdown'
import { kaikkiRaportitKategorioittain, Raportti } from './raporttikategoriat'
import { RaporttiLomake } from './RaporttiLomake'
import {
  alkuvalinta,
  kategorianValinta,
  NäkyväKategoria,
  NäkyväRaportti,
  näkyvätKategoriat,
  organisaatioidenNimet,
  RaportinOrganisaatio,
  raportinValinta
} from './raporttivalinta'
import { Teksti } from './Teksti'

export const Raportit: React.FC<{ polku: string[] }> = ({ polku }) => {
  const organisaatiot = useApiOnce(fetchRaporttienOrganisaatiot)
  const päivitysaika = useApiOnce(fetchRaportointikannanPäivitysaika)
  useOnApiError(organisaatiot, showInternalError)
  useOnApiError(päivitysaika, showInternalError)

  return isSuccess(organisaatiot) ? (
    <Raporttinäkymä
      polku={polku}
      organisaatiot={organisaatiot.data}
      päivitysaika={isSuccess(päivitysaika) ? päivitysaika.data : undefined}
    />
  ) : (
    <div className="ajax-loading-placeholder">{'Ladataan...'}</div>
  )
}

type RaporttinäkymäProps = {
  polku: string[]
  organisaatiot: RaportinOrganisaatio[]
  päivitysaika: string | undefined
}

const Raporttinäkymä: React.FC<RaporttinäkymäProps> = ({
  polku,
  organisaatiot,
  päivitysaika
}) => {
  const kategoriat = useMemo(
    () => näkyvätKategoriat(kaikkiRaportitKategorioittain, organisaatiot),
    [organisaatiot]
  )
  const nimet = useMemo(
    () => organisaatioidenNimet(organisaatiot),
    [organisaatiot]
  )
  const [valinta, setValinta] = useState(() => alkuvalinta(kategoriat, polku))

  const kategoria: NäkyväKategoria<Raportti> | undefined =
    kategoriat[valinta.kategoriaIdx]
  const raportti = kategoria?.raportit[valinta.raporttiIdx]

  useEffect(() => {
    if (kategoria && raportti) {
      replaceLocation(
        `/koski/raportit/${kategoria.id}/${raportti.id}${window.location.search}`
      )
    }
  }, [kategoria, raportti])

  return (
    <div className="content-area raportit">
      <div className="main-content">
        {organisaatiot.length > 0 ? (
          <Kategoriavalitsin
            kategoriat={kategoriat}
            valittu={valinta.kategoriaIdx}
            onValinta={(kategoriaIdx) =>
              setValinta((v) =>
                kategoriaIdx === v.kategoriaIdx
                  ? v
                  : kategorianValinta(kategoriat, v, kategoriaIdx)
              )
            }
          />
        ) : (
          <div className="error" role="error" aria-live="polite">
            <Teksti avain="virhe-ei-organisaatiokayttaoikeuksia" />
          </div>
        )}
        {kategoria && (
          <h2>
            <Teksti avain={kategoria.heading} />
          </h2>
        )}
        <Raporttivalitsin
          raportit={kategoria ? kategoria.raportit : []}
          valittu={valinta.raporttiIdx}
          onValinta={(raporttiIdx) =>
            setValinta((v) => raportinValinta(kategoriat, v, raporttiIdx))
          }
        />
        <div className="organisaatio-valitsin">
          {raportti && raportti.organisaatiot.length > 0 ? (
            <OrganisaatioDropdown
              organisaatiot={raportti.organisaatiot}
              valittu={valinta.organisaatio}
              onValinta={(organisaatio) =>
                setValinta((v) => ({ ...v, organisaatio }))
              }
            />
          ) : null}
        </div>
        {kategoria && raportti && (
          <RaporttiLomake
            key={`${kategoria.id}/${raportti.id}`}
            lomake={raportti.lomake}
            organisaatio={valinta.organisaatio}
            päivitysaika={päivitysaika}
            organisaatioidenNimet={nimet}
          />
        )}
      </div>
    </div>
  )
}

type KategoriavalitsinProps = {
  kategoriat: NäkyväKategoria<Raportti>[]
  valittu: number
  onValinta: (kategoriaIdx: number) => void
}

const Kategoriavalitsin: React.FC<KategoriavalitsinProps> = ({
  kategoriat,
  valittu,
  onValinta
}) => (
  <div className="tabs-container">
    {kategoriat.map((kategoria, index) =>
      kategoria.visible ? (
        <span
          key={index}
          className={`tabs-item ${index === valittu ? 'tabs-item-selected' : ''}`}
          onClick={() => onValinta(index)}
        >
          <span className="tabs-item-text">
            <Teksti avain={kategoria.tab} />
          </span>
          <span className="tabs-item-caret" />
        </span>
      ) : null
    )}
  </div>
)

type RaporttivalitsinProps = {
  raportit: NäkyväRaportti<Raportti>[]
  valittu: number
  onValinta: (raporttiIdx: number) => void
}

const Raporttivalitsin: React.FC<RaporttivalitsinProps> = ({
  raportit,
  valittu,
  onValinta
}) => (
  <div className="raportti-valitsin">
    {raportit.length < 2 ? null : (
      <ul className="pills-container">
        {raportit.map((raportti, index) =>
          raportti.visible ? (
            <li
              key={raportti.id}
              onClick={() => onValinta(index)}
              className={
                index === valittu
                  ? 'pills-item pills-item-selected'
                  : 'pills-item'
              }
            >
              <Teksti avain={raportti.name} />
            </li>
          ) : null
        )}
      </ul>
    )}
  </div>
)
