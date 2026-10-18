import { useEffect, useMemo, useState } from 'react';
import {
    Typography, Space, Button, Select, message, Tag, Modal, Form,
    Popconfirm, Card, Empty, Switch, Tooltip, Badge, Checkbox,
} from 'antd';
import {
    ReloadOutlined, SettingOutlined, PlusOutlined, UserAddOutlined, CloseOutlined, SyncOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import {
    DndContext, DragOverlay, PointerSensor, useSensor, useSensors,
    useDroppable, useDraggable, type DragEndEvent, type DragStartEvent,
} from '@dnd-kit/core';

import { employeesApi } from '../../api/employees.api';
import type { Employee } from '../../api/employees.api';
import { workstationsApi } from '../../api/workstations.api';
import type { Workstation } from '../../api/workstations.api';
import { shiftPatternsApi } from '../../api/shiftPatterns.api';
import type { ShiftPattern } from '../../api/shiftPatterns.api';
import { qualificationsApi } from '../../api/qualifications.api';
import type { Qualification } from '../../api/qualifications.api';
import { workCalendarApi } from '../../api/workCalendar.api';
import type {
    DayBoard as DayBoardType,
    ShiftSlot,
    WorkstationBoard,
    ShiftBoard,
    QualificationRefreshResult,
} from '../../api/workCalendar.api';

const { Title, Text } = Typography;

type SlotDragData = { type: 'employee'; employee: Employee };
type SlotDropData = { type: 'slot'; slot: ShiftSlot; workstationId: string; shiftPatternId: string };

interface Props {
    date: string;
    departmentId?: string;
    onClose: () => void;
}

const DayBoard = ({ date, departmentId, onClose }: Props) => {
    const [board, setBoard] = useState<DayBoardType | null>(null);
    const [loading, setLoading] = useState(false);

    const [employees, setEmployees] = useState<Employee[]>([]);
    const [workstations, setWorkstations] = useState<Workstation[]>([]);
    const [patterns, setPatterns] = useState<ShiftPattern[]>([]);
    const [qualifications, setQualifications] = useState<Qualification[]>([]);

    const [activeEmployee, setActiveEmployee] = useState<Employee | null>(null);
    const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 5 } }));

    const [wsSettingsOpen, setWsSettingsOpen] = useState(false);
    const [wsSettingsForm] = Form.useForm();

    const [addShiftOpen, setAddShiftOpen] = useState(false);
    const [addShiftFor, setAddShiftFor] = useState<{ workstationId: string } | null>(null);
    const [addShiftForm] = Form.useForm();

    const [addSlotOpen, setAddSlotOpen] = useState(false);
    const [addSlotFor, setAddSlotFor] = useState<{ workstationId: string; shiftPatternId: string } | null>(null);
    const [addSlotForm] = Form.useForm();

    // Состояние кнопки «Обновить квалификации»
    const [refreshing, setRefreshing] = useState(false);
    const [refreshResult, setRefreshResult] = useState<QualificationRefreshResult | null>(null);

    const loadDictionaries = async () => {
        try {
            const [emp, ws, pat, qs] = await Promise.all([
                employeesApi.getAll(0, 500),
                workstationsApi.getAll(0, 500),
                shiftPatternsApi.getAll(0, 200),
                qualificationsApi.getAll(0, 500),
            ]);
            setEmployees(emp.content ?? []);
            setWorkstations(ws.content ?? []);
            setPatterns(pat.content ?? []);
            setQualifications(qs.content ?? []);
        } catch {
            message.error('Ошибка загрузки справочников');
        }
    };

    const loadBoard = async () => {
        setLoading(true);
        try {
            const data = await workCalendarApi.getDayBoard(date);
            setBoard(data);
        } catch {
            message.error('Ошибка загрузки дня');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadDictionaries();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    useEffect(() => {
        loadBoard();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [date]);

    const filteredEmployees = useMemo(() => {
        if (!departmentId) return employees;
        return employees.filter((e) => e.departmentId === departmentId);
    }, [employees, departmentId]);

    const busyEmployeeIds = useMemo(() => {
        const set = new Set<string>();
        if (!board) return set;
        for (const ws of board.workstations) {
            for (const sh of ws.shifts) {
                for (const s of sh.slots) {
                    if (s.employeeId) set.add(s.employeeId);
                }
            }
        }
        return set;
    }, [board]);

    const mergedWorkstations = useMemo<WorkstationBoard[]>(() => {
        const filtered = departmentId
            ? workstations.filter((w) => w.departmentId === departmentId && w.isActive)
            : workstations.filter((w) => w.isActive);

        const fromBoard = new Map<string, WorkstationBoard>();
        if (board) {
            for (const w of board.workstations) {
                fromBoard.set(w.workstationId, w);
            }
        }

        return filtered.map((w) => {
            const existing = fromBoard.get(w.id);
            if (existing) return existing;
            return {
                workstationId: w.id,
                workstationName: w.name,
                departmentId: w.departmentId,
                requiredQualificationId: null,
                isWorking: true,
                shifts: [],
            };
        });
    }, [workstations, board, departmentId]);

    const handleDragStart = (e: DragStartEvent) => {
        const data = e.active.data.current as SlotDragData | undefined;
        if (data?.type === 'employee') setActiveEmployee(data.employee);
    };

    const handleDragEnd = async (e: DragEndEvent) => {
        setActiveEmployee(null);
        const overData = e.over?.data.current as SlotDropData | undefined;
        const activeData = e.active.data.current as SlotDragData | undefined;
        if (!overData || overData.type !== 'slot' || !activeData || activeData.type !== 'employee') return;

        const { slot } = overData;
        const employee = activeData.employee;

        if (slot.employeeId) {
            message.warning('В слоте уже есть сотрудник');
            return;
        }

        try {
            await workCalendarApi.assignEmployee(slot.id, { employeeId: employee.id });
            message.success(`${employee.lastName} ${employee.firstName} назначен`);
            loadBoard();
        } catch (err: any) {
            const errCode = err.response?.data?.error;
            const msg = err.response?.data?.message || 'Ошибка назначения';

            if (errCode === 'QUALIFICATION_MISMATCH') {
                Modal.confirm({
                    title: 'Нет требуемой квалификации',
                    content: 'У сотрудника нет квалификации, требуемой этим слотом. Назначить всё равно?',
                    okText: 'Да, назначить',
                    cancelText: 'Отмена',
                    okButtonProps: { danger: true },
                    onOk: async () => {
                        try {
                            await workCalendarApi.assignEmployee(slot.id, {
                                employeeId: employee.id,
                                forceQualificationOverride: true,
                                qualificationOverrideReason: 'Назначено вручную через override',
                            });
                            message.success('Назначено с override квалификации');
                            loadBoard();
                        } catch (e: any) {
                            message.error(e.response?.data?.message || 'Ошибка');
                        }
                    },
                });
                return;
            }

            if (msg.includes('already has a shift')) {
                Modal.confirm({
                    title: 'У сотрудника уже есть смена в этот день',
                    content: 'Назначить вторую смену (override)?',
                    okText: 'Да, override',
                    cancelText: 'Отмена',
                    onOk: async () => {
                        try {
                            await workCalendarApi.assignEmployee(slot.id, {
                                employeeId: employee.id,
                                force: true,
                                overrideReason: 'Назначено через drag-n-drop',
                            });
                            message.success('Назначено с override');
                            loadBoard();
                        } catch (e: any) {
                            message.error(e.response?.data?.message || 'Ошибка');
                        }
                    },
                });
            } else {
                message.error(msg);
            }
        }
    };

    const handleUnassign = async (slotId: string) => {
        try {
            await workCalendarApi.unassignEmployee(slotId);
            message.success('Сотрудник снят');
            loadBoard();
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка');
        }
    };

    const handleDeleteSlot = async (slotId: string) => {
        try {
            await workCalendarApi.deleteSlot(slotId);
            message.success('Слот удалён');
            loadBoard();
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка удаления');
        }
    };

    const handleToggleDayOffWork = async (slot: ShiftSlot, isDayOffWork: boolean) => {
        try {
            await workCalendarApi.updateSlot(slot.id, { isDayOffWork });
            loadBoard();
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка сохранения флага');
        }
    };

    const handleToggleWorkstation = async (ws: WorkstationBoard) => {
        try {
            await workCalendarApi.setWorkstationDayStatus({
                workstationId: ws.workstationId,
                date,
                isWorking: !ws.isWorking,
            });
            loadBoard();
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка');
        }
    };

    const handleOpenAddShift = (workstationId: string) => {
        setAddShiftFor({ workstationId });
        addShiftForm.resetFields();
        setAddShiftOpen(true);
    };

    const handleSaveAddShift = async () => {
        if (!addShiftFor) return;
        try {
            const values = await addShiftForm.validateFields();
            await workCalendarApi.addWorkstationShift({
                workstationId: addShiftFor.workstationId,
                date,
                shiftPatternId: values.shiftPatternId,
            });
            setAddShiftOpen(false);
            loadBoard();
        } catch (err: any) {
            if (err.errorFields) return;
            message.error(err.response?.data?.message || 'Ошибка');
        }
    };

    const handleDeleteWorkstationShift = async (id: string) => {
        try {
            await workCalendarApi.deleteWorkstationShift(id);
            loadBoard();
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка');
        }
    };

    const handleOpenAddSlot = (workstationId: string, shiftPatternId: string) => {
        setAddSlotFor({ workstationId, shiftPatternId });
        addSlotForm.resetFields();
        setAddSlotOpen(true);
    };

    const handleSaveAddSlot = async () => {
        if (!addSlotFor) return;
        try {
            const values = await addSlotForm.validateFields();
            await workCalendarApi.createSlot({
                date,
                workstationId: addSlotFor.workstationId,
                shiftPatternId: addSlotFor.shiftPatternId,
                requiredQualificationId: values.requiredQualificationId || undefined,
            });
            setAddSlotOpen(false);
            loadBoard();
        } catch (err: any) {
            if (err.errorFields) return;
            message.error(err.response?.data?.message || 'Ошибка создания слота');
        }
    };

    const workstationQualifications = (workstationId: string): Qualification[] => {
        const w = workstations.find((x) => x.id === workstationId);
        if (!w || !w.qualificationIds) return [];
        return w.qualificationIds
            .map((qid) => qualifications.find((q) => q.id === qid))
            .filter((q): q is Qualification => !!q);
    };

    const handleOpenWsSettings = () => {
        const initial: any = {};
        for (const ws of workstations) {
            initial[`ws_${ws.id}`] =
                board?.workstations.find((b) => b.workstationId === ws.id)?.isWorking ?? true;
        }
        wsSettingsForm.setFieldsValue(initial);
        setWsSettingsOpen(true);
    };

    const handleSaveWsSettings = async () => {
        try {
            const values = await wsSettingsForm.getFieldsValue();
            for (const ws of workstations) {
                const isWorking = values[`ws_${ws.id}`];
                const current = board?.workstations.find((b) => b.workstationId === ws.id);
                const currentIsWorking = current?.isWorking ?? true;
                if (isWorking !== currentIsWorking) {
                    await workCalendarApi.setWorkstationDayStatus({
                        workstationId: ws.id,
                        date,
                        isWorking,
                    });
                }
            }
            setWsSettingsOpen(false);
            loadBoard();
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка');
        }
    };

    /**
     * Кнопка «Обновить квалификации»:
     * 1) синкает квалификации всех сотрудников тенанта из employee-service в локальный ref;
     * 2) перезагружает доску, чтобы пересчитались флаги qualificationMismatch.
     */
    const handleRefreshQualifications = async () => {
        setRefreshing(true);
        setRefreshResult(null);
        try {
            const result = await workCalendarApi.refreshQualifications();
            setRefreshResult(result);
            message.success(
                `Квалификации обновлены: сотрудников ${result.employeesOk}` +
                (result.employeesFailed > 0 ? `, ошибок ${result.employeesFailed}` : '') +
                `, квалификаций ${result.qualificationsSynced}`
            );
            await loadBoard();
        } catch (err: any) {
            message.error(err.response?.data?.message || 'Ошибка обновления квалификаций');
        } finally {
            setRefreshing(false);
        }
    };

    return (
        <DndContext sensors={sensors} onDragStart={handleDragStart} onDragEnd={handleDragEnd}>
            <div style={{ display: 'flex', gap: 16, marginTop: 24 }}>
                <div style={{ flex: 1, minWidth: 0 }}>
                    <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} wrap>
                        <Title level={4} style={{ color: 'white', margin: 0 }}>
                            {dayjs(date).format('dddd, DD MMMM YYYY')}
                        </Title>
                        <Space wrap>
                            <Button
                                icon={<SyncOutlined spin={refreshing} />}
                                onClick={handleRefreshQualifications}
                                loading={refreshing}
                            >
                                Обновить квалификации
                            </Button>
                            {refreshResult && (
                                <Text style={{ color: '#888', fontSize: 12 }}>
                                    ok: {refreshResult.employeesOk}
                                    {refreshResult.employeesFailed > 0
                                        ? `, ошибок: ${refreshResult.employeesFailed}`
                                        : ''}
                                    , квалификаций: {refreshResult.qualificationsSynced}
                                </Text>
                            )}
                            <Button icon={<SettingOutlined />} onClick={handleOpenWsSettings}>
                                Настроить станки
                            </Button>
                            <Button icon={<ReloadOutlined />} onClick={loadBoard} />
                            <Button onClick={onClose}>Закрыть день</Button>
                        </Space>
                    </Space>

                    {loading && <Card style={{ background: '#2d2d3f', border: 'none' }}>Загрузка…</Card>}

                    {!loading && mergedWorkstations.map((ws) => (
                        <WorkstationCard
                            key={ws.workstationId}
                            ws={ws}
                            workstations={workstations}
                            patterns={patterns}
                            onToggleWorking={() => handleToggleWorkstation(ws)}
                            onAddShift={() => handleOpenAddShift(ws.workstationId)}
                            onDeleteShift={handleDeleteWorkstationShift}
                            onAddSlot={(shiftPatternId) => handleOpenAddSlot(ws.workstationId, shiftPatternId)}
                            onUnassign={handleUnassign}
                            onDeleteSlot={handleDeleteSlot}
                            onToggleDayOffWork={handleToggleDayOffWork}
                        />
                    ))}

                    {!loading && mergedWorkstations.length === 0 && (
                        <Empty description="Нет станков. Создай станок в разделе «Рабочие места»." />
                    )}
                </div>

                <div style={{ width: 260, flexShrink: 0 }}>
                    <Card
                        title={<span style={{ color: 'white' }}>Сотрудники</span>}
                        style={{ background: '#2d2d3f', border: 'none', position: 'sticky', top: 0 }}
                        styles={{ header: { borderBottom: '1px solid #3d3d4f' } }}
                    >
                        <div style={{ maxHeight: 'calc(100vh - 300px)', overflowY: 'auto' }}>
                            {filteredEmployees.length === 0 && <Text type="secondary">Нет сотрудников</Text>}
                            {filteredEmployees.map((emp) => (
                                <DraggableEmployeeCard
                                    key={emp.id}
                                    employee={emp}
                                    busy={busyEmployeeIds.has(emp.id)}
                                />
                            ))}
                        </div>
                    </Card>
                </div>
            </div>

            <Modal
                title="Какие станки работают в этот день"
                open={wsSettingsOpen}
                onOk={handleSaveWsSettings}
                onCancel={() => setWsSettingsOpen(false)}
                okText="Сохранить"
                cancelText="Отмена"
                width={600}
            >
                <Form form={wsSettingsForm} layout="vertical">
                    {workstations.map((w) => (
                        <Form.Item
                            key={w.id}
                            name={`ws_${w.id}`}
                            label={`${w.code} — ${w.name}`}
                            valuePropName="checked"
                        >
                            <Switch checkedChildren="Работает" unCheckedChildren="Простой" />
                        </Form.Item>
                    ))}
                </Form>
            </Modal>

            <Modal
                title="Добавить смену на станок"
                open={addShiftOpen}
                onOk={handleSaveAddShift}
                onCancel={() => setAddShiftOpen(false)}
                okText="Добавить"
                cancelText="Отмена"
                width={420}
            >
                <Form form={addShiftForm} layout="vertical">
                    <Form.Item
                        name="shiftPatternId"
                        label="Шаблон смены"
                        rules={[{ required: true, message: 'Выберите шаблон' }]}
                    >
                        <Select
                            options={patterns
                                .filter((p) => p.isActive)
                                .map((p) => ({ value: p.id, label: `${p.code} — ${p.name}` }))}
                        />
                    </Form.Item>
                </Form>
            </Modal>

            <Modal
                title="Новый слот"
                open={addSlotOpen}
                onOk={handleSaveAddSlot}
                onCancel={() => setAddSlotOpen(false)}
                okText="Создать"
                cancelText="Отмена"
                width={420}
            >
                <Form form={addSlotForm} layout="vertical">
                    <Form.Item
                        name="requiredQualificationId"
                        label="Требуемая квалификация"
                    >
                        <Select
                            allowClear
                            placeholder="Без требований"
                            optionFilterProp="label"
                            options={addSlotFor
                                ? workstationQualifications(addSlotFor.workstationId).map((q) => ({
                                    value: q.id,
                                    label: `${q.code} — ${q.name}`,
                                }))
                                : []}
                        />
                    </Form.Item>
                </Form>
            </Modal>

            <DragOverlay>
                {activeEmployee && (
                    <div style={{ padding: '6px 12px', background: '#2ecc71', color: 'white', borderRadius: 4 }}>
                        {activeEmployee.lastName} {activeEmployee.firstName}
                    </div>
                )}
            </DragOverlay>
        </DndContext>
    );
};

// ---------- Workstation card ----------

const WorkstationCard = ({
                             ws, workstations, patterns,
                             onToggleWorking, onAddShift, onDeleteShift, onAddSlot, onUnassign, onDeleteSlot, onToggleDayOffWork,
                         }: {
    ws: WorkstationBoard;
    workstations: Workstation[];
    patterns: ShiftPattern[];
    onToggleWorking: () => void;
    onAddShift: () => void;
    onDeleteShift: (id: string) => void;
    onAddSlot: (shiftPatternId: string) => void;
    onUnassign: (slotId: string) => void;
    onDeleteSlot: (slotId: string) => void;
    onToggleDayOffWork: (slot: ShiftSlot, isDayOffWork: boolean) => void;
}) => {
    const w = workstations.find((x) => x.id === ws.workstationId);
    const displayName = w ? `${w.code} — ${w.name}` : ws.workstationId;

    return (
        <Card
            style={{
                background: '#2d2d3f',
                border: 'none',
                marginBottom: 12,
                opacity: ws.isWorking ? 1 : 0.5,
            }}
            styles={{ header: { borderBottom: '1px solid #3d3d4f' } }}
            title={
                <Space>
                    <span style={{ color: 'white', fontWeight: 500 }}>🏭 {displayName}</span>
                    {!ws.isWorking && <Tag color="red">Простой</Tag>}
                </Space>
            }
            extra={
                <Space>
                    <Tooltip title={ws.isWorking ? 'Поставить на простой' : 'Вернуть в работу'}>
                        <Button size="small" onClick={onToggleWorking}>
                            {ws.isWorking ? '⏸' : '▶'}
                        </Button>
                    </Tooltip>
                    <Button size="small" icon={<PlusOutlined />} onClick={onAddShift}>
                        Смена
                    </Button>
                </Space>
            }
        >
            {ws.shifts.length === 0 && (
                <Text type="secondary">Нет смен на этот день. Добавь через кнопку «+ Смена».</Text>
            )}

            {ws.shifts.map((sh) => (
                <ShiftBlock
                    key={sh.workstationShiftId}
                    shift={sh}
                    patterns={patterns}
                    onDeleteShift={() => onDeleteShift(sh.workstationShiftId)}
                    onAddSlot={() => onAddSlot(sh.shiftPatternId)}
                    onUnassign={onUnassign}
                    onDeleteSlot={onDeleteSlot}
                    onToggleDayOffWork={onToggleDayOffWork}
                />
            ))}
        </Card>
    );
};

// ---------- Shift block ----------

const ShiftBlock = ({
                        shift, patterns, onDeleteShift, onAddSlot, onUnassign, onDeleteSlot, onToggleDayOffWork,
                    }: {
    shift: ShiftBoard;
    patterns: ShiftPattern[];
    onDeleteShift: () => void;
    onAddSlot: () => void;
    onUnassign: (id: string) => void;
    onDeleteSlot: (id: string) => void;
    onToggleDayOffWork: (slot: ShiftSlot, isDayOffWork: boolean) => void;
}) => {
    const p = patterns.find((x) => x.id === shift.shiftPatternId);
    const timeRange = p ? `${p.startTime.slice(0, 5)}–${p.endTime.slice(0, 5)}` : '';

    return (
        <div style={{ marginBottom: 12 }}>
            <Space style={{ marginBottom: 8 }} size="small" wrap>
                <Tag color="blue">{shift.shiftPatternName ?? 'Смена'}</Tag>
                {timeRange && <Text type="secondary" style={{ fontSize: 12 }}>{timeRange}</Text>}
                {shift.totalHours != null && (
                    <Text type="secondary" style={{ fontSize: 12 }}>{shift.totalHours}ч</Text>
                )}
                {Number(shift.nightHours) > 0 && (
                    <Tag color="purple">ночь: {shift.nightHours}ч</Tag>
                )}
                <Button
                    size="small"
                    type="text"
                    icon={<PlusOutlined />}
                    onClick={onAddSlot}
                    style={{ color: '#2ecc71' }}
                >
                    слот
                </Button>
                <Popconfirm title="Удалить смену?" onConfirm={onDeleteShift} okText="Да" cancelText="Нет">
                    <Button size="small" type="text" danger icon={<CloseOutlined />} />
                </Popconfirm>
            </Space>

            <Space wrap size={8}>
                {shift.slots.map((slot) => (
                    <SlotCell
                        key={slot.id}
                        slot={slot}
                        onUnassign={() => onUnassign(slot.id)}
                        onDelete={() => onDeleteSlot(slot.id)}
                        onToggleDayOffWork={onToggleDayOffWork}
                    />
                ))}
                {shift.slots.length === 0 && (
                    <Text type="secondary" style={{ fontSize: 12 }}>
                        Нет слотов. Нажми «+ слот».
                    </Text>
                )}
            </Space>
        </div>
    );
};

// ---------- Slot cell ----------

const SlotCell = ({
                      slot, onUnassign, onDelete, onToggleDayOffWork,
                  }: {
    slot: ShiftSlot;
    onUnassign: () => void;
    onDelete: () => void;
    onToggleDayOffWork: (slot: ShiftSlot, isDayOffWork: boolean) => void;
}) => {
    const { setNodeRef, isOver } = useDroppable({
        id: `slot-${slot.id}`,
        data: {
            type: 'slot',
            slot,
            workstationId: slot.workstationId,
            shiftPatternId: slot.shiftPatternId,
        } as SlotDropData,
    });

    const filled = !!slot.employeeId;

    return (
        <div
            ref={setNodeRef}
            style={{
                minWidth: 180,
                padding: '6px 8px',
                borderRadius: 4,
                background: slot.qualificationMismatch
                    ? 'rgba(243, 156, 18, 0.25)'
                    : filled ? '#3d5a80' : isOver ? '#2ecc71' : '#1e1e2f',
                border: `1px dashed ${slot.qualificationMismatch ? '#f39c12' : filled ? '#3d5a80' : '#3d3d4f'}`,
                color: 'white',
                fontSize: 12,
                display: 'flex',
                flexDirection: 'column',
                gap: 4,
            }}
        >
            {filled ? (
                <>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 6 }}>
                        <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                            {slot.employeeFullName ?? slot.employeeId}
                            {slot.overridden && <Tag color="orange" style={{ marginLeft: 6 }}>override</Tag>}
                            {slot.qualificationMismatch && (
                                <Tooltip title="У сотрудника нет требуемой квалификации">
                                    <Tag color="orange" style={{ marginLeft: 6 }}>⚠ квал.</Tag>
                                </Tooltip>
                            )}
                        </span>
                        <Space size={2}>
                            <Tooltip title="Снять сотрудника">
                                <Button
                                    size="small"
                                    type="text"
                                    style={{ color: 'white', padding: 0, height: 16 }}
                                    onClick={(e) => { e.stopPropagation(); onUnassign(); }}
                                >
                                    ↑
                                </Button>
                            </Tooltip>
                            <Popconfirm title="Удалить слот?" onConfirm={onDelete} okText="Да" cancelText="Нет">
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

                    <Checkbox
                        checked={slot.isDayOffWork}
                        onChange={(e) => onToggleDayOffWork(slot, e.target.checked)}
                        style={{ color: slot.isDayOffWork ? '#ff7a45' : '#aaa', fontSize: 11 }}
                    >
                        работа в выходной
                    </Checkbox>
                </>
            ) : (
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <UserAddOutlined style={{ color: '#888' }} />
                    <span style={{ flex: 1, color: '#888' }}>Свободен</span>
                    <Popconfirm title="Удалить слот?" onConfirm={onDelete} okText="Да" cancelText="Нет">
                        <Button
                            size="small"
                            type="text"
                            style={{ color: '#ff7875', padding: 0, height: 16 }}
                            onClick={(e) => e.stopPropagation()}
                        >
                            ×
                        </Button>
                    </Popconfirm>
                </div>
            )}
        </div>
    );
};

// ---------- Draggable employee card ----------

const DraggableEmployeeCard = ({ employee, busy }: { employee: Employee; busy: boolean }) => {
    const { attributes, listeners, setNodeRef, isDragging } = useDraggable({
        id: `employee-${employee.id}`,
        data: { type: 'employee', employee } as SlotDragData,
    });

    return (
        <div
            ref={setNodeRef}
            {...listeners}
            {...attributes}
            style={{
                padding: '6px 10px',
                marginBottom: 6,
                borderRadius: 4,
                background: isDragging ? '#1d8a4b' : busy ? '#3d3d4f' : '#1e1e2f',
                color: busy ? '#888' : 'white',
                fontSize: 13,
                cursor: 'grab',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
            }}
        >
            <span style={{ overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {employee.lastName} {employee.firstName}
            </span>
            {busy && (
                <Badge
                    status="processing"
                    text={<span style={{ color: '#888', fontSize: 11 }}>занят</span>}
                />
            )}
        </div>
    );
};

export default DayBoard;