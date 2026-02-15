export type User = {
  id: number;
  firstName: string | null;
  lastName: string | null;
  email: string;
  handle: string;
};

export type Tweet = {
  id: number;
  content: string;
  author: User;
  timestamp?: string;
};

export type UserRequest = {
  firstName: string;
  lastName: string;
  email: string;
  handle: string;
  username: string;
  password: string;
};

export type TweetRequest = {
  content: string;
  authorId: number;
};

export type TokenRequest = {
  username: string;
  password: string;
};

export type TokenResponse = {
  access_token: string;
  actions: string[];
};

export type Session = {
  token: string;
  actions: string[];
  username: string;
};
