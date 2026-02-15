import type { Session } from "./types";

export const TOKEN_KEY = "twitter_access_token";
export const ACTIONS_KEY = "twitter_actions";
export const USERNAME_KEY = "twitter_username";

export function loadSession(): Session | null {
  const token = localStorage.getItem(TOKEN_KEY);
  const actionsRaw = localStorage.getItem(ACTIONS_KEY);
  const username = localStorage.getItem(USERNAME_KEY);
  if (!token || !actionsRaw || !username) {
    return null;
  }
  try {
    const actions = JSON.parse(actionsRaw) as string[];
    if (!Array.isArray(actions)) {
      return null;
    }
    return { token, actions, username };
  } catch {
    return null;
  }
}

export function saveSession(session: Session): void {
  localStorage.setItem(TOKEN_KEY, session.token);
  localStorage.setItem(ACTIONS_KEY, JSON.stringify(session.actions));
  localStorage.setItem(USERNAME_KEY, session.username);
}

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(ACTIONS_KEY);
  localStorage.removeItem(USERNAME_KEY);
}
