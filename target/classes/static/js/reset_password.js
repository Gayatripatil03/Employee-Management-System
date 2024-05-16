window.onload = function() {

    var errorMessageDisplayed = false;

    if (!errorMessageDisplayed) {

        var errorMessage = document.querySelector('.error-text3');
        if (errorMessage) {
            errorMessage.style.display = 'block';
            errorMessageDisplayed = true;


            setTimeout(function() {
                errorMessage.style.display = 'none';
            }, 2000);
        }
    }
};
