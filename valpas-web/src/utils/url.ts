export const buildUrl = (host: string, params?: object) =>
  host + buildParamString(params)

export const buildParamString = (params?: object) =>
  params
    ? "?" +
      Object.entries(params)
        .map((entry) =>
          entry.map((token) => encodeURIComponent(token.toString())).join("="),
        )
        .join("&")
    : ""

export const publicUrl = () => process.env.PUBLIC_URL || "/"

export const virkailijaPublicUrl = () => publicUrl() + "/virkailija"

export const absoluteValpasUrl = (path?: string) =>
  location.origin + publicUrl() + (path || "")

export const absoluteKoskiUrl = (path?: string) =>
  location.origin + "/koski" + (path || "")
