import { defineConfig } from 'vitest/config'

export default defineConfig({
  root: import.meta.dirname,
  test: {
    include: ['test/unit-test/**/*.test.ts'],
    setupFiles: ['test/unit-test/setup/browserGlobals.ts']
  }
})
