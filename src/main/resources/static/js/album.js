const albumId = $('#albumId').text();
const filesInput = $('#filesInput');
const noPicturesAlert = $('#noPicturesAlert');
const deletePictureModal = $('#deletePictureModal');

filesInput.on('change', () => {
    $.each(filesInput.prop('files'), (index, file) => {
        upload(file);
    });
    filesInput.val('');
});

function upload(file) {
    noPicturesAlert.attr('hidden', true);
    let pictureCol = $('<div></div>').addClass('col mb-4 picture-col');
    let emptySlot = $('<div></div>').addClass('border align-content-center text-center').css('height', '150px');
    let spinner = $('<div></div').addClass('spinner-border');
    emptySlot.append(spinner);
    pictureCol.append(emptySlot);
    $('#picturesArea').prepend(pictureCol);

    let formData = new FormData();
    formData.append('file', file);
    $.ajax({
        url: `/albums/${albumId}/pictures`,
        type: 'POST',
        data: formData,
        processData:false,
        contentType:false
    }).done((uploadedPicture) => {
        if (file.name.toLowerCase().endsWith('.heic')) {
            let picture = $('<img />').addClass('img-fluid').attr('src', `/${uploadedPicture.file.fileLink}`).css('cursor', 'zoom-in');
            showUploadedPicture(picture, pictureCol, uploadedPicture);
        } else {
            let fileReader = new FileReader();
            fileReader.onload = function (event) {
                let picture = $('<img />').addClass('img-fluid').attr('src', event.target.result).css('cursor', 'zoom-in');
                showUploadedPicture(picture, pictureCol, uploadedPicture);
            }
            fileReader.readAsDataURL(file);
        }
    }).fail(function (data) {
        pictureCol.remove();
        if (!$('.picture-col').length) {
            noPicturesAlert.attr('hidden', false);
        }
        handleError(data, getMessage('picture.failed-to-create'));
    });
}

function showUploadedPicture(picture, pictureCol, uploadedPicture) {
    picture.on('click', () => {
        zoomImage(picture);
    })
    pictureCol.empty();
    pictureCol.attr('id', `picture-col-${uploadedPicture.id}`);
    let actionsBtnSpan = $('<span></span>').addClass('float-end pt-1').css('margin-bottom', '-36px')
        .css('margin-right', '4px').css('position', 'relative').css('z-index', '2');
    let actionsBtn = $('<button></button>').attr('type', 'button').addClass('btn btn-sm btn-outline-light rounded-circle')
        .attr('title', getMessage('actions')).attr('data-bs-toggle', 'dropdown')
        .attr('aria-expanded', 'false').append($('<i></i>').addClass('fa-solid fa-ellipsis'));
    let dropdownList = $('<ul></ul>').addClass('dropdown-menu');
    let dropdownDownloadItem = $('<a download></a>').attr('type', 'button').addClass('dropdown-item')
        .attr('data-id', uploadedPicture.id).attr('href', `/${uploadedPicture.file.fileLink}`).text(getMessage('download'));
    let dropdownDeleteItem = $('<a></a>').attr('type', 'button').addClass('dropdown-item').attr('data-id',uploadedPicture.id)
        .text(getMessage('delete'));
    dropdownDeleteItem.on('click', () => {
        showDeletePictureModal(dropdownDeleteItem);
    });
    dropdownList.append($('<li></li>').append(dropdownDownloadItem)).append($('<li></li>').append(dropdownDeleteItem));
    actionsBtnSpan.append(actionsBtn).append(dropdownList);
    pictureCol.append(actionsBtnSpan);
    pictureCol.append(picture);
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
    }).done(function() {
        $(`#picture-col-${id}`).remove();
        if (!$('.picture-col').length) {
            noPicturesAlert.attr('hidden', false);
        }
        successToast(getMessage('picture.deleted'));
    }).fail(function(data) {
        handleError(data, getMessage('picture.failed-to-delete'));
    });
}