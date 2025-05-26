const searchBtn = $('#searchBtn');
const searchForm = $('#searchForm');

setThemeSwitchBtnTitle();

searchBtn.on('click', (event) => {
    event.stopPropagation();
    searchBtn.attr('hidden', true);
    searchForm.attr('hidden', false);
});

searchForm.on('click', (event) => {
    event.stopPropagation();
});

$(window).click(function() {
    if(window.location.pathname !== '/search') {
        searchForm.attr('hidden', true);
        searchForm.find('input').val('');
        searchBtn.attr('hidden', false);
    }
});

function setThemeSwitchBtnTitle() {
    let title = (theme === 'dark') ? getMessage('info.switch-to-light-theme') : getMessage('info.switch-to-dark-theme');
    $('#themeSwitchBtn').attr('title', title);
}

function changeTheme() {
    theme = (theme === 'light') ? 'dark' : 'light';
    localStorage.setItem('bs-theme', theme);
    $('html').attr('data-bs-theme', theme);
    setThemeSwitchBtnTitle();
}

function changeLocale(locale) {
    let urlParams = new URLSearchParams(window.location.search);
    urlParams.set('lang', locale);
    window.location.replace('?' + urlParams.toString());
}

function copyAppLink() {
    navigator.clipboard.writeText(window.location.origin);
    successToast(getMessage('info.link-copied'))
}

function shareAppOnVk() {
    window.open(`https://vk.com/share.php?url=${window.location.origin}&title=${getMessage('info.app-description')}`);
}

function shareAppOnTelegram() {
    window.open(`https://t.me/share/url?url=${window.location.origin}&text=${getMessage('info.app-description')}`);
}

function shareAppOnWhatsApp() {
    window.open(`https://api.whatsapp.com/send?text=${getMessage('info.app-description')} ${window.location.origin}`);
}

function setupToggles() {
    $('.with-popover').on('shown.bs.popover', () => {
        $('.btn-close').on('click', () => $('.with-popover').popover('hide'));
    });

    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
    const popoverList = [...popoverTriggerList].map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));
}
