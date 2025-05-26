package ru.javaprojects.picnest.common.validation;

import lombok.experimental.UtilityClass;
import ru.javaprojects.picnest.common.HasId;
import ru.javaprojects.picnest.common.error.IllegalRequestDataException;

@UtilityClass
public class ValidationUtil {

    public static void checkNew(HasId bean) {
        if (!bean.isNew()) {
            throw new IllegalRequestDataException(bean.getClass().getSimpleName() + " must be new (id=null)",
                    "error.has-id.must-be-new", new Object[]{bean.getClass().getSimpleName()});
        }
    }

    public static void checkNotNew(HasId bean) {
        if (bean.isNew()) {
            throw new IllegalRequestDataException(bean.getClass().getSimpleName() + " must not be new (id!=null)",
                    "error.has-id.must-not-be-new", new Object[]{bean.getClass().getSimpleName()});
        }
    }
}
