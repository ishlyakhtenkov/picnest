const selectors = $('.selectpicker');
$.each(selectors, function (i, selector) {
    let selectorLabel = null;
    let selectorFormControl = null;
    let selectorTitle = null;

    $(selector).on('loaded.bs.select', () => {
        selectorLabel = $(selector).parent().parent().find('.selector-label');
        selectorFormControl = $(selector).parent();
        selectorTitle = $(selector).parent().find('.filter-option-inner-inner');

        $('.dropdown-toggle.btn-white').addClass('border-0 pt-0').attr('style', 'outline: none !important');
        if ($(selector).val().length !== 0) {
            selectorTitle.removeClass('text-muted');
            selectorLabel.attr('hidden', false);
            selectorFormControl.removeClass('pt-2').addClass('pt-3');
        } else {
            selectorTitle.addClass('text-muted');
        }
    })

    $(selector).on('shown.bs.select', () => {
        selectorFormControl.addClass('border-primary-subtle')
            .attr('style', 'box-shadow: 0 0 0 0.25rem rgba(0, 110, 255, 0.25)');
        selectorLabel.attr('hidden', false);
        if ($(selector).val().length === 0) {
            selectorTitle.html('&nbsp;');
        } else {
            selectorTitle.removeClass('text-muted');
        }
    })

    $(selector).on('changed.bs.select', () => {
        if ($(selector).val().length === 0) {
            selectorFormControl.removeClass('pt-3').addClass('pt-2');
            selectorTitle.addClass('text-muted');
            selectorTitle.html('&nbsp;');
        } else {
            selectorTitle.removeClass('text-muted');
            selectorFormControl.removeClass('pt-2').addClass('pt-3');
            selectorLabel.attr('hidden', false);
        }
    })

    $(selector).on('hide.bs.select', () => {
        selectorFormControl.removeClass('border-primary-subtle').attr('style', '');
        if ($(selector).val().length === 0) {
            selectorLabel.attr('hidden', true);
            selectorTitle.addClass('text-muted').html($(selector).prop('title'));
        }
    })
})
