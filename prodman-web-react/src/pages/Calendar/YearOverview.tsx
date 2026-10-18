import { useMemo } from 'react';
import { Typography } from 'antd';
import dayjs, { Dayjs } from 'dayjs';
import 'dayjs/locale/ru';
import isoWeek from 'dayjs/plugin/isoWeek';

dayjs.extend(isoWeek);

const { Title, Text } = Typography;

dayjs.locale('ru');

const MONTH_NAMES = [
    'Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь',
    'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь',
];

const WEEKDAYS = ['П', 'В', 'С', 'Ч', 'П', 'С', 'В'];

interface Props {
    year: number;
    onYearChange: (year: number) => void;
    onMonthClick: (month: number) => void;
}

const YearOverview = ({ year, onYearChange, onMonthClick }: Props) => {
    const months = useMemo(() => {
        const result: { month: number; weeks: (Dayjs | null)[][] }[] = [];
        for (let m = 0; m < 12; m++) {
            const start = dayjs().year(year).month(m).startOf('month');
            const end = dayjs().year(year).month(m).endOf('month');
            // start.isoWeekday() — 1..7 (Пн=1)
            const firstWeekday = start.isoWeekday();
            const daysInMonth = end.date();

            const cells: (Dayjs | null)[] = [];
            // Пустые ячейки до первого дня
            for (let i = 1; i < firstWeekday; i++) cells.push(null);
            for (let d = 1; d <= daysInMonth; d++) cells.push(start.date(d));
            // Дополняем до 42 (6 строк × 7)
            while (cells.length < 42) cells.push(null);

            const weeks: (Dayjs | null)[][] = [];
            for (let i = 0; i < cells.length; i += 7) {
                weeks.push(cells.slice(i, i + 7));
            }
            result.push({ month: m, weeks });
        }
        return result;
    }, [year]);

    const today = dayjs();

    return (
        <div style={{ padding: '0 8px' }}>
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', marginBottom: 24, gap: 16 }}>
                <Title
                    level={1}
                    style={{
                        color: '#2E8B57',
                        margin: 0,
                        fontSize: 72,
                        cursor: 'pointer',
                        userSelect: 'none',
                    }}
                    onClick={() => onYearChange(year - 1)}
                    title="Предыдущий год"
                >
                    ‹
                </Title>
                <Title level={1} style={{ color: 'white', margin: 0, fontSize: 72 }}>
                    {year}
                </Title>
                <Title
                    level={1}
                    style={{
                        color: '#2E8B57',
                        margin: 0,
                        fontSize: 72,
                        cursor: 'pointer',
                        userSelect: 'none',
                    }}
                    onClick={() => onYearChange(year + 1)}
                    title="Следующий год"
                >
                    ›
                </Title>
            </div>

            <div
                style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(4, 1fr)',
                    gap: 16,
                    maxWidth: 1200,
                    margin: '0 auto',
                }}
            >
                {months.map(({ month, weeks }) => (
                    <div
                        key={month}
                        onClick={() => onMonthClick(month)}
                        style={{
                            background: '#2d2d3f',
                            border: '1px solid #3d3d4f',
                            borderRadius: 8,
                            padding: 12,
                            cursor: 'pointer',
                            transition: 'border-color 0.15s, transform 0.15s',
                        }}
                        onMouseEnter={(e) => {
                            e.currentTarget.style.borderColor = '#2E8B57';
                            e.currentTarget.style.transform = 'translateY(-2px)';
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.borderColor = '#3d3d4f';
                            e.currentTarget.style.transform = 'translateY(0)';
                        }}
                    >
                        <Text style={{ color: 'white', fontWeight: 600, fontSize: 14, display: 'block', marginBottom: 6 }}>
                            {MONTH_NAMES[month]}
                        </Text>

                        <div
                            style={{
                                display: 'grid',
                                gridTemplateColumns: 'repeat(7, 1fr)',
                                gap: 2,
                                fontSize: 11,
                            }}
                        >
                            {WEEKDAYS.map((wd, i) => (
                                <div
                                    key={`wd-${i}`}
                                    style={{ textAlign: 'center', color: '#666', padding: '2px 0' }}
                                >
                                    {wd}
                                </div>
                            ))}
                            {weeks.flat().map((d, i) => {
                                if (!d) return <div key={`e-${i}`} />;
                                const isToday =
                                    d.year() === today.year() &&
                                    d.month() === today.month() &&
                                    d.date() === today.date();
                                const isWeekend = d.isoWeekday() === 6 || d.isoWeekday() === 7;
                                return (
                                    <div
                                        key={d.format('YYYY-MM-DD')}
                                        style={{
                                            textAlign: 'center',
                                            padding: '2px 0',
                                            borderRadius: 2,
                                            color: isToday ? 'white' : (isWeekend ? '#ff7a45' : '#aaa'),
                                            background: isToday ? '#2E8B57' : 'transparent',
                                        }}
                                    >
                                        {d.date()}
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default YearOverview;