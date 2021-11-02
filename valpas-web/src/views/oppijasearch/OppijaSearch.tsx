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
import "./OppijaSearch.less"

const b = bem("oppijasearch")

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

export type OppijaSearchProps = {
  searchState: ApiMethodState<HenkilöhakuResult>
  onQuery: (query: string) => void
  prevPath: string
}

export const OppijaSearch = (props: OppijaSearchProps) => {
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
            <OppijaSearchResults
              hakutulos={props.searchState.data}
              prevPath={props.prevPath}
            />
          )}
          {isError(props.searchState) && (
            <OppijaSearchError statusCode={props.searchState.status} />
          )}
        </div>
      </TextField>
    </Form>
  )
}

type OppijaSearchResultsProps = {
  hakutulos: HenkilöhakuResult
  prevPath: string
}

const OppijaSearchResults = (props: OppijaSearchResultsProps) => {
  if (isLöytyiHenkilöhakuResult(props.hakutulos)) {
    return (
      <OppijaSearchMatchResult
        henkilö={props.hakutulos}
        prevPath={props.prevPath}
      />
    )
  }
  return <OppijaSearchUndefinedResult />
}

const OppijaSearchUndefinedResult = () => (
  <div className={b("resultvalue")}>
    <T id={"oppijahaku__ei_tuloksia"} />
  </div>
)

type OppijaSearchMatchResultProps = {
  henkilö: LöytyiHenkilöhakuResult
  prevPath: string
}

const OppijaSearchMatchResult = (props: OppijaSearchMatchResultProps) => {
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

type OppijaSearchErrorProps = {
  statusCode?: number
}

const OppijaSearchError = (props: OppijaSearchErrorProps) => {
  const [message, showAsError] = getErrorText(props.statusCode)
  return (
    <div className={b("resultvalue", { error: showAsError })}>{message}</div>
  )
}

const getErrorText = (statusCode?: number): [string, boolean] => {
  switch (statusCode) {
    case 400:
      return [t("oppijahaku__validointivirhe"), true]
    case 403:
      return [t("oppijahaku__ei_tuloksia"), false]
    default:
      return [t("apivirhe__virheellinen_pyyntö", { virhe: status }), true]
  }
}
