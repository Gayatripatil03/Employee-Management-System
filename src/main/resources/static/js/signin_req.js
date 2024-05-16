document.addEventListener("DOMContentLoaded", function() {

    function hideNotification(element) {
        setTimeout(function() {
            element.style.display = 'none';
        }, 2000);
    }

    var errorAlert = document.querySelector('.alert.alert-danger');
    var successAlert = document.querySelector('.alert.alert-success');
    var infoAlert = document.querySelector('.alert.alert-info');
    var invalidAlert = document.querySelector('.alert.alert-invalid');

    if (errorAlert) {
        hideNotification(errorAlert);
    }

    if (successAlert) {
        hideNotification(successAlert);
    }

    if (infoAlert) {
        hideNotification(infoAlert);
    }

    if (invalidAlert) {
        hideNotification(invalidAlert);
    }
});



