export const buildUrl = (host: string, params?: object) =>
  host + buildParamString(params)

export const buildParamString = (params?: object) =>
  params
    ? "?" +
      Object.entries(params)
        .map((entry) =>
          entry.map((token) => encodeURIComponent(token.toString())).join("=")
        )
        .join("&")
    : ""

export const publicUrl = () => process.env.PUBLIC_URL || "/"

export const absoluteUrl = (path?: string) =>
  location.host + publicUrl() + (path || "")
