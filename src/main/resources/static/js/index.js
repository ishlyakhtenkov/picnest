setupToggles();

function showAddProjectPage(newBtn) {
    if (authUser != null) {
        window.location.href = '/projects/add';
    } else {
        $('.with-popover').popover('hide');
        newBtn.popover('toggle');
    }
}

let currentNav = null;
if (window.location.pathname.startsWith('/tags/')) {
    currentNav = 'tagNav';
    let navs = $('#navs');

    let navItem = $('<li></li>').addClass('nav-item');
    let navLink = $('<button></button>').addClass('nav-link secondary-nav-link pt-0').attr('type', 'button')
        .attr('id', 'tagNav')
        .text('#' + (window.location.pathname.substring(window.location.pathname.lastIndexOf('/') + 1)));
    navItem.append(navLink);
    navs.append(navItem);
} else {
    let urlParams = new URLSearchParams(window.location.search);

    if (urlParams.has('popular')) {
        currentNav = 'popularNav';
    } else if (authUser != null && urlParams.has('by-author') && urlParams.get('by-author') ==  authUser.id) {
        currentNav = 'myNav';
    } else if (!urlParams.has('by-author')) {
        currentNav = 'freshNav';
    }
}

if (currentNav != null) {
    $(`#${currentNav}`).addClass('active');
}

function showPopular() {
    if (currentNav !== 'popularNav') {
        window.location.href = '/?popular';
    }
}

function showFresh() {
    if (currentNav !== 'freshNav') {
        window.location.href = '/';
    }
}

function showMy() {
    if (currentNav !== 'myNav') {
        window.location.href = `/?by-author=${authUser.id}`;
    }
}

let currentPage = 0;
let loading = false;
const projectsArea = $('#projectsArea');
const loadingDiv = $('#loading')[0];

const observer = new IntersectionObserver(async (entries) => {
    if (entries[0].isIntersecting && !loading) {
        loading = true;
        currentPage++;
        getProjects();
    }
}, { threshold: 0.25 });

if (loadingDiv) {
    observer.observe(loadingDiv);
}

function getProjects() {
    let pathVariable;
    if (currentNav != null) {
        pathVariable = currentNav === 'popularNav' ? 'popular' : (currentNav === 'tagNav' ? 'by-tag' : 'fresh');
    } else {
        pathVariable = 'fresh';
    }
    let params;
    if (pathVariable === 'by-tag') {
        params = { page: currentPage, size: 9, tag:  window.location.pathname.substring(window.location.pathname.lastIndexOf('/') + 1) }
    } else {
        params = { page: currentPage, size: 9 }
    }

    $.ajax({
        url: `/projects/${pathVariable}`,
        data: params
    }).done(projectsPage => {
        if (projectsPage.content.length !== 0) {
            projectsPage.content.forEach(project => {
                let col = $('<div></div>').addClass('col mb-4');
                col.append(generateProjectCard(project, 'h-100', '2', false));
                projectsArea.append(col);
            });
            setupToggles();
        } else {
            observer.disconnect();
            loadingDiv.remove();
        }
        loading = false;
    }).fail(function(data) {
        handleError(data, getMessage('project.failed-to-get-projects'));
        loading = false;
    });
}





