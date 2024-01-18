import bem from "bem-ts"
import React from "react"
import { Link } from "react-router-dom"
import { ApiMethodState } from "../../api/apiHooks"
import { isError, isLoading, isSuccess } from "../../api/apiUtils"
import { SubmitButton } from "../../components/buttons/SubmitButton"
import { Form } from "../../components/forms/Form"
import { TextField } from "../../components/forms/TextField"
import { Spinner } from "../../components/icons/Spinner"
import { T, t } from "../../i18n/i18n"
import {
  HenkilöhakuResult,
  isEiLöytynytEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult,
  isLöytyiHenkilöhakuResult,
  LöytyiHenkilöhakuResult,
} from "../../state/apitypes/henkilohaku"
import { useBasePath } from "../../state/basePath"
import {
  expectAtLeastOne,
  expectValidHetu,
  expectValidOid,
} from "../../state/formValidators"
import { oppijaPath } from "../../state/paths"
import { FormValidators, useFormState } from "../../state/useFormState"
import "./MaksuttomuusOppijaSearch.less"

const b = bem("maksuttomuusoppijasearch")

type OppijaSearchValues = {
  query: string
}

const initialValues: OppijaSearchValues = {
  query: "",
}

const validators: FormValidators<OppijaSearchValues> = {
  query: [
    expectAtLeastOne("oppijahaku__validointivirhe", [
      expectValidHetu(),
      expectValidOid(),
    ]),
  ],
}

export type MaksuttomuusOppijaSearchProps = {
  searchState: ApiMethodState<HenkilöhakuResult>
  onQuery: (query: string) => void
  prevPath: string
}

export const MaksuttomuusOppijaSearch = (
  props: MaksuttomuusOppijaSearchProps,
) => {
  const form = useFormState({ initialValues, validators })

  const submit = form.submitCallback((data) => {
    props.onQuery(data.query)
  })

  return (
    <Form className={b()} onSubmit={submit}>
      <TextField
        label={t("oppijahaku__hae_hetulla_tai_oppijanumerolla")}
        {...form.fieldProps("query")}
      >
        <SubmitButton
          className={b("submit")}
          onClick={form.submitCallback(console.log)}
          disabled={!form.isValid || isLoading(props.searchState)}
          value={t("oppijahaku__hae")}
        />
        <div className={b("results")}>
          {isLoading(props.searchState) && <Spinner />}
          {isSuccess(props.searchState) && (
            <MaksuttomuusOppijaSearchResults
              hakutulos={props.searchState.data}
              prevPath={props.prevPath}
            />
          )}
          {isError(props.searchState) && (
            <MaksuttomuusOppijaSearchError
              statusCode={props.searchState.status}
            />
          )}
        </div>
      </TextField>
    </Form>
  )
}

type MaksuttomuusOppijaSearchResultsProps = {
  hakutulos: HenkilöhakuResult
  prevPath: string
}

const MaksuttomuusOppijaSearchResults = (
  props: MaksuttomuusOppijaSearchResultsProps,
) => {
  if (isLöytyiHenkilöhakuResult(props.hakutulos)) {
    return (
      <MaksuttomuusOppijaSearchMatchResult
        henkilö={props.hakutulos}
        prevPath={props.prevPath}
      />
    )
  } else if (
    isEiLöytynytEiLainTaiMaksuttomuudenPiirissäHenkilöhakuResult(
      props.hakutulos,
    )
  ) {
    return (
      <MaksuttomuusOppijaSearchUndefinedResult
        eiLöytynytIlmoitusId={"oppijahaku__maksuttomuus_ei_näytettävä_oppija"}
      />
    )
  }
  return (
    <MaksuttomuusOppijaSearchUndefinedResult
      eiLöytynytIlmoitusId={"oppijahaku__maksuttomuutta_ei_pysty_päättelemään"}
    />
  )
}

type MaksuttomuusOppijaSearchUndefinedResultProps = {
  eiLöytynytIlmoitusId: string
}

const MaksuttomuusOppijaSearchUndefinedResult = (
  props: MaksuttomuusOppijaSearchUndefinedResultProps,
) => (
  <div className={b("resultvalue")}>
    <T id={props.eiLöytynytIlmoitusId} />
  </div>
)

type MaksuttomuusOppijaSearchMatchResultProps = {
  henkilö: LöytyiHenkilöhakuResult
  prevPath: string
}

const MaksuttomuusOppijaSearchMatchResult = (
  props: MaksuttomuusOppijaSearchMatchResultProps,
) => {
  const basePath = useBasePath()
  const result = props.henkilö

  return (
    <div className={b("resultvalue")}>
      <T id="oppijahaku__löytyi" />
      {": "}
      <Link
        className={b("resultlink")}
        to={oppijaPath.href(basePath, {
          oppijaOid: result.oid,
          prev: props.prevPath,
        })}
      >
        {result.sukunimi} {result.etunimet} {result.hetu && `(${result.hetu})`}
      </Link>
    </div>
  )
}

type MaksuttomuusOppijaSearchErrorProps = {
  statusCode?: number
}

const MaksuttomuusOppijaSearchError = (
  props: MaksuttomuusOppijaSearchErrorProps,
) => {
  const [message, showAsError] = getMaksuttomuusErrorText(props.statusCode)
  return (
    <div className={b("resultvalue", { error: showAsError })}>{message}</div>
  )
}

const getMaksuttomuusErrorText = (statusCode?: number): [string, boolean] => {
  switch (statusCode) {
    case 400:
      return [t("oppijahaku__validointivirhe"), true]
    case 403:
      return [t("oppijahaku__maksuttomuus_sisäinen_virhe"), false]
    default:
      return [t("apivirhe__virheellinen_pyyntö", { virhe: status }), true]
  }
}
