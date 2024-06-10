document.addEventListener("DOMContentLoaded", function() {
    var profileToggle = document.getElementById("profile-toggle");
    var userDropdown = document.getElementById("userDropdown");
    var profileImageDropdown = document.getElementById("userLogoDropdown");
    var popupContainer = document.getElementById("popupContainer");
    var addPictureButton = document.getElementById("addPictureButton");
    var closeButton = document.getElementById("closeButton");
    var cameraIcon = document.getElementById("camera-icon");
    var saveButton = document.getElementById("saveButton");
    var profileImage = document.getElementById("selectedProfileImage");
    var fileInput = document.getElementById("fileInput");
    var selectedFileName = document.getElementById("selectedFileName");
    var containerDisplayed = false;
    var sessionAlertBox = document.getElementById("sessionAlertBox");
    var sessionMessage = document.getElementById("sessionMessage");
    var sessionAlertButton = document.getElementById("sessionAlertButton");

    sessionAlertBox.style.display = "none";

    profileToggle.addEventListener("click", function(event) {
        event.preventDefault();
        if (userDropdown.style.display === "block") {
            userDropdown.style.display = "none";
        } else {
            userDropdown.style.display = "block";
        }
    });

    profileImageDropdown.addEventListener("click", function(event) {
        event.stopPropagation();
        if (!containerDisplayed) {
            popupContainer.style.display = "block";
            containerDisplayed = true;
        }
    });

    cameraIcon.addEventListener("click", function(event) {
        userDropdown.style.display = "none";
        event.stopPropagation();
        if (!containerDisplayed) {
            popupContainer.style.display = "block";
            containerDisplayed = true;
        }
    });

    addPictureButton.addEventListener("click", function() {
        fileInput.click();
    });

    fileInput.addEventListener("change", function(event) {
        var input = event.target;
        var reader = new FileReader();
        var image = document.getElementById('selectedProfileImage');

        reader.onload = function() {
            image.src = reader.result;
        };

        reader.readAsDataURL(input.files[0]);

        var fileName = input.files[0].name;
        selectedFileName.textContent = fileName;

        saveButton.style.display = "block";
    });

    saveButton.addEventListener("click", function() {
        var file = fileInput.files[0];
        if (file) {
            if (file.size > 1 * 1024 * 1024) {
                var errorMessage = "File size limit exceeded. Please upload a file up to 1MB.";
                showAlert(errorMessage);
                return;
            }

            var formData = new FormData();
            formData.append("file", file);

            fetch("/admin/uploadProfileImage", {
                    method: "POST",
                    body: formData
                })
                .then(response => response.text())
                .then(message => {
                    if (message.includes("File size limit exceeded")) {
                        showAlert(message);
                    } else {
                        window.location.reload();
                    }
                })
                .catch(error => console.error("Error:", error));
        }
    });

    function showAlert(message) {
        if (!message) {
            sessionAlertBox.style.display = "none";
            return;
        } else {
            sessionMessage.textContent = message;
            sessionAlertBox.style.display = "block";
        }
    }

    closeButton.addEventListener("click", function() {
        popupContainer.style.display = "none";
        containerDisplayed = false;
        window.location.reload();
    });

    sessionAlertButton.addEventListener("click", function() {
        sessionAlertBox.style.display = "none";
        window.location.reload();
    });

    document.addEventListener("click", function(event) {
        if (!popupContainer.contains(event.target) && event.target !== profileImageDropdown && event.target !== cameraIcon) {
            popupContainer.style.display = "none";
            containerDisplayed = false;
        }
    });
});

