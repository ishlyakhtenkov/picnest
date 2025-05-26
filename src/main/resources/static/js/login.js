const forgotPasswordModal = $('#forgotPasswordModal');
const emailSentModal = $('#emailSentModal');

forgotPasswordModal.on('show.bs.modal', function(e) {
    $(e.currentTarget).find('#email').val('');
});

function forgotPassword() {
    let email = forgotPasswordModal.find('#email').val();
    if (email.length) {
        $.ajax({
            url: "/profile/forgot-password",
            type: "POST",
            data: "email=" + email
        }).done(function () {
            forgotPasswordModal.modal('toggle');
            emailSentModal.modal('toggle');
        }).fail(function(data) {
            handleError(data, getMessage('user.failed-to-reset-password'));
        });
    }
}
