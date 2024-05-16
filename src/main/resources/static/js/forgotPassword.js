document.addEventListener("DOMContentLoaded", function() {
    const errorMessageElements = document.querySelectorAll('.err');
    const errorMessageKey = 'errorMessageDisplayed';

    if (!sessionStorage.getItem(errorMessageKey)) {
        errorMessageElements.forEach(errorMessageElement => {
            errorMessageElement.style.display = 'block';
            
            sessionStorage.setItem(errorMessageKey, 'true');
        });

        setTimeout(() => {
            errorMessageElements.forEach(errorMessageElement => {
                errorMessageElement.style.display = 'none';
            });
            sessionStorage.removeItem(errorMessageKey);
        }, 2000);
    } else {
        errorMessageElements.forEach(errorMessageElement => {
            errorMessageElement.style.display = 'none';
        });
    }
});
