export enum Role {
  ADMIN = 'ADMIN',
  OPERATOR = 'OPERATOR',
  ANALYST = 'ANALYST',
  VISITOR = 'VISITOR'
}

export interface User {
  id: string;
  username: string;
  email: string;
  role: Role;
  enabled: boolean;
  createdAt: string;
}
