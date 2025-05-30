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
            let picture = $('<img />').addClass('img-fluid').attr('src', `/${uploadedPicture.file.fileLink}`);
            pictureCol.empty();
            pictureCol.append(picture);
        } else {
            let fileReader = new FileReader();
            fileReader.onload = function (event) {
                let picture = $('<img />').addClass('img-fluid').attr('src', event.target.result);
                pictureCol.empty();
                pictureCol.append(picture);
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

function openFilesInput() {
    filesInput.click();
}