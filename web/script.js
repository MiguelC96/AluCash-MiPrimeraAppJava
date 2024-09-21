function showRegisterForm() {
    document.getElementById('loginForm').classList.add('hidden');
    document.getElementById('registerForm').classList.remove('hidden');
}

function showLoginForm() {
    document.getElementById('registerForm').classList.add('hidden');
    document.getElementById('loginForm').classList.remove('hidden');
}

//Funcion para login
function login() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    fetch('/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
    })
    .then(response => {
        if (response.ok) {
            // Si la respuesta HTTP es 2xx
            return response.json();  // Parseamos la respuesta como JSON
        } else {
            throw new Error('Error en la autenticación');
        }
    })
    .then(data => {
        if (data.message) {
            // Login exitoso, redirigimos al menú
            localStorage.setItem('username', username);  // Guardamos el usuario en el localStorage
            window.location.href = "menu.html";  // Redirigimos a la página del menú
        } else if (data.error) {
            // Mostrar el error si la autenticación falla
            document.getElementById('loginResponse').innerText = `Error: ${data.error}`;
            document.getElementById('loginResponse').classList.remove('hidden');
        }
    })
    .catch(error => {
        console.error('Error en el login:', error);
        document.getElementById('loginResponse').innerText = 'Error en el login. Intenta nuevamente.';
        document.getElementById('loginResponse').classList.remove('hidden');
    });
}

//Funcion para registro
function register() {
    // Obtener los valores de los campos del formulario
    const newUsername = document.getElementById('newUsername').value;
    const accountType = document.getElementById('accountType').value;
    const initialBalance = document.getElementById('initialBalance').value;
    const newPassword = document.getElementById('newPassword').value;

    // Enviar la solicitud de registro al servidor
    fetch('/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `username=${encodeURIComponent(newUsername)}&accountType=${encodeURIComponent(accountType)}&saldo=${encodeURIComponent(initialBalance)}&password=${encodeURIComponent(newPassword)}`
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Error en la respuesta del servidor');
        }
        return response.json();  // Parsear la respuesta como JSON
    })
    .then(data => {
        // Mostrar el mensaje de la respuesta
        const responseElement = document.getElementById('registerResponse');
        responseElement.innerText = data.message || data.error;
        responseElement.classList.remove('hidden');

        // Redirigir al formulario de login si el registro fue exitoso
        if (data.status === 'success') {
            setTimeout(showLoginForm, 2000); // Redirigir después de 2 segundos
        }
    })
    .catch(error => {
        console.error('Error:', error);
        // Mostrar mensaje de error
        const responseElement = document.getElementById('registerResponse');
        responseElement.innerText = 'Ocurrió un error al registrarse.';
        responseElement.classList.remove('hidden');
    });
}

// Función para mostrar el formulario de inicio de sesión y ocultar el de registro
function showLoginForm() {
    const registerForm = document.getElementById('registerForm');
    const loginForm = document.getElementById('loginForm');

    if (registerForm) {
        registerForm.classList.add('hidden');
    } else {
        console.error('Element with ID "registerForm" not found');
    }

    if (loginForm) {
        loginForm.classList.remove('hidden');
    } else {
        console.error('Element with ID "loginForm" not found');
    }
}

// Funciones para el menú del cliente
function checkBalance() {
    const username = localStorage.getItem('username'); // Usamos el nombre de usuario guardado

    fetch('/balance', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `username=${encodeURIComponent(username)}`
    })
    .then(response => response.text()) // Obtén la respuesta como texto
    .then(data => {
        try {
            // Intenta parsear la respuesta como JSON
            const jsonData = JSON.parse(data); 
            if (jsonData.saldo) {
                document.getElementById('clientResponse').innerText = `Tu saldo es: $${jsonData.saldo}`;
            } else if (jsonData.error) {
                document.getElementById('clientResponse').innerText = `Error: ${jsonData.error}`;
            }
            document.getElementById('clientResponse').classList.remove('hidden');
        } catch (e) {
            // Si ocurre un error al parsear, muestra la respuesta original
            console.error('Error al parsear el JSON:', e);
            document.getElementById('clientResponse').innerText = `Respuesta inesperada: ${data}`;
            document.getElementById('clientResponse').classList.remove('hidden');
        }
    })
    .catch(error => {
        console.error('Error al consultar el saldo:', error);
        document.getElementById('clientResponse').innerText = 'Error al consultar el saldo. Intenta nuevamente.';
        document.getElementById('clientResponse').classList.remove('hidden');
    });
}

//Funcion deposito
function deposit() {
    const username = localStorage.getItem('username');
    const amount = document.getElementById('depositAmount').value;

    fetch('/deposit', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `username=${encodeURIComponent(username)}&amount=${encodeURIComponent(amount)}`
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Error en el depósito: ' + response.statusText);
        }
        return response.json();
    })
    .then(data => {
        document.getElementById('clientResponse').innerText = data.message || 'Saldo actualizado: ' + data.saldo;
        document.getElementById('clientResponse').classList.remove('hidden');
    })
    .catch(error => {
        console.error(error);
        document.getElementById('clientResponse').innerText = error.message;
        document.getElementById('clientResponse').classList.remove('hidden');
    });
}

// Funcion Retirar
function withdraw() {
    const withdrawAmount = document.getElementById('withdrawAmount').value;

    // Obtener el nombre del usuario logueado
    const currentUsername = localStorage.getItem('username');

    if (!withdrawAmount || withdrawAmount <= 0) {
        alert('Por favor, ingresa una cantidad válida para retirar.');
        return;
    }

    // Enviar la solicitud de retiro al servidor
    fetch('/withdraw', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `username=${encodeURIComponent(currentUsername)}&amount=${encodeURIComponent(withdrawAmount)}`
    })
    .then(response => response.json())
    .then(data => {
        const responseElement = document.getElementById('withdrawResponse');
        if (data.error) {
            responseElement.innerText = `Error: ${data.error}`;
        } else {
            responseElement.innerText = `Éxito: ${data.message}`;
        }
        responseElement.classList.remove('hidden');
    })
    .catch(error => {
        console.error('Error en la solicitud:', error);
        document.getElementById('withdrawResponse').innerText = 'Ocurrió un error al intentar realizar el retiro.';
        document.getElementById('withdrawResponse').classList.remove('hidden');
    });
}

//Funcion Transferir
function transfer() {
    const recipientUsername = document.getElementById('recipientUsername').value;
    const transferAmount = document.getElementById('transferAmount').value;

    // Obtener el nombre del usuario logueado
    const currentUsername = localStorage.getItem('username');

    if (!recipientUsername || !transferAmount || transferAmount <= 0) {
        alert('Por favor, completa todos los campos con datos válidos.');
        return;
    }

    // Enviar la solicitud de transferencia al servidor
    fetch('/transfer', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `fromUsername=${encodeURIComponent(currentUsername)}&toUsername=${encodeURIComponent(recipientUsername)}&amount=${encodeURIComponent(transferAmount)}`
    })
    .then(response => response.json())
    .then(data => {
        const responseElement = document.getElementById('transferResponse');
        if (data.error) {
            responseElement.innerText = `Error: ${data.error}`;
        } else {
            responseElement.innerText = `Éxito: ${data.message}`;
        }
        responseElement.classList.remove('hidden');
    })
    .catch(error => {
        console.error('Error en la solicitud:', error);
        document.getElementById('transferResponse').innerText = 'Ocurrió un error al intentar realizar la transferencia.';
        document.getElementById('transferResponse').classList.remove('hidden');
    });
}

function logout() {
    // Enviar la solicitud de cierre de sesión al servidor
    fetch('/logout', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Error en la respuesta del servidor');
        }
        return response.json();  // Parsear la respuesta como JSON
    })
    .then(data => {
        const responseElement = document.getElementById('logoutResponse');
        responseElement.innerText = data.message || data.error;
        responseElement.classList.remove('hidden');

        // Redirigir al formulario de login si el cierre de sesión fue exitoso
        if (data.status === 'success') {
            setTimeout(() => {
                showLoginForm(); // Mostrar el formulario de login
                window.location.href = 'index.html'; // Redirigir a la página de inicio
            }, 2000); // Redirigir después de 2 segundos
        }
    })
    .catch(error => {
        console.error('Error:', error);
        // Mostrar mensaje de error
        const responseElement = document.getElementById('logoutResponse');
        responseElement.innerText = 'Ocurrió un error al cerrar sesión.';
        responseElement.classList.remove('hidden');
    });
}

