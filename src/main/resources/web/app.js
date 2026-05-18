let currentUser = null;
let equipmentCache = [];
let courseCache = [];
let technicianCache = [];
let labCache = [];
let consumableCache = [];
let reservationCache = [];
let maintenanceCache = [];
let inventoryCache = [];
let labReportCache = [];
let statusReportCache = [];

const $ = (id) => document.getElementById(id);
const API_PREFIX = (window.location.pathname === '/lab' || window.location.pathname.startsWith('/lab/')) ? '/lab' : '';
const LANG_STORAGE_KEY = 'databaseLabLanguage';
let currentLanguage = localStorage.getItem(LANG_STORAGE_KEY) === 'en' ? 'en' : 'zh';
const BOOKABLE_EQUIPMENT_STATUSES = new Set(['AVAILABLE', 'RESERVED']);

const ROLE_LABELS = {
    zh: {
        ADMIN: '管理员',
        TEACHER: '教师',
        STUDENT: '学生',
        TECHNICIAN: '技术员'
    },
    en: {
        ADMIN: 'Admin',
        TEACHER: 'Teacher',
        STUDENT: 'Student',
        TECHNICIAN: 'Technician'
    }
};

const STATUS_LABELS = {
    zh: {
        AVAILABLE: '可预约',
        RESERVED: '已预约',
        IN_USE: '使用中',
        MAINTENANCE: '维修中',
        RETIRED: '已退役',
        PENDING: '待审批',
        APPROVED: '已批准',
        REJECTED: '已拒绝',
        CANCELLED: '已取消',
        COMPLETED: '已完成',
        NO_SHOW: '未到场',
        OPEN: '待处理',
        ASSIGNED: '已分配',
        IN_PROGRESS: '处理中',
        RESOLVED: '已解决',
        CLOSED: '已关闭',
        LOW: '库存低',
        OK: '正常'
    },
    en: {
        AVAILABLE: 'Available',
        RESERVED: 'Reserved',
        IN_USE: 'In use',
        MAINTENANCE: 'Maintenance',
        RETIRED: 'Retired',
        PENDING: 'Pending',
        APPROVED: 'Approved',
        REJECTED: 'Rejected',
        CANCELLED: 'Cancelled',
        COMPLETED: 'Completed',
        NO_SHOW: 'No show',
        OPEN: 'Open',
        ASSIGNED: 'Assigned',
        IN_PROGRESS: 'In progress',
        RESOLVED: 'Resolved',
        CLOSED: 'Closed',
        LOW: 'Low stock',
        OK: 'Normal'
    }
};

const RISK_LABELS = {
    zh: {
        LOW: '低',
        MEDIUM: '中',
        HIGH: '高',
        URGENT: '紧急'
    },
    en: {
        LOW: 'Low',
        MEDIUM: 'Medium',
        HIGH: 'High',
        URGENT: 'Urgent'
    }
};

const CATEGORY_LABELS = {
    zh: {
        Computing: '计算设备',
        Robotics: '机器人设备',
        Measurement: '测量设备',
        Sensor: '传感器',
        Network: '网络设备'
    },
    en: {}
};

const COURSE_LABELS = {
    zh: {
        'Databases and Information Systems': '数据库与信息系统',
        'Machine Learning Engineering': '机器学习工程'
    },
    en: {}
};

const EQUIPMENT_NAME_LABELS = {
    zh: {
        'GPU Workstation A': 'GPU 工作站 A',
        'Mobile Robot TurtleBot': 'TurtleBot 移动机器人',
        'Digital Oscilloscope': '数字示波器',
        'ECG Sensor Kit': 'ECG 传感器套件',
        'IoT Gateway Set': '物联网网关套件',
        'Managed Switch Rack': '可管理交换机机架'
    },
    en: {}
};

const ITEM_LABELS = {
    zh: {
        'Robot battery pack': '机器人电池包',
        'ECG electrode pad': 'ECG 电极贴片',
        'Ethernet cable': '网线',
        'Micro SD card': 'Micro SD 卡'
    },
    en: {}
};

const UNIT_LABELS = {
    zh: {
        piece: '件',
        pack: '包',
        box: '盒'
    },
    en: {
        piece: 'piece',
        pack: 'pack',
        box: 'box'
    }
};

const DATA_TEXT_LABELS = {
    zh: {
        'Train small image classifier': '训练小型图像分类模型',
        'IoT gateway demo preparation': '物联网网关演示准备',
        'Measure sensor output for lab exercise': '测量实验课传感器输出',
        'ECG cable is loose': 'ECG 线缆松动',
        'Switch fan noise': '交换机风扇噪声'
    },
    en: {}
};

// The front-end keeps role rules visible, so each demo account only sees its own workspace.
const ROLE_TABS = {
    ADMIN: ['equipment', 'reservations', 'maintenance', 'inventory', 'reports'],
    TEACHER: ['reservations', 'equipment', 'reports'],
    STUDENT: ['reservations', 'equipment'],
    TECHNICIAN: ['maintenance', 'equipment', 'inventory']
};

const MESSAGE_LABELS = {
    zh: {
        'Invalid username or password': '用户名或密码不正确。',
        'Registration finished.': '注册完成。',
        'Reservation decision saved.': '预约审批已保存。',
        'Cancel request finished.': '取消预约已完成。',
        'Maintenance ticket updated.': '维修工单已更新。',
        'Equipment updated.': '设备信息已更新。',
        'Equipment retired.': '设备已标记为退役。',
        'Consumable item added.': '耗材种类已新增。',
        'Stock changed.': '库存已调整。',
        'This equipment is already booked in the selected time.': '该设备在所选时间已经被预约。',
        'One or more selected equipment items are already booked in the selected time.': '所选设备里至少有一台在该时间段已被预约。',
        'At least one equipment item is required': '请至少选择一台设备。',
        'Admin permission required': '只有管理员可以执行这个操作。',
        'Purpose is required': '请填写用途。',
        'Start time and end time are required': '请填写开始时间和结束时间。',
        'End time must be after start time': '结束时间必须晚于开始时间。',
        'Equipment does not exist': '设备不存在。',
        'Only pending reservations can be decided': '只有待审批预约可以审批。',
        'Teacher or admin permission required': '只有教师或管理员可以审批预约。',
        'Technician or admin permission required': '只有技术员或管理员可以执行这个操作。',
        'Required field is missing: userId': '缺少用户身份信息，请重新登录。',
        'Field must be a number: userId': '用户身份信息格式不正确，请重新登录。',
        'Ticket title is required': '请填写工单标题。',
        'Description is required': '请填写故障描述。',
        'Status is required': '请选择状态。',
        'Change amount cannot be zero': '库存变动数量不能为 0。',
        'Reason is required': '请填写变动原因。',
        'Username is required': '请填写用户名。',
        'Password is required': '请填写密码。',
        'Full name is required': '请填写姓名。',
        'Email is required': '请填写邮箱。',
        'Username or email already exists': '用户名或邮箱已存在。',
        'Consumable item is required': '请填写耗材名称。',
        'Unit is required': '请选择单位。',
        'Quantity cannot be negative': '数量不能为负数。',
        'Reorder level cannot be negative': '补货线不能为负数。'
    },
    en: {}
};

const UI_TEXT = {
    zh: {
        'app.title': '实验室设备管理系统',
        'app.subtitle': '设备预约、维修工单与耗材管理平台',
        'login.title': '实验室设备服务',
        'login.description': '按身份进入对应工作台，完成预约、审批、维修和库存处理。',
        'login.demoAccounts': '演示账号：admin/admin123，teacher/teacher123，student1/student123，tech/tech123',
        'user.guest': '未登录',
        'user.badge': '{name}（{role}）',
        'nav.equipment': '设备目录',
        'nav.reservations': '预约管理',
        'nav.maintenance': '维修工单',
        'nav.inventory': '耗材库存',
        'nav.reports': '统计报表',
        'nav.logout': '退出登录',
        'equipment.title': '设备目录',
        'equipment.subtitle': '查询实验室设备，发现故障时可以提交维修工单。',
        'equipment.searchPlaceholder': '设备编号、名称或类别',
        'reservations.title': '预约管理',
        'reservations.subtitle': '提交设备预约申请，并处理待审批预约。',
        'reservation.new': '新建预约申请',
        'reservation.selectEquipment': '选择设备',
        'reservation.selectEquipmentHint': '可以勾选多台设备一起提交预约。',
        'reservation.time': '预约时间',
        'reservation.timeHint': '选择开始和结束时间。',
        'reservation.purposePlaceholder': '简单说明为什么要使用这台设备',
        'reservation.consumables': '耗材需求',
        'reservation.consumablesHint': '不需要耗材可以留空；需要时添加物品和数量。',
        'reservation.noConsumables': '本次预约暂不需要耗材。',
        'reservation.selectedCount': '已选 {count} 台',
        'reservation.notSelected': '未选择',
        'reservation.selectedSummary': '已选：{names}',
        'reservation.noSelectionSummary': '未选择设备，展开后可勾选一台或多台设备。',
        'reservation.noConsumableChoices': '暂无可申请耗材。',
        'reservation.stockMeta': '库存 {quantity} {unit}',
        'maintenance.title': '维修工单',
        'maintenance.subtitle': '跟踪设备故障和维修处理进度。',
        'inventory.title': '耗材库存',
        'inventory.subtitle': '管理实验室耗材库存，并记录每一次库存变动。',
        'reports.title': '统计报表',
        'reports.subtitle': '基于 SQL 视图和聚合查询生成的管理统计。',
        'reports.labUsage': '实验室使用情况',
        'reports.equipmentStatus': '设备状态汇总',
        'form.username': '用户名',
        'form.password': '密码',
        'form.fullName': '姓名',
        'form.email': '邮箱',
        'form.confirmPassword': '确认密码',
        'form.course': '课程',
        'form.startTime': '开始时间',
        'form.endTime': '结束时间',
        'form.purpose': '用途',
        'form.assetTag': '设备编号',
        'form.equipmentName': '设备名称',
        'form.category': '类别',
        'form.lab': '实验室',
        'form.status': '状态',
        'form.purchaseDate': '购买日期',
        'form.riskLevel': '风险等级',
        'form.notes': '备注',
        'form.comment': '审批备注',
        'form.title': '标题',
        'form.description': '故障描述',
        'form.priority': '优先级',
        'form.technician': '维修人员',
        'form.note': '备注',
        'form.itemName': '耗材名称',
        'form.unit': '单位',
        'form.initialQuantity': '初始数量',
        'form.reorderLevel': '补货线',
        'form.amount': '变动数量',
        'form.reason': '原因',
        'table.assetTag': '设备编号',
        'table.equipmentName': '设备名称',
        'table.equipment': '设备',
        'table.category': '类别',
        'table.lab': '实验室',
        'table.status': '状态',
        'table.riskLevel': '风险等级',
        'table.openTickets': '未完成工单',
        'table.actions': '操作',
        'table.requester': '申请人',
        'table.start': '开始',
        'table.end': '结束',
        'table.purpose': '用途',
        'table.consumables': '耗材需求',
        'table.reporter': '上报人',
        'table.technician': '维修人员',
        'table.title': '标题',
        'table.priority': '优先级',
        'table.reportedAt': '上报时间',
        'table.consumable': '耗材',
        'table.unit': '单位',
        'table.quantity': '数量',
        'table.reorderLevel': '补货线',
        'table.stockAlert': '库存提醒',
        'table.reservations': '预约数',
        'table.approved': '已批准',
        'table.completed': '已完成',
        'table.highRisk': '高风险',
        'action.login': '登录',
        'action.registerStudent': '注册学生账号',
        'action.search': '搜索',
        'action.reset': '重置',
        'action.addEquipment': '新增设备',
        'action.refresh': '刷新',
        'action.addConsumable': '添加耗材',
        'action.submitRequest': '提交申请',
        'action.addConsumableType': '新增耗材',
        'action.cancel': '取消',
        'action.save': '保存',
        'action.report': '报修',
        'action.edit': '编辑',
        'action.retire': '退役',
        'action.expand': '展开选择',
        'action.collapse': '收起',
        'action.approve': '批准',
        'action.reject': '拒绝',
        'action.update': '更新',
        'action.adjust': '调整',
        'action.removeConsumable': '移除耗材',
        'modal.registerTitle': '注册学生账号',
        'modal.registerHint': '自助注册只开放学生身份，管理员、教师和技术员账号由管理员维护。',
        'modal.passwordMismatch': '两次输入的密码不一致。',
        'modal.editEquipment': '编辑设备',
        'modal.addEquipment': '新增设备',
        'modal.retireEquipment': '退役设备',
        'modal.retireConfirm': '确认将 {assetTag} 标记为已退役？历史预约、维修工单和报表记录会保留。',
        'modal.approveReservation': '批准预约',
        'modal.rejectReservation': '拒绝预约',
        'modal.approveComment': '同意预约',
        'modal.rejectComment': '拒绝预约',
        'modal.reportProblem': '提交设备报修',
        'modal.updateTicket': '更新维修工单',
        'modal.addConsumableType': '新增耗材种类',
        'modal.changeStock': '调整库存',
        'placeholder.category': '例如 Network / Computing',
        'placeholder.note': '填写简短处理记录',
        'placeholder.itemName': '例如：传感器连接线',
        'placeholder.reason': '例如：实验课使用',
        'aria.consumableName': '耗材名称',
        'aria.quantity': '申请数量',
        'risk.suffix': '风险',
        'common.none': '无',
        'common.notAssigned': '未分配',
        'common.normal': '正常',
        'workspace.label': '{role}工作台',
        'workspace.ADMIN.title': '总览实验室资源与运行状态',
        'workspace.ADMIN.subtitle': '管理设备台账、预约流转、维修进度和耗材库存。',
        'workspace.TEACHER.title': '处理课程预约与实验安排',
        'workspace.TEACHER.subtitle': '优先查看待审批预约，再检查课程相关设备使用情况。',
        'workspace.STUDENT.title': '预约实验设备，准备课程实践',
        'workspace.STUDENT.subtitle': '先选设备和时间，需要耗材时随预约一起提交。',
        'workspace.TECHNICIAN.title': '跟进维修任务与耗材保障',
        'workspace.TECHNICIAN.subtitle': '优先处理开放工单，及时关注低库存耗材。',
        'metric.availableEquipment': '可预约设备',
        'metric.myPending': '我的待审批',
        'metric.myApproved': '我的已批准',
        'metric.pendingReservations': '待审批预约',
        'metric.labCoverage': '实验室覆盖',
        'metric.openTickets': '待处理工单',
        'metric.lowStock': '低库存耗材',
        'metric.maintenanceEquipment': '维修中设备',
        'metric.totalEquipment': '设备总数',
        'message.reservationSubmitted': '预约申请 #{id} 已提交。',
        'message.equipmentSaved': '设备 #{id} 已保存。',
        'message.ticketCreated': '维修工单 #{id} 已创建。',
        'message.equipmentCannotReserve': '设备当前状态为{status}，不能预约。',
        'message.notEnoughStock': '库存不足，当前数量为 {quantity}。'
    },
    en: {
        'app.title': 'Laboratory Equipment Management System',
        'app.subtitle': 'Equipment reservations, maintenance tickets, and consumable inventory',
        'login.title': 'Laboratory Equipment Service',
        'login.description': 'Enter the workspace for your role to handle reservations, approvals, repairs, and inventory.',
        'login.demoAccounts': 'Demo accounts: admin/admin123, teacher/teacher123, student1/student123, tech/tech123',
        'user.guest': 'Not signed in',
        'user.badge': '{name} ({role})',
        'nav.equipment': 'Equipment',
        'nav.reservations': 'Reservations',
        'nav.maintenance': 'Maintenance',
        'nav.inventory': 'Inventory',
        'nav.reports': 'Reports',
        'nav.logout': 'Sign out',
        'equipment.title': 'Equipment Catalog',
        'equipment.subtitle': 'Search laboratory equipment and report faults when needed.',
        'equipment.searchPlaceholder': 'Asset tag, name, or category',
        'reservations.title': 'Reservation Management',
        'reservations.subtitle': 'Submit equipment reservation requests and process pending approvals.',
        'reservation.new': 'New Reservation Request',
        'reservation.selectEquipment': 'Select Equipment',
        'reservation.selectEquipmentHint': 'Select one or more devices for the same request.',
        'reservation.time': 'Reservation Time',
        'reservation.timeHint': 'Choose the start and end time.',
        'reservation.purposePlaceholder': 'Briefly explain why you need this equipment',
        'reservation.consumables': 'Consumable Needs',
        'reservation.consumablesHint': 'Leave blank if none are needed, or add items and quantities.',
        'reservation.noConsumables': 'No consumables are needed for this reservation.',
        'reservation.selectedCount': '{count} selected',
        'reservation.notSelected': 'None selected',
        'reservation.selectedSummary': 'Selected: {names}',
        'reservation.noSelectionSummary': 'No equipment selected. Expand this section to select one or more devices.',
        'reservation.noConsumableChoices': 'No consumables are available to request.',
        'reservation.stockMeta': 'Stock {quantity} {unit}',
        'maintenance.title': 'Maintenance Tickets',
        'maintenance.subtitle': 'Track equipment faults and repair progress.',
        'inventory.title': 'Consumable Inventory',
        'inventory.subtitle': 'Manage laboratory consumables and record every stock change.',
        'reports.title': 'Reports',
        'reports.subtitle': 'Management statistics generated from SQL views and aggregate queries.',
        'reports.labUsage': 'Laboratory Usage',
        'reports.equipmentStatus': 'Equipment Status Summary',
        'form.username': 'Username',
        'form.password': 'Password',
        'form.fullName': 'Full name',
        'form.email': 'Email',
        'form.confirmPassword': 'Confirm password',
        'form.course': 'Course',
        'form.startTime': 'Start time',
        'form.endTime': 'End time',
        'form.purpose': 'Purpose',
        'form.assetTag': 'Asset tag',
        'form.equipmentName': 'Equipment name',
        'form.category': 'Category',
        'form.lab': 'Lab',
        'form.status': 'Status',
        'form.purchaseDate': 'Purchase date',
        'form.riskLevel': 'Risk level',
        'form.notes': 'Notes',
        'form.comment': 'Approval comment',
        'form.title': 'Title',
        'form.description': 'Fault description',
        'form.priority': 'Priority',
        'form.technician': 'Technician',
        'form.note': 'Note',
        'form.itemName': 'Consumable name',
        'form.unit': 'Unit',
        'form.initialQuantity': 'Initial quantity',
        'form.reorderLevel': 'Reorder level',
        'form.amount': 'Change amount',
        'form.reason': 'Reason',
        'table.assetTag': 'Asset Tag',
        'table.equipmentName': 'Equipment Name',
        'table.equipment': 'Equipment',
        'table.category': 'Category',
        'table.lab': 'Lab',
        'table.status': 'Status',
        'table.riskLevel': 'Risk Level',
        'table.openTickets': 'Open Tickets',
        'table.actions': 'Actions',
        'table.requester': 'Requester',
        'table.start': 'Start',
        'table.end': 'End',
        'table.purpose': 'Purpose',
        'table.consumables': 'Consumables',
        'table.reporter': 'Reporter',
        'table.technician': 'Technician',
        'table.title': 'Title',
        'table.priority': 'Priority',
        'table.reportedAt': 'Reported At',
        'table.consumable': 'Consumable',
        'table.unit': 'Unit',
        'table.quantity': 'Quantity',
        'table.reorderLevel': 'Reorder Level',
        'table.stockAlert': 'Stock Alert',
        'table.reservations': 'Reservations',
        'table.approved': 'Approved',
        'table.completed': 'Completed',
        'table.highRisk': 'High Risk',
        'action.login': 'Sign in',
        'action.registerStudent': 'Register student account',
        'action.search': 'Search',
        'action.reset': 'Reset',
        'action.addEquipment': 'Add equipment',
        'action.refresh': 'Refresh',
        'action.addConsumable': 'Add consumable',
        'action.submitRequest': 'Submit request',
        'action.addConsumableType': 'Add consumable',
        'action.cancel': 'Cancel',
        'action.save': 'Save',
        'action.report': 'Report fault',
        'action.edit': 'Edit',
        'action.retire': 'Retire',
        'action.expand': 'Expand',
        'action.collapse': 'Collapse',
        'action.approve': 'Approve',
        'action.reject': 'Reject',
        'action.update': 'Update',
        'action.adjust': 'Adjust',
        'action.removeConsumable': 'Remove consumable',
        'modal.registerTitle': 'Register Student Account',
        'modal.registerHint': 'Self-registration only creates student accounts. Admin, teacher, and technician accounts are managed by administrators.',
        'modal.passwordMismatch': 'The two passwords do not match.',
        'modal.editEquipment': 'Edit Equipment',
        'modal.addEquipment': 'Add Equipment',
        'modal.retireEquipment': 'Retire Equipment',
        'modal.retireConfirm': 'Mark {assetTag} as retired? Historical reservations, maintenance tickets, and reports will be kept.',
        'modal.approveReservation': 'Approve Reservation',
        'modal.rejectReservation': 'Reject Reservation',
        'modal.approveComment': 'Approved',
        'modal.rejectComment': 'Rejected',
        'modal.reportProblem': 'Report Equipment Fault',
        'modal.updateTicket': 'Update Maintenance Ticket',
        'modal.addConsumableType': 'Add Consumable Type',
        'modal.changeStock': 'Adjust Stock',
        'placeholder.category': 'For example: Network / Computing',
        'placeholder.note': 'Add a short progress note',
        'placeholder.itemName': 'For example: sensor cable',
        'placeholder.reason': 'For example: used in lab class',
        'aria.consumableName': 'Consumable name',
        'aria.quantity': 'Requested quantity',
        'risk.suffix': 'risk',
        'common.none': 'None',
        'common.notAssigned': 'Not assigned',
        'common.normal': 'Normal',
        'workspace.label': '{role} Workspace',
        'workspace.ADMIN.title': 'Overview of lab resources and operations',
        'workspace.ADMIN.subtitle': 'Manage equipment records, reservation workflows, repairs, and consumable inventory.',
        'workspace.TEACHER.title': 'Handle course reservations and lab schedules',
        'workspace.TEACHER.subtitle': 'Review pending approvals first, then check equipment usage for your courses.',
        'workspace.STUDENT.title': 'Reserve lab equipment for course practice',
        'workspace.STUDENT.subtitle': 'Choose equipment and time first, then add consumables if needed.',
        'workspace.TECHNICIAN.title': 'Track repairs and consumable readiness',
        'workspace.TECHNICIAN.subtitle': 'Prioritize open tickets and keep an eye on low-stock consumables.',
        'metric.availableEquipment': 'Available Equipment',
        'metric.myPending': 'My Pending',
        'metric.myApproved': 'My Approved',
        'metric.pendingReservations': 'Pending Reservations',
        'metric.labCoverage': 'Lab Coverage',
        'metric.openTickets': 'Open Tickets',
        'metric.lowStock': 'Low-stock Consumables',
        'metric.maintenanceEquipment': 'Equipment in Maintenance',
        'metric.totalEquipment': 'Total Equipment',
        'message.reservationSubmitted': 'Reservation request #{id} submitted.',
        'message.equipmentSaved': 'Equipment #{id} saved.',
        'message.ticketCreated': 'Maintenance ticket #{id} created.',
        'message.equipmentCannotReserve': 'Equipment status is {status}, so it cannot be reserved.',
        'message.notEnoughStock': 'Not enough stock. Current quantity is {quantity}.'
    }
};

function t(key, values = {}) {
    let text = UI_TEXT[currentLanguage]?.[key] || UI_TEXT.zh[key] || key;
    Object.entries(values).forEach(([name, value]) => {
        text = text.replaceAll(`{${name}}`, String(value ?? ''));
    });
    return text;
}

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('[data-lang-option]').forEach(button => {
        button.addEventListener('click', () => switchLanguage(button.dataset.langOption));
    });
    $('loginForm').addEventListener('submit', login);
    $('registerButton').addEventListener('click', openRegisterForm);
    $('logoutButton').addEventListener('click', logout);
    $('equipmentSearch').addEventListener('submit', searchEquipment);
    $('resetEquipmentSearch').addEventListener('click', () => loadEquipment());
    $('addEquipmentButton').addEventListener('click', () => openEquipmentForm());
    $('toggleEquipmentPicker').addEventListener('click', toggleEquipmentPicker);
    $('reservationForm').addEventListener('submit', createReservation);
    $('addConsumableRequest').addEventListener('click', () => addConsumableRequestRow());
    $('refreshReservations').addEventListener('click', loadReservations);
    $('refreshMaintenance').addEventListener('click', loadMaintenance);
    $('addConsumableButton').addEventListener('click', () => openConsumableForm());
    $('refreshInventory').addEventListener('click', loadInventory);
    $('refreshReports').addEventListener('click', loadReports);
    $('modalCancel').addEventListener('click', () => $('modal').close());

    document.querySelectorAll('.tab[data-tab]').forEach(button => {
        button.addEventListener('click', () => switchTab(button.dataset.tab));
    });

    initializeReservationTimeInputs();
    applyStaticTranslations();
    updateUserBadge();
});


async function login(event) {
    event.preventDefault();
    const data = new FormData(event.target);
    const result = await post('/api/login', data);
    if (!result.ok) {
        showToast(result.message, true);
        return;
    }
    enterApp(result);
    await loadAll();
}

function enterApp(result) {
    currentUser = result;
    $('loginView').classList.add('hidden');
    $('appView').classList.remove('hidden');
    updateUserBadge();
    document.body.dataset.role = result.role;
    applyRoleVisibility();
}

function logout() {
    currentUser = null;
    delete document.body.dataset.role;
    $('loginView').classList.remove('hidden');
    $('appView').classList.add('hidden');
    updateUserBadge();
}

function openRegisterForm() {
    openModal(t('modal.registerTitle'), `
        <label>${escapeHtml(t('form.username'))} <input name="username" required></label>
        <label>${escapeHtml(t('form.fullName'))} <input name="fullName" required></label>
        <label>${escapeHtml(t('form.email'))} <input name="email" type="email" required></label>
        <label>${escapeHtml(t('form.password'))} <input name="password" type="password" required></label>
        <label>${escapeHtml(t('form.confirmPassword'))} <input name="confirmPassword" type="password" required></label>
        <p class="hint">${escapeHtml(t('modal.registerHint'))}</p>
    `, async (data) => {
        if (data.get('password') !== data.get('confirmPassword')) {
            showToast(t('modal.passwordMismatch'), true);
            return;
        }
        const result = await post('/api/register', data);
        showToast(result.message, !result.ok);
        if (result.ok) {
            enterApp(result);
            await loadAll();
        }
    });
}

async function loadAll() {
    // Load only the data needed by the current role. Students do not need inventory or reports.
    const tasks = [loadEquipment()];
    if (currentUser.role === 'ADMIN') {
        tasks.push(loadLabs());
    }
    if (canUseTab('reservations')) {
        tasks.push(loadCourses(), loadReservations(), loadConsumableChoices());
    }
    if (canUseTab('maintenance')) {
        tasks.push(loadTechnicians(), loadMaintenance());
    }
    if (canUseTab('inventory')) {
        tasks.push(loadInventory());
    }
    if (canUseTab('reports')) {
        tasks.push(loadReports());
    }
    await Promise.all(tasks);
    renderRoleWorkspace();
}

function switchTab(tabId) {
    if (!canUseTab(tabId)) {
        return;
    }
    document.querySelectorAll('.tab').forEach(button => button.classList.toggle('active', button.dataset.tab === tabId));
    document.querySelectorAll('.tab-page').forEach(page => page.classList.toggle('active', page.id === tabId));
}

function applyRoleVisibility() {
    const allowedTabs = tabsForRole(currentUser?.role);
    // Hide tabs instead of disabling them so each role has a cleaner workspace.
    document.querySelectorAll('.tab[data-tab]').forEach(button => {
        button.classList.toggle('hidden', !allowedTabs.includes(button.dataset.tab));
    });
    document.querySelectorAll('.tab-page').forEach(page => {
        page.classList.toggle('hidden', !allowedTabs.includes(page.id));
    });
    $('addEquipmentButton').classList.toggle('hidden', currentUser?.role !== 'ADMIN');
    $('addConsumableButton').classList.toggle('hidden', currentUser?.role !== 'ADMIN');
    switchTab(allowedTabs[0] || 'equipment');
}

function tabsForRole(role) {
    return ROLE_TABS[role] || ['equipment'];
}

function canUseTab(tabId) {
    return Boolean(tabId && tabsForRole(currentUser?.role).includes(tabId));
}

async function searchEquipment(event) {
    event.preventDefault();
    const q = new FormData(event.target).get('q');
    await loadEquipment(q);
}

async function loadEquipment(q = '') {
    const url = q ? `/api/equipment?q=${encodeURIComponent(q)}` : '/api/equipment';
    equipmentCache = await get(url);
    fillRows('equipmentRows', equipmentCache, e => `
        <tr>
            <td>${e.id}</td>
            <td>${escapeHtml(e.assetTag)}</td>
            <td>${escapeHtml(label(EQUIPMENT_NAME_LABELS, e.name))}</td>
            <td>${escapeHtml(label(CATEGORY_LABELS, e.category))}</td>
            <td>${escapeHtml(e.labCode)}</td>
            <td>${badge(e.status)}</td>
            <td>${escapeHtml(label(RISK_LABELS, e.riskLevel))}</td>
            <td>${e.openTicketCount}</td>
            <td>${equipmentActions(e)}</td>
        </tr>
    `);
    renderEquipmentPicker();
    renderRoleWorkspace();
}

function equipmentActions(equipment) {
    const buttons = [];
    if (equipment.status !== 'RETIRED') {
        buttons.push(`<button type="button" onclick="openReportProblem(${equipment.id})"><i class="icon-alert-triangle"></i> ${escapeHtml(t('action.report'))}</button>`);
    }
    if (currentUser?.role === 'ADMIN') {
        // Admin manages equipment records; other roles can only report problems.
        buttons.push(`<button type="button" class="secondary" onclick="openEquipmentForm(${equipment.id})"><i class="icon-edit"></i> ${escapeHtml(t('action.edit'))}</button>`);
        if (equipment.status !== 'RETIRED') {
            buttons.push(`<button type="button" class="danger" onclick="openRetireEquipment(${equipment.id})"><i class="icon-archive"></i> ${escapeHtml(t('action.retire'))}</button>`);
        }
    }
    return buttons.length ? `<div class="row-actions">${buttons.join('')}</div>` : '';
}

async function loadLabs() {
    labCache = await get('/api/labs');
}

async function loadCourses() {
    if (!currentUser) return;
    courseCache = await get(`/api/courses?userId=${currentUser.id}`);
    const select = $('reservationCourse');
    select.innerHTML = courseCache.map(c => `<option value="${c.id}">${escapeHtml(c.code)} - ${escapeHtml(label(COURSE_LABELS, c.name))}</option>`).join('');
}

async function loadTechnicians() {
    technicianCache = await get('/api/users/technicians');
}

function renderEquipmentPicker() {
    const container = $('reservationEquipment');
    if (!container) return;
    // Keep existing selections when the equipment table is refreshed after search or status updates.
    const selectedBefore = new Set(selectedEquipmentIds().map(String));
    container.innerHTML = equipmentCache.map(equipment => equipmentChoiceHtml(equipment, selectedBefore)).join('');
    container.querySelectorAll('input[name="equipmentChoice"]').forEach(input => {
        input.addEventListener('change', updateEquipmentSelectionSummary);
    });
    updateEquipmentSelectionSummary();
}

function equipmentChoiceHtml(equipment, selectedIds) {
    const bookable = BOOKABLE_EQUIPMENT_STATUSES.has(equipment.status);
    const checked = bookable && selectedIds.has(String(equipment.id));
    const className = ['equipment-choice'];
    if (checked) className.push('selected');
    if (!bookable) className.push('disabled');
    return `
        <label class="${className.join(' ')}">
            <input type="checkbox" name="equipmentChoice" value="${equipment.id}" ${checked ? 'checked' : ''} ${bookable ? '' : 'disabled'}>
            <span class="equipment-choice-main">
                <strong>${escapeHtml(label(EQUIPMENT_NAME_LABELS, equipment.name))}</strong>
                <span>${escapeHtml(equipment.assetTag)} / ${escapeHtml(equipment.labCode)}</span>
            </span>
            <span class="equipment-choice-meta">
                ${badge(equipment.status)}
                <span>${escapeHtml(`${label(RISK_LABELS, equipment.riskLevel)} ${t('risk.suffix')}`)}</span>
            </span>
        </label>
    `;
}

function selectedEquipmentIds() {
    return Array.from(document.querySelectorAll('#reservationEquipment input[name="equipmentChoice"]:checked'))
        .filter(input => !input.disabled)
        .map(input => input.value);
}

function updateEquipmentSelectionSummary() {
    const selectedInputs = Array.from(document.querySelectorAll('#reservationEquipment input[name="equipmentChoice"]'));
    selectedInputs.forEach(input => {
        input.closest('.equipment-choice')?.classList.toggle('selected', input.checked);
    });

    const ids = selectedEquipmentIds();
    $('reservationEquipmentIds').value = ids.join(',');
    $('selectedEquipmentCount').textContent = ids.length ? t('reservation.selectedCount', { count: ids.length }) : t('reservation.notSelected');

    const selectedNames = ids
        .map(id => equipmentCache.find(item => String(item.id) === String(id)))
        .filter(Boolean)
        .map(item => currentLanguage === 'zh'
            ? `${label(EQUIPMENT_NAME_LABELS, item.name)}（${item.assetTag}）`
            : `${label(EQUIPMENT_NAME_LABELS, item.name)} (${item.assetTag})`);
    $('selectedEquipmentSummary').textContent = selectedNames.length
        ? t('reservation.selectedSummary', { names: selectedNames.join(currentLanguage === 'zh' ? '、' : ', ') })
        : t('reservation.noSelectionSummary');
}

function toggleEquipmentPicker() {
    const section = $('reservationEquipmentSection');
    const button = $('toggleEquipmentPicker');
    if (!section || !button) return;
    // The collapsed state keeps the reservation form short while still showing what was selected.
    const collapsed = section.classList.toggle('collapsed');
    button.innerHTML = collapsed
        ? `<i class="icon-chevron-down"></i> ${escapeHtml(t('action.expand'))}`
        : `<i class="icon-chevron-up"></i> ${escapeHtml(t('action.collapse'))}`;
}

async function openEquipmentForm(equipmentId = null) {
    if (labCache.length === 0) {
        await loadLabs();
    }
    const equipment = equipmentId ? equipmentCache.find(item => item.id === equipmentId) : null;
    const labOptions = labCache.map(lab => optionHtml(lab.id, `${lab.code} - ${lab.name}`, equipment?.labId)).join('');
    openModal(equipment ? t('modal.editEquipment') : t('modal.addEquipment'), `
        ${equipment ? `<input type="hidden" name="equipmentId" value="${equipment.id}">` : ''}
        <label>${escapeHtml(t('form.assetTag'))} <input name="assetTag" value="${escapeHtml(equipment?.assetTag || '')}" required></label>
        <label>${escapeHtml(t('form.equipmentName'))} <input name="name" value="${escapeHtml(equipment?.name || '')}" required></label>
        <label>${escapeHtml(t('form.category'))} <input name="category" value="${escapeHtml(equipment?.category || '')}" placeholder="${escapeHtml(t('placeholder.category'))}" required></label>
        <label>${escapeHtml(t('form.lab'))} <select name="labId" required>${labOptions}</select></label>
        <label>${escapeHtml(t('form.status'))}
            <select name="status">
                ${optionHtml('AVAILABLE', label(STATUS_LABELS, 'AVAILABLE'), equipment?.status || 'AVAILABLE')}
                ${optionHtml('RESERVED', label(STATUS_LABELS, 'RESERVED'), equipment?.status)}
                ${optionHtml('IN_USE', label(STATUS_LABELS, 'IN_USE'), equipment?.status)}
                ${optionHtml('MAINTENANCE', label(STATUS_LABELS, 'MAINTENANCE'), equipment?.status)}
                ${optionHtml('RETIRED', label(STATUS_LABELS, 'RETIRED'), equipment?.status)}
            </select>
        </label>
        <label>${escapeHtml(t('form.purchaseDate'))} <input name="purchaseDate" type="date" value="${escapeHtml(equipment?.purchaseDate || '')}"></label>
        <label>${escapeHtml(t('form.riskLevel'))}
            <select name="riskLevel">
                ${optionHtml('LOW', label(RISK_LABELS, 'LOW'), equipment?.riskLevel || 'LOW')}
                ${optionHtml('MEDIUM', label(RISK_LABELS, 'MEDIUM'), equipment?.riskLevel)}
                ${optionHtml('HIGH', label(RISK_LABELS, 'HIGH'), equipment?.riskLevel)}
            </select>
        </label>
        <label>${escapeHtml(t('form.notes'))} <input name="notes" value="${escapeHtml(equipment?.notes || '')}"></label>
    `, async (data) => {
        data.append('userId', currentUser.id);
        const result = await post('/api/equipment/save', data);
        showToast(result.message, !result.ok);
        if (result.ok) {
            await Promise.all([loadEquipment(), canUseTab('reports') ? loadReports() : Promise.resolve()]);
        }
    });
}

function openRetireEquipment(equipmentId) {
    const equipment = equipmentCache.find(item => item.id === equipmentId);
    openModal(t('modal.retireEquipment'), `
        <p class="hint">${escapeHtml(t('modal.retireConfirm', { assetTag: equipment?.assetTag || '' }))}</p>
    `, async (data) => {
        data.append('equipmentId', equipmentId);
        data.append('userId', currentUser.id);
        const result = await post('/api/equipment/retire', data);
        showToast(result.message, !result.ok);
        if (result.ok) {
            await Promise.all([loadEquipment(), canUseTab('reports') ? loadReports() : Promise.resolve()]);
        }
    });
}

function optionHtml(value, text, selectedValue) {
    const selected = String(value) === String(selectedValue ?? '') ? ' selected' : '';
    return `<option value="${escapeHtml(value)}"${selected}>${escapeHtml(text)}</option>`;
}

async function createReservation(event) {
    event.preventDefault();
    const data = new FormData(event.target);
    const equipmentIds = selectedEquipmentIds();
    if (equipmentIds.length === 0) {
        showToast('At least one equipment item is required', true);
        return;
    }
    const normalizedTimes = normalizedReservationTimes();
    if (!normalizedTimes) {
        return;
    }
    data.set('startTime', normalizedTimes.startTime);
    data.set('endTime', normalizedTimes.endTime);
    // The backend expects a comma list for equipment and id:quantity pairs for consumables.
    data.set('equipmentIds', equipmentIds.join(','));
    data.set('consumableRequests', collectConsumableRequests());
    data.append('userId', currentUser.id);
    const result = await post('/api/reservations/create', data);
    showToast(result.message, !result.ok);
    if (result.ok) {
        renderConsumableRequestEditor([]);
        await loadReservations();
    }
}

function initializeReservationTimeInputs() {
    const startInput = document.querySelector('input[name="startTime"]');
    const endInput = document.querySelector('input[name="endTime"]');
    if (!startInput || !endInput) return;

    // Native datetime-local controls are easier for users than typing a date format by hand.
    const start = roundedFutureDate(1);
    const end = new Date(start);
    end.setHours(end.getHours() + 2);
    startInput.value = dateTimeLocalValue(start);
    endInput.value = dateTimeLocalValue(end);
    startInput.min = dateTimeLocalValue(roundedFutureDate(0));
    endInput.min = startInput.value;

    startInput.addEventListener('change', () => syncEndTimeWithStart());
    endInput.addEventListener('change', () => syncEndTimeWithStart(false));
}

function syncEndTimeWithStart(adjustEnd = true) {
    const startInput = document.querySelector('input[name="startTime"]');
    const endInput = document.querySelector('input[name="endTime"]');
    const start = parseDateTimeLocal(startInput?.value);
    const end = parseDateTimeLocal(endInput?.value);
    if (!startInput || !endInput || !start) return;

    endInput.min = startInput.value;
    if (adjustEnd && (!end || end <= start)) {
        const nextEnd = new Date(start);
        nextEnd.setHours(nextEnd.getHours() + 2);
        endInput.value = dateTimeLocalValue(nextEnd);
    }
}

function normalizedReservationTimes() {
    const startInput = document.querySelector('input[name="startTime"]');
    const endInput = document.querySelector('input[name="endTime"]');
    const start = parseDateTimeLocal(startInput?.value);
    const end = parseDateTimeLocal(endInput?.value);
    if (!start || !end) {
        showToast('Start time and end time are required', true);
        return null;
    }
    if (end <= start) {
        showToast('End time must be after start time', true);
        return null;
    }
    return {
        startTime: startInput.value.replace('T', ' '),
        endTime: endInput.value.replace('T', ' ')
    };
}

function roundedFutureDate(hoursAhead) {
    const date = new Date();
    date.setHours(date.getHours() + hoursAhead);
    date.setSeconds(0, 0);
    const remainder = date.getMinutes() % 30;
    if (remainder !== 0) {
        date.setMinutes(date.getMinutes() + (30 - remainder));
    }
    return date;
}

function dateTimeLocalValue(date) {
    const pad = value => String(value).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function parseDateTimeLocal(value) {
    if (!value) return null;
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? null : date;
}

async function loadReservations() {
    if (!currentUser) return;
    const rows = await get(`/api/reservations?userId=${currentUser.id}`);
    reservationCache = rows;
    const canDecide = currentUser.role === 'ADMIN' || currentUser.role === 'TEACHER';
    fillRows('reservationRows', rows, r => `
        <tr>
            <td>${r.id}</td>
            <td>${escapeHtml(r.assetTag)}</td>
            <td>${escapeHtml(translateEquipmentNames(r.equipmentName))}</td>
            <td>${escapeHtml(r.requesterName)}</td>
            <td>${escapeHtml(r.startTime)}</td>
            <td>${escapeHtml(r.endTime)}</td>
            <td>${badge(r.status)}</td>
            <td>${escapeHtml(label(DATA_TEXT_LABELS, r.purpose))}</td>
            <td>${escapeHtml(translateConsumableNeeds(r.consumableNeeds))}</td>
            <td>${reservationActions(r, canDecide)}</td>
        </tr>
    `);
    renderRoleWorkspace();
}

async function loadConsumableChoices() {
    consumableCache = await get('/api/inventory');
    renderConsumableRequestEditor(collectConsumableRows());
}

function renderConsumableRequestEditor(rows = []) {
    const container = $('reservationConsumables');
    if (!container) return;
    container.innerHTML = '';
    rows.forEach(row => addConsumableRequestRow(row.id, row.quantity));
    updateConsumableEmptyState();
}

function addConsumableRequestRow(consumableId = '', quantity = 1) {
    const container = $('reservationConsumables');
    if (!container || consumableCache.length === 0) {
        showToast(t('reservation.noConsumableChoices'), true);
        return;
    }
    const row = document.createElement('div');
    row.className = 'consumable-request-row';
    row.innerHTML = `
        <select data-consumable-select aria-label="${escapeHtml(t('aria.consumableName'))}"></select>
        <input type="number" min="1" value="${Number(quantity) || 1}" data-consumable-quantity aria-label="${escapeHtml(t('aria.quantity'))}">
        <span class="consumable-stock"></span>
        <button type="button" class="secondary icon-action" aria-label="${escapeHtml(t('action.removeConsumable'))}"><i class="icon-trash-2"></i></button>
    `;
    container.appendChild(row);

    // Each consumable can appear once in the request, so duplicate options are disabled later.
    const select = row.querySelector('[data-consumable-select]');
    select.innerHTML = consumableCache.map(item => `<option value="${item.id}">${escapeHtml(consumableOptionText(item))}</option>`).join('');
    select.value = String(consumableId || preferredConsumableId());
    row.querySelector('.icon-action').addEventListener('click', () => {
        row.remove();
        refreshConsumableRows();
    });
    select.addEventListener('change', refreshConsumableRows);
    refreshConsumableRows();
}

function preferredConsumableId() {
    const used = new Set(collectConsumableRows().map(row => String(row.id)));
    const unused = consumableCache.find(item => !used.has(String(item.id)));
    return String((unused || consumableCache[0]).id);
}

function refreshConsumableRows() {
    const rows = Array.from(document.querySelectorAll('#reservationConsumables .consumable-request-row'));
    const selected = rows.map(row => row.querySelector('[data-consumable-select]').value);
    rows.forEach(row => {
        const select = row.querySelector('[data-consumable-select]');
        const current = select.value || preferredConsumableId();
        select.innerHTML = consumableCache.map(item => {
            const value = String(item.id);
            const disabled = selected.includes(value) && value !== current;
            return `<option value="${value}" ${value === current ? 'selected' : ''} ${disabled ? 'disabled' : ''}>${escapeHtml(consumableOptionText(item))}</option>`;
        }).join('');
        updateConsumableRowMeta(row);
    });
    updateConsumableEmptyState();
}

function updateConsumableRowMeta(row) {
    const item = consumableCache.find(entry => String(entry.id) === row.querySelector('[data-consumable-select]').value);
    const meta = row.querySelector('.consumable-stock');
    meta.textContent = item ? t('reservation.stockMeta', { quantity: item.quantity, unit: label(UNIT_LABELS, item.unit) }) : '';
}

function updateConsumableEmptyState() {
    const empty = $('noConsumableRequests');
    if (!empty) return;
    const hasRows = document.querySelectorAll('#reservationConsumables .consumable-request-row').length > 0;
    empty.classList.toggle('hidden', hasRows);
}

function consumableOptionText(item) {
    return `${item.labCode} / ${label(ITEM_LABELS, item.itemName)}`;
}

function collectConsumableRows() {
    return Array.from(document.querySelectorAll('#reservationConsumables .consumable-request-row'))
        .map(row => ({
            id: row.querySelector('[data-consumable-select]')?.value,
            quantity: Number(row.querySelector('[data-consumable-quantity]')?.value || 0)
        }))
        .filter(row => row.id && row.quantity > 0);
}

function collectConsumableRequests() {
    const totals = new Map();
    collectConsumableRows().forEach(row => {
        totals.set(row.id, (totals.get(row.id) || 0) + row.quantity);
    });
    return Array.from(totals.entries())
        .map(([id, quantity]) => `${id}:${quantity}`)
        .join(',');
}

function reservationActions(row, canDecide) {
    const buttons = [];
    if (canDecide && row.status === 'PENDING') {
        buttons.push(`<button type="button" onclick="decideReservation(${row.id}, true)"><i class="icon-check"></i> ${escapeHtml(t('action.approve'))}</button>`);
        buttons.push(`<button type="button" class="danger" onclick="decideReservation(${row.id}, false)"><i class="icon-x"></i> ${escapeHtml(t('action.reject'))}</button>`);
    }
    if (row.status === 'PENDING' || row.status === 'APPROVED') {
        buttons.push(`<button type="button" class="secondary" onclick="cancelReservation(${row.id})"><i class="icon-x-circle"></i> ${escapeHtml(t('action.cancel'))}</button>`);
    }
    return buttons.join(' ');
}

function decideReservation(id, approve) {
    openModal(approve ? t('modal.approveReservation') : t('modal.rejectReservation'), `
        <label>${escapeHtml(t('form.comment'))} <input name="comment" value="${escapeHtml(approve ? t('modal.approveComment') : t('modal.rejectComment'))}"></label>
    `, async (data) => {
        data.append('reservationId', id);
        data.append('userId', currentUser.id);
        data.append('approve', approve);
        const result = await post('/api/reservations/decide', data);
        showToast(result.message, !result.ok);
        await loadReservations();
    });
}

async function cancelReservation(id) {
    const data = new FormData();
    data.append('reservationId', id);
    data.append('userId', currentUser.id);
    const result = await post('/api/reservations/cancel', data);
    showToast(result.message, !result.ok);
    await loadReservations();
}

function openReportProblem(equipmentId) {
    openModal(t('modal.reportProblem'), `
        <label>${escapeHtml(t('form.title'))} <input name="title" required></label>
        <label>${escapeHtml(t('form.description'))} <input name="description" required></label>
        <label>${escapeHtml(t('form.priority'))}
            <select name="priority">
                ${optionHtml('LOW', label(RISK_LABELS, 'LOW'))}
                ${optionHtml('MEDIUM', label(RISK_LABELS, 'MEDIUM'))}
                ${optionHtml('HIGH', label(RISK_LABELS, 'HIGH'))}
                ${optionHtml('URGENT', label(RISK_LABELS, 'URGENT'))}
            </select>
        </label>
    `, async (data) => {
        data.append('equipmentId', equipmentId);
        data.append('userId', currentUser.id);
        const result = await post('/api/maintenance/report', data);
        showToast(result.message, !result.ok);
        if (result.ok) {
            await Promise.all([loadEquipment(), loadMaintenance()]);
        }
    });
}

async function loadMaintenance() {
    const rows = await get('/api/maintenance');
    maintenanceCache = rows;
    const canUpdate = currentUser && (currentUser.role === 'ADMIN' || currentUser.role === 'TECHNICIAN');
    fillRows('maintenanceRows', rows, t => `
        <tr>
            <td>${t.id}</td>
            <td>${escapeHtml(t.assetTag)}</td>
            <td>${escapeHtml(label(EQUIPMENT_NAME_LABELS, t.equipmentName))}</td>
            <td>${escapeHtml(t.reporterName)}</td>
            <td>${escapeHtml(translatePersonText(t.technicianName))}</td>
            <td>${escapeHtml(label(DATA_TEXT_LABELS, t.title))}</td>
            <td>${escapeHtml(label(RISK_LABELS, t.priority))}</td>
            <td>${badge(t.status)}</td>
            <td>${escapeHtml(t.reportedAt)}</td>
            <td>${canUpdate ? `<button type="button" onclick="openUpdateTicket(${t.id})"><i class="icon-edit"></i> ${escapeHtml(t('action.update'))}</button>` : ''}</td>
        </tr>
    `);
    renderRoleWorkspace();
}

function openUpdateTicket(ticketId) {
    const techOptions = technicianCache.map(t => `<option value="${t.id}">${escapeHtml(t.fullName)}</option>`).join('');
    openModal(t('modal.updateTicket'), `
        <label>${escapeHtml(t('form.technician'))} <select name="technicianId">${techOptions}</select></label>
        <label>${escapeHtml(t('form.status'))}
            <select name="status">
                ${optionHtml('ASSIGNED', label(STATUS_LABELS, 'ASSIGNED'))}
                ${optionHtml('IN_PROGRESS', label(STATUS_LABELS, 'IN_PROGRESS'))}
                ${optionHtml('RESOLVED', label(STATUS_LABELS, 'RESOLVED'))}
                ${optionHtml('CLOSED', label(STATUS_LABELS, 'CLOSED'))}
            </select>
        </label>
        <label>${escapeHtml(t('form.note'))} <input name="note" placeholder="${escapeHtml(t('placeholder.note'))}"></label>
    `, async (data) => {
        data.append('ticketId', ticketId);
        data.append('userId', currentUser.id);
        const result = await post('/api/maintenance/update', data);
        showToast(result.message, !result.ok);
        if (result.ok) {
            await Promise.all([loadEquipment(), loadMaintenance()]);
        }
    });
}

async function loadInventory() {
    const rows = await get('/api/inventory');
    consumableCache = rows;
    inventoryCache = rows;
    renderConsumableRequestEditor(collectConsumableRows());
    const canChange = currentUser && (currentUser.role === 'ADMIN' || currentUser.role === 'TECHNICIAN');
    fillRows('inventoryRows', rows, item => `
        <tr>
            <td>${item.id}</td>
            <td>${escapeHtml(item.labCode)}</td>
            <td>${escapeHtml(label(ITEM_LABELS, item.itemName))}</td>
            <td>${escapeHtml(label(UNIT_LABELS, item.unit))}</td>
            <td>${item.quantity}</td>
            <td>${item.reorderLevel}</td>
            <td>${item.lowStock ? badge('LOW') : `<span style="color: var(--text-muted);">${escapeHtml(t('common.normal'))}</span>`}</td>
            <td>${canChange ? `<div class="row-actions"><button type="button" onclick="openChangeStock(${item.id})"><i class="icon-plus-minus"></i> ${escapeHtml(t('action.adjust'))}</button></div>` : ''}</td>
        </tr>
    `);
    renderRoleWorkspace();
}

async function openConsumableForm() {
    if (labCache.length === 0) {
        await loadLabs();
    }
    const labOptions = labCache.map(lab => optionHtml(lab.id, `${lab.code} - ${lab.name}`)).join('');
    openModal(t('modal.addConsumableType'), `
        <label>${escapeHtml(t('form.lab'))} <select name="labId" required>${labOptions}</select></label>
        <label>${escapeHtml(t('form.itemName'))} <input name="itemName" placeholder="${escapeHtml(t('placeholder.itemName'))}" required></label>
        <label>${escapeHtml(t('form.unit'))}
            <select name="unit">
                ${optionHtml('piece', label(UNIT_LABELS, 'piece'))}
                ${optionHtml('pack', label(UNIT_LABELS, 'pack'))}
                ${optionHtml('box', label(UNIT_LABELS, 'box'))}
            </select>
        </label>
        <label>${escapeHtml(t('form.initialQuantity'))} <input name="quantity" type="number" min="0" value="0" required></label>
        <label>${escapeHtml(t('form.reorderLevel'))} <input name="reorderLevel" type="number" min="0" value="1" required></label>
    `, async (data) => {
        data.append('userId', currentUser.id);
        const result = await post('/api/inventory/add', data);
        showToast(result.message, !result.ok);
        if (result.ok) {
            await Promise.all([loadInventory(), canUseTab('reports') ? loadReports() : Promise.resolve()]);
        }
    });
}

function openChangeStock(consumableId) {
    openModal(t('modal.changeStock'), `
        <label>${escapeHtml(t('form.amount'))} <input name="amount" value="1" required></label>
        <label>${escapeHtml(t('form.reason'))} <input name="reason" placeholder="${escapeHtml(t('placeholder.reason'))}" required></label>
    `, async (data) => {
        data.append('consumableId', consumableId);
        data.append('userId', currentUser.id);
        const result = await post('/api/inventory/change', data);
        showToast(result.message, !result.ok);
        if (result.ok) {
            await loadInventory();
        }
    });
}

async function loadReports() {
    const [labRows, statusRows] = await Promise.all([
        get('/api/reports/lab-usage'),
        get('/api/reports/equipment-status')
    ]);
    labReportCache = labRows;
    statusReportCache = statusRows;
    renderReportsFromCache();
    renderRoleWorkspace();
}

function renderReportsFromCache() {
    fillRows('labReportRows', labReportCache, r => `
        <tr>
            <td>${escapeHtml(r.label)}</td>
            <td>${r.reservations}</td>
            <td>${r.approved}</td>
            <td>${r.completed}</td>
        </tr>
    `);
    fillRows('statusReportRows', statusReportCache, r => `
        <tr>
            <td>${badge(r.label)}</td>
            <td>${r.equipmentCount}</td>
            <td>${r.highRisk}</td>
            <td>${r.openTickets}</td>
        </tr>
    `);
}

function renderRoleWorkspace() {
    const container = $('roleWorkspace');
    if (!container || !currentUser) return;

    const profile = roleWorkspaceProfile(currentUser.role);
    const metrics = roleMetrics(currentUser.role);
    container.innerHTML = `
        <div class="workspace-hero ${escapeHtml(currentUser.role.toLowerCase())}">
            <div>
                <p class="eyebrow">${escapeHtml(t('workspace.label', { role: label(ROLE_LABELS, currentUser.role) }))}</p>
                <h2>${escapeHtml(profile.title)}</h2>
                <p>${escapeHtml(profile.subtitle)}</p>
            </div>
        </div>
        <div class="metric-grid">
            ${metrics.map(metric => `
                <div class="metric-card">
                    <span class="metric-icon"><i class="${escapeHtml(metric.icon)}"></i></span>
                    <strong>${escapeHtml(metric.value)}</strong>
                    <span>${escapeHtml(metric.label)}</span>
                </div>
            `).join('')}
        </div>
    `;
}

function roleWorkspaceProfile(role) {
    const profiles = {
        ADMIN: {
            title: t('workspace.ADMIN.title'),
            subtitle: t('workspace.ADMIN.subtitle')
        },
        TEACHER: {
            title: t('workspace.TEACHER.title'),
            subtitle: t('workspace.TEACHER.subtitle')
        },
        STUDENT: {
            title: t('workspace.STUDENT.title'),
            subtitle: t('workspace.STUDENT.subtitle')
        },
        TECHNICIAN: {
            title: t('workspace.TECHNICIAN.title'),
            subtitle: t('workspace.TECHNICIAN.subtitle')
        }
    };
    return profiles[role] || profiles.STUDENT;
}

function roleMetrics(role) {
    const availableEquipment = equipmentCache.filter(item => item.status === 'AVAILABLE').length;
    const maintenanceEquipment = equipmentCache.filter(item => item.status === 'MAINTENANCE').length;
    const pendingReservations = reservationCache.filter(item => item.status === 'PENDING').length;
    const approvedReservations = reservationCache.filter(item => item.status === 'APPROVED').length;
    const openTickets = maintenanceCache.filter(item => ['OPEN', 'ASSIGNED', 'IN_PROGRESS'].includes(item.status)).length;
    const lowStock = inventoryCache.filter(item => item.lowStock).length;
    const totalLabs = labReportCache.length || new Set(equipmentCache.map(item => item.labCode)).size;

    // These numbers are simple dashboard hints, not separate navigation buttons.
    if (role === 'STUDENT') {
        return [
            { label: t('metric.availableEquipment'), value: availableEquipment, icon: 'icon-cpu', tab: 'equipment' },
            { label: t('metric.myPending'), value: pendingReservations, icon: 'icon-clock-3', tab: 'reservations' },
            { label: t('metric.myApproved'), value: approvedReservations, icon: 'icon-check-circle-2', tab: 'reservations' }
        ];
    }
    if (role === 'TEACHER') {
        return [
            { label: t('metric.pendingReservations'), value: pendingReservations, icon: 'icon-clipboard-check', tab: 'reservations' },
            { label: t('metric.availableEquipment'), value: availableEquipment, icon: 'icon-cpu', tab: 'equipment' },
            { label: t('metric.labCoverage'), value: totalLabs, icon: 'icon-building-2', tab: 'reports' }
        ];
    }
    if (role === 'TECHNICIAN') {
        return [
            { label: t('metric.openTickets'), value: openTickets, icon: 'icon-wrench', tab: 'maintenance' },
            { label: t('metric.lowStock'), value: lowStock, icon: 'icon-package-x', tab: 'inventory' },
            { label: t('metric.maintenanceEquipment'), value: maintenanceEquipment, icon: 'icon-alert-triangle', tab: 'equipment' }
        ];
    }
    return [
        { label: t('metric.totalEquipment'), value: equipmentCache.length, icon: 'icon-boxes', tab: 'equipment' },
        { label: t('metric.pendingReservations'), value: pendingReservations, icon: 'icon-clipboard-check', tab: 'reservations' },
        { label: t('metric.maintenanceEquipment'), value: maintenanceEquipment, icon: 'icon-wrench', tab: 'maintenance' },
        { label: t('metric.lowStock'), value: lowStock, icon: 'icon-package-x', tab: 'inventory' }
    ];
}

function openModal(title, bodyHtml, onSubmit) {
    const modal = $('modal');
    $('modalTitle').textContent = title;
    $('modalBody').innerHTML = bodyHtml;
    $('modalForm').onsubmit = async (event) => {
        event.preventDefault();
        const data = new FormData(event.target);
        modal.close();
        await onSubmit(data);
    };
    modal.showModal();
}

async function get(url) {
    const response = await fetch(`${API_PREFIX}${url}`);
    const data = await response.json();
    if (data.ok === false) {
        showToast(data.message, true);
        return [];
    }
    return data;
}

async function post(url, data) {
    const response = await fetch(`${API_PREFIX}${url}`, {
        method: 'POST',
        body: new URLSearchParams(data)
    });
    return response.json();
}

function fillRows(id, rows, render) {
    $(id).innerHTML = rows.map(render).join('');
}

function switchLanguage(language) {
    if (!['zh', 'en'].includes(language) || language === currentLanguage) {
        return;
    }
    currentLanguage = language;
    localStorage.setItem(LANG_STORAGE_KEY, language);
    applyStaticTranslations();
    updateUserBadge();
    rerenderCurrentData();
}

function applyStaticTranslations() {
    document.documentElement.lang = currentLanguage === 'en' ? 'en' : 'zh-CN';
    document.title = t('app.title');

    document.querySelectorAll('[data-i18n]').forEach(element => {
        setTranslatedContent(element, t(element.dataset.i18n));
    });
    document.querySelectorAll('[data-i18n-placeholder]').forEach(element => {
        element.setAttribute('placeholder', t(element.dataset.i18nPlaceholder));
    });
    document.querySelectorAll('[data-lang-option]').forEach(button => {
        const active = button.dataset.langOption === currentLanguage;
        button.classList.toggle('active', active);
        button.setAttribute('aria-pressed', String(active));
    });
}

function setTranslatedContent(element, text) {
    const icon = Array.from(element.children).find(child => child.tagName === 'I');
    if (!icon) {
        element.textContent = text;
        return;
    }
    const iconHtml = icon.outerHTML;
    element.innerHTML = `${iconHtml} ${escapeHtml(text)}`;
}

function updateUserBadge() {
    const badge = $('userBadge');
    if (!badge) return;
    const labelText = currentUser
        ? t('user.badge', { name: currentUser.fullName, role: label(ROLE_LABELS, currentUser.role) })
        : t('user.guest');
    badge.innerHTML = `<i class="icon-user"></i> ${escapeHtml(labelText)}`;
}

function rerenderCurrentData() {
    if (equipmentCache.length > 0) {
        fillRows('equipmentRows', equipmentCache, e => `
            <tr>
                <td>${e.id}</td>
                <td>${escapeHtml(e.assetTag)}</td>
                <td>${escapeHtml(label(EQUIPMENT_NAME_LABELS, e.name))}</td>
                <td>${escapeHtml(label(CATEGORY_LABELS, e.category))}</td>
                <td>${escapeHtml(e.labCode)}</td>
                <td>${badge(e.status)}</td>
                <td>${escapeHtml(label(RISK_LABELS, e.riskLevel))}</td>
                <td>${e.openTicketCount}</td>
                <td>${equipmentActions(e)}</td>
            </tr>
        `);
        renderEquipmentPicker();
    }
    if (courseCache.length > 0) {
        const select = $('reservationCourse');
        select.innerHTML = courseCache.map(c => `<option value="${c.id}">${escapeHtml(c.code)} - ${escapeHtml(label(COURSE_LABELS, c.name))}</option>`).join('');
    }
    if (reservationCache.length > 0 && currentUser) {
        const canDecide = currentUser.role === 'ADMIN' || currentUser.role === 'TEACHER';
        fillRows('reservationRows', reservationCache, r => `
            <tr>
                <td>${r.id}</td>
                <td>${escapeHtml(r.assetTag)}</td>
                <td>${escapeHtml(translateEquipmentNames(r.equipmentName))}</td>
                <td>${escapeHtml(r.requesterName)}</td>
                <td>${escapeHtml(r.startTime)}</td>
                <td>${escapeHtml(r.endTime)}</td>
                <td>${badge(r.status)}</td>
                <td>${escapeHtml(label(DATA_TEXT_LABELS, r.purpose))}</td>
                <td>${escapeHtml(translateConsumableNeeds(r.consumableNeeds))}</td>
                <td>${reservationActions(r, canDecide)}</td>
            </tr>
        `);
    }
    if (maintenanceCache.length > 0) {
        const canUpdate = currentUser && (currentUser.role === 'ADMIN' || currentUser.role === 'TECHNICIAN');
        fillRows('maintenanceRows', maintenanceCache, ticket => `
            <tr>
                <td>${ticket.id}</td>
                <td>${escapeHtml(ticket.assetTag)}</td>
                <td>${escapeHtml(label(EQUIPMENT_NAME_LABELS, ticket.equipmentName))}</td>
                <td>${escapeHtml(ticket.reporterName)}</td>
                <td>${escapeHtml(translatePersonText(ticket.technicianName))}</td>
                <td>${escapeHtml(label(DATA_TEXT_LABELS, ticket.title))}</td>
                <td>${escapeHtml(label(RISK_LABELS, ticket.priority))}</td>
                <td>${badge(ticket.status)}</td>
                <td>${escapeHtml(ticket.reportedAt)}</td>
                <td>${canUpdate ? `<button type="button" onclick="openUpdateTicket(${ticket.id})"><i class="icon-edit"></i> ${escapeHtml(t('action.update'))}</button>` : ''}</td>
            </tr>
        `);
    }
    if (inventoryCache.length > 0) {
        const canChange = currentUser && (currentUser.role === 'ADMIN' || currentUser.role === 'TECHNICIAN');
        fillRows('inventoryRows', inventoryCache, item => `
            <tr>
                <td>${item.id}</td>
                <td>${escapeHtml(item.labCode)}</td>
                <td>${escapeHtml(label(ITEM_LABELS, item.itemName))}</td>
                <td>${escapeHtml(label(UNIT_LABELS, item.unit))}</td>
                <td>${item.quantity}</td>
                <td>${item.reorderLevel}</td>
                <td>${item.lowStock ? badge('LOW') : `<span style="color: var(--text-muted);">${escapeHtml(t('common.normal'))}</span>`}</td>
                <td>${canChange ? `<div class="row-actions"><button type="button" onclick="openChangeStock(${item.id})"><i class="icon-plus-minus"></i> ${escapeHtml(t('action.adjust'))}</button></div>` : ''}</td>
            </tr>
        `);
        renderConsumableRequestEditor(collectConsumableRows());
    }
    if (labReportCache.length > 0 || statusReportCache.length > 0) {
        renderReportsFromCache();
    }
    if (currentUser) {
        renderRoleWorkspace();
    }
}

function badge(text) {
    const raw = String(text);
    return `<span class="status ${escapeHtml(raw)}">${escapeHtml(label(STATUS_LABELS, raw))}</span>`;
}

let toastTimer = null;
function showToast(message, isError = false) {
    const toast = $('toast');
    toast.textContent = translateMessage(message);
    toast.className = 'toast' + (isError ? ' error' : '');
    if (toastTimer) clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
        toast.classList.add('hiding');
        setTimeout(() => {
            toast.classList.add('hidden');
            toast.classList.remove('hiding');
        }, 300);
    }, 2800);
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function label(map, value) {
    const entries = map[currentLanguage] || map;
    return entries[String(value)] || value || '';
}

function translateEquipmentNames(value) {
    return String(value || '')
        .split(', ')
        .map(name => label(EQUIPMENT_NAME_LABELS, name))
        .join(currentLanguage === 'zh' ? '，' : ', ');
}

function translateConsumableNeeds(value) {
    const text = String(value || '').trim();
    if (!text) {
        return t('common.none');
    }
    return text.split(', ').map(item => {
        const match = item.match(/^(.+) x(\d+)$/);
        if (!match) {
            return item;
        }
        return `${label(ITEM_LABELS, match[1])} x${match[2]}`;
    }).join(currentLanguage === 'zh' ? '，' : ', ');
}

function translatePersonText(value) {
    return String(value || '') === 'Not assigned' ? t('common.notAssigned') : value;
}

function translateMessage(message) {
    const text = String(message ?? '');
    const messageLabels = MESSAGE_LABELS[currentLanguage] || {};
    if (messageLabels[text]) {
        return messageLabels[text];
    }

    let match = text.match(/^Reservation request #(\d+) submitted\.$/);
    if (match) {
        return t('message.reservationSubmitted', { id: match[1] });
    }

    match = text.match(/^Equipment #(\d+) saved\.$/);
    if (match) {
        return t('message.equipmentSaved', { id: match[1] });
    }

    match = text.match(/^Maintenance ticket #(\d+) created\.$/);
    if (match) {
        return t('message.ticketCreated', { id: match[1] });
    }

    match = text.match(/^Equipment status is (.+), so it cannot be reserved\.$/);
    if (match) {
        return t('message.equipmentCannotReserve', { status: label(STATUS_LABELS, match[1]) });
    }

    match = text.match(/^Not enough stock\. Current quantity is (\d+)$/);
    if (match) {
        return t('message.notEnoughStock', { quantity: match[1] });
    }

    return text;
}
