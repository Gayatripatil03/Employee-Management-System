document.getElementById("profileImageForm").addEventListener("submit", function(event) {
    event.preventDefault();
    var profileImageInput = document.getElementById("profileImageInput");
    var file = profileImageInput.files[0];
    var formData = new FormData();
    formData.append("file", file);
    formData.append("userId", "123");
    fetch("/uploadProfileImage", {
        method: "POST",
        body: formData
    }).then(response => {
        if (response.ok) {
            console.log("Profile image uploaded successfully");
        } else {
            console.error("Error uploading profile image");
        }
    }).catch(error => {
        console.error("Error uploading profile image:", error);
    });
});
