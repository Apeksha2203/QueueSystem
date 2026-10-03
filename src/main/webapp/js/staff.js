document.addEventListener("DOMContentLoaded", function () {

    const callNextBtn = document.getElementById("callNextBtn");

    const startBtn = document.getElementById("startBtn");
    const completeBtn = document.getElementById("completeBtn");
    const skipBtn = document.getElementById("skipBtn");

    const pauseBtn = document.getElementById("pauseBtn");
    const resumeBtn = document.getElementById("resumeBtn");
    const checkoutBtn = document.getElementById("checkoutBtn");

    const currentToken = document.getElementById("currentToken");
    const waitingCount = document.getElementById("waitingCount");
    const completedCount = document.getElementById("completedCount");
    const counterStatus = document.getElementById("counterStatus");

    const serviceToken = document.getElementById("serviceToken");
    const serviceStudent = document.getElementById("serviceStudent");

    // Call Next Student
    if (callNextBtn) {
        callNextBtn.addEventListener("click", function () {

            const firstWaitingRow =
                document.querySelector("#queueTableBody tr");

            if (!firstWaitingRow) {
                alert("No students are waiting.");
                return;
            }

            const cells = firstWaitingRow.querySelectorAll("td");

            const token = cells[0].textContent.trim();
            const student = cells[1].textContent.trim();

            currentToken.textContent = token;
            serviceToken.textContent = token;
            serviceStudent.textContent = student;

            firstWaitingRow.remove();

            let count = parseInt(waitingCount.textContent);
            if (count > 0) {
                waitingCount.textContent = count - 1;
            }
        });
    }

    // Start Service
    if (startBtn) {
        startBtn.addEventListener("click", function () {

            if (serviceToken.textContent === "--") {
                alert("Please call a student first.");
                return;
            }

            alert("Service started for " + serviceToken.textContent);
        });
    }

    // Complete Service
    if (completeBtn) {
        completeBtn.addEventListener("click", function () {

            if (serviceToken.textContent === "--") {
                alert("No active service.");
                return;
            }

            let completed = parseInt(completedCount.textContent);
            completedCount.textContent = completed + 1;

            currentToken.textContent = "--";
            serviceToken.textContent = "--";
            serviceStudent.textContent = "--";
        });
    }

    // Skip Student
    if (skipBtn) {
        skipBtn.addEventListener("click", function () {

            if (serviceToken.textContent === "--") {
                alert("No active service.");
                return;
            }

            currentToken.textContent = "--";
            serviceToken.textContent = "--";
            serviceStudent.textContent = "--";
        });
    }

    // Pause Counter
    if (pauseBtn) {
        pauseBtn.addEventListener("click", function () {

            counterStatus.textContent = "Paused";

            pauseBtn.style.display = "none";
            resumeBtn.style.display = "inline-block";
        });
    }

    // Resume Counter
    if (resumeBtn) {
        resumeBtn.addEventListener("click", function () {

            counterStatus.textContent = "Available";

            resumeBtn.style.display = "none";
            pauseBtn.style.display = "inline-block";
        });
    }

    // Check Out
    if (checkoutBtn) {
        checkoutBtn.addEventListener("click", function () {

            counterStatus.textContent = "Checked Out";

            pauseBtn.style.display = "none";
            resumeBtn.style.display = "none";
            checkoutBtn.disabled = true;
        });
    }

});