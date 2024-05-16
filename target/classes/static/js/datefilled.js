document.addEventListener("DOMContentLoaded", function() {
    var birthDateInput = document.getElementById("birthDate");
    birthDateInput.addEventListener("change", function() {
        if (this.value) {
            this.classList.add("filled");
        } else {
            this.classList.remove("filled");
        }
    });
});
