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

enMessages.set('info.characters-left', 'characters left');
enMessages.set('info.success', 'Success');
enMessages.set('info.error', 'Error');
enMessages.set('info.only-for-auth-users', 'Only for authenticated users');
enMessages.set('info.switch-to-light-theme', 'Switch to light theme');
enMessages.set('info.switch-to-dark-theme', 'Switch to dark theme');
enMessages.set('info.link-copied', 'Link copied');
enMessages.set('info.app-description', 'A platform for secure storage of your pictures.');
enMessages.set('info.failed-to-get-tags', 'Failed to get tags');

enMessages.set('cancel', 'Cancel');
enMessages.set('like', 'Like');
enMessages.set('edit', 'Edit');
enMessages.set('delete', 'Delete');
enMessages.set('change-image', 'Change image');
enMessages.set('choose-image', 'Choose image');
enMessages.set('logo', 'Logo');
enMessages.set('login', 'Login');
enMessages.set('login', 'Login');
enMessages.set('download', 'Download');
enMessages.set('actions', 'Actions');

enMessages.set('album.failed-to-create', 'Failed to create album');
enMessages.set('album.failed-to-update', 'Failed to update album');
enMessages.set('album.failed-to-delete', 'Failed to delete album "{0}"');
enMessages.set('album.created', 'Album "{0}" has been created');
enMessages.set('album.updated', 'Album "{0}" has been updated');
enMessages.set('album.deleted', 'Album "{0}" has been deleted');
enMessages.set('album.create', 'Create album');
enMessages.set('album.edit', 'Edit album');
enMessages.set('create', 'Create');
enMessages.set('save', 'Save');
enMessages.set('album.sure-to-delete', 'Do you really want to delete album "{0}"?');
enMessages.set('album.pictures-will-be-delete', 'This action will delete all pictures in the album!');
enMessages.set('picture.failed-to-create', 'Failed to upload picture');
enMessages.set('picture.pictures', 'pictures');
enMessages.set('picture.deleted', 'Picture has been deleted');
enMessages.set('picture.failed-to-delete', 'Failed to delete picture');


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

ruMessages.set('info.characters-left', 'символов осталось');
ruMessages.set('info.success', 'Успешно');
ruMessages.set('info.error', 'Ошибка');
ruMessages.set('info.only-for-auth-users', 'Только для авторизованных пользователей');
ruMessages.set('info.switch-to-light-theme', 'Включить светлую тему');
ruMessages.set('info.switch-to-dark-theme', 'Включить темную тему');
ruMessages.set('info.link-copied', 'Ссылка скопирована');
ruMessages.set('info.app-description', 'Платформа для безопасного хранения ваших фотографий.');
ruMessages.set('info.failed-to-get-tags', 'Не удалось загрузить тэги');

ruMessages.set('cancel', 'Отмена');
ruMessages.set('like', 'Нравится');
ruMessages.set('edit', 'Редактировать');
ruMessages.set('delete', 'Удалить');
ruMessages.set('change-image', 'Сменить картинку');
ruMessages.set('choose-image', 'Выбрать картинку');
ruMessages.set('logo', 'Логотип');
ruMessages.set('login', 'Вход');
ruMessages.set('download', 'Скачать');
ruMessages.set('actions', 'Действия');

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
ruMessages.set('album.pictures-will-be-delete', 'Это действие приведет к удалению всех фотографий в альбоме!');
ruMessages.set('picture.failed-to-create', 'Не удалось загрузить фото');
ruMessages.set('picture.pictures', 'фото');
ruMessages.set('picture.deleted', 'Фото удалено');
ruMessages.set('picture.failed-to-delete', 'Не удалось удалить фото');


function getMessage(messageCode, args) {
    let message = locale === 'ru' ? ruMessages.get(messageCode) : enMessages.get(messageCode);
    if (args != null) {
        for (let i = 0; i < args.length; i++) {
            message = message.replace(`{${i}}`, args[i]);
        }
    }
    return message;
}
