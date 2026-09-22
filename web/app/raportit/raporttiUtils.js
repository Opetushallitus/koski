import { parseQuery } from '../util/url'
import { t } from '../i18n/i18n'

export const filterOrgTreeByRaporttityyppi = (raporttityyppi, orgs) =>
  (orgs || []).flatMap((org) => {
    const children = filterOrgTreeByRaporttityyppi(raporttityyppi, org.children)
    return org.raportit.includes(raporttityyppi)
      ? [{ ...org, children }]
      : children
  })

export const today = () => new Date()

// TODO: feature flag raporttien lataamiseen massaluovutus-rajapintojen kautta
export const massaluovutusKaytossa = () =>
  parseQuery(window.location.search).taustalataus === 'true'

export const isEqualDate = (a, b) =>
  a.getYear() === b.getYear() &&
  a.getMonth() === b.getMonth() &&
  a.getDate() === b.getDate()

export const organisaatioNimet = (organisaatiot) => {
  const nimet = {}
  const kerääNimi = (orgs) =>
    (orgs || []).forEach((org) => {
      nimet[org.oid] = t(org.nimi)
      kerääNimi(org.children)
    })
  kerääNimi(organisaatiot)
  return nimet
}

export const selectFromState = (stateP) => ({
  selectedOrganisaatioP: stateP.map((state) => state.selectedOrganisaatio),
  dbUpdatedP: stateP.map((state) => state.dbUpdated),
  organisaatioNimetP: stateP.map((state) =>
    organisaatioNimet(state.organisaatiot)
  )
})
