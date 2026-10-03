export const Role = {
  PLATFORM_ADMIN: 'PLATFORM_ADMIN',
  ADMIN: 'ADMIN',
  OPERATOR: 'OPERATOR',
  ANALYST: 'ANALYST',
  VISITOR: 'VISITOR',
} as const;
export type Role = (typeof Role)[keyof typeof Role];

export const TenantStatus = {
  TRIAL: 'TRIAL',
  ACTIVE: 'ACTIVE',
  SUSPENDED: 'SUSPENDED',
  CANCELLED: 'CANCELLED',
} as const;
export type TenantStatus = (typeof TenantStatus)[keyof typeof TenantStatus];

export const TenantPlan = {
  FREE: 'FREE',
  BASIC: 'BASIC',
  UNLIM: 'UNLIM',
} as const;
export type TenantPlan = (typeof TenantPlan)[keyof typeof TenantPlan];

export interface User {
  id: string;
  tenantId: string | null;
  username: string;
  email: string;
  role: Role;
  enabled: boolean;
  createdAt: string;
}

export interface LoginRequest {
  /** Slug тенанта. Не нужен для PLATFORM_ADMIN. */
  slug?: string;
  username: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  userId: string;
  username: string;
  email: string;
  role: Role;
  tenantId: string | null;
  tenantSlug: string | null;
  tenantStatus: TenantStatus | null;
  tenantPlan: TenantPlan | null;
  expiresIn: number;
}

export interface RegisterTenantRequest {
  tenantName: string;
  tenantSlug: string;
  username: string;
  email: string;
  password: string;
}