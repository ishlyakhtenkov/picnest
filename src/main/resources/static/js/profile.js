const changePasswordModal = $('#changePasswordModal');

changePasswordModal.on('show.bs.modal', function(e) {
    let modal = $(e.currentTarget);
    resetPasswordModalValues(modal);
    modal.find('#currentPassword').val('');
});

function changePassword() {
    let password = changePasswordModal.find('#password').val();
    let repeatPassword = changePasswordModal.find('#repeatPassword').val();
    if (password.length && repeatPassword.length && password === repeatPassword) {
        $.ajax({
            url: "/profile/change-password",
            type: "PATCH",
            data: "password=" + password
        }).done(function () {
            changePasswordModal.modal('toggle');
            successToast(getMessage('user.password-changed'));
        }).fail(function(data) {
            handleError(data, getMessage('user.failed-to-change-password'));
        });
    }
}
