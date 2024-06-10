window.onload = function() {
    var urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('invalid')) {
        document.getElementById('invalidAlert').style.display = 'block';
    }
     if (urlParams.has('logout')) {
        document.getElementById('logoutAlert').style.display = 'block';
    }
    if (urlParams.has('emailChanged')) {
        document.getElementById('emailChangedAlert').style.display = 'block';
    }
    if (urlParams.has('error')) {
        document.getElementById('errorAlert').style.display = 'block';
    }
};

function submitForm() {
    var usernameInput = document.getElementsByName('username')[0];
    var passwordInput = document.getElementsByName('password')[0];
    var formIsValid = true;

    if (usernameInput.value.trim() === '') {
        formIsValid = false;
        document.getElementById('alertMessage').textContent = "Please fill in all required fields.";
        document.getElementById('customAlert').style.display = 'block';
    } else if (passwordInput.value.trim() === '') {
        formIsValid = false;
        document.getElementById('alertMessage').textContent = "Please fill in all required fields.";
        document.getElementById('customAlert').style.display = 'block';
    } else if (!isValidPassword(passwordInput.value)) {
        formIsValid = false;
        document.getElementById('alertMessage').textContent = "Password must contain at least one number and one uppercase and lowercase letter, and be at least 8 characters long.";
        document.getElementById('customAlert').style.display = 'block';
    }

    if (formIsValid) {
        var errorAlert = document.querySelector('.alert.alert-invalid');
        if (errorAlert && errorAlert.style.display !== 'none') {
            console.log("Invalid error exists");
            var errorMessage = errorAlert.textContent.trim(); 
            alert(errorMessage); 
            errorAlert.querySelector('.closebtn').addEventListener('click', function() {
                errorAlert.style.display = 'none';
            });
        } else {
            document.getElementById('loginForm').submit();
        }
    }
}

function isValidPassword(password) {
    var passwordRegex = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$/;
    return passwordRegex.test(password);
}


function hideCustomAlert() {
    document.getElementById('customAlert').style.display = 'none';
    window.location.href = '/signin';
}

function hideInvalidAlert() {
    document.getElementById('invalidAlert').style.display = 'none';
    window.location.href = '/signin';
}

function hideLogoutAlert() {
    document.getElementById('logoutAlert').style.display = 'none';
    window.location.href = '/signin';
}
function hideEmailChangedAlert() {
    document.getElementById('emailChangedAlert').style.display = 'none';
    window.location.href = '/signin';
}
function hideErrorAlert() {
    document.getElementById('errorAlert').style.display = 'none';
    window.location.href = '/signin';
}

window.addEventListener('load', function() {
    var urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('invalid')) {
        document.getElementById('invalidAlert').style.display = 'block';
    }
    if (urlParams.has('logout')) {
        document.getElementById('logoutAlert').style.display = 'block';
    }
    if (urlParams.has('emailChanged')) {
        document.getElementById('emailChangedAlert').style.display = 'block';
    }
    if (urlParams.has('error')) {
        document.getElementById('errorAlert').style.display = 'block';
    }
});


