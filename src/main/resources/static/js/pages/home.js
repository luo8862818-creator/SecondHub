const successMessage = document.querySelector("#successMessage");

if (successMessage) {
    setTimeout(() => {
        successMessage.classList.add("is-hidden");

        setTimeout(() => {
            successMessage.remove();
        }, 300);
    }, 2000);
}