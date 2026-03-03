// API Base URL
const API_URL = 'http://localhost:8080/api';

// Elementos del DOM
const registerForm = document.getElementById('registerForm');
const usernameInput = document.getElementById('username');
const emailInput = document.getElementById('email');
const fullNameInput = document.getElementById('fullName');
const passwordInput = document.getElementById('password');
const confirmPasswordInput = document.getElementById('confirmPassword');
const registerBtn = document.getElementById('registerBtn');
const messageBox = document.getElementById('messageBox');

// Validación en tiempo real
let usernameAvailable = false;
let emailAvailable = false;
let passwordsMatch = false;

// Debounce para validaciones asíncronas
let usernameTimeout, emailTimeout;

/**
 * Valida disponibilidad de username en tiempo real
 */
usernameInput.addEventListener('input', function() {
    const username = this.value.trim();
    const validationSpan = document.getElementById('usernameValidation');
    
    // Reset
    validationSpan.textContent = '';
    validationSpan.className = 'validation-message';
    this.classList.remove('valid', 'invalid');
    
    if (username.length < 4) {
        return;
    }
    
    // Debounce para no hacer muchas peticiones
    clearTimeout(usernameTimeout);
    usernameTimeout = setTimeout(() => {
        checkUsernameAvailability(username);
    }, 500);
});

/**
 * Valida disponibilidad de email en tiempo real
 */
emailInput.addEventListener('input', function() {
    const email = this.value.trim();
    const validationSpan = document.getElementById('emailValidation');
    
    // Reset
    validationSpan.textContent = '';
    validationSpan.className = 'validation-message';
    this.classList.remove('valid', 'invalid');
    
    if (!isValidEmail(email)) {
        return;
    }
    
    // Debounce
    clearTimeout(emailTimeout);
    emailTimeout = setTimeout(() => {
        checkEmailAvailability(email);
    }, 500);
});

/**
 * Valida que las contraseñas coincidan
 */
confirmPasswordInput.addEventListener('input', function() {
    validatePasswordMatch();
});

passwordInput.addEventListener('input', function() {
    if (confirmPasswordInput.value) {
        validatePasswordMatch();
    }
});

/**
 * Verifica disponibilidad de username
 */
async function checkUsernameAvailability(username) {
    const validationSpan = document.getElementById('usernameValidation');
    
    try {
        const response = await fetch(`${API_URL}/auth/check-username?username=${encodeURIComponent(username)}`);
        const data = await response.json();
        
        if (data.available) {
            usernameInput.classList.add('valid');
            usernameInput.classList.remove('invalid');
            validationSpan.textContent = '✓ Username disponible';
            validationSpan.className = 'validation-message success';
            usernameAvailable = true;
        } else {
            usernameInput.classList.add('invalid');
            usernameInput.classList.remove('valid');
            validationSpan.textContent = '✗ Este username ya está en uso';
            validationSpan.className = 'validation-message error';
            usernameAvailable = false;
        }
    } catch (error) {
        console.error('Error checking username:', error);
    }
}

/**
 * Verifica disponibilidad de email
 */
async function checkEmailAvailability(email) {
    const validationSpan = document.getElementById('emailValidation');
    
    try {
        const response = await fetch(`${API_URL}/auth/check-email?email=${encodeURIComponent(email)}`);
        const data = await response.json();
        
        if (data.available) {
            emailInput.classList.add('valid');
            emailInput.classList.remove('invalid');
            validationSpan.textContent = '✓ Email disponible';
            validationSpan.className = 'validation-message success';
            emailAvailable = true;
        } else {
            emailInput.classList.add('invalid');
            emailInput.classList.remove('valid');
            validationSpan.textContent = '✗ Este email ya está registrado';
            validationSpan.className = 'validation-message error';
            emailAvailable = false;
        }
    } catch (error) {
        console.error('Error checking email:', error);
    }
}

/**
 * Valida que las contraseñas coincidan
 */
function validatePasswordMatch() {
    const password = passwordInput.value;
    const confirmPassword = confirmPasswordInput.value;
    const validationSpan = document.getElementById('passwordMatch');
    
    if (confirmPassword === '') {
        validationSpan.textContent = '';
        confirmPasswordInput.classList.remove('valid', 'invalid');
        passwordsMatch = false;
        return;
    }
    
    if (password === confirmPassword) {
        confirmPasswordInput.classList.add('valid');
        confirmPasswordInput.classList.remove('invalid');
        validationSpan.textContent = '✓ Las contraseñas coinciden';
        validationSpan.className = 'validation-message success';
        passwordsMatch = true;
    } else {
        confirmPasswordInput.classList.add('invalid');
        confirmPasswordInput.classList.remove('valid');
        validationSpan.textContent = '✗ Las contraseñas no coinciden';
        validationSpan.className = 'validation-message error';
        passwordsMatch = false;
    }
}

/**
 * Valida formato de email
 */
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

/**
 * Maneja el envío del formulario de registro
 */
registerForm.addEventListener('submit', async function(e) {
    e.preventDefault();
    
    // Validaciones finales
    if (!usernameAvailable) {
        showMessage('El username no está disponible', 'error');
        return;
    }
    
    if (!emailAvailable) {
        showMessage('El email no está disponible', 'error');
        return;
    }
    
    if (!passwordsMatch) {
        showMessage('Las contraseñas no coinciden', 'error');
        return;
    }
    
    const formData = {
        username: usernameInput.value.trim(),
        email: emailInput.value.trim(),
        fullName: fullNameInput.value.trim(),
        password: passwordInput.value
    };
    
    // Deshabilita botón y muestra loading
    registerBtn.disabled = true;
    registerBtn.classList.add('loading');
    registerBtn.textContent = 'Creando cuenta...';
    
    try {
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        });
        
        const data = await response.json();
        
        if (response.ok) {
            showMessage('¡Cuenta creada exitosamente! Redirigiendo al login...', 'success');
            
            // Limpia formulario
            registerForm.reset();
            
            // Redirige al login después de 2 segundos
            setTimeout(() => {
                window.location.href = '/';
            }, 2000);
        } else {
            showMessage(data.message || 'Error al crear la cuenta', 'error');
            registerBtn.disabled = false;
            registerBtn.classList.remove('loading');
            registerBtn.textContent = 'Crear Cuenta';
        }
    } catch (error) {
        console.error('Error:', error);
        showMessage('Error de conexión. Intente nuevamente.', 'error');
        registerBtn.disabled = false;
        registerBtn.classList.remove('loading');
        registerBtn.textContent = 'Crear Cuenta';
    }
});

/**
 * Muestra mensajes al usuario
 */
function showMessage(message, type) {
    messageBox.textContent = message;
    messageBox.className = `message-box ${type}`;
    messageBox.style.display = 'block';
    
    // Scroll suave hacia el mensaje
    messageBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    
    // Auto-oculta mensajes de error después de 5 segundos
    if (type === 'error') {
        setTimeout(() => {
            messageBox.style.display = 'none';
        }, 5000);
    }
}
