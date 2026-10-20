document.addEventListener("DOMContentLoaded", () => {
  const imageInput = document.querySelector("#images");
  const videoInput = document.querySelector("#video");

  const imagePreviewList = document.querySelector("#imagePreviewList");
  const videoPreview = document.querySelector("#videoPreview");

  const imageUploadHint = document.querySelector("#imageUploadHint");
  const videoUploadHint = document.querySelector("#videoUploadHint");

  if (!imageInput || !videoInput || !imagePreviewList || !videoPreview) {
    return;
  }

  const MAX_IMAGES = 9;
  const MAX_IMAGE_SIZE = 5 * 1024 * 1024;
  const MAX_VIDEO_SIZE = 100 * 1024 * 1024;

  const allowedImageTypes = ["image/jpeg", "image/png", "image/webp"];

  const allowedVideoTypes = ["video/mp4", "video/webm"];

  let selectedImages = [];
  let selectedVideo = null;
  let videoObjectUrl = null;

  function setStatus(element, message, isError = false) {
    element.textContent = message;
    element.classList.toggle("is-error", isError);
  }

  function formatSize(bytes) {
    return bytes >= 1024 * 1024
      ? `${(bytes / 1024 / 1024).toFixed(2)} MB`
      : `${(bytes / 1024).toFixed(1)} KB`;
  }

  function syncImageInput() {
    const dataTransfer = new DataTransfer();

    selectedImages.forEach((file) => {
      dataTransfer.items.add(file);
    });

    imageInput.files = dataTransfer.files;
  }

  function renderImages() {
    imagePreviewList.replaceChildren();

    selectedImages.forEach((file, index) => {
      const item = document.createElement("div");
      item.className = "media-preview-item";

      const image = document.createElement("img");
      const objectUrl = URL.createObjectURL(file);

      image.src = objectUrl;
      image.alt = file.name;

      image.addEventListener(
        "load",
        () => {
          URL.revokeObjectURL(objectUrl);
        },
        { once: true },
      );

      image.addEventListener(
        "error",
        () => {
          URL.revokeObjectURL(objectUrl);
        },
        { once: true },
      );

      const name = document.createElement("div");
      name.className = "media-preview-name";
      name.textContent = `${file.name} (${formatSize(file.size)})`;

      const removeButton = document.createElement("button");
      removeButton.type = "button";
      removeButton.className = "media-remove-button";
      removeButton.textContent = "移除";
      removeButton.setAttribute("aria-label", `移除图片 ${file.name}`);

      removeButton.addEventListener("click", () => {
        selectedImages.splice(index, 1);

        syncImageInput();
        renderImages();

        setStatus(
          imageUploadHint,
          `已选择 ${selectedImages.length}/${MAX_IMAGES} 张图片`,
        );
      });

      item.append(image, name, removeButton);
      imagePreviewList.appendChild(item);
    });
  }

  imageInput.addEventListener("change", () => {
    const incomingFiles = Array.from(imageInput.files || []);

    if (incomingFiles.length === 0) {
      return;
    }

    let rejectedCount = 0;

    for (const file of incomingFiles) {
      if (!allowedImageTypes.includes(file.type)) {
        rejectedCount++;
        continue;
      }

      if (file.size > MAX_IMAGE_SIZE) {
        rejectedCount++;
        continue;
      }

      if (selectedImages.length >= MAX_IMAGES) {
        rejectedCount++;
        continue;
      }

      selectedImages.push(file);
    }

    syncImageInput();
    renderImages();

    if (rejectedCount > 0) {
      setStatus(
        imageUploadHint,
        `已选择 ${selectedImages.length}/${MAX_IMAGES} 张图片，${rejectedCount} 个文件未添加（格式、大小或数量不符合要求）`,
        true,
      );
    } else {
      setStatus(
        imageUploadHint,
        `已选择 ${selectedImages.length}/${MAX_IMAGES} 张图片`,
      );
    }
  });

  function clearVideoPreview() {
    videoPreview.replaceChildren();

    if (videoObjectUrl) {
      URL.revokeObjectURL(videoObjectUrl);
      videoObjectUrl = null;
    }
  }

  videoInput.addEventListener("change", () => {
    const file = videoInput.files?.[0];

    if (!file) {
      return;
    }

    if (!allowedVideoTypes.includes(file.type)) {
      videoInput.value = "";
      selectedVideo = null;
      clearVideoPreview();

      setStatus(videoUploadHint, "视频仅支持 MP4 或 WebM 格式", true);
      return;
    }

    if (file.size > MAX_VIDEO_SIZE) {
      videoInput.value = "";
      selectedVideo = null;
      clearVideoPreview();

      setStatus(videoUploadHint, "视频大小不能超过 100 MB", true);
      return;
    }

    selectedVideo = file;
    clearVideoPreview();

    videoObjectUrl = URL.createObjectURL(file);

    const video = document.createElement("video");
    video.src = videoObjectUrl;
    video.controls = true;
    video.preload = "metadata";

    const name = document.createElement("p");
    name.className = "media-preview-name";
    name.textContent = `${file.name} (${formatSize(file.size)})`;

    const removeButton = document.createElement("button");
    removeButton.type = "button";
    removeButton.className = "media-remove-button";
    removeButton.textContent = "移除视频";

    removeButton.addEventListener("click", () => {
      selectedVideo = null;
      videoInput.value = "";
      clearVideoPreview();

      setStatus(videoUploadHint, "尚未选择视频");
    });

    videoPreview.append(video, name, removeButton);

    setStatus(videoUploadHint, `已选择视频：${file.name}`);
  });

  // 初始状态
  setStatus(imageUploadHint, `已选择 0/${MAX_IMAGES} 张图片`);
  setStatus(videoUploadHint, "尚未选择视频");
});
