document.addEventListener("DOMContentLoaded", () => {
  const viewer = document.querySelector("#imageViewer");
  const viewerImage = document.querySelector("#imageViewerImage");
  const counter = document.querySelector("#imageViewerCounter");

  if (!viewer || !viewerImage || !counter) {
    return;
  }

  const images = Array.from(
    document.querySelectorAll(".image-carousel .carousel-slide img"),
  );

  const closeButton = viewer.querySelector(".image-viewer-close");
  const prevButton = viewer.querySelector(".image-viewer-prev");
  const nextButton = viewer.querySelector(".image-viewer-next");

  let currentIndex = 0;

  function updateViewer() {
    if (images.length === 0) {
      return;
    }

    viewerImage.src = images[currentIndex].src;
    viewerImage.alt = images[currentIndex].alt || "商品图片大图";
    counter.textContent = `${currentIndex + 1} / ${images.length}`;

    prevButton.hidden = images.length <= 1;
    nextButton.hidden = images.length <= 1;
  }

  function openViewer(index) {
    currentIndex = index;
    updateViewer();

    viewer.classList.add("is-open");
    viewer.setAttribute("aria-hidden", "false");
    document.body.style.overflow = "hidden";
    closeButton.focus();
  }

  function closeViewer() {
    viewer.classList.remove("is-open");
    viewer.setAttribute("aria-hidden", "true");
    viewerImage.src = "";
    document.body.style.overflow = "";
  }

  function moveImage(direction) {
    if (images.length <= 1) {
      return;
    }

    currentIndex = (currentIndex + direction + images.length) % images.length;

    updateViewer();
  }

  images.forEach((image, index) => {
    image.addEventListener("click", () => openViewer(index));
  });

  closeButton.addEventListener("click", closeViewer);
  prevButton.addEventListener("click", () => moveImage(-1));
  nextButton.addEventListener("click", () => moveImage(1));

  viewer.addEventListener("click", (event) => {
    if (event.target === viewer || event.target === viewerImage) {
      closeViewer();
    }
  });

  document.addEventListener("keydown", (event) => {
    if (!viewer.classList.contains("is-open")) {
      return;
    }

    if (event.key === "Escape") {
      closeViewer();
    } else if (event.key === "ArrowLeft") {
      moveImage(-1);
    } else if (event.key === "ArrowRight") {
      moveImage(1);
    }
  });
});
