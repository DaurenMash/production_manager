import { useEffect, useMemo, useState, type ReactNode } from 'react';
import {
  Typography, Space, Button, Select, DatePicker, message, Tag, Modal, Form,
  Input, InputNumber, Popconfirm, Card, Empty,
} from 'antd';
import {
  LeftOutlined, RightOutlined, ReloadOutlined,
} from '@ant-design/icons';
import dayjs, { Dayjs } from 'dayjs';
import isoWeek from 'dayjs/plugin/isoWeek';
import {
  DndContext, DragOverlay,
  PointerSensor, useSensor, useSensors, useDroppable, useDraggable,
  type DragEndEvent, type DragStartEvent,
} from '@dnd-kit/core';

import { employeesApi } from '../../api/employees.api';
import type { Employee } from '../../api/employees.api';
import { departmentsApi } from '../../api/departments.api';
import type { Department } from '../../api/departments.api';
import { workstationsApi } from '../../api/workstations.api';
import type { Workstation } from '../../api/workstations.api';
import { shiftPatternsApi } from '../../api/shiftPatterns.api';
import type { ShiftPattern } from '../../api/shiftPatterns.api';
import { shiftAssignmentsApi } from '../../api/shiftAssignments.api';
import type { ShiftAssignment } from '../../api/shiftAssignments.api';

dayjs.extend(isoWeek);

const { Title, Text } = Typography;

type ViewMode = 'week' | 'month';

interface CellKey {
  employeeId: string;
  date: string;
}

const WorkCalendar = () => {
  const [view, setView] = useState<ViewMode>('week');
  const [anchor, setAnchor] = useState<Dayjs>(dayjs());
  const [departmentId, setDepartmentId] = useState<string | undefined>();

  const [employees, setEmployees] = useState<Employee[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [workstations, setWorkstations] = useState<Workstation[]>([]);
  const [patterns, setPatterns] = useState<ShiftPattern[]>([]);
  const [assignments, setAssignments] = useState<ShiftAssignment[]>([]);

  const [activePattern, setActivePattern] = useState<ShiftPattern | null>(null);
  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 5 } }));

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<ShiftAssignment | null>(null);
  const [form] = Form.useForm();
  const [saving, setSaving] = useState(false);

  const rangeStart = useMemo(() => {
    return view === 'week' ? anchor.startOf('isoWeek') : anchor.startOf('month');
  }, [view, anchor]);

  const rangeEnd = useMemo(() => {
    return view === 'week' ? anchor.endOf('isoWeek') : anchor.endOf('month');
  }, [view, anchor]);

  const days = useMemo(() => {
    const list: Dayjs[] = [];
    let d = rangeStart;
    while (d.isBefore(rangeEnd) || d.isSame(rangeEnd, 'day')) {
      list.push(d);
      d = d.add(1, 'day');
    }
    return list;
  }, [rangeStart, rangeEnd]);

  const loadDictionaries = async () => {
    try {
      const [emp, dep, ws, pat] = await Promise.all([
        employeesApi.getAll(0, 500),
        departmentsApi.getAll(0, 200),
        workstationsApi.getAll(0, 500),
        shiftPatternsApi.getAll(0, 200),
      ]);
      setEmployees(emp.content ?? []);
      setDepartments(dep.content ?? []);
      setWorkstations(ws.content ?? []);
      setPatterns(pat.content ?? []);
    } catch {
      message.error('Ошибка загрузки справочников');
    }
  };

  const loadAssignments = async () => {
    try {
      const data = await shiftAssignmentsApi.listByRange(
          rangeStart.format('YYYY-MM-DD'),
          rangeEnd.format('YYYY-MM-DD'),
      );
      setAssignments(data ?? []);
    } catch {
      message.error('Ошибка загрузки назначений');
    }
  };

  useEffect(() => {
    loadDictionaries();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    loadAssignments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [rangeStart.format('YYYY-MM-DD'), rangeEnd.format('YYYY-MM-DD')]);

  const filteredEmployees = departmentId
      ? employees.filter((e) => e.departmentId === departmentId)
      : employees;

  const assignmentsByCell = useMemo(() => {
    const map = new Map<string, ShiftAssignment[]>();
    for (const a of assignments) {
      const key = `${a.employeeId}|${a.date}`;
      const list = map.get(key) ?? [];
      list.push(a);
      map.set(key, list);
    }
    return map;
  }, [assignments]);

  const handlePrev = () => {
    setAnchor(view === 'week' ? anchor.subtract(1, 'week') : anchor.subtract(1, 'month'));
  };
  const handleNext = () => {
    setAnchor(view === 'week' ? anchor.add(1, 'week') : anchor.add(1, 'month'));
  };
  const handleToday = () => setAnchor(dayjs());

  const handleDragStart = (e: DragStartEvent) => {
    const data = e.active.data.current as any;
    if (data?.type === 'pattern') setActivePattern(data.pattern);
  };

  const handleDragEnd = async (e: DragEndEvent) => {
    setActivePattern(null);
    const overData = e.over?.data.current as any;
    const activeData = e.active.data.current as any;
    if (!overData || overData.type !== 'cell') return;

    const cell: CellKey = overData.cell;

    if (activeData?.type === 'pattern') {
      const pattern: ShiftPattern = activeData.pattern;
      const employee = employees.find((emp) => emp.id === cell.employeeId);
      const ws = workstations.find((w) => w.departmentId === employee?.departmentId);
      if (!ws) {
        message.error('У выбранного сотрудника нет отдела со станциями');
        return;
      }
      try {
        await shiftAssignmentsApi.create({
          date: cell.date,
          employeeId: cell.employeeId,
          workstationId: ws.id,
          shiftPatternId: pattern.id,
        });
        message.success('Назначение создано');
        loadAssignments();
      } catch (err: any) {
        const msg = err.response?.data?.message || 'Ошибка создания';
        if (msg.includes('already has a shift')) {
          Modal.confirm({
            title: 'У сотрудника уже есть смена в этот день',
            content: 'Создать вторую смену с override?',
            okText: 'Да, override',
            cancelText: 'Отмена',
            onOk: async () => {
              try {
                await shiftAssignmentsApi.create({
                  date: cell.date,
                  employeeId: cell.employeeId,
                  workstationId: ws.id,
                  shiftPatternId: pattern.id,
                  force: true,
                  overrideReason: 'Назначено через drag-n-drop',
                });
                message.success('Создано с override');
                loadAssignments();
              } catch (e: any) {
                message.error(e.response?.data?.message || 'Ошибка');
              }
            },
          });
        } else {
          message.error(msg);
        }
      }
      return;
    }

    if (activeData?.type === 'assignment') {
      const a: ShiftAssignment = activeData.assignment;
      if (a.date === cell.date && a.employeeId === cell.employeeId) return;
      try {
        await shiftAssignmentsApi.update(a.id, {
          date: cell.date,
          employeeId: cell.employeeId,
          workstationId: a.workstationId,
          shiftPatternId: a.shiftPatternId,
        });
        message.success('Перенесено');
        loadAssignments();
      } catch (err: any) {
        message.error(err.response?.data?.message || 'Ошибка переноса');
      }
    }
  };

  const openCreateModal = (employeeId: string, date: string) => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({
      employeeId,
      date: dayjs(date),
    });
    setModalOpen(true);
  };

  const openEditModal = (a: ShiftAssignment) => {
    setEditing(a);
    form.setFieldsValue({
      employeeId: a.employeeId,
      date: dayjs(a.date),
      workstationId: a.workstationId,
      shiftPatternId: a.shiftPatternId,
      actualHours: a.actualHours,
      actualNightHours: a.actualNightHours,
      comment: a.comment,
    });
    setModalOpen(true);
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      const payload = {
        date: values.date.format('YYYY-MM-DD'),
        employeeId: values.employeeId,
        workstationId: values.workstationId,
        shiftPatternId: values.shiftPatternId,
        actualHours: values.actualHours,
        actualNightHours: values.actualNightHours,
        comment: values.comment,
      };
      if (editing) {
        await shiftAssignmentsApi.update(editing.id, payload);
        message.success('Обновлено');
      } else {
        await shiftAssignmentsApi.create(payload);
        message.success('Создано');
      }
      setModalOpen(false);
      loadAssignments();
    } catch (err: any) {
      if (err.errorFields) return;
      message.error(err.response?.data?.message || 'Ошибка сохранения');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await shiftAssignmentsApi.delete(id);
      message.success('Удалено');
      loadAssignments();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Ошибка удаления');
    }
  };

  return (
      <DndContext sensors={sensors} onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
        <div>
          <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} wrap>
            <Title level={3} style={{ color: 'white', margin: 0 }}>
              Рабочий календарь
            </Title>
            <Space wrap>
              <Select
                  placeholder="Все отделы"
                  allowClear
                  style={{ width: 220 }}
                  value={departmentId}
                  onChange={setDepartmentId}
                  options={departments.map((d) => ({ value: d.id, label: d.name }))}
              />
              <Select
                  value={view}
                  onChange={(v) => setView(v as ViewMode)}
                  options={[
                    { value: 'week', label: 'Неделя' },
                    { value: 'month', label: 'Месяц' },
                  ]}
                  style={{ width: 120 }}
              />
              <Button icon={<LeftOutlined />} onClick={handlePrev} />
              <DatePicker
                  value={anchor}
                  onChange={(v) => v && setAnchor(v)}
                  format="DD.MM.YYYY"
                  allowClear={false}
              />
              <Button icon={<RightOutlined />} onClick={handleNext} />
              <Button onClick={handleToday}>Сегодня</Button>
              <Button icon={<ReloadOutlined />} onClick={loadAssignments} />
            </Space>
          </Space>

          <Card
              size="small"
              style={{ background: '#2d2d3f', border: 'none', marginBottom: 12 }}
              styles={{ body: { padding: 12 } }}
          >
            <Space wrap size="small">
              <Text style={{ color: '#aaa' }}>Перетащи смену на ячейку:</Text>
              {patterns.filter((p) => p.isActive).map((p) => (
                  <DraggablePattern key={p.id} pattern={p} />
              ))}
              {patterns.length === 0 && (
                  <Text type="secondary">Нет шаблонов смен. Создай в разделе «Шаблоны смен».</Text>
              )}
            </Space>
          </Card>

          <div style={{ overflowX: 'auto', background: '#2d2d3f', borderRadius: 8 }}>
            <table style={{ borderCollapse: 'collapse', minWidth: '100%' }}>
              <thead>
              <tr>
                <th
                    style={{
                      padding: 8,
                      textAlign: 'left',
                      color: '#aaa',
                      position: 'sticky',
                      left: 0,
                      background: '#2d2d3f',
                      borderBottom: '1px solid #3d3d4f',
                      minWidth: 200,
                    }}
                >
                  Сотрудник
                </th>
                {days.map((d) => {
                  const isWeekend = d.day() === 0 || d.day() === 6;
                  return (
                      <th
                          key={d.format('YYYY-MM-DD')}
                          style={{
                            padding: 8,
                            textAlign: 'center',
                            color: isWeekend ? '#ff7a45' : '#aaa',
                            borderBottom: '1px solid #3d3d4f',
                            minWidth: 100,
                          }}
                      >
                        <div>{d.format('ddd')}</div>
                        <div style={{ fontSize: 16, fontWeight: 600, color: 'white' }}>
                          {d.format('DD')}
                        </div>
                      </th>
                  );
                })}
              </tr>
              </thead>
              <tbody>
              {filteredEmployees.length === 0 && (
                  <tr>
                    <td colSpan={days.length + 1} style={{ padding: 24, textAlign: 'center' }}>
                      <Empty description="Нет сотрудников" />
                    </td>
                  </tr>
              )}
              {filteredEmployees.map((emp) => (
                  <tr key={emp.id}>
                    <td
                        style={{
                          padding: 8,
                          color: 'white',
                          position: 'sticky',
                          left: 0,
                          background: '#2d2d3f',
                          borderBottom: '1px solid #3d3d4f',
                        }}
                    >
                      <div style={{ fontWeight: 500 }}>
                        {emp.lastName} {emp.firstName}
                      </div>
                      <div style={{ fontSize: 12, color: '#888' }}>{emp.code}</div>
                    </td>
                    {days.map((d) => {
                      const dateStr = d.format('YYYY-MM-DD');
                      const cellAssignments = assignmentsByCell.get(`${emp.id}|${dateStr}`) ?? [];
                      return (
                          <DroppableCell
                              key={dateStr}
                              cell={{ employeeId: emp.id, date: dateStr }}
                              onClick={() => openCreateModal(emp.id, dateStr)}
                          >
                            <Space direction="vertical" size={4} style={{ width: '100%' }}>
                              {cellAssignments.map((a) => (
                                  <DraggableAssignment
                                      key={a.id}
                                      assignment={a}
                                      onEdit={() => openEditModal(a)}
                                      onDelete={() => handleDelete(a.id)}
                                  />
                              ))}
                            </Space>
                          </DroppableCell>
                      );
                    })}
                  </tr>
              ))}
              </tbody>
            </table>
          </div>

          <Modal
              title={editing ? 'Редактирование назначения' : 'Новое назначение'}
              open={modalOpen}
              onOk={handleSave}
              onCancel={() => setModalOpen(false)}
              confirmLoading={saving}
              okText="Сохранить"
              cancelText="Отмена"
              width={520}
          >
            <Form form={form} layout="vertical">
              <Form.Item name="employeeId" label="Сотрудник" rules={[{ required: true }]}>
                <Select
                    showSearch
                    optionFilterProp="label"
                    options={employees.map((e) => ({
                      value: e.id,
                      label: `${e.lastName} ${e.firstName} (${e.code})`,
                    }))}
                />
              </Form.Item>

              <Form.Item name="date" label="Дата" rules={[{ required: true }]}>
                <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
              </Form.Item>

              <Form.Item name="shiftPatternId" label="Шаблон смены" rules={[{ required: true }]}>
                <Select
                    options={patterns.map((p) => ({ value: p.id, label: `${p.code} — ${p.name}` }))}
                />
              </Form.Item>

              <Form.Item name="workstationId" label="Рабочая станция" rules={[{ required: true }]}>
                <Select
                    showSearch
                    optionFilterProp="label"
                    options={workstations.map((w) => ({ value: w.id, label: `${w.code} — ${w.name}` }))}
                />
              </Form.Item>

              <Form.Item name="actualHours" label="Фактические часы (необязательно)">
                <InputNumber min={0} max={24} step={0.5} style={{ width: '100%' }} />
              </Form.Item>

              <Form.Item name="actualNightHours" label="Фактические ночные (необязательно)">
                <InputNumber min={0} max={24} step={0.5} style={{ width: '100%' }} />
              </Form.Item>

              <Form.Item name="comment" label="Комментарий">
                <Input.TextArea rows={2} />
              </Form.Item>
            </Form>
          </Modal>
        </div>

        <DragOverlay>
          {activePattern && (
              <div
                  style={{
                    padding: '6px 12px',
                    background: '#2ecc71',
                    color: 'white',
                    borderRadius: 4,
                    cursor: 'grabbing',
                  }}
              >
                {activePattern.name}
              </div>
          )}
        </DragOverlay>
      </DndContext>
  );
};

const DraggablePattern = ({ pattern }: { pattern: ShiftPattern }) => {
  const { attributes, listeners, setNodeRef, isDragging } = useDraggable({
    id: `pattern-${pattern.id}`,
    data: { type: 'pattern', pattern },
  });
  return (
      <div
          ref={setNodeRef}
          {...listeners}
          {...attributes}
          style={{
            padding: '4px 10px',
            background: isDragging ? '#1d8a4b' : '#2ecc71',
            color: 'white',
            borderRadius: 4,
            cursor: 'grab',
            fontSize: 13,
            userSelect: 'none',
          }}
      >
        {pattern.name}
        {Number(pattern.nightHours) > 0 && (
            <Tag color="purple" style={{ marginLeft: 6 }}>
              {pattern.nightHours}ч ночь
            </Tag>
        )}
      </div>
  );
};

const DraggableAssignment = ({
                               assignment, onEdit, onDelete,
                             }: {
  assignment: ShiftAssignment;
  onEdit: () => void;
  onDelete: () => void;
}) => {
  const { attributes, listeners, setNodeRef, isDragging } = useDraggable({
    id: `assign-${assignment.id}`,
    data: { type: 'assignment', assignment },
  });
  return (
      <div
          ref={setNodeRef}
          style={{
            padding: '4px 6px',
            background: isDragging ? '#1d8a4b' : '#3d5a80',
            color: 'white',
            borderRadius: 4,
            fontSize: 12,
            cursor: 'grab',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
      >
      <span
          {...listeners}
          {...attributes}
          style={{ flex: 1, cursor: 'grab' }}
          title={`${assignment.shiftPatternName ?? ''} • ${assignment.workstationName ?? ''}`}
      >
        {assignment.shiftPatternName ?? '—'}
      </span>
        <Space size={2}>
          <Button
              size="small"
              type="text"
              style={{ color: 'white', padding: 0, height: 16 }}
              onClick={(e) => { e.stopPropagation(); onEdit(); }}
          >
            ✎
          </Button>
          <Popconfirm title="Удалить?" onConfirm={onDelete} okText="Да" cancelText="Нет">
            <Button
                size="small"
                type="text"
                style={{ color: '#ff7875', padding: 0, height: 16 }}
                onClick={(e) => e.stopPropagation()}
            >
              ×
            </Button>
          </Popconfirm>
        </Space>
      </div>
  );
};

const DroppableCell = ({
                         cell, children, onClick,
                       }: {
  cell: { employeeId: string; date: string };
  children: ReactNode;
  onClick: () => void;
}) => {
  const { setNodeRef, isOver } = useDroppable({
    id: `cell-${cell.employeeId}-${cell.date}`,
    data: { type: 'cell', cell },
  });
  return (
      <td
          ref={setNodeRef}
          onClick={onClick}
          style={{
            padding: 4,
            verticalAlign: 'top',
            minWidth: 100,
            minHeight: 60,
            borderBottom: '1px solid #3d3d4f',
            borderLeft: '1px solid #3d3d4f',
            background: isOver ? '#3d5a80' : 'transparent',
            cursor: 'pointer',
            transition: 'background 0.15s',
          }}
      >
        {children}
      </td>
  );
};

export default WorkCalendar;