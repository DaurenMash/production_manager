import apiClient from './client';

export type MonthCrossingMode = 'KEEP_START_DAY' | 'SPLIT';

export interface TenantSettings {
    tenantId: string;
    monthCrossingMode: MonthCrossingMode;
    baseCoefficientName: string;
    baseCoefficientValue: number;
    weekendCoefficientName: string;
    weekendCoefficientValue: number;
    nightCoefficientName: string;
    nightCoefficientValue: number;
    createdAt: string;
    updatedAt: string;
}

export interface UpdateTenantSettingsRequest {
    monthCrossingMode: MonthCrossingMode;
    baseCoefficientName: string;
    baseCoefficientValue: number;
    weekendCoefficientName: string;
    weekendCoefficientValue: number;
    nightCoefficientName: string;
    nightCoefficientValue: number;
}

export const tenantSettingsApi = {
    get: (): Promise<TenantSettings> =>
        apiClient.get('/employee-service/api/tenant-settings').then((res) => res.data),

    update: (data: UpdateTenantSettingsRequest): Promise<TenantSettings> =>
        apiClient.put('/employee-service/api/tenant-settings', data).then((res) => res.data),
};