import { defineConfig, devices } from "@playwright/test"
import * as os from "os"

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */
// import dotenv from 'dotenv';
// import path from 'path';
// dotenv.config({ path: path.resolve(__dirname, '.env') });

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: "./test/e2e",
  /* Maximum time one test can run for. */
  timeout: 30 * 1000,
  expect: {
    /**
     * Maximum time expect() should wait for the condition to be met.
     * For example in `await expect(locator).toHaveText();`
     */
    timeout: 5 * 1000,
  },
  /* Run tests in files in parallel */
  fullyParallel: true,
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? 1 : undefined,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  reporter: "html",
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Base URL to use in actions like `await page.goto('/')`. */
    baseURL: "http://localhost:7051",

    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    trace: "on-first-retry",
  },

  /* Configure projects for major browsers */
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },

    // {
    //   name: "firefox",
    //   use: { ...devices["Desktop Firefox"] },
    // },
    //
    // {
    //   name: "webkit",
    //   use: { ...devices["Desktop Safari"] },
    // },

    /* Test against mobile viewports. */
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] },
    // },
    // {
    //   name: 'Mobile Safari',
    //   use: { ...devices['iPhone 12'] },
    // },

    /* Test against branded browsers. */
    // {
    //   name: 'Microsoft Edge',
    //   use: { ...devices['Desktop Edge'], channel: 'msedge' },
    // },
    // {
    //   name: 'Google Chrome',
    //   use: { ...devices['Desktop Chrome'], channel: 'chrome' },
    // },
  ],

  /* Run your local dev server before starting the tests */
  webServer: [
    {
      command: `KOSKI_BACKEND_HOST=http://localhost:${process.env.KOSKI_BACKEND_PORT || "7021"} pnpm run start:alb-proxy`,
      url: "http://localhost:7023/healthcheck",
      reuseExistingServer: !process.env.CI,
      stdout: "pipe",
      stderr: "pipe",
      timeout: 60 * 1000,
    },
    {
      command: `KOSKI_BACKEND_HOST=http://localhost:${process.env.KOSKI_BACKEND_PORT || "7021"} RESOURCE_ENDPOINT_URL=https://localhost:7022/koski/api/omadata-oauth2/resource-server ENABLE_LOCAL_MTLS=true CLIENT_ID=omadataoauth2sample pnpm exec tsx src/index.ts`,
      url: "http://localhost:7051/api/healthcheck",
      reuseExistingServer: !process.env.CI,
      stdout: "pipe",
      stderr: "pipe",
      timeout: 60 * 1000,
    },
    {
      command: `KOSKI_BACKEND_HOST=http://localhost:${process.env.KOSKI_BACKEND_PORT || "7021"} pnpm run start:java-sample`,
      url: "http://localhost:7052",
      reuseExistingServer: !process.env.CI,
      stdout: "pipe",
      stderr: "pipe",
      timeout: 2 * 60 * 1000,
    },
  ],
})

function getMyIp(): string {
  const addresses = Object.values(os.networkInterfaces())
    .flatMap((iface) => iface ?? []) // drop undefined
    .filter((a): a is os.NetworkInterfaceInfo => !!a) // type guard

  const addr = addresses.find((a) => a.family === "IPv4" && !a.internal)
  if (!addr) {
    throw new Error("No external IPv4 address found")
  }
  return addr.address
}
