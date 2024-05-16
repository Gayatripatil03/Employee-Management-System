document.addEventListener("DOMContentLoaded", function() {
    var profileToggle = document.getElementById("profile-toggle");
    var userDropdown = document.getElementById("userDropdown");
    var employeeListLink = document.getElementById("employeeListLink");

    userDropdown.style.display = "none";

    profileToggle.addEventListener("click", function(event) {
        event.preventDefault();
        if (userDropdown.style.display === "block") {
            userDropdown.style.display = "none";
        } else {
            userDropdown.style.display = "block";
        }
    });

    document.addEventListener("click", function(event) {
        if (!userDropdown.contains(event.target) && !profileToggle.contains(event.target)) {
            userDropdown.style.display = "none";
        }
    });
    
    employeeListLink.addEventListener("click", function() {

        employeeListLink.classList.add("clicked-link");
    });
});
