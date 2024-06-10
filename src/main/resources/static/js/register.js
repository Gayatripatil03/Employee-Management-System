function Register() {
    var valid = true;
    var alertMessage = "";

    const fullName = document.querySelector('input[name="fullName"]');
    if (!/^[a-zA-Z ]+$/.test(fullName.value)) {
        alertMessage += "Full Name must only contain alphabetic characters.\n";
        valid = false;
    }

    const email = document.querySelector('input[name="email"]');
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
        alertMessage += "Invalid email format.\n";
        valid = false;
    }

    const phone = document.querySelector('input[name="phone"]');
    if (!/^\d{10}$/.test(phone.value)) {
        alertMessage += "Phone number must be 10 digits.\n";
        valid = false;
    }

    const birthDate = document.querySelector('input[name="birthDate"]');
    const currentDate = new Date().toISOString().split('T')[0];
    if (birthDate.value >= currentDate) {
        alertMessage += "Birth Date must be in the past.\n";
        valid = false;
    }

    const address = document.querySelector('input[name="address"]');
    if (address.value.trim() === "") {
        alertMessage += "Address cannot be empty.\n";
        valid = false;
    }

    const password = document.querySelector('input[name="password"]');
    const passwordPattern = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$/;
    if (!passwordPattern.test(password.value)) {
        alertMessage += "Password must contain at least one number, one uppercase and lowercase letter, and at least 8 characters.\n";
        valid = false;
    }

    const role = document.querySelector('input[name="role"]');
    if (!["ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN"].includes(role.value)) {
        alertMessage += "Role must be ROLE_USER, ROLE_MANAGER, or ROLE_ADMIN.\n";
        valid = false;
    }

    if (fullName.value.trim() === "" || email.value.trim() === "" || phone.value.trim() === "" || birthDate.value.trim() === "" || address.value.trim() === "" || password.value.trim() === "" || role.value.trim() === "") {
        alertMessage = "Please fill in all fields.";
        valid = false;
    }

    if (!valid) {    
        showAlert(alertMessage);
        return false;
    }else{
    	   document.getElementById('registerForm').submit();       
    }
    

}

function showAlert(message) {
    const alertBox = document.getElementById('registercustomAlert');
    const alertMessage = document.getElementById('registeralertMessage');
    alertMessage.textContent = message;
    alertBox.style.display = 'block';
}

function closeAlert() {
    const alertBox = document.getElementById('registercustomAlert');
    alertBox.style.display = 'none';
}

 






