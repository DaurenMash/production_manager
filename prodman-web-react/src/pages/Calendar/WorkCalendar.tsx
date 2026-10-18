import { useEffect, useMemo, useState } from 'react';
import { Space, Select, message, Card } from 'antd';
import dayjs from 'dayjs';
import 'dayjs/locale/ru';

import { departmentsApi } from '../../api/departments.api';
import type { Department } from '../../api/departments.api';
import { workCalendarApi } from '../../api/workCalendar.api';
import type { MonthSummary } from '../../api/workCalendar.api';
import YearOverview from './YearOverview';
import MonthGrid from './MonthGrid';
import DayBoard from './DayBoard';

dayjs.locale('ru');

type View = 'year' | 'month' | 'day';

const WorkCalendar = () => {
  const [view, setView] = useState<View>('year');
  const [year, setYear] = useState<number>(dayjs().year());
  const [month, setMonth] = useState<number>(dayjs().month());
  const [selectedDate, setSelectedDate] = useState<string | null>(null);

  const [departmentId, setDepartmentId] = useState<string | undefined>();
  const [departments, setDepartments] = useState<Department[]>([]);

  const [summary, setSummary] = useState<MonthSummary | null>(null);
  const [summaryLoading, setSummaryLoading] = useState(false);

  const loadDepartments = async () => {
    try {
      const dep = await departmentsApi.getAll(0, 200);
      setDepartments(dep.content ?? []);
    } catch {
      message.error('Ошибка загрузки отделов');
    }
  };

  useEffect(() => {
    loadDepartments();
  }, []);

  // Загрузка сводки месяца — только когда view = 'month'
  const monthRange = useMemo(() => {
    const start = dayjs().year(year).month(month).startOf('month');
    const end = dayjs().year(year).month(month).endOf('month');
    return {
      from: start.format('YYYY-MM-DD'),
      to: end.format('YYYY-MM-DD'),
    };
  }, [year, month]);

  useEffect(() => {
    if (view !== 'month') return;
    let cancelled = false;
    setSummaryLoading(true);
    workCalendarApi
        .getMonthSummary(monthRange.from, monthRange.to)
        .then((data) => {
          if (!cancelled) setSummary(data);
        })
        .catch(() => {
          if (!cancelled) message.error('Ошибка загрузки сводки месяца');
        })
        .finally(() => {
          if (!cancelled) setSummaryLoading(false);
        });
    return () => {
      cancelled = true;
    };
  }, [view, monthRange.from, monthRange.to]);

  const handleMonthClick = (m: number) => {
    setMonth(m);
    setView('month');
  };

  const handleDayClick = (dateStr: string) => {
    setSelectedDate(dateStr);
    setView('day');
  };

  const handlePrevMonth = () => {
    if (month === 0) {
      setYear(year - 1);
      setMonth(11);
    } else {
      setMonth(month - 1);
    }
  };

  const handleNextMonth = () => {
    if (month === 11) {
      setYear(year + 1);
      setMonth(0);
    } else {
      setMonth(month + 1);
    }
  };

  return (
      <div>
        <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} wrap>
          <Space>
            <span style={{ color: 'white', fontSize: 20, fontWeight: 600 }}>Рабочий календарь</span>
          </Space>
          <Space wrap>
            <Select
                placeholder="Все отделы"
                allowClear
                style={{ width: 220 }}
                value={departmentId}
                onChange={setDepartmentId}
                options={departments.map((d) => ({ value: d.id, label: `${d.code} — ${d.name}` }))}
            />
          </Space>
        </Space>

        {view === 'year' && (
            <YearOverview
                year={year}
                onYearChange={setYear}
                onMonthClick={handleMonthClick}
            />
        )}

        {view === 'month' && (
            <MonthGrid
                year={year}
                month={month}
                days={summary?.days ?? []}
                loading={summaryLoading}
                onDayClick={handleDayClick}
                onPrevMonth={handlePrevMonth}
                onNextMonth={handleNextMonth}
                onBackToYear={() => setView('year')}
            />
        )}

        {view === 'day' && selectedDate && (
            <Card
                style={{ background: '#2d2d3f', border: 'none', marginTop: 8 }}
                styles={{ body: { padding: 12 } }}
            >
              <DayBoard
                  date={selectedDate}
                  departmentId={departmentId}
                  onClose={() => {
                    setView('month');
                    setSelectedDate(null);
                  }}
              />
            </Card>
        )}
      </div>
  );
};

export default WorkCalendar;