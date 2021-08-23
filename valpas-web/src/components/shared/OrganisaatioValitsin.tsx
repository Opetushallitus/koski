import { boolean } from "fp-ts"
import * as A from "fp-ts/Array"
import * as Eq from "fp-ts/Eq"
import { pipe } from "fp-ts/function"
import * as Ord from "fp-ts/Ord"
import * as string from "fp-ts/string"
import React from "react"
import { getLocalized, t } from "../../i18n/i18n"
import {
  Kayttooikeusrooli,
  Oid,
  OrganisaatioHierarkia,
  OrganisaatioJaKayttooikeusrooli,
} from "../../state/common"
import {
  sessionStateStorage,
  useStoredState,
} from "../../state/useSessionStoreState"
import { Dropdown } from "../forms/Dropdown"

export type OrganisaatioValitsinProps = {
  organisaatioTyyppi: string
  organisaatioHierarkia: OrganisaatioHierarkia[]
  valittuOrganisaatioOid: Oid
  onChange: (value?: Oid) => void
  label: string
  containerClassName?: string
}

export const useStoredOrgState = (
  organisaatioTyyppiKey: string,
  allowedOrgs: OrganisaatioHierarkia[]
) => {
  const organisaatioOids = allowedOrgs.map((o) => o.oid)
  const fallback = organisaatioOids[0] || null
  return useStoredState<string | null>(
    sessionStateStorage<string | null, string | null>(
      `organisaatioOid-${organisaatioTyyppiKey}`,
      fallback,
      (value) => value,
      (serialized) =>
        serialized && organisaatioOids.includes(serialized)
          ? serialized
          : fallback
    )
  )
}

export const OrganisaatioValitsin = (props: OrganisaatioValitsinProps) => {
  const [, setStoredOrgOid] = useStoredOrgState(
    props.organisaatioTyyppi,
    props.organisaatioHierarkia
  )

  const onChange = (oid?: Oid) => {
    if (oid) {
      setStoredOrgOid(oid)
    }
    props.onChange(oid)
  }

  return (
    <Dropdown
      selectorId="organisaatiovalitsin"
      containerClassName={props.containerClassName}
      label={props.label}
      options={getOrgOptions(props.organisaatioHierarkia)}
      value={props.valittuOrganisaatioOid}
      onChange={onChange}
    />
  )
}

export const getOrganisaatiot = (
  käyttöoikeusroolit: OrganisaatioJaKayttooikeusrooli[],
  käytettäväKäyttöoikeus: Kayttooikeusrooli,
  organisaatioTyyppi: string,
  haeLakkautetut: boolean = true
): OrganisaatioHierarkia[] => {
  const sallitutKäyttöoikeusroolit = käyttöoikeusroolit.filter(
    (kayttooikeusrooli) =>
      kayttooikeusrooli.kayttooikeusrooli == käytettäväKäyttöoikeus
  )
  const kaikki = pipe(
    sallitutKäyttöoikeusroolit,
    A.map((kayttooikeus) =>
      getOrganisaatiotHierarkiastaRecur([kayttooikeus.organisaatioHierarkia])
    ),
    A.flatten
  )

  return pipe(
    kaikki,
    A.filter(
      (organisaatioHierarkia) =>
        organisaatioHierarkia.organisaatiotyypit.includes(organisaatioTyyppi) &&
        (haeLakkautetut || organisaatioHierarkia.aktiivinen)
    ),
    A.sortBy([Ord.reverse(byAktiivinen), byLocalizedNimi])
  )
}

const byAktiivinen = pipe(
  boolean.Ord,
  Ord.contramap(
    (organisaatioHierarkia: OrganisaatioHierarkia) =>
      organisaatioHierarkia.aktiivinen
  )
)

const byLocalizedNimi = pipe(
  string.Ord,
  Ord.contramap(
    (organisaatioHierarkia: OrganisaatioHierarkia) =>
      `${getLocalized(organisaatioHierarkia.nimi)}`
  )
)

const getOrganisaatiotHierarkiastaRecur = (
  organisaatioHierarkiat: OrganisaatioHierarkia[]
): OrganisaatioHierarkia[] => {
  if (!organisaatioHierarkiat.length) {
    return []
  } else {
    const lapset = pipe(
      organisaatioHierarkiat,
      A.map((organisaatioHierarkia) =>
        getOrganisaatiotHierarkiastaRecur(organisaatioHierarkia.children)
      ),
      A.flatten,
      A.map(removeChildren)
    )

    return organisaatioHierarkiat.map(removeChildren).concat(lapset)
  }
}

const removeChildren = (
  organisaatioHierarkia: OrganisaatioHierarkia
): OrganisaatioHierarkia => ({
  ...organisaatioHierarkia,
  children: [],
})

const eqOrgs = Eq.fromEquals(
  (a: OrganisaatioHierarkia, b: OrganisaatioHierarkia) => a.oid === b.oid
)

const getOrgOptions = (orgs: OrganisaatioHierarkia[]) =>
  pipe(
    orgs,
    A.uniq(eqOrgs),
    A.map((org: OrganisaatioHierarkia) => ({
      value: org.oid,
      display: `${
        !org.aktiivinen
          ? t("organisaatiovalitsin__lakkautettu_prefix") + ": "
          : ""
      }${getLocalized(org.nimi)} (${org.oid})`,
    }))
  )
