const changePasswordModal = $('#changePasswordModal');
let changePasswordUserId;
let changePasswordUserName;

changePasswordModal.on('show.bs.modal', function(e) {
    let changePasswordBtn = $(e.relatedTarget);
    changePasswordUserId = changePasswordBtn.data('id');
    changePasswordUserName = changePasswordBtn.data('name');
    console.log($(e.relatedTarget));
    let modal = $(e.currentTarget);
    resetPasswordModalValues(modal);
    modal.find('#changePasswordModalUserId').val(changePasswordUserId);
    modal.find('#changePasswordModalUserName').val(changePasswordUserName);
    modal.find('#changePasswordModalLabel').text(getMessage('user.change-password-for', [changePasswordUserName]));
});

function changePassword() {
    let id = changePasswordModal.find('#changePasswordModalUserId').val();
    let name = changePasswordModal.find('#changePasswordModalUserName').val();
    let password = changePasswordModal.find('#password').val();
    let repeatPassword = changePasswordModal.find('#repeatPassword').val();
    if (password.length && repeatPassword.length && password === repeatPassword) {
        $.ajax({
            url: `/management/users/change-password/${id}`,
            type: "PATCH",
            data: "password=" + password
        }).done(function () {
            changePasswordModal.modal('toggle');
            successToast(getMessage('user.password-changed-for', [name]));
        }).fail(function (data) {
            handleError(data, getMessage('user.failed-to-change-password-for', [name]));
        });
    }
}

function enableUser(checkbox) {
    let id = checkbox.data('id');
    let name = checkbox.data('name');
    let enabled = checkbox.prop('checked');
    $.ajax({
        url: `/management/users/${id}`,
        type: "PATCH",
        data: "enabled=" + enabled
    }).done(function() {
        successToast(getMessage(enabled ? 'user.enabled' : 'user.disabled', [name]));
        checkbox.prop('title', getMessage(enabled ? 'user.disable' : 'user.enable'));
        if (!enabled) {
            $(`#row-${id}`).find('.online-circle').removeClass('text-success').addClass('text-danger').prop('title', 'offline');
        }
    }).fail(function(data) {
        checkbox.prop('checked', !enabled);
        handleError(data, getMessage(enabled ? 'user.failed-to-enable' : 'user.failed-to-disable', [name]));
    });
}
