
document.addEventListener("DOMContentLoaded", function() {

    var dropdownButton = document.getElementById("navbarDropdown");
    var dropdownMenu = document.querySelector(".dropdown-menu");


    dropdownButton.addEventListener("click", function(event) {
        event.stopPropagation();
        dropdownMenu.classList.toggle("show");
    });


    window.addEventListener("click", function(event) {
        if (!event.target.matches('.profile-user')) {
            dropdownMenu.classList.remove('show');
        }
    });
});
