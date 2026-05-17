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
const BOOKABLE_EQUIPMENT_STATUSES = new Set(['AVAILABLE', 'RESERVED']);

const ROLE_LABELS = {
    ADMIN: '管理员',
    TEACHER: '教师',
    STUDENT: '学生',
    TECHNICIAN: '技术员'
};

const STATUS_LABELS = {
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
};

const RISK_LABELS = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    URGENT: '紧急'
};

const CATEGORY_LABELS = {
    Computing: '计算设备',
    Robotics: '机器人设备',
    Measurement: '测量设备',
    Sensor: '传感器',
    Network: '网络设备'
};

const COURSE_LABELS = {
    'Databases and Information Systems': '数据库与信息系统',
    'Machine Learning Engineering': '机器学习工程'
};

const EQUIPMENT_NAME_LABELS = {
    'GPU Workstation A': 'GPU 工作站 A',
    'Mobile Robot TurtleBot': 'TurtleBot 移动机器人',
    'Digital Oscilloscope': '数字示波器',
    'ECG Sensor Kit': 'ECG 传感器套件',
    'IoT Gateway Set': '物联网网关套件',
    'Managed Switch Rack': '可管理交换机机架'
};

const ITEM_LABELS = {
    'Robot battery pack': '机器人电池包',
    'ECG electrode pad': 'ECG 电极贴片',
    'Ethernet cable': '网线',
    'Micro SD card': 'Micro SD 卡'
};

const UNIT_LABELS = {
    piece: '件',
    pack: '包',
    box: '盒'
};

const DATA_TEXT_LABELS = {
    'Train small image classifier': '训练小型图像分类模型',
    'IoT gateway demo preparation': '物联网网关演示准备',
    'Measure sensor output for lab exercise': '测量实验课传感器输出',
    'ECG cable is loose': 'ECG 线缆松动',
    'Switch fan noise': '交换机风扇噪声'
};

// The front-end keeps role rules visible, so each demo account only sees its own workspace.
const ROLE_TABS = {
    ADMIN: ['equipment', 'reservations', 'maintenance', 'inventory', 'reports'],
    TEACHER: ['reservations', 'equipment', 'reports'],
    STUDENT: ['reservations', 'equipment'],
    TECHNICIAN: ['maintenance', 'equipment', 'inventory']
};

const MESSAGE_LABELS = {
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
};

document.addEventListener('DOMContentLoaded', () => {
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
    $('userBadge').innerHTML = `<i class="icon-user"></i> ${escapeHtml(result.fullName)}（${escapeHtml(label(ROLE_LABELS, result.role))}）`;
    document.body.dataset.role = result.role;
    applyRoleVisibility();
}

function logout() {
    currentUser = null;
    delete document.body.dataset.role;
    $('loginView').classList.remove('hidden');
    $('appView').classList.add('hidden');
    $('userBadge').innerHTML = '<i class="icon-user"></i> 未登录';
}

function openRegisterForm() {
    openModal('注册学生账号', `
        <label>用户名 <input name="username" required></label>
        <label>姓名 <input name="fullName" required></label>
        <label>邮箱 <input name="email" type="email" required></label>
        <label>密码 <input name="password" type="password" required></label>
        <label>确认密码 <input name="confirmPassword" type="password" required></label>
        <p class="hint">自助注册只开放学生身份，管理员、教师和技术员账号由管理员维护。</p>
    `, async (data) => {
        if (data.get('password') !== data.get('confirmPassword')) {
            showToast('两次输入的密码不一致。', true);
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
        buttons.push(`<button type="button" onclick="openReportProblem(${equipment.id})"><i class="icon-alert-triangle"></i> 报修</button>`);
    }
    if (currentUser?.role === 'ADMIN') {
        // Admin manages equipment records; other roles can only report problems.
        buttons.push(`<button type="button" class="secondary" onclick="openEquipmentForm(${equipment.id})"><i class="icon-edit"></i> 编辑</button>`);
        if (equipment.status !== 'RETIRED') {
            buttons.push(`<button type="button" class="danger" onclick="openRetireEquipment(${equipment.id})"><i class="icon-archive"></i> 退役</button>`);
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
                <span>${escapeHtml(label(RISK_LABELS, equipment.riskLevel))}风险</span>
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
    $('selectedEquipmentCount').textContent = ids.length ? `已选 ${ids.length} 台` : '未选择';

    const selectedNames = ids
        .map(id => equipmentCache.find(item => String(item.id) === String(id)))
        .filter(Boolean)
        .map(item => `${label(EQUIPMENT_NAME_LABELS, item.name)}（${item.assetTag}）`);
    $('selectedEquipmentSummary').textContent = selectedNames.length
        ? `已选：${selectedNames.join('、')}`
        : '未选择设备，展开后可勾选一台或多台设备。';
}

function toggleEquipmentPicker() {
    const section = $('reservationEquipmentSection');
    const button = $('toggleEquipmentPicker');
    if (!section || !button) return;
    // The collapsed state keeps the reservation form short while still showing what was selected.
    const collapsed = section.classList.toggle('collapsed');
    button.innerHTML = collapsed
        ? '<i class="icon-chevron-down"></i> 展开选择'
        : '<i class="icon-chevron-up"></i> 收起';
}

async function openEquipmentForm(equipmentId = null) {
    if (labCache.length === 0) {
        await loadLabs();
    }
    const equipment = equipmentId ? equipmentCache.find(item => item.id === equipmentId) : null;
    const labOptions = labCache.map(lab => optionHtml(lab.id, `${lab.code} - ${lab.name}`, equipment?.labId)).join('');
    openModal(equipment ? '编辑设备' : '新增设备', `
        ${equipment ? `<input type="hidden" name="equipmentId" value="${equipment.id}">` : ''}
        <label>设备编号 <input name="assetTag" value="${escapeHtml(equipment?.assetTag || '')}" required></label>
        <label>设备名称 <input name="name" value="${escapeHtml(equipment?.name || '')}" required></label>
        <label>类别 <input name="category" value="${escapeHtml(equipment?.category || '')}" placeholder="例如 Network / Computing" required></label>
        <label>实验室 <select name="labId" required>${labOptions}</select></label>
        <label>状态
            <select name="status">
                ${optionHtml('AVAILABLE', '可预约', equipment?.status || 'AVAILABLE')}
                ${optionHtml('RESERVED', '已预约', equipment?.status)}
                ${optionHtml('IN_USE', '使用中', equipment?.status)}
                ${optionHtml('MAINTENANCE', '维修中', equipment?.status)}
                ${optionHtml('RETIRED', '已退役', equipment?.status)}
            </select>
        </label>
        <label>购买日期 <input name="purchaseDate" type="date" value="${escapeHtml(equipment?.purchaseDate || '')}"></label>
        <label>风险等级
            <select name="riskLevel">
                ${optionHtml('LOW', '低', equipment?.riskLevel || 'LOW')}
                ${optionHtml('MEDIUM', '中', equipment?.riskLevel)}
                ${optionHtml('HIGH', '高', equipment?.riskLevel)}
            </select>
        </label>
        <label>备注 <input name="notes" value="${escapeHtml(equipment?.notes || '')}"></label>
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
    openModal('退役设备', `
        <p class="hint">确认将 ${escapeHtml(equipment?.assetTag || '')} 标记为已退役？历史预约、维修工单和报表记录会保留。</p>
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
        showToast('请至少选择一台设备。', true);
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
        showToast('请填写开始时间和结束时间。', true);
        return null;
    }
    if (end <= start) {
        showToast('结束时间必须晚于开始时间。', true);
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
        showToast('暂无可申请耗材。', true);
        return;
    }
    const row = document.createElement('div');
    row.className = 'consumable-request-row';
    row.innerHTML = `
        <select data-consumable-select aria-label="耗材名称"></select>
        <input type="number" min="1" value="${Number(quantity) || 1}" data-consumable-quantity aria-label="申请数量">
        <span class="consumable-stock"></span>
        <button type="button" class="secondary icon-action" aria-label="移除耗材"><i class="icon-trash-2"></i></button>
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
    meta.textContent = item ? `库存 ${item.quantity} ${label(UNIT_LABELS, item.unit)}` : '';
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
        buttons.push(`<button type="button" onclick="decideReservation(${row.id}, true)"><i class="icon-check"></i> 批准</button>`);
        buttons.push(`<button type="button" class="danger" onclick="decideReservation(${row.id}, false)"><i class="icon-x"></i> 拒绝</button>`);
    }
    if (row.status === 'PENDING' || row.status === 'APPROVED') {
        buttons.push(`<button type="button" class="secondary" onclick="cancelReservation(${row.id})"><i class="icon-x-circle"></i> 取消</button>`);
    }
    return buttons.join(' ');
}

function decideReservation(id, approve) {
    openModal(approve ? '批准预约' : '拒绝预约', `
        <label>审批备注 <input name="comment" value="${approve ? '同意预约' : '拒绝预约'}"></label>
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
    openModal('提交设备报修', `
        <label>标题 <input name="title" required></label>
        <label>故障描述 <input name="description" required></label>
        <label>优先级
            <select name="priority">
                <option value="LOW">低</option>
                <option value="MEDIUM">中</option>
                <option value="HIGH">高</option>
                <option value="URGENT">紧急</option>
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
            <td>${canUpdate ? `<button type="button" onclick="openUpdateTicket(${t.id})"><i class="icon-edit"></i> 更新</button>` : ''}</td>
        </tr>
    `);
    renderRoleWorkspace();
}

function openUpdateTicket(ticketId) {
    const techOptions = technicianCache.map(t => `<option value="${t.id}">${escapeHtml(t.fullName)}</option>`).join('');
    openModal('更新维修工单', `
        <label>维修人员 <select name="technicianId">${techOptions}</select></label>
        <label>状态
            <select name="status">
                <option value="ASSIGNED">已分配</option>
                <option value="IN_PROGRESS">处理中</option>
                <option value="RESOLVED">已解决</option>
                <option value="CLOSED">已关闭</option>
            </select>
        </label>
        <label>备注 <input name="note" placeholder="填写简短处理记录"></label>
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
            <td>${item.lowStock ? badge('LOW') : '<span style="color: var(--text-muted);">正常</span>'}</td>
            <td>${canChange ? `<div class="row-actions"><button type="button" onclick="openChangeStock(${item.id})"><i class="icon-plus-minus"></i> 调整</button></div>` : ''}</td>
        </tr>
    `);
    renderRoleWorkspace();
}

async function openConsumableForm() {
    if (labCache.length === 0) {
        await loadLabs();
    }
    const labOptions = labCache.map(lab => optionHtml(lab.id, `${lab.code} - ${lab.name}`)).join('');
    openModal('新增耗材种类', `
        <label>实验室 <select name="labId" required>${labOptions}</select></label>
        <label>耗材名称 <input name="itemName" placeholder="例如：传感器连接线" required></label>
        <label>单位
            <select name="unit">
                ${optionHtml('piece', '件')}
                ${optionHtml('pack', '包')}
                ${optionHtml('box', '盒')}
            </select>
        </label>
        <label>初始数量 <input name="quantity" type="number" min="0" value="0" required></label>
        <label>补货线 <input name="reorderLevel" type="number" min="0" value="1" required></label>
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
    openModal('调整库存', `
        <label>变动数量 <input name="amount" value="1" required></label>
        <label>原因 <input name="reason" placeholder="例如：实验课使用" required></label>
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
    fillRows('labReportRows', labRows, r => `
        <tr>
            <td>${escapeHtml(r.label)}</td>
            <td>${r.reservations}</td>
            <td>${r.approved}</td>
            <td>${r.completed}</td>
        </tr>
    `);
    fillRows('statusReportRows', statusRows, r => `
        <tr>
            <td>${badge(r.label)}</td>
            <td>${r.equipmentCount}</td>
            <td>${r.highRisk}</td>
            <td>${r.openTickets}</td>
        </tr>
    `);
    renderRoleWorkspace();
}

function renderRoleWorkspace() {
    const container = $('roleWorkspace');
    if (!container || !currentUser) return;

    const profile = roleWorkspaceProfile(currentUser.role);
    const metrics = roleMetrics(currentUser.role);
    container.innerHTML = `
        <div class="workspace-hero ${escapeHtml(currentUser.role.toLowerCase())}">
            <div>
                <p class="eyebrow">${escapeHtml(label(ROLE_LABELS, currentUser.role))}工作台</p>
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
            title: '总览实验室资源与运行状态',
            subtitle: '管理设备台账、预约流转、维修进度和耗材库存。'
        },
        TEACHER: {
            title: '处理课程预约与实验安排',
            subtitle: '优先查看待审批预约，再检查课程相关设备使用情况。'
        },
        STUDENT: {
            title: '预约实验设备，准备课程实践',
            subtitle: '先选设备和时间，需要耗材时随预约一起提交。'
        },
        TECHNICIAN: {
            title: '跟进维修任务与耗材保障',
            subtitle: '优先处理开放工单，及时关注低库存耗材。'
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
            { label: '可预约设备', value: availableEquipment, icon: 'icon-cpu', tab: 'equipment' },
            { label: '我的待审批', value: pendingReservations, icon: 'icon-clock-3', tab: 'reservations' },
            { label: '我的已批准', value: approvedReservations, icon: 'icon-check-circle-2', tab: 'reservations' }
        ];
    }
    if (role === 'TEACHER') {
        return [
            { label: '待审批预约', value: pendingReservations, icon: 'icon-clipboard-check', tab: 'reservations' },
            { label: '可预约设备', value: availableEquipment, icon: 'icon-cpu', tab: 'equipment' },
            { label: '实验室覆盖', value: totalLabs, icon: 'icon-building-2', tab: 'reports' }
        ];
    }
    if (role === 'TECHNICIAN') {
        return [
            { label: '待处理工单', value: openTickets, icon: 'icon-wrench', tab: 'maintenance' },
            { label: '低库存耗材', value: lowStock, icon: 'icon-package-x', tab: 'inventory' },
            { label: '维修中设备', value: maintenanceEquipment, icon: 'icon-alert-triangle', tab: 'equipment' }
        ];
    }
    return [
        { label: '设备总数', value: equipmentCache.length, icon: 'icon-boxes', tab: 'equipment' },
        { label: '待审批预约', value: pendingReservations, icon: 'icon-clipboard-check', tab: 'reservations' },
        { label: '维修中设备', value: maintenanceEquipment, icon: 'icon-wrench', tab: 'maintenance' },
        { label: '低库存耗材', value: lowStock, icon: 'icon-package-x', tab: 'inventory' }
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
    const response = await fetch(url);
    const data = await response.json();
    if (data.ok === false) {
        showToast(data.message, true);
        return [];
    }
    return data;
}

async function post(url, data) {
    const response = await fetch(url, {
        method: 'POST',
        body: new URLSearchParams(data)
    });
    return response.json();
}

function fillRows(id, rows, render) {
    $(id).innerHTML = rows.map(render).join('');
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
    return map[String(value)] || value || '';
}

function translateEquipmentNames(value) {
    return String(value || '')
        .split(', ')
        .map(name => label(EQUIPMENT_NAME_LABELS, name))
        .join('，');
}

function translateConsumableNeeds(value) {
    const text = String(value || '').trim();
    if (!text) {
        return '无';
    }
    return text.split(', ').map(item => {
        const match = item.match(/^(.+) x(\d+)$/);
        if (!match) {
            return item;
        }
        return `${label(ITEM_LABELS, match[1])} x${match[2]}`;
    }).join('，');
}

function translatePersonText(value) {
    return String(value || '') === 'Not assigned' ? '未分配' : value;
}

function translateMessage(message) {
    const text = String(message ?? '');
    if (MESSAGE_LABELS[text]) {
        return MESSAGE_LABELS[text];
    }

    let match = text.match(/^Reservation request #(\d+) submitted\.$/);
    if (match) {
        return `预约申请 #${match[1]} 已提交。`;
    }

    match = text.match(/^Equipment #(\d+) saved\.$/);
    if (match) {
        return `设备 #${match[1]} 已保存。`;
    }

    match = text.match(/^Maintenance ticket #(\d+) created\.$/);
    if (match) {
        return `维修工单 #${match[1]} 已创建。`;
    }

    match = text.match(/^Equipment status is (.+), so it cannot be reserved\.$/);
    if (match) {
        return `设备当前状态为${label(STATUS_LABELS, match[1])}，不能预约。`;
    }

    match = text.match(/^Not enough stock\. Current quantity is (\d+)$/);
    if (match) {
        return `库存不足，当前数量为 ${match[1]}。`;
    }

    return text;
}
