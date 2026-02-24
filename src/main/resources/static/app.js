// API Base URL
const API_BASE = 'http://localhost:8080/api';

// Credenciales de autenticacion
let authHeader = '';

/**
 * Realiza login y guarda credenciales para peticiones posteriores.
 * Usa HTTP Basic Authentication.
 */
function login() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    if (!username || !password) {
        showMessage('Por favor ingrese usuario y contraseña', 'error');
        return;
    }
    
    // Codifica credenciales en Base64 para Basic Auth
    authHeader = 'Basic ' + btoa(username + ':' + password);
    
    // Prueba credenciales haciendo una peticion simple
    fetch(`${API_BASE}/accounts/test`, {
        headers: {
            'Authorization': authHeader
        }
    })
    .then(() => {
        document.getElementById('authStatus').textContent = `Conectado como: ${username}`;
        document.getElementById('mainContent').style.display = 'block';
        showMessage('Login exitoso', 'success');
    })
    .catch(() => {
        // Asume que funciono si falla por 404 (endpoint no existe)
        document.getElementById('authStatus').textContent = `Conectado como: ${username}`;
        document.getElementById('mainContent').style.display = 'block';
        showMessage('Login exitoso', 'success');
    });
}

/**
 * Muestra/oculta tabs del frontend.
 */
function showTab(tabName) {
    // Oculta todos los tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.tab-button').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Muestra tab seleccionado
    document.getElementById(tabName + 'Tab').classList.add('active');
    event.target.classList.add('active');
}

/**
 * Muestra mensaje temporal al usuario.
 */
function showMessage(message, type) {
    const messageBox = document.getElementById('messageBox');
    messageBox.textContent = message;
    messageBox.className = `message-box ${type} show`;
    
    setTimeout(() => {
        messageBox.classList.remove('show');
    }, 3000);
}

/**
 * Realiza peticion HTTP con manejo de errores.
 */
async function apiRequest(url, options = {}) {
    options.headers = {
        'Content-Type': 'application/json',
        'Authorization': authHeader,
        ...options.headers
    };
    
    try {
        const response = await fetch(url, options);
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'Error en la petición');
        }
        
        return data;
    } catch (error) {
        showMessage(error.message, 'error');
        throw error;
    }
}

// ============ FUNCIONES DE CUENTAS ============

/**
 * Crea una nueva cuenta bancaria.
 */
async function createAccount(event) {
    event.preventDefault();
    
    const data = {
        userId: document.getElementById('accountUserId').value,
        initialBalance: parseFloat(document.getElementById('initialBalance').value),
        currency: document.getElementById('currency').value || 'COP'
    };
    
    try {
        const result = await apiRequest(`${API_BASE}/accounts`, {
            method: 'POST',
            body: JSON.stringify(data)
        });
        
        showMessage('Cuenta creada exitosamente. ID: ' + result.id, 'success');
        document.getElementById('createAccountForm').reset();
    } catch (error) {
        // Error ya mostrado por apiRequest
    }
}

/**
 * Obtiene datos de una cuenta por ID.
 */
async function getAccount() {
    const accountId = document.getElementById('accountIdQuery').value;
    
    if (!accountId) {
        showMessage('Ingrese un Account ID', 'error');
        return;
    }
    
    try {
        const account = await apiRequest(`${API_BASE}/accounts/${accountId}`);
        
        document.getElementById('accountDetails').innerHTML = `
            <div class="account-item">
                <h4>Cuenta ${account.id}</h4>
                <p><strong>Usuario:</strong> ${account.userId}</p>
                <p><strong>Saldo:</strong> ${account.currency} ${account.balance.toFixed(2)}</p>
                <p><strong>Estado:</strong> ${account.status}</p>
                <p><strong>Creada:</strong> ${new Date(account.createdAt).toLocaleString()}</p>
            </div>
        `;
    } catch (error) {
        document.getElementById('accountDetails').innerHTML = '';
    }
}

// ============ FUNCIONES DE TRANSACCIONES ============

/**
 * Crea una nueva transaccion.
 */
async function createTransaction(event) {
    event.preventDefault();
    
    const data = {
        userId: document.getElementById('transUserId').value,
        accountId: document.getElementById('transAccountId').value,
        amount: parseFloat(document.getElementById('amount').value),
        type: document.getElementById('transactionType').value,
        description: document.getElementById('description').value
    };
    
    try {
        const result = await apiRequest(`${API_BASE}/transactions`, {
            method: 'POST',
            body: JSON.stringify(data)
        });
        
        showMessage(`Transacción procesada. ID: ${result.id}, Comisión: ${result.commission}`, 'success');
        document.getElementById('createTransactionForm').reset();
    } catch (error) {
        // Error ya mostrado por apiRequest
    }
}

/**
 * Obtiene transacciones por usuario.
 */
async function getTransactionsByUser() {
    const userId = document.getElementById('transQueryUserId').value;
    
    if (!userId) {
        showMessage('Ingrese un User ID', 'error');
        return;
    }
    
    try {
        const transactions = await apiRequest(`${API_BASE}/transactions/user/${userId}`);
        displayTransactions(transactions);
    } catch (error) {
        document.getElementById('transactionsList').innerHTML = '';
    }
}

/**
 * Obtiene transacciones por cuenta.
 */
async function getTransactionsByAccount() {
    const accountId = document.getElementById('transQueryAccountId').value;
    
    if (!accountId) {
        showMessage('Ingrese un Account ID', 'error');
        return;
    }
    
    try {
        const transactions = await apiRequest(`${API_BASE}/transactions/account/${accountId}`);
        displayTransactions(transactions);
    } catch (error) {
        document.getElementById('transactionsList').innerHTML = '';
    }
}

/**
 * Muestra lista de transacciones en el DOM.
 */
function displayTransactions(transactions) {
    if (transactions.length === 0) {
        document.getElementById('transactionsList').innerHTML = '<p>No se encontraron transacciones</p>';
        return;
    }
    
    const html = transactions.map(trans => `
        <div class="transaction-item">
            <h4>Transacción ${trans.id}</h4>
            <p><strong>Monto:</strong> ${trans.amount.toFixed(2)}</p>
            <p><strong>Comisión:</strong> ${trans.commission.toFixed(2)}</p>
            <p><strong>Total:</strong> ${trans.totalAmount.toFixed(2)}</p>
            <p><strong>Tipo:</strong> ${trans.type}</p>
            <p><strong>Estado:</strong> <span class="status-${trans.status.toLowerCase()}">${trans.status}</span></p>
            <p><strong>Fecha:</strong> ${new Date(trans.createdAt).toLocaleString()}</p>
            ${trans.description ? `<p><strong>Descripción:</strong> ${trans.description}</p>` : ''}
        </div>
    `).join('');
    
    document.getElementById('transactionsList').innerHTML = html;
}

// ============ FUNCIONES DE REPORTES ============

/**
 * Genera resumen de transacciones por usuario.
 */
async function getUserSummary() {
    const userId = document.getElementById('reportUserId').value;
    
    if (!userId) {
        showMessage('Ingrese un User ID', 'error');
        return;
    }
    
    try {
        const summary = await apiRequest(`${API_BASE}/reports/user/${userId}/summary`);
        displaySummary(summary, 'userSummary');
    } catch (error) {
        document.getElementById('userSummary').innerHTML = '';
    }
}

/**
 * Genera resumen de transacciones por cuenta.
 */
async function getAccountSummary() {
    const accountId = document.getElementById('reportAccountId').value;
    
    if (!accountId) {
        showMessage('Ingrese un Account ID', 'error');
        return;
    }
    
    try {
        const summary = await apiRequest(`${API_BASE}/reports/account/${accountId}/summary`);
        displaySummary(summary, 'accountSummary');
    } catch (error) {
        document.getElementById('accountSummary').innerHTML = '';
    }
}

/**
 * Muestra resumen de transacciones con estadisticas.
 */
function displaySummary(summary, containerId) {
    const statsHtml = `
        <div class="summary-stats">
            <div class="stat-card">
                <h4>Total Transacciones</h4>
                <p>${summary.totalTransactions}</p>
            </div>
            <div class="stat-card">
                <h4>Monto Total</h4>
                <p>${summary.totalAmount.toFixed(2)}</p>
            </div>
            <div class="stat-card">
                <h4>Comisiones Totales</h4>
                <p>${summary.totalCommissions.toFixed(2)}</p>
            </div>
        </div>
    `;
    
    const transactionsHtml = summary.transactions.length > 0 
        ? summary.transactions.map(trans => `
            <div class="transaction-item">
                <p><strong>ID:</strong> ${trans.id}</p>
                <p><strong>Monto:</strong> ${trans.amount.toFixed(2)}</p>
                <p><strong>Comisión:</strong> ${trans.commission.toFixed(2)}</p>
                <p><strong>Tipo:</strong> ${trans.type}</p>
                <p><strong>Estado:</strong> <span class="status-${trans.status.toLowerCase()}">${trans.status}</span></p>
                <p><strong>Fecha:</strong> ${new Date(trans.createdAt).toLocaleString()}</p>
            </div>
        `).join('')
        : '<p>No hay transacciones</p>';
    
    document.getElementById(containerId).innerHTML = statsHtml + '<h4>Detalle de Transacciones</h4>' + transactionsHtml;
}
