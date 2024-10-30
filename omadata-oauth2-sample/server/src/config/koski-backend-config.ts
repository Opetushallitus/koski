export const koskiBackendHost =
  process.env.KOSKI_BACKEND_HOST || 'http://localhost:7021'
export const resourceEndpointUrl =
  process.env.RESOURCE_ENDPOINT_URL ||
  `${koskiBackendHost}/koski/api/omadata-oauth2/resource-server`
