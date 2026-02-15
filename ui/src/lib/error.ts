import { ApiClientError } from "./api";

export function getErrorMessage(error: unknown): string {
  if (error instanceof ApiClientError) {
    if (typeof error.body === "object" && error.body !== null && "message" in error.body) {
      const message = (error.body as { message?: unknown }).message;
      if (typeof message === "string" && message.trim().length > 0) {
        return message;
      }
    }
    if (error.status === 401) {
      return "Invalid credentials or expired session";
    }
    if (error.status === 403) {
      return "You do not have permission to perform this action";
    }
    if (error.status === 409) {
      return "Resource already exists";
    }
    return `Request failed (${error.status})`;
  }

  if (error instanceof Error && error.message.trim().length > 0) {
    return error.message;
  }

  return "Unexpected error";
}

export function isUnauthorized(error: unknown): boolean {
  return error instanceof ApiClientError && (error.status === 401 || error.status === 403);
}
