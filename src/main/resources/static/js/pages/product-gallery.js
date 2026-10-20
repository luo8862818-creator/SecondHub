
document.addEventListener("DOMContentLoaded", () => {
    const carousels = document.querySelectorAll("[data-carousel]");

    carousels.forEach(carousel => {
        const track = carousel.querySelector(".carousel-track");
        const slides = Array.from(
            carousel.querySelectorAll(".carousel-slide")
        );

        const prevButton = carousel.querySelector(".carousel-prev");
        const nextButton = carousel.querySelector(".carousel-next");
        const counter = carousel.querySelector(".carousel-counter");

        if (!track || slides.length === 0) {
            return;
        }

        let currentIndex = 0;

        function stopInactiveVideos() {
            slides.forEach((slide, index) => {
                if (index !== currentIndex) {
                    const video = slide.querySelector("video");

                    if (video) {
                        video.pause();
                    }
                }
            });
        }

        function updateCarousel() {
            track.style.transform =
                `translateX(-${currentIndex * 100}%)`;

            if (counter) {
                counter.textContent =
                    `${currentIndex + 1} / ${slides.length}`;
            }

            // 只有多张媒体时才需要切换按钮
            const hasMultipleSlides = slides.length > 1;

            prevButton.hidden = !hasMultipleSlides;
            nextButton.hidden = !hasMultipleSlides;

            stopInactiveVideos();
        }

        function move(direction) {
            if (slides.length <= 1) {
                return;
            }

            currentIndex =
                (currentIndex + direction + slides.length)
                % slides.length;

            updateCarousel();
        }

        prevButton.addEventListener("click", () => move(-1));
        nextButton.addEventListener("click", () => move(1));

        updateCarousel();
    });
});
