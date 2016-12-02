import Http from './http'

export const userP = Http.get('/koski/user').mapError()
  .toProperty()

export const logout = () => {
  document.location = '/koski/user/logout'
}