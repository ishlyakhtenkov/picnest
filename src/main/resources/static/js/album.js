const albumId = $('#albumId').text();
const filesInput = $('#filesInput');
const noPicturesAlert = $('#noPicturesAlert');

filesInput.on('change', () => {
    $.each(filesInput.prop('files'), (index, file) => {
        upload(file);
    });
    filesInput.val('');
});

function upload(file) {
    noPicturesAlert.attr('hidden', true);
    let pictureCardCol = $('<div></div>').addClass('col mb-4 picture-col');
    let pictureCardSlot = $('<div></div>').addClass('border rounded align-content-center text-center').css('height', '150px');
    let spinner = $('<div></div').addClass('spinner-border');
    pictureCardSlot.append(spinner);
    pictureCardCol.append(pictureCardSlot);
    $('#picturesArea').prepend(pictureCardCol);

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
            let picture = $('<img />').addClass('img-fluid').attr('src', `/${uploadedPicture.file.fileLink}`).css('height', '100%');
            pictureCardSlot.empty();
            pictureCardSlot.append(picture);
        } else {
            let fileReader = new FileReader();
            fileReader.onload = function (event) {
                let picture = $('<img />').addClass('img-fluid').attr('src', event.target.result).css('height', '100%');
                pictureCardSlot.empty();
                pictureCardSlot.append(picture);
            }
            fileReader.readAsDataURL(file);
        }
    }).fail(function (data) {
        pictureCardCol.remove();
        if (!$('.picture-col').length) {
            noPicturesAlert.attr('hidden', false);
        }
        handleError(data, getMessage('picture.failed-to-create'));
    });
}

function openFilesInput() {
    filesInput.click();
}