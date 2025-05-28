const albumModal = $('#albumModal');
const deleteAlbumModal = $('#deleteAlbumModal');
setupToggles();

function showAlbumModalForCreate(newBtn) {
    if (authUser != null) {
        albumModal.find('#albumModalTitle').text(getMessage('album.create'));
        albumModal.data('albumId', null);
        albumModal.find('#albumName').val('');
        albumModal.find('#confirmBtn').text(getMessage('create'));
        albumModal.modal('toggle');
    } else {
        $('.with-popover').popover('hide');
        newBtn.popover('toggle');
    }
}

function showAlbumModalForUpdate(editBtn) {
    let albumId = editBtn.data('id');
    let albumName = $(`#albumName-${albumId}`).text();
    albumModal.find('#albumModalTitle').text(getMessage('album.edit'));
    albumModal.data('albumId', albumId);
    albumModal.find('#albumName').val(albumName);
    albumModal.find('#confirmBtn').text(getMessage('save'));
    albumModal.modal('toggle');
}

function createOrUpdateAlbum() {
    let id = albumModal.data('albumId');
    let name = albumModal.find('#albumName').val();
    if (name.length) {
        if (id === null) {
            createAlbum(name);
        } else {
            updateAlbum(id, name);
        }
    }
}

function createAlbum(name) {
    $.ajax({
        url: '/albums',
        type: 'POST',
        data: "name=" + name
    }).done((album) => {
        albumModal.modal('toggle');
        let albumCardCol = $('<div></div>').addClass('col mb-4').attr('id', `albumCardCol-${album.id}`);
        $('#albumsArea').prepend(albumCardCol.append(generateAlbumCard(album)));
        successToast(getMessage('album.created', [name]));
    }).fail(function (data) {
        handleError(data, getMessage('album.failed-to-create'));
    });
}

function generateAlbumCard(album) {
    let card = $('<div></div>').addClass('card h-100 album-card');
    let ratio = $('<div></div>').addClass('ratio').css('--bs-aspect-ratio', '50%');
    let imageCap = $('<img>').addClass('card-img-top').attr('src', `/images/album.jpg`).css('object-fit', 'cover');
    ratio.append(imageCap);
    card.append(ratio);
    let cardBody = $('<div></div>').addClass('card-body');
    let editBtnSpan = $('<span></span>').addClass('float-end px-3 pt-2 pb-1').css('margin-top', '-14px')
        .css('margin-right', '-16px').css('position', 'relative').css('z-index', '2');
    let editBtn = $('<button></button>').attr('type', 'button').addClass('btn btn-link link-secondary p-0')
        .attr('title', getMessage('edit')).attr('data-bs-toggle', 'dropdown')
        .attr('aria-expanded', 'false').append($('<i></i>').addClass('fa-solid fa-ellipsis-vertical'));
    let dropdownList = $('<ul></ul>').addClass('dropdown-menu');
    let dropdownEditItem = $('<a></a>').attr('type', 'button').addClass('dropdown-item').attr('data-id', album.id)
        .text(getMessage('edit'));
    dropdownEditItem.on('click', () => {
        showAlbumModalForUpdate(dropdownEditItem);
    })
    let dropdownDeleteItem = $('<a></a>').attr('type', 'button').addClass('dropdown-item').attr('data-id', album.id)
        .text(getMessage('delete'));
    dropdownDeleteItem.on('click', () => {
        showDeleteAlbumModal(dropdownDeleteItem);
    })
    dropdownList.append($('<li></li>').append(dropdownEditItem)).append($('<li></li>').append(dropdownDeleteItem));
    editBtnSpan.append(editBtn).append(dropdownList);
    cardBody.append(editBtnSpan);
    let title = $('<h5></h5>').addClass('card-title').attr('id', `albumName-${album.id}`).text(album.name);
    cardBody.append(title);
    let infoRow = $('<div></div>').addClass('row');
    let photoCountCol = $('<div></div>').addClass('col text-muted  mt-auto').text('?? photos'); //TODO
    let createdCol = $('<div></div>').addClass('col small text-muted fst-italic text-end mt-auto')
        .text(formatDateTime(album.created).split(' ')[0]);
    infoRow.append(photoCountCol).append(createdCol);
    cardBody.append(infoRow);
    let stretchedLink = $('<a></a>').addClass('stretched-link').attr('href', '#'); //TODO
    cardBody.append(stretchedLink);
    card.append(cardBody);
    return card;
}

function updateAlbum(id, name) {
    $.ajax({
        url: `/albums/${id}`,
        type: 'PUT',
        data: "name=" + name
    }).done(() => {
        albumModal.modal('toggle');
        $(`#albumName-${id}`).text(name);
        successToast(getMessage('album.updated', [name]));
    }).fail(function (data) {
        handleError(data, getMessage('album.failed-to-update'));
    });
}

function showDeleteAlbumModal(deleteBtn) {
    let albumId = deleteBtn.data('id');
    let albumName = $(`#albumName-${albumId}`).text();
    deleteAlbumModal.find('#deleteAlbumModalLabel')
        .html(`<div class="fw-bold">${getMessage('album.sure-to-delete', [albumName])}</div>
               <div class="text-danger">${getMessage('album.photos-will-be-delete')}</div>`);
    deleteAlbumModal.data('albumId', albumId).data('albumName', albumName);
    deleteAlbumModal.modal('toggle');
}

function deleteAlbum() {
    let id = deleteAlbumModal.data('albumId');
    let name = deleteAlbumModal.data('albumName');
    deleteAlbumModal.modal('toggle');
    $.ajax({
        url: `/albums/${id}`,
        type: "DELETE"
    }).done(function() {
        $(`#albumCardCol-${id}`).remove();
        successToast(getMessage('album.deleted', [name]));
    }).fail(function(data) {
        handleError(data, getMessage('album.failed-to-delete', [name]));
    });
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
    } else if (authUser != null && urlParams.has('by-author') && urlParams.get('by-author') == authUser.id) {
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
}, {threshold: 0.25});

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
        params = {
            page: currentPage,
            size: 9,
            tag: window.location.pathname.substring(window.location.pathname.lastIndexOf('/') + 1)
        }
    } else {
        params = {page: currentPage, size: 9}
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
    }).fail(function (data) {
        handleError(data, getMessage('project.failed-to-get-projects'));
        loading = false;
    });
}





