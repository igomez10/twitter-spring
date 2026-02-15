import type {
  TokenRequest,
  TokenResponse,
  Tweet,
  TweetRequest,
  User,
  UserRequest
} from "./types";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "/api";

export class ApiClientError extends Error {
  status: number;
  body: unknown;

  constructor(status: number, message: string, body: unknown) {
    super(message);
    this.status = status;
    this.body = body;
  }
}

type RequestOptions = {
  method?: string;
  token?: string;
  body?: unknown;
};

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = {};
  if (options.token) {
    headers.Authorization = `Bearer ${options.token}`;
  }
  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method ?? "GET",
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body)
  });

  const textBody = await response.text();
  let body: unknown = null;
  if (textBody) {
    try {
      body = JSON.parse(textBody) as unknown;
    } catch {
      body = textBody;
    }
  }

  if (!response.ok) {
    const message =
      typeof body === "object" && body !== null && "message" in body && typeof body.message === "string"
        ? body.message
        : response.statusText || "Request failed";
    throw new ApiClientError(response.status, message, body);
  }

  return body as T;
}

export function createUser(payload: UserRequest): Promise<User> {
  return request<User>("/users", { method: "POST", body: payload });
}

export function requestToken(payload: TokenRequest): Promise<TokenResponse> {
  return request<TokenResponse>("/oauth/token", { method: "POST", body: payload });
}

export function listUsers(token: string): Promise<User[]> {
  return request<User[]>("/users", { token });
}

export function updateUser(id: number, payload: UserRequest, token: string): Promise<User> {
  return request<User>(`/users/${id}`, { method: "PUT", token, body: payload });
}

export function deleteUser(id: number, token: string): Promise<void> {
  return request<void>(`/users/${id}`, { method: "DELETE", token });
}

export function listTweets(token: string): Promise<Tweet[]> {
  return request<Tweet[]>("/tweets", { token });
}

export function createTweet(payload: TweetRequest, token: string): Promise<Tweet> {
  return request<Tweet>("/tweets", { method: "POST", token, body: payload });
}

export function updateTweet(id: number, payload: TweetRequest, token: string): Promise<Tweet> {
  return request<Tweet>(`/tweets/${id}`, { method: "PUT", token, body: payload });
}

export function deleteTweet(id: number, token: string): Promise<void> {
  return request<void>(`/tweets/${id}`, { method: "DELETE", token });
}
