window.onload = function() {
    checkActionHappened();
};

function checkActionHappened() {
    let actionSpan = $("#actionSpan");
    if (actionSpan.length) {
        successToast(`${actionSpan.data('action')}`);
    }
}

$.ajaxSetup({
    beforeSend: function(xhr, settings) {
        if (!csrfSafeMethod(settings.type) && !this.crossDomain) {
            let token = $("meta[name='_csrf']").attr("content");
            let header = $("meta[name='_csrf_header']").attr("content");
            xhr.setRequestHeader(header, token);
        }
    }
});

function csrfSafeMethod(method) {
    // these HTTP methods do not require CSRF protection
    return (/^(GET|HEAD|OPTIONS|TRACE)$/.test(method));
}

function successToast(message) {
    $.toast({
        heading: getMessage('info.success'),
        text: message,
        showHideTransition: 'slide',
        position: 'bottom-right',
        icon: 'success',
        hideAfter : 6000
    })
}

function failToast(message) {
    $.toast({
        heading: getMessage('info.error'),
        text: message,
        showHideTransition: 'slide',
        position: 'bottom-right',
        icon: 'error',
        hideAfter : 6000
    })
}

function handleError(data, title) {
    if (data.status === 0) {
        window.location.reload();
    }
    let message = `${title}:<br>`;
    if (data.status === 422) {
        let invalidParams = data.responseJSON.invalid_params;
        if (invalidParams != null) {
            console.log(invalidParams);
            $.each(invalidParams, function(param, errorMessage) {
                message += `${errorMessage}<br>`;
            });
        } else {
            message += data.responseJSON.detail;
        }
    } else {
        if (data.responseJSON) {
            message += data.responseJSON.detail;
        } else {
            message += data.status;
        }
    }
    failToast(message);
}

function zoomImage(image) {
    $('#zoomImageModalHeader').text(image.data('filename'));
    $('#zoomImage').attr('src', image.attr('src'));
    $('#zoomImageModal').modal('show');
}

function getAvatarLink(avatar) {
    return avatar != null ? (avatar.fileLink.startsWith('https://') ? avatar.fileLink : `/${avatar.fileLink}`) : '/images/no-avatar.svg';
}

function formatDateTime(dateTime) {
    let dateAndTime = dateTime.split('T');
    let dateParts =  dateAndTime[0].split('-');
    let formattedDate = `${dateParts[2]}.${dateParts[1]}.${dateParts[0]}`;
    let formattedTime = dateAndTime[1].substring(0, dateAndTime[1].lastIndexOf(':'));
    return `${formattedDate} ${formattedTime}`;
}

function resetPasswordModalValues(modal) {
    modal.find('#password').val('').removeClass('is-invalid');
    modal.find('#repeatPassword').val('').removeClass('is-invalid');
    modal.find('#passwordMatchError').text('');
    modal.find('#confirmBtn').prop('disabled', false);
}
