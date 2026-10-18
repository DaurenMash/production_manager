import { useMemo } from 'react';
import { Typography, Tooltip } from 'antd';
import dayjs, { Dayjs } from 'dayjs';
import 'dayjs/locale/ru';
import isoWeek from 'dayjs/plugin/isoWeek';
import type { DaySummary } from '../../api/workCalendar.api';

dayjs.extend(isoWeek);
dayjs.locale('ru');

const { Text } = Typography;

const MONTH_NAMES = [
    'Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь',
    'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь',
];

const WEEKDAYS = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

const MAX_VISIBLE_WORKSTATIONS = 10;

interface Props {
    year: number;
    month: number; // 0..11
    days: DaySummary[];
    loading?: boolean;
    onDayClick: (date: string) => void;
    onPrevMonth: () => void;
    onNextMonth: () => void;
    onBackToYear: () => void;
}

/** Определяем цвет ячейки по данным дня. */
const pickDayColor = (d: DaySummary): { bg: string; border: string } => {
    // Нет станков вообще — серый
    if (d.totalWorkstations === 0) {
        return { bg: 'transparent', border: '#3d3d4f' };
    }
    // Все слоты заполнены и есть слоты — зелёный
    if (d.totalSlots > 0 && d.openSlots === 0) {
        return { bg: 'rgba(46, 204, 113, 0.15)', border: '#2ecc71' };
    }
    // Есть пустые слоты, но их ≤ 20% — оранжевый
    const openPercent = d.totalSlots > 0 ? d.openSlots / d.totalSlots : 0;
    if (d.totalSlots > 0 && openPercent <= 0.2) {
        return { bg: 'rgba(243, 156, 18, 0.15)', border: '#f39c12' };
    }
    // Остальное — красный
    return { bg: 'rgba(231, 76, 60, 0.15)', border: '#e74c3c' };
};

const MonthGrid = ({
                       year, month, days, loading,
                       onDayClick, onPrevMonth, onNextMonth, onBackToYear,
                   }: Props) => {
    const cells = useMemo(() => {
        const start = dayjs().year(year).month(month).startOf('month');
        const end = dayjs().year(year).month(month).endOf('month');
        const firstWeekday = start.isoWeekday();
        const daysInMonth = end.date();

        const list: (Dayjs | null)[] = [];
        for (let i = 1; i < firstWeekday; i++) list.push(null);
        for (let d = 1; d <= daysInMonth; d++) list.push(start.date(d));
        while (list.length < 42) list.push(null);
        return list;
    }, [year, month]);

    const summaryByDate = useMemo(() => {
        const map = new Map<string, DaySummary>();
        for (const d of days) map.set(d.date, d);
        return map;
    }, [days]);

    const today = dayjs();

    return (
        <div style={{ padding: '0 8px' }}>
            <div
                style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: 16,
                }}
            >
                <Text
                    style={{ color: '#2E8B57', cursor: 'pointer', fontSize: 14 }}
                    onClick={onBackToYear}
                >
                    ← Весь год
                </Text>

                <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                    <Text
                        style={{ color: '#2E8B57', cursor: 'pointer', fontSize: 28, userSelect: 'none' }}
                        onClick={onPrevMonth}
                        title="Предыдущий месяц"
                    >
                        ‹
                    </Text>
                    <Text style={{ color: 'white', fontSize: 24, fontWeight: 600 }}>
                        {MONTH_NAMES[month]} {year}
                    </Text>
                    <Text
                        style={{ color: '#2E8B57', cursor: 'pointer', fontSize: 28, userSelect: 'none' }}
                        onClick={onNextMonth}
                        title="Следующий месяц"
                    >
                        ›
                    </Text>
                </div>

                <div style={{ width: 120 }} />
            </div>

            {/* Заголовок недели */}
            <div
                style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(7, 1fr)',
                    gap: 4,
                    marginBottom: 8,
                }}
            >
                {WEEKDAYS.map((wd, i) => (
                    <div
                        key={wd + i}
                        style={{
                            textAlign: 'center',
                            color: i >= 5 ? '#ff7a45' : '#aaa',
                            fontWeight: 600,
                            fontSize: 13,
                            padding: 4,
                        }}
                    >
                        {wd}
                    </div>
                ))}
            </div>

            {/* Сетка */}
            <div
                style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(7, 1fr)',
                    gap: 4,
                }}
            >
                {cells.map((d, idx) => {
                    if (!d) return <div key={`empty-${idx}`} style={{ minHeight: 110 }} />;

                    const dateStr = d.format('YYYY-MM-DD');
                    const summary = summaryByDate.get(dateStr);
                    const isToday =
                        d.year() === today.year() &&
                        d.month() === today.month() &&
                        d.date() === today.date();
                    const isWeekend = d.isoWeekday() >= 6;

                    const color = summary ? pickDayColor(summary) : { bg: 'transparent', border: '#3d3d4f' };
                    const showNames =
                        summary &&
                        summary.workstations.length > 0 &&
                        summary.workstations.length <= MAX_VISIBLE_WORKSTATIONS;

                    return (
                        <Tooltip
                            key={dateStr}
                            title={
                                summary
                                    ? `${summary.workingWorkstations} из ${summary.totalWorkstations} станков · ` +
                                    `${summary.filledSlots} / ${summary.totalSlots} слотов`
                                    : 'Нет данных'
                            }
                            placement="top"
                        >
                            <div
                                onClick={() => onDayClick(dateStr)}
                                style={{
                                    minHeight: 110,
                                    padding: 6,
                                    borderRadius: 6,
                                    border: `2px solid ${isToday ? '#2E8B57' : color.border}`,
                                    background: color.bg,
                                    cursor: 'pointer',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    transition: 'background 0.15s',
                                }}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.background = 'rgba(46, 139, 87, 0.25)';
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.background = color.bg;
                                }}
                            >
                                <div
                                    style={{
                                        color: isWeekend ? '#ff7a45' : 'white',
                                        fontWeight: 600,
                                        fontSize: 13,
                                        marginBottom: 4,
                                        display: 'flex',
                                        justifyContent: 'space-between',
                                    }}
                                >
                                    <span>{d.date()}</span>
                                    {summary && summary.totalSlots > 0 && (
                                        <span style={{ color: '#aaa', fontSize: 11, fontWeight: 400 }}>
                      {summary.filledSlots}/{summary.totalSlots}
                    </span>
                                    )}
                                </div>

                                {summary && summary.workingWorkstations === 0 && summary.totalWorkstations > 0 && (
                                    <Text style={{ color: '#ff7a45', fontSize: 11 }}>Простой</Text>
                                )}

                                {showNames && summary && (
                                    <div
                                        style={{
                                            display: 'flex',
                                            flexDirection: 'column',
                                            gap: 1,
                                            fontSize: 10,
                                            color: '#ccc',
                                            overflow: 'hidden',
                                        }}
                                    >
                                        {summary.workstations.slice(0, MAX_VISIBLE_WORKSTATIONS).map((w) => (
                                            <div
                                                key={w.id}
                                                style={{
                                                    overflow: 'hidden',
                                                    textOverflow: 'ellipsis',
                                                    whiteSpace: 'nowrap',
                                                }}
                                                title={w.name ?? w.id}
                                            >
                                                • {w.name ?? w.id.slice(0, 8)}
                                            </div>
                                        ))}
                                    </div>
                                )}

                                {summary && !showNames && summary.workingWorkstations > 0 && (
                                    <div style={{ fontSize: 11, color: '#2ecc71', marginTop: 2 }}>
                                        🏭 {summary.workingWorkstations}
                                    </div>
                                )}
                            </div>
                        </Tooltip>
                    );
                })}
            </div>

            {loading && (
                <div style={{ textAlign: 'center', color: '#aaa', marginTop: 12 }}>Загрузка…</div>
            )}
        </div>
    );
};

export default MonthGrid;