import { expect, type Page } from "@playwright/test";

type AuthFixture = {
  username: string;
  password: string;
  handle: string;
  email: string;
};

export function uniqueSuffix(): string {
  return `${Date.now()}-${Math.floor(Math.random() * 10000)}`;
}

export async function signup(page: Page, suffix: string): Promise<AuthFixture> {
  const username = `user${suffix}`;
  const password = `Pass${suffix}!`;
  const handle = `handle${suffix}`;
  const email = `user${suffix}@example.com`;

  await page.goto("/signup");
  await page.getByLabel("First name").fill("Play");
  await page.getByLabel("Last name").fill("Wright");
  await page.getByLabel("Email").fill(email);
  await page.getByLabel("Handle").fill(handle);
  await page.getByLabel("Username").fill(username);
  await page.getByLabel("Password").fill(password);
  await page.getByTestId("signup-submit").click();

  await expect(page.getByText("Account created. You can now sign in.")).toBeVisible();

  return { username, password, handle, email };
}

export async function login(page: Page, username: string, password: string): Promise<void> {
  await page.goto("/login");
  await page.getByLabel("Username").fill(username);
  await page.getByLabel("Password").fill(password);
  await page.getByTestId("login-submit").click();
  await expect(page.getByRole("heading", { name: "Twitter Spring Console" })).toBeVisible();
}

export async function signupAndLogin(page: Page, suffix = uniqueSuffix()): Promise<AuthFixture> {
  const fixture = await signup(page, suffix);
  await login(page, fixture.username, fixture.password);
  return fixture;
}
