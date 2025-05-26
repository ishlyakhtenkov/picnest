const selectors = $('.selectpicker');
$.each(selectors, function (i, selector) {
    $(selector).on('loaded.bs.select', () => {
        $('.dropdown-toggle.btn-white').addClass('border-0 ').attr('style', 'outline: none !important')
        if ($(selector).val().length !== 0) {
            $(selector).parent().find('.filter-option-inner-inner').removeClass('text-muted');
        } else {
            $(selector).parent().find('.filter-option-inner-inner').addClass('text-muted');
        }
    })

    $(selector).on('shown.bs.select', () => {
        $(selector).parent().addClass('border-primary-subtle').attr('style', 'box-shadow: 0 0 0 0.25rem rgba(0, 110, 255, 0.25)');
    })

    $(selector).on('changed.bs.select', () => {
        $(selector).parent().find('.filter-option-inner-inner').removeClass('text-muted');
    })

    $(selector).on('hide.bs.select', () => {
        $(selector).parent().removeClass('border-primary-subtle').attr('style', '');
    })
})
