const changePasswordModal = $('#changePasswordModal');
const projectsTab = $('#projectsTab');
const qualificationTab = $('#qualificationTab');

let href = window.location.href;
if (href.includes('#qualification')) {
    $('#qualificationTabBtn').click();
} else if (href.includes('#projects')) {
    $('#projectsTabBtn').click();
}

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

function showInformation() {
    window.history.replaceState(null, null, "#");
    sessionStorage.removeItem('prevUrlHash');
}

function showQualification() {
    window.history.replaceState(null, null, "#qualification");
    sessionStorage.setItem('prevUrlHash', 'qualification');
    if (qualificationTab.children().length === 0) {
        getProjectsAndFillTabs();
    }
}

function showProjects() {
    window.history.replaceState(null, null, "#projects");
    sessionStorage.setItem('prevUrlHash', 'projects');
    if (projectsTab.children().length === 0) {
        getProjectsAndFillTabs();
    }
}

function getProjectsAndFillTabs() {
    $.ajax({
        url: '/projects/by-author',
        data: `userId=${$('#userId').text()}`
    }).done(projects => {
        fillProjectsTab(projects);
        fillQualificationTab(projects);
    }).fail(function(data) {
        handleError(data, getMessage('project.failed-to-get-projects'));
    });
}

function fillProjectsTab(projects) {
    projectsTab.empty();
    let row = $('<div></div>').addClass('row justify-content-center');
    let column = $('<div></div>').addClass(projects.length === 0 ? 'col-md-12' : 'col-md-9');
    if (authUser != null && authUser.id == $('#userId').text()) {
        let addNewBtnSm = $('<a></a>').addClass('btn btn-sm btn-outline-success float-end d-none d-sm-block')
            .css('margin-top', '-20px').attr('href', '/projects/add')
            .html(`<i class="fa-solid fa-plus me-1"></i>${getMessage('project.add-new')}`);
        let addNewBtn = $('<a></a>').addClass('btn btn-sm btn-outline-success d-sm-none').attr('href', '/projects/add')
            .html(`<i class="fa-solid fa-plus me-1"></i>${getMessage('project.add-new')}`);
        column.append(addNewBtn);
        column.append(addNewBtnSm);
    }
    row.append(column);
    projects.forEach(project => {
        column.append(generateProjectCard(project, 'mt-3', '4', true));
    });
    projectsTab.append(row);
    if (projects.length === 0) {
        let noProjects = $('<div></div>').addClass('alert alert-secondary mb-0').css('margin-top', '10px')
            .text(getMessage('profile.no-projects'));
        projectsTab.append(noProjects);
    }

    setupToggles();
}

function deleteProject(id, name) {
    $.ajax({
        url: `/projects/${id}`,
        type: "DELETE"
    }).done(function () {
        successToast(getMessage('project.deleted', [name]));
        getProjectsAndFillTabs();
    }).fail(function (data) {
        handleError(data, getMessage('project.failed-to-delete', [name]));
    });
}

function hideProject(id, name, hideBtn) {
    $(`#manageBtn-${id}`).click();
    let visible = !hideBtn.find('i').attr('class').includes('fa-eye-slash');
    $.ajax({
        url: `/projects/${id}`,
        type: "PATCH",
        data: "visible=" + visible
    }).done(function () {
        successToast(getMessage(visible ? 'project.has-been-revealed' : 'project.has-been-hided', [name]));
        if (!visible) {
            let invisibleSymbol = $('<i></i>').addClass('fa-solid fa-eye-slash text-warning tiny float-end')
                .attr('title', getMessage('project.hidden-from-users')).css('position', 'relative')
                .css('z-index', '2');
            $(`#${id}-name-elem`).append(invisibleSymbol);
        } else {
            $(`#${id}-name-elem`).find('i').remove();
        }
        hideBtn.html(`<i class="fa-solid ${visible ? 'fa-eye-slash' : 'fa-eye'} fa-fw text-warning me-2"></i>${getMessage(visible ? 'project.hide' : 'project.reveal')}`);
    }).fail(function (data) {
        handleError(data, getMessage(visible ? 'project.failed-to-reveal' : 'project.failed-to-hide', [name]));
    });
}

function fillQualificationTab(projects) {
    qualificationTab.empty();
    let technologies = [];
    let alreadyAdded = [];
    projects.forEach(project => {
        project.technologies.forEach(technology => {
            if ($.inArray(technology.name, alreadyAdded) === -1) {
                technologies.push(technology);
                alreadyAdded.push(technology.name);
            }
        });
    });

    technologies.sort(technologiesComparator);

    technologies.forEach(technology => {
        let technologySpan = $('<span></span>').addClass('badge bg-body-tertiary me-2 mt-2');
        let technologyLink = $('<a></a>').attr('type', 'button').attr('target', '_blank').attr('href', technology.url)
            .addClass('link-underline link-underline-opacity-0 link-underline-opacity-75-hover link-body-emphasis');
        let technologyImage = $('<img>').addClass('align-bottom me-1').attr('src', `/${technology.logo.fileLink}`)
            .attr('width', '32').attr('height', '32');
        technologyLink.append(technologyImage).append(technology.name);
        technologySpan.append(technologyLink);
        qualificationTab.append(technologySpan);
    });

    if (projects.length === 0) {
        let noQualification = $('<div></div>').addClass('alert alert-secondary mt-1 mb-0')
            .text(getMessage('profile.no-qualification'));
        qualificationTab.append(noQualification);
    }
}

const priorities = new Map();
priorities.set('ULTRA', '0');
priorities.set('VERY_HIGH', '1');
priorities.set('HIGH', '2');
priorities.set('MEDIUM', '3');
priorities.set('LOW', '4');
priorities.set('VERY_LOW', '5');

function technologiesComparator(t1, t2) {
    if (t1.usage !== t2.usage) {
        return (t1.usage === 'BACKEND') ? -1 : 1;
    }
    if (t1.priority !== t2.priority) {
        return (priorities.get(t1.priority) < priorities.get(t2.priority)) ? -1 : 1
    }
    if (t1.name !== t2.name) {
        return (t1.name < t2.name) ? -1 : 1;
    }
    return 0;
}
