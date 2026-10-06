import { defaultTimeout } from "./timeouts"

export const sleep = (time: number) =>
  new Promise((resolve) => setTimeout(resolve, time))

export const eventually = async <T>(
  test: () => Promise<T>,
  timeout = defaultTimeout,
): Promise<T> => {
  const expirationTime = new Date().getTime() + timeout
  let error = null
  while (true) {
    if (new Date().getTime() > expirationTime) {
      throw error
    }
    try {
      return await test()
    } catch (err) {
      error = err
      await sleep(10)
    }
  }
}

// Jestin expectille ei voi antaa omaa virheviestiä, joten se lisätään
// virheilmoitukseen tällä.
export const withMessage = (message: string, doAssert: () => void) => {
  try {
    doAssert()
  } catch (error) {
    if (error instanceof Error) {
      error.message = `${message}\n\n${error.message}`
    }
    throw error
  }
}
