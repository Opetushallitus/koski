export const filterOrgTreeByRaporttityyppi = (raporttityyppi, orgs) =>
  (orgs || []).flatMap(org => {
      const children = filterOrgTreeByRaporttityyppi(raporttityyppi, org.children)
      return org.raportit.includes(raporttityyppi)
          ? [{ ...org, children }]
          : children
  })

export const today = () => new Date()
