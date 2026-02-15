import { expect, test } from "@playwright/test";
import { login, signup, uniqueSuffix } from "./helpers";

test("signup then login reaches dashboard", async ({ page }) => {
  const creds = await signup(page, uniqueSuffix());
  await login(page, creds.username, creds.password);
  await expect(page.getByText(`Signed in as @${creds.username}`)).toBeVisible();
});

test("invalid login shows API error", async ({ page }) => {
  await page.goto("/login");
  await page.getByLabel("Username").fill("does-not-exist");
  await page.getByLabel("Password").fill("bad-pass");
  await page.getByTestId("login-submit").click();

  await expect(page.getByText("Invalid credentials")).toBeVisible();
});
