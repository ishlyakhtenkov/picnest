const enMessages = new Map();
const ruMessages = new Map();

enMessages.set('user.passwords-not-match', 'passwords do not match');
enMessages.set('user.change-password-for', 'Change password for "{0}"');
enMessages.set('user.password-changed', 'Password has been changed');
enMessages.set('user.password-changed-for', 'Password for "{0}" has been changed');
enMessages.set('user.enable', 'Enable user');
enMessages.set('user.disable', 'Disable user');
enMessages.set('user.enabled', 'User "{0}" has been enabled');
enMessages.set('user.disabled', 'User "{0}" has been disabled');
enMessages.set('user.deleted', 'User "{0}" has been deleted');
enMessages.set('user.failed-to-change-password', 'Failed to change password');
enMessages.set('user.failed-to-change-password-for', 'Failed to change password for "{0}"');
enMessages.set('user.failed-to-reset-password', 'Failed to reset password');
enMessages.set('user.failed-to-enable', 'Failed to enable user "{0}"');
enMessages.set('user.failed-to-disable', 'Failed to disable user "{0}"');
enMessages.set('user.failed-to-delete', 'Failed to delete user "{0}"');
enMessages.set('user.failed-to-get-profiles', 'Failed to get users');

enMessages.set('comment.comments', 'Comments');
enMessages.set('comment.leave-comment-here', 'Leave a comment here');
enMessages.set('comment.send', 'Send');
enMessages.set('comment.reply', 'Reply');
enMessages.set('comment.delete', 'Delete comment');
enMessages.set('comment.deleted', 'Comment has been deleted');
enMessages.set('comment.failed-to-add', 'Failed to add comment');
enMessages.set('comment.failed-to-update', 'Failed to update comment');
enMessages.set('comment.failed-to-like', 'Failed to like comment');
enMessages.set('comment.failed-to-dislike', 'Failed to dislike comment');
enMessages.set('comment.failed-to-delete', 'Failed to delete comment');

enMessages.set('project.reveal', 'Reveal');
enMessages.set('project.hide', 'Hide');
enMessages.set('project.show', 'Show');
enMessages.set('project.delete', 'Delete project');
enMessages.set('project.has-been-revealed', 'Project "{0}" has been revealed');
enMessages.set('project.has-been-hided', 'Project "{0}" has been hided');
enMessages.set('project.visible-to-users', 'Visible to users');
enMessages.set('project.hidden-from-users', 'Hidden from users');
enMessages.set('project.docker-compose-file', 'Docker compose file');
enMessages.set('project.deleted', 'Project "{0}" has been deleted');
enMessages.set('project.failed-to-like', 'Failed to like project');
enMessages.set('project.failed-to-dislike', 'Failed to dislike project');
enMessages.set('project.failed-to-reveal', 'Failed to reveal project "{0}"');
enMessages.set('project.failed-to-hide', 'Failed to hide project "{0}"');
enMessages.set('project.failed-to-delete', 'Failed to delete project "{0}"');
enMessages.set('project.description-elements.title', 'Title');
enMessages.set('project.description-elements.paragraph', 'Paragraph');
enMessages.set('project.description-elements.image', 'Image');
enMessages.set('project.description-elements.move-up', 'Move up');
enMessages.set('project.description-elements.move-down', 'Move down');
enMessages.set('project.failed-to-get-projects', 'Failed to get projects');
enMessages.set('project.manage', 'Manage project');
enMessages.set('project.author', 'Author');
enMessages.set('project.add-new', 'New');
enMessages.set('project.share', 'Share');
enMessages.set('project.copy-link', 'Copy link');
enMessages.set('project.share-on-vk', 'Share on VK');
enMessages.set('project.share-on-telegram', 'Share on Telegram');
enMessages.set('project.share-on-whatsapp', 'Share on WhatsApp');

enMessages.set('architecture', 'Architecture');
enMessages.set('architecture.deleted', 'Architecture "{0}" has been deleted');
enMessages.set('architecture.failed-to-delete', 'Failed to delete architecture "{0}"');
enMessages.set('technology.deleted', 'Technology "{0}" has been deleted');
enMessages.set('technology.failed-to-delete', 'Failed to delete technology "{0}"');

enMessages.set('info.characters-left', 'characters left');
enMessages.set('info.success', 'Success');
enMessages.set('info.error', 'Error');
enMessages.set('info.empty-image-elements', 'You have empty image elements');
enMessages.set('info.only-for-auth-users', 'Only for authenticated users');
enMessages.set('info.switch-to-light-theme', 'Switch to light theme');
enMessages.set('info.switch-to-dark-theme', 'Switch to dark theme');
enMessages.set('info.link-copied', 'Link copied');
enMessages.set('info.app-description', 'A platform for secure storage of your photos and videos.');
enMessages.set('info.failed-to-get-tags', 'Failed to get tags');

enMessages.set('profile.no-qualification', 'Qualification not defined as there are no projects');
enMessages.set('profile.no-projects', 'There are no projects yet');

enMessages.set('cancel', 'Cancel');
enMessages.set('like', 'Like');
enMessages.set('edit', 'Edit');
enMessages.set('delete', 'Delete');
enMessages.set('change-image', 'Change image');
enMessages.set('choose-image', 'Choose image');
enMessages.set('logo', 'Logo');
enMessages.set('login', 'Login');
enMessages.set('login', 'Login');

enMessages.set('album.failed-to-create', 'Failed to create album');
enMessages.set('album.failed-to-update', 'Failed to update album');
enMessages.set('album.failed-to-delete', 'Failed to update album "{0}"');
enMessages.set('album.created', 'Album "{0}" has been created');
enMessages.set('album.updated', 'Album "{0}" has been updated');
enMessages.set('album.deleted', 'Album "{0}" has been deleted');
enMessages.set('album.create', 'Create album');
enMessages.set('album.edit', 'Edit album');
enMessages.set('create', 'Create');
enMessages.set('save', 'Save');
enMessages.set('album.sure-to-delete', 'Do you really want to delete album "{0}"?');
enMessages.set('album.photos-will-be-delete', 'This action will delete all photos in the album!');
enMessages.set('photo.failed-to-create', 'Failed to upload photo');


ruMessages.set('user.passwords-not-match', 'пароли не совпадают');
ruMessages.set('user.change-password-for', 'Сменить пароль для "{0}"');
ruMessages.set('user.password-changed', 'Пароль сменен');
ruMessages.set('user.password-changed-for', 'Пароль для "{0}" был изменен');
ruMessages.set('user.enable', 'Активировать пользователя');
ruMessages.set('user.disable', 'Заблокировать пользователя');
ruMessages.set('user.enabled', 'Пользователь "{0}" был активирован');
ruMessages.set('user.disabled', 'Пользователь "{0}" был заблокирован');
ruMessages.set('user.deleted', 'Пользователь "{0}" был удален');
ruMessages.set('user.failed-to-change-password', 'Не удалось сменить пароль');
ruMessages.set('user.failed-to-change-password-for', 'Не удалось изменить пароль для "{0}"');
ruMessages.set('user.failed-to-reset-password', 'Не удалось сбросить пароль');
ruMessages.set('user.failed-to-enable', 'Не удалось активировать пользователя "{0}"');
ruMessages.set('user.failed-to-disable', 'Не удалось заблокировать пользователя "{0}"');
ruMessages.set('user.failed-to-delete', 'Не удалось удалить пользователя "{0}"');
ruMessages.set('user.failed-to-get-profiles', 'Не удалось загрузить профили');

ruMessages.set('comment.comments', 'Комментарии');
ruMessages.set('comment.leave-comment-here', 'Оставьте комментарий здесь');
ruMessages.set('comment.send', 'Отправить');
ruMessages.set('comment.reply', 'Ответить');
ruMessages.set('comment.delete', 'Удалить комментарий');
ruMessages.set('comment.deleted', 'Комментарий удален');
ruMessages.set('comment.failed-to-add', 'Не удалось добавить комментарий');
ruMessages.set('comment.failed-to-update', 'Не удалось обновить комментарий');
ruMessages.set('comment.failed-to-like', 'Не удалось лайкнуть комментарий');
ruMessages.set('comment.failed-to-dislike', 'Не удалось дизлайкнуть комментарий');
ruMessages.set('comment.failed-to-delete', 'Не удалось удалить комментарий');

ruMessages.set('project.reveal', 'Сделать видимым');
ruMessages.set('project.hide', 'Скрыть');
ruMessages.set('project.show', 'Показать');
ruMessages.set('project.delete', 'Удалить проект');
ruMessages.set('project.has-been-revealed', 'Проект "{0}" стал виден пользователям');
ruMessages.set('project.has-been-hided', 'Проект "{0}" был скрыт');
ruMessages.set('project.visible-to-users', 'Виден пользователям');
ruMessages.set('project.hidden-from-users', 'Скрыт от пользователей');
ruMessages.set('project.docker-compose-file', 'Docker compose файл');
ruMessages.set('project.deleted', 'Проект "{0}" был удален');
ruMessages.set('project.failed-to-like', 'Не удалось лайкнуть проект');
ruMessages.set('project.failed-to-dislike', 'Не удалось дизлайкнуть проект');
ruMessages.set('project.failed-to-reveal', 'Не удалось сделать видимым проект "{0}"');
ruMessages.set('project.failed-to-hide', 'Не удалось скрыть проект "{0}"');
ruMessages.set('project.failed-to-delete', 'Не удалось удалить проект "{0}"');
ruMessages.set('project.description-elements.title', 'Заголовок');
ruMessages.set('project.description-elements.paragraph', 'Абзац');
ruMessages.set('project.description-elements.image', 'Картинка');
ruMessages.set('project.description-elements.move-up', 'Сместить вверх');
ruMessages.set('project.description-elements.move-down', 'Сместить вниз');
ruMessages.set('project.failed-to-get-projects', 'Не удалось загрузить проекты');
ruMessages.set('project.manage', 'Управление проектом');
ruMessages.set('project.author', 'Автор');
ruMessages.set('project.add-new', 'Новый');
ruMessages.set('project.share', 'Поделиться');
ruMessages.set('project.copy-link', 'Скопировать ссылку');
ruMessages.set('project.share-on-vk', 'Поделиться в VK');
ruMessages.set('project.share-on-telegram', 'Поделиться в Telegram');
ruMessages.set('project.share-on-whatsapp', 'Поделиться в WhatsApp');

ruMessages.set('architecture', 'Архитектура');
ruMessages.set('architecture.deleted', 'Архитектура "{0}" была удалена');
ruMessages.set('architecture.failed-to-delete', 'Не удалось удалить архитектуру "{0}"');
ruMessages.set('technology.deleted', 'Технология "{0}" была удалена');
ruMessages.set('technology.failed-to-delete', 'Не удалось удалить технологию "{0}"');

ruMessages.set('info.characters-left', 'символов осталось');
ruMessages.set('info.success', 'Успешно');
ruMessages.set('info.error', 'Ошибка');
ruMessages.set('info.empty-image-elements', 'У вас есть пустые элементы-картинки');
ruMessages.set('info.only-for-auth-users', 'Только для авторизованных пользователей');
ruMessages.set('info.switch-to-light-theme', 'Включить светлую тему');
ruMessages.set('info.switch-to-dark-theme', 'Включить темную тему');
ruMessages.set('info.link-copied', 'Ссылка скопирована');
ruMessages.set('info.app-description', 'Платформа для безопасного хранения ваших фотографий и видеозаписей.');
ruMessages.set('info.failed-to-get-tags', 'Не удалось загрузить тэги');

ruMessages.set('profile.no-qualification', 'Квалификация не определена, так как нет проектов');
ruMessages.set('profile.no-projects', 'Проектов пока нет');

ruMessages.set('cancel', 'Отмена');
ruMessages.set('like', 'Нравится');
ruMessages.set('edit', 'Редактировать');
ruMessages.set('delete', 'Удалить');
ruMessages.set('change-image', 'Сменить картинку');
ruMessages.set('choose-image', 'Выбрать картинку');
ruMessages.set('logo', 'Логотип');
ruMessages.set('login', 'Вход');

ruMessages.set('album.failed-to-create', 'Не удалось создать альбом');
ruMessages.set('album.failed-to-update', 'Не удалось обновить альбом');
ruMessages.set('album.failed-to-delete', 'Не удалось удалить альбом "{0}"');
ruMessages.set('album.created', 'Альбом "{0}" создан');
ruMessages.set('album.updated', 'Альбом "{0}" обновлен');
ruMessages.set('album.deleted', 'Альбом "{0}" удален');
ruMessages.set('album.create', 'Создать альбом');
ruMessages.set('album.edit', 'Редактировать альбом');
ruMessages.set('create', 'Создать');
ruMessages.set('save', 'Сохранить');
ruMessages.set('album.sure-to-delete', 'Вы уверены, что хотите удалить альбом "{0}"?');
ruMessages.set('album.photos-will-be-delete', 'Это действие приведет к удалению всех фотографий в альбоме!');
ruMessages.set('photo.failed-to-create', 'Не удалось загрузить фото');


function getMessage(messageCode, args) {
    let message = locale === 'ru' ? ruMessages.get(messageCode) : enMessages.get(messageCode);
    if (args != null) {
        for (let i = 0; i < args.length; i++) {
            message = message.replace(`{${i}}`, args[i]);
        }
    }
    return message;
}
