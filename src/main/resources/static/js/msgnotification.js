window.onload = function() {

    var errorMessageDisplayed = false;


    if (!errorMessageDisplayed) {

        var errorMessage = document.querySelector('.error-text');
        if (errorMessage) {
            errorMessage.style.display = 'block';
            errorMessageDisplayed = true;

            setTimeout(function() {
                errorMessage.style.display = 'none';
            }, 10000);
        }
    }
};
