const albumId = $('#albumId').text();
const filesInput = $('#filesInput');
const noPicturesAlert = $('#noPicturesAlert');
const deletePictureModal = $('#deletePictureModal');

function zoomImageInCarousel(image) {
    let id = image.attr('id').split('-')[1];
    $('.carousel-item').removeClass('active');
    $(`#carousel-item-${id}`).addClass('active');
    $('#carouselModal').modal('toggle');
}

filesInput.on('change', () => {
    $.each(filesInput.prop('files'), (index, file) => {
        upload(file);
    });
    filesInput.val('');
});

function upload(file) {
    noPicturesAlert.attr('hidden', true);
    let emptyCol = $('<div></div>').addClass('col mb-4');
    let emptySlot = $('<div></div>').addClass('border align-content-center text-center').css('height', '150px');
    let spinner = $('<div></div').addClass('spinner-border');
    emptySlot.append(spinner);
    emptyCol.append(emptySlot);
    $('#picturesArea').prepend(emptyCol);

    let formData = new FormData();
    formData.append('file', file);
    $.ajax({
        url: `/albums/${albumId}/pictures`,
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false
    }).done((picture) => {
        let image = null;
        if (picture.type === 'IMAGE') {
            image = $('<img />').addClass('img-fluid').attr('id', `img-${picture.id}`)
                .attr('src', `/${picture.file.fileLink}`).css('cursor', 'zoom-in');
        } else if (picture.type === 'VIDEO') {
            image = $('<video controls></video>').addClass('img-fluid').attr('id', `img-${picture.id}`)
                .attr('src', `/${picture.file.fileLink}`).attr('poster', `/pictures/${picture.id}/preview`);
        }
        showPictureOnPage(image, emptyCol, picture);
    }).fail(function (data) {
        emptyCol.remove();
        if (!$('.picture-col').length) {
            noPicturesAlert.attr('hidden', false);
        }
        handleError(data, getMessage('picture.failed-to-create'));
    });
}

function showPictureOnPage(image, emptyCol, picture) {
    if (picture.type === 'IMAGE') {
        image.on('click', () => {
            zoomImageInCarousel(image);
        });
    }
    let pictureCol = $('<div></div>').addClass('col mb-4 picture-col');
    pictureCol.attr('id', `picture-col-${picture.id}`);
    let actionsBtnSpan = $('<span></span>').addClass('float-end pt-1').css('margin-bottom', '-36px')
        .css('margin-right', '4px').css('position', 'relative').css('z-index', '2');
    let actionsBtn = $('<button></button>').attr('type', 'button').addClass('btn btn-sm btn-outline-light rounded-circle opacity-25')
        .attr('title', getMessage('actions')).attr('data-bs-toggle', 'dropdown')
        .attr('aria-expanded', 'false').append($('<i></i>').addClass('fa-solid fa-ellipsis'));
    let dropdownList = $('<ul></ul>').addClass('dropdown-menu');
    let dropdownDownloadItem = $('<a download></a>').attr('type', 'button').addClass('dropdown-item')
        .attr('data-id', picture.id).attr('href', `/${picture.file.fileLink}`).text(getMessage('download'));
    let dropdownDeleteItem = $('<a></a>').attr('type', 'button').addClass('dropdown-item').attr('data-id', picture.id)
        .text(getMessage('delete'));
    dropdownDeleteItem.on('click', () => {
        showDeletePictureModal(dropdownDeleteItem);
    });
    dropdownList.append($('<li></li>').append(dropdownDownloadItem)).append($('<li></li>').append(dropdownDeleteItem));
    actionsBtnSpan.append(actionsBtn).append(dropdownList);
    pictureCol.append(actionsBtnSpan);
    pictureCol.append(image);
    emptyCol.remove();
    if ($('.picture-col').length) {
        $('.picture-col').first().before(pictureCol);
    } else {
        $('#picturesArea').prepend(pictureCol);
    }

    let carouselItem = $('<div></div>').addClass('carousel-item').attr('id', `carousel-item-${picture.id}`);
    let carouselItemImg = null;
    if (picture.type === 'IMAGE') {
        carouselItemImg = $('<img />').addClass('img-fluid').attr('src', image.attr('src')).css('max-height', '80vh');
    } else if (picture.type === 'VIDEO') {
        carouselItemImg = $('<video controls></video>').addClass('img-fluid').attr('src', image.attr('src'))
            .css('max-height', '80vh').attr('poster', `/pictures/${picture.id}/preview`);
    }
    carouselItem.append(carouselItemImg);
    $('#pictureCarouselInner').prepend(carouselItem);
}

function openFilesInput() {
    filesInput.click();
}

function showDeletePictureModal(deleteBtn) {
    deletePictureModal.data('pictureId', deleteBtn.data('id'));
    deletePictureModal.modal('toggle');
}

function deletePicture() {
    let id = deletePictureModal.data('pictureId');
    deletePictureModal.modal('toggle');
    $.ajax({
        url: `/pictures/${id}`,
        type: "DELETE"
    }).done(function () {
        $(`#picture-col-${id}`).remove();
        if (!$('.picture-col').length) {
            noPicturesAlert.attr('hidden', false);
        }
        $(`#carousel-item-${id}`).remove();
        successToast(getMessage('picture.deleted'));
    }).fail(function (data) {
        handleError(data, getMessage('picture.failed-to-delete'));
    });
}