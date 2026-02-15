import "@testing-library/jest-dom/vitest";
import { afterEach, beforeEach } from "vitest";
import { cleanup } from "@testing-library/react";

function createStorage(): Storage {
  const store = new Map<string, string>();
  return {
    get length() {
      return store.size;
    },
    clear() {
      store.clear();
    },
    getItem(key: string) {
      return store.has(key) ? store.get(key)! : null;
    },
    key(index: number) {
      return Array.from(store.keys())[index] ?? null;
    },
    removeItem(key: string) {
      store.delete(key);
    },
    setItem(key: string, value: string) {
      store.set(key, value);
    }
  };
}

if (typeof window !== "undefined") {
  const storage = createStorage();
  Object.defineProperty(window, "localStorage", {
    value: storage,
    configurable: true
  });
  Object.defineProperty(globalThis, "localStorage", {
    value: storage,
    configurable: true
  });
}

afterEach(() => {
  cleanup();
});

beforeEach(() => {
  localStorage.clear();
});
