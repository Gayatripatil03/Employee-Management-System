window.onload = function() {
    var errorMessage = document.querySelector('.error-text');
    var successMessage = document.querySelector('.success-text');

    if (errorMessage || successMessage) {
        var alertBox = document.createElement('div');
        alertBox.className = 'alert-box';
        alertBox.innerHTML = '<p>' + (errorMessage ? errorMessage.textContent.trim() : successMessage.textContent.trim()) + '</p>' + '<button class="alert-button">OK</button>';
        document.body.appendChild(alertBox);

        var alertButton = document.querySelector('.alert-button');
        alertButton.onclick = function() {
            alertBox.style.display = 'none';
        };
    }
};
