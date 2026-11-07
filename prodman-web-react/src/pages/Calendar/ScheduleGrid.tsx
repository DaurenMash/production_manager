import { useMemo } from 'react';
import { Tooltip, Typography } from 'antd';
import dayjs from 'dayjs';
import 'dayjs/locale/ru';
import isoWeek from 'dayjs/plugin/isoWeek';
import type { ScheduleResponse, ScheduleCell, ScheduleRow } from '../../types/schedule.types';

dayjs.extend(isoWeek);
dayjs.locale('ru');

const { Text } = Typography;

const WEEKDAYS_SHORT = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
const WEEKEND_COLOR = '#ff7a45';
const MUTED_BG = '#aaa';
const MUTED_TEXT = '#aaa';

interface Props {
    data: ScheduleResponse | null;
    loading?: boolean;
}

function cellVisual(cell: ScheduleCell | undefined): {
    bg: string;
    color: string;
    label: string;
    title: string;
} {
    if (!cell || cell.kind === 'EMPTY' || cell.kind === 'DAY_OFF') {
        return { bg: 'transparent', color: MUTED_TEXT, label: 'В', title: 'Выходной' };
    }
    switch (cell.kind) {
        case 'SHIFT': {
            const bg = cell.color ?? '#3d5a80';
            const label = cell.shiftCode ?? '•';
            const parts = [cell.shiftName ?? label];
            if (cell.workstationName) parts.push(cell.workstationName);
            if (cell.totalHours != null) parts.push(`${cell.totalHours}ч`);
            if (Number(cell.nightHours) > 0) parts.push(`ночь ${cell.nightHours}ч`);
            if (cell.isDayOffWork) parts.push('выходной');
            return { bg, color: '#fff', label, title: parts.join(' · ') };
        }
        case 'VACATION':
            return { bg: MUTED_BG, color: MUTED_TEXT, label: 'Отп', title: 'Отпуск' };
        case 'SICK_LEAVE':
            return { bg: MUTED_BG, color: MUTED_TEXT, label: 'Бол', title: 'Больничный' };
        case 'UNAVAILABLE':
            return { bg: MUTED_BG, color: MUTED_TEXT, label: '—', title: 'Недоступен' };
        case 'FIRED':
            return { bg: MUTED_BG, color: MUTED_TEXT, label: 'Ув', title: 'Уволен' };
        default:
            return { bg: 'transparent', color: MUTED_TEXT, label: '?', title: cell.kind };
    }
}

function buildLegend(data: ScheduleResponse | null) {
    const seen = new Map<string, { code: string; color: string; name: string }>();
    if (!data) return [];
    for (const row of data.employees) {
        for (const c of row.days) {
            if (c.kind !== 'SHIFT' || !c.shiftCode) continue;
            if (!seen.has(c.shiftCode)) {
                seen.set(c.shiftCode, {
                    code: c.shiftCode,
                    color: c.color ?? '#3d5a80',
                    name: c.shiftName ?? c.shiftCode,
                });
            }
        }
    }
    return Array.from(seen.values());
}

const ScheduleGrid = ({ data, loading }: Props) => {
    // ВСЕ хуки — до любых return
    const days = useMemo(() => {
        if (!data || data.employees.length === 0) return [];
        return data.employees[0].days.map((c) => c.date);
    }, [data]);

    const legend = useMemo(() => buildLegend(data), [data]);

    const sortedRows = useMemo<ScheduleRow[]>(() => {
        if (!data) return [];
        return [...data.employees].sort((a, b) => {
            const da = a.departmentId ?? '';
            const db = b.departmentId ?? '';
            if (da !== db) return da.localeCompare(db);
            return (a.employeeFullName ?? '').localeCompare(b.employeeFullName ?? '');
        });
    }, [data]);

    const monthGroups = useMemo(() => {
        const groups: { label: string; span: number }[] = [];
        for (const d of days) {
            const m = dayjs(d);
            const label = m.format('MMMM YYYY');
            const last = groups[groups.length - 1];
            if (last && last.label === label) last.span += 1;
            else groups.push({ label, span: 1 });
        }
        return groups;
    }, [days]);

    // Теперь можно ранние return
    if (!data) {
        return <Text style={{ color: '#aaa' }}>Нет данных за выбранный период.</Text>;
    }
    if (sortedRows.length === 0) {
        return <Text style={{ color: '#aaa' }}>Нет сотрудников в выбранном диапазоне.</Text>;
    }

    return (
        <div style={{ overflowX: 'auto', paddingBottom: 8 }}>
            {legend.length > 0 && (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, marginBottom: 12 }}>
                    {legend.map((l) => (
                        <div key={l.code} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                            <span
                                style={{
                                    display: 'inline-block',
                                    width: 16,
                                    height: 16,
                                    borderRadius: 3,
                                    background: l.color,
                                }}
                            />
                            <Text style={{ color: '#ddd', fontSize: 12 }}>
                                <b>{l.code}</b> — {l.name}
                            </Text>
                        </div>
                    ))}
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <span style={{ display: 'inline-block', width: 16, height: 16, borderRadius: 3, background: MUTED_BG }} />
                        <Text style={{ color: '#ddd', fontSize: 12 }}>Отп/Бол/—</Text>
                    </div>
                </div>
            )}

            <table
                style={{
                    borderCollapse: 'collapse',
                    fontSize: 12,
                    color: 'white',
                    minWidth: 400 + days.length * 30,
                }}
            >
                <thead>
                <tr>
                    <th style={{ position: 'sticky', left: 0, background: '#1e1e2f', zIndex: 2 }} />
                    {monthGroups.map((g, i) => (
                        <th
                            key={i}
                            colSpan={g.span}
                            style={{
                                textAlign: 'center',
                                padding: '4px 0',
                                color: '#aaa',
                                fontWeight: 500,
                                borderBottom: '1px solid #3d3d4f',
                            }}
                        >
                            {g.label}
                        </th>
                    ))}
                    {/* Заголовки для итоговых колонок */}
                    <th colSpan={3} style={{ textAlign: 'center', padding: '4px 0', color: '#aaa', fontWeight: 500, borderBottom: '1px solid #3d3d4f', borderLeft: '2px solid #3d3d4f' }}>
                        Итого
                    </th>
                </tr>
                <tr>
                    <th
                        style={{
                            position: 'sticky',
                            left: 0,
                            background: '#1e1e2f',
                            textAlign: 'left',
                            padding: '6px 8px',
                            minWidth: 180,
                            zIndex: 2,
                            borderBottom: '1px solid #3d3d4f',
                        }}
                    >
                        Сотрудник
                    </th>
                    {days.map((d) => {
                        const m = dayjs(d);
                        const wd = m.isoWeekday();
                        const isWeekend = wd >= 6;
                        return (
                            <th
                                key={d}
                                title={m.format('dddd, DD MMMM YYYY')}
                                style={{
                                    width: 30,
                                    padding: '4px 0',
                                    textAlign: 'center',
                                    fontWeight: 500,
                                    color: isWeekend ? WEEKEND_COLOR : '#ccc',
                                    borderBottom: '1px solid #3d3d4f',
                                }}
                            >
                                <div style={{ fontSize: 10, color: isWeekend ? WEEKEND_COLOR : '#888' }}>
                                    {WEEKDAYS_SHORT[wd - 1]}
                                </div>
                                <div>{m.date()}</div>
                            </th>
                        );
                    })}
                    {/* Итоговые колонки */}
                    <th style={{ padding: '4px 8px', textAlign: 'center', color: '#aaa', borderBottom: '1px solid #3d3d4f', borderLeft: '2px solid #3d3d4f' }} title="Отработано часов">Ч</th>
                    <th style={{ padding: '4px 8px', textAlign: 'center', color: '#aaa', borderBottom: '1px solid #3d3d4f' }} title="Ночных часов">Н</th>
                    <th style={{ padding: '4px 8px', textAlign: 'center', color: '#aaa', borderBottom: '1px solid #3d3d4f' }} title="Количество смен">С</th>
                </tr>
                </thead>
                <tbody>
                {sortedRows.map((row) => {
                    // Считаем итоги
                    let totalHours = 0;
                    let nightHours = 0;
                    let shiftsCount = 0;

                    row.days.forEach(cell => {
                        if (cell.kind === 'SHIFT') {
                            shiftsCount++;
                            totalHours += Number(cell.totalHours || 0);
                            nightHours += Number(cell.nightHours || 0);
                        }
                    });

                    return (
                        <tr key={row.employeeId}>
                            <td
                                style={{
                                    position: 'sticky',
                                    left: 0,
                                    background: '#1e1e2f',
                                    padding: '4px 8px',
                                    whiteSpace: 'nowrap',
                                    borderBottom: '1px solid #2d2d3f',
                                    zIndex: 1,
                                }}
                            >
                                {row.employeeFullName ?? row.employeeId}
                            </td>
                            {row.days.map((cell) => {
                                const v = cellVisual(cell);
                                return (
                                    <td
                                        key={cell.date}
                                        style={{
                                            padding: 2,
                                            borderBottom: '1px solid #2d2d3f',
                                            borderRight: '1px solid #2d2d3f',
                                            textAlign: 'center',
                                        }}
                                    >
                                        <Tooltip title={v.title}>
                                            <div
                                                style={{
                                                    background: v.bg,
                                                    color: v.color,
                                                    borderRadius: 3,
                                                    padding: '2px 0',
                                                    fontSize: 10,
                                                    height: 20,
                                                    lineHeight: '16px',
                                                    overflow: 'hidden',
                                                    whiteSpace: 'nowrap',
                                                }}
                                            >
                                                {v.label}
                                            </div>
                                        </Tooltip>
                                    </td>
                                );
                            })}
                            {/* Ячейки с итогами */}
                            <td style={{ padding: '4px 8px', textAlign: 'right', borderBottom: '1px solid #2d2d3f', borderLeft: '2px solid #3d3d4f', fontWeight: 'bold' }}>
                                {totalHours > 0 ? totalHours : '—'}
                            </td>
                            <td style={{ padding: '4px 8px', textAlign: 'right', borderBottom: '1px solid #2d2d3f', color: nightHours > 0 ? '#d3adf7' : '#666' }}>
                                {nightHours > 0 ? nightHours : '—'}
                            </td>
                            <td style={{ padding: '4px 8px', textAlign: 'right', borderBottom: '1px solid #2d2d3f' }}>
                                {shiftsCount > 0 ? shiftsCount : '—'}
                            </td>
                        </tr>
                    );
                })}
                </tbody>
            </table>

            {loading && (
                <div style={{ textAlign: 'center', color: '#aaa', marginTop: 12 }}>Загрузка…</div>
            )}
        </div>
    );
};

export default ScheduleGrid;