const albumId = $('#albumId').text();
const filesInput = $('#filesInput');
const noPhotosAlert = $('#noPhotosAlert');

filesInput.on('change', () => {
    $.each(filesInput.prop('files'), (index, file) => {
        upload(file);
    });
    filesInput.val('');
});

function upload(file) {
    noPhotosAlert.attr('hidden', true);
    let photoCardCol = $('<div></div>').addClass('col mb-4 photo-col');
    let photoCardSlot = $('<div></div>').addClass('border rounded align-content-center text-center').css('height', '150px');
    let spinner = $('<div></div').addClass('spinner-border');
    photoCardSlot.append(spinner);
    photoCardCol.append(photoCardSlot);
    $('#photosArea').prepend(photoCardCol);

    let formData = new FormData();
    formData.append('file', file);
    $.ajax({
        url: `/albums/${albumId}/photos`,
        type: 'POST',
        data: formData,
        processData:false,
        contentType:false
    }).done((photo) => {
        let fileReader = new FileReader();
        fileReader.onload = function (event) {
            let photo = $('<img />').addClass('img-fluid').attr('src', event.target.result).css('height', '100%');
            photoCardSlot.empty();
            photoCardSlot.append(photo);
        }
        fileReader.readAsDataURL(file);
    }).fail(function (data) {
        photoCardCol.remove();
        if (!$('.photo-col').length) {
            noPhotosAlert.attr('hidden', false);
        }
        handleError(data, getMessage('photo.failed-to-create'));
    });
}

function openFilesInput() {
    filesInput.click();
}