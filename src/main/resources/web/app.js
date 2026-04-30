let currentUser = null;
let equipmentCache = [];
let courseCache = [];
let technicianCache = [];

const $ = (id) => document.getElementById(id);

document.addEventListener('DOMContentLoaded', () => {
    $('loginForm').addEventListener('submit', login);
    $('logoutButton').addEventListener('click', logout);
    $('equipmentSearch').addEventListener('submit', searchEquipment);
    $('resetEquipmentSearch').addEventListener('click', () => loadEquipment());
    $('reservationForm').addEventListener('submit', createReservation);
    $('refreshReservations').addEventListener('click', loadReservations);
    $('refreshMaintenance').addEventListener('click', loadMaintenance);
    $('refreshInventory').addEventListener('click', loadInventory);
    $('refreshReports').addEventListener('click', loadReports);
    $('modalCancel').addEventListener('click', () => $('modal').close());

    document.querySelectorAll('.tab').forEach(button => {
        button.addEventListener('click', () => switchTab(button.dataset.tab));
    });
});

async function login(event) {
    event.preventDefault();
    const data = new FormData(event.target);
    const result = await post('/api/login', data);
    if (!result.ok) {
        showToast(result.message, true);
        return;
    }
    currentUser = result;
    $('loginView').classList.add('hidden');
    $('appView').classList.remove('hidden');
    $('userBadge').textContent = `${result.fullName} (${result.role})`;
    await loadAll();
}

function logout() {
    currentUser = null;
    $('loginView').classList.remove('hidden');
    $('appView').classList.add('hidden');
    $('userBadge').textContent = 'Not logged in';
}

async function loadAll() {
    await Promise.all([
        loadEquipment(),
        loadCourses(),
        loadTechnicians(),
        loadReservations(),
        loadMaintenance(),
        loadInventory(),
        loadReports()
    ]);
}

function switchTab(tabId) {
    document.querySelectorAll('.tab').forEach(button => button.classList.toggle('active', button.dataset.tab === tabId));
    document.querySelectorAll('.tab-page').forEach(page => page.classList.toggle('active', page.id === tabId));
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
            <td>${escapeHtml(e.name)}</td>
            <td>${escapeHtml(e.category)}</td>
            <td>${escapeHtml(e.labCode)}</td>
            <td>${badge(e.status)}</td>
            <td>${escapeHtml(e.riskLevel)}</td>
            <td>${e.openTicketCount}</td>
            <td><button type="button" onclick="openReportProblem(${e.id})">Report</button></td>
        </tr>
    `);
    fillEquipmentSelect();
}

async function loadCourses() {
    if (!currentUser) return;
    courseCache = await get(`/api/courses?userId=${currentUser.id}`);
    const select = $('reservationCourse');
    select.innerHTML = courseCache.map(c => `<option value="${c.id}">${escapeHtml(c.code)} - ${escapeHtml(c.name)}</option>`).join('');
}

async function loadTechnicians() {
    technicianCache = await get('/api/users/technicians');
}

function fillEquipmentSelect() {
    const select = $('reservationEquipment');
    select.innerHTML = equipmentCache
        .map(e => `<option value="${e.id}">${escapeHtml(e.assetTag)} - ${escapeHtml(e.name)} (${escapeHtml(e.status)})</option>`)
        .join('');
}

async function createReservation(event) {
    event.preventDefault();
    const data = new FormData(event.target);
    data.append('userId', currentUser.id);
    const result = await post('/api/reservations/create', data);
    showToast(result.message, !result.ok);
    if (result.ok) {
        await loadReservations();
    }
}

async function loadReservations() {
    if (!currentUser) return;
    const rows = await get(`/api/reservations?userId=${currentUser.id}`);
    const canDecide = currentUser.role === 'ADMIN' || currentUser.role === 'TEACHER';
    fillRows('reservationRows', rows, r => `
        <tr>
            <td>${r.id}</td>
            <td>${escapeHtml(r.assetTag)}</td>
            <td>${escapeHtml(r.equipmentName)}</td>
            <td>${escapeHtml(r.requesterName)}</td>
            <td>${escapeHtml(r.startTime)}</td>
            <td>${escapeHtml(r.endTime)}</td>
            <td>${badge(r.status)}</td>
            <td>${escapeHtml(r.purpose)}</td>
            <td>${reservationActions(r, canDecide)}</td>
        </tr>
    `);
}

function reservationActions(row, canDecide) {
    const buttons = [];
    if (canDecide && row.status === 'PENDING') {
        buttons.push(`<button type="button" onclick="decideReservation(${row.id}, true)">Approve</button>`);
        buttons.push(`<button type="button" class="danger" onclick="decideReservation(${row.id}, false)">Reject</button>`);
    }
    if (row.status === 'PENDING' || row.status === 'APPROVED') {
        buttons.push(`<button type="button" class="secondary" onclick="cancelReservation(${row.id})">Cancel</button>`);
    }
    return buttons.join(' ');
}

async function decideReservation(id, approve) {
    const comment = prompt('Comment', approve ? 'Approved' : 'Rejected') || '';
    const data = new FormData();
    data.append('reservationId', id);
    data.append('userId', currentUser.id);
    data.append('approve', approve);
    data.append('comment', comment);
    const result = await post('/api/reservations/decide', data);
    showToast(result.message, !result.ok);
    await loadReservations();
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
    openModal('Report Equipment Problem', `
        <label>Title <input name="title" required></label>
        <label>Description <input name="description" required></label>
        <label>Priority
            <select name="priority">
                <option>LOW</option>
                <option>MEDIUM</option>
                <option>HIGH</option>
                <option>URGENT</option>
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
    const canUpdate = currentUser && (currentUser.role === 'ADMIN' || currentUser.role === 'TECHNICIAN');
    fillRows('maintenanceRows', rows, t => `
        <tr>
            <td>${t.id}</td>
            <td>${escapeHtml(t.assetTag)}</td>
            <td>${escapeHtml(t.equipmentName)}</td>
            <td>${escapeHtml(t.reporterName)}</td>
            <td>${escapeHtml(t.technicianName)}</td>
            <td>${escapeHtml(t.title)}</td>
            <td>${escapeHtml(t.priority)}</td>
            <td>${badge(t.status)}</td>
            <td>${escapeHtml(t.reportedAt)}</td>
            <td>${canUpdate ? `<button type="button" onclick="openUpdateTicket(${t.id})">Update</button>` : ''}</td>
        </tr>
    `);
}

function openUpdateTicket(ticketId) {
    const techOptions = technicianCache.map(t => `<option value="${t.id}">${escapeHtml(t.fullName)}</option>`).join('');
    openModal('Update Maintenance Ticket', `
        <label>Technician <select name="technicianId">${techOptions}</select></label>
        <label>Status
            <select name="status">
                <option>ASSIGNED</option>
                <option>IN_PROGRESS</option>
                <option>RESOLVED</option>
                <option>CLOSED</option>
            </select>
        </label>
        <label>Note <input name="note" placeholder="Short update note"></label>
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
    const canChange = currentUser && (currentUser.role === 'ADMIN' || currentUser.role === 'TECHNICIAN');
    fillRows('inventoryRows', rows, item => `
        <tr>
            <td>${item.id}</td>
            <td>${escapeHtml(item.labCode)}</td>
            <td>${escapeHtml(item.itemName)}</td>
            <td>${escapeHtml(item.unit)}</td>
            <td>${item.quantity}</td>
            <td>${item.reorderLevel}</td>
            <td>${item.lowStock ? badge('LOW') : 'No'}</td>
            <td>${canChange ? `<button type="button" onclick="openChangeStock(${item.id})">Change</button>` : ''}</td>
        </tr>
    `);
}

function openChangeStock(consumableId) {
    openModal('Change Stock', `
        <label>Change Amount <input name="amount" value="1" required></label>
        <label>Reason <input name="reason" placeholder="e.g. used in lab" required></label>
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
    return `<span class="status ${escapeHtml(String(text))}">${escapeHtml(String(text))}</span>`;
}

function showToast(message, isError = false) {
    const toast = $('toast');
    toast.textContent = message;
    toast.classList.toggle('error', isError);
    toast.classList.remove('hidden');
    setTimeout(() => toast.classList.add('hidden'), 2800);
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
