export const filterOrgTreeByRaporttityyppi = (raporttityyppi, orgs) =>
  (orgs || []).flatMap((org) => {
    const children = filterOrgTreeByRaporttityyppi(raporttityyppi, org.children)
    return org.raportit.includes(raporttityyppi)
      ? [{ ...org, children }]
      : children
  })

export const today = () => new Date()

export const isEqualDate = (a, b) =>
  a.getYear() === b.getYear() &&
  a.getMonth() === b.getMonth() &&
  a.getDate() === b.getDate()

export const selectFromState = (stateP) => ({
  selectedOrganisaatioP: stateP.map((state) => state.selectedOrganisaatio),
  dbUpdatedP: stateP.map((state) => state.dbUpdated)
})
