import { defineConfig, devices } from "@playwright/test";

const backendPort = 18080;
const frontendPort = 14173;
const useExternalServers = process.env.PLAYWRIGHT_USE_EXTERNAL_SERVERS === "1";

export default defineConfig({
  testDir: "./tests/e2e",
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: "list",
  use: {
    baseURL: `http://127.0.0.1:${frontendPort}`,
    trace: "retain-on-failure"
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] }
    }
  ],
  webServer: useExternalServers
    ? undefined
    : [
        {
          command:
            `cd .. && ./mvnw -q -DskipTests spring-boot:run -Dspring-boot.run.profiles=e2e ` +
            `-Dspring-boot.run.arguments=--server.port=${backendPort}`,
          url: `http://127.0.0.1:${backendPort}/actuator/health`,
          timeout: 180_000,
          reuseExistingServer: false
        },
        {
          command: `npm run dev -- --config vite.e2e.config.ts --host 127.0.0.1 --port ${frontendPort}`,
          url: `http://127.0.0.1:${frontendPort}`,
          timeout: 60_000,
          reuseExistingServer: false
        }
      ]
});
