function copyLink(projectId) {
    navigator.clipboard.writeText(window.location.origin + `/projects/${projectId}/view`);
    successToast(getMessage('info.link-copied'))
}

function shareOnVk(shareBtn) {
    window.open(`https://vk.com/share.php?url=${window.location.origin}/projects/${shareBtn.data('id')}/view&title=${shareBtn.data('name')}`);
}

function shareOnTelegram(shareBtn) {
    window.open(`https://t.me/share/url?url=${window.location.origin}/projects/${shareBtn.data('id')}/view&text=${shareBtn.data('name')}`);
}

function shareOnWhatsApp(shareBtn) {
    window.open(`https://api.whatsapp.com/send?text=${shareBtn.data('name')} ${window.location.origin}/projects/${shareBtn.data('id')}/view`);
}
